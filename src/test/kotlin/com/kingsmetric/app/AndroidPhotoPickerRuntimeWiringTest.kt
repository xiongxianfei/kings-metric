package com.kingsmetric.app

import com.kingsmetric.importflow.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPhotoPickerRuntimeWiringTest {

    @Test
    fun `T1 runtime starts in explicit idle state`() {
        val runtime = photoPickerRuntime()

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.status)
    }

    @Test
    fun `T2 successful picker import updates runtime with stored screenshot request`() {
        val runtime = photoPickerRuntime()

        runtime.handlePickerResult("content://shots/1")

        val status = runtime.state.status
        assertTrue(status is ImportRuntimeStatus.ReadyForRecognition)
        status as ImportRuntimeStatus.ReadyForRecognition
        assertEquals("content://shots/1", status.request.originalUri)
        assertEquals("shot-1", status.request.screenshotId)
    }

    @Test
    fun `T3 cancelled picker keeps runtime in non-error idle state`() {
        val runtime = photoPickerRuntime()

        runtime.handlePickerResult(null)

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.status)
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
            ImportRuntimeStatus.Failed(PickerFailure.UNREADABLE_SOURCE),
            unreadable.state.status
        )
        assertEquals(
            ImportRuntimeStatus.Failed(PickerFailure.LOCAL_STORAGE),
            storageFailure.state.status
        )
    }
}

private fun photoPickerRuntime(
    uriStorage: UriScreenshotStorage = FakeUriScreenshotStorage(),
    importStarter: (ImportedScreenshotRequest) -> ImportResult = { ImportResult.Cancelled }
): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = uriStorage,
            importStarter = importStarter
        )
    )
}
