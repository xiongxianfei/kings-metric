package com.kingsmetric

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.FakeUriScreenshotStorage
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
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
        composeRule.onNodeWithText("Select one screenshot to import.").assertIsDisplayed()
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
        composeRule.onNodeWithText("No saved matches yet.").assertIsDisplayed()
    }

    @Test
    fun reviewSaveSuccess_navigatesToHistoryDestination() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow(recordStore = FakeRecordStore()),
                initialRoute = AppRoute.Review.path(),
                initialReviewDraft = AppShellTestFixtures.supportedDraft()
            )
        }

        composeRule.onNodeWithTag("confirm-save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("No saved matches yet.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("No saved matches yet.").assertIsDisplayed()
    }

    @Test
    fun importToReviewToSave_navigatesThroughTheRuntimeShell() {
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
        composeRule.onNodeWithTag("confirm-save").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm-save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("No saved matches yet.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("No saved matches yet.").assertIsDisplayed()
    }

    @Test
    fun emptyLaunch_allowsNavigationToHistoryAndDashboardEmptyStates() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = testRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled },
                reviewWorkflow = AppShellTestFixtures.workflow()
            )
        }

        composeRule.onNodeWithText("History").performClick()
        composeRule.onNodeWithText("No saved matches yet.").assertIsDisplayed()

        composeRule.onNodeWithText("Dashboard").performClick()
        composeRule.onNodeWithText("No saved metrics yet.").assertIsDisplayed()
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
}
