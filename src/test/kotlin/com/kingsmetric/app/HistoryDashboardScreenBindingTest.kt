package com.kingsmetric.app

import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RecordLookupResult
import com.kingsmetric.data.local.RepositorySaveResult
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.data.local.SavedMatchDao
import com.kingsmetric.data.local.SavedMatchEntity
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.MatchDetailState
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.SavedMatchRecord
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryDashboardScreenStateMapperTest {

    @Test
    fun `T1 screen state mappers convert repository models into history and dashboard UI state`() {
        val historyState = HistoryContentState.Loaded(
            records = listOf(
                com.kingsmetric.history.MatchHistoryListItem(
                    recordId = "record-1",
                    savedAt = 200L,
                    hero = "Sun Shangxiang",
                    result = "victory"
                )
            )
        ).toHistoryScreenUiState()
        val dashboardState = DashboardContentState.Loaded(
            metrics = com.kingsmetric.dashboard.DashboardMetricsCalculator().calculate(
                listOf(savedHistoryRecord())
            )
        ).toDashboardScreenUiState()

        assertTrue(historyState.content is HistoryContentState.Loaded)
        historyState.content as HistoryContentState.Loaded
        assertEquals("record-1", historyState.content.records.single().recordId)

        assertTrue(dashboardState.content is DashboardContentState.Loaded)
        dashboardState.content as DashboardContentState.Loaded
        assertEquals("Sun Shangxiang", dashboardState.content.metrics.heroUsage.single().hero)
    }

    @Test
    fun `T2 detail screen state preserves field visibility when screenshot preview is unavailable`() {
        val detailState = MatchDetailState(
            record = savedHistoryRecord(),
            screenshotPreview = ScreenshotPreviewState.Unavailable
        ).toDetailScreenUiState()

        assertEquals(PreviewAvailability.Unavailable, detailState.previewAvailability)
        assertTrue(detailState.fields.any { it.key == FieldKey.HERO && it.value == "Sun Shangxiang" })
        assertTrue(detailState.fields.any { it.key == FieldKey.KDA && it.value == "11/1/5" })
    }
}

class HistoryDashboardScreenBindingIntegrationTest {

    @Test
    fun `IT1 history screen renders repository-backed saved records`() = runBlocking {
        val binder = historyBinder()

        val job = binder.bind(this)
        waitForHistoryLoaded(binder)

        val state = binder.state.value
        assertTrue(state.content is HistoryContentState.Loaded)
        state.content as HistoryContentState.Loaded
        assertEquals("Sun Shangxiang", state.content.records.single().hero)
        job.cancel()
    }

    @Test
    fun `IT2 dashboard screen renders repository-backed metrics`() = runBlocking {
        val binder = dashboardBinder()

        val job = binder.bind(this)
        waitForDashboardLoaded(binder)

        val state = binder.state.value
        assertTrue(state.content is DashboardContentState.Loaded)
        state.content as DashboardContentState.Loaded
        assertEquals(1, state.content.metrics.winRate?.totalMatches)
        job.cancel()
    }

    @Test
    fun `IT3 empty repository state shows explicit empty states`() = runBlocking {
        val emptyRepository = repository(dao = FakeSavedMatchDao())
        val historyBinder = HistoryScreenBinder(emptyRepository)
        val dashboardBinder = DashboardScreenBinder(emptyRepository)

        val historyJob = historyBinder.bind(this)
        val dashboardJob = dashboardBinder.bind(this)

        withTimeout(5_000) {
            while (historyBinder.state.value.content !is HistoryContentState.Empty ||
                dashboardBinder.state.value.content !is DashboardContentState.Empty) {
                kotlinx.coroutines.yield()
            }
        }

        assertEquals(HistoryContentState.Empty, historyBinder.state.value.content)
        assertEquals(DashboardContentState.Empty, dashboardBinder.state.value.content)
        historyJob.cancel()
        dashboardJob.cancel()
    }

    @Test
    fun `IT4 repository updates refresh visible history and dashboard UI`() = runBlocking {
        val dao = FakeSavedMatchDao()
        val repository = repository(dao = dao)
        val historyBinder = HistoryScreenBinder(repository)
        val dashboardBinder = DashboardScreenBinder(repository)

        val historyJob = historyBinder.bind(this)
        val dashboardJob = dashboardBinder.bind(this)

        dao.insert(savedMatchEntity("record-1"))

        waitForHistoryLoaded(historyBinder)
        waitForDashboardLoaded(dashboardBinder)

        assertTrue(historyBinder.state.value.content is HistoryContentState.Loaded)
        assertTrue(dashboardBinder.state.value.content is DashboardContentState.Loaded)
        historyJob.cancel()
        dashboardJob.cancel()
    }

