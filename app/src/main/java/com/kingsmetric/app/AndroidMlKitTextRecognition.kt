package com.kingsmetric.app

import android.content.Context
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
    private val boundsReader = AndroidImageBoundsReader()

    override fun load(path: String): LoadedBitmap {
        val file = File(path)
        if (!file.exists()) {
            throw BitmapDecodeException()
        }
        val bounds = boundsReader.read(file.absolutePath)
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw BitmapDecodeException()
        }
        return LoadedBitmap(path)
    }
}

class AndroidMlKitTextRecognizer(
    private val context: Context
) : MlKitRecognizer {
    override fun recognize(bitmap: LoadedBitmap, plan: RecognitionRegionPlan): RecognitionOutput {
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
        val ocrText = result.text
        val analysis = try {
            SupportedTemplateTextMapper.map(ocrText, plan.requestedFields)
        } catch (exception: Exception) {
            throw OcrExtractionException(
                message = exception.message ?: "ocr mapping failed",
                ocrText = ocrText
            )
        }
        return RecognitionOutput(
            analysis = analysis,
            ocrText = ocrText
        )
    }
}

internal object SupportedTemplateTextMapper {
    private val resultLabels = listOf("胜利", "失败")
    private val resultPattern = Regex(resultLabels.joinToString("|") { Regex.escape(it) })
    private val laneNames = listOf("发育路", "对抗路", "中路", "打野", "游走")
    private val laneAliases = listOf(
        "发育路" to listOf("发育路", "发有路"),
        "对抗路" to listOf("对抗路"),
        "中路" to listOf("中路"),
        "打野" to listOf("打野"),
        "游走" to listOf("游走")
    )
    private val allLaneLabels = laneAliases.flatMap { it.second }.distinct()
    private val dataTabLabels = listOf("数据")
    private val damageDealtLabels = listOf("对英雄输出", "对英雄輸出", "对英雄出", "输出伤害", "輸出伤害", "输出")
    private val damageShareLabels = listOf("输出占比", "輸出占比")
    private val damageTakenLabels = listOf("承受英雄伤害", "总承受伤害", "承受伤害", "承伤")
    private val damageTakenShareLabels = listOf("承伤占比")
    private val economyLabels = listOf("总经济", "经济")
    private val goldShareLabels = listOf("经济占比")
    private val farmingGoldLabels = listOf("打野经济")
    private val lastHitsLabels = listOf("补刀数", "补刀")
    private val participationLabels = listOf("参团率", "团率")
    private val controlDurationLabels = listOf("控制时长")
    private val towerDamageLabels = listOf("对塔伤害")
    private val playerSummaryExclusionLabels = listOf(
        "总览",
        "复盘",
        "我方胜利",
        "关键团战输出",
        "个人数据",
        "同队对比",
        "对位对比",
        "生成精彩时刻",
        "金牌",
        "MVP",
        "输出",
        "承伤",
        "经济",
        "团队"
    )
    private val ratingPattern = Regex("""\b\d+(?:\.\d+)\b""")
    private val kdaPattern = Regex("""(\d{1,4}/\d{1,2}/\d{1,2})""")

