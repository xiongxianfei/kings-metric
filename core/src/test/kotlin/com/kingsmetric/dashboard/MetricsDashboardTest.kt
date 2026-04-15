package com.kingsmetric.dashboard

import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MetricsDashboardTest {

    @Test
    fun `T1 compute win rate from saved records`() {
        val calculator = DashboardMetricsCalculator()

        val metrics = calculator.calculate(
            listOf(
                DashboardFixtures.record(recordId = "record-1", result = "victory"),
                DashboardFixtures.record(recordId = "record-2", result = "defeat"),
                DashboardFixtures.record(recordId = "record-3", result = "victory")
            )
        )

        assertEquals(66.67, metrics.winRate?.percentage ?: 0.0, 0.01)
        assertEquals(2, metrics.winRate?.wins)
        assertEquals(3, metrics.winRate?.totalMatches)
    }

    @Test
    fun `T2 compute average KDA from saved records that contain valid KDA values`() {
        val calculator = DashboardMetricsCalculator()

        val metrics = calculator.calculate(
            listOf(
                DashboardFixtures.record(recordId = "record-1", kda = "12/2/6"),
                DashboardFixtures.record(recordId = "record-2", kda = "9/1/8")
            )
        )

        assertEquals(13.0, metrics.averageKda?.value ?: 0.0, 0.01)
        assertEquals(2, metrics.averageKda?.sampleSize)
    }

    @Test
    fun `T3 compute hero usage counts from saved records`() {
        val calculator = DashboardMetricsCalculator()

        val metrics = calculator.calculate(
            listOf(
                DashboardFixtures.record(recordId = "record-1", hero = "Sun Shangxiang"),
                DashboardFixtures.record(recordId = "record-2", hero = "Sun Shangxiang"),
                DashboardFixtures.record(recordId = "record-3", hero = "Diaochan")
            )
        )

        assertEquals(
            listOf(
                HeroUsageMetric(hero = "Sun Shangxiang", matches = 2),
                HeroUsageMetric(hero = "Diaochan", matches = 1)
            ),
            metrics.heroUsage
        )
    }

    @Test
    fun `T4 do not fabricate metrics from missing optional inputs`() {
        val calculator = DashboardMetricsCalculator()

        val metrics = calculator.calculate(
            listOf(
                DashboardFixtures.record(recordId = "record-1", fields = DashboardFixtures.baseFields()),
                DashboardFixtures.record(
                    recordId = "record-2",
                    fields = DashboardFixtures.baseFields() + (FieldKey.KILL_PARTICIPATION_COUNT to null)
                )
            )
        )

        assertNull(metrics.recentPerformance?.averageKillParticipationCount)
    }

    @Test
    fun `T5 metric logic is exposed outside UI code and testable as pure logic`() {
        val calculator = DashboardMetricsCalculator()

        val metrics = calculator.calculate(listOf(DashboardFixtures.record()))

        assertNotNull(metrics.winRate)
    }

    @Test
    fun `IT1 dashboard shows aggregate metrics when saved records exist`() {
        val controller = dashboardController(
            repository = FakeDashboardRepository(
                records = listOf(DashboardFixtures.record())
            )
        )

        val state = controller.load()

        assertTrue(state.content is DashboardContentState.Loaded)
        state.content as DashboardContentState.Loaded
        assertEquals(100.0, state.content.metrics.winRate?.percentage ?: 0.0, 0.01)
        assertTrue(state.content.metrics.heroUsage.isNotEmpty())
    }

    @Test
    fun `IT2 dashboard shows explicit empty state when no records exist`() {
        val controller = dashboardController(
            repository = FakeDashboardRepository(records = emptyList())
        )

        val state = controller.load()

        assertEquals(DashboardContentState.Empty, state.content)
    }

    @Test
    fun `IT3 dashboard refreshes after saved record set changes`() {
        val repository = FakeDashboardRepository(
            records = listOf(DashboardFixtures.record(recordId = "record-1", result = "victory"))
        )
        val controller = dashboardController(repository = repository)

        val initial = controller.load()
        repository.replaceRecords(
            listOf(
                DashboardFixtures.record(recordId = "record-1", result = "victory"),
                DashboardFixtures.record(recordId = "record-2", result = "defeat")
            )
        )
        val refreshed = controller.refresh()

        assertTrue(initial.content is DashboardContentState.Loaded)
        assertTrue(refreshed.content is DashboardContentState.Loaded)
        initial.content as DashboardContentState.Loaded
        refreshed.content as DashboardContentState.Loaded
        assertEquals(100.0, initial.content.metrics.winRate?.percentage ?: 0.0, 0.01)
        assertEquals(50.0, refreshed.content.metrics.winRate?.percentage ?: 0.0, 0.01)
    }

    @Test
    fun `IT4 one metric can degrade gracefully when required source data is missing while other metrics remain visible`() {
        val controller = dashboardController(
            repository = FakeDashboardRepository(
                records = listOf(
                    DashboardFixtures.record(),
                    DashboardFixtures.record(
                        recordId = "record-2",
                        fields = DashboardFixtures.baseFields() + (FieldKey.KILL_PARTICIPATION_COUNT to null)
                    )
                )
            )
        )

        val state = controller.load()

        assertTrue(state.content is DashboardContentState.Loaded)
        state.content as DashboardContentState.Loaded
        assertEquals(100.0, state.content.metrics.winRate?.percentage ?: 0.0, 0.01)
        assertNull(state.content.metrics.recentPerformance?.averageKillParticipationCount)
    }

    @Test
    fun `IT5 history load failure shows error state`() {
        val controller = dashboardController(
            repository = FakeDashboardRepository(shouldFailOnLoad = true)
        )

        val state = controller.load()

        assertTrue(state.content is DashboardContentState.Error)
        state.content as DashboardContentState.Error
        assertTrue(state.content.canRetry)
    }
}

private fun dashboardController(
    repository: FakeDashboardRepository = FakeDashboardRepository()
): MetricsDashboardController {
    return MetricsDashboardController(
        repository = repository,
        calculator = DashboardMetricsCalculator()
    )
}

private object DashboardFixtures {
    fun record(
        recordId: String = "record-1",
        result: String = "victory",
        hero: String = "Sun Shangxiang",
        kda: String = "11/1/5",
        fields: Map<FieldKey, String?> = baseFields()
    ): SavedMatchHistoryRecord {
        return SavedMatchHistoryRecord(
            recordId = recordId,
            savedAt = 100L,
            screenshotId = "shot-$recordId",
            screenshotPath = "stored/$recordId.png",
            fields = fields +
                (FieldKey.RESULT to result) +
                (FieldKey.HERO to hero) +
                (FieldKey.KDA to kda)
        )
    }

    fun baseFields(): Map<FieldKey, String?> = mapOf(
        FieldKey.RESULT to "victory",
        FieldKey.HERO to "Sun Shangxiang",
        FieldKey.PLAYER_NAME to "King",
        FieldKey.KDA to "11/1/5",
        FieldKey.KILL_PARTICIPATION_COUNT to "16"
    )
}
