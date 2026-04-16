package com.kingsmetric.ui.theme

import com.kingsmetric.app.AppRoute
import com.kingsmetric.app.AppShellDestinationKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellVisualFoundationTest {

    @Test
    fun sharedFoundation_exposes_one_reusable_shell_baseline() {
        val foundation = AppShellVisualFoundation.shared

        assertEquals(20, foundation.screenHorizontalPaddingDp)
        assertEquals(16, foundation.screenVerticalPaddingDp)
        assertEquals(16, foundation.sectionSpacingDp)
        assertEquals("shell-route-title", foundation.routeTitleStyleKey)
        assertEquals("shell-primary-action", foundation.primaryActionStyleKey)
        assertEquals("shell-surface-card", foundation.surfaceStyleKey)
        assertEquals("shell-state-block", foundation.stateBlockStyleKey)
    }

    @Test
    fun primaryDestinations_share_the_same_shell_presentation_tokens() {
        val foundation = AppShellVisualFoundation.shared
        val import = AppShellVisualFoundation.presentationFor(AppRoute.Import)
        val history = AppShellVisualFoundation.presentationFor(AppRoute.History)
        val dashboard = AppShellVisualFoundation.presentationFor(AppRoute.Dashboard)

        assertEquals(AppShellDestinationKind.Primary, import.destinationKind)
        assertEquals(AppShellDestinationKind.Primary, history.destinationKind)
        assertEquals(AppShellDestinationKind.Primary, dashboard.destinationKind)
        assertTrue(import.usesSharedFoundation)
        assertTrue(history.usesSharedFoundation)
        assertTrue(dashboard.usesSharedFoundation)
        assertEquals(foundation.screenHorizontalPaddingDp, import.screenHorizontalPaddingDp)
        assertEquals(import.screenHorizontalPaddingDp, history.screenHorizontalPaddingDp)
        assertEquals(import.screenHorizontalPaddingDp, dashboard.screenHorizontalPaddingDp)
        assertEquals(foundation.routeTitleStyleKey, import.routeTitleStyleKey)
        assertEquals(import.routeTitleStyleKey, history.routeTitleStyleKey)
        assertEquals(import.routeTitleStyleKey, dashboard.routeTitleStyleKey)
        assertEquals(foundation.surfaceStyleKey, history.surfaceStyleKey)
        assertEquals(history.surfaceStyleKey, dashboard.surfaceStyleKey)
        assertEquals(foundation.stateBlockStyleKey, history.stateBlockStyleKey)
        assertEquals(history.stateBlockStyleKey, dashboard.stateBlockStyleKey)
    }

    @Test
    fun secondaryScreens_inherit_shared_title_and_spacing_tokens() {
        val foundation = AppShellVisualFoundation.shared
        val review = AppShellVisualFoundation.presentationFor(AppRoute.Review)
        val detail = AppShellVisualFoundation.presentationFor(AppRoute.RecordDetail)

        assertEquals(AppShellDestinationKind.Secondary, review.destinationKind)
        assertEquals(AppShellDestinationKind.Secondary, detail.destinationKind)
        assertTrue(review.usesSharedFoundation)
        assertTrue(detail.usesSharedFoundation)
        assertEquals(foundation.routeTitleStyleKey, review.routeTitleStyleKey)
        assertEquals(review.routeTitleStyleKey, detail.routeTitleStyleKey)
        assertEquals(foundation.screenHorizontalPaddingDp, review.screenHorizontalPaddingDp)
        assertEquals(review.screenHorizontalPaddingDp, detail.screenHorizontalPaddingDp)
        assertEquals(foundation.primaryActionStyleKey, review.primaryActionStyleKey)
        assertNull(detail.primaryActionStyleKey)
    }

    @Test
    fun visualFoundation_stays_in_the_app_ui_layer() {
        assertTrue(
            AppShellVisualFoundation::class.java.name.startsWith("com.kingsmetric.ui.")
        )
    }
}
