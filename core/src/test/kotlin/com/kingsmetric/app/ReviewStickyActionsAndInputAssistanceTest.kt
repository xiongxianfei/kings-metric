package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.SaveResult
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import com.kingsmetric.importflow.TemplateValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewStickyActionsAndInputAssistanceTest {

    @Test
    fun layoutState_boundsPreviewAndKeepsStickySaveVisibleWhenPreviewExists() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.supportedDraft(),
            previewAvailable = true
        ).state.value

        assertEquals(220, state.layout.previewMaxHeightDp)
        assertTrue(state.layout.stickySaveActionVisible)
        assertTrue(state.layout.singleScrollSurface)
    }

    @Test
    fun layoutState_missingPreviewCollapsesPreviewSpaceWithoutHidingGuidance() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.requiredMissingDraft(),
            previewAvailable = false
        ).state.value

        assertEquals(0, state.layout.previewMaxHeightDp)
        assertNotNull(state.blockerSummary)
        assertTrue(state.layout.stickySaveActionVisible)
    }

    @Test
    fun fieldMetadata_exposesConciseFormatHintsForAmbiguousFields() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.requiredMissingDraft()
        ).state.value

        val kda = state.sections.flatMap { it.fields }.first { it.key == FieldKey.KDA }
        val damageShare = state.sections.flatMap { it.fields }.first { it.key == FieldKey.DAMAGE_SHARE }
        val duration = state.sections.flatMap { it.fields }.first { it.key == FieldKey.CONTROL_DURATION }

        assertEquals("Example: 11/1/5", kda.hint)
        assertEquals("Example: 34%", damageShare.hint)
        assertEquals("Example: 00:14", duration.hint)
    }

    @Test
    fun fieldMetadata_usesInputAffordancesForNumericAndRateFields() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.supportedDraft()
        ).state.value

        val lastHits = state.sections.flatMap { it.fields }.first { it.key == FieldKey.LAST_HITS }
        val goldShare = state.sections.flatMap { it.fields }.first { it.key == FieldKey.GOLD_SHARE }
        val lane = state.sections.flatMap { it.fields }.first { it.key == FieldKey.LANE }

        assertEquals(ReviewInputAffordance.Number, lastHits.inputAffordance)
        assertEquals(ReviewInputAffordance.Percentage, goldShare.inputAffordance)
        assertEquals(ReviewInputAffordance.Text, lane.inputAffordance)
    }

    @Test
    fun unresolvedRequiredFields_arePrioritizedAheadOfOptionalCleanup() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.blockedWithOptionalHighlightDraft()
        ).state.value
        val summaryFields = state.blockerSummary?.fieldLabels
        val economyFields = state.sections
            .first { it.id == ReviewSectionId.ECONOMY }
            .fields
            .map { it.key }

        assertEquals(listOf("Damage Dealt"), summaryFields)
        assertEquals(
            listOf(
                FieldKey.LAST_HITS,
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_SHARE,
                FieldKey.GOLD_FROM_FARMING
            ),
            economyFields
        )
    }

    @Test
    fun wrongFormatEditRemainsInStateWhenSaveIsStillBlocked() {
        val viewModel = reviewViewModel(
            draft = ReviewUxFixtures.requiredMissingDraft()
        )

        viewModel.updateField(FieldKey.KDA, "wrong")
        val result = viewModel.confirmSave()

        assertTrue(result is SaveResult.Blocked)
        assertEquals("wrong", viewModel.state.value.fields.getValue(FieldKey.KDA).value)
        assertNotNull(viewModel.state.value.blockerSummary)
    }

    @Test
    fun optionalHighlightedFieldsNeverBecomeRequiredBlockers() {
        val state = reviewViewModel(
            draft = ReviewUxFixtures.optionalMissingDraft()
        ).state.value
        val lastHits = state.sections.flatMap { it.fields }.first { it.key == FieldKey.LAST_HITS }

        assertTrue(lastHits.highlighted)
        assertFalse(lastHits.blocking)
        assertFalse(lastHits.required)
        assertNull(state.blockerSummary)
    }
}

private fun reviewViewModel(
    draft: DraftRecord,
    previewAvailable: Boolean = true
): ReviewScreenViewModel {
    return ReviewScreenViewModel(
        draft = draft,
        workflow = ReviewUxFixtures.workflow(),
        previewAvailableResolver = { previewAvailable }
    )
}

private object ReviewUxFixtures {
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

    fun supportedDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun requiredMissingDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.DAMAGE_DEALT),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun optionalMissingDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun blockedWithOptionalHighlightDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(
            visibleFields = FieldKey.all - setOf(FieldKey.DAMAGE_DEALT, FieldKey.LAST_HITS)
        ),
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
            visibleSections = setOf(
                Section.DAMAGE,
                Section.DAMAGE_TAKEN,
                Section.ECONOMY,
                Section.TEAM_PARTICIPATION
            ),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }
}
