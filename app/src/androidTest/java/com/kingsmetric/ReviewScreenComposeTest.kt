package com.kingsmetric

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import com.kingsmetric.app.ReviewScreenRoute
import com.kingsmetric.app.ReviewScreenViewModel
import com.kingsmetric.app.RoomRepositoryRecordStore
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeRecordStore
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import com.kingsmetric.importflow.TemplateValidator
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ReviewScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reviewScreen_showsBlockingRequiredFieldAndDisablesConfirm() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(draft = ReviewScreenFixtures.requiredMissingDraft())
            )
        }

        composeRule.onNodeWithText("Complete the required fields before saving.").assertIsDisplayed()
        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Review next: Match Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Required fields: KDA Ratio").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm-save").assertIsNotEnabled()
    }

    @Test
    fun reviewScreen_optionalHighlightDoesNotBlockConfirm() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(draft = ReviewScreenFixtures.optionalMissingDraft())
            )
        }

        composeRule.onNodeWithText("Check highlighted fields before saving.").assertIsDisplayed()
        composeRule.onNodeWithTag("field-LAST_HITS").assertExists()
        composeRule.onNodeWithText("Economy").assertExists()
        composeRule.onNodeWithTag("confirm-save").assertIsEnabled()
    }

    @Test
    fun reviewScreen_groupsFieldsIntoSectionsWithVisibleLabels() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(draft = ReviewScreenFixtures.supportedDraft())
            )
        }

        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
        composeRule.onNodeWithText("Damage Output").assertExists()
        composeRule.onNodeWithText("Survivability").assertExists()
        composeRule.onNodeWithText("Economy").assertExists()
        composeRule.onNodeWithText("Team Play").assertExists()
        composeRule.onAllNodesWithText("Required field").assertCountEquals(13)
        composeRule.onAllNodesWithText("Optional field").assertCountEquals(5)
        composeRule.onAllNodesWithTag("review-section").assertCountEquals(5)
    }

    @Test
    fun reviewScreen_missingPreviewStillShowsFieldData() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft(),
                    previewAvailable = false
                )
            )
        }

        composeRule.onNodeWithText(
            "Screenshot preview unavailable. Match data is still available below."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Match Summary").assertIsDisplayed()
        composeRule.onNodeWithTag("field-RESULT").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_saveFailureKeepsEditsIntact() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft(),
                    recordStore = FakeRecordStore(shouldFail = true)
                )
            )
        }

        composeRule.onNodeWithTag("field-LANE").performTextClearance()
        composeRule.onNodeWithTag("field-LANE").performTextInput("Farm Lane")
        composeRule.onNodeWithTag("confirm-save").performClick()

        composeRule.onNodeWithText("Could not save this match locally. Try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Farm Lane").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_tallPreviewStillShowsConfirmAndFields() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val previewFile = createPreviewImage(context, "review-preview.png")

        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft().copy(
                        screenshotPath = previewFile.absolutePath
                    )
                )
            )
        }

        composeRule.onNodeWithTag("confirm-save").assertIsDisplayed()
        composeRule.onNodeWithTag("field-RESULT").assertIsDisplayed()
        composeRule.onNodeWithText("Screenshot preview").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_stickySaveActionRemainsVisibleWithLongGroupedForm() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.requiredMissingDraft()
                )
            )
        }

        composeRule.onNodeWithTag("confirm-save").assertIsDisplayed()
        composeRule.onNodeWithTag("field-DAMAGE_DEALT_TO_OPPONENTS").assertExists()
        composeRule.onNodeWithText("Complete the required fields before saving.").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_ambiguousFieldsShowConciseInputHints() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft()
                )
            )
        }

        composeRule.onAllNodesWithText("Example: 34%").assertCountEquals(4)
        composeRule.onAllNodesWithText("Whole number").assertCountEquals(7)
        composeRule.onAllNodesWithText("Example: 00:14").assertCountEquals(1)
    }

    @Test
    fun reviewScreen_editingKeepsFieldAndSaveActionReachable() {
        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.requiredMissingDraft()
                )
            )
        }

        composeRule.onNodeWithTag("field-DAMAGE_DEALT").performClick()
        composeRule.onNodeWithTag("field-DAMAGE_DEALT").performTextInput("12345")
        composeRule.onNodeWithTag("field-DAMAGE_DEALT").performImeAction()

        composeRule.onNodeWithTag("confirm-save").assertIsDisplayed()
        composeRule.onNodeWithTag("field-DAMAGE_DEALT").assertIsDisplayed()
    }

    @Test
    fun reviewScreen_realRepositorySaveCompletesWithoutLocalSaveError() {
        var saveSucceeded = false

        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft(),
                    workflow = ReviewScreenFixtures.realRepositoryWorkflow()
                ),
                onSaveSucceeded = { saveSucceeded = true }
            )
        }

        composeRule.onNodeWithTag("confirm-save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { saveSucceeded }
        composeRule.onNodeWithText("Could not save this match locally. Try again.").assertDoesNotExist()
    }

    @Test
    fun reviewScreen_successfulSaveCallsRouteCallback() {
        var saveSucceeded = false

        composeRule.setContent {
            ReviewScreenRoute(
                viewModel = reviewViewModel(
                    draft = ReviewScreenFixtures.supportedDraft()
                ),
                onSaveSucceeded = { saveSucceeded = true }
            )
        }

        composeRule.onNodeWithTag("confirm-save").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { saveSucceeded }
    }
}

private fun reviewViewModel(
    draft: DraftRecord,
    previewAvailable: Boolean = true,
    recordStore: FakeRecordStore = FakeRecordStore(),
    workflow: MatchImportWorkflow = ReviewScreenFixtures.workflow(recordStore)
): ReviewScreenViewModel {
    return ReviewScreenViewModel(
        draft = draft,
        workflow = workflow,
        previewAvailableResolver = { previewAvailable }
    )
}

private object ReviewScreenFixtures {
    private val parser = DraftParser()

    fun workflow(recordStore: FakeRecordStore): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = recordStore,
            validator = TemplateValidator(),
            parser = parser
        )
    }

    fun realRepositoryWorkflow(): MatchImportWorkflow {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val database = Room.inMemoryDatabaseBuilder(
            context,
            KingsMetricDatabase::class.java
        ).build()
        val repository = RoomObservedMatchRepository(
            dao = database.savedMatchDao(),
            screenshotFiles = object : LocalScreenshotFileStore {
                override fun exists(path: String): Boolean = true
            },
            recordIdProvider = object : RecordIdProvider {
                override fun nextId(): String = "record-1"
            },
            savedAtProvider = object : SavedAtProvider {
                override fun now(): Long = 1L
            }
        )
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = RoomRepositoryRecordStore(repository),
            validator = TemplateValidator(),
            parser = parser
        )
    }

    fun supportedDraft(): DraftRecord {
        return parser.createDraft(
            analysis = supportedAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }

    fun optionalMissingDraft(): DraftRecord {
        return parser.createDraft(
            analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }

    fun requiredMissingDraft(): DraftRecord {
        return parser.createDraft(
            analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.KDA),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }

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

private fun createPreviewImage(
    context: android.content.Context,
    name: String
): File {
    val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(Color.BLUE)
    val file = File(context.cacheDir, name)
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    bitmap.recycle()
    return file
}
