package com.kingsmetric

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.FakeUriScreenshotStorage
import com.kingsmetric.app.RoomRepositoryRecordStore
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedMatchEntity
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.TemplateValidator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppShellNavigationComposeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun reviewRoute_withoutDraft_returnsToImportWithMessage() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(),
                initialRoute = AppRoute.Review.path()
            )
        }

        composeRule.onNodeWithText("Review draft is no longer available.").assertIsDisplayed()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Import Match")
        composeRule.onNodeWithText("Select one supported screenshot to start review.").assertIsDisplayed()
    }

    @Test
    fun detailRoute_withoutRecordId_returnsToHistoryWithMessage() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(),
                initialRoute = "detail/"
            )
        }

        composeRule.onNodeWithText("Saved record is no longer available.").assertIsDisplayed()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Match History")
        composeRule.onNodeWithText("No saved matches yet. Save a reviewed match to see it here.").assertIsDisplayed()
    }

    @Test
    fun reviewSaveSuccess_invokesShellSuccessCallbackFromReviewRoute() {
        var saveCallbackFired = false

        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(recordStore = FakeRecordStore()),
                initialRoute = AppRoute.Review.path(),
                initialReviewDraft = AppShellTestFixtures.supportedDraft(),
                onReviewSaveSucceeded = { saveCallbackFired = true }
            )
        }

        composeRule.onNodeWithTag("confirm-save").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { saveCallbackFired }
    }

    @Test
    fun importToReviewToSave_completesSaveThroughTheRuntimeShell() {
        var saveCallbackFired = false

        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(recordStore = FakeRecordStore()),
                testImportedDraft = AppShellTestFixtures.supportedDraft(),
                onReviewSaveSucceeded = { saveCallbackFired = true }
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Use Test Draft").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Use Test Draft").performClick()
        composeRule.onNodeWithTag("confirm-save").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm-save").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { saveCallbackFired }
    }

    @Test
    fun successfulSave_withRealRepositoryPersistsRecordForHistory() {
        val repository = testRepository()
        var saveCallbackFired = false

        composeRule.setContent {
            HistoryDashboardRoot(
                repository = repository,
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.realRepositoryWorkflow(repository),
                initialRoute = AppRoute.Review.path(),
                initialReviewDraft = AppShellTestFixtures.supportedDraft(),
                onReviewSaveSucceeded = { saveCallbackFired = true }
            )
        }

        composeRule.onNodeWithTag("confirm-save").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { saveCallbackFired }
        composeRule.runOnIdle {
            org.junit.Assert.assertTrue(repository.hasSavedRecords())
        }
    }

    @Test
    fun appLaunch_shows_primary_navigation_and_active_destination() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow()
            )
        }

        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onNodeWithTag("nav-history", useUnmergedTree = true).assertIsNotSelected()
        composeRule.onNodeWithTag("nav-dashboard", useUnmergedTree = true).assertIsNotSelected()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Import Match")
    }

    @Test
    fun primaryNavigation_moves_between_import_history_and_dashboard_with_context() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow()
            )
        }

        composeRule.onNodeWithTag("nav-history", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("nav-history", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Match History")
        composeRule.onNodeWithText("No saved matches yet. Save a reviewed match to see it here.").assertIsDisplayed()

        composeRule.onNodeWithTag("nav-dashboard", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("nav-dashboard", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Dashboard")
        composeRule.onNodeWithText("No saved metrics yet. Save a reviewed match to see them here.").assertIsDisplayed()

        composeRule.onNodeWithTag("nav-import", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("nav-import", useUnmergedTree = true).assertIsSelected()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Import Match")
    }

    @Test
    fun reviewRoute_is_a_focused_task_screen_with_close_behavior() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(recordStore = FakeRecordStore()),
                testImportedDraft = AppShellTestFixtures.supportedDraft()
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Use Test Draft").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Use Test Draft").performClick()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Review Match")
        composeRule.onNodeWithTag("shell-secondary-action").assertIsDisplayed()
        composeRule.onNodeWithText("Close").assertIsDisplayed()
        composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(0)

        composeRule.onNodeWithTag("shell-secondary-action").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Import Match")
        composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun detailRoute_has_back_behavior_and_returns_to_history() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepositoryWithSavedRecord(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(),
                initialRoute = AppRoute.History.path()
            )
        }

        composeRule.onNodeWithTag("nav-history", useUnmergedTree = true).performClick()
        composeRule.onNodeWithTag("history-record-record-1").performClick()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Match Detail")
        composeRule.onNodeWithText("Back").assertIsDisplayed()
        composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(0)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(0)

        composeRule.onNodeWithTag("shell-secondary-action").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Match History")
        composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun shell_does_not_show_duplicated_competing_primary_navigation_controls() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow()
            )
        }

        composeRule.onAllNodesWithTag("nav-import", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-history", useUnmergedTree = true).assertCountEquals(1)
        composeRule.onAllNodesWithTag("nav-dashboard", useUnmergedTree = true).assertCountEquals(1)
    }
}

private fun testRepository(): RoomObservedMatchRepository {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(
        context,
        KingsMetricDatabase::class.java
    ).allowMainThreadQueries().build()
    return RoomObservedMatchRepository(
        dao = database.savedMatchDao(),
        screenshotFiles = object : LocalScreenshotFileStore {
            override fun exists(path: String): Boolean = false
        },
        recordIdProvider = object : RecordIdProvider {
            override fun nextId(): String = "record-1"
        },
        savedAtProvider = object : SavedAtProvider {
            override fun now(): Long = 1L
        }
    )
}

private fun testRepositoryWithSavedRecord(): RoomObservedMatchRepository {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(
        context,
        KingsMetricDatabase::class.java
    ).allowMainThreadQueries().build()
    database.savedMatchDao().insert(
        SavedMatchEntity(
            recordId = "record-1",
            savedAt = 1L,
            screenshotId = "shot-1",
            screenshotPath = "stored/shot-1.png",
            result = "victory",
            hero = "Sun Shangxiang",
            playerName = "Player",
            lane = "Farm Lane",
            score = "20-10",
            kda = "11/1/5",
            damageDealt = "12345",
            damageShare = "34%",
            damageTaken = "9876",
            damageTakenShare = "28%",
            totalGold = "12001",
            goldShare = "31%",
            participationRate = "76%",
            goldFromFarming = "3500",
            lastHits = "80",
            killParticipationCount = "13",
            controlDuration = "00:14",
            damageDealtToOpponents = "10101"
        )
    )
    return RoomObservedMatchRepository(
        dao = database.savedMatchDao(),
        screenshotFiles = object : LocalScreenshotFileStore {
            override fun exists(path: String): Boolean = false
        },
        recordIdProvider = object : RecordIdProvider {
            override fun nextId(): String = "record-2"
        },
        savedAtProvider = object : SavedAtProvider {
            override fun now(): Long = 2L
        }
    )
}

private object AppShellTestFixtures {
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

    fun supportedDraft(): DraftRecord {
        return parser.createDraft(
            analysis = com.kingsmetric.app.MlKitFixtures.supportedAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }
    fun realRepositoryWorkflow(repository: RoomObservedMatchRepository): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = RoomRepositoryRecordStore(repository),
            validator = TemplateValidator(),
            parser = parser
        )
    }
}
