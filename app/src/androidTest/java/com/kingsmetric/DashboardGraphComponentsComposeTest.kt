package com.kingsmetric

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.DashboardGraphPanelUiState
import com.kingsmetric.app.DashboardGraphSectionUiState
import com.kingsmetric.app.DashboardHeroUsageBarUiState
import com.kingsmetric.app.DashboardRecentResultPointUiState
import com.kingsmetric.ui.components.DashboardGraphSection
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardGraphComponentsComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun graphSection_rendersRecentResultsAndHeroUsagePanelsWithVisibleLabels() {
        composeRule.setContent {
            DashboardGraphSection(
                section = DashboardGraphSectionUiState(
                    panels = listOf(
                        DashboardGraphPanelUiState.RecentResults(
                            title = "Recent Results",
                            points = listOf(
                                DashboardRecentResultPointUiState(
                                    recordId = "record-1",
                                    isVictory = true,
                                    resultLabel = "Victory"
                                ),
                                DashboardRecentResultPointUiState(
                                    recordId = "record-2",
                                    isVictory = false,
                                    resultLabel = "Defeat"
                                )
                            )
                        ),
                        DashboardGraphPanelUiState.HeroUsage(
                            title = "Hero Usage",
                            bars = listOf(
                                DashboardHeroUsageBarUiState(
                                    hero = "Sun Shangxiang",
                                    matches = 2,
                                    countLabel = "2 matches"
                                ),
                                DashboardHeroUsageBarUiState(
                                    hero = "Arli",
                                    matches = 1,
                                    countLabel = "1 match"
                                )
                            )
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithTag("dashboard-graphs-section").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-graph-recent-results").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-graph-hero-usage").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-graph-recent-results-point-0").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-graph-hero-usage-bar-0").assertIsDisplayed()
        composeRule.onNodeWithText("Recent Results").assertIsDisplayed()
        composeRule.onNodeWithText("Victory").assertIsDisplayed()
        composeRule.onNodeWithText("Defeat").assertIsDisplayed()
        composeRule.onNodeWithText("Hero Usage").assertIsDisplayed()
        composeRule.onNodeWithText("Sun Shangxiang").assertIsDisplayed()
        composeRule.onNodeWithText("2 matches").assertIsDisplayed()
        composeRule.onNodeWithText("Arli").assertIsDisplayed()
        composeRule.onNodeWithText("1 match").assertIsDisplayed()
    }

    @Test
    fun graphSection_rendersUnavailablePanelMessageWithUserFacingText() {
        composeRule.setContent {
            DashboardGraphSection(
                section = DashboardGraphSectionUiState(
                    panels = listOf(
                        DashboardGraphPanelUiState.Unavailable(
                            kind = com.kingsmetric.app.DashboardGraphKind.HeroUsage,
                            title = "Hero Usage",
                            message = "Hero usage graph is unavailable for the current saved matches."
                        )
                    )
                )
            )
        }

        composeRule.onNodeWithTag("dashboard-graph-hero-usage-unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Hero Usage").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Hero usage graph is unavailable for the current saved matches."
        ).assertIsDisplayed()
    }

    @Test
    fun graphSection_narrowPhoneWidthKeepsPanelsVisibleInPortraitFlow() {
        composeRule.setContent {
            Box(modifier = Modifier.width(320.dp)) {
                DashboardGraphSection(
                    section = DashboardGraphSectionUiState(
                        panels = listOf(
                            DashboardGraphPanelUiState.RecentResults(
                                title = "Recent Results",
                                points = listOf(
                                    DashboardRecentResultPointUiState(
                                        recordId = "record-1",
                                        isVictory = true,
                                        resultLabel = "Victory"
                                    ),
                                    DashboardRecentResultPointUiState(
                                        recordId = "record-2",
                                        isVictory = false,
                                        resultLabel = "Defeat"
                                    ),
                                    DashboardRecentResultPointUiState(
                                        recordId = "record-3",
                                        isVictory = true,
                                        resultLabel = "Victory"
                                    )
                                )
                            ),
                            DashboardGraphPanelUiState.HeroUsage(
                                title = "Hero Usage",
                                bars = listOf(
                                    DashboardHeroUsageBarUiState(
                                        hero = "Sun Shangxiang",
                                        matches = 3,
                                        countLabel = "3 matches"
                                    ),
                                    DashboardHeroUsageBarUiState(
                                        hero = "Consort Yu",
                                        matches = 2,
                                        countLabel = "2 matches"
                                    ),
                                    DashboardHeroUsageBarUiState(
                                        hero = "Arli",
                                        matches = 1,
                                        countLabel = "1 match"
                                    )
                                )
                            )
                        )
                    )
                )
            }
        }

        composeRule.onNodeWithTag("dashboard-graph-recent-results").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard-graph-hero-usage").assertIsDisplayed()
        composeRule.onNodeWithText("Consort Yu").assertIsDisplayed()
        composeRule.onNodeWithText("3 matches").assertIsDisplayed()
    }
}
