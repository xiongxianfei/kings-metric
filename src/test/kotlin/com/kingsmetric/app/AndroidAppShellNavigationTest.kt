package com.kingsmetric.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidAppShellNavigationTest {

    @Test
    fun `T1 navigation route definitions cover import review history dashboard and detail destinations`() {
        val routes = AppRoutes.all

        assertEquals(
            setOf(
                AppRoute.Import.pattern,
                AppRoute.Review.pattern,
                AppRoute.History.pattern,
                AppRoute.Dashboard.pattern,
                AppRoute.RecordDetail.pattern
            ),
            routes.map { it.pattern }.toSet()
        )
    }

    @Test
    fun `T2 navigation coordinator maps save success to the expected destination`() {
        val coordinator = appCoordinator()

        val state = coordinator.onSaveSucceeded(
            coordinator.resolveLaunchState(hasSavedRecords = false)
        )

        assertEquals(AppRoute.History, state.currentRoute)
        assertEquals("history", state.currentPath)
    }

    @Test
    fun `T3 missing required route arguments resolve to a safe fallback instruction`() {
        val coordinator = appCoordinator()

        val state = coordinator.openDetail(recordId = null)

        assertEquals(AppRoute.History, state.currentRoute)
        assertEquals("Saved record is no longer available.", state.userMessage)
    }

    @Test
    fun `IT1 app launch shows the root destination without crashing`() {
        val shell = appShell()

        val state = shell.launch(hasSavedRecords = false)

        assertEquals(AppRoute.Import, state.currentRoute)
        assertTrue(state.availableRoutes.contains(AppRoute.History))
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
        assertEquals(AppRoute.Import, state.currentRoute)
    }
}

private fun appCoordinator(): AppNavigationCoordinator {
    return AppNavigationCoordinator()
}

private fun appShell(): AppShell {
    return AppShell(appCoordinator())
}
