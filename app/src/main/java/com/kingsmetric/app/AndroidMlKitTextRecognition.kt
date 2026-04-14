package com.kingsmetric.app

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.OcrExtractionException
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import java.io.File

class AndroidBitmapLoader : BitmapLoader {
    override fun load(path: String): LoadedBitmap {
        val file = File(path)
        if (!file.exists()) {
            throw BitmapDecodeException()
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath) ?: throw BitmapDecodeException()
        decoded.recycle()
        return LoadedBitmap(path)
    }
}

class AndroidMlKitTextRecognizer(
    private val context: Context
) : MlKitRecognizer {
    override fun recognize(bitmap: LoadedBitmap, plan: RecognitionRegionPlan): ScreenshotAnalysis {
        val image = try {
            InputImage.fromFilePath(context, Uri.fromFile(File(bitmap.path)))
        } catch (_: Exception) {
            throw OcrExtractionException("could not open image")
        }

        val recognizer = TextRecognition.getClient(
            ChineseTextRecognizerOptions.Builder().build()
        )
        val result = try {
            Tasks.await(recognizer.process(image))
        } catch (_: Exception) {
            throw OcrExtractionException("ocr failed")
        } finally {
            recognizer.close()
        }
        return SupportedTemplateTextMapper.map(result.text, plan.requestedFields)
    }
}

private object SupportedTemplateTextMapper {
    private val laneNames = listOf("发育路", "对抗路", "中路", "打野", "游走")

