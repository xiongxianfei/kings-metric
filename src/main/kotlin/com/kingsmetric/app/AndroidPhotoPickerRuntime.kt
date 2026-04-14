package com.kingsmetric.app

import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.ImportResult

sealed interface ImportRuntimeStatus {
    data object Idle : ImportRuntimeStatus
    data class ReviewReady(val draft: DraftRecord) : ImportRuntimeStatus
    data class Failed(val message: String) : ImportRuntimeStatus
}

data class ImportRuntimeUiState(
    val status: ImportRuntimeStatus
)

class AndroidPhotoPickerRuntime(
    private val adapter: AndroidPhotoPickerImportAdapter,
    private val recognizeImportedScreenshot: (ImportedScreenshotRequest) -> ImportResult
) {
    var state: ImportRuntimeUiState = ImportRuntimeUiState(ImportRuntimeStatus.Idle)
        private set

    fun reset() {
        state = ImportRuntimeUiState(ImportRuntimeStatus.Idle)
    }

    fun handlePickerResult(uri: String?) {
        state = when (val result = adapter.handlePickerResult(uri)) {
            PickerImportResult.Idle -> ImportRuntimeUiState(ImportRuntimeStatus.Idle)
            is PickerImportResult.ReadyForImport -> {
                when (val recognition = recognizeImportedScreenshot(result.request)) {
                    is ImportResult.DraftReady -> {
                        ImportRuntimeUiState(ImportRuntimeStatus.ReviewReady(recognition.draft))
                    }
                    is ImportResult.Unsupported -> {
                        ImportRuntimeUiState(ImportRuntimeStatus.Failed(recognition.reason))
                    }
                    is ImportResult.ImportFailed -> {
                        ImportRuntimeUiState(ImportRuntimeStatus.Failed(recognition.message))
                    }
                    is ImportResult.StorageFailed -> {
                        ImportRuntimeUiState(ImportRuntimeStatus.Failed(recognition.message))
                    }
                    ImportResult.Cancelled -> ImportRuntimeUiState(ImportRuntimeStatus.Idle)
                }
            }
            is PickerImportResult.ImportFailed -> {
                ImportRuntimeUiState(
                    ImportRuntimeStatus.Failed("Could not import screenshot from the selected source.")
                )
            }
            is PickerImportResult.StorageFailed -> {
                ImportRuntimeUiState(
                    ImportRuntimeStatus.Failed("Could not save screenshot locally.")
                )
            }
        }
    }
}
