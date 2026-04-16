package com.kingsmetric.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.kingsmetric.dashboard.DashboardContentState
import com.kingsmetric.dashboard.DashboardMetricsCalculator
import com.kingsmetric.history.HistoryContentState
import com.kingsmetric.history.MatchDetailState
import com.kingsmetric.history.MatchHistoryListItem
import com.kingsmetric.history.SavedMatchHistoryRecord
import com.kingsmetric.history.ScreenshotPreviewState
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.SavedMatchRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

@Entity(tableName = "saved_matches")
data class SavedMatchEntity(
    @PrimaryKey val recordId: String,
    val savedAt: Long,
    val screenshotId: String,
    val screenshotPath: String,
    val result: String?,
    val hero: String?,
    val playerName: String?,
    val lane: String?,
    val score: String?,
    val kda: String?,
    val damageDealt: String?,
    val damageShare: String?,
    val damageTaken: String?,
    val damageTakenShare: String?,
    val totalGold: String?,
    val goldShare: String?,
    val participationRate: String?,
    val goldFromFarming: String?,
    val lastHits: String?,
    val killParticipationCount: String?,
    val controlDuration: String?,
    val damageDealtToOpponents: String?
)

@Dao
interface SavedMatchDao {
    @Query("SELECT * FROM saved_matches ORDER BY savedAt DESC, recordId ASC")
    fun observeAll(): Flow<List<SavedMatchEntity>>

    @Query("SELECT COUNT(*) FROM saved_matches")
    fun countAll(): Int

    @Query("SELECT * FROM saved_matches WHERE recordId = :recordId LIMIT 1")
    fun getById(recordId: String): SavedMatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: SavedMatchEntity)
}

interface RecordIdProvider {
    fun nextId(): String
}

interface SavedAtProvider {
    fun now(): Long
}

interface LocalScreenshotFileStore {
    fun exists(path: String): Boolean
}

sealed interface RepositorySaveResult {
    data class Saved(val record: SavedMatchHistoryRecord) : RepositorySaveResult
    data class Error(val message: String) : RepositorySaveResult
}

sealed interface RecordLookupResult {
    data class Found(val detail: MatchDetailState) : RecordLookupResult
    data object NotFound : RecordLookupResult
    data class Error(val message: String) : RecordLookupResult
}

class RoomObservedMatchRepository(
    private val dao: SavedMatchDao,
    private val screenshotFiles: LocalScreenshotFileStore,
    private val recordIdProvider: RecordIdProvider,
    private val savedAtProvider: SavedAtProvider,
    private val calculator: DashboardMetricsCalculator = DashboardMetricsCalculator()
) {

    fun hasSavedRecords(): Boolean {
        return try {
            dao.countAll() > 0
        } catch (_: IllegalStateException) {
            false
        }
    }

    fun save(record: SavedMatchRecord): RepositorySaveResult {
        val entity = record.toEntity(
            recordId = recordIdProvider.nextId(),
            savedAt = savedAtProvider.now()
        )
        return try {
            dao.insert(entity)
            RepositorySaveResult.Saved(entity.toHistoryRecord())
        } catch (_: IllegalStateException) {
            RepositorySaveResult.Error("Could not save record locally.")
        }
    }

    fun observeHistory(): Flow<HistoryContentState> {
        return dao.observeAll()
            .map { entities ->
                if (entities.isEmpty()) {
                    HistoryContentState.Empty
                } else {
                    HistoryContentState.Loaded(
                        records = entities.map { entity ->
                            MatchHistoryListItem(
                                recordId = entity.recordId,
                                savedAt = entity.savedAt,
                                hero = entity.hero,
                                result = entity.result,
                                lane = entity.lane,
                                score = entity.score,
                                kda = entity.kda,
                                screenshotAvailable = screenshotFiles.exists(entity.screenshotPath)
                            )
                        }
                    )
                }
            }
            .catch {
                emit(HistoryContentState.Error("Could not load saved matches."))
            }
    }

    fun observeDashboard(): Flow<DashboardContentState> {
        return dao.observeAll()
            .map { entities ->
                if (entities.isEmpty()) {
                    DashboardContentState.Empty
                } else {
                    DashboardContentState.Loaded(
                        metrics = calculator.calculate(entities.map(SavedMatchEntity::toHistoryRecord))
                    )
                }
            }
            .catch {
                emit(DashboardContentState.Error("Could not load dashboard metrics."))
            }
    }

    fun getDetail(recordId: String): RecordLookupResult {
        val entity = try {
            dao.getById(recordId)
        } catch (_: IllegalStateException) {
            return RecordLookupResult.Error("Could not load saved match.")
        } ?: return RecordLookupResult.NotFound

        val record = entity.toHistoryRecord()
        return RecordLookupResult.Found(
            detail = MatchDetailState(
                record = record,
                screenshotPreview = record.screenshotPath
                    .takeIf { screenshotFiles.exists(it) }
                    ?.let(ScreenshotPreviewState::Available)
                    ?: ScreenshotPreviewState.Unavailable
            )
        )
    }
}

