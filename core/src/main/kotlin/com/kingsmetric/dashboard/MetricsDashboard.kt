package com.kingsmetric.dashboard

import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey
import kotlin.math.round

data class WinRateMetric(
    val wins: Int,
    val totalMatches: Int,
    val percentage: Double
)

data class AverageKdaMetric(
    val value: Double,
    val sampleSize: Int
)

data class HeroUsageMetric(
    val hero: String,
    val matches: Int
)

data class RecentPerformanceMetric(
    val recentMatchCount: Int,
    val winRatePercentage: Double,
    val averageKillParticipationCount: Double?
)

data class DashboardMetrics(
    val winRate: WinRateMetric?,
    val averageKda: AverageKdaMetric?,
    val heroUsage: List<HeroUsageMetric>,
    val recentPerformance: RecentPerformanceMetric?
)

sealed interface DashboardContentState {
    data object Empty : DashboardContentState
    data class Loaded(val metrics: DashboardMetrics) : DashboardContentState
    data class Error(val message: String, val canRetry: Boolean = true) : DashboardContentState
}

data class MetricsDashboardUiState(
    val content: DashboardContentState
)

interface DashboardRepository {
    fun loadRecords(): List<SavedMatchHistoryRecord>
}

class DashboardMetricsCalculator {

    fun calculate(records: List<SavedMatchHistoryRecord>): DashboardMetrics {
        return DashboardMetrics(
            winRate = calculateWinRate(records),
            averageKda = calculateAverageKda(records),
            heroUsage = calculateHeroUsage(records),
            recentPerformance = calculateRecentPerformance(records)
        )
    }

    private fun calculateWinRate(records: List<SavedMatchHistoryRecord>): WinRateMetric? {
        if (records.isEmpty()) {
            return null
        }
        val wins = records.count { it.fields[FieldKey.RESULT] == "victory" }
        return WinRateMetric(
            wins = wins,
            totalMatches = records.size,
            percentage = percentage(wins, records.size)
        )
    }

    private fun calculateAverageKda(records: List<SavedMatchHistoryRecord>): AverageKdaMetric? {
        val kdaValues = records.mapNotNull { record ->
            record.fields[FieldKey.KDA]?.let(::parseKdaRatio)
        }
        if (kdaValues.isEmpty()) {
            return null
        }
        return AverageKdaMetric(
            value = roundToTwoDecimals(kdaValues.average()),
            sampleSize = kdaValues.size
        )
    }

    private fun calculateHeroUsage(records: List<SavedMatchHistoryRecord>): List<HeroUsageMetric> {
        return records
            .mapNotNull { it.fields[FieldKey.HERO] }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { HeroUsageMetric(hero = it.key, matches = it.value) }
    }

    private fun calculateRecentPerformance(records: List<SavedMatchHistoryRecord>): RecentPerformanceMetric? {
        if (records.isEmpty()) {
            return null
        }
        val recentRecords = records
            .sortedByDescending { it.savedAt }
            .take(5)
        val wins = recentRecords.count { it.fields[FieldKey.RESULT] == "victory" }
        val killParticipationValues = recentRecords.map { record ->
            record.fields[FieldKey.KILL_PARTICIPATION_COUNT]?.toDoubleOrNull()
        }
        return RecentPerformanceMetric(
            recentMatchCount = recentRecords.size,
            winRatePercentage = percentage(wins, recentRecords.size),
            averageKillParticipationCount = killParticipationValues
                .takeIf { values -> values.all { it != null } }
                ?.filterNotNull()
                ?.average()
                ?.let(::roundToTwoDecimals)
        )
    }

    private fun parseKdaRatio(value: String): Double? {
        val parts = value.split("/")
        if (parts.size != 3) {
            return null
        }
        val kills = parts[0].toDoubleOrNull() ?: return null
        val deaths = parts[1].toDoubleOrNull() ?: return null
        val assists = parts[2].toDoubleOrNull() ?: return null
        val denominator = if (deaths == 0.0) 1.0 else deaths
        return (kills + assists) / denominator
    }

    private fun percentage(part: Int, total: Int): Double {
        return roundToTwoDecimals(part.toDouble() * 100 / total.toDouble())
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100) / 100
    }
}

class MetricsDashboardController(
    private val repository: DashboardRepository,
    private val calculator: DashboardMetricsCalculator
) {

    fun load(): MetricsDashboardUiState = loadState()

    fun refresh(): MetricsDashboardUiState = loadState()

    private fun loadState(): MetricsDashboardUiState {
        val records = try {
            repository.loadRecords()
        } catch (_: IllegalStateException) {
            return MetricsDashboardUiState(
                content = DashboardContentState.Error("Could not load dashboard metrics.")
            )
        }

        if (records.isEmpty()) {
            return MetricsDashboardUiState(content = DashboardContentState.Empty)
        }

        return MetricsDashboardUiState(
            content = DashboardContentState.Loaded(
                metrics = calculator.calculate(records)
            )
        )
    }
}

class FakeDashboardRepository(
    records: List<SavedMatchHistoryRecord> = emptyList(),
    private val shouldFailOnLoad: Boolean = false
) : DashboardRepository {

    private var currentRecords: List<SavedMatchHistoryRecord> = records

    override fun loadRecords(): List<SavedMatchHistoryRecord> {
        if (shouldFailOnLoad) {
            throw IllegalStateException("dashboard load failed")
        }
        return currentRecords
    }

    fun replaceRecords(records: List<SavedMatchHistoryRecord>) {
        currentRecords = records
    }
}
