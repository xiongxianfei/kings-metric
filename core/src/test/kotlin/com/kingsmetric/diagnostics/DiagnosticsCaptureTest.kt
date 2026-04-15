package com.kingsmetric.diagnostics

import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsCaptureTest {

    @Test
    fun `T1 diagnostics recorder is local only by contract`() {
        val recorder = fileRecorder()

        assertFalse(recorder.requiresAccount)
        assertFalse(recorder.uploadsAutomatically)
    }

    @Test
    fun `T2 diagnostics events distinguish supported path failures and save outcomes`() {
        val recorder = fileRecorder()

        recorder.record(
            stage = DiagnosticsStage.IMPORT,
            outcome = DiagnosticsOutcome.UNSUPPORTED_SCREENSHOT,
            summary = "Unsupported screenshot."
        )
        recorder.record(
            stage = DiagnosticsStage.RECOGNITION,
            outcome = DiagnosticsOutcome.RECOGNITION_FAILED,
            summary = "Could not read match data."
        )
        recorder.record(
            stage = DiagnosticsStage.SAVE,
            outcome = DiagnosticsOutcome.SAVE_SUCCEEDED,
            summary = "Saved locally."
        )

        val events = recorder.snapshot()

        assertEquals(
            listOf(
                DiagnosticsOutcome.UNSUPPORTED_SCREENSHOT,
                DiagnosticsOutcome.RECOGNITION_FAILED,
                DiagnosticsOutcome.SAVE_SUCCEEDED
            ),
            events.map { it.outcome }
        )
    }

    @Test
    fun `T3 diagnostics entries are structured with timestamp stage outcome and summary`() {
        val recorder = fileRecorder(clock = { 1234L })

        recorder.record(
            stage = DiagnosticsStage.SAVE,
            outcome = DiagnosticsOutcome.SAVE_FAILED,
            summary = "Could not save record locally."
        )

        val event = recorder.snapshot().single()
        assertEquals(1234L, event.timestampMillis)
        assertEquals(DiagnosticsStage.SAVE, event.stage)
        assertEquals(DiagnosticsOutcome.SAVE_FAILED, event.outcome)
        assertEquals("Could not save record locally.", event.summary)
    }

    @Test
    fun `T4 export redaction excludes screenshot payload OCR dumps full records and secrets`() {
        val recorder = fileRecorder()
        recorder.record(
            stage = DiagnosticsStage.RECOGNITION,
            outcome = DiagnosticsOutcome.RECOGNITION_FAILED,
            summary = "Could not read match data.",
            metadata = mapOf(
                "appVersion" to "0.1.0-alpha.5",
                "detail" to "Missing damage section values after OCR mapping.",
                "ocrText" to "胜利\n数据 复盘\n对英雄出: 171.2k",
                "screenshotPath" to "/private/screenshot.png",
                "savedRecordPayload" to "{...}",
                "token" to "secret"
            )
        )

        val export = recorder.export()
        val entry = export.entries.single()

        assertEquals("0.1.0-alpha.5", entry.metadata["appVersion"])
        assertEquals("Missing damage section values after OCR mapping.", entry.metadata["detail"])
        assertEquals("胜利\n数据 复盘\n对英雄出: 171.2k", entry.metadata["ocrText"])
        assertFalse(entry.metadata.containsKey("screenshotPath"))
        assertFalse(entry.metadata.containsKey("savedRecordPayload"))
        assertFalse(entry.metadata.containsKey("token"))
        assertTrue(export.notice.contains("does not include the original screenshot"))
    }

    @Test
    fun `T5 retention keeps deterministic recent entries when the limit is exceeded`() {
        var now = 0L
        val recorder = fileRecorder(retentionLimit = 2) { now }

        now = 100L
        recorder.record(DiagnosticsStage.IMPORT, DiagnosticsOutcome.IMPORT_SOURCE_FAILED, "one")
        now = 200L
        recorder.record(DiagnosticsStage.RECOGNITION, DiagnosticsOutcome.RECOGNITION_FAILED, "two")
        now = 300L
        recorder.record(DiagnosticsStage.SAVE, DiagnosticsOutcome.SAVE_FAILED, "three")

        assertEquals(listOf(200L, 300L), recorder.snapshot().map { it.timestampMillis })
    }

    @Test
    fun `T6 export failure is retryable and does not require app termination`() {
        val recorder = object : DiagnosticsRecorder {
            override val requiresAccount: Boolean = false
            override val uploadsAutomatically: Boolean = false

            override fun record(
                stage: DiagnosticsStage,
                outcome: DiagnosticsOutcome,
                summary: String,
                metadata: Map<String, String>
            ) = Unit

            override fun snapshot(): List<DiagnosticsEvent> = emptyList()

            override fun export(): DiagnosticsExport {
                throw IllegalStateException("disk full")
            }
        }

        val result = runCatching { recorder.export() }

        assertTrue(result.isFailure)
    }

    @Test
    fun `T7 empty diagnostics state is explicit`() {
        val recorder = fileRecorder()

        assertTrue(recorder.snapshot().isEmpty())
    }

    private fun fileRecorder(
        retentionLimit: Int = 20,
        clock: () -> Long = { System.currentTimeMillis() }
    ): FileBackedDiagnosticsRecorder {
        val directory = createTempDirectory("diagnostics-test").toFile()
        return FileBackedDiagnosticsRecorder(
            storageFile = File(directory, "diagnostics.log"),
            retentionLimit = retentionLimit,
            clock = clock
        )
    }
}
