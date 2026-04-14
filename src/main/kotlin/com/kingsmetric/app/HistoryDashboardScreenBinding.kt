package com.kingsmetric.app

import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.data.local.RecordLookupResult
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.MatchDetailState
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetailFieldUiState(
    val key: FieldKey,
    val value: String?
)

data class DetailScreenUiState(
    val recordId: String,
    val screenshotPath: String?,
    val previewAvailability: PreviewAvailability,
    val fields: List<DetailFieldUiState>
)

data class HistoryScreenUiState(
    val content: HistoryContentState,
    val detail: DetailScreenUiState? = null,
    val userMessage: String? = null
)

data class DashboardScreenUiState(
    val content: DashboardContentState
)

fun HistoryContentState.toHistoryScreenUiState(
    detail: DetailScreenUiState? = null,
    userMessage: String? = null
): HistoryScreenUiState {
    return HistoryScreenUiState(
        content = this,
        detail = detail,
        userMessage = userMessage
    )
}

fun DashboardContentState.toDashboardScreenUiState(): DashboardScreenUiState {
    return DashboardScreenUiState(content = this)
}

fun MatchDetailState.toDetailScreenUiState(): DetailScreenUiState {
    return DetailScreenUiState(
        recordId = record.recordId,
        screenshotPath = when (screenshotPreview) {
            is ScreenshotPreviewState.Available -> screenshotPreview.path
            ScreenshotPreviewState.Unavailable -> null
        },
        previewAvailability = when (screenshotPreview) {
            is ScreenshotPreviewState.Available -> PreviewAvailability.Available
            ScreenshotPreviewState.Unavailable -> PreviewAvailability.Unavailable
        },
        fields = FieldKey.entries.map { key ->
            DetailFieldUiState(key = key, value = record.fields[key])
        }
    )
}

class HistoryScreenBinder(
    private val repository: RoomObservedMatchRepository
) {
    private val _state = MutableStateFlow(HistoryContentState.Empty.toHistoryScreenUiState())
    val state: StateFlow<HistoryScreenUiState> = _state.asStateFlow()

    fun bind(scope: CoroutineScope): Job {
        return scope.launch {
            repository.observeHistory().collect { content ->
                _state.value = content.toHistoryScreenUiState(
                    detail = _state.value.detail,
                    userMessage = if (content is HistoryContentState.Error) content.message else _state.value.userMessage
                )
            }
        }
    }

    fun openDetail(scope: CoroutineScope, recordId: String): Job {
        return scope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.getDetail(recordId)
            }
            _state.value = when (result) {
                is RecordLookupResult.Found -> _state.value.copy(
                    detail = result.detail.toDetailScreenUiState(),
                    userMessage = null
                )
                RecordLookupResult.NotFound -> _state.value.copy(
                    detail = null,
                    userMessage = "Saved match is no longer available."
                )
                is RecordLookupResult.Error -> _state.value.copy(
                    detail = null,
                    userMessage = result.message
                )
            }
        }
    }
}

class DashboardScreenBinder(
    private val repository: RoomObservedMatchRepository
) {
    private val _state = MutableStateFlow(DashboardContentState.Empty.toDashboardScreenUiState())
    val state: StateFlow<DashboardScreenUiState> = _state.asStateFlow()

    fun bind(scope: CoroutineScope): Job {
        return scope.launch {
            repository.observeDashboard().collect { content ->
                _state.value = content.toDashboardScreenUiState()
            }
        }
    }
}
