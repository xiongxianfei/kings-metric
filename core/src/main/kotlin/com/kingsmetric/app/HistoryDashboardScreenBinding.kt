package com.kingsmetric.app

import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.dashboard.DashboardMetrics
import com.kingsmetric.data.local.RecordLookupResult
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.MatchHistoryListItem
import com.kingsmetric.history.MatchDetailState
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
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

data class HistoryRowUiState(
    val recordId: String,
    val categoryLabel: String,
    val primaryText: String,
    val resultText: String,
    val quickSummaryItems: List<HistoryQuickSummaryItemUiState> = emptyList(),
    val recencyText: String,
    val previewText: String?,
    val selectable: Boolean
)

enum class HistoryQuickSummaryKind {
    RESULT,
    LANE,
    KDA,
    SCORE
}

data class HistoryQuickSummaryItemUiState(
    val kind: HistoryQuickSummaryKind,
    val text: String
)

data class DetailFieldDisplayUiState(
    val label: String,
    val valueText: String
)

data class DetailSectionUiState(
    val title: String,
    val fields: List<DetailFieldDisplayUiState>
)

data class DetailScreenUiState(
    val recordId: String,
    val screenshotPath: String?,
    val previewAvailability: PreviewAvailability,
    val fields: List<DetailFieldUiState> = emptyList(),
    val summaryTitle: String = "",
    val summaryResult: String = "",
    val summaryMetaText: String = "",
    val backLabel: String = "History",
    val previewStatusLabel: String = "Screenshot",
    val previewStatusText: String = "",
    val sections: List<DetailSectionUiState> = emptyList()
)

data class HistoryScreenUiState(
    val content: HistoryContentState,
    val rows: List<HistoryRowUiState> = emptyList(),
    val detail: DetailScreenUiState? = null,
    val userMessage: String? = null
)

data class DashboardCardUiState(
    val label: String,
    val valueText: String
)

data class DashboardScreenUiState(
    val content: DashboardContentState,
    val primaryCards: List<DashboardCardUiState> = emptyList(),
    val contextText: String? = null,
    val sparseDataText: String? = null,
    val secondaryNotes: List<String> = emptyList()
)

fun HistoryContentState.toHistoryScreenUiState(
    detail: DetailScreenUiState? = null,
    userMessage: String? = null
): HistoryScreenUiState {
    return HistoryScreenUiState(
        content = this,
        rows = when (this) {
            is HistoryContentState.Loaded -> records.map(MatchHistoryListItem::toHistoryRowUiState)
            HistoryContentState.Empty,
            is HistoryContentState.Error -> emptyList()
        },
        detail = detail,
        userMessage = userMessage
    )
}

fun DashboardContentState.toDashboardScreenUiState(): DashboardScreenUiState {
    return when (this) {
        DashboardContentState.Empty -> DashboardScreenUiState(content = this)
        is DashboardContentState.Error -> DashboardScreenUiState(content = this)
        is DashboardContentState.Loaded -> DashboardScreenUiState(
            content = this,
            primaryCards = metrics.toPrimaryCards(),
            contextText = metrics.sampleContextText(),
            sparseDataText = metrics.sparseDataText(),
            secondaryNotes = metrics.secondaryNotes()
        )
    }
}

fun MatchDetailState.toDetailScreenUiState(): DetailScreenUiState {
    val previewAvailability = when (screenshotPreview) {
        is ScreenshotPreviewState.Available -> PreviewAvailability.Available
        ScreenshotPreviewState.Unavailable -> PreviewAvailability.Unavailable
    }
    return DetailScreenUiState(
        recordId = record.recordId,
        screenshotPath = when (screenshotPreview) {
            is ScreenshotPreviewState.Available -> screenshotPreview.path
            ScreenshotPreviewState.Unavailable -> null
        },
        previewAvailability = previewAvailability,
        fields = FieldKey.entries.map { key ->
            DetailFieldUiState(key = key, value = record.fields[key])
        },
        summaryTitle = record.fields[FieldKey.HERO] ?: "Hero not entered",
        summaryResult = formatResult(record.fields[FieldKey.RESULT]),
        summaryMetaText = formatSavedAt(record.savedAt),
        backLabel = "History",
        previewStatusLabel = "Screenshot",
        previewStatusText = if (previewAvailability == PreviewAvailability.Available) {
            "Screenshot available"
        } else {
            "Screenshot preview unavailable"
        },
        sections = detailSectionsFor(record)
    )
}

