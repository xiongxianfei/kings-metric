package com.kingsmetric.app

import com.kingsmetric.diagnostics.DiagnosticsOutcome
import com.kingsmetric.diagnostics.DiagnosticsRecorder
import com.kingsmetric.diagnostics.DiagnosticsStage
import com.kingsmetric.diagnostics.DiagnosticsEvent
import com.kingsmetric.diagnostics.DiagnosticsExport
import com.kingsmetric.diagnostics.DiagnosticsExportEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsScreenIntegrationTest {

    @Test
    fun `T1 diagnostics screen exposes explicit empty state and disables export`() {
        val viewModel = DiagnosticsScreenViewModel(TestDiagnosticsRecorder())

        val state = viewModel.state.value

        assertEquals("No diagnostics captured yet.", state.emptyStateText)
        assertFalse(state.exportEnabled)
        assertEquals("Copy Diagnostics", state.exportButtonLabel)
    }

    @Test
    fun `T2 diagnostics screen shows newest entries first with readable labels`() {
        val recorder = TestDiagnosticsRecorder().apply {
            record(
                stage = DiagnosticsStage.IMPORT,
                outcome = DiagnosticsOutcome.UNSUPPORTED_SCREENSHOT,
                summary = "Unsupported screenshot."
            )
            record(
                stage = DiagnosticsStage.SAVE,
                outcome = DiagnosticsOutcome.SAVE_FAILED,
                summary = "Could not save record locally."
            )
        }
        val viewModel = DiagnosticsScreenViewModel(recorder)

        val state = viewModel.state.value

        assertEquals(2, state.entries.size)
        assertEquals("Save Failed", state.entries.first().title)
        assertEquals("Save", state.entries.first().stageText)
        assertTrue(state.entries.first().summary.contains("Could not save record locally."))
        assertEquals("Unsupported Screenshot", state.entries.last().title)
        assertTrue(state.exportEnabled)
    }

    @Test
    fun `T3 export formatter produces bounded readable diagnostics text`() {
        val recorder = TestDiagnosticsRecorder().apply {
            record(
                stage = DiagnosticsStage.RECOGNITION,
                outcome = DiagnosticsOutcome.RECOGNITION_FAILED,
                summary = "Could not read match data.",
                metadata = mapOf(
                    "appVersion" to "0.1.0-alpha.5",
                    "surface" to "import",
                    "detail" to "Missing damage section values after OCR mapping.",
                    "ocrText" to "胜利\n数据 复盘\n对英雄出: 171.2k"
                )
            )
        }
        val viewModel = DiagnosticsScreenViewModel(recorder)
        var exportedText: String? = null

        viewModel.export { text ->
            exportedText = text
            true
        }

        requireNotNull(exportedText)
        assertTrue(exportedText!!.contains("Kings Metric Diagnostics"))
        assertTrue(exportedText!!.contains("Recognition Failed"))
        assertTrue(exportedText!!.contains("appVersion: 0.1.0-alpha.5"))
        assertTrue(exportedText!!.contains("detail: Missing damage section values after OCR mapping."))
        assertTrue(exportedText!!.contains("ocrText:"))
        assertTrue(exportedText!!.contains("对英雄出: 171.2k"))
    }

    @Test
    fun `T3b diagnostics viewer includes bounded detail when present`() {
        val recorder = TestDiagnosticsRecorder().apply {
            record(
                stage = DiagnosticsStage.RECOGNITION,
                outcome = DiagnosticsOutcome.RECOGNITION_FAILED,
                summary = "Could not read match data.",
                metadata = mapOf("detail" to "Missing damage section values after OCR mapping.")
            )
        }
        val viewModel = DiagnosticsScreenViewModel(recorder)

        val state = viewModel.state.value

        assertTrue(state.entries.single().summary.contains("Reason: Missing damage section values after OCR mapping."))
    }

    @Test
    fun `T3c diagnostics viewer includes OCR text when present`() {
        val recorder = TestDiagnosticsRecorder().apply {
            record(
                stage = DiagnosticsStage.RECOGNITION,
                outcome = DiagnosticsOutcome.RECOGNITION_FAILED,
                summary = "Could not read match data.",
                metadata = mapOf(
                    "detail" to "Missing damage section values after OCR mapping.",
                    "ocrText" to "胜利\n数据 复盘\n对英雄出: 171.2k"
                )
            )
        }
        val viewModel = DiagnosticsScreenViewModel(recorder)

        val state = viewModel.state.value

        assertEquals("胜利\n数据 复盘\n对英雄出: 171.2k", state.entries.single().ocrText)
    }

    @Test
    fun `T4 successful copy updates user message`() {
        val recorder = TestDiagnosticsRecorder().apply {
            record(
                stage = DiagnosticsStage.SAVE,
                outcome = DiagnosticsOutcome.SAVE_SUCCEEDED,
                summary = "Saved locally."
            )
        }
        val viewModel = DiagnosticsScreenViewModel(recorder)

        viewModel.export { true }

        assertEquals(
            "Diagnostics copied. Paste them into your support message.",
            viewModel.state.value.userMessage
        )
    }

    @Test
    fun `T5 failed export updates retryable message`() {
        val recorder = ThrowingDiagnosticsRecorder()
        val viewModel = DiagnosticsScreenViewModel(recorder)

        viewModel.export { true }

        assertEquals(
            "Couldn't export diagnostics. Try again.",
            viewModel.state.value.userMessage
        )
    }

    @Test
    fun `IT5 refresh reflects newly captured diagnostics entries`() {
        val recorder = TestDiagnosticsRecorder()
        val viewModel = DiagnosticsScreenViewModel(recorder)

        recorder.record(
            stage = DiagnosticsStage.IMPORT,
            outcome = DiagnosticsOutcome.IMPORT_SOURCE_FAILED,
            summary = "The selected screenshot could not be imported."
        )
        viewModel.refresh()

        val state = viewModel.state.value
        assertEquals(1, state.entries.size)
        assertEquals("Import Source Failed", state.entries.single().title)
        assertNotNull(state.notice)
    }
}

private class TestDiagnosticsRecorder : DiagnosticsRecorder {
    private val events = mutableListOf<DiagnosticsEvent>()

    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) {
        events += DiagnosticsEvent(
            timestampMillis = events.size.toLong(),
            stage = stage,
            outcome = outcome,
            summary = summary,
            metadata = metadata
        )
    }

    override fun snapshot(): List<DiagnosticsEvent> = events.toList()

    override fun export(): DiagnosticsExport {
        return DiagnosticsExport(
            exportedAtMillis = 999L,
            notice = "This export does not include the original screenshot, raw OCR text, or full saved match data.",
            entries = events.map {
                DiagnosticsExportEntry(
                    timestampMillis = it.timestampMillis,
                    stage = it.stage,
                    outcome = it.outcome,
                    summary = it.summary,
                    metadata = it.metadata.filterKeys { key ->
                        key == "appVersion" || key == "surface" || key == "buildType" || key == "detail" || key == "ocrText"
                    }
                )
            }
        )
    }
}
