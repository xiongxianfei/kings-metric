package com.kingsmetric.app

import com.kingsmetric.importflow.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPhotoPickerAndFileStorageIntegrationTest {

    @Test
    fun `T1 picker result mapper converts a selected Uri into an intake request`() {
        val adapter = photoPickerAdapter()

        val result = adapter.mapPickerResult("content://shots/1")

        assertTrue(result is PickerSelectionResult.Selected)
        result as PickerSelectionResult.Selected
        assertEquals("content://shots/1", result.uri)
    }

    @Test
    fun `T2 cancelled picker result maps to a non-error idle outcome`() {
        val adapter = photoPickerAdapter()

        val result = adapter.mapPickerResult(null)

        assertEquals(PickerSelectionResult.Cancelled, result)
    }

    @Test
    fun `T3 storage adapter maps unreadable Uri access to unreadable-source failure`() {
        val adapter = photoPickerAdapter(
            uriStorage = FakeUriScreenshotStorage(unreadableUris = setOf("content://shots/unreadable"))
        )

        val result = adapter.importFromPicker("content://shots/unreadable")

        assertTrue(result is PickerImportResult.ImportFailed)
        result as PickerImportResult.ImportFailed
        assertEquals(PickerFailure.UNREADABLE_SOURCE, result.failure)
    }

    @Test
    fun `T4 storage adapter maps copy failure to local-storage failure`() {
        val adapter = photoPickerAdapter(
            uriStorage = FakeUriScreenshotStorage(copyFailUris = setOf("content://shots/copy-fails"))
        )

        val result = adapter.importFromPicker("content://shots/copy-fails")

        assertTrue(result is PickerImportResult.StorageFailed)
        result as PickerImportResult.StorageFailed
        assertEquals(PickerFailure.LOCAL_STORAGE, result.failure)
    }

    @Test
    fun `IT1 selecting one image copies it into app-managed storage`() {
        val storage = FakeUriScreenshotStorage()
        val adapter = photoPickerAdapter(uriStorage = storage)

        val result = adapter.importFromPicker("content://shots/1")

        assertTrue(result is PickerImportResult.ReadyForImport)
        result as PickerImportResult.ReadyForImport
        assertEquals("shot-1", result.request.screenshotId)
        assertEquals("stored/shot-1-content__shots_1.png", result.request.localPath)
        assertEquals(listOf("content://shots/1"), storage.copiedUris)
    }

    @Test
    fun `IT2 cancelling picker leaves import state idle`() {
        val adapter = photoPickerAdapter()

        val result = adapter.handlePickerResult(null)

        assertEquals(PickerImportResult.Idle, result)
    }

    @Test
    fun `IT3 unreadable Uri reports import failure without partial downstream work`() {
        val workflow = FakePickerImportWorkflow()
        val adapter = photoPickerAdapter(
            uriStorage = FakeUriScreenshotStorage(unreadableUris = setOf("content://shots/unreadable")),
            importStarter = workflow::start
        )

        val result = adapter.importFromPicker("content://shots/unreadable")

        assertTrue(result is PickerImportResult.ImportFailed)
        assertTrue(workflow.startedRequests.isEmpty())
    }

    @Test
    fun `IT4 copy failure reports storage failure and does not start recognition`() {
        val workflow = FakePickerImportWorkflow()
        val adapter = photoPickerAdapter(
            uriStorage = FakeUriScreenshotStorage(copyFailUris = setOf("content://shots/copy-fails")),
            importStarter = workflow::start
        )

        val result = adapter.importFromPicker("content://shots/copy-fails")

        assertTrue(result is PickerImportResult.StorageFailed)
        assertFalse(workflow.startedRequests.isNotEmpty())
    }
}

private fun photoPickerAdapter(
    uriStorage: FakeUriScreenshotStorage = FakeUriScreenshotStorage(),
    importStarter: (ImportedScreenshotRequest) -> ImportResult = { FakePickerImportWorkflow().start(it) }
): AndroidPhotoPickerImportAdapter {
    return AndroidPhotoPickerImportAdapter(
        uriStorage = uriStorage,
        importStarter = importStarter
    )
}
