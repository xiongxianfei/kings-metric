package com.kingsmetric.app

import com.kingsmetric.importflow.DraftField
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.SaveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val status: ReviewScreenStatus,
    val userMessage: String? = null
)

class ReviewScreenViewModel(
    draft: DraftRecord,
    private val workflow: MatchImportWorkflow,
    private val previewAvailableResolver: (String?) -> Boolean = { screenshotPath ->
        screenshotPath != null
    }
) {
    private var currentDraft: DraftRecord = draft
    private val _state = MutableStateFlow(currentDraft.toUiState())
    val state: StateFlow<ReviewScreenUiState> = _state.asStateFlow()

    fun updateField(fieldKey: FieldKey, value: String) {
        currentDraft = workflow.updateField(currentDraft, fieldKey, value)
        _state.value = currentDraft.toUiState(status = _state.value.status)
    }

    fun confirmSave(): SaveResult {
        val result = workflow.confirmSave(currentDraft)
        _state.value = when (result) {
            is SaveResult.Saved -> currentDraft.toUiState(status = ReviewScreenStatus.Saved)
            is SaveResult.Blocked -> {
                currentDraft = result.draft
                result.draft.toUiState(
                    status = ReviewScreenStatus.Reviewing,
                    userMessage = result.reason
                )
            }
            is SaveResult.StorageFailed -> {
                currentDraft = result.draft ?: currentDraft
                currentDraft.toUiState(
                    status = ReviewScreenStatus.Reviewing,
                    userMessage = result.message
                )
            }
        }
        return result
    }

    private fun DraftRecord.toUiState(
        status: ReviewScreenStatus = ReviewScreenStatus.Reviewing,
        userMessage: String? = null
    ): ReviewScreenUiState {
        val review = ReviewState.fromDraft(this)
        return ReviewScreenUiState(
            screenshotPath = screenshotPath,
            previewAvailability = if (previewAvailableResolver(screenshotPath) && review.screenshotAvailable) {
                PreviewAvailability.Available
            } else {
                PreviewAvailability.Unavailable
            },
            fields = fields,
            highlightedFields = review.highlightedFields,
            blockingFields = review.blockingFields,
            canConfirm = review.canConfirm,
            status = status,
            userMessage = userMessage
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
        val state = viewModel.state.value
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
