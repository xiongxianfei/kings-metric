package com.kingsmetric

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.kingsmetric.app.AndroidBitmapLoader
import com.kingsmetric.app.AndroidMlKitTextRecognizer
import com.kingsmetric.app.AndroidPhotoPickerRuntime
import com.kingsmetric.app.DashboardScreenBinder
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.ImportRuntimeStatus
import com.kingsmetric.app.HistoryScreenBinder
import com.kingsmetric.app.HistoryScreenUiState
import com.kingsmetric.app.MlKitRecognitionAdapter
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.app.ReviewScreenRoute
import com.kingsmetric.app.ReviewScreenViewModel
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RepositorySaveResult
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.RecordStore
import com.kingsmetric.importflow.SavedMatchRecord
import com.kingsmetric.importflow.TemplateValidator
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class HomeTab {
    Import,
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
    val recognitionAdapter = remember(context) {
        MlKitRecognitionAdapter(
            bitmapLoader = AndroidBitmapLoader(),
            recognizer = AndroidMlKitTextRecognizer(context)
        )
    }
    val reviewWorkflow = remember(repository) {
        MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = RoomRecordStoreAdapter(repository),
            validator = TemplateValidator(),
            parser = DraftParser()
        )
    }
    val importRuntime = remember(context) {
        AndroidPhotoPickerRuntime(
            adapter = com.kingsmetric.app.AndroidPhotoPickerImportAdapter(
                uriStorage = AndroidUriScreenshotStorage(context),
                importStarter = { _: com.kingsmetric.app.ImportedScreenshotRequest ->
                    com.kingsmetric.importflow.ImportResult.Cancelled
                }
            ),
            recognizeImportedScreenshot = { request ->
                recognitionAdapter.recognize(request.localPath)
            }
        )
    }
    val scope = rememberCoroutineScope()
    val historyState by historyBinder.state.collectAsState()
    val dashboardState by dashboardBinder.state.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Import) }
    var reviewDraft by remember { mutableStateOf<DraftRecord?>(null) }

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
            Button(onClick = { selectedTab = HomeTab.Import }) {
                Text("Import")
            }
            Button(onClick = { selectedTab = HomeTab.History }) {
                Text("History")
            }
            Button(onClick = { selectedTab = HomeTab.Dashboard }) {
                Text("Dashboard")
            }
        }

        when (selectedTab) {
            HomeTab.Import -> {
                if (reviewDraft != null) {
                    val draft = reviewDraft ?: error("review draft missing")
                    val reviewViewModel = remember(draft, reviewWorkflow) {
                        ReviewScreenViewModel(
                            draft = draft,
                            workflow = reviewWorkflow,
                            previewAvailableResolver = { path ->
                                path?.let(::File)?.exists() == true
                            }
                        )
                    }
                    ReviewScreenRoute(
                        viewModel = reviewViewModel,
                        onSaveSucceeded = {
                            reviewDraft = null
                            importRuntime.reset()
                            selectedTab = HomeTab.History
                        }
                    )
                } else {
                    ImportScreen(
                        runtime = importRuntime,
                        onReviewDraftReady = { draft ->
                            reviewDraft = draft
                        }
                    )
                }
            }
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
fun ImportScreen(
    runtime: AndroidPhotoPickerRuntime,
    onReviewDraftReady: (DraftRecord) -> Unit
) {
    var status by remember(runtime) { mutableStateOf(runtime.state.status) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        scope.launch {
            val updated = withContext(Dispatchers.Default) {
                runtime.handlePickerResult(uri?.toString())
                runtime.state.status
            }
            status = updated
            if (updated is ImportRuntimeStatus.ReviewReady) {
                onReviewDraftReady(updated.draft)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Text("Import Screenshot")
        }

        when (val current = status) {
            ImportRuntimeStatus.Idle -> Text("Select one screenshot to import.")
            is ImportRuntimeStatus.Failed -> {
                Text(current.message)
            }
            is ImportRuntimeStatus.ReviewReady -> {
                Text("Review draft ready.")
                Text(current.draft.screenshotPath.orEmpty())
            }
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

private class RoomRecordStoreAdapter(
    private val repository: RoomObservedMatchRepository
) : RecordStore {
    override fun save(record: SavedMatchRecord): SavedMatchRecord {
        return when (repository.save(record)) {
            is RepositorySaveResult.Saved -> record
            is RepositorySaveResult.Error -> {
                throw IllegalStateException("Could not save record locally.")
            }
        }
    }
}
