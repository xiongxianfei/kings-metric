package com.kingsmetric.app

import com.kingsmetric.importflow.Anchor
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.ScreenshotAnalysis
import com.kingsmetric.importflow.Section
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewFieldGroupingAndLabelsTest {

    private val mapper = ReviewFieldGroupingMapper()

    @Test
    fun reviewGrouping_assignsAllFieldsToNamedSections() {
        val grouping = mapper.map(ReviewState.fromDraft(ReviewFieldGroupingFixtures.supportedDraft()))

        assertEquals(
            listOf(
                ReviewSectionId.MATCH_SUMMARY,
                ReviewSectionId.DAMAGE_OUTPUT,
                ReviewSectionId.SURVIVABILITY,
                ReviewSectionId.ECONOMY,
                ReviewSectionId.TEAM_PLAY
            ),
            grouping.sections.map { it.id }
        )
        assertEquals(FieldKey.all, grouping.sections.flatMap { it.fields }.map { it.key }.toSet())
        assertTrue(grouping.sections.all { it.title.isNotBlank() })
    }

    @Test
    fun reviewGrouping_usesStableFieldToSectionMapping() {
        val grouping = mapper.map(ReviewState.fromDraft(ReviewFieldGroupingFixtures.supportedDraft()))
        val fieldsBySection = grouping.sections.associate { section ->
            section.id to section.fields.map { it.key }.toSet()
        }

        assertEquals(
            setOf(
                FieldKey.RESULT,
                FieldKey.HERO,
                FieldKey.PLAYER_NAME,
                FieldKey.LANE,
                FieldKey.SCORE,
                FieldKey.KDA
            ),
            fieldsBySection.getValue(ReviewSectionId.MATCH_SUMMARY)
        )
        assertEquals(
            setOf(
                FieldKey.DAMAGE_DEALT,
                FieldKey.DAMAGE_SHARE,
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS
            ),
            fieldsBySection.getValue(ReviewSectionId.DAMAGE_OUTPUT)
        )
        assertEquals(
            setOf(
                FieldKey.DAMAGE_TAKEN,
                FieldKey.DAMAGE_TAKEN_SHARE,
                FieldKey.CONTROL_DURATION
            ),
            fieldsBySection.getValue(ReviewSectionId.SURVIVABILITY)
        )
        assertEquals(
            setOf(
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_SHARE,
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS
            ),
            fieldsBySection.getValue(ReviewSectionId.ECONOMY)
        )
        assertEquals(
            setOf(
                FieldKey.PARTICIPATION_RATE,
                FieldKey.KILL_PARTICIPATION_COUNT
            ),
            fieldsBySection.getValue(ReviewSectionId.TEAM_PLAY)
        )
    }

    @Test
    fun reviewGrouping_exposesUserFacingLabelsAndRequiredState() {
        val grouping = mapper.map(ReviewState.fromDraft(ReviewFieldGroupingFixtures.supportedDraft()))
        val kdaField = grouping.findField(FieldKey.KDA)
        val lastHitsField = grouping.findField(FieldKey.LAST_HITS)

        assertEquals("KDA Ratio", kdaField.label)
        assertTrue(kdaField.required)
        assertEquals("Last Hits", lastHitsField.label)
        assertFalse(lastHitsField.required)
    }

    @Test
    fun reviewGrouping_marksBlockingAndNonBlockingAttentionDistinctly() {
        val blockedGrouping = mapper.map(
            ReviewState.fromDraft(ReviewFieldGroupingFixtures.requiredMissingDraft())
        )
        val highlightedGrouping = mapper.map(
            ReviewState.fromDraft(ReviewFieldGroupingFixtures.optionalMissingDraft())
        )

        assertTrue(blockedGrouping.findField(FieldKey.KDA).blocking)
        assertTrue(blockedGrouping.findField(FieldKey.KDA).highlighted)
        assertFalse(highlightedGrouping.findField(FieldKey.LAST_HITS).blocking)
        assertTrue(highlightedGrouping.findField(FieldKey.LAST_HITS).highlighted)
    }

    @Test
    fun reviewGrouping_buildsBlockerSummaryWithSectionsToVisit() {
        val grouping = mapper.map(ReviewState.fromDraft(ReviewFieldGroupingFixtures.multipleBlockersDraft()))
        val summary = grouping.blockerSummary

        assertNotNull(summary)
        assertEquals(
            "Complete the required fields before saving.",
            summary?.message
        )
        assertEquals(
            listOf("Match Summary", "Damage Output"),
            summary?.sectionsToVisit
        )
        assertEquals(
            listOf("Damage Dealt", "KDA Ratio"),
            summary?.fieldLabels
        )
    }

    @Test
    fun reviewGrouping_preservesEditabilityAndValidationState() {
        val draft = ReviewFieldGroupingFixtures.requiredMissingDraft()
        val reviewState = ReviewState.fromDraft(draft)

        val grouping = mapper.map(reviewState)

        assertEquals(FieldKey.all, grouping.editableFields)
        assertFalse(grouping.canConfirm)
        assertEquals(draft.fields.keys, grouping.sections.flatMap { it.fields }.map { it.key }.toSet())
    }
}

private fun ReviewGrouping.findField(key: FieldKey): ReviewFieldPresentation {
    return sections.flatMap { it.fields }.first { it.key == key }
}

private object ReviewFieldGroupingFixtures {
    private val parser = DraftParser()

    fun supportedDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun optionalMissingDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun requiredMissingDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(visibleFields = FieldKey.all - FieldKey.KDA),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    fun multipleBlockersDraft(): DraftRecord = parser.createDraft(
        analysis = supportedAnalysis(
            visibleFields = FieldKey.all - setOf(FieldKey.KDA, FieldKey.DAMAGE_DEALT)
        ),
        screenshotId = "shot-1",
        screenshotPath = "stored/1-fixture.png"
    )

    private fun supportedAnalysis(
        visibleFields: Set<FieldKey> = FieldKey.all
    ): ScreenshotAnalysis {
        val rawValues = mapOf(
            FieldKey.RESULT to "victory",
            FieldKey.HERO to "Sun Shangxiang",
            FieldKey.PLAYER_NAME to "King",
            FieldKey.LANE to "Clash Lane",
            FieldKey.SCORE to "20-10",
            FieldKey.KDA to "11/1/5",
            FieldKey.DAMAGE_DEALT to "12345",
            FieldKey.DAMAGE_SHARE to "34%",
            FieldKey.DAMAGE_TAKEN to "9850",
            FieldKey.DAMAGE_TAKEN_SHARE to "28%",
            FieldKey.TOTAL_GOLD to "12543",
            FieldKey.GOLD_SHARE to "31%",
            FieldKey.PARTICIPATION_RATE to "76%",
            FieldKey.GOLD_FROM_FARMING to "3680",
            FieldKey.LAST_HITS to "71",
            FieldKey.KILL_PARTICIPATION_COUNT to "13",
            FieldKey.CONTROL_DURATION to "00:14",
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
        )
        return ScreenshotAnalysis(
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(
                Section.DAMAGE,
                Section.DAMAGE_TAKEN,
                Section.ECONOMY,
                Section.TEAM_PARTICIPATION
            ),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }
}