private fun MatchHistoryListItem.toHistoryRowUiState(): HistoryRowUiState {
    val summaryItems = buildList {
        add(
            HistoryQuickSummaryItemUiState(
                kind = HistoryQuickSummaryKind.RESULT,
                text = formatResult(result)
            )
        )
        lane?.trim()?.takeIf(String::isNotEmpty)?.let { laneValue ->
            add(
                HistoryQuickSummaryItemUiState(
                    kind = HistoryQuickSummaryKind.LANE,
                    text = laneValue
                )
            )
        }
        kda?.trim()?.takeIf(String::isNotEmpty)?.let { kdaValue ->
            add(
                HistoryQuickSummaryItemUiState(
                    kind = HistoryQuickSummaryKind.KDA,
                    text = kdaValue
                )
            )
        }
        score?.trim()?.takeIf(String::isNotEmpty)?.let { scoreValue ->
            add(
                HistoryQuickSummaryItemUiState(
                    kind = HistoryQuickSummaryKind.SCORE,
                    text = scoreValue
                )
            )
        }
    }

    return HistoryRowUiState(
        recordId = recordId,
        categoryLabel = "Saved match",
        primaryText = hero ?: "Hero not entered",
        resultText = formatResult(result),
        quickSummaryItems = summaryItems,
        recencyText = formatSavedAt(savedAt),
        previewText = if (screenshotAvailable) null else "Preview unavailable",
        selectable = true
    )
}

private fun detailSectionsFor(record: SavedMatchHistoryRecord): List<DetailSectionUiState> {
    return listOf(
        "Match Summary" to listOf(
            FieldKey.PLAYER_NAME,
            FieldKey.LANE,
            FieldKey.SCORE,
            FieldKey.KDA
        ),
        "Damage Output" to listOf(
            FieldKey.DAMAGE_DEALT,
            FieldKey.DAMAGE_SHARE,
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS
        ),
        "Survivability" to listOf(
            FieldKey.DAMAGE_TAKEN,
            FieldKey.DAMAGE_TAKEN_SHARE,
            FieldKey.CONTROL_DURATION
        ),
        "Economy" to listOf(
            FieldKey.TOTAL_GOLD,
            FieldKey.GOLD_SHARE,
            FieldKey.GOLD_FROM_FARMING,
            FieldKey.LAST_HITS
        ),
        "Team Play" to listOf(
            FieldKey.PARTICIPATION_RATE,
            FieldKey.KILL_PARTICIPATION_COUNT
        )
    ).map { (title, fields) ->
        DetailSectionUiState(
            title = title,
            fields = fields.map { key ->
                DetailFieldDisplayUiState(
                    label = SharedUxCopy.field(key).label,
                    valueText = record.fields[key] ?: "Not entered"
                )
            }
        )
    }
}

private fun DashboardMetrics.toPrimaryCards(): List<DashboardCardUiState> {
    return listOf(
        DashboardCardUiState(
            label = "Win Rate",
            valueText = winRate?.let { "${it.percentage}%" } ?: "Not enough data"
        ),
        DashboardCardUiState(
            label = "Average KDA",
            valueText = averageKda?.value?.toString() ?: "Not enough data"
        ),
        DashboardCardUiState(
            label = "Most Played Hero",
            valueText = heroUsage.firstOrNull()?.hero ?: "Not enough data"
        )
    )
}

private fun DashboardMetrics.sampleContextText(): String? {
    val sampleSize = winRate?.totalMatches ?: return null
    return if (sampleSize == 1) {
        "Based on 1 saved match"
    } else {
        "Based on $sampleSize saved matches"
    }
}

private fun DashboardMetrics.sparseDataText(): String? {
    val sampleSize = winRate?.totalMatches ?: return null
    return if (sampleSize < 3) {
        "Based on limited match history."
    } else {
        null
    }
}

private fun DashboardMetrics.secondaryNotes(): List<String> {
    val notes = mutableListOf<String>()
    if (averageKda == null || recentPerformance?.averageKillParticipationCount == null) {
        notes += "Some metrics need more saved data."
    }
    return notes
}

private fun formatResult(value: String?): String {
    return when (value) {
        "victory" -> "Victory"
        "defeat" -> "Defeat"
        null -> "Result not entered"
        else -> value.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
    }
}

private fun formatSavedAt(savedAt: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    return "Saved ${formatter.format(Instant.ofEpochMilli(savedAt).atZone(ZoneOffset.UTC))}"
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
