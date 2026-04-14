package com.kingsmetric

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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

        composeRule.onNodeWithText("Continue Review").assertIsDisplayed()
        composeRule.onNodeWithText("Continue Review").performClick()
        composeRule.runOnIdle {
            assertNotNull(selectedDraft)
        }
    }
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
