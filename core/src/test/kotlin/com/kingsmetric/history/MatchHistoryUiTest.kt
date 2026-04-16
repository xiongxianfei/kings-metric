package com.kingsmetric.history

import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchHistoryUiTest {

    @Test
    fun `T1 history state exposes records in stable recent-first order`() {
        val controller = historyController(
            repository = FakeMatchHistoryRepository(
                records = listOf(
                    HistoryFixtures.record(recordId = "record-1", savedAt = 100L),
                    HistoryFixtures.record(recordId = "record-3", savedAt = 300L),
                    HistoryFixtures.record(recordId = "record-2", savedAt = 200L)
                )
            )
        )

        val state = controller.loadHistory()

        assertTrue(state.history is HistoryContentState.Loaded)
        state.history as HistoryContentState.Loaded
        assertEquals(listOf("record-3", "record-2", "record-1"), state.history.records.map { it.recordId })
        assertEquals("Farm Lane", state.history.records.first().lane)
        assertEquals("11/1/5", state.history.records.first().kda)
        assertEquals("20-10", state.history.records.first().score)
    }

    @Test
    fun `T5 history state exposes approved richer quick summary fields from saved records`() {
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(HistoryFixtures.record()))
        )

        val state = controller.loadHistory()

        assertTrue(state.history is HistoryContentState.Loaded)
        state.history as HistoryContentState.Loaded
        val row = state.history.records.single()
        assertEquals("victory", row.result)
        assertEquals("Farm Lane", row.lane)
        assertEquals("11/1/5", row.kda)
        assertEquals("20-10", row.score)
    }

    @Test
    fun `T2 empty history state is explicit`() {
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = emptyList())
        )

        val state = controller.loadHistory()

        assertEquals(HistoryContentState.Empty, state.history)
    }

    @Test
    fun `T3 detail state includes saved fields and screenshot reference when available`() {
        val record = HistoryFixtures.record()
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(record))
        )

        val state = controller.openRecord(controller.loadHistory(), record.recordId)

        assertTrue(state.detail != null)
        assertEquals("victory", state.detail?.record?.fields?.get(FieldKey.RESULT))
        assertEquals(
            ScreenshotPreviewState.Available(record.screenshotPath),
            state.detail?.screenshotPreview
        )
    }

    @Test
    fun `T4 detail state degrades gracefully when screenshot file is missing`() {
        val record = HistoryFixtures.record()
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(record)),
            screenshotFiles = FakeScreenshotFileChecker(existingPaths = emptySet())
        )

        val state = controller.openRecord(controller.loadHistory(), record.recordId)

        assertTrue(state.detail != null)
        assertEquals(ScreenshotPreviewState.Unavailable, state.detail?.screenshotPreview)
        assertEquals("Sun Shangxiang", state.detail?.record?.fields?.get(FieldKey.HERO))
    }

    @Test
    fun `IT1 saved records appear in the history list`() {
        val records = listOf(
            HistoryFixtures.record(recordId = "record-1", savedAt = 100L),
            HistoryFixtures.record(recordId = "record-2", savedAt = 200L)
        )
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = records)
        )

        val state = controller.loadHistory()

        assertTrue(state.history is HistoryContentState.Loaded)
        state.history as HistoryContentState.Loaded
        assertEquals(2, state.history.records.size)
    }

    @Test
    fun `IT2 empty local storage shows the empty state`() {
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = emptyList())
        )

        val state = controller.loadHistory()

        assertEquals(HistoryContentState.Empty, state.history)
        assertNull(state.detail)
    }

    @Test
    fun `IT3 user opens one record from history and sees saved fields`() {
        val record = HistoryFixtures.record()
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(record))
        )

        val state = controller.openRecord(controller.loadHistory(), record.recordId)

        assertEquals(record.recordId, state.detail?.record?.recordId)
        assertEquals("11/1/5", state.detail?.record?.fields?.get(FieldKey.KDA))
    }

    @Test
    fun `IT4 user opens one record with a missing screenshot file and sees a non-crashing unavailable-image state`() {
        val record = HistoryFixtures.record()
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(record)),
            screenshotFiles = FakeScreenshotFileChecker(existingPaths = emptySet())
        )

        val state = controller.openRecord(controller.loadHistory(), record.recordId)

        assertEquals(ScreenshotPreviewState.Unavailable, state.detail?.screenshotPreview)
        assertEquals(record.recordId, state.detail?.record?.recordId)
    }

    @Test
    fun `IT5 history load failure shows recoverable error state`() {
        val controller = historyController(
            repository = FakeMatchHistoryRepository(shouldFailOnLoad = true)
        )

        val state = controller.loadHistory()

        assertTrue(state.history is HistoryContentState.Error)
        state.history as HistoryContentState.Error
        assertTrue(state.history.canRetry)
    }

    @Test
    fun `IT6 missing record detail request returns to safe list state and reports failure`() {
        val record = HistoryFixtures.record()
        val controller = historyController(
            repository = FakeMatchHistoryRepository(records = listOf(record))
        )
        val loadedState = controller.loadHistory()

        val state = controller.openRecord(loadedState, "missing-record")

        assertEquals(loadedState.history, state.history)
        assertNull(state.detail)
        assertEquals("Saved match is no longer available.", state.userMessage)
    }
}

private fun historyController(
    repository: FakeMatchHistoryRepository = FakeMatchHistoryRepository(),
    screenshotFiles: FakeScreenshotFileChecker = FakeScreenshotFileChecker()
): MatchHistoryController {
    return MatchHistoryController(
        repository = repository,
        screenshotFiles = screenshotFiles
    )
}

private object HistoryFixtures {
    fun record(
        recordId: String = "record-1",
        savedAt: Long = 200L,
        screenshotPath: String = "stored/record-1.png"
    ): SavedMatchHistoryRecord {
        return SavedMatchHistoryRecord(
            recordId = recordId,
            savedAt = savedAt,
            screenshotId = "shot-$recordId",
            screenshotPath = screenshotPath,
            fields = mapOf(
                FieldKey.RESULT to "victory",
                FieldKey.HERO to "Sun Shangxiang",
                FieldKey.PLAYER_NAME to "King",
                FieldKey.LANE to "Farm Lane",
                FieldKey.SCORE to "20-10",
                FieldKey.KDA to "11/1/5"
            )
        )
    }
}
