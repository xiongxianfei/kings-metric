package com.kingsmetric.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun ReviewScreenRoute(
    viewModel: ReviewScreenViewModel,
    onSaveSucceeded: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val previewLoader = remember { AndroidPreviewBitmapLoader() }
    val previewBitmap = remember(state.screenshotPath, state.previewAvailability) {
        if (state.previewAvailability == PreviewAvailability.Available) {
            state.screenshotPath?.let { path ->
                runCatching {
                    previewLoader.load(
                        path = path,
                        maxDimensionPx = 1080
                    )
                }.getOrNull()
            }
        } else {
            null
        }
    }

    DisposableEffect(previewBitmap) {
        onDispose {
            previewBitmap?.recycle()
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == ReviewScreenStatus.Saved) {
            onSaveSucceeded()
        }
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { viewModel.confirmSave() },
                    enabled = state.canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm-save")
                ) {
                    Text("Confirm Save")
                }
            }
        }
    ) { paddingValues ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
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
                                .heightIn(max = state.layout.previewMaxHeightDp.dp),
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
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = field.inputAffordance.toKeyboardType(),
                                        imeAction = ImeAction.Done
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("field-${field.key.name}")
                                )
                                Text(if (field.required) "Required field" else "Optional field")
                                field.hint?.let { hint ->
                                    Text(hint)
                                }
                                when {
                                    field.blocking -> Text("Required before saving")
                                    field.highlighted -> Text("Needs review")
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

private fun ReviewInputAffordance.toKeyboardType(): KeyboardType {
    return when (this) {
        ReviewInputAffordance.Text -> KeyboardType.Text
        ReviewInputAffordance.Number -> KeyboardType.Number
        ReviewInputAffordance.Percentage -> KeyboardType.Number
        ReviewInputAffordance.Duration -> KeyboardType.Number
        ReviewInputAffordance.Ratio -> KeyboardType.Text
    }
}
