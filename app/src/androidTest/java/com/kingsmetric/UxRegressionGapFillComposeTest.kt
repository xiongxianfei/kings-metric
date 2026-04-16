package com.kingsmetric

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
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
import com.kingsmetric.data.local.SavedMatchEntity
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
class UxRegressionGapFillComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun blockedReviewWithSeveralRequiredFieldsKeepsBlockersVisible() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = uxGapRepository(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { ImportResult.Cancelled },
                reviewWorkflow = uxGapWorkflow(),
                initialRoute = AppRoute.Review.path(),
                initialReviewDraft = uxGapDraft(
                    missingRequiredFields = setOf(FieldKey.KDA, FieldKey.DAMAGE_DEALT)
                )
            )
        }

        composeRule.onNodeWithText("Review Match").assertIsDisplayed()
        composeRule.onNodeWithText("Complete the required fields before saving.").assertIsDisplayed()
        composeRule.onNodeWithText("Required fields: Damage Dealt, KDA Ratio").assertIsDisplayed()
        composeRule.onNodeWithText("Review next: Match Summary, Damage Output").assertIsDisplayed()
        composeRule.onNodeWithTag("field-KDA").assertIsDisplayed()
        composeRule.onNodeWithTag("field-DAMAGE_DEALT").assertExists()
        composeRule.onNodeWithTag("confirm-save").assertIsNotEnabled()
    }

    @Test
    fun historyToDetailWithMissingPreviewKeepsFallbackClear() {
        composeRule.setContent {
            HistoryDashboardRoot(
                repository = uxGapRepositoryWithSavedRecord(),
                uriStorage = FakeUriScreenshotStorage(),
                recognizeStoredScreenshot = { ImportResult.Cancelled },
                reviewWorkflow = uxGapWorkflow(),
                initialRoute = AppRoute.History.path()
            )
        }

        composeRule.onNodeWithText("Preview unavailable").assertIsDisplayed()
        composeRule.onNodeWithTag("history-record-record-1").performClick()
        composeRule.onNodeWithTag("shell-title").assertTextEquals("Match Detail")
        composeRule.onNodeWithTag("detail-preview-card").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshot preview unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Back").assertIsDisplayed()
    }
}

private fun uxGapRepository(): RoomObservedMatchRepository {
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

private fun uxGapRepositoryWithSavedRecord(): RoomObservedMatchRepository {
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

private fun uxGapWorkflow(): MatchImportWorkflow {
    return MatchImportWorkflow(
        screenshotStore = FakeScreenshotStore(),
        analyzer = FakeScreenshotAnalyzer(emptyMap()),
        recordStore = FakeRecordStore(),
        validator = TemplateValidator(),
        parser = DraftParser()
    )
}

private fun uxGapDraft(missingRequiredFields: Set<FieldKey>): DraftRecord {
    val parser = DraftParser()
    return parser.createDraft(
        analysis = com.kingsmetric.app.MlKitFixtures.supportedAnalysis(
            visibleFields = FieldKey.all - missingRequiredFields
        ),
        screenshotId = "shot-1",
        screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
    )
}
