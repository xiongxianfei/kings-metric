package com.kingsmetric.app

import com.kingsmetric.importflow.ImportResult

data class ImportedScreenshotRequest(
    val screenshotId: String,
    val localPath: String,
    val originalUri: String
)

sealed interface PickerSelectionResult {
    data class Selected(val uri: String) : PickerSelectionResult
    data object Cancelled : PickerSelectionResult
}

enum class PickerFailure {
    UNREADABLE_SOURCE,
    LOCAL_STORAGE
}

sealed interface PickerImportResult {
    data object Idle : PickerImportResult
    data class ReadyForImport(val request: ImportedScreenshotRequest) : PickerImportResult
    data class ImportFailed(val failure: PickerFailure) : PickerImportResult
    data class StorageFailed(val failure: PickerFailure) : PickerImportResult
}

data class StoredUriScreenshot(
    val screenshotId: String,
    val localPath: String,
    val originalUri: String
)

interface UriScreenshotStorage {
    fun copyFromUri(uri: String): StoredUriScreenshot
}

class UnreadableUriException : IllegalStateException("uri unreadable")

class AndroidPhotoPickerImportAdapter(
    private val uriStorage: UriScreenshotStorage,
    private val importStarter: (ImportedScreenshotRequest) -> ImportResult
) {

    fun mapPickerResult(uri: String?): PickerSelectionResult {
        return uri?.let(PickerSelectionResult::Selected) ?: PickerSelectionResult.Cancelled
    }

    fun handlePickerResult(uri: String?): PickerImportResult {
        return when (val selection = mapPickerResult(uri)) {
            PickerSelectionResult.Cancelled -> PickerImportResult.Idle
            is PickerSelectionResult.Selected -> importFromPicker(selection.uri)
        }
    }

    fun importFromPicker(uri: String): PickerImportResult {
        val stored = try {
            uriStorage.copyFromUri(uri)
        } catch (_: UnreadableUriException) {
            return PickerImportResult.ImportFailed(PickerFailure.UNREADABLE_SOURCE)
        } catch (_: IllegalStateException) {
            return PickerImportResult.StorageFailed(PickerFailure.LOCAL_STORAGE)
        }

        val request = ImportedScreenshotRequest(
            screenshotId = stored.screenshotId,
            localPath = stored.localPath,
            originalUri = stored.originalUri
        )
        importStarter(request)
        return PickerImportResult.ReadyForImport(request)
    }
}

class FakeUriScreenshotStorage(
    private val unreadableUris: Set<String> = emptySet(),
    private val copyFailUris: Set<String> = emptySet()
) : UriScreenshotStorage {

    val copiedUris = mutableListOf<String>()

    override fun copyFromUri(uri: String): StoredUriScreenshot {
        if (uri in unreadableUris) {
            throw UnreadableUriException()
        }
        if (uri in copyFailUris) {
            throw IllegalStateException("copy failed")
        }
        copiedUris += uri
        val screenshotId = "shot-${copiedUris.size}"
        return StoredUriScreenshot(
            screenshotId = screenshotId,
            localPath = "stored/$screenshotId-${uri.replace("://", "__").replace("/", "_")}.png",
            originalUri = uri
        )
    }
}

class FakePickerImportWorkflow {
    val startedRequests = mutableListOf<ImportedScreenshotRequest>()

    fun start(request: ImportedScreenshotRequest): ImportResult {
        startedRequests += request
        return ImportResult.Cancelled
    }
}
