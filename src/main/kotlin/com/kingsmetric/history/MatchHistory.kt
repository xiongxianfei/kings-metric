package com.kingsmetric.history

import com.kingsmetric.importflow.FieldKey

data class SavedMatchHistoryRecord(
    val recordId: String,
    val savedAt: Long,
    val screenshotId: String,
    val screenshotPath: String,
    val fields: Map<FieldKey, String?>
)

data class MatchHistoryListItem(
    val recordId: String,
    val savedAt: Long,
    val hero: String?,
    val result: String?,
    val screenshotAvailable: Boolean = true
)

sealed interface HistoryContentState {
    data object Empty : HistoryContentState
    data class Loaded(val records: List<MatchHistoryListItem>) : HistoryContentState
    data class Error(val message: String, val canRetry: Boolean = true) : HistoryContentState
}

sealed interface ScreenshotPreviewState {
    data class Available(val path: String) : ScreenshotPreviewState
    data object Unavailable : ScreenshotPreviewState
}

data class MatchDetailState(
    val record: SavedMatchHistoryRecord,
    val screenshotPreview: ScreenshotPreviewState
)

data class MatchHistoryUiState(
    val history: HistoryContentState,
    val detail: MatchDetailState? = null,
    val userMessage: String? = null
)

interface MatchHistoryRepository {
    fun loadHistory(): List<SavedMatchHistoryRecord>
    fun getRecord(recordId: String): SavedMatchHistoryRecord?
}

interface ScreenshotFileChecker {
    fun exists(path: String): Boolean
}

class MatchHistoryController(
    private val repository: MatchHistoryRepository,
    private val screenshotFiles: ScreenshotFileChecker
) {

    fun loadHistory(): MatchHistoryUiState {
        val records = try {
            repository.loadHistory()
        } catch (_: IllegalStateException) {
            return MatchHistoryUiState(
                history = HistoryContentState.Error(
                    message = "Could not load saved matches."
                )
            )
        }

        if (records.isEmpty()) {
            return MatchHistoryUiState(history = HistoryContentState.Empty)
        }

        return MatchHistoryUiState(
            history = HistoryContentState.Loaded(
                records = records
                    .sortedWith(compareByDescending<SavedMatchHistoryRecord> { it.savedAt }.thenBy { it.recordId })
                    .map { record ->
                        MatchHistoryListItem(
                            recordId = record.recordId,
                            savedAt = record.savedAt,
                            hero = record.fields[FieldKey.HERO],
                            result = record.fields[FieldKey.RESULT]
                        )
                    }
            )
        )
    }

    fun openRecord(currentState: MatchHistoryUiState, recordId: String): MatchHistoryUiState {
        val record = repository.getRecord(recordId)
            ?: return currentState.copy(
                detail = null,
                userMessage = "Saved match is no longer available."
            )

        return currentState.copy(
            detail = MatchDetailState(
                record = record,
                screenshotPreview = record.screenshotPath
                    .takeIf { screenshotFiles.exists(it) }
                    ?.let(ScreenshotPreviewState::Available)
                    ?: ScreenshotPreviewState.Unavailable
            ),
            userMessage = null
        )
    }
}

class FakeMatchHistoryRepository(
    private val records: List<SavedMatchHistoryRecord> = emptyList(),
    private val shouldFailOnLoad: Boolean = false
) : MatchHistoryRepository {

    override fun loadHistory(): List<SavedMatchHistoryRecord> {
        if (shouldFailOnLoad) {
            throw IllegalStateException("history load failed")
        }
        return records
    }

    override fun getRecord(recordId: String): SavedMatchHistoryRecord? {
        return records.firstOrNull { it.recordId == recordId }
    }
}

class FakeScreenshotFileChecker(
    private val existingPaths: Set<String> = setOf("stored/record-1.png")
) : ScreenshotFileChecker {

    override fun exists(path: String): Boolean = path in existingPaths
}
