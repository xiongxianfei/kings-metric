package com.kingsmetric.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SavedMatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KingsMetricDatabase : RoomDatabase() {
    abstract fun savedMatchDao(): SavedMatchDao
}
