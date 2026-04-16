package com.kingsmetric

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.AndroidPhotoPickerImportAdapter
import com.kingsmetric.app.AndroidPhotoPickerRuntime
import com.kingsmetric.app.DashboardCardUiState
import com.kingsmetric.app.DashboardGraphKind
import com.kingsmetric.app.DashboardGraphPanelUiState
import com.kingsmetric.app.DashboardGraphSectionUiState
import com.kingsmetric.app.DashboardHeroUsageBarUiState
import com.kingsmetric.app.DashboardRecentResultPointUiState
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailFieldDisplayUiState
import com.kingsmetric.app.DetailSectionUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.FakeUriScreenshotStorage
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.app.ReviewScreenRoute
import com.kingsmetric.app.ReviewScreenViewModel
import com.kingsmetric.app.toDashboardScreenUiState
import com.kingsmetric.app.toHistoryScreenUiState
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.TemplateValidator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedUxLabelsAndStateMessagingComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun reviewScreenRendersSharedUserFacingLabels() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = sharedUxReviewViewModel(draft = SharedUxLabelsFixtures.supportedDraft())
            )
        }

        composeRule.onNodeWithText("Hero").assertIsDisplayed()
        composeRule.onAllNodesWithText("HERO").assertCountEquals(0)
    }

    @Test
    fun importUnsupportedStateUsesRetryOrientedSharedWording() {
        val runtime = unsupportedRuntime()

        composeRule.setContent {
            ImportScreen(
                runtime = runtime,
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithText(
            "This screenshot isn't supported. Try another post-match personal stats screenshot."
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            "Image does not match the supported personal-stats template."
        ).assertCountEquals(0)
    }

    @Test
    fun historyAndDashboardEmptyStatesUseSameNoDataStyle() {
        composeRule.setContent {
            Column {
                HistoryScreen(state = HistoryContentState.Empty.toHistoryScreenUiState(), onRecordSelected = {})
                DashboardScreen(state = DashboardContentState.Empty.toDashboardScreenUiState())
            }
        }

        composeRule.onNodeWithText(
            "No saved matches yet. Save a reviewed match to see it here."
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            "No saved metrics yet. Save a reviewed match to see them here."
        ).assertIsDisplayed()
    }

    @Test
    fun detailMissingPreviewUsesSharedFallbackWording() {
        composeRule.setContent {
            RecordDetailScreen(
                state = DetailScreenUiState(
                    recordId = "record-1",
                    screenshotPath = null,
                    previewAvailability = PreviewAvailability.Unavailable,
                    summaryTitle = "Sun Shangxiang",
                    summaryResult = "Victory",
                    previewStatusLabel = "Screenshot",
                    previewStatusText = "Screenshot preview unavailable",
                    sections = listOf(
                        DetailSectionUiState(
                            title = "Match Summary",
                            fields = listOf(
                                DetailFieldDisplayUiState("Hero", "Sun Shangxiang"),
                                DetailFieldDisplayUiState("KDA Ratio", "11/1/5")
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithText(
            "Screenshot preview unavailable"
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Hero").assertIsDisplayed()
        composeRule.onAllNodesWithText("Sun Shangxiang").assertCountEquals(2)
        composeRule.onAllNodesWithText("HERO").assertCountEquals(0)
    }

    @Test
    fun dashboardGraphStatesUseSharedUserFacingWording() {
        composeRule.setContent {
            DashboardScreen(
                state = DashboardScreenUiState(
                    content = DashboardContentState.Loaded(
                        metrics = com.kingsmetric.dashboard.DashboardMetricsCalculator().calculate(emptyList())
                    ),
                    primaryCards = listOf(
                        DashboardCardUiState("Win Rate", "100.0%"),
                        DashboardCardUiState("Average KDA", "13.0"),
                        DashboardCardUiState("Most Played Hero", "Sun Shangxiang")
                    ),
                    graphSection = DashboardGraphSectionUiState(
                        panels = listOf(
                            DashboardGraphPanelUiState.RecentResults(
                                title = "Recent Results",
                                points = listOf(
                                    DashboardRecentResultPointUiState(
                                        recordId = "record-1",
                                        isVictory = true,
                                        resultLabel = "Victory"
                                    )
                                )
                            ),
                            DashboardGraphPanelUiState.Unavailable(
                                kind = DashboardGraphKind.HeroUsage,
                                title = "Hero Usage",
                                message = "Hero usage graph is unavailable for the current saved matches."
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithText("Recent Results").assertIsDisplayed()
        composeRule.onNodeWithText("Hero Usage").assertIsDisplayed()
        composeRule.onNodeWithText("Hero usage graph is unavailable for the current saved matches.").assertIsDisplayed()
        composeRule.onAllNodesWithText("dashboard_graph_unavailable").assertCountEquals(0)
    }
}

private fun sharedUxReviewViewModel(draft: DraftRecord): ReviewScreenViewModel {
    return ReviewScreenViewModel(
        draft = draft,
        workflow = MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = FakeRecordStore(),
            validator = TemplateValidator(),
            parser = DraftParser()
        ),
        previewAvailableResolver = { false }
    )
}

private fun unsupportedRuntime(): AndroidPhotoPickerRuntime {
    val runtime = AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = {
            ImportResult.Unsupported("Image does not match the supported personal-stats template.")
        }
    )
    runtime.handlePickerResult("content://shots/unsupported")
    return runtime
}

private object SharedUxLabelsFixtures {
    private val parser = DraftParser()

    fun supportedDraft(): DraftRecord {
        return parser.createDraft(
            analysis = com.kingsmetric.app.MlKitFixtures.supportedAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }
}
