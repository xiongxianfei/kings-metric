package com.kingsmetric.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kingsmetric.app.DashboardGraphKind
import com.kingsmetric.app.DashboardGraphPanelUiState
import com.kingsmetric.app.DashboardGraphSectionUiState
import com.kingsmetric.app.DashboardHeroUsageBarUiState
import com.kingsmetric.app.DashboardRecentResultPointUiState

@Composable
fun DashboardGraphSection(
    section: DashboardGraphSectionUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard-graphs-section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        section.panels.forEach { panel ->
            when (panel) {
                is DashboardGraphPanelUiState.RecentResults -> RecentResultsGraphPanel(panel)
                is DashboardGraphPanelUiState.HeroUsage -> HeroUsageGraphPanel(panel)
                is DashboardGraphPanelUiState.Unavailable -> UnavailableGraphPanel(panel)
            }
        }
    }
}

@Composable
private fun RecentResultsGraphPanel(
    panel: DashboardGraphPanelUiState.RecentResults
) {
    com.kingsmetric.ui.components.ShellSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "dashboard-graph-recent-results"
    ) {
        Text(panel.title, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            panel.points.forEachIndexed { index, point ->
                RecentResultPoint(
                    point = point,
                    modifier = Modifier
                        .width(52.dp)
                        .testTag("dashboard-graph-recent-results-point-$index")
                )
            }
        }
    }
}

@Composable
private fun RecentResultPoint(
    point: DashboardRecentResultPointUiState,
    modifier: Modifier = Modifier
) {
    val pointColor = if (point.isVictory) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(
                    color = pointColor,
                    shape = RoundedCornerShape(16.dp)
                )
        )
        Text(
            text = point.resultLabel,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun HeroUsageGraphPanel(
    panel: DashboardGraphPanelUiState.HeroUsage
) {
    val maxMatches = panel.bars.maxOfOrNull(DashboardHeroUsageBarUiState::matches)?.coerceAtLeast(1) ?: 1
    com.kingsmetric.ui.components.ShellSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "dashboard-graph-hero-usage"
    ) {
        Text(panel.title, style = MaterialTheme.typography.titleMedium)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            panel.bars.forEachIndexed { index, bar ->
                HeroUsageBar(
                    bar = bar,
                    maxMatches = maxMatches,
                    modifier = Modifier.testTag("dashboard-graph-hero-usage-bar-$index")
                )
            }
        }
    }
}

@Composable
private fun HeroUsageBar(
    bar: DashboardHeroUsageBarUiState,
    maxMatches: Int,
    modifier: Modifier = Modifier
) {
    val fillFraction = bar.matches.toFloat() / maxMatches.toFloat()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(bar.hero, style = MaterialTheme.typography.bodyMedium)
            Text(bar.countLabel, style = MaterialTheme.typography.bodySmall)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fillFraction)
                    .height(14.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
            )
        }
    }
}

@Composable
private fun UnavailableGraphPanel(
    panel: DashboardGraphPanelUiState.Unavailable
) {
    com.kingsmetric.ui.components.ShellSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = panel.kind.toUnavailableTag()
    ) {
        Text(panel.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = panel.message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun DashboardGraphKind.toUnavailableTag(): String {
    return when (this) {
        DashboardGraphKind.RecentResults -> "dashboard-graph-recent-results-unavailable"
        DashboardGraphKind.HeroUsage -> "dashboard-graph-hero-usage-unavailable"
    }
}
