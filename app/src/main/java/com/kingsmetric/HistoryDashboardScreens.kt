package com.kingsmetric

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kingsmetric.app.AndroidPhotoPickerImportAdapter
import com.kingsmetric.app.AppShellChrome
import com.kingsmetric.app.AppShellDestinationKind
import com.kingsmetric.app.AndroidPhotoPickerRuntime
import com.kingsmetric.app.AppNavigationCoordinator
import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.AppRoutes
import com.kingsmetric.app.AppShellState
import com.kingsmetric.app.DashboardScreenBinder
import com.kingsmetric.app.DashboardScreenUiState
import com.kingsmetric.app.DetailScreenUiState
import com.kingsmetric.app.DiagnosticsScreenRoute
import com.kingsmetric.app.DiagnosticsScreenViewModel
import com.kingsmetric.app.HistoryScreenBinder
import com.kingsmetric.app.HistoryScreenUiState
import com.kingsmetric.app.ImportScreenUiStateMapper
import com.kingsmetric.app.ImportRuntimeStatus
import com.kingsmetric.app.PreviewAvailability
import com.kingsmetric.app.ReviewScreenRoute
import com.kingsmetric.app.ReviewScreenViewModel
import com.kingsmetric.app.SharedMessageKey
import com.kingsmetric.app.SharedUxCopy
import com.kingsmetric.app.UriScreenshotStorage
import com.kingsmetric.diagnostics.DiagnosticsRecorder
import com.kingsmetric.diagnostics.NoOpDiagnosticsRecorder
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.MatchImportWorkflow
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDashboardRoot(
    repository: RoomObservedMatchRepository,
    uriStorage: UriScreenshotStorage,
    recognizeStoredScreenshot: (String) -> ImportResult,
    reviewWorkflow: MatchImportWorkflow,
    diagnosticsRecorder: DiagnosticsRecorder = NoOpDiagnosticsRecorder,
    navigationCoordinator: AppNavigationCoordinator = remember { AppNavigationCoordinator() },
    initialRoute: String? = null,
    initialReviewDraft: DraftRecord? = null,
    testImportedDraft: DraftRecord? = null,
    onReviewSaveSucceeded: (() -> Unit)? = null
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
            },
            diagnosticsRecorder = diagnosticsRecorder
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
    var reviewDraft by rememberReviewDraftState(initialReviewDraft = initialReviewDraft)
    var rootMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var clearDraftAfterReviewExit by rememberSaveable { mutableStateOf(false) }

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
            Text(SharedUxCopy.message(SharedMessageKey.APP_LOADING).text)
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = AppShellChrome.routeForPath(
        navBackStackEntry?.destination?.route ?: launchState!!.currentPath
    )
    val shellChrome = AppShellChrome.forRoute(currentRoute)

    LaunchedEffect(launchState) {
        rootMessage = launchState?.userMessage
    }

    LaunchedEffect(currentRoute, clearDraftAfterReviewExit) {
        if (clearDraftAfterReviewExit && currentRoute != AppRoute.Review) {
            reviewDraft = null
            clearDraftAfterReviewExit = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = shellChrome.title,
                        modifier = Modifier.testTag("shell-title")
                    )
                },
                navigationIcon = {
                    if (shellChrome.kind == AppShellDestinationKind.Secondary) {
                        TextButton(
                            onClick = {
                                when (currentRoute) {
                                    AppRoute.Review -> {
                                        if (!navController.popBackStack()) {
                                            navigatePrimary(navController, AppRoute.Import)
                                        }
                                    }
                                    AppRoute.RecordDetail -> {
                                        if (!navController.popBackStack()) {
                                            navigatePrimary(navController, AppRoute.History)
                                        }
                                    }
                                    else -> Unit
                                }
                            },
                            modifier = Modifier.testTag("shell-secondary-action")
                        ) {
                            Text(shellChrome.secondaryActionLabel.orEmpty())
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (shellChrome.kind == AppShellDestinationKind.Primary) {
                Column(modifier = Modifier.testTag("primary-nav")) {
                    NavigationBar {
                        AppShellChrome.primaryRoutes.forEach { route ->
                            val chrome = AppShellChrome.forRoute(route)
                            NavigationBarItem(
                                selected = route == currentRoute,
                                onClick = { navigatePrimary(navController, route) },
                                icon = { Text(chrome.navigationLabel.take(1)) },
                                label = { Text(chrome.navigationLabel) },
                                modifier = Modifier.testTag("nav-${route.path()}")
                            )
                        }
                    }
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        testImportedDraft = testImportedDraft,
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
                        Text(SharedUxCopy.message(SharedMessageKey.IMPORT_IDLE).text)
                    } else {
                        val reviewViewModel = remember(draft, reviewWorkflow) {
                            ReviewScreenViewModel(
                                draft = draft,
                                workflow = reviewWorkflow,
                                onDraftChanged = { updatedDraft ->
                                    reviewDraft = updatedDraft
                                },
                                diagnosticsRecorder = diagnosticsRecorder,
                                previewAvailableResolver = { path ->
                                    path?.let(::File)?.exists() == true
                                }
                            )
                        }
                        ReviewScreenRoute(
                            viewModel = reviewViewModel,
                            onSaveSucceeded = {
                                onReviewSaveSucceeded?.invoke()
                                clearDraftAfterReviewExit = true
                                navController.navigate(AppRoute.History.path()) {
                                    popUpTo(AppRoute.Review.path()) {
                                        inclusive = true
                                    }
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
                composable(AppRoute.Diagnostics.pattern) {
                    val diagnosticsViewModel = remember(diagnosticsRecorder) {
                        DiagnosticsScreenViewModel(diagnosticsRecorder)
                    }
                    DiagnosticsScreenRoute(viewModel = diagnosticsViewModel)
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
}

@Composable
fun ImportScreen(
    runtime: AndroidPhotoPickerRuntime,
    testImportedDraft: DraftRecord? = null,
    onReviewDraftReady: (DraftRecord) -> Unit
) {
    val runtimeState by runtime.state.collectAsState()
    val status = runtimeState.status
    val model = remember(status) { ImportScreenUiStateMapper().map(status) }
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        scope.launch {
            runtime.beginImport()
            withContext(Dispatchers.Default) {
                runtime.handlePickerResult(uri?.toString())
            }
            val updated = runtime.state.value.status
            if (updated is ImportRuntimeStatus.ReviewReady) {
                onReviewDraftReady(updated.draft)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(model.title)
        Text(model.supportedScreenshotHint)

        testImportedDraft?.let { draft ->
            Button(
                onClick = {
                    onReviewDraftReady(draft)
                }
            ) {
                Text("Use Test Draft")
            }
        }
        Button(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.testTag("import-primary-action")
        ) {
            Text(model.primaryActionLabel)
        }

        Text(model.guidance)

        when (val current = status) {
            ImportRuntimeStatus.Idle,
            ImportRuntimeStatus.InProgress,
            is ImportRuntimeStatus.Unsupported,
            is ImportRuntimeStatus.SourceFailed,
            is ImportRuntimeStatus.StorageFailed,
            is ImportRuntimeStatus.Failed -> Unit
            is ImportRuntimeStatus.ReviewReady -> {
                Text(current.draft.screenshotPath.orEmpty())
            }
        }

        val reviewDraft = model.reviewDraft
        if (model.showContinueReview && reviewDraft != null) {
            Button(
                onClick = { onReviewDraftReady(reviewDraft) }
            ) {
                Text(model.continueReviewLabel)
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
            HistoryContentState.Empty -> Text(SharedUxCopy.message(SharedMessageKey.HISTORY_EMPTY).text)
            is HistoryContentState.Error -> Text(content.message)
            is HistoryContentState.Loaded -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.recordId }) { row ->
                        Card(
                            modifier = Modifier
                                .testTag("history-record-${row.recordId}")
                                .fillMaxWidth()
                                .clickable(enabled = row.selectable) { onRecordSelected(row.recordId) }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    row.primaryText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    row.resultText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(row.recencyText, style = MaterialTheme.typography.bodySmall)
                                row.previewText?.let { previewText ->
                                    Text(previewText, style = MaterialTheme.typography.bodySmall)
                                }
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
        DashboardContentState.Empty -> Text(SharedUxCopy.message(SharedMessageKey.DASHBOARD_EMPTY).text)
        is DashboardContentState.Error -> Text(content.message)
        is DashboardContentState.Loaded -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.contextText?.let { Text(it) }
                state.primaryCards.forEach { card ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(card.label, style = MaterialTheme.typography.labelMedium)
                            Text(card.valueText, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                state.sparseDataText?.let { Text(it) }
                state.secondaryNotes.forEach { note -> Text(note) }
            }
        }
    }
}

@Composable
fun RecordDetailScreen(state: DetailScreenUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(state.summaryTitle, style = MaterialTheme.typography.headlineSmall)
        Text(state.summaryResult, style = MaterialTheme.typography.titleMedium)
        Text(
            if (state.previewAvailability == PreviewAvailability.Available) {
                state.screenshotPath ?: state.previewStatusText
            } else {
                SharedUxCopy.message(SharedMessageKey.MISSING_SCREENSHOT_PREVIEW).text
            }
        )
        state.sections.forEach { section ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(section.title, style = MaterialTheme.typography.titleMedium)
                    section.fields.forEachIndexed { index, field ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(field.label, style = MaterialTheme.typography.labelMedium)
                            Text(field.valueText, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (index != section.fields.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun navigatePrimary(
    navController: androidx.navigation.NavHostController,
    route: AppRoute
) {
    navController.navigate(route.path()) {
        popUpTo(navController.graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
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
