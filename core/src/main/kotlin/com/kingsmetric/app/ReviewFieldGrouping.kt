package com.kingsmetric.app

import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ReviewState

enum class ReviewInputAffordance {
    Text,
    Number,
    Percentage,
    Duration,
    Ratio
}

enum class ReviewSectionId(val title: String) {
    MATCH_SUMMARY("Match Summary"),
    DAMAGE_OUTPUT("Damage Output"),
    SURVIVABILITY("Survivability"),
    ECONOMY("Economy"),
    TEAM_PLAY("Team Play")
}

data class ReviewFieldPresentation(
    val key: FieldKey,
    val label: String,
    val value: String?,
    val required: Boolean,
    val hint: String?,
    val inputAffordance: ReviewInputAffordance,
    val highlighted: Boolean,
    val blocking: Boolean
)

data class ReviewSectionPresentation(
    val id: ReviewSectionId,
    val title: String,
    val fields: List<ReviewFieldPresentation>,
    val hasBlockingFields: Boolean,
    val hasHighlightedFields: Boolean
)

data class ReviewBlockerSummary(
    val message: String,
    val sectionsToVisit: List<String>,
    val fieldLabels: List<String>
)

data class ReviewGrouping(
    val sections: List<ReviewSectionPresentation>,
    val blockerSummary: ReviewBlockerSummary?,
    val attentionSummary: String?,
    val editableFields: Set<FieldKey>,
    val canConfirm: Boolean
)

data class ReviewLayoutState(
    val previewMaxHeightDp: Int,
    val stickySaveActionVisible: Boolean,
    val singleScrollSurface: Boolean
)

class ReviewFieldGroupingMapper {

    fun map(reviewState: ReviewState): ReviewGrouping {
        val sections = sectionOrder.map { sectionId ->
            val fields = sectionFields.getValue(sectionId).map { fieldKey ->
                ReviewFieldPresentation(
                    key = fieldKey,
                    label = SharedUxCopy.field(fieldKey).label,
                    value = reviewState.fields.getValue(fieldKey).value,
                    required = SharedUxCopy.field(fieldKey).required,
                    hint = hintFor(fieldKey),
                    inputAffordance = inputAffordanceFor(fieldKey),
                    highlighted = fieldKey in reviewState.highlightedFields,
                    blocking = fieldKey in reviewState.blockingFields
                )
            }.sortedWith(
                compareByDescending<ReviewFieldPresentation> { it.blocking }
                    .thenByDescending { it.highlighted }
                    .thenBy { sectionFields.getValue(sectionId).indexOf(it.key) }
            )
            ReviewSectionPresentation(
                id = sectionId,
                title = sectionId.title,
                fields = fields,
                hasBlockingFields = fields.any { it.blocking },
                hasHighlightedFields = fields.any { it.highlighted }
            )
        }

        val blockerSummary = if (reviewState.blockingFields.isNotEmpty()) {
            ReviewBlockerSummary(
                message = SharedUxCopy.message(SharedMessageKey.REVIEW_BLOCKED_SAVE).text,
                sectionsToVisit = sections
                    .filter { it.hasBlockingFields }
                    .map { it.title },
                fieldLabels = reviewState.blockingFields
                    .map { SharedUxCopy.field(it).label }
                    .sorted()
            )
        } else {
            null
        }

        val attentionSummary = if (reviewState.blockingFields.isEmpty() &&
            reviewState.highlightedFields.isNotEmpty()
        ) {
            SharedUxCopy.message(SharedMessageKey.REVIEW_NEEDS_ATTENTION).text
        } else {
            null
        }

        return ReviewGrouping(
            sections = sections,
            blockerSummary = blockerSummary,
            attentionSummary = attentionSummary,
            editableFields = reviewState.editableFields,
            canConfirm = reviewState.canConfirm
        )
    }

    fun layoutFor(
        previewAvailability: PreviewAvailability,
        hasBlockerSummary: Boolean
    ): ReviewLayoutState {
        return ReviewLayoutState(
            previewMaxHeightDp = if (previewAvailability == PreviewAvailability.Available) 220 else 0,
            stickySaveActionVisible = true,
            singleScrollSurface = true
        )
    }

    private companion object {
        val sectionOrder = listOf(
            ReviewSectionId.MATCH_SUMMARY,
            ReviewSectionId.DAMAGE_OUTPUT,
            ReviewSectionId.SURVIVABILITY,
            ReviewSectionId.ECONOMY,
            ReviewSectionId.TEAM_PLAY
        )

        val sectionFields = mapOf(
            ReviewSectionId.MATCH_SUMMARY to listOf(
                FieldKey.RESULT,
                FieldKey.HERO,
                FieldKey.PLAYER_NAME,
                FieldKey.LANE,
                FieldKey.SCORE,
                FieldKey.KDA
            ),
            ReviewSectionId.DAMAGE_OUTPUT to listOf(
                FieldKey.DAMAGE_DEALT,
                FieldKey.DAMAGE_SHARE,
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS
            ),
            ReviewSectionId.SURVIVABILITY to listOf(
                FieldKey.DAMAGE_TAKEN,
                FieldKey.DAMAGE_TAKEN_SHARE,
                FieldKey.CONTROL_DURATION
            ),
            ReviewSectionId.ECONOMY to listOf(
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_SHARE,
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS
            ),
            ReviewSectionId.TEAM_PLAY to listOf(
                FieldKey.PARTICIPATION_RATE,
                FieldKey.KILL_PARTICIPATION_COUNT
            )
        )

        fun hintFor(fieldKey: FieldKey): String? {
            return when (fieldKey) {
                FieldKey.SCORE -> "Example: 20-10"
                FieldKey.KDA -> "Example: 11/1/5"
                FieldKey.DAMAGE_SHARE,
                FieldKey.DAMAGE_TAKEN_SHARE,
                FieldKey.GOLD_SHARE,
                FieldKey.PARTICIPATION_RATE -> "Example: 34%"
                FieldKey.CONTROL_DURATION -> "Example: 00:14"
                FieldKey.DAMAGE_DEALT,
                FieldKey.DAMAGE_TAKEN,
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS,
                FieldKey.KILL_PARTICIPATION_COUNT,
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS -> "Whole number"
                else -> null
            }
        }

        fun inputAffordanceFor(fieldKey: FieldKey): ReviewInputAffordance {
            return when (fieldKey) {
                FieldKey.DAMAGE_SHARE,
                FieldKey.DAMAGE_TAKEN_SHARE,
                FieldKey.GOLD_SHARE,
                FieldKey.PARTICIPATION_RATE -> ReviewInputAffordance.Percentage
                FieldKey.CONTROL_DURATION -> ReviewInputAffordance.Duration
                FieldKey.KDA,
                FieldKey.SCORE -> ReviewInputAffordance.Ratio
                FieldKey.DAMAGE_DEALT,
                FieldKey.DAMAGE_TAKEN,
                FieldKey.TOTAL_GOLD,
                FieldKey.GOLD_FROM_FARMING,
                FieldKey.LAST_HITS,
                FieldKey.KILL_PARTICIPATION_COUNT,
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS -> ReviewInputAffordance.Number
                else -> ReviewInputAffordance.Text
            }
        }
    }
}
