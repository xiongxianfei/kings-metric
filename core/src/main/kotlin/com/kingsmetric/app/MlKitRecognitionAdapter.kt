package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.OcrExtractionException
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import com.kingsmetric.importflow.TemplateValidationResult
import com.kingsmetric.importflow.TemplateValidator

data class RecognitionRegionPlan(
    val templateId: String,
    val requestedFields: Set<FieldKey>
)

interface BitmapLoader {
    fun load(path: String): LoadedBitmap
}

data class LoadedBitmap(
    val path: String
)

data class RecognitionOutput(
    val analysis: ScreenshotAnalysis,
    val ocrText: String
)

interface MlKitRecognizer {
    fun recognize(bitmap: LoadedBitmap, plan: RecognitionRegionPlan): RecognitionOutput
}

class BitmapDecodeException : IllegalStateException("bitmap decode failed")

class MlKitRecognitionAdapter(
    private val bitmapLoader: BitmapLoader,
    private val recognizer: MlKitRecognizer,
    private val validator: TemplateValidator = TemplateValidator(),
    private val parser: DraftParser = DraftParser()
) {

    fun regionPlanFor(path: String): RecognitionRegionPlan {
        return RecognitionRegionPlan(
            templateId = "supported-personal-stats-v1",
            requestedFields = FieldKey.all
        )
    }

    fun recognize(path: String): ImportResult {
        val bitmap = try {
            bitmapLoader.load(path)
        } catch (_: BitmapDecodeException) {
            return ImportResult.ImportFailed("Could not decode screenshot for recognition.")
        }

        val recognitionOutput = try {
            recognizer.recognize(bitmap, regionPlanFor(path))
        } catch (_: OcrExtractionException) {
            return ImportResult.ImportFailed("Could not extract screenshot data for review.")
        } catch (_: Exception) {
            return ImportResult.ImportFailed("Could not extract screenshot data for review.")
        }
        val analysis = recognitionOutput.analysis

        return when (val validation = validator.validate(analysis)) {
            TemplateValidationResult.Supported -> {
                val draft = parser.createDraft(
                    analysis = analysis,
                    screenshotId = path.substringAfterLast('/').substringBefore('.'),
                    screenshotPath = path
                )
                ImportResult.DraftReady(
                    storedScreenshot = com.kingsmetric.importflow.StoredScreenshot(
                        id = draft.screenshotId ?: path,
                        path = path,
                        originalSourcePath = path
                    ),
                    draft = draft,
                    reviewState = ReviewState.fromDraft(draft)
                )
            }
            is TemplateValidationResult.Unsupported -> {
                ImportResult.Unsupported(
                    "Image does not match the supported personal-stats template. ${validation.reason}",
                    ocrText = recognitionOutput.ocrText
                )
            }
        }
    }
}

class FakeBitmapLoader(
    private val existingPaths: Set<String>? = null
) : BitmapLoader {
    val loadedPaths = mutableListOf<String>()

    override fun load(path: String): LoadedBitmap {
        if (existingPaths != null && path !in existingPaths) {
            throw BitmapDecodeException()
        }
        loadedPaths += path
        return LoadedBitmap(path)
    }
}

class FakeMlKitRecognizer(
    private val analysisByPath: Map<String, ScreenshotAnalysis> = emptyMap(),
    private val failPaths: Set<String> = emptySet()
) : MlKitRecognizer {
    val recognizedPaths = mutableListOf<String>()

    override fun recognize(bitmap: LoadedBitmap, plan: RecognitionRegionPlan): RecognitionOutput {
        val path = bitmap.path
        recognizedPaths += path
        if (path in failPaths) {
            throw OcrExtractionException("ocr failed")
        }
        val analysis = analysisByPath[path]
            ?: error("No ML Kit analysis configured for $path")
        return RecognitionOutput(
            analysis = analysis,
            ocrText = "fake ocr text for $path"
        )
    }
}

object MlKitFixtures {
    fun supportedAnalysis(
        anchors: Set<Anchor> = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
        visibleSections: Set<Section> = setOf(
            Section.DAMAGE,
            Section.DAMAGE_TAKEN,
            Section.ECONOMY,
            Section.TEAM_PARTICIPATION
        ),
        visibleFields: Set<FieldKey> = FieldKey.all,
        lowConfidenceFields: Set<FieldKey> = emptySet()
    ): ScreenshotAnalysis {
        val rawValues = mapOf(
            FieldKey.RESULT to "victory",
            FieldKey.HERO to "Sun Shangxiang",
            FieldKey.PLAYER_NAME to "King",
            FieldKey.LANE to "Clash Lane",
            FieldKey.SCORE to "20-10",
            FieldKey.KDA to "11/1/5",
            FieldKey.DAMAGE_DEALT to "12345",
            FieldKey.DAMAGE_SHARE to "34%",
            FieldKey.DAMAGE_TAKEN to "9850",
            FieldKey.DAMAGE_TAKEN_SHARE to "28%",
            FieldKey.TOTAL_GOLD to "12543",
            FieldKey.GOLD_SHARE to "31%",
            FieldKey.PARTICIPATION_RATE to "76%",
            FieldKey.GOLD_FROM_FARMING to "3680",
            FieldKey.LAST_HITS to "71",
            FieldKey.KILL_PARTICIPATION_COUNT to "13",
            FieldKey.CONTROL_DURATION to "00:14",
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
        )
        return ScreenshotAnalysis(
            anchors = anchors,
            visibleSections = visibleSections,
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = lowConfidenceFields
        )
    }
}
