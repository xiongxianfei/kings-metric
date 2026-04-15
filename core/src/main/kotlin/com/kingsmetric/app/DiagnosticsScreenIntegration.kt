package com.kingsmetric.app

import com.kingsmetric.diagnostics.DiagnosticsEvent
import com.kingsmetric.diagnostics.DiagnosticsExport
import com.kingsmetric.diagnostics.DiagnosticsOutcome
import com.kingsmetric.diagnostics.DiagnosticsRecorder
import com.kingsmetric.diagnostics.DiagnosticsStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DiagnosticsEntryPresentation(
    val title: String,
    val stageText: String,
    val summary: String,
    val timestampText: String,
    val ocrText: String?
)

data class DiagnosticsScreenUiState(
    val notice: String,
    val entries: List<DiagnosticsEntryPresentation>,
    val emptyStateText: String?,
    val exportEnabled: Boolean,
    val exportButtonLabel: String,
    val userMessage: String?
)

class DiagnosticsExportFormatter {
    fun defaultNotice(): String {
        return "This export does not include the original screenshot or full saved match data. It may include OCR text captured during a failed recognition attempt."
    }

    fun format(export: DiagnosticsExport): String {
        val lines = mutableListOf<String>()
        lines += "Kings Metric Diagnostics"
        lines += "Exported At: ${export.exportedAtMillis}"
        lines += "Notice: ${export.notice}"
        lines += ""
        if (export.entries.isEmpty()) {
            lines += "No diagnostics captured yet."
        } else {
            export.entries.forEach { entry ->
                lines += "- ${outcomeLabel(entry.outcome)}"
                lines += "  Stage: ${stageLabel(entry.stage)}"
                lines += "  Time: ${entry.timestampMillis}"
                lines += "  Summary: ${entry.summary}"
                if (entry.metadata.isNotEmpty()) {
                    entry.metadata.toSortedMap().forEach { (key, value) ->
                        if (key == "ocrText") {
                            lines += "  ocrText:"
                            value.lines().forEach { line ->
                                lines += "    $line"
                            }
                        } else {
                            lines += "  $key: $value"
                        }
                    }
                }
                lines += ""
            }
        }
        return lines.joinToString("\n").trimEnd()
    }
}

class DiagnosticsScreenViewModel(
    private val recorder: DiagnosticsRecorder,
    private val formatter: DiagnosticsExportFormatter = DiagnosticsExportFormatter()
) {
    private val _state = MutableStateFlow(buildState())
    val state: StateFlow<DiagnosticsScreenUiState> = _state.asStateFlow()

    fun refresh() {
        _state.value = buildState(userMessage = _state.value.userMessage)
    }

    fun export(copyDiagnosticsText: (String) -> Boolean) {
        val exportText = runCatching { formatter.format(recorder.export()) }.getOrNull()
        if (exportText == null) {
            _state.value = buildState(
                userMessage = "Couldn't export diagnostics. Try again."
            )
            return
        }

        val copied = runCatching { copyDiagnosticsText(exportText) }.getOrDefault(false)
        _state.value = buildState(
            userMessage = if (copied) {
                "Diagnostics copied. Paste them into your support message."
            } else {
                "Couldn't export diagnostics. Try again."
            }
        )
    }

    private fun buildState(userMessage: String? = null): DiagnosticsScreenUiState {
        val entries = recorder.snapshot()
            .asReversed()
            .map { it.toPresentation() }
        return DiagnosticsScreenUiState(
            notice = formatter.defaultNotice(),
            entries = entries,
            emptyStateText = if (entries.isEmpty()) "No diagnostics captured yet." else null,
            exportEnabled = entries.isNotEmpty(),
            exportButtonLabel = "Copy Diagnostics",
            userMessage = userMessage
        )
    }

    private fun DiagnosticsEvent.toPresentation(): DiagnosticsEntryPresentation {
        val detail = metadata["detail"]?.takeIf { it.isNotBlank() }
        return DiagnosticsEntryPresentation(
            title = outcomeLabel(outcome),
            stageText = stageLabel(stage),
            summary = if (detail == null) summary else "$summary\nReason: $detail",
            timestampText = "Time: $timestampMillis",
            ocrText = metadata["ocrText"]?.takeIf { it.isNotBlank() }
        )
    }
}

private fun stageLabel(stage: DiagnosticsStage): String {
    return when (stage) {
        DiagnosticsStage.IMPORT -> "Import"
        DiagnosticsStage.RECOGNITION -> "Recognition"
        DiagnosticsStage.REVIEW -> "Review"
        DiagnosticsStage.SAVE -> "Save"
        DiagnosticsStage.HISTORY -> "History"
        DiagnosticsStage.DETAIL -> "Detail"
        DiagnosticsStage.DASHBOARD -> "Dashboard"
    }
}

private fun outcomeLabel(outcome: DiagnosticsOutcome): String {
    return when (outcome) {
        DiagnosticsOutcome.IMPORT_SOURCE_FAILED -> "Import Source Failed"
        DiagnosticsOutcome.IMPORT_STORAGE_FAILED -> "Import Storage Failed"
        DiagnosticsOutcome.UNSUPPORTED_SCREENSHOT -> "Unsupported Screenshot"
        DiagnosticsOutcome.RECOGNITION_FAILED -> "Recognition Failed"
        DiagnosticsOutcome.REVIEW_BLOCKED -> "Review Blocked"
        DiagnosticsOutcome.SAVE_FAILED -> "Save Failed"
        DiagnosticsOutcome.SAVE_SUCCEEDED -> "Save Succeeded"
    }
}
