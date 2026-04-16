package com.kingsmetric

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.AndroidPhotoPickerImportAdapter
import com.kingsmetric.app.AndroidPhotoPickerRuntime
import com.kingsmetric.app.FakeUriScreenshotStorage
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.StoredScreenshot
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun firstOpen_explains_supported_screenshot_type_and_import_action() {
        composeRule.setContent {
            ImportScreen(
                runtime = idleRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithText(
            "Supported screenshot: one Chinese post-match personal stats detailed-data screen."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("import-supported-card").assertIsDisplayed()
        composeRule.onNodeWithTag("import-status-block").assertIsDisplayed()
        composeRule.onNodeWithText("Next: choose one supported screenshot to start review.").assertIsDisplayed()
        composeRule.onNodeWithTag("import-primary-action").assertIsDisplayed()
    }

    @Test
    fun unsupportedResult_shows_clear_mismatch_guidance_and_retry_path() {
        composeRule.setContent {
            ImportScreen(
                runtime = unsupportedRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithText("Unsupported Screenshot").assertIsDisplayed()
        composeRule.onNodeWithText("This screenshot isn't supported. Try another post-match personal stats screenshot.").assertIsDisplayed()
        composeRule.onNodeWithText("Next: try another supported post-match personal stats screenshot.").assertIsDisplayed()
        composeRule.onNodeWithTag("import-primary-action").assertIsDisplayed()
    }

    @Test
    fun unreadableSourceFailure_shows_source_guidance_without_unsupported_label() {
        composeRule.setContent {
            ImportScreen(
                runtime = unreadableRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithText("Can't Read Selected Screenshot").assertIsDisplayed()
        composeRule.onNodeWithText("The selected screenshot could not be imported. Try another image.").assertIsDisplayed()
        composeRule.onNodeWithText("Next: try another image from your device.").assertIsDisplayed()
    }

    @Test
    fun localSaveFailure_shows_local_storage_guidance() {
        composeRule.setContent {
            ImportScreen(
                runtime = storageFailureRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithText("Couldn't Save Screenshot").assertIsDisplayed()
        composeRule.onNodeWithText("The screenshot could not be saved locally. Try again.").assertIsDisplayed()
        composeRule.onNodeWithText("Next: try the import again.").assertIsDisplayed()
    }

    @Test
    fun reviewReadyState_exposesContinueReviewAction() {
        val runtime = readyRuntime()
        var selectedDraft: DraftRecord? = null

        composeRule.setContent {
            ImportScreen(
                runtime = runtime,
                onReviewDraftReady = { draft -> selectedDraft = draft }
            )
        }

        composeRule.onNodeWithText("Next: continue into review to verify and save the match.").assertIsDisplayed()
        composeRule.onAllNodesWithTag("import-primary-action").assertCountEquals(0)
        composeRule.onNodeWithText("Continue Review").assertIsDisplayed()
        composeRule.onNodeWithText("Continue Review").performClick()
        composeRule.runOnIdle {
            assertNotNull(selectedDraft)
        }
    }

    @Test
    fun repeatedFailures_still_leave_import_action_obvious() {
        composeRule.setContent {
            ImportScreen(
                runtime = unsupportedRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithTag("import-primary-action").assertIsDisplayed()
    }

    @Test
    fun pickerCancellation_leaves_import_without_false_failure() {
        composeRule.setContent {
            ImportScreen(
                runtime = idleRuntime(),
                onReviewDraftReady = {}
            )
        }

        composeRule.onNodeWithTag("import-primary-action").assertIsDisplayed()
        composeRule.onAllNodesWithText("Unsupported Screenshot").assertCountEquals(0)
        composeRule.onAllNodesWithText("Can't Read Selected Screenshot").assertCountEquals(0)
        composeRule.onAllNodesWithText("Couldn't Save Screenshot").assertCountEquals(0)
    }
}

private fun idleRuntime(): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = { ImportResult.Cancelled }
    )
}

private fun readyRuntime(): AndroidPhotoPickerRuntime {
    val parser = DraftParser()
    val runtime = AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = { request ->
            val draft = parser.createDraft(
                analysis = com.kingsmetric.app.MlKitFixtures.supportedAnalysis(),
                screenshotId = request.screenshotId,
                screenshotPath = request.localPath
            )
            ImportResult.DraftReady(
                storedScreenshot = StoredScreenshot(
                    id = request.screenshotId,
                    path = request.localPath,
                    originalSourcePath = request.originalUri
                ),
                draft = draft,
                reviewState = ReviewState.fromDraft(draft)
            )
        }
    )
    runtime.handlePickerResult("content://shots/1")
    return runtime
}

private fun unsupportedRuntime(): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = { ImportResult.Unsupported("Unsupported screenshot.") }
    ).also { runtime ->
        runtime.handlePickerResult("content://shots/unsupported")
    }
}

private fun unreadableRuntime(): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(unreadableUris = setOf("content://shots/unreadable")),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = { ImportResult.Cancelled }
    ).also { runtime ->
        runtime.handlePickerResult("content://shots/unreadable")
    }
}

private fun storageFailureRuntime(): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = FakeUriScreenshotStorage(copyFailUris = setOf("content://shots/storage")),
            importStarter = { ImportResult.Cancelled }
        ),
        recognizeImportedScreenshot = { ImportResult.Cancelled }
    ).also { runtime ->
        runtime.handlePickerResult("content://shots/storage")
    }
}
