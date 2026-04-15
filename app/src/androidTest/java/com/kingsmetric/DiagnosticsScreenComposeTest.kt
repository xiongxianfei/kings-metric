package com.kingsmetric

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kingsmetric.app.DiagnosticsScreenRoute
import com.kingsmetric.app.DiagnosticsScreenViewModel
import com.kingsmetric.diagnostics.DiagnosticsEvent
import com.kingsmetric.diagnostics.DiagnosticsExport
import com.kingsmetric.diagnostics.DiagnosticsOutcome
import com.kingsmetric.diagnostics.DiagnosticsRecorder
import com.kingsmetric.diagnostics.DiagnosticsStage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DiagnosticsScreenComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun diagnostics_screen_empty_state_is_explicit_and_copy_disabled() {
        composeRule.setContent {
            DiagnosticsScreenRoute(
                viewModel = DiagnosticsScreenViewModel(DiagnosticsTestRecorder())
            )
        }

        composeRule.onNodeWithTag("diagnostics-empty-state").assertIsDisplayed()
        composeRule.onNodeWithTag("copy-diagnostics").assertIsNotEnabled()
    }

    @Test
    fun diagnostics_screen_with_entries_shows_rows_and_copy_success_message() {
        val recorder = DiagnosticsTestRecorder().apply {
            record(
                stage = DiagnosticsStage.SAVE,
                outcome = DiagnosticsOutcome.SAVE_FAILED,
                summary = "Could not save record locally."
            )
        }
        var copiedText: String? = null

        composeRule.setContent {
            DiagnosticsScreenRoute(
                viewModel = DiagnosticsScreenViewModel(recorder),
                copyDiagnosticsText = { text ->
                    copiedText = text
                    true
                }
            )
        }

        composeRule.onAllNodesWithTag("diagnostics-entry-0").assertCountEquals(1)
        composeRule.onNodeWithText("Save Failed").assertIsDisplayed()
        composeRule.onNodeWithTag("copy-diagnostics").assertIsEnabled()
        composeRule.onNodeWithTag("copy-diagnostics").performClick()
        composeRule.onNodeWithText("Diagnostics copied. Paste them into your support message.").assertIsDisplayed()
        assertTrue(copiedText?.contains("Save Failed") == true)
    }

    @Test
    fun diagnostics_screen_shows_ocr_text_when_present() {
        val recorder = DiagnosticsTestRecorder().apply {
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

        composeRule.setContent {
            DiagnosticsScreenRoute(
                viewModel = DiagnosticsScreenViewModel(recorder)
            )
        }

        composeRule.onNodeWithText("OCR Text:\n胜利\n数据 复盘\n对英雄出: 171.2k").assertIsDisplayed()
    }
}

private class DiagnosticsTestRecorder : DiagnosticsRecorder {
    private val now = mutableListOf<DiagnosticsEvent>()

    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) {
        now += DiagnosticsEvent(
            timestampMillis = now.size.toLong(),
            stage = stage,
            outcome = outcome,
            summary = summary,
            metadata = metadata
        )
    }

    override fun snapshot(): List<DiagnosticsEvent> = now.toList()

    override fun export(): DiagnosticsExport {
        return DiagnosticsExport(
            exportedAtMillis = 1L,
            notice = "This export does not include the original screenshot or full saved match data. It may include OCR text captured during a failed recognition attempt.",
            entries = now.map {
                com.kingsmetric.diagnostics.DiagnosticsExportEntry(
                    timestampMillis = it.timestampMillis,
                    stage = it.stage,
                    outcome = it.outcome,
                    summary = it.summary,
                    metadata = it.metadata
                )
            }
        )
    }
}
