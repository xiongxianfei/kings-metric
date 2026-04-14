package com.kingsmetric.app

enum class AppShellDestinationKind {
    Primary,
    Secondary
}

data class AppShellDestinationChrome(
    val route: AppRoute,
    val title: String,
    val navigationLabel: String,
    val kind: AppShellDestinationKind,
    val secondaryActionLabel: String? = null
)

object AppShellChrome {
    val primaryRoutes: List<AppRoute> = listOf(
        AppRoute.Import,
        AppRoute.History,
        AppRoute.Dashboard
    )

    fun forRoute(route: AppRoute): AppShellDestinationChrome {
        return when (route) {
            AppRoute.Import -> AppShellDestinationChrome(
                route = route,
                title = "Import Match",
                navigationLabel = "Import",
                kind = AppShellDestinationKind.Primary
            )
            AppRoute.History -> AppShellDestinationChrome(
                route = route,
                title = "Match History",
                navigationLabel = "History",
                kind = AppShellDestinationKind.Primary
            )
            AppRoute.Dashboard -> AppShellDestinationChrome(
                route = route,
                title = "Dashboard",
                navigationLabel = "Dashboard",
                kind = AppShellDestinationKind.Primary
            )
            AppRoute.Review -> AppShellDestinationChrome(
                route = route,
                title = "Review Match",
                navigationLabel = "Review",
                kind = AppShellDestinationKind.Secondary,
                secondaryActionLabel = "Close"
            )
            AppRoute.RecordDetail -> AppShellDestinationChrome(
                route = route,
                title = "Match Detail",
                navigationLabel = "Detail",
                kind = AppShellDestinationKind.Secondary,
                secondaryActionLabel = "Back"
            )
        }
    }

    fun routeForPath(path: String?): AppRoute {
        return when {
            path == null -> AppRoute.Import
            path == AppRoute.Import.path() -> AppRoute.Import
            path == AppRoute.History.path() -> AppRoute.History
            path == AppRoute.Dashboard.path() -> AppRoute.Dashboard
            path == AppRoute.Review.path() -> AppRoute.Review
            path == AppRoute.RecordDetail.pattern -> AppRoute.RecordDetail
            path.startsWith("detail/") -> AppRoute.RecordDetail
            else -> AppRoute.Import
        }
    }
}
