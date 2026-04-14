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
        }
        return SupportedTemplateTextMapper.map(result.text, plan.requestedFields)
    }
}

private object SupportedTemplateTextMapper {
    private val laneNames = listOf("发育路", "对抗路", "中路", "打野", "游走")
    private val labelsByField = mapOf(
        FieldKey.DAMAGE_DEALT to listOf("对英雄输出"),
        FieldKey.DAMAGE_SHARE to listOf("输出占比"),
        FieldKey.DAMAGE_TAKEN to listOf("承受英雄伤害"),
        FieldKey.DAMAGE_TAKEN_SHARE to listOf("承伤占比"),
        FieldKey.TOTAL_GOLD to listOf("经济"),
        FieldKey.GOLD_SHARE to listOf("经济占比"),
        FieldKey.GOLD_FROM_FARMING to listOf("打野经济"),
        FieldKey.LAST_HITS to listOf("补刀数"),
        FieldKey.PARTICIPATION_RATE to listOf("参团率"),
        FieldKey.CONTROL_DURATION to listOf("控制时长"),
        FieldKey.DAMAGE_DEALT_TO_OPPONENTS to listOf("对塔伤害")
    )

    fun map(text: String, requestedFields: Set<FieldKey>): ScreenshotAnalysis {
        val normalizedText = text
            .replace('％', '%')
            .replace('：', ' ')
            .replace(Regex("""[ \t]+"""), " ")
        val lines = normalizedText.lines().map(String::trim).filter(String::isNotEmpty)
        val rawValues = mutableMapOf<FieldKey, String>()

        firstMatch(normalizedText, Regex("""(胜利|失败)"""))?.let {
            rawValues[FieldKey.RESULT] = it
        }
        firstMatch(normalizedText, Regex("""(\d+)\s*vs\s*(\d+)""", RegexOption.IGNORE_CASE))?.let {
            val score = Regex("""(\d+)\s*vs\s*(\d+)""", RegexOption.IGNORE_CASE).find(it)
            if (score != null) {
                rawValues[FieldKey.SCORE] = "${score.groupValues[1]} vs ${score.groupValues[2]}"
            }
        }
        firstMatch(normalizedText, Regex("""\d+/\d+/\d+"""))?.let {
            rawValues[FieldKey.KDA] = it
        }

        laneNames.firstOrNull { normalizedText.contains(it) }?.let { lane ->
            rawValues[FieldKey.LANE] = lane
            val playerLine = lines.firstOrNull { it.contains(lane) && Regex("""\d+/\d+/\d+""").containsMatchIn(it) }
            playerLine
                ?.substringBefore(lane)
                ?.replace(Regex("""[^\p{IsHan}、A-Za-z0-9]"""), " ")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { rawValues[FieldKey.PLAYER_NAME] = it }
        }

        labelsByField.forEach { (fieldKey, labels) ->
            if (fieldKey !in requestedFields) {
                return@forEach
            }
            labels.firstNotNullOfOrNull { label ->
                extractValueAfterLabel(lines, label)
            }?.let { rawValues[fieldKey] = it }
        }

        val anchors = buildSet {
            if (FieldKey.RESULT in rawValues) {
                add(Anchor.RESULT_HEADER)
            }
            if (normalizedText.contains("数据")) {
                add(Anchor.DATA_TAB_SELECTED)
            }
            if (FieldKey.KDA in rawValues && FieldKey.LANE in rawValues) {
                add(Anchor.SUMMARY_CARD)
            }
        }

        val visibleSections = buildSet {
            if (normalizedText.contains("对英雄输出") || normalizedText.contains("输出占比")) {
                add(Section.DAMAGE)
            }
            if (normalizedText.contains("承受英雄伤害") || normalizedText.contains("承伤占比")) {
                add(Section.DAMAGE_TAKEN)
            }
            if (normalizedText.contains("经济") || normalizedText.contains("补刀数")) {
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
            visibleFields = rawValues.keys,
            rawValues = rawValues,
            lowConfidenceFields = emptySet()
        )
    }

    private fun firstMatch(text: String, pattern: Regex): String? {
        return pattern.find(text)?.value
    }

    private fun extractValueAfterLabel(lines: List<String>, label: String): String? {
        val pattern = Regex("""${Regex.escape(label)}\s*([0-9]+(?:\.[0-9]+)?(?:k|%|s)?)""", RegexOption.IGNORE_CASE)
        return lines.firstNotNullOfOrNull { line ->
            pattern.find(line)?.groupValues?.getOrNull(1)
        }
    }
}
