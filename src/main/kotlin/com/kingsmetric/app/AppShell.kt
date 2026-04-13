package com.kingsmetric.app

sealed class AppRoute(val pattern: String) {
    data object Import : AppRoute("import")
    data object Review : AppRoute("review")
    data object History : AppRoute("history")
    data object Dashboard : AppRoute("dashboard")
    data object RecordDetail : AppRoute("detail/{recordId}")
}

object AppRoutes {
    val all: Set<AppRoute> = setOf(
        AppRoute.Import,
        AppRoute.Review,
        AppRoute.History,
        AppRoute.Dashboard,
        AppRoute.RecordDetail
    )
}

data class AppShellState(
    val currentRoute: AppRoute,
    val currentPath: String,
    val availableRoutes: Set<AppRoute>,
    val userMessage: String? = null,
    val navigationHistory: List<String> = emptyList(),
    val isBlank: Boolean = false
)

class AppNavigationCoordinator {

    fun resolveLaunchState(hasSavedRecords: Boolean): AppShellState {
        val route = if (hasSavedRecords) AppRoute.History else AppRoute.Import
        return AppShellState(
            currentRoute = route,
            currentPath = route.path(),
            availableRoutes = AppRoutes.all
        )
    }

    fun openReview(currentState: AppShellState, draftAvailable: Boolean): AppShellState {
        if (!draftAvailable) {
            return currentState.copy(
                currentRoute = AppRoute.Import,
                currentPath = AppRoute.Import.path(),
                userMessage = "Review draft is no longer available.",
                navigationHistory = currentState.navigationHistory + currentState.currentPath
            )
        }
        return currentState.copy(
            currentRoute = AppRoute.Review,
            currentPath = AppRoute.Review.path(),
            userMessage = null,
            navigationHistory = currentState.navigationHistory + currentState.currentPath
        )
    }

    fun onSaveSucceeded(currentState: AppShellState): AppShellState {
        return currentState.copy(
            currentRoute = AppRoute.History,
            currentPath = AppRoute.History.path(),
            userMessage = null,
            navigationHistory = currentState.navigationHistory + currentState.currentPath
        )
    }

    fun openDetail(recordId: String?): AppShellState {
        return if (recordId.isNullOrBlank()) {
            AppShellState(
                currentRoute = AppRoute.History,
                currentPath = AppRoute.History.path(),
                availableRoutes = AppRoutes.all,
                userMessage = "Saved record is no longer available."
            )
        } else {
            AppShellState(
                currentRoute = AppRoute.RecordDetail,
                currentPath = AppRoute.RecordDetail.path(recordId),
                availableRoutes = AppRoutes.all
            )
        }
    }

    private fun AppRoute.path(recordId: String? = null): String {
        return when (this) {
            AppRoute.Import -> "import"
            AppRoute.Review -> "review"
            AppRoute.History -> "history"
            AppRoute.Dashboard -> "dashboard"
            AppRoute.RecordDetail -> "detail/${recordId ?: "{recordId}"}"
        }
    }
}

class AppShell(
    private val coordinator: AppNavigationCoordinator
) {

    fun launch(hasSavedRecords: Boolean): AppShellState {
        return coordinator.resolveLaunchState(hasSavedRecords)
    }

    fun navigateToReview(currentState: AppShellState, draftAvailable: Boolean): AppShellState {
        return coordinator.openReview(currentState, draftAvailable)
    }

    fun onSaveSucceeded(currentState: AppShellState): AppShellState {
        return coordinator.onSaveSucceeded(currentState)
    }

    fun openDetail(recordId: String?): AppShellState {
        return coordinator.openDetail(recordId)
    }
}
