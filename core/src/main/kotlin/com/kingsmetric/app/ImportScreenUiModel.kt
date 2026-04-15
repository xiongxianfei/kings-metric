package com.kingsmetric.app

import com.kingsmetric.importflow.DraftRecord

data class ImportScreenUiModel(
    val title: String,
    val supportedScreenshotHint: String,
    val guidance: String,
    val primaryActionLabel: String,
    val showContinueReview: Boolean,
    val continueReviewLabel: String = "Continue Review",
    val reviewDraft: DraftRecord? = null
)

class ImportScreenUiStateMapper {

    fun map(status: ImportRuntimeStatus): ImportScreenUiModel {
        return when (status) {
            ImportRuntimeStatus.Idle -> baseModel(
                title = "Import Screenshot",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_IDLE).text
            )
            ImportRuntimeStatus.InProgress -> baseModel(
                title = "Preparing Screenshot",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_IN_PROGRESS).text
            )
            is ImportRuntimeStatus.Unsupported -> baseModel(
                title = "Unsupported Screenshot",
                guidance = status.message
            )
            is ImportRuntimeStatus.SourceFailed -> baseModel(
                title = "Can't Read Selected Screenshot",
                guidance = status.message
            )
            is ImportRuntimeStatus.StorageFailed -> baseModel(
                title = "Couldn't Save Screenshot",
                guidance = status.message
            )
            is ImportRuntimeStatus.Failed -> baseModel(
                title = "Couldn't Prepare Review",
                guidance = status.message
            )
            is ImportRuntimeStatus.ReviewReady -> baseModel(
                title = "Review Ready",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_REVIEW_READY).text,
                showContinueReview = true,
                reviewDraft = status.draft
            )
        }
    }

    private fun baseModel(
        title: String,
        guidance: String,
        showContinueReview: Boolean = false,
        reviewDraft: DraftRecord? = null
    ): ImportScreenUiModel {
        return ImportScreenUiModel(
            title = title,
            supportedScreenshotHint = SharedUxCopy.message(SharedMessageKey.IMPORT_SUPPORTED_EXPECTATION).text,
            guidance = guidance,
            primaryActionLabel = SharedUxCopy.message(SharedMessageKey.IMPORT_ACTION).text,
            showContinueReview = showContinueReview,
            reviewDraft = reviewDraft
        )
    }
}
