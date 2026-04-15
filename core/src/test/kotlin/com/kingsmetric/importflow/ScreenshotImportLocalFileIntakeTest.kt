package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotImportLocalFileIntakeUnitTest {

    @Test
    fun `T1 accept one selected screenshot input`() {
        val result = SingleScreenshotSelectionPolicy().accept(listOf("picked-image.png"))

        assertTrue(result is ScreenshotSelectionResult.Accepted)
        result as ScreenshotSelectionResult.Accepted
        assertEquals("picked-image.png", result.sourcePath)
    }

    @Test
    fun `T2 treat repeated import of the same screenshot as a new intake attempt`() {
        val workflow = intakeWorkflow()

        val first = workflow.importScreenshot("picked-image.png")
        val second = workflow.importScreenshot("picked-image.png")

        assertTrue(first is ImportResult.DraftReady)
        assertTrue(second is ImportResult.DraftReady)
        first as ImportResult.DraftReady
        second as ImportResult.DraftReady
        assertNotEquals(first.storedScreenshot.id, second.storedScreenshot.id)
        assertNotEquals(first.storedScreenshot.path, second.storedScreenshot.path)
    }

    @Test
    fun `T3 create a stable screenshot reference after successful copy`() {
        val result = intakeWorkflow().importScreenshot("picked-image.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertTrue(result.storedScreenshot.id.isNotBlank())
        assertTrue(result.storedScreenshot.path.startsWith("stored/"))
    }

    @Test
    fun `T4 preserve original screenshot content metadata through intake`() {
        val result = intakeWorkflow().importScreenshot("picked-image.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("picked-image.png", result.storedScreenshot.originalSourcePath)
        assertFalse(result.storedScreenshot.path == result.storedScreenshot.originalSourcePath)
    }
}

class ScreenshotImportLocalFileIntakeIntegrationTest {

    @Test
    fun `IT2 intake stops when source image cannot be read`() {
        val workflow = intakeWorkflow(
            screenshotStore = FakeScreenshotStore(unreadablePaths = setOf("broken-source.png"))
        )

        val result = workflow.importScreenshot("broken-source.png")

        assertTrue(result is ImportResult.ImportFailed)
        result as ImportResult.ImportFailed
        assertTrue(result.message.contains("import", ignoreCase = true))
    }

    @Test
    fun `IT3 intake stops when local storage write fails`() {
        val workflow = intakeWorkflow(
            screenshotStore = FakeScreenshotStore(failPaths = setOf("write-fails.png"))
        )

        val result = workflow.importScreenshot("write-fails.png")

        assertTrue(result is ImportResult.StorageFailed)
        result as ImportResult.StorageFailed
        assertTrue(result.message.contains("save", ignoreCase = true))
    }

    @Test
    fun `IT4 user cancel during image selection returns to idle import state`() {
        val result = intakeWorkflow().importSelection(emptyList())

        assertTrue(result is ImportResult.Cancelled)
    }

}

private fun intakeWorkflow(
    screenshotStore: FakeScreenshotStore = FakeScreenshotStore()
): MatchImportWorkflow {
    return MatchImportWorkflow(
        screenshotStore = screenshotStore,
        analyzer = FakeScreenshotAnalyzer(
            mapOf("picked-image.png" to IntakeFixtures.supportedAnalysis())
        ),
        recordStore = FakeRecordStore(),
        validator = TemplateValidator(),
        parser = DraftParser()
    )
}

private object IntakeFixtures {
    fun supportedAnalysis(): ScreenshotAnalysis {
        return ScreenshotAnalysis(
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            languageCode = "zh-CN",
            visibleFields = FieldKey.all,
            rawValues = mapOf(
                FieldKey.RESULT to "胜利",
                FieldKey.HERO to "孙尚香",
                FieldKey.PLAYER_NAME to "King",
                FieldKey.LANE to "对抗路",
                FieldKey.SCORE to "20-10",
                FieldKey.KDA to "11/1/5",
                FieldKey.DAMAGE_DEALT to "12345",
                FieldKey.DAMAGE_SHARE to "34%",
                FieldKey.DAMAGE_TAKEN to "9850",
                FieldKey.DAMAGE_TAKEN_SHARE to "28%",
                FieldKey.TOTAL_GOLD to "12543",
                FieldKey.GOLD_SHARE to "31%",
                FieldKey.PARTICIPATION_RATE to "76%",
                FieldKey.GOLD_FROM_FARMING to "3680",
                FieldKey.LAST_HITS to "71",
                FieldKey.KILL_PARTICIPATION_COUNT to "13",
                FieldKey.CONTROL_DURATION to "00:14",
                FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
            ),
            lowConfidenceFields = emptySet()
        )
    }
}