private fun SavedMatchRecord.toEntity(
    recordId: String,
    savedAt: Long
): SavedMatchEntity {
    return SavedMatchEntity(
        recordId = recordId,
        savedAt = savedAt,
        screenshotId = screenshotId,
        screenshotPath = screenshotPath,
        result = fields[FieldKey.RESULT],
        hero = fields[FieldKey.HERO],
        playerName = fields[FieldKey.PLAYER_NAME],
        lane = fields[FieldKey.LANE],
        score = fields[FieldKey.SCORE],
        kda = fields[FieldKey.KDA],
        damageDealt = fields[FieldKey.DAMAGE_DEALT],
        damageShare = fields[FieldKey.DAMAGE_SHARE],
        damageTaken = fields[FieldKey.DAMAGE_TAKEN],
        damageTakenShare = fields[FieldKey.DAMAGE_TAKEN_SHARE],
        totalGold = fields[FieldKey.TOTAL_GOLD],
        goldShare = fields[FieldKey.GOLD_SHARE],
        participationRate = fields[FieldKey.PARTICIPATION_RATE],
        goldFromFarming = fields[FieldKey.GOLD_FROM_FARMING],
        lastHits = fields[FieldKey.LAST_HITS],
        killParticipationCount = fields[FieldKey.KILL_PARTICIPATION_COUNT],
        controlDuration = fields[FieldKey.CONTROL_DURATION],
        damageDealtToOpponents = fields[FieldKey.DAMAGE_DEALT_TO_OPPONENTS]
    )
}

private fun SavedMatchEntity.toHistoryRecord(): SavedMatchHistoryRecord {
    return SavedMatchHistoryRecord(
        recordId = recordId,
        savedAt = savedAt,
        screenshotId = screenshotId,
        screenshotPath = screenshotPath,
        fields = mapOf(
            FieldKey.RESULT to result,
            FieldKey.HERO to hero,
            FieldKey.PLAYER_NAME to playerName,
            FieldKey.LANE to lane,
            FieldKey.SCORE to score,
            FieldKey.KDA to kda,
            FieldKey.DAMAGE_DEALT to damageDealt,
            FieldKey.DAMAGE_SHARE to damageShare,
            FieldKey.DAMAGE_TAKEN to damageTaken,
            FieldKey.DAMAGE_TAKEN_SHARE to damageTakenShare,
            FieldKey.TOTAL_GOLD to totalGold,
            FieldKey.GOLD_SHARE to goldShare,
            FieldKey.PARTICIPATION_RATE to participationRate,
            FieldKey.GOLD_FROM_FARMING to goldFromFarming,
            FieldKey.LAST_HITS to lastHits,
            FieldKey.KILL_PARTICIPATION_COUNT to killParticipationCount,
            FieldKey.CONTROL_DURATION to controlDuration,
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS to damageDealtToOpponents
        )
    )
}
