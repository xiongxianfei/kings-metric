package com.kingsmetric.marksman

enum class MarksmanLaneSuggestionCategory {
    EconomyRhythm,
    RiskDisciplineAndSurvival,
    FollowTeamIsolation,
    OutputContribution
}

data class MarksmanLaneSuggestion(
    val category: MarksmanLaneSuggestionCategory,
    val title: String,
    val rationale: String,
    val evidenceLine: String
)

sealed interface MarksmanLaneSuggestionState {
    data class Suggestions(val items: List<MarksmanLaneSuggestion>) : MarksmanLaneSuggestionState
    data object NoHighPrioritySuggestions : MarksmanLaneSuggestionState
}

class MarksmanLaneSuggestionEngine {

    fun suggestionsFor(
        analysis: MarksmanLaneAnalysisState.Eligible,
        metrics: MarksmanLaneDetailedMetrics
    ): MarksmanLaneSuggestionState {
        val suggestions = buildList {
            economyRhythmSuggestion(analysis, metrics)?.let(::add)
            riskDisciplineSuggestion(analysis, metrics)?.let(::add)
            followTeamSuggestion(analysis, metrics)?.let(::add)
            outputContributionSuggestion(analysis, metrics)?.let(::add)
        }.take(MAX_VISIBLE_SUGGESTIONS)

        return if (suggestions.isEmpty()) {
            MarksmanLaneSuggestionState.NoHighPrioritySuggestions
        } else {
            MarksmanLaneSuggestionState.Suggestions(suggestions)
        }
    }

    private fun economyRhythmSuggestion(
        analysis: MarksmanLaneAnalysisState.Eligible,
        metrics: MarksmanLaneDetailedMetrics
    ): MarksmanLaneSuggestion? {
        val economyGroup = metrics.group(MarksmanLaneMetricGroup.EconomyAndFarming)
        if (!economyGroup.isComplete) {
            return null
        }

        val goldShare = parsePercent(analysis.input.goldShare) ?: return null
        val goldFromFarming = parseNumber(analysis.input.goldFromFarming) ?: return null
        val lastHits = parseNumber(analysis.input.lastHits) ?: return null

        if (goldShare >= 25.0 || goldFromFarming >= 3000.0 || lastHits >= 60.0) {
            return null
        }

        return MarksmanLaneSuggestion(
            category = MarksmanLaneSuggestionCategory.EconomyRhythm,
            title = "Economy Rhythm",
            rationale = "Your farming pace lagged behind a stable marksman lane curve in this match.",
            evidenceLine = "Gold share ${analysis.input.goldShare}, farming gold ${analysis.input.goldFromFarming}, last hits ${analysis.input.lastHits}."
        )
    }

    private fun riskDisciplineSuggestion(
        analysis: MarksmanLaneAnalysisState.Eligible,
        metrics: MarksmanLaneDetailedMetrics
    ): MarksmanLaneSuggestion? {
        val riskGroup = metrics.group(MarksmanLaneMetricGroup.SurvivalAndRisk)
        if (!riskGroup.isComplete) {
            return null
        }

        val deaths = parseKdaDeaths(analysis.input.kda) ?: return null
        val damageTakenShare = parsePercent(analysis.input.damageTakenShare) ?: return null

        if (deaths < 5.0 && damageTakenShare < 33.0) {
            return null
        }

        return MarksmanLaneSuggestion(
            category = MarksmanLaneSuggestionCategory.RiskDisciplineAndSurvival,
            title = "Risk Discipline",
            rationale = "Your survival profile suggests fights became too expensive for a marksman carry slot.",
            evidenceLine = "KDA ${analysis.input.kda}, damage taken share ${analysis.input.damageTakenShare}."
        )
    }

    private fun followTeamSuggestion(
        analysis: MarksmanLaneAnalysisState.Eligible,
        metrics: MarksmanLaneDetailedMetrics
    ): MarksmanLaneSuggestion? {
        val teamfightGroup = metrics.group(MarksmanLaneMetricGroup.TeamfightPresence)
        val participation = parsePercent(analysis.input.participationRate) ?: return null
        if (teamfightGroup.metrics.first { it.field == com.kingsmetric.importflow.FieldKey.PARTICIPATION_RATE }.availability ==
            MarksmanLaneDetailedMetricAvailability.Unavailable
        ) {
            return null
        }
        if (participation >= 65.0) {
            return null
        }

        return MarksmanLaneSuggestion(
            category = MarksmanLaneSuggestionCategory.FollowTeamIsolation,
            title = "Follow Team",
            rationale = "Your teamfight presence was low for a marksman lane role, so later fights likely missed your damage window.",
            evidenceLine = "Participation rate ${analysis.input.participationRate}."
        )
    }

    private fun outputContributionSuggestion(
        analysis: MarksmanLaneAnalysisState.Eligible,
        metrics: MarksmanLaneDetailedMetrics
    ): MarksmanLaneSuggestion? {
        val outputGroup = metrics.group(MarksmanLaneMetricGroup.OutputAndPressure)
        if (outputGroup.metrics.first { it.field == com.kingsmetric.importflow.FieldKey.DAMAGE_SHARE }.availability ==
            MarksmanLaneDetailedMetricAvailability.Unavailable
        ) {
            return null
        }

        val damageShare = parsePercent(analysis.input.damageShare) ?: return null
        if (damageShare >= 25.0) {
            return null
        }

        return MarksmanLaneSuggestion(
            category = MarksmanLaneSuggestionCategory.OutputContribution,
            title = "Output Contribution",
            rationale = "Your damage contribution stayed low for a marksman lane slot in this match.",
            evidenceLine = "Damage share ${analysis.input.damageShare}, damage to opponents ${analysis.input.damageDealtToOpponents ?: "unavailable"}."
        )
    }

    private fun parsePercent(value: String?): Double? {
        return value
            ?.trim()
            ?.removeSuffix("%")
            ?.toDoubleOrNull()
    }

    private fun parseNumber(value: String?): Double? {
        return value?.trim()?.toDoubleOrNull()
    }

    private fun parseKdaDeaths(value: String?): Double? {
        val parts = value?.split("/") ?: return null
        if (parts.size != 3) {
            return null
        }
        return parts[1].toDoubleOrNull()
    }

    private companion object {
        const val MAX_VISIBLE_SUGGESTIONS = 3
    }
}
