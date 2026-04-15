package com.kingsmetric.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidAppShellNavigationTest {

    @Test
    fun `T1 shell chrome classifies primary and secondary destinations`() {
        assertEquals(AppShellDestinationKind.Primary, AppShellChrome.forRoute(AppRoute.Import).kind)
        assertEquals(AppShellDestinationKind.Primary, AppShellChrome.forRoute(AppRoute.History).kind)
        assertEquals(AppShellDestinationKind.Primary, AppShellChrome.forRoute(AppRoute.Dashboard).kind)
        assertEquals(AppShellDestinationKind.Primary, AppShellChrome.forRoute(AppRoute.Diagnostics).kind)
        assertEquals(AppShellDestinationKind.Secondary, AppShellChrome.forRoute(AppRoute.Review).kind)
        assertEquals(AppShellDestinationKind.Secondary, AppShellChrome.forRoute(AppRoute.RecordDetail).kind)
    }

    @Test
    fun `T2 shell chrome exposes titles for every route`() {
        assertEquals("Import Match", AppShellChrome.forRoute(AppRoute.Import).title)
        assertEquals("Match History", AppShellChrome.forRoute(AppRoute.History).title)
        assertEquals("Dashboard", AppShellChrome.forRoute(AppRoute.Dashboard).title)
        assertEquals("Diagnostics", AppShellChrome.forRoute(AppRoute.Diagnostics).title)
        assertEquals("Review Match", AppShellChrome.forRoute(AppRoute.Review).title)
        assertEquals("Match Detail", AppShellChrome.forRoute(AppRoute.RecordDetail).title)
    }

    @Test
    fun `T3 missing draft state resolves to a safe fallback instruction`() {
        val coordinator = appCoordinator()

        val state = coordinator.openReview(
            currentState = coordinator.resolveLaunchState(hasSavedRecords = false),
            draftAvailable = false
        )

        assertEquals(AppRoute.Import, state.currentRoute)
        assertEquals("Review draft is no longer available.", state.userMessage)
    }

    @Test
    fun `T4 missing detail identity resolves to a safe fallback instruction`() {
        val coordinator = appCoordinator()

        val state = coordinator.openDetail(recordId = null)

        assertEquals(AppRoute.History, state.currentRoute)
        assertEquals("Saved record is no longer available.", state.userMessage)
    }

    @Test
    fun `T5 navigation coordinator maps save success to the expected destination`() {
        val coordinator = appCoordinator()

        val state = coordinator.onSaveSucceeded(
            coordinator.resolveLaunchState(hasSavedRecords = false)
        )

        assertEquals(AppRoute.History, state.currentRoute)
        assertEquals("history", state.currentPath)
    }

    @Test
    fun `IT1 app launch shows the root destination without crashing`() {
        val shell = appShell()

        val state = shell.launch(hasSavedRecords = false)

        assertEquals(AppRoute.Import, state.currentRoute)
        assertTrue(state.availableRoutes.contains(AppRoute.History))
        assertTrue(state.availableRoutes.contains(AppRoute.Diagnostics))
        assertEquals(AppShellDestinationKind.Primary, AppShellChrome.forRoute(state.currentRoute).kind)
    }

    @Test
    fun `IT2 user can navigate from import to review and then to the post-save destination`() {
        val shell = appShell()
        val launched = shell.launch(hasSavedRecords = false)
        val review = shell.navigateToReview(launched, draftAvailable = true)
        val saved = shell.onSaveSucceeded(review)

        assertEquals(AppRoute.Review, review.currentRoute)
        assertEquals(AppRoute.History, saved.currentRoute)
        assertTrue(saved.navigationHistory.contains("review"))
    }

    @Test
    fun `IT3 opening detail with a missing record id returns to a safe destination with an error state`() {
        val shell = appShell()

        val state = shell.openDetail(recordId = null)

        assertEquals(AppRoute.History, state.currentRoute)
        assertEquals("Saved record is no longer available.", state.userMessage)
        assertFalse(state.isBlank)
    }

    @Test
    fun `IT4 launch with empty local data still allows navigation to dashboard and history empty states`() {
        val shell = appShell()

        val state = shell.launch(hasSavedRecords = false)

        assertTrue(state.availableRoutes.contains(AppRoute.History))
        assertTrue(state.availableRoutes.contains(AppRoute.Dashboard))
        assertTrue(state.availableRoutes.contains(AppRoute.Diagnostics))
        assertEquals(AppRoute.Import, state.currentRoute)
    }
}

private fun appCoordinator(): AppNavigationCoordinator {
    return AppNavigationCoordinator()
}

private fun appShell(): AppShell {
    return AppShell(appCoordinator())
}
