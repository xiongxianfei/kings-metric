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
    val layout: ReviewLayoutState,
    val fields: Map<FieldKey, DraftField>,
    val sections: List<ReviewSectionPresentation>,
    val highlightedFields: Set<FieldKey>,
    val blockingFields: Set<FieldKey>,
    val blockerSummary: ReviewBlockerSummary?,
    val attentionSummary: String?,
    val canConfirm: Boolean,
    val status: ReviewScreenStatus,
    val userMessage: String? = null
)

class ReviewScreenViewModel(
    draft: DraftRecord,
    private val workflow: MatchImportWorkflow,
    private val groupingMapper: ReviewFieldGroupingMapper = ReviewFieldGroupingMapper(),
    private val onDraftChanged: (DraftRecord?) -> Unit = {},
    private val previewAvailableResolver: (String?) -> Boolean = { screenshotPath ->
        screenshotPath != null
    }
) {
    private var currentDraft: DraftRecord = draft
    private val _state = MutableStateFlow(currentDraft.toUiState())
    val state: StateFlow<ReviewScreenUiState> = _state.asStateFlow()

    fun updateField(fieldKey: FieldKey, value: String) {
        currentDraft = workflow.updateField(currentDraft, fieldKey, value)
        onDraftChanged(currentDraft)
        _state.value = currentDraft.toUiState(status = _state.value.status)
    }

    fun confirmSave(): SaveResult {
        val result = workflow.confirmSave(currentDraft)
        _state.value = when (result) {
            is SaveResult.Saved -> currentDraft.toUiState(status = ReviewScreenStatus.Saved)
            is SaveResult.Blocked -> {
                currentDraft = result.draft
                onDraftChanged(currentDraft)
                result.draft.toUiState(
                    status = ReviewScreenStatus.Reviewing,
                    userMessage = SharedUxCopy.message(SharedMessageKey.REVIEW_BLOCKED_SAVE).text
                )
            }
            is SaveResult.StorageFailed -> {
                currentDraft = result.draft ?: currentDraft
                onDraftChanged(currentDraft)
                currentDraft.toUiState(
                    status = ReviewScreenStatus.Reviewing,
                    userMessage = SharedUxCopy.message(SharedMessageKey.REVIEW_SAVE_FAILED).text
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
        val grouping = groupingMapper.map(review)
        val previewAvailability = if (previewAvailableResolver(screenshotPath) && review.screenshotAvailable) {
            PreviewAvailability.Available
        } else {
            PreviewAvailability.Unavailable
        }
        return ReviewScreenUiState(
            screenshotPath = screenshotPath,
            previewAvailability = previewAvailability,
            layout = groupingMapper.layoutFor(
                previewAvailability = previewAvailability,
                hasBlockerSummary = grouping.blockerSummary != null
            ),
            fields = fields,
            sections = grouping.sections,
            highlightedFields = review.highlightedFields,
            blockingFields = review.blockingFields,
            blockerSummary = grouping.blockerSummary,
            attentionSummary = grouping.attentionSummary,
            canConfirm = grouping.canConfirm,
            status = status,
            userMessage = userMessage
        )
    }
}

data class ReviewScreenModel(
    val screenshotPath: String?,
    val previewAvailability: PreviewAvailability,
    val layout: ReviewLayoutState,
    val fields: Map<FieldKey, DraftField>,
    val sections: List<ReviewSectionPresentation>,
    val highlightedFields: Set<FieldKey>,
    val blockingFields: Set<FieldKey>,
    val blockerSummary: ReviewBlockerSummary?,
    val attentionSummary: String?,
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
            layout = state.layout,
            fields = state.fields,
            sections = state.sections,
            highlightedFields = state.highlightedFields,
            blockingFields = state.blockingFields,
            blockerSummary = state.blockerSummary,
            attentionSummary = state.attentionSummary,
            canConfirm = state.canConfirm
        )
    }
}
