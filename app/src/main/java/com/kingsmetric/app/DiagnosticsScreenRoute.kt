package com.kingsmetric.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.kingsmetric.ui.components.ShellPrimaryActionButton
import com.kingsmetric.ui.components.ShellStateBlock
import com.kingsmetric.ui.components.ShellSurfaceCard

@Composable
fun DiagnosticsScreenRoute(
    viewModel: DiagnosticsScreenViewModel,
    copyDiagnosticsText: ((String) -> Boolean)? = null
) {
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val copyAction = copyDiagnosticsText ?: { text: String ->
        clipboardManager.setText(AnnotatedString(text))
        true
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShellSurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "diagnostics-support-card"
        ) {
            Text("Support details", style = MaterialTheme.typography.titleMedium)
            Text(state.currentVersionLabel, style = MaterialTheme.typography.labelMedium)
            Text(
                state.currentVersionValue,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag("diagnostics-current-version")
            )
            Text(
                state.notice,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        ShellSurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "diagnostics-export-card"
        ) {
            Text("Share diagnostics", style = MaterialTheme.typography.titleMedium)
            Text(
                "Copy a bounded diagnostics report to paste into your support message.",
                style = MaterialTheme.typography.bodyMedium
            )
            ShellPrimaryActionButton(
                label = state.exportButtonLabel,
                onClick = { viewModel.export(copyAction) },
                enabled = state.exportEnabled,
                buttonTag = "copy-diagnostics"
            )
            state.userMessage?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        state.emptyStateText?.let {
            ShellStateBlock(
                title = "No diagnostics yet",
                message = it,
                testTag = "diagnostics-empty-state"
            )
        }

        if (state.entries.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.entries) { index, entry ->
                    ShellSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "diagnostics-entry-$index"
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Stage: ${entry.stageText}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(entry.timestampText, style = MaterialTheme.typography.bodySmall)
                        Text(entry.summary, style = MaterialTheme.typography.bodyMedium)
                        entry.detailText?.let { detailText ->
                            Text(
                                detailText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.testTag("diagnostics-entry-detail-$index")
                            )
                        }
                        entry.surfaceText?.let { surfaceText ->
                            Text(
                                surfaceText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag("diagnostics-entry-surface-$index")
                            )
                        }
                        entry.ocrText?.let { ocrText ->
                            Text("OCR Text", style = MaterialTheme.typography.labelMedium)
                            Text(
                                ocrText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.testTag("diagnostics-entry-ocr-$index")
                            )
                        }
                    }
                }
            }
        }
    }
}
