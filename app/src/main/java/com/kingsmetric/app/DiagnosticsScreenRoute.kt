package com.kingsmetric.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(state.notice)
        Text(state.currentVersionLabel, style = MaterialTheme.typography.labelMedium)
        Text(
            state.currentVersionValue,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("diagnostics-current-version")
        )
        state.userMessage?.let { Text(it) }
        Button(
            onClick = { viewModel.export(copyAction) },
            enabled = state.exportEnabled,
            modifier = Modifier.testTag("copy-diagnostics")
        ) {
            Text(state.exportButtonLabel)
        }
        state.emptyStateText?.let {
            Text(
                it,
                modifier = Modifier.testTag("diagnostics-empty-state")
            )
        }
        if (state.entries.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.entries) { index, entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("diagnostics-entry-$index")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium)
                            Text(entry.stageText, style = MaterialTheme.typography.labelMedium)
                            Text(entry.summary, style = MaterialTheme.typography.bodyMedium)
                            entry.ocrText?.let { ocrText ->
                                Text(
                                    "OCR Text:\n$ocrText",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(entry.timestampText, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
