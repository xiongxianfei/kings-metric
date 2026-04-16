package com.kingsmetric.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kingsmetric.ui.theme.AppShellVisualFoundation

@Composable
fun ShellSurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.withOptionalTag(testTag),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
fun ShellStateBlock(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    testTag: String = AppShellVisualFoundation.shared.stateBlockStyleKey,
    supportingContent: @Composable ColumnScope.() -> Unit = {}
) {
    ShellSurfaceCard(
        modifier = modifier,
        testTag = testTag,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
        supportingContent()
    }
}

@Composable
fun ShellPrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonTag: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AppShellVisualFoundation.shared.primaryActionStyleKey)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .withOptionalTag(buttonTag),
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun Modifier.withOptionalTag(testTag: String?): Modifier {
    return if (testTag == null) {
        this
    } else {
        this.then(Modifier.testTag(testTag))
    }
}
