package com.kingsmetric.marksman

import com.kingsmetric.data.local.SavedMatchEntity
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey

private const val MARKSMAN_LANE = "\u53d1\u80b2\u8def"
private const val LEGACY_MARKSMAN_LANE_ALIAS = "Farm Lane"

internal data class MarksmanLaneMetricGroupDefinition(
    val group: MarksmanLaneMetricGroup,
    val fields: List<FieldKey>
)

enum class MarksmanLaneMetricGroup {
    MatchContext,
    EconomyAndFarming,
    OutputAndPressure,
    SurvivalAndRisk,
    TeamfightPresence
}

internal val marksmanLaneMetricGroups = listOf(
    MarksmanLaneMetricGroupDefinition(
        group = MarksmanLaneMetricGroup.MatchContext,
        fields = listOf(FieldKey.RESULT, FieldKey.LANE, FieldKey.SCORE, FieldKey.KDA)
    ),
    MarksmanLaneMetricGroupDefinition(
        group = MarksmanLaneMetricGroup.EconomyAndFarming,
        fields = listOf(
            FieldKey.TOTAL_GOLD,
            FieldKey.GOLD_SHARE,
            FieldKey.GOLD_FROM_FARMING,
            FieldKey.LAST_HITS
        )
    ),
    MarksmanLaneMetricGroupDefinition(
        group = MarksmanLaneMetricGroup.OutputAndPressure,
        fields = listOf(
            FieldKey.DAMAGE_DEALT,
            FieldKey.DAMAGE_SHARE,
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS,
            FieldKey.SCORE
        )
    ),
    MarksmanLaneMetricGroupDefinition(
        group = MarksmanLaneMetricGroup.SurvivalAndRisk,
        fields = listOf(
            FieldKey.KDA,
            FieldKey.DAMAGE_TAKEN,
            FieldKey.DAMAGE_TAKEN_SHARE
        )
    ),
    MarksmanLaneMetricGroupDefinition(
        group = MarksmanLaneMetricGroup.TeamfightPresence,
        fields = listOf(
            FieldKey.PARTICIPATION_RATE,
            FieldKey.KILL_PARTICIPATION_COUNT,
            FieldKey.CONTROL_DURATION
        )
    )
)

data class MarksmanLaneMetricGroupCoverage(
    val group: MarksmanLaneMetricGroup,
    val missingFields: Set<FieldKey>
) {
    val isComplete: Boolean
        get() = missingFields.isEmpty()
}

enum class MarksmanLaneInsufficiencyReason {
    MissingLane
}

data class MarksmanLaneAnalysisInput(
    val result: String?,
    val lane: String,
    val score: String?,
    val kda: String?,
    val totalGold: String?,
    val goldShare: String?,
    val goldFromFarming: String?,
    val lastHits: String?,
    val damageDealt: String?,
    val damageShare: String?,
    val damageDealtToOpponents: String?,
    val damageTaken: String?,
    val damageTakenShare: String?,
    val participationRate: String?,
    val killParticipationCount: String?,
    val controlDuration: String?
) {
    fun valueFor(field: FieldKey): String? {
        return when (field) {
            FieldKey.RESULT -> result
            FieldKey.LANE -> lane
            FieldKey.SCORE -> score
            FieldKey.KDA -> kda
            FieldKey.TOTAL_GOLD -> totalGold
            FieldKey.GOLD_SHARE -> goldShare
            FieldKey.GOLD_FROM_FARMING -> goldFromFarming
            FieldKey.LAST_HITS -> lastHits
            FieldKey.DAMAGE_DEALT -> damageDealt
            FieldKey.DAMAGE_SHARE -> damageShare
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS -> damageDealtToOpponents
            FieldKey.DAMAGE_TAKEN -> damageTaken
            FieldKey.DAMAGE_TAKEN_SHARE -> damageTakenShare
            FieldKey.PARTICIPATION_RATE -> participationRate
            FieldKey.KILL_PARTICIPATION_COUNT -> killParticipationCount
            FieldKey.CONTROL_DURATION -> controlDuration
            else -> null
        }
    }
}

sealed interface MarksmanLaneAnalysisState {
    data class Eligible(
        val input: MarksmanLaneAnalysisInput,
        val groupCoverage: List<MarksmanLaneMetricGroupCoverage>
    ) : MarksmanLaneAnalysisState

