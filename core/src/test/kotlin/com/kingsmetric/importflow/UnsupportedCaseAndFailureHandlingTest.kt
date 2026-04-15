package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnsupportedCaseAndFailureHandlingTest {

    @Test
    fun `T1 unsupported screenshot message identifies template mismatch intent`() {
        val workflow = failureWorkflow(
            analysisByPath = mapOf("wrong-screen.png" to FailureFixtures.unsupportedWrongScreenAnalysis())
        )

        val result = workflow.importScreenshot("wrong-screen.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertTrue(result.reason.contains("supported personal-stats template"))
        assertEquals(FailureAction.RETRY_IMPORT, result.nextAction)
    }

    @Test
    fun `T2 save blocked state retains unresolved required field context`() {
        val workflow = failureWorkflow()
        val draft = FailureFixtures.requiredMissingDraft()

        val result = workflow.confirmSave(draft)

        assertTrue(result is SaveResult.Blocked)
        result as SaveResult.Blocked
        assertEquals(draft, result.draft)
        assertEquals(FailureAction.CORRECT_REVIEW_FIELDS, result.nextAction)
    }

    @Test
    fun `T3 storage failure result distinguishes screenshot save failure from record save failure`() {
        val importWorkflow = failureWorkflow(
            screenshotStore = FakeScreenshotStore(failPaths = setOf("save-fails.png"))
        )
        val saveWorkflow = failureWorkflow(recordStore = FakeRecordStore(shouldFail = true))

        val importResult = importWorkflow.importScreenshot("save-fails.png")
        val saveResult = saveWorkflow.confirmSave(FailureFixtures.validDraft())

        assertTrue(importResult is ImportResult.StorageFailed)
        assertTrue(saveResult is SaveResult.StorageFailed)

        importResult as ImportResult.StorageFailed
        saveResult as SaveResult.StorageFailed

        assertEquals(FailureAction.RETRY_IMPORT, importResult.nextAction)
        assertEquals(FailureAction.RETRY_SAVE, saveResult.nextAction)
    }

    @Test
    fun `IT1 unsupported screenshot rejects and returns user to retryable import state`() {
        val workflow = failureWorkflow(
            analysisByPath = mapOf("wrong-screen.png" to FailureFixtures.unsupportedWrongScreenAnalysis())
        )

        val result = workflow.importScreenshot("wrong-screen.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertEquals(FailureAction.RETRY_IMPORT, result.nextAction)
    }

    @Test
    fun `IT2 unsupported language variant rejects and returns user to retryable import state`() {
        val workflow = failureWorkflow(
            analysisByPath = mapOf("other-language.png" to FailureFixtures.supportedOtherLanguageAnalysis())
        )

        val result = workflow.importScreenshot("other-language.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertEquals(FailureAction.RETRY_IMPORT, result.nextAction)
    }

    @Test
    fun `IT3 blocked save keeps review state and user edits intact`() {
        val workflow = failureWorkflow()
        val editedDraft = workflow.updateField(FailureFixtures.requiredMissingDraft(), FieldKey.LANE, "发育路")

        val result = workflow.confirmSave(editedDraft)

        assertTrue(result is SaveResult.Blocked)
        result as SaveResult.Blocked
        assertEquals("发育路", result.draft.require(FieldKey.LANE).value)
        assertEquals(FailureAction.CORRECT_REVIEW_FIELDS, result.nextAction)
    }

    @Test
    fun `IT4 screenshot storage failure stops downstream processing and shows error`() {
        val workflow = failureWorkflow(
            screenshotStore = FakeScreenshotStore(failPaths = setOf("storage-failure.png"))
        )

        val result = workflow.importScreenshot("storage-failure.png")

        assertTrue(result is ImportResult.StorageFailed)
        result as ImportResult.StorageFailed
        assertEquals(FailureAction.RETRY_IMPORT, result.nextAction)
    }

    @Test
    fun `IT5 record persistence failure shows save failure without marking success`() {
        val workflow = failureWorkflow(recordStore = FakeRecordStore(shouldFail = true))

        val result = workflow.confirmSave(FailureFixtures.validDraft())

        assertTrue(result is SaveResult.StorageFailed)
        result as SaveResult.StorageFailed
        assertEquals(false, result.saved)
        assertEquals(FailureAction.RETRY_SAVE, result.nextAction)
    }

    @Test
    fun `IT6 screenshot remains consistent when record persistence fails after intake succeeded`() {
        val workflow = failureWorkflow(recordStore = FakeRecordStore(shouldFail = true))
        val draft = FailureFixtures.validDraft()

        val result = workflow.confirmSave(draft)

        assertTrue(result is SaveResult.StorageFailed)
        result as SaveResult.StorageFailed
        assertEquals(draft.screenshotPath, result.draft?.screenshotPath)
    }
}

private fun failureWorkflow(
    screenshotStore: FakeScreenshotStore = FakeScreenshotStore(),
    recordStore: FakeRecordStore = FakeRecordStore(),
    analysisByPath: Map<String, ScreenshotAnalysis> = mapOf(
        "valid.png" to FailureFixtures.supportedFullAnalysis()
    )
): MatchImportWorkflow {
    return MatchImportWorkflow(
        screenshotStore = screenshotStore,
        analyzer = FakeScreenshotAnalyzer(analysisByPath),
        recordStore = recordStore,
        validator = TemplateValidator(),
        parser = DraftParser()
    )
}

private object FailureFixtures {
    private val parser = DraftParser()

    fun validDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedFullAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-valid.png"
        )

    fun requiredMissingDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedFullAnalysis(visibleFields = FieldKey.all - FieldKey.KDA),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-valid.png"
        )

    fun supportedFullAnalysis(
        visibleFields: Set<FieldKey> = FieldKey.all
    ): ScreenshotAnalysis {
        val rawValues = mapOf(
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
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }

    fun unsupportedWrongScreenAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis().copy(
            anchors = setOf(Anchor.SUMMARY_CARD),
            visibleSections = setOf(Section.DAMAGE)
        )

    fun supportedOtherLanguageAnalysis(): ScreenshotAnalysis =
        supportedFullAnalysis().copy(languageCode = "en-US")
}
