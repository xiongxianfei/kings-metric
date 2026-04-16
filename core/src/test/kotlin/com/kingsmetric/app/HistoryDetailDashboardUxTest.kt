package com.kingsmetric.app

import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.dashboard.DashboardMetricsCalculator
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.MatchHistoryListItem
import com.kingsmetric.history.MatchDetailState
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDetailDashboardUxTest {

    @Test
    fun historyRowMapper_promotesPrimaryScanFieldsAndRecencyInBoundedOrder() {
        val state = HistoryContentState.Loaded(
            records = listOf(
                MatchHistoryListItem(
                    recordId = "record-1",
                    savedAt = 1_710_000_000_000L,
                    hero = "Sun Shangxiang",
                    result = "victory",
                    lane = "Farm Lane",
                    score = "20-10",
                    kda = "11/1/5",
                    screenshotAvailable = true
                )
            )
        ).toHistoryScreenUiState()

        val row = state.rows.single()
        assertEquals("Saved match", row.categoryLabel)
        assertEquals("Sun Shangxiang", row.primaryText)
        assertEquals("Victory", row.resultText)
        assertTrue(row.recencyText.startsWith("Saved "))
        assertEquals(
            listOf(
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Victory"),
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.LANE, "Farm Lane"),
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.KDA, "11/1/5"),
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.SCORE, "20-10")
            ),
            row.quickSummaryItems
        )
        assertNull(row.previewText)
        assertTrue(row.selectable)
    }

    @Test
    fun historyRowMapper_omitsMissingOptionalQuickSummaryFieldsInRelativeOrder() {
        val state = HistoryContentState.Loaded(
            records = listOf(
                MatchHistoryListItem(
                    recordId = "record-2",
                    savedAt = 1_710_000_000_000L,
                    hero = "Sun Shangxiang",
                    result = "defeat",
                    lane = null,
                    score = null,
                    kda = "5/3/7",
                    screenshotAvailable = false
                )
            )
        ).toHistoryScreenUiState()

        val row = state.rows.single()
        assertEquals("Saved match", row.categoryLabel)
        assertEquals("Sun Shangxiang", row.primaryText)
        assertEquals("Defeat", row.resultText)
        assertEquals(
            listOf(
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Defeat"),
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.KDA, "5/3/7")
            ),
            row.quickSummaryItems
        )
        assertEquals("Preview unavailable", row.previewText)
        assertTrue(row.selectable)
    }

    @Test
    fun historyRowMapper_usesReadableFallbacksForHeroAndResultAndKeepsReducedSummaryClean() {
        val state = HistoryContentState.Loaded(
            records = listOf(
                MatchHistoryListItem(
                    recordId = "record-3",
                    savedAt = 1_710_000_000_000L,
                    hero = null,
                    result = null,
                    lane = null,
                    score = null,
                    kda = null,
                    screenshotAvailable = true
                )
            )
        ).toHistoryScreenUiState()

        val row = state.rows.single()
        assertEquals("Hero not entered", row.primaryText)
        assertEquals("Result not entered", row.resultText)
        assertEquals(
            listOf(
                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Result not entered")
            ),
            row.quickSummaryItems
        )
        assertNull(row.previewText)
    }

    @Test
    fun detailMapper_groupsFieldsAndUsesReadableFallbacks() {
        val state = MatchDetailState(
            record = detailRecord(
                fields = detailFields() + (FieldKey.LAST_HITS to null)
            ),
            screenshotPreview = ScreenshotPreviewState.Unavailable
        ).toDetailScreenUiState()

        assertEquals("Sun Shangxiang", state.summaryTitle)
        assertEquals("Victory", state.summaryResult)
        assertTrue(state.summaryMetaText.startsWith("Saved "))
        assertEquals("Screenshot", state.previewStatusLabel)
        assertEquals("Screenshot preview unavailable", state.previewStatusText)
        assertTrue(state.sections.any { it.title == "Match Summary" })
        assertTrue(
            state.sections.flatMap { it.fields }.any { field ->
                field.label == "Last Hits" && field.valueText == "Not entered"
            }
        )
        assertTrue(state.marksmanInsights is MarksmanInsightsUiState.Eligible)
        state.marksmanInsights as MarksmanInsightsUiState.Eligible
        assertTrue(state.marksmanInsights.metricGroups.any { it.title == "Economy And Farming" })
    }

    @Test
    fun detailMapper_preservesPredictableBackNavigation() {
        val state = MatchDetailState(
            record = detailRecord(),
            screenshotPreview = ScreenshotPreviewState.Available("stored/shot-1.png")
        ).toDetailScreenUiState()

        assertEquals("History", state.backLabel)
        assertEquals("Screenshot available", state.previewStatusText)
    }

    @Test
    fun detailMapper_marksmanEligibleRecordAddsBoundedInsightsAndSuggestions() {
        val state = MatchDetailState(
            record = detailRecord(
                fields = detailFields() + mapOf(
                    FieldKey.LANE to MARKSMAN_LANE,
                    FieldKey.GOLD_SHARE to "21%",
                    FieldKey.GOLD_FROM_FARMING to "2200",
                    FieldKey.LAST_HITS to "48",
                    FieldKey.KDA to "3/5/4",
                    FieldKey.DAMAGE_TAKEN_SHARE to "34%",
                    FieldKey.PARTICIPATION_RATE to "58%",
                    FieldKey.DAMAGE_SHARE to "22%"
                )
            ),
            screenshotPreview = ScreenshotPreviewState.Available("stored/shot-1.png")
        ).toDetailScreenUiState()

        assertTrue(state.marksmanInsights is MarksmanInsightsUiState.Eligible)
        state.marksmanInsights as MarksmanInsightsUiState.Eligible
        assertEquals(
            listOf(
                "Match Context",
                "Economy And Farming",
                "Output And Pressure",
                "Survival And Risk",
                "Teamfight Presence"
            ),
            state.marksmanInsights.metricGroups.map { it.title }
        )
        assertTrue(state.marksmanInsights.suggestions is MarksmanSuggestionsUiState.Suggestions)
        state.marksmanInsights.suggestions as MarksmanSuggestionsUiState.Suggestions
        assertEquals(3, state.marksmanInsights.suggestions.items.size)
        assertEquals(
            listOf(
                "Economy Rhythm",
                "Risk Discipline And Survival",
                "Follow-Team / Isolation"
            ),
            state.marksmanInsights.suggestions.items.map { it.categoryLabel }
        )
        assertTrue(state.marksmanInsights.suggestions.items.all { it.evidenceText.isNotBlank() })
    }

    @Test
    fun detailMapper_missingLaneShowsMarksmanInsufficientState() {
        val state = MatchDetailState(
            record = detailRecord(
                fields = detailFields() + (FieldKey.LANE to null)
            ),
            screenshotPreview = ScreenshotPreviewState.Available("stored/shot-1.png")
        ).toDetailScreenUiState()

        assertEquals(
            MarksmanInsightsUiState.Insufficient(
                message = "Not enough saved data to determine marksman lane insights."
            ),
            state.marksmanInsights
        )
    }

    @Test
    fun detailMapper_marksmanAnalysisFailureDegradesOnlyInsightsLayer() {
        val state = MatchDetailState(
            record = detailRecord(
                fields = detailFields() + (FieldKey.LANE to MARKSMAN_LANE)
            ),
            screenshotPreview = ScreenshotPreviewState.Available("stored/shot-1.png")
        ).toDetailScreenUiState(
            marksmanInsightsMapper = { throw IllegalStateException("boom") }
        )

        assertEquals(
            MarksmanInsightsUiState.Error(
                message = "Marksman lane insights are unavailable for this match."
            ),
            state.marksmanInsights
        )
        assertTrue(state.sections.any { it.title == "Damage Output" })
    }

    @Test
    fun dashboardMapper_promotesPrimaryMetricsAndSampleContext() {
        val metrics = DashboardMetricsCalculator().calculate(
            listOf(
                dashboardRecord("record-1", "victory"),
                dashboardRecord("record-2", "defeat"),
                dashboardRecord("record-3", "victory")
            )
        )

        val state = DashboardContentState.Loaded(metrics).toDashboardScreenUiState()

        assertEquals(listOf("Win Rate", "Average KDA", "Most Played Hero"), state.primaryCards.map { it.label })
        assertEquals("Based on 3 saved matches", state.contextText)
        assertNull(state.sparseDataText)
    }

    @Test
    fun dashboardMapper_distinguishesSparseDataAndPartialMetrics() {
        val metrics = DashboardMetricsCalculator().calculate(
            listOf(
                dashboardRecord(
                    "record-1",
                    "victory",
                    fields = detailFields() + (FieldKey.KILL_PARTICIPATION_COUNT to null)
                )
            )
        )

        val state = DashboardContentState.Loaded(metrics).toDashboardScreenUiState()

        assertEquals("Based on 1 saved match", state.contextText)
        assertEquals("Based on limited match history.", state.sparseDataText)
        assertTrue(state.secondaryNotes.contains("Some metrics need more saved data."))
    }

    @Test
    fun dashboardMapper_preservesMetricValuesAndEmptyFailureStates() {
        val metrics = DashboardMetricsCalculator().calculate(
            listOf(dashboardRecord("record-1", "victory"))
        )

        val loaded = DashboardContentState.Loaded(metrics).toDashboardScreenUiState()
        val empty = DashboardContentState.Empty.toDashboardScreenUiState()
        val error = DashboardContentState.Error("Could not load dashboard metrics.").toDashboardScreenUiState()

        assertEquals("100.0%", loaded.primaryCards.first().valueText)
        assertEquals(DashboardContentState.Empty, empty.content)
        assertTrue(error.content is DashboardContentState.Error)
        assertTrue(error.primaryCards.isEmpty())
    }
}

