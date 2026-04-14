package com.kingsmetric.app

import com.kingsmetric.importflow.DraftField
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.SaveResult

enum class PreviewAvailability {
    Available,
    Unavailable
}

enum class ReviewScreenStatus {
    Reviewing,
    Saved
}

data class ReviewScreenUiState(
    val screenshotPath: String?,
    val previewAvailability: PreviewAvailability,
    val fields: Map<FieldKey, DraftField>,
    val highlightedFields: Set<FieldKey>,
    val blockingFields: Set<FieldKey>,
    val canConfirm: Boolean,
    val status: ReviewScreenStatus
)

class ReviewScreenViewModel(
    draft: DraftRecord,
    private val workflow: MatchImportWorkflow
) {
    private var currentDraft: DraftRecord = draft

    var state: ReviewScreenUiState = currentDraft.toUiState()
        private set

    fun updateField(fieldKey: FieldKey, value: String) {
        currentDraft = workflow.updateField(currentDraft, fieldKey, value)
        state = currentDraft.toUiState(status = state.status)
    }

    fun confirmSave(): SaveResult {
        val result = workflow.confirmSave(currentDraft)
        state = when (result) {
            is SaveResult.Saved -> currentDraft.toUiState(status = ReviewScreenStatus.Saved)
            is SaveResult.Blocked -> {
                currentDraft = result.draft
                result.draft.toUiState(status = ReviewScreenStatus.Reviewing)
            }
            is SaveResult.StorageFailed -> {
                currentDraft = result.draft ?: currentDraft
                currentDraft.toUiState(status = ReviewScreenStatus.Reviewing)
            }
        }
        return result
    }

    private fun DraftRecord.toUiState(
        status: ReviewScreenStatus = ReviewScreenStatus.Reviewing
    ): ReviewScreenUiState {
        val review = ReviewState.fromDraft(this)
        return ReviewScreenUiState(
            screenshotPath = screenshotPath,
            previewAvailability = if (review.screenshotAvailable) {
                PreviewAvailability.Available
            } else {
                PreviewAvailability.Unavailable
            },
            fields = fields,
            highlightedFields = review.highlightedFields,
            blockingFields = review.blockingFields,
            canConfirm = review.canConfirm,
            status = status
        )
    }
}

data class ReviewScreenModel(
    val screenshotPath: String?,
    val previewAvailability: PreviewAvailability,
    val fields: Map<FieldKey, DraftField>,
    val highlightedFields: Set<FieldKey>,
    val blockingFields: Set<FieldKey>,
    val canConfirm: Boolean
)

class ReviewScreen(
    private val viewModel: ReviewScreenViewModel
) {
    fun render(): ReviewScreenModel {
        val state = viewModel.state
        return ReviewScreenModel(
            screenshotPath = state.screenshotPath,
            previewAvailability = state.previewAvailability,
            fields = state.fields,
            highlightedFields = state.highlightedFields,
            blockingFields = state.blockingFields,
            canConfirm = state.canConfirm
        )
    }
}
