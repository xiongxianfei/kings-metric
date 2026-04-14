package com.kingsmetric

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kingsmetric.app.MlKitRecognitionAdapter
import com.kingsmetric.app.UriScreenshotStorage
import com.kingsmetric.data.local.RoomObservedMatchRepository
import com.kingsmetric.importflow.MatchImportWorkflow
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var repository: RoomObservedMatchRepository
    @Inject lateinit var uriStorage: UriScreenshotStorage
    @Inject lateinit var recognitionAdapter: MlKitRecognitionAdapter
    @Inject lateinit var reviewWorkflow: MatchImportWorkflow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KingsMetricApp(
                repository = repository,
                uriStorage = uriStorage,
                recognizeStoredScreenshot = recognitionAdapter::recognize,
                reviewWorkflow = reviewWorkflow
            )
        }
    }
}

@Composable
private fun KingsMetricApp(
    repository: RoomObservedMatchRepository,
    uriStorage: UriScreenshotStorage,
    recognizeStoredScreenshot: (String) -> com.kingsmetric.importflow.ImportResult,
    reviewWorkflow: MatchImportWorkflow
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HistoryDashboardRoot(
                repository = repository,
                uriStorage = uriStorage,
                recognizeStoredScreenshot = recognizeStoredScreenshot,
                reviewWorkflow = reviewWorkflow
            )
        }
    }
}
