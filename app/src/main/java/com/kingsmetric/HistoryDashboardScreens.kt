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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kingsmetric.app.AndroidPhotoPickerImportAdapter
import com.kingsmetric.app.AndroidPhotoPickerRuntime
import com.kingsmetric.app.AppNavigationCoordinator
import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.AppRoutes
import com.kingsmetric.app.AppShellState
import com.kingsmetric.app.DashboardScreenBinder
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.HistoryScreenBinder
import com.kingsmetric.app.HistoryScreenUiState
import com.kingsmetric.app.ImportRuntimeStatus
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.app.ReviewScreenRoute
import com.kingsmetric.app.ReviewScreenViewModel
import com.kingsmetric.app.UriScreenshotStorage
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.MatchImportWorkflow
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryDashboardRoot(
    repository: RoomObservedMatchRepository,
    uriStorage: UriScreenshotStorage,
    recognizeStoredScreenshot: (String) -> ImportResult,
    reviewWorkflow: MatchImportWorkflow,
    navigationCoordinator: AppNavigationCoordinator = remember { AppNavigationCoordinator() },
    initialRoute: String? = null,
    initialReviewDraft: DraftRecord? = null
) {
    val historyBinder = remember(repository) { HistoryScreenBinder(repository) }
    val dashboardBinder = remember(repository) { DashboardScreenBinder(repository) }
    val importAdapter = remember(uriStorage) {
        AndroidPhotoPickerImportAdapter(
            uriStorage = uriStorage,
            importStarter = { ImportResult.Cancelled }
        )
    }
    val importRuntime = remember(importAdapter, recognizeStoredScreenshot) {
        AndroidPhotoPickerRuntime(
            adapter = importAdapter,
            recognizeImportedScreenshot = { request ->
                recognizeStoredScreenshot(request.localPath)
            }
        )
    }
    val scope = rememberCoroutineScope()
    val historyState by historyBinder.state.collectAsState()
    val dashboardState by dashboardBinder.state.collectAsState()
    val launchState by produceState<AppShellState?>(initialValue = null, repository, navigationCoordinator, initialRoute, initialReviewDraft) {
        value = resolveLaunchState(
            repository = repository,
            coordinator = navigationCoordinator,
            initialRoute = initialRoute,
            initialReviewDraft = initialReviewDraft
        )
    }
    var reviewDraft by remember(initialReviewDraft) { mutableStateOf(initialReviewDraft) }
    var rootMessage by rememberSaveable { mutableStateOf<String?>(null) }

    DisposableEffect(historyBinder, dashboardBinder, scope) {
        val historyJob = historyBinder.bind(scope)
        val dashboardJob = dashboardBinder.bind(scope)
        onDispose {
            historyJob.cancel()
            dashboardJob.cancel()
        }
    }

    if (launchState == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Loading app...")
        }
        return
    }

    val navController = rememberNavController()

    LaunchedEffect(launchState) {
        rootMessage = launchState?.userMessage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    navController.navigate(AppRoute.Import.path()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Text("Import")
            }
            Button(
                onClick = {
                    navController.navigate(AppRoute.History.path()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Text("History")
            }
            Button(
                onClick = {
                    navController.navigate(AppRoute.Dashboard.path()) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                Text("Dashboard")
            }
        }

        rootMessage?.let { message ->
            Text(message)
        }

        NavHost(
            navController = navController,
            startDestination = launchState!!.currentPath,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppRoute.Import.pattern) {
                ImportScreen(
                    runtime = importRuntime,
                    onReviewDraftReady = { draft ->
                        reviewDraft = draft
                        rootMessage = null
                        navController.navigate(AppRoute.Review.path()) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppRoute.Review.pattern) {
                val draft = reviewDraft
                if (draft == null) {
                    LaunchedEffect(Unit) {
                        val fallback = navigationCoordinator.openReview(
                            currentState = navigationCoordinator.resolveLaunchState(hasSavedRecords = repository.hasSavedRecords()),
                            draftAvailable = false
                        )
                        rootMessage = fallback.userMessage
                        navController.navigate(fallback.currentPath) {
                            popUpTo(AppRoute.Import.path()) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    Text("Select one screenshot to import.")
                } else {
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
                            navController.navigate(AppRoute.History.path()) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                            importRuntime.reset()
                            rootMessage = null
                        }
                    )
                }
            }
            composable(AppRoute.History.pattern) {
                HistoryScreen(
                    state = historyState,
                    onRecordSelected = { recordId ->
                        navController.navigate(AppRoute.RecordDetail.path(recordId))
                    }
                )
            }
            composable(AppRoute.Dashboard.pattern) {
                DashboardScreen(state = dashboardState)
            }
            composable(
                route = AppRoute.RecordDetail.pattern,
                arguments = listOf(
                    navArgument("recordId") {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getString("recordId")
                LaunchedEffect(recordId) {
                    if (recordId.isNullOrBlank()) {
                        val fallback = navigationCoordinator.openDetail(recordId = null)
                        rootMessage = fallback.userMessage
                        navController.navigate(fallback.currentPath) {
                            popUpTo(AppRoute.History.path()) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else {
                        historyBinder.openDetail(scope, recordId)
                    }
                }
                LaunchedEffect(recordId, historyState.detail, historyState.userMessage) {
                    if (!recordId.isNullOrBlank() &&
                        historyState.detail == null &&
                        historyState.userMessage != null
                    ) {
                        rootMessage = historyState.userMessage
                        navController.navigate(AppRoute.History.path()) {
                            popUpTo(AppRoute.History.path()) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
                when {
                    recordId.isNullOrBlank() -> Text("Loading record...")
                    historyState.detail?.recordId == recordId -> RecordDetailScreen(
                        state = historyState.detail!!
                    )
                    else -> Text("Loading record...")
                }
            }
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
            is ImportRuntimeStatus.Failed -> Text(current.message)
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
                                Text(
                                    record.hero ?: "Unknown Hero",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    record.result ?: "Unknown Result",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        state.userMessage?.let { message ->
            Text(message, style = MaterialTheme.typography.bodyMedium)
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

private suspend fun resolveLaunchState(
    repository: RoomObservedMatchRepository,
    coordinator: AppNavigationCoordinator,
    initialRoute: String?,
    initialReviewDraft: DraftRecord?
): AppShellState {
    if (initialRoute == null) {
        return withContext(Dispatchers.IO) {
            coordinator.resolveLaunchState(hasSavedRecords = repository.hasSavedRecords())
        }
    }

    return when {
        initialRoute == AppRoute.Import.path() -> AppShellState(
            currentRoute = AppRoute.Import,
            currentPath = AppRoute.Import.path(),
            availableRoutes = AppRoutes.all
        )
        initialRoute == AppRoute.History.path() -> AppShellState(
            currentRoute = AppRoute.History,
            currentPath = AppRoute.History.path(),
            availableRoutes = AppRoutes.all
        )
        initialRoute == AppRoute.Dashboard.path() -> AppShellState(
            currentRoute = AppRoute.Dashboard,
            currentPath = AppRoute.Dashboard.path(),
            availableRoutes = AppRoutes.all
        )
        initialRoute == AppRoute.Review.path() -> {
            if (initialReviewDraft == null) {
                coordinator.openReview(
                    currentState = coordinator.resolveLaunchState(hasSavedRecords = repository.hasSavedRecords()),
                    draftAvailable = false
                )
            } else {
                AppShellState(
                    currentRoute = AppRoute.Review,
                    currentPath = AppRoute.Review.path(),
                    availableRoutes = AppRoutes.all
                )
            }
        }
        initialRoute.startsWith("detail/") -> {
            val recordId = initialRoute.removePrefix("detail/").ifBlank { null }
            coordinator.openDetail(recordId)
        }
        else -> coordinator.resolveLaunchState(hasSavedRecords = repository.hasSavedRecords()).copy(
            userMessage = "Could not open the requested screen."
        )
    }
}
