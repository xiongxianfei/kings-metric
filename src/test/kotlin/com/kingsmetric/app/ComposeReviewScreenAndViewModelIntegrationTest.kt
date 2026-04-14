package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.ReviewFlag
import com.kingsmetric.importflow.SaveResult
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import com.kingsmetric.importflow.TemplateValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeReviewScreenAndViewModelIntegrationTest {

    @Test
    fun `T1 review ViewModel exposes screenshot path field state and confirm availability from a draft`() {
        val viewModel = reviewViewModel(draft = ReviewScreenFixtures.supportedDraft())

        val state = viewModel.state.value

        assertEquals("stored/1-fixture.png", state.screenshotPath)
        assertTrue(state.fields.isNotEmpty())
        assertTrue(state.canConfirm)
    }

    @Test
    fun `T2 editing a field updates state and clears resolved blocking status when valid`() {
        val viewModel = reviewViewModel(draft = ReviewScreenFixtures.requiredMissingDraft())

        viewModel.updateField(FieldKey.KDA, "11/1/5")

        val state = viewModel.state.value
        assertEquals("11/1/5", state.fields.getValue(FieldKey.KDA).value)
        assertFalse(state.blockingFields.contains(FieldKey.KDA))
    }

    @Test
    fun `T3 save validation failure keeps current edits in ViewModel state`() {
        val viewModel = reviewViewModel(draft = ReviewScreenFixtures.requiredMissingDraft())

        viewModel.updateField(FieldKey.LANE, "Farm Lane")
        val result = viewModel.confirmSave()

        assertTrue(result is SaveResult.Blocked)
        assertEquals("Farm Lane", viewModel.state.value.fields.getValue(FieldKey.LANE).value)
    }

    @Test
    fun `IT1 compose review screen shows highlighted required unresolved fields`() {
        val screen = reviewScreen(draft = ReviewScreenFixtures.requiredMissingDraft())

        val model = screen.render()

        assertTrue(model.highlightedFields.contains(FieldKey.KDA))
        assertTrue(model.blockingFields.contains(FieldKey.KDA))
        assertFalse(model.canConfirm)
    }

    @Test
    fun `IT2 optional unresolved fields remain highlighted but do not block confirm`() {
        val screen = reviewScreen(draft = ReviewScreenFixtures.optionalMissingDraft())

        val model = screen.render()

        assertTrue(model.highlightedFields.contains(FieldKey.LAST_HITS))
        assertFalse(model.blockingFields.contains(FieldKey.LAST_HITS))
        assertTrue(model.canConfirm)
    }

    @Test
    fun `IT3 missing screenshot preview shows unavailable-preview UI while field data remains visible`() {
        val screen = reviewScreen(
            draft = ReviewScreenFixtures.supportedDraft(),
            previewAvailable = false
        )

        val model = screen.render()

        assertEquals(PreviewAvailability.Unavailable, model.previewAvailability)
        assertTrue(model.fields.isNotEmpty())
    }

    @Test
    fun `IT4 save failure leaves the user on review with edits intact`() {
        val workflow = ReviewScreenFixtures.workflow(recordStore = FakeRecordStore(shouldFail = true))
        val viewModel = ReviewScreenViewModel(
            draft = ReviewScreenFixtures.supportedDraft(),
            workflow = workflow
        )

        viewModel.updateField(FieldKey.LANE, "Farm Lane")
        val result = viewModel.confirmSave()

        assertTrue(result is SaveResult.StorageFailed)
        assertEquals("Farm Lane", viewModel.state.value.fields.getValue(FieldKey.LANE).value)
        assertEquals(ReviewScreenStatus.Reviewing, viewModel.state.value.status)
    }
}

private fun reviewViewModel(
    draft: DraftRecord,
    previewAvailable: Boolean = true
): ReviewScreenViewModel {
    return ReviewScreenViewModel(
        draft = draft,
        workflow = ReviewScreenFixtures.workflow(),
        previewAvailableResolver = { previewAvailable }
    )
}

private fun reviewScreen(
    draft: DraftRecord,
    previewAvailable: Boolean = true
): ReviewScreen {
    return ReviewScreen(reviewViewModel(draft, previewAvailable))
}

private object ReviewScreenFixtures {
    private val parser = DraftParser()

    fun workflow(recordStore: FakeRecordStore = FakeRecordStore()): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = recordStore,
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

    private fun supportedAnalysis(
        visibleFields: Set<FieldKey> = FieldKey.all
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
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }
}
