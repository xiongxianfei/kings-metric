package com.kingsmetric.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ReviewScreenRoute(
    viewModel: ReviewScreenViewModel,
    onSaveSucceeded: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val previewBitmap = remember(state.screenshotPath, state.previewAvailability) {
        if (state.previewAvailability == PreviewAvailability.Available) {
            state.screenshotPath?.let(BitmapFactory::decodeFile)
        } else {
            null
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == ReviewScreenStatus.Saved) {
            onSaveSucceeded()
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.previewAvailability == PreviewAvailability.Available) {
            Text("Screenshot preview")
            previewBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Screenshot preview",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            state.screenshotPath?.let { path ->
                Text(path)
            }
        } else {
            Text("Screenshot preview unavailable")
        }

        state.userMessage?.let { message ->
            Text(message)
        }

        if (state.blockingFields.isNotEmpty()) {
            Text(
                "Blocking field: ${state.blockingFields.joinToString { it.name }}"
            )
        }

        val nonBlockingHighlights = state.highlightedFields - state.blockingFields
        if (nonBlockingHighlights.isNotEmpty()) {
            Text(
                "Needs review: ${nonBlockingHighlights.joinToString { it.name }}"
            )
        }

        Button(
            onClick = { viewModel.confirmSave() },
            enabled = state.canConfirm,
            modifier = Modifier.testTag("confirm-save")
        ) {
            Text("Confirm Save")
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            state.fields.values.forEach { field ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = field.value.orEmpty(),
                        onValueChange = { newValue ->
                            viewModel.updateField(field.key, newValue)
                        },
                        label = { Text(field.key.name) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field-${field.key.name}")
                    )
                    when {
                        field.key in state.blockingFields -> Text("Blocking field")
                        field.key in state.highlightedFields -> Text("Needs review")
                    }
                }
            }
        }
    }
}
