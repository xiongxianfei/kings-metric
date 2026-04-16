package com.kingsmetric

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.DashboardCardUiState
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailFieldDisplayUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.DetailSectionUiState
import com.kingsmetric.app.HistoryQuickSummaryItemUiState
import com.kingsmetric.app.HistoryQuickSummaryKind
import com.kingsmetric.app.HistoryRowUiState
import com.kingsmetric.app.HistoryScreenUiState
import com.kingsmetric.app.MarksmanInsightMetricGroupUiState
import com.kingsmetric.app.MarksmanInsightSuggestionItemUiState
import com.kingsmetric.app.MarksmanInsightsUiState
import com.kingsmetric.app.MarksmanSuggestionsUiState
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.history.HistoryContentState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDetailDashboardUxComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyScreen_showsScanFieldsAndRecencyPerRow() {
        composeRule.setContent {
            HistoryScreen(
                state = HistoryScreenUiState(
                    content = HistoryContentState.Loaded(emptyList()),
                    rows = listOf(
                        HistoryRowUiState(
                            recordId = "record-1",
                            categoryLabel = "Saved match",
                            primaryText = "Sun Shangxiang",
                            resultText = "Victory",
                            quickSummaryItems = listOf(
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Victory"),
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.LANE, "Farm Lane"),
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.KDA, "11/1/5"),
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.SCORE, "20-10")
                            ),
                            recencyText = "Saved Apr 15, 2026",
                            previewText = null,
                            selectable = true
                        )
                    )
                ),
                onRecordSelected = {}
            )
        }

        composeRule.onNodeWithText("Saved match").assertIsDisplayed()
        composeRule.onNodeWithText("Sun Shangxiang").assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-1-result", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-1-lane", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-1-kda", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-1-score", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Victory").assertIsDisplayed()
        composeRule.onNodeWithText("Farm Lane").assertIsDisplayed()
        composeRule.onNodeWithText("11/1/5").assertIsDisplayed()
        composeRule.onNodeWithText("20-10").assertIsDisplayed()
        composeRule.onNodeWithText("Saved Apr 15, 2026").assertIsDisplayed()
        composeRule.onNodeWithTag("history-record-record-1").assertIsDisplayed()
    }

    @Test
    fun historyScreen_missingOptionalSummaryItemsRemainReadableAndOrdered() {
        composeRule.setContent {
            HistoryScreen(
                state = HistoryScreenUiState(
                    content = HistoryContentState.Loaded(emptyList()),
                    rows = listOf(
                        HistoryRowUiState(
                            recordId = "record-2",
                            categoryLabel = "Saved match",
                            primaryText = "Sun Shangxiang",
                            resultText = "Defeat",
                            quickSummaryItems = listOf(
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Defeat"),
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.KDA, "5/3/7")
                            ),
                            recencyText = "Saved Apr 14, 2026",
                            previewText = "Preview unavailable",
                            selectable = true
                        )
                    )
                ),
                onRecordSelected = {}
            )
        }

        composeRule.onNodeWithText("Sun Shangxiang").assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-2", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-2-result", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-2-kda", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Defeat").assertIsDisplayed()
        composeRule.onNodeWithText("5/3/7").assertIsDisplayed()
        composeRule.onNodeWithText("Preview unavailable").assertIsDisplayed()
        composeRule.onNodeWithTag("history-record-record-2").assertIsDisplayed()
    }

    @Test
    fun historyScreen_fullyReducedRowKeepsFallbacksAndRecencyOnly() {
        composeRule.setContent {
            HistoryScreen(
                state = HistoryScreenUiState(
                    content = HistoryContentState.Loaded(emptyList()),
                    rows = listOf(
                        HistoryRowUiState(
                            recordId = "record-3",
                            categoryLabel = "Saved match",
                            primaryText = "Hero not entered",
                            resultText = "Result not entered",
                            quickSummaryItems = listOf(
                                HistoryQuickSummaryItemUiState(HistoryQuickSummaryKind.RESULT, "Result not entered")
                            ),
                            recencyText = "Saved Apr 13, 2026",
                            previewText = null,
                            selectable = true
                        )
                    )
                ),
                onRecordSelected = {}
            )
        }

        composeRule.onNodeWithText("Hero not entered").assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-3", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("history-row-summary-record-3-result", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Result not entered").assertIsDisplayed()
        composeRule.onNodeWithText("Saved Apr 13, 2026").assertIsDisplayed()
    }

    @Test
    fun detailScreen_showsSummaryAndGroupedSections() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = "stored/shot-1.png",
                    previewAvailability = PreviewAvailability.Available,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    summaryMetaText = "Saved Apr 15, 2026",
                    backLabel = "History",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot available",
                    marksmanInsights = MarksmanInsightsUiState.Eligible(
                        metricGroups = listOf(
                            MarksmanInsightMetricGroupUiState(
                                title = "Economy And Farming",
                                statusText = null,
                                metrics = listOf(
                                    DetailFieldDisplayUiState("Gold Share", "21%"),
                                    DetailFieldDisplayUiState("Last Hits", "48")
                                )
                            )
                        ),
                        suggestions = MarksmanSuggestionsUiState.Suggestions(
                            items = listOf(
                                MarksmanInsightSuggestionItemUiState(
                                    categoryLabel = "Economy Rhythm",
                                    title = "Economy Rhythm",
                                    rationale = "Your farming pace lagged behind a stable marksman lane curve in this match.",
                                    evidenceText = "Gold share 21%, farming gold 2200, last hits 48."
                                )
                            )
                        )
                    ),
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Match Summary",
                            fields = listOf(
                                DetailFieldDisplayUiState("Player", "Summoner One"),
                                DetailFieldDisplayUiState("KDA Ratio", "11/1/5")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithText("Sun Shangxiang").assertIsDisplayed()
        composeRule.onNodeWithText("Victory").assertIsDisplayed()
        composeRule.onNodeWithText("Saved Apr 15, 2026").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-summary-card").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-preview-card").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-marksman-section").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-marksman-group-economy-and-farming").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-marksman-suggestions").assertIsDisplayed()
        composeRule.onNodeWithText("Marksman Lane Insights").assertIsDisplayed()
        composeRule.onNodeWithText("Economy Rhythm").assertIsDisplayed()
        composeRule.onNodeWithText("Gold share 21%, farming gold 2200, last hits 48.").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshot available").assertIsDisplayed()
        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Player").assertIsDisplayed()
        composeRule.onNodeWithText("Summoner One").assertIsDisplayed()
        composeRule.onNodeWithText("11/1/5").assertIsDisplayed()
    }

    @Test
    fun detailScreen_missingPreviewKeepsFieldDataVisible() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = null,
                    previewAvailability = PreviewAvailability.Unavailable,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    summaryMetaText = "Saved Apr 15, 2026",
                    backLabel = "History",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot preview unavailable",
                    marksmanInsights = MarksmanInsightsUiState.Unavailable(
                        message = "Marksman lane insights are unavailable for this lane."
                    ),
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Economy",
                            fields = listOf(
                                DetailFieldDisplayUiState("Last Hits", "Not entered")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithTag("detail-preview-card").assertIsDisplayed()
        composeRule.onNodeWithTag("detail-marksman-state").assertIsDisplayed()
        composeRule.onNodeWithText("Marksman lane insights are unavailable for this lane.").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshot preview unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Last Hits").assertIsDisplayed()
        composeRule.onNodeWithText("Not entered").assertIsDisplayed()
    }

    @Test
    fun detailScreen_missingLaneShowsMarksmanInsufficientStateAndKeepsRawDetailVisible() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = "stored/shot-1.png",
                    previewAvailability = PreviewAvailability.Available,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    summaryMetaText = "Saved Apr 15, 2026",
                    backLabel = "History",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot available",
                    marksmanInsights = MarksmanInsightsUiState.Insufficient(
                        message = "Not enough saved data to determine marksman lane insights."
                    ),
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Team Play",
                            fields = listOf(
                                DetailFieldDisplayUiState("Participation Rate", "Not entered")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithTag("detail-marksman-state").assertIsDisplayed()
        composeRule.onNodeWithText("Not enough saved data to determine marksman lane insights.").assertIsDisplayed()
        composeRule.onNodeWithText("Team Play").assertIsDisplayed()
        composeRule.onNodeWithText("Participation Rate").assertIsDisplayed()
    }

    @Test
    fun detailScreen_marksmanNeutralStateRendersWithoutLeavingAnEmptyGap() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = "stored/shot-1.png",
                    previewAvailability = PreviewAvailability.Available,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    summaryMetaText = "Saved Apr 15, 2026",
                    backLabel = "History",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot available",
                    marksmanInsights = MarksmanInsightsUiState.Eligible(
                        metricGroups = listOf(
                            MarksmanInsightMetricGroupUiState(
                                title = "Match Context",
                                statusText = null,
                                metrics = listOf(
                                    DetailFieldDisplayUiState("Lane", "发育路"),
                                    DetailFieldDisplayUiState("Score", "20-10")
                                )
                            )
                        ),
                        suggestions = MarksmanSuggestionsUiState.Neutral(
                            message = "No high-priority marksman suggestions for this match."
                        )
                    ),
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Match Summary",
                            fields = listOf(
                                DetailFieldDisplayUiState("Player", "Summoner One")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithTag("detail-marksman-suggestions").assertIsDisplayed()
        composeRule.onNodeWithText("No high-priority marksman suggestions for this match.").assertIsDisplayed()
        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
    }

    @Test
    fun detailScreen_marksmanPartialDataShowsReadableUnavailability() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = "stored/shot-1.png",
                    previewAvailability = PreviewAvailability.Available,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    summaryMetaText = "Saved Apr 15, 2026",
                    backLabel = "History",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot available",
                    marksmanInsights = MarksmanInsightsUiState.Eligible(
                        metricGroups = listOf(
                            MarksmanInsightMetricGroupUiState(
                                title = "Economy And Farming",
                                statusText = "Some saved metrics are unavailable for this group.",
                                metrics = listOf(
                                    DetailFieldDisplayUiState("Gold Share", "21%"),
                                    DetailFieldDisplayUiState("Gold from Farming", "Not enough saved data")
                                )
                            )
                        ),
                        suggestions = MarksmanSuggestionsUiState.Neutral(
                            message = "No high-priority marksman suggestions for this match."
                        )
                    ),
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Economy",
                            fields = listOf(
                                DetailFieldDisplayUiState("Gold Share", "21%")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithText("Some saved metrics are unavailable for this group.").assertIsDisplayed()
        composeRule.onNodeWithText("Gold from Farming").assertIsDisplayed()
        composeRule.onNodeWithText("Not enough saved data").assertIsDisplayed()
        composeRule.onNodeWithText("Economy").assertIsDisplayed()
    }

    @Test
    fun detailScreen_phoneSizedViewport_canReachFullDamageOutputSection() {
        composeRule.setContent {
            Box(modifier = androidx.compose.ui.Modifier.height(320.dp)) {
                RecordDetailScreen(
                    state = fullDetailScreenState()
                )
            }
        }

        composeRule.onNodeWithTag("detail-scroll")
            .performScrollToNode(hasText("Damage Output"))

        composeRule.onNodeWithText("Damage Output").assertIsDisplayed()

        composeRule.onNodeWithTag("detail-scroll")
            .performScrollToNode(hasTestTag("detail-section-damage-output"))

        composeRule.onNodeWithTag("detail-section-damage-output").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsPrimaryCardsContextAndSparseNote() {
        composeRule.setContent {
            DashboardScreen(
                state = DashboardScreenUiState(
                    content = DashboardContentState.Loaded(
                        metrics = com.kingsmetric.dashboard.DashboardMetricsCalculator().calculate(emptyList())
                    ),
                    primaryCards = listOf(
                        DashboardCardUiState("Win Rate", "66.7%"),
                        DashboardCardUiState("Average KDA", "13.0"),
                        DashboardCardUiState("Most Played Hero", "Sun Shangxiang")
                    ),
                    contextText = "Based on 2 saved matches",
                    sparseDataText = "Based on limited match history.",
                    secondaryNotes = listOf("Some metrics need more saved data.")
                )
            )
        }

        composeRule.onNodeWithTag("dashboard-primary-section").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-context-card").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-sparse-card").assertIsDisplayed()
        composeRule.onNodeWithText("Win Rate").assertIsDisplayed()
        composeRule.onNodeWithText("66.7%").assertIsDisplayed()
        composeRule.onNodeWithText("Current metrics").assertIsDisplayed()
        composeRule.onNodeWithText("Based on 2 saved matches").assertIsDisplayed()
        composeRule.onNodeWithText("Based on limited match history.").assertIsDisplayed()
        composeRule.onNodeWithText("Some metrics need more saved data.").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_emptyState_staysClear() {
        composeRule.setContent {
            DashboardScreen(
                state = DashboardScreenUiState(
                    content = DashboardContentState.Empty
                )
            )
        }

        composeRule.onNodeWithTag("dashboard-empty-state").assertIsDisplayed()
        composeRule.onNodeWithText("No saved metrics yet. Save a reviewed match to see them here.").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_failureState_staysVisible() {
        composeRule.setContent {
            DashboardScreen(
                state = DashboardScreenUiState(
                    content = DashboardContentState.Error("Could not load dashboard metrics.")
                )
            )
        }

        composeRule.onNodeWithTag("dashboard-error-state").assertIsDisplayed()
        composeRule.onNodeWithText("Could not load dashboard metrics.").assertIsDisplayed()
    }
}

private fun fullDetailScreenState(): DetailScreenUiState {
    return DetailScreenUiState(
        recordId = "record-1",
        screenshotPath = "stored/shot-1.png",
        previewAvailability = PreviewAvailability.Available,
        summaryTitle = "Sun Shangxiang",
        summaryResult = "Victory",
        summaryMetaText = "Saved Apr 15, 2026",
        backLabel = "History",
        previewStatusLabel = "Screenshot",
        previewStatusText = "Screenshot available",
        marksmanInsights = MarksmanInsightsUiState.Eligible(
            metricGroups = listOf(
                MarksmanInsightMetricGroupUiState(
                    title = "Output And Pressure",
                    statusText = null,
                    metrics = listOf(
                        DetailFieldDisplayUiState("Damage Share", "34%"),
                        DetailFieldDisplayUiState("Damage to Opponents", "10101")
                    )
                )
            ),
            suggestions = MarksmanSuggestionsUiState.Suggestions(
                items = listOf(
                    MarksmanInsightSuggestionItemUiState(
                        categoryLabel = "Output Contribution",
                        title = "Output Contribution",
                        rationale = "Your damage contribution stayed low for a marksman lane slot in this match.",
                        evidenceText = "Damage share 22%, damage to opponents 10101."
                    )
                )
            )
        ),
        sections = listOf(
            DetailSectionUiState(
                title = "Match Summary",
                fields = listOf(
                    DetailFieldDisplayUiState("Player", "Summoner One"),
                    DetailFieldDisplayUiState("Lane", "Farm Lane"),
                    DetailFieldDisplayUiState("Score", "20-10"),
                    DetailFieldDisplayUiState("KDA Ratio", "11/1/5")
                )
            ),
            DetailSectionUiState(
                title = "Damage Output",
                fields = listOf(
                    DetailFieldDisplayUiState("Damage Dealt", "12345"),
                    DetailFieldDisplayUiState("Damage Share", "34%"),
                    DetailFieldDisplayUiState("Damage to Opponents", "10101")
                )
            ),
            DetailSectionUiState(
                title = "Survivability",
                fields = listOf(
                    DetailFieldDisplayUiState("Damage Taken", "9876"),
                    DetailFieldDisplayUiState("Damage Taken Share", "28%"),
                    DetailFieldDisplayUiState("Control Duration", "00:14")
                )
            ),
            DetailSectionUiState(
                title = "Economy",
                fields = listOf(
                    DetailFieldDisplayUiState("Total Gold", "12001"),
                    DetailFieldDisplayUiState("Gold Share", "31%"),
                    DetailFieldDisplayUiState("Gold from Farming", "3500"),
                    DetailFieldDisplayUiState("Last Hits", "80")
                )
            ),
            DetailSectionUiState(
                title = "Team Play",
                fields = listOf(
                    DetailFieldDisplayUiState("Participation Rate", "76%"),
                    DetailFieldDisplayUiState("Kill Participation Count", "13")
                )
            )
        )
    )
}
