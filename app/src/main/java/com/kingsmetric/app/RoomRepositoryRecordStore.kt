package com.kingsmetric.app

import com.kingsmetric.data.local.RepositorySaveResult
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.importflow.RecordStore
import com.kingsmetric.importflow.SavedMatchRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class RoomRepositoryRecordStore(
    private val repository: RoomObservedMatchRepository
) : RecordStore {
    override fun save(record: SavedMatchRecord): SavedMatchRecord {
        return runBlocking {
            when (val result = withContext(Dispatchers.IO) { repository.save(record) }) {
                is RepositorySaveResult.Saved -> record
                is RepositorySaveResult.Error -> {
                    throw IllegalStateException(result.message)
                }
            }
        }
    }
}
