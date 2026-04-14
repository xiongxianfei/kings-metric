package com.kingsmetric.app

import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.ImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ImportRuntimeStatus {
    data object Idle : ImportRuntimeStatus
    data object InProgress : ImportRuntimeStatus
    data class Unsupported(val message: String) : ImportRuntimeStatus
    data class SourceFailed(val message: String) : ImportRuntimeStatus
    data class StorageFailed(val message: String) : ImportRuntimeStatus
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
    private val _state = MutableStateFlow(ImportRuntimeUiState(ImportRuntimeStatus.Idle))
    val state: StateFlow<ImportRuntimeUiState> = _state.asStateFlow()

    fun reset() {
        _state.value = ImportRuntimeUiState(ImportRuntimeStatus.Idle)
    }

    fun beginImport() {
        _state.value = ImportRuntimeUiState(ImportRuntimeStatus.InProgress)
    }

    fun handlePickerResult(uri: String?) {
        _state.value = when (val result = adapter.handlePickerResult(uri)) {
            PickerImportResult.Idle -> ImportRuntimeUiState(ImportRuntimeStatus.Idle)
            is PickerImportResult.ReadyForImport -> {
                when (val recognition = recognizeImportedScreenshot(result.request)) {
                    is ImportResult.DraftReady -> {
                        ImportRuntimeUiState(ImportRuntimeStatus.ReviewReady(recognition.draft))
                    }
                    is ImportResult.Unsupported -> {
                        ImportRuntimeUiState(
                            ImportRuntimeStatus.Unsupported(
                                SharedUxCopy.message(SharedMessageKey.IMPORT_UNSUPPORTED).text
                            )
                        )
                    }
                    is ImportResult.ImportFailed -> {
                        ImportRuntimeUiState(
                            ImportRuntimeStatus.Failed(
                                SharedUxCopy.message(SharedMessageKey.IMPORT_OCR_FAILED).text
                            )
                        )
                    }
                    is ImportResult.StorageFailed -> {
                        ImportRuntimeUiState(
                            ImportRuntimeStatus.StorageFailed(
                                SharedUxCopy.message(SharedMessageKey.IMPORT_STORAGE_FAILED).text
                            )
                        )
                    }
                    ImportResult.Cancelled -> ImportRuntimeUiState(ImportRuntimeStatus.Idle)
                }
            }
            is PickerImportResult.ImportFailed -> {
                ImportRuntimeUiState(
                    ImportRuntimeStatus.SourceFailed(
                        SharedUxCopy.message(SharedMessageKey.IMPORT_SOURCE_FAILED).text
                    )
                )
            }
            is PickerImportResult.StorageFailed -> {
                ImportRuntimeUiState(
                    ImportRuntimeStatus.StorageFailed(
                        SharedUxCopy.message(SharedMessageKey.IMPORT_STORAGE_FAILED).text
                    )
                )
            }
        }
    }
}
