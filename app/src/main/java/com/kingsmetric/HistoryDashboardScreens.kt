package com.kingsmetric

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.kingsmetric.app.DashboardScreenBinder
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.HistoryScreenBinder
import com.kingsmetric.app.HistoryScreenUiState
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.history.HistoryContentState
import java.io.File
import java.util.UUID

private enum class HomeTab {
    History,
    Dashboard
}

@Composable
fun HistoryDashboardRoot() {
    val context = LocalContext.current
    val repository = remember(context) {
        val database = Room.databaseBuilder(
            context,
            KingsMetricDatabase::class.java,
            "kings-metric.db"
        ).build()
        RoomObservedMatchRepository(
            dao = database.savedMatchDao(),
            screenshotFiles = AndroidScreenshotFileStore(),
            recordIdProvider = UuidRecordIdProvider(),
            savedAtProvider = SystemSavedAtProvider()
        )
    }
    val historyBinder = remember(repository) { HistoryScreenBinder(repository) }
    val dashboardBinder = remember(repository) { DashboardScreenBinder(repository) }
    val scope = rememberCoroutineScope()
    val historyState by historyBinder.state.collectAsState()
    val dashboardState by dashboardBinder.state.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.History) }

    DisposableEffect(historyBinder, dashboardBinder, scope) {
        val historyJob = historyBinder.bind(scope)
        val dashboardJob = dashboardBinder.bind(scope)
        onDispose {
            historyJob.cancel()
            dashboardJob.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { selectedTab = HomeTab.History }) {
                Text("History")
            }
            Button(onClick = { selectedTab = HomeTab.Dashboard }) {
                Text("Dashboard")
            }
        }

        when (selectedTab) {
            HomeTab.History -> HistoryScreen(
                state = historyState,
                onRecordSelected = { recordId ->
                    historyBinder.openDetail(scope, recordId)
                }
            )
            HomeTab.Dashboard -> DashboardScreen(state = dashboardState)
        }
    }
}

@Composable
fun HistoryScreen(
    state: HistoryScreenUiState,
    onRecordSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (val content = state.content) {
            HistoryContentState.Empty -> Text("No saved matches yet.")
            is HistoryContentState.Error -> Text(content.message)
            is HistoryContentState.Loaded -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(content.records, key = { it.recordId }) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecordSelected(record.recordId) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(record.hero ?: "Unknown Hero", style = MaterialTheme.typography.titleMedium)
                                Text(record.result ?: "Unknown Result", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        state.userMessage?.let { message ->
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }

        state.detail?.let { detail ->
            RecordDetailScreen(state = detail)
        }
    }
}

@Composable
fun DashboardScreen(state: DashboardScreenUiState) {
    when (val content = state.content) {
        DashboardContentState.Empty -> Text("No saved metrics yet.")
        is DashboardContentState.Error -> Text(content.message)
        is DashboardContentState.Loaded -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Win Rate: ${content.metrics.winRate?.percentage ?: 0.0}%")
                Text("Average KDA: ${content.metrics.averageKda?.value ?: 0.0}")
                Text(
                    "Most Played Hero: ${
                        content.metrics.heroUsage.firstOrNull()?.hero ?: "None"
                    }"
                )
            }
        }
    }
}

@Composable
fun RecordDetailScreen(state: DetailScreenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Record ${state.recordId}", style = MaterialTheme.typography.titleMedium)
        Text(
            if (state.previewAvailability == PreviewAvailability.Available) {
                state.screenshotPath ?: "Preview available"
            } else {
                "Screenshot preview unavailable"
            }
        )
        state.fields.forEach { field ->
            Text("${field.key.name}: ${field.value ?: "-"}")
        }
    }
}

private class AndroidScreenshotFileStore : LocalScreenshotFileStore {
    override fun exists(path: String): Boolean = File(path).exists()
}

private class UuidRecordIdProvider : RecordIdProvider {
    override fun nextId(): String = UUID.randomUUID().toString()
}

private class SystemSavedAtProvider : SavedAtProvider {
    override fun now(): Long = System.currentTimeMillis()
}
