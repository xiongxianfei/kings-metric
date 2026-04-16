package com.kingsmetric.ui.theme

import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.AppShellChrome
import com.kingsmetric.app.AppShellDestinationKind

data class ShellVisualFoundation(
    val screenHorizontalPaddingDp: Int,
    val screenVerticalPaddingDp: Int,
    val sectionSpacingDp: Int,
    val routeTitleStyleKey: String,
    val primaryActionStyleKey: String,
    val surfaceStyleKey: String,
    val stateBlockStyleKey: String
)

data class ShellRoutePresentation(
    val route: AppRoute,
    val destinationKind: AppShellDestinationKind,
    val usesSharedFoundation: Boolean,
    val screenHorizontalPaddingDp: Int,
    val screenVerticalPaddingDp: Int,
    val routeTitleStyleKey: String,
    val primaryActionStyleKey: String?,
    val surfaceStyleKey: String,
    val stateBlockStyleKey: String
)

object AppShellVisualFoundation {
    val shared = ShellVisualFoundation(
        screenHorizontalPaddingDp = 20,
        screenVerticalPaddingDp = 16,
        sectionSpacingDp = 16,
        routeTitleStyleKey = "shell-route-title",
        primaryActionStyleKey = "shell-primary-action",
        surfaceStyleKey = "shell-surface-card",
        stateBlockStyleKey = "shell-state-block"
    )

    fun presentationFor(route: AppRoute): ShellRoutePresentation {
        val chrome = AppShellChrome.forRoute(route)
        return ShellRoutePresentation(
            route = route,
            destinationKind = chrome.kind,
            usesSharedFoundation = true,
            screenHorizontalPaddingDp = shared.screenHorizontalPaddingDp,
            screenVerticalPaddingDp = shared.screenVerticalPaddingDp,
            routeTitleStyleKey = shared.routeTitleStyleKey,
            primaryActionStyleKey = when (route) {
                AppRoute.Import,
                AppRoute.Review -> shared.primaryActionStyleKey
                else -> null
            },
            surfaceStyleKey = shared.surfaceStyleKey,
            stateBlockStyleKey = shared.stateBlockStyleKey
        )
    }
}
