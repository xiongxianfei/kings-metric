package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewAndManualCorrectionFlowTest {

    @Test
    fun `T1 review state includes screenshot reference and extracted fields`() {
        val draft = ReviewFixtures.supportedDraft()

        val reviewState = ReviewState.fromDraft(draft)

        assertEquals("stored/1-fixture.png", reviewState.screenshotPath)
        assertTrue(reviewState.fields.isNotEmpty())
    }

    @Test
    fun `T2 missing fields are highlighted`() {
        val reviewState = ReviewState.fromDraft(ReviewFixtures.optionalMissingDraft())

        assertTrue(reviewState.highlightedFields.contains(FieldKey.LAST_HITS))
    }

    @Test
    fun `T3 invalid fields are highlighted`() {
        val reviewState = ReviewState.fromDraft(ReviewFixtures.invalidFieldDraft())

        assertTrue(reviewState.highlightedFields.contains(FieldKey.KDA))
    }

    @Test
    fun `T4 low confidence fields are highlighted`() {
        val reviewState = ReviewState.fromDraft(ReviewFixtures.lowConfidenceDraft())

        assertTrue(reviewState.highlightedFields.contains(FieldKey.GOLD_FROM_FARMING))
    }

    @Test
    fun `T5 user edit updates field value in review state`() {
        val workflow = ReviewFixtures.workflow()
        val draft = ReviewFixtures.lowConfidenceDraft()

        val updatedDraft = workflow.updateField(draft, FieldKey.GOLD_FROM_FARMING, "3821")
        val reviewState = ReviewState.fromDraft(updatedDraft)

        assertEquals("3821", reviewState.fields.getValue(FieldKey.GOLD_FROM_FARMING).value)
    }

    @Test
    fun `T6 user edit clears unresolved status when the new value is valid`() {
        val workflow = ReviewFixtures.workflow()
        val draft = ReviewFixtures.requiredMissingDraft()

        val updatedDraft = workflow.updateField(draft, FieldKey.KDA, "11/1/5")
        val reviewState = ReviewState.fromDraft(updatedDraft)

        assertFalse(reviewState.highlightedFields.contains(FieldKey.KDA))
        assertFalse(reviewState.blockingFields.contains(FieldKey.KDA))
    }

    @Test
    fun `IT3 missing required field blocks final confirmation`() {
        val reviewState = ReviewState.fromDraft(ReviewFixtures.requiredMissingDraft())

        assertTrue(reviewState.blockingFields.contains(FieldKey.KDA))
        assertFalse(reviewState.canConfirm)
    }

    @Test
    fun `IT4 missing optional field does not block final confirmation`() {
        val reviewState = ReviewState.fromDraft(ReviewFixtures.optionalMissingDraft())

        assertTrue(reviewState.highlightedFields.contains(FieldKey.LAST_HITS))
        assertTrue(reviewState.canConfirm)
    }

    @Test
    fun `IT5 validation failure keeps user edits intact in review state`() {
        val workflow = ReviewFixtures.workflow()
        val editedDraft = workflow.updateField(ReviewFixtures.requiredMissingDraft(), FieldKey.LANE, "发育路")

        val save = workflow.confirmSave(editedDraft)
        val reviewState = ReviewState.fromDraft(editedDraft)

        assertTrue(save is SaveResult.Blocked)
        assertEquals("发育路", reviewState.fields.getValue(FieldKey.LANE).value)
    }

    @Test
    fun `IT6 screenshot preview failure does not discard field data`() {
        val reviewState = ReviewState.fromDraft(
            ReviewFixtures.supportedDraft().copy(screenshotPath = null)
        )

        assertFalse(reviewState.screenshotAvailable)
        assertTrue(reviewState.fields.isNotEmpty())
    }
}

private object ReviewFixtures {
    private val parser = DraftParser()

    fun workflow(): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = FakeRecordStore(),
            validator = TemplateValidator(),
            parser = parser
        )
    }

    fun supportedDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )

    fun optionalMissingDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )

    fun requiredMissingDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.KDA),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )

    fun lowConfidenceDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedAnalysis(lowConfidenceFields = setOf(FieldKey.GOLD_FROM_FARMING)),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )

    fun invalidFieldDraft(): DraftRecord =
        parser.createDraft(
            analysis = supportedAnalysis(
                rawValues = supportedRawValues() + (FieldKey.KDA to "bad-kda")
            ),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )

    private fun supportedAnalysis(
        visibleFields: Set<FieldKey> = FieldKey.all,
        lowConfidenceFields: Set<FieldKey> = emptySet(),
        rawValues: Map<FieldKey, String> = supportedRawValues()
    ): ScreenshotAnalysis {
        return ScreenshotAnalysis(
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = lowConfidenceFields
        )
    }

    private fun supportedRawValues(): Map<FieldKey, String> = mapOf(
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
}