    fun map(text: String, requestedFields: Set<FieldKey>): ScreenshotAnalysis {
        val normalizedText = normalize(text)
        val lines = normalizedText.lines().map(String::trim).filter(String::isNotEmpty)
        val rawValues = mutableMapOf<FieldKey, String>()

        match(normalizedText, Regex("""(胜利|失败)"""))?.let {
            rawValues[FieldKey.RESULT] = it
        }
        Regex("""(\d+)\s*vs\s*(\d+)""", RegexOption.IGNORE_CASE).find(normalizedText)?.let { score ->
            rawValues[FieldKey.SCORE] = "${score.groupValues[1]} vs ${score.groupValues[2]}"
        }
        match(normalizedText, Regex("""\d+/\d+/\d+"""))?.let {
            rawValues[FieldKey.KDA] = it
        }

        laneNames.firstOrNull(normalizedText::contains)?.let { lane ->
            rawValues[FieldKey.LANE] = lane
        }
        extractPlayerName(lines)?.let { rawValues[FieldKey.PLAYER_NAME] = it }
        extractTotalGold(normalizedText)?.let { rawValues[FieldKey.TOTAL_GOLD] = it }

        extractWithImmediateThenBounded(normalizedText, "对英雄输出", """[0-9]+(?:\.[0-9]+)?k""")
            ?: extractWithImmediateThenBounded(normalizedText, "对英雄輸出", """[0-9]+(?:\.[0-9]+)?k""")
        ?.let { rawValues[FieldKey.DAMAGE_DEALT] = it }

        extractWithImmediateThenBounded(normalizedText, "输出占比", """[0-9]+(?:\.[0-9]+)?%""")
            ?.let { rawValues[FieldKey.DAMAGE_SHARE] = it }

        extractWithImmediateThenBounded(normalizedText, "承受英雄伤害", """[0-9]+(?:\.[0-9]+)?k""")
            ?.let { rawValues[FieldKey.DAMAGE_TAKEN] = it }

        extractWithImmediateThenBounded(normalizedText, "承伤占比", """[0-9]+(?:\.[0-9]+)?%""")
            ?.let { rawValues[FieldKey.DAMAGE_TAKEN_SHARE] = it }

        extractLastBounded(normalizedText, "经济占比", """[0-9]+(?:\.[0-9]+)?%""")
            ?.let { rawValues[FieldKey.GOLD_SHARE] = it }

        extractWithImmediateThenBounded(normalizedText, "打野经济", """[0-9]+(?:\.[0-9]+)?k""")
            ?.let { rawValues[FieldKey.GOLD_FROM_FARMING] = it }

        extractWithImmediateThenBounded(normalizedText, "补刀数", """[0-9]+""")
            ?.let { rawValues[FieldKey.LAST_HITS] = it }

        extractLastBounded(normalizedText, "参团率", """[0-9]+(?:\.[0-9]+)?%""")
            ?.let { rawValues[FieldKey.PARTICIPATION_RATE] = it }

        extractLastBounded(normalizedText, "控制时长", """[0-9]+(?:\.[0-9]+)?s""")
            ?.let { rawValues[FieldKey.CONTROL_DURATION] = it }

        extractWithImmediateThenBounded(normalizedText, "对塔伤害", """[0-9]+(?:\.[0-9]+)?k""")
            ?.let { rawValues[FieldKey.DAMAGE_DEALT_TO_OPPONENTS] = it }

        val visibleFields = rawValues.keys.intersect(requestedFields)
        val filteredValues = rawValues.filterKeys { it in requestedFields }

        val anchors = buildSet {
            if (FieldKey.RESULT in filteredValues) {
                add(Anchor.RESULT_HEADER)
            }
            if (normalizedText.contains("数据")) {
                add(Anchor.DATA_TAB_SELECTED)
            }
            if (FieldKey.KDA in filteredValues && (FieldKey.LANE in filteredValues || FieldKey.PLAYER_NAME in filteredValues)) {
                add(Anchor.SUMMARY_CARD)
            }
        }

        val visibleSections = buildSet {
            if (normalizedText.contains("对英雄输出") || normalizedText.contains("对英雄輸出") || normalizedText.contains("输出占比")) {
                add(Section.DAMAGE)
            }
            if (normalizedText.contains("承受英雄伤害") || normalizedText.contains("承伤占比")) {
                add(Section.DAMAGE_TAKEN)
            }
            if (normalizedText.contains("经济占比") || normalizedText.contains("总经济") || normalizedText.contains("打野经济")) {
                add(Section.ECONOMY)
            }
            if (normalizedText.contains("参团率") || normalizedText.contains("控制时长")) {
                add(Section.TEAM_PARTICIPATION)
            }
        }

        return ScreenshotAnalysis(
            anchors = anchors,
            visibleSections = visibleSections,
            languageCode = if (normalizedText.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN }) {
                "zh-CN"
            } else {
                "und"
            },
            visibleFields = visibleFields,
            rawValues = filteredValues,
            lowConfidenceFields = emptySet()
        )
    }

    private fun normalize(text: String): String {
        return text
            .replace('：', ':')
            .replace('，', ' ')
            .replace(Regex("""[ \t]+"""), " ")
    }

    private fun match(text: String, pattern: Regex): String? = pattern.find(text)?.value

    private fun extractPlayerName(lines: List<String>): String? {
        val sourceLine = lines.firstOrNull { Regex("""\d+/\d+/\d+""").containsMatchIn(it) } ?: return null
        val prefix = Regex("""^(.*?)(?:\d+(?:\.\d+)?\s+\d+/\d+/\d+|\d+/\d+/\d+).*$""")
            .matchEntire(sourceLine)
            ?.groupValues
            ?.getOrNull(1)
            ?: sourceLine

        return prefix
            .replace(Regex("""[^\p{IsHan}A-Za-z0-9、·]"""), " ")
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    private fun extractTotalGold(text: String): String? {
        val summaryGold = Regex("""[\p{IsHan}A-Za-z0-9、·]+\s+([0-9]+(?:\.[0-9]+)?)\s+\d+/\d+/\d+""")
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
        if (summaryGold != null) {
            return summaryGold
        }

        return extractWithImmediateThenBounded(text, "总经济", """[0-9]+(?:\.[0-9]+)?k""")
    }

    private fun extractWithImmediateThenBounded(
        text: String,
        label: String,
        valuePattern: String
    ): String? {
        val immediatePattern = Regex(
            """${Regex.escape(label)}\s*[:：]?\s*($valuePattern)""",
            RegexOption.IGNORE_CASE
        )
        immediatePattern.find(text)?.groupValues?.getOrNull(1)?.let { return it }

        val boundedPattern = Regex(
            """${Regex.escape(label)}.{0,24}?($valuePattern)""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        return boundedPattern.find(text)?.groupValues?.getOrNull(1)
    }

    private fun extractLastBounded(
        text: String,
        label: String,
        valuePattern: String
    ): String? {
        val immediatePattern = Regex(
            """${Regex.escape(label)}\s*[:：]?\s*($valuePattern)""",
            RegexOption.IGNORE_CASE
        )
        immediatePattern.find(text)?.groupValues?.getOrNull(1)?.let { return it }

        val labelPattern = Regex(Regex.escape(label), RegexOption.IGNORE_CASE)
        val valueRegex = Regex(valuePattern, RegexOption.IGNORE_CASE)
        return labelPattern.findAll(text).lastOrNull()?.range?.last?.plus(1)?.let { startIndex ->
            val window = text.substring(startIndex, (startIndex + 64).coerceAtMost(text.length))
            valueRegex.findAll(window).lastOrNull()?.value
        }
    }
}
