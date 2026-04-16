package com.kingsmetric.app

import com.kingsmetric.importflow.DraftRecord

data class ImportScreenUiModel(
    val title: String,
    val supportedScreenshotHint: String,
    val guidance: String,
    val nextStepText: String,
    val primaryActionLabel: String,
    val showImportAction: Boolean,
    val showContinueReview: Boolean,
    val continueReviewLabel: String = "Continue Review",
    val reviewDraft: DraftRecord? = null
)

class ImportScreenUiStateMapper {

    fun map(status: ImportRuntimeStatus): ImportScreenUiModel {
        return when (status) {
            ImportRuntimeStatus.Idle -> baseModel(
                title = "Import Screenshot",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_IDLE).text,
                nextStepText = "Next: choose one supported screenshot to start review."
            )
            ImportRuntimeStatus.InProgress -> baseModel(
                title = "Preparing Screenshot",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_IN_PROGRESS).text,
                nextStepText = "Next: wait while the screenshot is prepared for review."
            )
            is ImportRuntimeStatus.Unsupported -> baseModel(
                title = "Unsupported Screenshot",
                guidance = status.message,
                nextStepText = "Next: try another supported post-match personal stats screenshot."
            )
            is ImportRuntimeStatus.SourceFailed -> baseModel(
                title = "Can't Read Selected Screenshot",
                guidance = status.message,
                nextStepText = "Next: try another image from your device."
            )
            is ImportRuntimeStatus.StorageFailed -> baseModel(
                title = "Couldn't Save Screenshot",
                guidance = status.message,
                nextStepText = "Next: try the import again."
            )
            is ImportRuntimeStatus.Failed -> baseModel(
                title = "Couldn't Prepare Review",
                guidance = status.message,
                nextStepText = "Next: try another supported screenshot."
            )
            is ImportRuntimeStatus.ReviewReady -> baseModel(
                title = "Review Ready",
                guidance = SharedUxCopy.message(SharedMessageKey.IMPORT_REVIEW_READY).text,
                nextStepText = "Next: continue into review to verify and save the match.",
                showImportAction = false,
                showContinueReview = true,
                reviewDraft = status.draft
            )
        }
    }

    private fun baseModel(
        title: String,
        guidance: String,
        nextStepText: String,
        showImportAction: Boolean = true,
        showContinueReview: Boolean = false,
        reviewDraft: DraftRecord? = null
    ): ImportScreenUiModel {
        return ImportScreenUiModel(
            title = title,
            supportedScreenshotHint = SharedUxCopy.message(SharedMessageKey.IMPORT_SUPPORTED_EXPECTATION).text,
            guidance = guidance,
            nextStepText = nextStepText,
            primaryActionLabel = SharedUxCopy.message(SharedMessageKey.IMPORT_ACTION).text,
            showImportAction = showImportAction,
            showContinueReview = showContinueReview,
            reviewDraft = reviewDraft
        )
    }
}