    @Test
    fun `IT5 missing screenshot file renders unavailable-preview UI in detail`() = runBlocking {
        val binder = HistoryScreenBinder(
            repository = repository(existingPaths = emptySet())
        )

        val job = binder.bind(this)
        waitForHistoryLoaded(binder)
        binder.openDetail(this, "record-1")

        withTimeout(5_000) {
            while (binder.state.value.detail == null) {
                kotlinx.coroutines.yield()
            }
        }

        val detail = binder.state.value.detail!!
        assertEquals(PreviewAvailability.Unavailable, detail.previewAvailability)
        assertFalse(detail.fields.isEmpty())
        job.cancel()
    }
}

private fun historyBinder(): HistoryScreenBinder {
    return HistoryScreenBinder(repository())
}

private fun dashboardBinder(): DashboardScreenBinder {
    return DashboardScreenBinder(repository())
}

private fun repository(
    dao: FakeSavedMatchDao = FakeSavedMatchDao(initialEntities = listOf(savedMatchEntity("record-1"))),
    existingPaths: Set<String> = setOf("stored/shot-1.png")
): RoomObservedMatchRepository {
    return RoomObservedMatchRepository(
        dao = dao,
        screenshotFiles = object : LocalScreenshotFileStore {
            override fun exists(path: String): Boolean = path in existingPaths
        },
        recordIdProvider = object : RecordIdProvider {
            override fun nextId(): String = "record-1"
        },
        savedAtProvider = object : SavedAtProvider {
            override fun now(): Long = 200L
        }
    )
}

private fun savedHistoryRecord(): SavedMatchHistoryRecord {
    return SavedMatchHistoryRecord(
        recordId = "record-1",
        savedAt = 200L,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        fields = baseFields()
    )
}

private fun savedMatchEntity(recordId: String): SavedMatchEntity {
    return SavedMatchEntity(
        recordId = recordId,
        savedAt = 200L,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        result = "victory",
        hero = "Sun Shangxiang",
        playerName = "Player",
        lane = "Farm Lane",
        score = "20-10",
        kda = "11/1/5",
        damageDealt = "12345",
        damageShare = "34%",
        damageTaken = "9876",
        damageTakenShare = "28%",
        totalGold = "12001",
        goldShare = "31%",
        participationRate = "76%",
        goldFromFarming = "3500",
        lastHits = "80",
        killParticipationCount = "13",
        controlDuration = "00:14",
        damageDealtToOpponents = "10101"
    )
}

private fun baseFields(): Map<FieldKey, String?> {
    return mapOf(
        FieldKey.RESULT to "victory",
        FieldKey.HERO to "Sun Shangxiang",
        FieldKey.PLAYER_NAME to "Player",
        FieldKey.LANE to "Farm Lane",
        FieldKey.SCORE to "20-10",
        FieldKey.KDA to "11/1/5",
        FieldKey.DAMAGE_DEALT to "12345",
        FieldKey.DAMAGE_SHARE to "34%",
        FieldKey.DAMAGE_TAKEN to "9876",
        FieldKey.DAMAGE_TAKEN_SHARE to "28%",
        FieldKey.TOTAL_GOLD to "12001",
        FieldKey.GOLD_SHARE to "31%",
        FieldKey.PARTICIPATION_RATE to "76%",
        FieldKey.GOLD_FROM_FARMING to "3500",
        FieldKey.LAST_HITS to "80",
        FieldKey.KILL_PARTICIPATION_COUNT to "13",
        FieldKey.CONTROL_DURATION to "00:14",
        FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
    )
}

private suspend fun waitForHistoryLoaded(binder: HistoryScreenBinder) {
    withTimeout(5_000) {
        while (binder.state.value.content !is HistoryContentState.Loaded) {
            kotlinx.coroutines.yield()
        }
    }
}

private suspend fun waitForDashboardLoaded(binder: DashboardScreenBinder) {
    withTimeout(5_000) {
        while (binder.state.value.content !is DashboardContentState.Loaded) {
            kotlinx.coroutines.yield()
        }
    }
}

private class FakeSavedMatchDao(
    initialEntities: List<SavedMatchEntity> = emptyList(),
    private val observeFailure: IllegalStateException? = null
) : SavedMatchDao {
    private val entities = MutableStateFlow(initialEntities)

    override fun observeAll(): Flow<List<SavedMatchEntity>> {
        return observeFailure?.let { failure ->
            flow { throw failure }
        } ?: entities
    }

    override fun getById(recordId: String): SavedMatchEntity? {
        return entities.value.firstOrNull { it.recordId == recordId }
    }

    override fun insert(entity: SavedMatchEntity) {
        entities.value = entities.value + entity
    }
}
