package com.kingsmetric.marksman

import com.kingsmetric.data.local.SavedMatchEntity
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarksmanLaneAnalysisInputTest {

    @Test
    fun `T1 saved history record with marksman lane becomes eligible and captures approved inputs`() {
        val state = MarksmanLaneAnalysisInputFactory().from(historyRecord())

        assertTrue(state is MarksmanLaneAnalysisState.Eligible)
        state as MarksmanLaneAnalysisState.Eligible
        assertEquals(MARKSMAN_LANE, state.input.lane)
        assertEquals("20-10", state.input.score)
        assertEquals("11/1/5", state.input.kda)
        assertEquals("12001", state.input.totalGold)
        assertEquals("31%", state.input.goldShare)
        assertEquals("3500", state.input.goldFromFarming)
        assertEquals("80", state.input.lastHits)
        assertEquals("12345", state.input.damageDealt)
        assertEquals("34%", state.input.damageShare)
        assertEquals("10101", state.input.damageDealtToOpponents)
        assertEquals("9876", state.input.damageTaken)
        assertEquals("28%", state.input.damageTakenShare)
        assertEquals("76%", state.input.participationRate)
        assertEquals("13", state.input.killParticipationCount)
        assertEquals("00:14", state.input.controlDuration)
    }

    @Test
    fun `T2 saved history record and room entity produce the same eligible analysis input`() {
        val factory = MarksmanLaneAnalysisInputFactory()

        val fromRecord = factory.from(historyRecord())
        val fromEntity = factory.from(savedMatchEntity())

        assertEquals(fromRecord, fromEntity)
    }

    @Test
    fun `T3 non marksman lane becomes unavailable for this lane`() {
        val state = MarksmanLaneAnalysisInputFactory().from(
            historyRecord(
                fields = historyRecord().fields + (FieldKey.LANE to MID_LANE)
            )
        )

        assertEquals(
            MarksmanLaneAnalysisState.UnavailableForThisLane(lane = MID_LANE),
            state
        )
    }

    @Test
    fun `T4 legacy farm lane alias stays eligible for marksman analysis`() {
        val factory = MarksmanLaneAnalysisInputFactory()

        val fromRecord = factory.from(
            historyRecord(
                fields = historyRecord().fields + (FieldKey.LANE to FARM_LANE_ALIAS)
            )
        )
        val fromEntity = factory.from(
            savedMatchEntity(lane = FARM_LANE_ALIAS)
        )

        assertTrue(fromRecord is MarksmanLaneAnalysisState.Eligible)
        assertTrue(fromEntity is MarksmanLaneAnalysisState.Eligible)
        fromRecord as MarksmanLaneAnalysisState.Eligible
        fromEntity as MarksmanLaneAnalysisState.Eligible
        assertEquals(MARKSMAN_LANE, fromRecord.input.lane)
        assertEquals(MARKSMAN_LANE, fromEntity.input.lane)
    }

    @Test
    fun `T4 missing lane becomes insufficient saved data`() {
        val state = MarksmanLaneAnalysisInputFactory().from(
            historyRecord(
                fields = historyRecord().fields + (FieldKey.LANE to null)
            )
        )

        assertEquals(
            MarksmanLaneAnalysisState.InsufficientSavedData(
                reason = MarksmanLaneInsufficiencyReason.MissingLane
            ),
            state
        )
    }

    @Test
    fun `T5 eligible partial input stays eligible and marks group coverage gaps explicitly`() {
        val state = MarksmanLaneAnalysisInputFactory().from(
            historyRecord(
                fields = historyRecord().fields + mapOf(
                    FieldKey.GOLD_FROM_FARMING to null,
                    FieldKey.LAST_HITS to null,
                    FieldKey.CONTROL_DURATION to null
                )
            )
        )

        assertTrue(state is MarksmanLaneAnalysisState.Eligible)
        state as MarksmanLaneAnalysisState.Eligible
        assertEquals(
            MarksmanLaneMetricGroupCoverage(
                group = MarksmanLaneMetricGroup.EconomyAndFarming,
                missingFields = setOf(FieldKey.GOLD_FROM_FARMING, FieldKey.LAST_HITS)
            ),
            state.groupCoverage.first { it.group == MarksmanLaneMetricGroup.EconomyAndFarming }
        )
        assertEquals(
            MarksmanLaneMetricGroupCoverage(
                group = MarksmanLaneMetricGroup.TeamfightPresence,
                missingFields = setOf(FieldKey.CONTROL_DURATION)
            ),
            state.groupCoverage.first { it.group == MarksmanLaneMetricGroup.TeamfightPresence }
        )
    }

    @Test
    fun `T6 saved history record and room entity stay aligned on group coverage for the same partial match`() {
        val fields = historyRecord().fields + mapOf(
            FieldKey.GOLD_FROM_FARMING to null,
            FieldKey.LAST_HITS to null,
            FieldKey.CONTROL_DURATION to null
        )
        val entity = savedMatchEntity(
            goldFromFarming = null,
            lastHits = null,
            controlDuration = null
        )
        val factory = MarksmanLaneAnalysisInputFactory()

        val fromRecord = factory.from(historyRecord(fields = fields))
        val fromEntity = factory.from(entity)

        assertEquals(fromRecord, fromEntity)
    }
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

private fun savedMatchEntity(
    lane: String? = MARKSMAN_LANE,
    goldFromFarming: String? = "3500",
    lastHits: String? = "80",
    controlDuration: String? = "00:14"
): SavedMatchEntity {
    return SavedMatchEntity(
        recordId = "record-1",
        savedAt = 200L,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        result = "victory",
        hero = "Sun Shangxiang",
        playerName = "Player",
        lane = lane,
        score = "20-10",
        kda = "11/1/5",
        damageDealt = "12345",
        damageShare = "34%",
        damageTaken = "9876",
        damageTakenShare = "28%",
        totalGold = "12001",
        goldShare = "31%",
        participationRate = "76%",
        goldFromFarming = goldFromFarming,
        lastHits = lastHits,
        killParticipationCount = "13",
        controlDuration = controlDuration,
        damageDealtToOpponents = "10101"
    )
}

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
private const val FARM_LANE_ALIAS = "Farm Lane"
private const val MID_LANE = "\u4e2d\u8def"