private fun detailRecord(
    fields: Map<FieldKey, String?> = detailFields()
): SavedMatchHistoryRecord {
    return SavedMatchHistoryRecord(
        recordId = "record-1",
        savedAt = 1_710_000_000_000L,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        fields = fields
    )
}

private fun dashboardRecord(
    recordId: String,
    result: String,
    fields: Map<FieldKey, String?> = detailFields()
): SavedMatchHistoryRecord {
    return detailRecord(
        fields = fields +
            (FieldKey.RESULT to result) +
            (FieldKey.HERO to "Sun Shangxiang") +
            (FieldKey.KDA to "11/1/5")
    ).copy(recordId = recordId)
}

private fun detailFields(): Map<FieldKey, String?> {
    return mapOf(
        FieldKey.RESULT to "victory",
        FieldKey.HERO to "Sun Shangxiang",
        FieldKey.PLAYER_NAME to "Player",
        FieldKey.LANE to "Farm Lane",
        FieldKey.SCORE to "20-10",
        FieldKey.KDA to "11/1/5",
        FieldKey.DAMAGE_DEALT to "12345",
        FieldKey.DAMAGE_SHARE to "34%",
        FieldKey.DAMAGE_TAKEN to "9876",
        FieldKey.DAMAGE_TAKEN_SHARE to "28%",
        FieldKey.TOTAL_GOLD to "12001",
        FieldKey.GOLD_SHARE to "31%",
        FieldKey.PARTICIPATION_RATE to "76%",
        FieldKey.GOLD_FROM_FARMING to "3500",
        FieldKey.LAST_HITS to "80",
        FieldKey.KILL_PARTICIPATION_COUNT to "13",
        FieldKey.CONTROL_DURATION to "00:14",
        FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
    )
}

private const val MARKSMAN_LANE = "\u53d1\u80b2\u8def"
