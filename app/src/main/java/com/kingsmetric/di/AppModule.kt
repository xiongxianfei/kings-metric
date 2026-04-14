package com.kingsmetric.di

import android.content.Context
import androidx.room.Room
import com.kingsmetric.AndroidUriScreenshotStorage
import com.kingsmetric.app.AndroidBitmapLoader
import com.kingsmetric.app.AndroidMlKitTextRecognizer
import com.kingsmetric.app.MlKitRecognitionAdapter
import com.kingsmetric.app.RoomRepositoryRecordStore
import com.kingsmetric.app.UriScreenshotStorage
import com.kingsmetric.data.local.KingsMetricDatabase
import com.kingsmetric.data.local.LocalScreenshotFileStore
import com.kingsmetric.data.local.RecordIdProvider
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.data.local.SavedAtProvider
import com.kingsmetric.data.local.SavedMatchDao
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.FakeScreenshotAnalyzer
import com.kingsmetric.importflow.FakeScreenshotStore
import com.kingsmetric.importflow.MatchImportWorkflow
import com.kingsmetric.importflow.SavedMatchRecord
import com.kingsmetric.importflow.TemplateValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): KingsMetricDatabase {
        return Room.databaseBuilder(
            context,
            KingsMetricDatabase::class.java,
            "kings-metric.db"
        ).build()
    }

    @Provides
    fun provideSavedMatchDao(
        database: KingsMetricDatabase
    ): SavedMatchDao = database.savedMatchDao()

    @Provides
    fun provideLocalScreenshotFileStore(): LocalScreenshotFileStore {
        return object : LocalScreenshotFileStore {
            override fun exists(path: String): Boolean = File(path).exists()
        }
    }

    @Provides
    fun provideRecordIdProvider(): RecordIdProvider {
        return object : RecordIdProvider {
            override fun nextId(): String = UUID.randomUUID().toString()
        }
    }

    @Provides
    fun provideSavedAtProvider(): SavedAtProvider {
        return object : SavedAtProvider {
            override fun now(): Long = System.currentTimeMillis()
        }
    }

    @Provides
    @Singleton
    fun provideRepository(
        dao: SavedMatchDao,
        screenshotFiles: LocalScreenshotFileStore,
        recordIdProvider: RecordIdProvider,
        savedAtProvider: SavedAtProvider
    ): RoomObservedMatchRepository {
        return RoomObservedMatchRepository(
            dao = dao,
            screenshotFiles = screenshotFiles,
            recordIdProvider = recordIdProvider,
            savedAtProvider = savedAtProvider
        )
    }

    @Provides
    @Singleton
    fun provideUriScreenshotStorage(
        @ApplicationContext context: Context
    ): UriScreenshotStorage = AndroidUriScreenshotStorage(context)

    @Provides
    @Singleton
    fun provideRecognitionAdapter(
        @ApplicationContext context: Context
    ): MlKitRecognitionAdapter {
        return MlKitRecognitionAdapter(
            bitmapLoader = AndroidBitmapLoader(),
            recognizer = AndroidMlKitTextRecognizer(context)
        )
    }

    @Provides
    @Singleton
    fun provideReviewWorkflow(
        repository: RoomObservedMatchRepository
    ): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = FakeScreenshotStore(),
            analyzer = FakeScreenshotAnalyzer(emptyMap()),
            recordStore = RoomRepositoryRecordStore(repository),
            validator = TemplateValidator(),
            parser = DraftParser()
        )
    }
}
