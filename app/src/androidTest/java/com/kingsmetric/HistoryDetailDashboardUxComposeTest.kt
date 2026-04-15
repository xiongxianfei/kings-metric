package com.kingsmetric

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.DashboardCardUiState
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailFieldDisplayUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.DetailSectionUiState
import com.kingsmetric.app.HistoryRowUiState
import com.kingsmetric.app.HistoryScreenUiState
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
                            primaryText = "Sun Shangxiang",
                            resultText = "Victory",
                            recencyText = "Saved Apr 15, 2026",
                            previewText = null,
                            selectable = true
                        )
                    )
                ),
                onRecordSelected = {}
            )
        }

        composeRule.onNodeWithText("Sun Shangxiang").assertIsDisplayed()
        composeRule.onNodeWithText("Victory").assertIsDisplayed()
        composeRule.onNodeWithText("Saved Apr 15, 2026").assertIsDisplayed()
        composeRule.onNodeWithTag("history-record-record-1").assertIsDisplayed()
    }

    @Test
    fun historyScreen_missingHeroAndPreviewRemainReadable() {
        composeRule.setContent {
            HistoryScreen(
                state = HistoryScreenUiState(
                    content = HistoryContentState.Loaded(emptyList()),
                    rows = listOf(
                        HistoryRowUiState(
                            recordId = "record-2",
                            primaryText = "Hero not entered",
                            resultText = "Defeat",
                            recencyText = "Saved Apr 14, 2026",
                            previewText = "Preview unavailable",
                            selectable = true
                        )
                    )
                ),
                onRecordSelected = {}
            )
        }

        composeRule.onNodeWithText("Hero not entered").assertIsDisplayed()
        composeRule.onNodeWithText("Preview unavailable").assertIsDisplayed()
        composeRule.onNodeWithTag("history-record-record-2").assertIsDisplayed()
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
                    backLabel = "History",
                    previewStatusText = "Preview available",
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
                    backLabel = "History",
                    previewStatusText = "Preview unavailable",
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

        composeRule.onNodeWithText(
            "Screenshot preview unavailable. Match data is still available below."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Last Hits").assertIsDisplayed()
        composeRule.onNodeWithText("Not entered").assertIsDisplayed()
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

        composeRule.onNodeWithText("Win Rate").assertIsDisplayed()
        composeRule.onNodeWithText("66.7%").assertIsDisplayed()
        composeRule.onNodeWithText("Based on 2 saved matches").assertIsDisplayed()
        composeRule.onNodeWithText("Based on limited match history.").assertIsDisplayed()
        composeRule.onNodeWithText("Some metrics need more saved data.").assertIsDisplayed()
    }
}
