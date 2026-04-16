package com.kingsmetric.marksman

import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarksmanLaneDetailedMetricsTest {

    @Test
    fun `T13 eligible marksman match produces all approved metric groups in stable order`() {
        val eligible = eligibleAnalysisState()

        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        assertEquals(
            listOf(
                MarksmanLaneMetricGroup.MatchContext,
                MarksmanLaneMetricGroup.EconomyAndFarming,
                MarksmanLaneMetricGroup.OutputAndPressure,
                MarksmanLaneMetricGroup.SurvivalAndRisk,
                MarksmanLaneMetricGroup.TeamfightPresence
            ),
            metrics.groups.map { it.group }
        )
        assertEquals(
            listOf(FieldKey.RESULT, FieldKey.LANE, FieldKey.SCORE, FieldKey.KDA),
            metrics.group(MarksmanLaneMetricGroup.MatchContext).metrics.map { it.field }
        )
        assertEquals(
            listOf(
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_SHARE,
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS
            ),
            metrics.group(MarksmanLaneMetricGroup.EconomyAndFarming).metrics.map { it.field }
        )
        assertEquals(
            listOf(
                FieldKey.DAMAGE_DEALT,
                FieldKey.DAMAGE_SHARE,
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS,
                FieldKey.SCORE
            ),
            metrics.group(MarksmanLaneMetricGroup.OutputAndPressure).metrics.map { it.field }
        )
        assertEquals(
            listOf(
                FieldKey.KDA,
                FieldKey.DAMAGE_TAKEN,
                FieldKey.DAMAGE_TAKEN_SHARE
            ),
            metrics.group(MarksmanLaneMetricGroup.SurvivalAndRisk).metrics.map { it.field }
        )
        assertEquals(
            listOf(
                FieldKey.PARTICIPATION_RATE,
                FieldKey.KILL_PARTICIPATION_COUNT,
                FieldKey.CONTROL_DURATION
            ),
            metrics.group(MarksmanLaneMetricGroup.TeamfightPresence).metrics.map { it.field }
        )
        assertTrue(metrics.groups.all { it.isComplete })
    }

    @Test
    fun `T14 missing optional fields do not fabricate metric values and only affected groups become partial`() {
        val eligible = eligibleAnalysisState(
            missingFields = setOf(
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS,
                FieldKey.CONTROL_DURATION
            )
        )

        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val economy = metrics.group(MarksmanLaneMetricGroup.EconomyAndFarming)
        assertEquals(MarksmanLaneGroupAvailability.Partial, economy.availability)
        assertEquals(
            MarksmanLaneDetailedMetricAvailability.Unavailable,
            economy.metrics.first { it.field == FieldKey.GOLD_FROM_FARMING }.availability
        )
        assertEquals(
            MarksmanLaneDetailedMetricAvailability.Unavailable,
            economy.metrics.first { it.field == FieldKey.LAST_HITS }.availability
        )
        assertEquals(
            null,
            economy.metrics.first { it.field == FieldKey.GOLD_FROM_FARMING }.value
        )

        val teamfight = metrics.group(MarksmanLaneMetricGroup.TeamfightPresence)
        assertEquals(MarksmanLaneGroupAvailability.Partial, teamfight.availability)
        assertEquals(
            MarksmanLaneDetailedMetricAvailability.Unavailable,
            teamfight.metrics.first { it.field == FieldKey.CONTROL_DURATION }.availability
        )
    }

    @Test
    fun `T15 one incomplete metric group does not blank out other independent groups`() {
        val eligible = eligibleAnalysisState(
            missingFields = setOf(FieldKey.CONTROL_DURATION)
        )

        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        assertEquals(
            MarksmanLaneGroupAvailability.Complete,
            metrics.group(MarksmanLaneMetricGroup.MatchContext).availability
        )
        assertEquals(
            MarksmanLaneGroupAvailability.Complete,
            metrics.group(MarksmanLaneMetricGroup.EconomyAndFarming).availability
        )
        assertEquals(
            MarksmanLaneGroupAvailability.Complete,
            metrics.group(MarksmanLaneMetricGroup.OutputAndPressure).availability
        )
        assertEquals(
            MarksmanLaneGroupAvailability.Complete,
            metrics.group(MarksmanLaneMetricGroup.SurvivalAndRisk).availability
        )
        assertEquals(
            MarksmanLaneGroupAvailability.Partial,
            metrics.group(MarksmanLaneMetricGroup.TeamfightPresence).availability
        )
    }

    @Test
    fun `T16 metrics never include fields outside the approved first release scope`() {
        val eligible = eligibleAnalysisState()

        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val metricFields = metrics.groups.flatMap { it.metrics }.map { it.field }.toSet()
        assertEquals(firstReleaseMetricFields, metricFields)
    }
}

private fun eligibleAnalysisState(
    missingFields: Set<FieldKey> = emptySet()
): MarksmanLaneAnalysisState.Eligible {
    val calculator = MarksmanLaneAnalysisInputFactory()
    val state = calculator.from(
        historyRecord(
            fields = defaultFields.mapValues { (key, value) ->
                if (key in missingFields) null else value
            }
        )
    )
    return state as MarksmanLaneAnalysisState.Eligible
}

private fun historyRecord(
    fields: Map<FieldKey, String?> = defaultFields
): SavedMatchHistoryRecord {
    return SavedMatchHistoryRecord(
        recordId = "record-1",
        savedAt = 200L,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        fields = fields
    )
}

private val firstReleaseMetricFields = setOf(
    FieldKey.RESULT,
    FieldKey.LANE,
    FieldKey.SCORE,
    FieldKey.KDA,
    FieldKey.TOTAL_GOLD,
    FieldKey.GOLD_SHARE,
    FieldKey.GOLD_FROM_FARMING,
    FieldKey.LAST_HITS,
    FieldKey.DAMAGE_DEALT,
    FieldKey.DAMAGE_SHARE,
    FieldKey.DAMAGE_DEALT_TO_OPPONENTS,
    FieldKey.DAMAGE_TAKEN,
    FieldKey.DAMAGE_TAKEN_SHARE,
    FieldKey.PARTICIPATION_RATE,
    FieldKey.KILL_PARTICIPATION_COUNT,
    FieldKey.CONTROL_DURATION
)

private val defaultFields = mapOf(
    FieldKey.RESULT to "victory",
    FieldKey.HERO to "Sun Shangxiang",
    FieldKey.PLAYER_NAME to "Player",
    FieldKey.LANE to MARKSMAN_LANE,
    FieldKey.SCORE to "20-10",
    FieldKey.KDA to "11/1/5",
    FieldKey.DAMAGE_DEALT to "12345",
    FieldKey.DAMAGE_SHARE to "34%",
    FieldKey.DAMAGE_TAKEN to "9876",
    FieldKey.DAMAGE_TAKEN_SHARE to "28%",
    FieldKey.TOTAL_GOLD to "12001",
    FieldKey.GOLD_SHARE to "31%",
    FieldKey.PARTICIPATION_RATE to "76%",
    FieldKey.GOLD_FROM_FARMING to "3500",
    FieldKey.LAST_HITS to "80",
    FieldKey.KILL_PARTICIPATION_COUNT to "13",
    FieldKey.CONTROL_DURATION to "00:14",
    FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
)

private const val MARKSMAN_LANE = "\u53d1\u80b2\u8def"
