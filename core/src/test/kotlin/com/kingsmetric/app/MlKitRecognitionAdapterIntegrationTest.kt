package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.ReviewFlag
import com.kingsmetric.importflow.Section
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MlKitRecognitionAdapterIntegrationTest {

    @Test
    fun `T1 region-mapping logic requests the expected field regions for the supported template`() {
        val adapter = recognitionAdapter()

        val plan = adapter.regionPlanFor("stored/shot-1.png")

        assertEquals("supported-personal-stats-v1", plan.templateId)
        assertTrue(plan.requestedFields.containsAll(setOf(FieldKey.RESULT, FieldKey.HERO, FieldKey.KDA)))
    }

    @Test
    fun `T2 low-confidence OCR output is preserved as review-required field state`() {
        val adapter = recognitionAdapter(
            recognizer = FakeMlKitRecognizer(
                analysisByPath = mapOf(
                    "stored/shot-1.png" to MlKitFixtures.supportedAnalysis(
                        lowConfidenceFields = setOf(FieldKey.GOLD_FROM_FARMING)
                    )
                )
            )
        )

        val result = adapter.recognize("stored/shot-1.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertTrue(ReviewFlag.LOW_CONFIDENCE in result.draft.require(FieldKey.GOLD_FROM_FARMING).flags)
    }

    @Test
    fun `T3 unsupported template detection blocks supported-draft creation`() {
        val adapter = recognitionAdapter(
            recognizer = FakeMlKitRecognizer(
                analysisByPath = mapOf(
                    "stored/cropped.png" to MlKitFixtures.supportedAnalysis(
                        visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY)
                    )
                )
            )
        )

        val result = adapter.recognize("stored/cropped.png")

        assertTrue(result is ImportResult.Unsupported)
    }

    @Test
    fun `IT1 stored screenshot file is decoded and passed through the ML Kit adapter`() {
        val bitmapLoader = FakeBitmapLoader(existingPaths = setOf("stored/shot-1.png"))
        val recognizer = FakeMlKitRecognizer(
            analysisByPath = mapOf("stored/shot-1.png" to MlKitFixtures.supportedAnalysis())
        )
        val adapter = recognitionAdapter(bitmapLoader = bitmapLoader, recognizer = recognizer)

        val result = adapter.recognize("stored/shot-1.png")

        assertTrue(result is ImportResult.DraftReady)
        assertEquals(listOf("stored/shot-1.png"), bitmapLoader.loadedPaths)
        assertEquals(listOf("stored/shot-1.png"), recognizer.recognizedPaths)
    }

    @Test
    fun `IT2 OCR failure returns a clear import failure`() {
        val adapter = recognitionAdapter(
            recognizer = FakeMlKitRecognizer(failPaths = setOf("stored/ocr-fails.png"))
        )

        val result = adapter.recognize("stored/ocr-fails.png")

        assertTrue(result is ImportResult.ImportFailed)
    }

    @Test
    fun `IT3 unexpected OCR mapping exception becomes import failure instead of crashing import`() {
        val adapter = recognitionAdapter(
            recognizer = object : MlKitRecognizer {
                override fun recognize(
                    bitmap: LoadedBitmap,
                    plan: RecognitionRegionPlan
                ): RecognitionOutput {
                    throw IllegalStateException("unexpected mapper failure")
                }
            }
        )

        val result = adapter.recognize("stored/shot-1.png")

        assertTrue(result is ImportResult.ImportFailed)
    }

    @Test
    fun `IT4 supported screenshot with one unreadable required field still produces a draftable unresolved result`() {
        val adapter = recognitionAdapter(
            recognizer = FakeMlKitRecognizer(
                analysisByPath = mapOf(
                    "stored/missing-kda.png" to MlKitFixtures.supportedAnalysis(
                        visibleFields = FieldKey.all - FieldKey.KDA
                    )
                )
            )
        )

        val result = adapter.recognize("stored/missing-kda.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals(null, result.draft.require(FieldKey.KDA).value)
        assertTrue(ReviewFlag.MISSING in result.draft.require(FieldKey.KDA).flags)
    }

    @Test
    fun `IT5 unsupported cropped screenshot remains rejected`() {
        val adapter = recognitionAdapter(
            recognizer = FakeMlKitRecognizer(
                analysisByPath = mapOf(
                    "stored/cropped.png" to MlKitFixtures.supportedAnalysis(
                        anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD),
                        visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN)
                    )
                )
            )
        )

        val result = adapter.recognize("stored/cropped.png")

        assertTrue(result is ImportResult.Unsupported)
    }

    @Test
    fun `IT6 unsupported result preserves OCR text for diagnostics export`() {
        val adapter = recognitionAdapter(
            recognizer = object : MlKitRecognizer {
                override fun recognize(
                    bitmap: LoadedBitmap,
                    plan: RecognitionRegionPlan
                ): RecognitionOutput {
                    return RecognitionOutput(
                        analysis = MlKitFixtures.supportedAnalysis(
                            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY)
                        ),
                        ocrText = "胜利\n数据 复盘\n对英雄出: 171.2k"
                    )
                }
            }
        )

        val result = adapter.recognize("stored/cropped.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertEquals("胜利\n数据 复盘\n对英雄出: 171.2k", result.ocrText)
    }
}

private fun recognitionAdapter(
    bitmapLoader: FakeBitmapLoader = FakeBitmapLoader(),
    recognizer: MlKitRecognizer = FakeMlKitRecognizer(
        analysisByPath = mapOf("stored/shot-1.png" to MlKitFixtures.supportedAnalysis())
    )
): MlKitRecognitionAdapter {
    return MlKitRecognitionAdapter(
        bitmapLoader = bitmapLoader,
        recognizer = recognizer
    )
}
