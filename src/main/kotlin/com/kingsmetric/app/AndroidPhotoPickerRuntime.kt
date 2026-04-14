package com.kingsmetric.app

sealed interface ImportRuntimeStatus {
    data object Idle : ImportRuntimeStatus
    data class ReadyForRecognition(val request: ImportedScreenshotRequest) : ImportRuntimeStatus
    data class Failed(val failure: PickerFailure) : ImportRuntimeStatus
}

data class ImportRuntimeUiState(
    val status: ImportRuntimeStatus
)

class AndroidPhotoPickerRuntime(
    private val adapter: AndroidPhotoPickerImportAdapter
) {
    var state: ImportRuntimeUiState = ImportRuntimeUiState(ImportRuntimeStatus.Idle)
        private set

    fun handlePickerResult(uri: String?) {
        state = when (val result = adapter.handlePickerResult(uri)) {
            PickerImportResult.Idle -> ImportRuntimeUiState(ImportRuntimeStatus.Idle)
            is PickerImportResult.ReadyForImport -> {
                ImportRuntimeUiState(
                    ImportRuntimeStatus.ReadyForRecognition(result.request)
                )
            }
            is PickerImportResult.ImportFailed -> {
                ImportRuntimeUiState(ImportRuntimeStatus.Failed(result.failure))
            }
            is PickerImportResult.StorageFailed -> {
                ImportRuntimeUiState(ImportRuntimeStatus.Failed(result.failure))
            }
        }
    }
}
