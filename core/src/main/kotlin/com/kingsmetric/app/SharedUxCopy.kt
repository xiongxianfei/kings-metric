package com.kingsmetric.app

import com.kingsmetric.importflow.FieldKey

enum class SharedMessageKey {
    APP_LOADING,
    IMPORT_ACTION,
    IMPORT_IDLE,
    IMPORT_SUPPORTED_EXPECTATION,
    IMPORT_IN_PROGRESS,
    IMPORT_REVIEW_READY,
    IMPORT_UNSUPPORTED,
    IMPORT_SOURCE_FAILED,
    IMPORT_STORAGE_FAILED,
    IMPORT_OCR_FAILED,
    HISTORY_EMPTY,
    DASHBOARD_EMPTY,
    MISSING_SCREENSHOT_PREVIEW,
    REVIEW_BLOCKED_SAVE,
    REVIEW_SAVE_FAILED,
    REVIEW_SAVE_SUCCESS,
    REVIEW_NEEDS_ATTENTION
}

data class SharedFieldCopy(
    val key: FieldKey,
    val label: String,
    val required: Boolean
)

data class SharedStateCopy(
    val text: String,
    val blocking: Boolean = false,
    val suggestsRetry: Boolean = false
)

object SharedUxCopy {
    val fieldCopy: Map<FieldKey, SharedFieldCopy> = FieldKey.entries.associateWith { key ->
        SharedFieldCopy(
            key = key,
            label = when (key) {
                FieldKey.RESULT -> "Result"
                FieldKey.HERO -> "Hero"
                FieldKey.PLAYER_NAME -> "Player"
                FieldKey.LANE -> "Lane"
                FieldKey.SCORE -> "Score"
                FieldKey.KDA -> "KDA Ratio"
                FieldKey.DAMAGE_DEALT -> "Damage Dealt"
                FieldKey.DAMAGE_SHARE -> "Damage Share"
                FieldKey.DAMAGE_TAKEN -> "Damage Taken"
                FieldKey.DAMAGE_TAKEN_SHARE -> "Damage Taken Share"
                FieldKey.TOTAL_GOLD -> "Total Gold"
                FieldKey.GOLD_SHARE -> "Gold Share"
                FieldKey.PARTICIPATION_RATE -> "Team Participation"
                FieldKey.GOLD_FROM_FARMING -> "Farming Gold"
                FieldKey.LAST_HITS -> "Last Hits"
                FieldKey.KILL_PARTICIPATION_COUNT -> "Kill Participation Count"
                FieldKey.CONTROL_DURATION -> "Control Duration"
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS -> "Damage to Opponents"
            },
            required = key.required
        )
    }

    val stateCopy: Map<SharedMessageKey, SharedStateCopy> = mapOf(
        SharedMessageKey.APP_LOADING to SharedStateCopy("Loading your matches..."),
        SharedMessageKey.IMPORT_ACTION to SharedStateCopy("Import Screenshot"),
        SharedMessageKey.IMPORT_IDLE to SharedStateCopy("Select one supported screenshot to start review."),
        SharedMessageKey.IMPORT_SUPPORTED_EXPECTATION to SharedStateCopy(
            "Supported screenshot: one Chinese post-match personal stats detailed-data screen."
        ),
        SharedMessageKey.IMPORT_IN_PROGRESS to SharedStateCopy(
            "Preparing your screenshot for review..."
        ),
        SharedMessageKey.IMPORT_REVIEW_READY to SharedStateCopy("Review your extracted match data before saving."),
        SharedMessageKey.IMPORT_UNSUPPORTED to SharedStateCopy(
            "This screenshot isn't supported. Try another post-match personal stats screenshot.",
            suggestsRetry = true
        ),
        SharedMessageKey.IMPORT_SOURCE_FAILED to SharedStateCopy(
            "The selected screenshot could not be imported. Try another image.",
            suggestsRetry = true
        ),
        SharedMessageKey.IMPORT_STORAGE_FAILED to SharedStateCopy(
            "The screenshot could not be saved locally. Try again.",
            suggestsRetry = true
        ),
        SharedMessageKey.IMPORT_OCR_FAILED to SharedStateCopy(
            "We couldn't read match data from this screenshot. Try another supported screenshot.",
            suggestsRetry = true
        ),
        SharedMessageKey.HISTORY_EMPTY to SharedStateCopy(
            "No saved matches yet. Save a reviewed match to see it here."
        ),
        SharedMessageKey.DASHBOARD_EMPTY to SharedStateCopy(
            "No saved metrics yet. Save a reviewed match to see them here."
        ),
        SharedMessageKey.MISSING_SCREENSHOT_PREVIEW to SharedStateCopy(
            "Screenshot preview unavailable. Match data is still available below."
        ),
        SharedMessageKey.REVIEW_BLOCKED_SAVE to SharedStateCopy(
            "Complete the required fields before saving.",
            blocking = true
        ),
        SharedMessageKey.REVIEW_SAVE_FAILED to SharedStateCopy(
            "Could not save this match locally. Try again.",
            suggestsRetry = true
        ),
        SharedMessageKey.REVIEW_SAVE_SUCCESS to SharedStateCopy("Match saved."),
        SharedMessageKey.REVIEW_NEEDS_ATTENTION to SharedStateCopy("Check highlighted fields before saving.")
    )

    fun field(key: FieldKey): SharedFieldCopy = fieldCopy.getValue(key)

    fun message(key: SharedMessageKey): SharedStateCopy = stateCopy.getValue(key)

    fun blockingSummary(fields: Set<FieldKey>): String {
        return "Complete before saving: ${joinLabels(fields)}"
    }

    fun needsAttentionSummary(fields: Set<FieldKey>): String {
        return "Check before saving: ${joinLabels(fields)}"
    }

    fun labeledValue(key: FieldKey, value: String?): String {
        return "${field(key).label}: ${value ?: "-"}"
    }

    private fun joinLabels(fields: Set<FieldKey>): String {
        return fields
            .map(::field)
            .sortedBy { it.label }
            .joinToString { it.label }
    }
}
