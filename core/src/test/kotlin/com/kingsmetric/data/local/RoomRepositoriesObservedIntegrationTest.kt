package com.kingsmetric.data.local

import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.SavedMatchRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomRepositoryMappingTest {

    @Test
    fun `T1 repository maps Room entities into history detail and dashboard domain models`() = runBlocking {
        val dao = FakeSavedMatchDao(
            initialEntities = listOf(
                savedMatchEntity(
                    recordId = "record-1",
                    savedAt = 200L,
                    hero = "Sun Shangxiang",
                    result = "victory",
                    kda = "11/1/5"
                )
            )
        )
        val repository = roomRepository(dao = dao)

        val history = repository.observeHistory().take(1).toList().single()
        assertTrue(history is HistoryContentState.Loaded)
        history as HistoryContentState.Loaded
        val historyRow = history.records.single()
        assertEquals("record-1", historyRow.recordId)
        assertEquals("Sun Shangxiang", historyRow.hero)
        assertEquals("Farm Lane", historyRow.lane)
        assertEquals("11/1/5", historyRow.kda)
        assertEquals("20-10", historyRow.score)

        val detail = repository.getDetail("record-1")
        assertTrue(detail is RecordLookupResult.Found)
        detail as RecordLookupResult.Found
        assertEquals("shot-1", detail.detail.record.screenshotId)
        assertEquals(ScreenshotPreviewState.Available("stored/shot-1.png"), detail.detail.screenshotPreview)

        val dashboard = repository.observeDashboard().take(1).toList().single()
        assertTrue(dashboard is DashboardContentState.Loaded)
        dashboard as DashboardContentState.Loaded
        assertEquals(100.0, dashboard.metrics.winRate!!.percentage, 0.0)
        assertEquals("Sun Shangxiang", dashboard.metrics.heroUsage.single().hero)
    }

    @Test
    fun `T2 missing record lookup maps to a safe not-found result`() {
        val repository = roomRepository()

        val result = repository.getDetail("missing-record")

        assertEquals(RecordLookupResult.NotFound, result)
    }

    @Test
    fun `T3 save failure maps to a repository error result without false success`() {
        val dao = FakeSavedMatchDao(saveFailure = IllegalStateException("save failed"))
        val repository = roomRepository(dao = dao)

        val result = repository.save(savedMatchRecord())

        assertTrue(result is RepositorySaveResult.Error)
        assertFalse(result is RepositorySaveResult.Saved)
    }
}

class RoomRepositoryObservedIntegrationTest {

    @Test
    fun `IT1 confirmed save persists a Room record with screenshot linkage`() {
        val dao = FakeSavedMatchDao()
        val repository = roomRepository(dao = dao)

        val result = repository.save(savedMatchRecord())

        assertTrue(result is RepositorySaveResult.Saved)
        val entity = dao.currentEntities.single()
        assertEquals("shot-1", entity.screenshotId)
        assertEquals("stored/shot-1.png", entity.screenshotPath)
    }

    @Test
    fun `IT2 history observer emits updated data after save`() = runBlocking {
        val dao = FakeSavedMatchDao()
        val repository = roomRepository(dao = dao)

        val emissions = mutableListOf<HistoryContentState>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            repository.observeHistory().take(2).toList(emissions)
        }

        repository.save(savedMatchRecord())
        withTimeout(5_000) {
            job.join()
        }