    data class UnavailableForThisLane(val lane: String) : MarksmanLaneAnalysisState

    data class InsufficientSavedData(
        val reason: MarksmanLaneInsufficiencyReason
    ) : MarksmanLaneAnalysisState
}

class MarksmanLaneAnalysisInputFactory {

    fun from(record: SavedMatchHistoryRecord): MarksmanLaneAnalysisState {
        return fromFieldAccess { key -> record.fields[key] }
    }

    fun from(entity: SavedMatchEntity): MarksmanLaneAnalysisState {
        return fromFieldAccess { key ->
            when (key) {
                FieldKey.RESULT -> entity.result
                FieldKey.LANE -> entity.lane
                FieldKey.SCORE -> entity.score
                FieldKey.KDA -> entity.kda
                FieldKey.DAMAGE_DEALT -> entity.damageDealt
                FieldKey.DAMAGE_SHARE -> entity.damageShare
                FieldKey.DAMAGE_TAKEN -> entity.damageTaken
                FieldKey.DAMAGE_TAKEN_SHARE -> entity.damageTakenShare
                FieldKey.TOTAL_GOLD -> entity.totalGold
                FieldKey.GOLD_SHARE -> entity.goldShare
                FieldKey.PARTICIPATION_RATE -> entity.participationRate
                FieldKey.GOLD_FROM_FARMING -> entity.goldFromFarming
                FieldKey.LAST_HITS -> entity.lastHits
                FieldKey.KILL_PARTICIPATION_COUNT -> entity.killParticipationCount
                FieldKey.CONTROL_DURATION -> entity.controlDuration
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS -> entity.damageDealtToOpponents
                else -> null
            }
        }
    }

    private fun fromFieldAccess(valueFor: (FieldKey) -> String?): MarksmanLaneAnalysisState {
        val rawLane = valueFor(FieldKey.LANE)?.trim().orEmpty()
        val normalizedLane = normalizeLane(rawLane)
        if (normalizedLane.isEmpty()) {
            return MarksmanLaneAnalysisState.InsufficientSavedData(
                reason = MarksmanLaneInsufficiencyReason.MissingLane
            )
        }
        if (normalizedLane != MARKSMAN_LANE) {
            return MarksmanLaneAnalysisState.UnavailableForThisLane(lane = rawLane)
        }

        val input = MarksmanLaneAnalysisInput(
            result = valueFor(FieldKey.RESULT),
            lane = normalizedLane,
            score = valueFor(FieldKey.SCORE),
            kda = valueFor(FieldKey.KDA),
            totalGold = valueFor(FieldKey.TOTAL_GOLD),
            goldShare = valueFor(FieldKey.GOLD_SHARE),
            goldFromFarming = valueFor(FieldKey.GOLD_FROM_FARMING),
            lastHits = valueFor(FieldKey.LAST_HITS),
            damageDealt = valueFor(FieldKey.DAMAGE_DEALT),
            damageShare = valueFor(FieldKey.DAMAGE_SHARE),
            damageDealtToOpponents = valueFor(FieldKey.DAMAGE_DEALT_TO_OPPONENTS),
            damageTaken = valueFor(FieldKey.DAMAGE_TAKEN),
            damageTakenShare = valueFor(FieldKey.DAMAGE_TAKEN_SHARE),
            participationRate = valueFor(FieldKey.PARTICIPATION_RATE),
            killParticipationCount = valueFor(FieldKey.KILL_PARTICIPATION_COUNT),
            controlDuration = valueFor(FieldKey.CONTROL_DURATION)
        )

        return MarksmanLaneAnalysisState.Eligible(
            input = input,
            groupCoverage = marksmanLaneMetricGroups.map { group ->
                MarksmanLaneMetricGroupCoverage(
                    group = group.group,
                    missingFields = group.fields.filterTo(linkedSetOf()) { key ->
                        valueFor(key).isNullOrBlank()
                    }
                )
            }
        )
    }

    private fun normalizeLane(rawLane: String): String {
        return when (rawLane) {
            LEGACY_MARKSMAN_LANE_ALIAS -> MARKSMAN_LANE
            else -> rawLane
        }
    }
}
