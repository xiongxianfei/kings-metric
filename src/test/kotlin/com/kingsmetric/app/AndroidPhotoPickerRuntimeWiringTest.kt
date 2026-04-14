package com.kingsmetric.app

import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.StoredScreenshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPhotoPickerRuntimeWiringTest {

    @Test
    fun `T1 runtime starts in explicit idle state`() {
        val runtime = photoPickerRuntime()

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.value.status)
    }

    @Test
    fun `T2 successful picker import updates runtime with a reviewable draft`() {
        val runtime = photoPickerRuntime(
            recognizeImportedScreenshot = { request ->
                ImportResult.DraftReady(
                    storedScreenshot = StoredScreenshot(
                        id = request.screenshotId,
                        path = request.localPath,
                        originalSourcePath = request.originalUri
                    ),
                    draft = RuntimeFixtures.supportedDraft().copy(
                        screenshotId = request.screenshotId,
                        screenshotPath = request.localPath
                    ),
                    reviewState = ReviewState.fromDraft(
                        RuntimeFixtures.supportedDraft().copy(
                            screenshotId = request.screenshotId,
                            screenshotPath = request.localPath
                        )
                    )
                )
            }
        )

        runtime.handlePickerResult("content://shots/1")

        val status = runtime.state.value.status
        assertTrue(status is ImportRuntimeStatus.ReviewReady)
        status as ImportRuntimeStatus.ReviewReady
        assertEquals("shot-1", status.draft.screenshotId)
        assertEquals("stored/shot-1-content__shots_1.png", status.draft.screenshotPath)
    }

    @Test
    fun `T3 cancelled picker keeps runtime in non-error idle state`() {
        val runtime = photoPickerRuntime()

        runtime.handlePickerResult(null)

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.value.status)
    }

    @Test
    fun `T4 unreadable and storage failures stay attributable in runtime state`() {
        val unreadable = photoPickerRuntime(
            uriStorage = FakeUriScreenshotStorage(unreadableUris = setOf("content://shots/unreadable"))
        )
        val storageFailure = photoPickerRuntime(
            uriStorage = FakeUriScreenshotStorage(copyFailUris = setOf("content://shots/copy-fails"))
        )

        unreadable.handlePickerResult("content://shots/unreadable")
        storageFailure.handlePickerResult("content://shots/copy-fails")

        assertEquals(
            ImportRuntimeStatus.Failed("Could not import screenshot from the selected source."),
            unreadable.state.value.status
        )
        assertEquals(
            ImportRuntimeStatus.Failed("Could not save screenshot locally."),
            storageFailure.state.value.status
        )
    }

    @Test
    fun `T5 unsupported or failed recognition becomes a retryable runtime failure`() {
        val unsupported = photoPickerRuntime(
            recognizeImportedScreenshot = { ImportResult.Unsupported("Unsupported screenshot.") }
        )
        val failed = photoPickerRuntime(
            recognizeImportedScreenshot = { ImportResult.ImportFailed("Could not extract screenshot data for review.") }
        )

        unsupported.handlePickerResult("content://shots/unsupported")
        failed.handlePickerResult("content://shots/failed")

        assertEquals(
            ImportRuntimeStatus.Failed("Unsupported screenshot."),
            unsupported.state.value.status
        )
        assertEquals(
            ImportRuntimeStatus.Failed("Could not extract screenshot data for review."),
            failed.state.value.status
        )
    }
}

private fun photoPickerRuntime(
    uriStorage: UriScreenshotStorage = FakeUriScreenshotStorage(),
    importStarter: (ImportedScreenshotRequest) -> ImportResult = { ImportResult.Cancelled },
    recognizeImportedScreenshot: (ImportedScreenshotRequest) -> ImportResult = { ImportResult.Cancelled }
): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = uriStorage,
            importStarter = importStarter
        ),
        recognizeImportedScreenshot = recognizeImportedScreenshot
    )
}

private object RuntimeFixtures {
    private val parser = DraftParser()

    fun supportedDraft() = parser.createDraft(
        analysis = MlKitFixtures.supportedAnalysis(
            visibleFields = FieldKey.all
        ),
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png"
    )
}
