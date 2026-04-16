package com.kingsmetric.marksman

import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarksmanLaneSuggestionsTest {

    @Test
    fun `T17 suggestions are deterministic for the same eligible metrics`() {
        val calculator = MarksmanLaneDetailedMetricsCalculator()
        val eligible = eligibleAnalysisState(
            overrides = mapOf(
                FieldKey.GOLD_SHARE to "21%",
                FieldKey.GOLD_FROM_FARMING to "2200",
                FieldKey.LAST_HITS to "48",
                FieldKey.PARTICIPATION_RATE to "58%",
                FieldKey.DAMAGE_SHARE to "22%",
                FieldKey.KDA to "3/5/4",
                FieldKey.DAMAGE_TAKEN_SHARE to "34%"
            )
        )
        val metrics = calculator.calculate(eligible)
        val engine = MarksmanLaneSuggestionEngine()

        val first = engine.suggestionsFor(eligible, metrics)
        val second = engine.suggestionsFor(eligible, metrics)

        assertEquals(first, second)
    }

    @Test
    fun `T18 economy rhythm suggestion includes title rationale evidence and category`() {
        val eligible = eligibleAnalysisState(
            overrides = mapOf(
                FieldKey.GOLD_SHARE to "21%",
                FieldKey.GOLD_FROM_FARMING to "2200",
                FieldKey.LAST_HITS to "48"
            )
        )
        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val result = MarksmanLaneSuggestionEngine().suggestionsFor(eligible, metrics)

        assertTrue(result is MarksmanLaneSuggestionState.Suggestions)
        result as MarksmanLaneSuggestionState.Suggestions
        val suggestion = result.items.first { it.category == MarksmanLaneSuggestionCategory.EconomyRhythm }
        assertEquals("Economy Rhythm", suggestion.title)
        assertTrue(suggestion.rationale.isNotBlank())
        assertTrue(suggestion.evidenceLine.contains("Gold share"))
    }

    @Test
    fun `T19 no high priority triggers returns explicit neutral state`() {
        val eligible = eligibleAnalysisState()
        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val result = MarksmanLaneSuggestionEngine().suggestionsFor(eligible, metrics)

        assertEquals(MarksmanLaneSuggestionState.NoHighPrioritySuggestions, result)
    }

    @Test
    fun `T20 more than three triggered categories are capped to top three suggestions`() {
        val eligible = eligibleAnalysisState(
            overrides = mapOf(
                FieldKey.GOLD_SHARE to "21%",
                FieldKey.GOLD_FROM_FARMING to "2200",
                FieldKey.LAST_HITS to "48",
                FieldKey.KDA to "3/5/4",
                FieldKey.DAMAGE_TAKEN_SHARE to "34%",
                FieldKey.PARTICIPATION_RATE to "58%",
                FieldKey.DAMAGE_SHARE to "22%"
            )
        )
        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val result = MarksmanLaneSuggestionEngine().suggestionsFor(eligible, metrics)

        assertTrue(result is MarksmanLaneSuggestionState.Suggestions)
        result as MarksmanLaneSuggestionState.Suggestions
        assertEquals(3, result.items.size)
        assertEquals(
            listOf(
                MarksmanLaneSuggestionCategory.EconomyRhythm,
                MarksmanLaneSuggestionCategory.RiskDisciplineAndSurvival,
                MarksmanLaneSuggestionCategory.FollowTeamIsolation
            ),
            result.items.map { it.category }
        )
    }

    @Test
    fun `T21 missing evidence does not fabricate a suggestion in that category`() {
        val eligible = eligibleAnalysisState(
            overrides = mapOf(
                FieldKey.GOLD_SHARE to "21%",
                FieldKey.GOLD_FROM_FARMING to null,
                FieldKey.LAST_HITS to null,
                FieldKey.PARTICIPATION_RATE to "58%"
            )
        )
        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val result = MarksmanLaneSuggestionEngine().suggestionsFor(eligible, metrics)

        assertTrue(result is MarksmanLaneSuggestionState.Suggestions)
        result as MarksmanLaneSuggestionState.Suggestions
        assertTrue(result.items.none { it.category == MarksmanLaneSuggestionCategory.EconomyRhythm })
        assertTrue(result.items.any { it.category == MarksmanLaneSuggestionCategory.FollowTeamIsolation })
    }

    @Test
    fun `T22 suggestions stay within approved first release categories and avoid deferred macro claims`() {
        val eligible = eligibleAnalysisState(
            overrides = mapOf(
                FieldKey.GOLD_SHARE to "21%",
                FieldKey.GOLD_FROM_FARMING to "2200",
                FieldKey.LAST_HITS to "48",
                FieldKey.KDA to "3/5/4",
                FieldKey.DAMAGE_TAKEN_SHARE to "34%",
                FieldKey.PARTICIPATION_RATE to "58%"
            )
        )
        val metrics = MarksmanLaneDetailedMetricsCalculator().calculate(eligible)

        val result = MarksmanLaneSuggestionEngine().suggestionsFor(eligible, metrics)

        assertTrue(result is MarksmanLaneSuggestionState.Suggestions)
        result as MarksmanLaneSuggestionState.Suggestions
        assertTrue(
            result.items.all {
                it.category in setOf(
                    MarksmanLaneSuggestionCategory.EconomyRhythm,
                    MarksmanLaneSuggestionCategory.RiskDisciplineAndSurvival,
                    MarksmanLaneSuggestionCategory.FollowTeamIsolation,
                    MarksmanLaneSuggestionCategory.OutputContribution
                )
            }
        )
        assertTrue(
            result.items.none {
                val text = "${it.title} ${it.rationale} ${it.evidenceLine}"
                text.contains("gank", ignoreCase = true) ||
                    text.contains("vision", ignoreCase = true) ||
                    text.contains("rotate", ignoreCase = true) ||
                    text.contains("dragon", ignoreCase = true) ||
                    text.contains("tower conversion", ignoreCase = true)
            }
        )
    }
}

private fun eligibleAnalysisState(
    overrides: Map<FieldKey, String?> = emptyMap()
): MarksmanLaneAnalysisState.Eligible {
    val factory = MarksmanLaneAnalysisInputFactory()
    val state = factory.from(
        historyRecord(
            fields = defaultFields + overrides
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
