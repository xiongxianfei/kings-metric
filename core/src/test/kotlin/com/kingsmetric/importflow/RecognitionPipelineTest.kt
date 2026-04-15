package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionPipelineTest {

    @Test
    fun `T13 create a draft for a supported screenshot`() {
        val workflow = recognitionWorkflow()

        val result = workflow.importScreenshot("fixture_supported_full.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
    }

    @Test
    fun `T14 mark optional unreadable field as missing`() {
        val workflow = recognitionWorkflow(
            analysisByPath = mapOf(
                "fixture_supported_optional_missing.png" to RecognitionFixtures.supportedOptionalMissingAnalysis()
            )
        )

        val result = workflow.importScreenshot("fixture_supported_optional_missing.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertTrue(result.draft.require(FieldKey.LAST_HITS).flags.contains(ReviewFlag.MISSING))
    }

    @Test
    fun `T15 mark required unreadable field unresolved`() {
        val workflow = recognitionWorkflow(
            analysisByPath = mapOf(
                "fixture_supported_required_missing.png" to RecognitionFixtures.supportedRequiredMissingAnalysis()
            )
        )

        val result = workflow.importScreenshot("fixture_supported_required_missing.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertTrue(result.draft.require(FieldKey.KDA).flags.contains(ReviewFlag.MISSING))
    }

    @Test
    fun `IT4 unsupported layout is rejected before saveable output`() {
        val workflow = recognitionWorkflow(
            analysisByPath = mapOf(
                "fixture_unsupported.png" to RecognitionFixtures.unsupportedWrongScreenAnalysis()
            )
        )

        val result = workflow.importScreenshot("fixture_unsupported.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertTrue(result.reason.contains("supported personal-stats template"))
    }

    @Test
    fun `IT5 unsupported language variant is rejected before saveable output`() {
        val workflow = recognitionWorkflow(
            analysisByPath = mapOf(
                "fixture_other_language.png" to RecognitionFixtures.supportedOtherLanguageAnalysis()
            )
        )

        val result = workflow.importScreenshot("fixture_other_language.png")

        assertTrue(result is ImportResult.Unsupported)
    }

    @Test
    fun `IT6 extraction failure returns explicit import failure`() {
        val workflow = MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FailingScreenshotAnalyzer(),
            recordStore = FakeRecordStore(),
            validator = TemplateValidator(),
            parser = DraftParser()
        )

        val result = workflow.importScreenshot("fixture_supported_full.png")

        assertTrue(result is ImportResult.ImportFailed)
        result as ImportResult.ImportFailed
        assertTrue(result.message.contains("extract", ignoreCase = true))
    }
}

private fun recognitionWorkflow(
    analysisByPath: Map<String, ScreenshotAnalysis> = mapOf(
        "fixture_supported_full.png" to RecognitionFixtures.supportedFullAnalysis()
    )
): MatchImportWorkflow {
    return MatchImportWorkflow(
        screenshotStore = FakeScreenshotStore(),
        analyzer = FakeScreenshotAnalyzer(analysisByPath),
        recordStore = FakeRecordStore(),
        validator = TemplateValidator(),
        parser = DraftParser()
    )
}

private object RecognitionFixtures {
    private val allSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION)
    private val allAnchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED)

    fun supportedFullAnalysis(
        anchors: Set<Anchor> = allAnchors,
        visibleSections: Set<Section> = allSections,
        visibleFields: Set<FieldKey> = FieldKey.all
    ): ScreenshotAnalysis {
        val extracted = mapOf(
            FieldKey.RESULT to "胜利",
            FieldKey.HERO to "孙尚香",
            FieldKey.PLAYER_NAME to "King",
            FieldKey.LANE to "对抗路",
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
            rawValues = extracted.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }

    fun supportedOptionalMissingAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS)

    fun supportedRequiredMissingAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis(visibleFields = FieldKey.all - FieldKey.KDA)

    fun supportedOtherLanguageAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis().copy(languageCode = "en-US")

    fun unsupportedWrongScreenAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis(
            anchors = setOf(Anchor.SUMMARY_CARD),
            visibleSections = setOf(Section.DAMAGE)
        )
}

private class FailingScreenshotAnalyzer : ScreenshotAnalyzer {
    override fun analyze(sourcePath: String): ScreenshotAnalysis {
        throw OcrExtractionException("ocr failed")
    }
}
