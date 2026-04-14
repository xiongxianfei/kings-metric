package com.kingsmetric.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.previewAvailability == PreviewAvailability.Available) {
            Text("Screenshot preview")
            previewBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Screenshot preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
            state.screenshotPath?.let { path ->
                Text(path)
            }
        } else {
            Text(SharedUxCopy.message(SharedMessageKey.MISSING_SCREENSHOT_PREVIEW).text)
        }

        state.userMessage?.let { message ->
            Text(message)
        }

        state.blockerSummary?.let { summary ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(summary.message)
                    Text("Review next: ${summary.sectionsToVisit.joinToString()}")
                    Text("Required fields: ${summary.fieldLabels.joinToString()}")
                }
            }
        }

        state.attentionSummary?.let { summary ->
            Text(summary)
        }

        Button(
            onClick = { viewModel.confirmSave() },
            enabled = state.canConfirm,
            modifier = Modifier.testTag("confirm-save")
        ) {
            Text("Confirm Save")
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            state.sections.forEach { section ->
                Column(
                    modifier = Modifier.testTag("review-section"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(section.title)
                    when {
                        section.hasBlockingFields -> Text("Contains required updates")
                        section.hasHighlightedFields -> Text("Contains fields to check")
                    }
                    section.fields.forEach { field ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = field.value.orEmpty(),
                                onValueChange = { newValue ->
                                    viewModel.updateField(field.key, newValue)
                                },
                                label = { Text(field.label) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("field-${field.key.name}")
                            )
                            Text(if (field.required) "Required field" else "Optional field")
                            when {
                                field.blocking -> Text("Required before saving")
                                field.highlighted -> Text("Needs review")
                            }
                        }
                    }
                }
            }
        }
    }
}