    fun map(
        text: String,
        requestedFields: Set<FieldKey>,
        playerSummaryLineExtractor: (List<String>) -> String? = ::extractPlayerSummaryLine
    ): ScreenshotAnalysis {
        val normalizedText = normalize(text)
        val lines = normalizedText.lines().map(String::trim).filter(String::isNotEmpty)
        val rawValues = mutableMapOf<FieldKey, String>()
        val playerSummaryLine = runCatching { playerSummaryLineExtractor(lines) }.getOrNull()

        fun <T> safeExtract(extractor: () -> T?): T? {
            return runCatching(extractor).getOrNull()
        }

        safeExtract { match(normalizedText, resultPattern) }?.let { rawValues[FieldKey.RESULT] = it }
        Regex("""(\d+)\s*vs\s*(\d+)""", RegexOption.IGNORE_CASE).find(normalizedText)?.let { score ->
            rawValues[FieldKey.SCORE] = "${score.groupValues[1]} vs ${score.groupValues[2]}"
        }
        safeExtract { extractKda(lines) }?.let { rawValues[FieldKey.KDA] = it }

        safeExtract { extractLane(lines, playerSummaryLine) }?.let { rawValues[FieldKey.LANE] = it }
        safeExtract { extractPlayerName(playerSummaryLine) }?.let { rawValues[FieldKey.PLAYER_NAME] = it }
        safeExtract { extractTotalGold(playerSummaryLine, lines) }?.let { rawValues[FieldKey.TOTAL_GOLD] = it }

        safeExtract {
            extractFirstLabeledValue(lines, damageDealtLabels, """[0-9]+(?:\.[0-9]+)?k""")
                ?: extractFirstLabeledValue(lines, damageShareLabels, """[0-9]+(?:\.[0-9]+)?k""")
        }?.let { rawValues[FieldKey.DAMAGE_DEALT] = it }
        safeExtract {
            extractFirstLabeledValue(lines, damageShareLabels, """[0-9]+(?:\.[0-9]+)?%""")
                ?: extractFirstLabeledValue(lines, damageDealtLabels, """[0-9]+(?:\.[0-9]+)?%""")
        }?.let { rawValues[FieldKey.DAMAGE_SHARE] = it }
        safeExtract {
            extractFirstLabeledValue(lines, damageTakenLabels, """[0-9]+(?:\.[0-9]+)?k""")
                ?: extractFirstLabeledValue(lines, damageTakenShareLabels, """[0-9]+(?:\.[0-9]+)?k""")
        }?.let { rawValues[FieldKey.DAMAGE_TAKEN] = it }
        safeExtract {
            extractFirstLabeledValue(lines, damageTakenShareLabels, """[0-9]+(?:\.[0-9]+)?%""")
                ?: extractFirstLabeledValue(lines, damageTakenLabels, """[0-9]+(?:\.[0-9]+)?%""")
        }?.let { rawValues[FieldKey.DAMAGE_TAKEN_SHARE] = it }
        safeExtract { extractLastLabeledValue(normalizedText, goldShareLabels, """[0-9]+(?:\.[0-9]+)?%""") }
            ?.let { rawValues[FieldKey.GOLD_SHARE] = it }
        safeExtract { extractLastLabeledValue(normalizedText, farmingGoldLabels, """[0-9]+(?:\.[0-9]+)?k""") }
            ?.let { rawValues[FieldKey.GOLD_FROM_FARMING] = it }
        safeExtract { extractLastLabeledValue(normalizedText, lastHitsLabels, """[0-9]+""") }
            ?.let { rawValues[FieldKey.LAST_HITS] = it }
        safeExtract { extractLastLabeledValue(normalizedText, participationLabels, """[0-9]+(?:\.[0-9]+)?%""") }
            ?.let { rawValues[FieldKey.PARTICIPATION_RATE] = it }
        safeExtract { extractLastLabeledValue(normalizedText, controlDurationLabels, """[0-9]+(?:\.[0-9]+)?s""") }
            ?.let { rawValues[FieldKey.CONTROL_DURATION] = it }
        safeExtract { extractLastLabeledValue(normalizedText, towerDamageLabels, """[0-9]+(?:\.[0-9]+)?k""") }
            ?.let { rawValues[FieldKey.DAMAGE_DEALT_TO_OPPONENTS] = it }

        val visibleFields = rawValues.keys.intersect(requestedFields)
        val filteredValues = rawValues.filterKeys { it in requestedFields }

        val anchors = buildSet {
            if (FieldKey.RESULT in filteredValues) {
                add(Anchor.RESULT_HEADER)
            }
            if (containsAny(normalizedText, dataTabLabels)) {
                add(Anchor.DATA_TAB_SELECTED)
            }
            if (FieldKey.KDA in filteredValues && (FieldKey.LANE in filteredValues || FieldKey.PLAYER_NAME in filteredValues)) {
                add(Anchor.SUMMARY_CARD)
            }
        }

        val visibleSections = buildSet {
            if (FieldKey.DAMAGE_DEALT in rawValues || FieldKey.DAMAGE_SHARE in rawValues ||
                containsAny(normalizedText, damageDealtLabels + damageShareLabels)
            ) {
                add(Section.DAMAGE)
            }
            if (FieldKey.DAMAGE_TAKEN in rawValues || FieldKey.DAMAGE_TAKEN_SHARE in rawValues ||
                containsAny(normalizedText, damageTakenLabels + damageTakenShareLabels)
            ) {
                add(Section.DAMAGE_TAKEN)
            }
            if (FieldKey.TOTAL_GOLD in rawValues || FieldKey.GOLD_SHARE in rawValues ||
                containsAny(normalizedText, economyLabels + goldShareLabels + farmingGoldLabels)
            ) {
                add(Section.ECONOMY)
            }
            if (FieldKey.PARTICIPATION_RATE in rawValues || FieldKey.CONTROL_DURATION in rawValues ||
                containsAny(normalizedText, participationLabels + controlDurationLabels)
            ) {
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
            .replace('｜', '|')
            .replace('\u3000', ' ')
            .replace(Regex("""[ \t]+"""), " ")
    }

    private fun match(text: String, pattern: Regex): String? = pattern.find(text)?.value

    private fun containsAny(text: String, labels: List<String>): Boolean = labels.any(text::contains)

    private fun extractPlayerSummaryLine(lines: List<String>): String? {
        return lines.firstOrNull { line ->
            ratingPattern.containsMatchIn(line) &&
                line.any { Character.UnicodeScript.of(it.code) == Character.UnicodeScript.HAN } &&
                !containsAny(line, playerSummaryExclusionLabels)
        }
    }

    private fun extractPlayerName(playerSummaryLine: String?): String? {
        val line = playerSummaryLine ?: return null
        val prefix = ratingPattern.find(line)?.let { rating ->
            line.substring(0, rating.range.first)
        } ?: line.substringBeforeLast('/')
        val cleaned = allLaneLabels.fold(prefix) { current, lane -> current.replace(lane, " ") }

        return cleaned
            .replace(Regex("""[^\p{IsHan}A-Za-z0-9、]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    private fun extractTotalGold(playerSummaryLine: String?, lines: List<String>): String? {
        val summaryGold = playerSummaryLine?.let { line ->
            ratingPattern.find(line)?.value
        }
        if (summaryGold != null) {
            return summaryGold
        }
        return extractFirstLabeledValue(lines, economyLabels, """[0-9]+(?:\.[0-9]+)?k?""")
    }

    private fun extractKda(lines: List<String>): String? {
        val candidate = lines.asReversed()
            .mapNotNull { line ->
                kdaPattern.findAll(line).lastOrNull()?.groupValues?.getOrNull(1)
            }
            .firstOrNull()
            ?: return null

        val parts = candidate.split('/')
        if (parts.size != 3) {
            return null
        }
        return "${parts[0].takeLast(2)}/${parts[1]}/${parts[2]}"
    }

    private fun extractLane(lines: List<String>, playerSummaryLine: String?): String? {
        canonicalLaneFor(playerSummaryLine)?.let { return it }
        lines.forEach { line ->
            canonicalLaneFor(line)?.let { return it }
        }
        return null
    }

    private fun canonicalLaneFor(text: String?): String? {
        if (text.isNullOrBlank()) {
            return null
        }
        return laneAliases.firstNotNullOfOrNull { (canonicalLane, aliases) ->
            if (!aliases.any(text::contains)) {
                return@firstNotNullOfOrNull null
            }
            if (canonicalLane == "打野" && farmingGoldLabels.any(text::contains)) {
                return@firstNotNullOfOrNull null
            }
            canonicalLane
        }
    }

    private fun extractFirstLabeledValue(
        lines: List<String>,
        labels: List<String>,
        valuePattern: String
    ): String? {
        labels.forEach { label ->
            lines.forEachIndexed { index, line ->
                if (!line.contains(label)) {
                    return@forEachIndexed
                }
                extractValueFromWindow(
                    label = label,
                    window = collectWindow(lines, index, 3),
                    valuePattern = valuePattern,
                    preferLastMatch = false
                )?.let { return it }
            }
        }
        return null
    }

    private fun extractLastLabeledValue(
        text: String,
        labels: List<String>,
        valuePattern: String
    ): String? {
        labels.forEach { label ->
            val immediatePattern = Regex(
                """${Regex.escape(label)}\s*[:]?\s*($valuePattern)""",
                RegexOption.IGNORE_CASE
            )
            immediatePattern.find(text)?.groupValues?.getOrNull(1)?.let { return it }
        }

        val valueRegex = Regex(valuePattern, RegexOption.IGNORE_CASE)
        labels.forEach { label ->
            val labelPattern = Regex(Regex.escape(label), RegexOption.IGNORE_CASE)
            labelPattern.findAll(text).lastOrNull()?.range?.last?.plus(1)?.let { startIndex ->
                val window = text.substring(startIndex, (startIndex + 64).coerceAtMost(text.length))
                valueRegex.findAll(window).lastOrNull()?.value?.let { return it }
            }
        }
        return null
    }

    private fun collectWindow(lines: List<String>, startIndex: Int, lookaheadLines: Int): String {
        val endExclusive = (startIndex + lookaheadLines).coerceAtMost(lines.size)
        return lines.subList(startIndex, endExclusive).joinToString(" ")
    }

    private fun extractValueFromWindow(
        label: String,
        window: String,
        valuePattern: String,
        preferLastMatch: Boolean
    ): String? {
        val immediatePattern = Regex(
            """${Regex.escape(label)}\s*[:]?\s*($valuePattern)""",
            RegexOption.IGNORE_CASE
        )
        immediatePattern.find(window)?.groupValues?.getOrNull(1)?.let { return it }

        val boundedPattern = Regex(
            """${Regex.escape(label)}.{0,40}?($valuePattern)""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val matches = boundedPattern.findAll(window).map { it.groupValues[1] }.toList()
        if (matches.isNotEmpty()) {
            return if (preferLastMatch) matches.lastOrNull() else matches.firstOrNull()
        }

        val valueBeforeLabelPattern = Regex(
            """($valuePattern).{0,24}?${Regex.escape(label)}""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val reversedMatches = valueBeforeLabelPattern.findAll(window).map { it.groupValues[1] }.toList()
        return if (preferLastMatch) reversedMatches.lastOrNull() else reversedMatches.firstOrNull()
    }
}
