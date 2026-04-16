package com.kingsmetric.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.kingsmetric.ui.components.ShellPrimaryActionButton
import com.kingsmetric.ui.components.ShellStateBlock
import com.kingsmetric.ui.components.ShellSurfaceCard

@Composable
fun ReviewScreenRoute(
    viewModel: ReviewScreenViewModel,
    onSaveSucceeded: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val previewLoader = remember { AndroidPreviewBitmapLoader() }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val previewTargetWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val previewTargetHeightPx = with(density) { state.layout.previewMaxHeightDp.dp.roundToPx() }
    val previewBitmap = remember(
        state.screenshotPath,
        state.previewAvailability,
        previewTargetWidthPx,
        previewTargetHeightPx
    ) {
        if (state.previewAvailability == PreviewAvailability.Available) {
            state.screenshotPath?.let { path ->
                runCatching {
                    previewLoader.load(
                        path = path,
                        targetWidthPx = previewTargetWidthPx,
                        targetHeightPx = previewTargetHeightPx
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
                ShellPrimaryActionButton(
                    label = "Confirm Save",
                    onClick = { viewModel.confirmSave() },
                    enabled = state.canConfirm,
                    buttonTag = "confirm-save"
                )
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
                    ShellSurfaceCard(testTag = "review-preview-card") {
                        Text("Screenshot preview", style = MaterialTheme.typography.titleMedium)
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
                            Text(path, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    ShellStateBlock(
                        message = SharedUxCopy.message(SharedMessageKey.MISSING_SCREENSHOT_PREVIEW).text,
                        testTag = "review-preview-missing"
                    )
                }

                state.userMessage?.let { message ->
                    ShellStateBlock(message = message)
                }

                state.blockerSummary?.let { summary ->
                    ShellStateBlock(
                        title = "Review required",
                        message = summary.message,
                        testTag = "review-blocker-card"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Review next: ${summary.sectionsToVisit.joinToString()}")
                            Text("Required fields: ${summary.fieldLabels.joinToString()}")
                        }
                    }
                }

                state.attentionSummary?.let { summary ->
                    ShellStateBlock(
                        title = "Needs review",
                        message = summary,
                        testTag = "review-attention-card"
                    )
                }

                state.sections.forEach { section ->
                    ShellSurfaceCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "review-section-card"
                    ) {
                        Column(
                            modifier = Modifier.testTag("review-section"),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(section.title, style = MaterialTheme.typography.titleMedium)
                            when {
                                section.hasBlockingFields -> Text(
                                    "Contains required updates",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                section.hasHighlightedFields -> Text(
                                    "Contains fields to check",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            section.fields.forEachIndexed { index, field ->
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
                                    Text(
                                        if (field.required) "Required field" else "Optional field",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    field.hint?.let { hint ->
                                        Text(hint, style = MaterialTheme.typography.bodySmall)
                                    }
                                    when {
                                        field.blocking -> Text(
                                            "Required before saving",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        field.highlighted -> Text(
                                            "Needs review",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                if (index != section.fields.lastIndex) {
                                    Divider()
                                }
                            }
                        }
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