        assertEquals(HistoryContentState.Empty, emissions.first())
        assertTrue(emissions.last() is HistoryContentState.Loaded)
    }

    @Test
    fun `IT3 dashboard observer emits updated aggregate inputs after save`() = runBlocking {
        val dao = FakeSavedMatchDao()
        val repository = roomRepository(dao = dao)

        val emissions = mutableListOf<DashboardContentState>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            repository.observeDashboard().take(2).toList(emissions)
        }

        repository.save(savedMatchRecord())
        withTimeout(5_000) {
            job.join()
        }

        assertEquals(DashboardContentState.Empty, emissions.first())
        val updated = emissions.last()
        assertTrue(updated is DashboardContentState.Loaded)
        updated as DashboardContentState.Loaded
        assertEquals(1, updated.metrics.winRate?.totalMatches)
    }

    @Test
    fun `IT4 read failure surfaces an error result`() = runBlocking {
        val dao = FakeSavedMatchDao(observeFailure = IllegalStateException("read failed"))
        val repository = roomRepository(dao = dao)

        val state = repository.observeHistory().take(1).toList().single()

        assertTrue(state is HistoryContentState.Error)
    }

    @Test
    fun `IT5 detail lookup for missing record returns a safe not-found state`() {
        val dao = FakeSavedMatchDao()
        val repository = roomRepository(dao = dao)

        val result = repository.getDetail("record-404")

        assertEquals(RecordLookupResult.NotFound, result)
    }

    @Test
    fun `IT6 repository and controller stay aligned on richer history-row inputs`() = runBlocking {
        val entity = savedMatchEntity(
            recordId = "record-1",
            savedAt = 200L,
            hero = "Sun Shangxiang",
            result = "victory",
            kda = "11/1/5"
        )
        val repository = roomRepository(
            dao = FakeSavedMatchDao(initialEntities = listOf(entity))
        )
        val controller = com.kingsmetric.history.MatchHistoryController(
            repository = com.kingsmetric.history.FakeMatchHistoryRepository(
                records = listOf(
                    com.kingsmetric.history.SavedMatchHistoryRecord(
                        recordId = "record-1",
                        savedAt = 200L,
                        screenshotId = "shot-1",
                        screenshotPath = "stored/shot-1.png",
                        fields = mapOf(
                            FieldKey.RESULT to "victory",
                            FieldKey.HERO to "Sun Shangxiang",
                            FieldKey.LANE to "Farm Lane",
                            FieldKey.SCORE to "20-10",
                            FieldKey.KDA to "11/1/5"
                        )
                    )
                )
            ),
            screenshotFiles = com.kingsmetric.history.FakeScreenshotFileChecker(
                existingPaths = setOf("stored/shot-1.png")
            )
        )

        val repositoryHistory = repository.observeHistory().take(1).toList().single()
        assertTrue(repositoryHistory is HistoryContentState.Loaded)
        repositoryHistory as HistoryContentState.Loaded
        val controllerHistory = controller.loadHistory()
        assertTrue(controllerHistory.history is HistoryContentState.Loaded)
        controllerHistory.history as HistoryContentState.Loaded

        val repositoryRow = repositoryHistory.records.single()
        val controllerRow = controllerHistory.history.records.single()
        assertEquals(controllerRow.result, repositoryRow.result)
        assertEquals(controllerRow.lane, repositoryRow.lane)
        assertEquals(controllerRow.kda, repositoryRow.kda)
        assertEquals(controllerRow.score, repositoryRow.score)
    }
}

private fun roomRepository(
    dao: FakeSavedMatchDao = FakeSavedMatchDao()
): RoomObservedMatchRepository {
    return RoomObservedMatchRepository(
        dao = dao,
        screenshotFiles = FakeLocalScreenshotFileStore(existingPaths = setOf("stored/shot-1.png")),
        recordIdProvider = FixedRecordIdProvider("record-1"),
        savedAtProvider = FixedSavedAtProvider(200L)
    )
}

private fun savedMatchRecord(): SavedMatchRecord {
    return SavedMatchRecord(
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        fields = mapOf(
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
    )
}

private fun savedMatchEntity(
    recordId: String,
    savedAt: Long,
    hero: String,
    result: String,
    kda: String
): SavedMatchEntity {
    return SavedMatchEntity(
        recordId = recordId,
        savedAt = savedAt,
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png",
        result = result,
        hero = hero,
        playerName = "Player",
        lane = "Farm Lane",
        score = "20-10",
        kda = kda,
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

private class FixedRecordIdProvider(
    private val value: String
) : RecordIdProvider {
    override fun nextId(): String = value
}

private class FixedSavedAtProvider(
    private val value: Long
) : SavedAtProvider {
    override fun now(): Long = value
}

private class FakeLocalScreenshotFileStore(
    private val existingPaths: Set<String>
) : LocalScreenshotFileStore {
    override fun exists(path: String): Boolean = path in existingPaths
}

private class FakeSavedMatchDao(
    initialEntities: List<SavedMatchEntity> = emptyList(),
    private val saveFailure: IllegalStateException? = null,
    private val observeFailure: IllegalStateException? = null,
    private val getFailure: IllegalStateException? = null
) : SavedMatchDao {
    private val entities = MutableStateFlow(initialEntities)

    val currentEntities: List<SavedMatchEntity>
        get() = entities.value

    override fun observeAll(): Flow<List<SavedMatchEntity>> {
        return observeFailure?.let { failure ->
            flow { throw failure }
        } ?: entities
    }

    override fun countAll(): Int = entities.value.size

    override fun getById(recordId: String): SavedMatchEntity? {
        getFailure?.let { throw it }
        return entities.value.firstOrNull { it.recordId == recordId }
    }

    override fun insert(entity: SavedMatchEntity) {
        saveFailure?.let { throw it }
        entities.value = entities.value + entity
    }
}
