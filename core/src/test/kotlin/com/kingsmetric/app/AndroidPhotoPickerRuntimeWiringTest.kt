package com.kingsmetric.app

import com.kingsmetric.diagnostics.DiagnosticsOutcome
import com.kingsmetric.diagnostics.DiagnosticsRecorder
import com.kingsmetric.diagnostics.DiagnosticsStage
import com.kingsmetric.importflow.ImportResult
import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ReviewState
import com.kingsmetric.importflow.StoredScreenshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidPhotoPickerRuntimeWiringTest {

    @Test
    fun `T1 runtime starts in explicit idle state`() {
        val runtime = photoPickerRuntime()

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.value.status)
    }

    @Test
    fun `T2 successful picker import updates runtime with a reviewable draft`() {
        val runtime = photoPickerRuntime(
            recognizeImportedScreenshot = { request ->
                ImportResult.DraftReady(
                    storedScreenshot = StoredScreenshot(
                        id = request.screenshotId,
                        path = request.localPath,
                        originalSourcePath = request.originalUri
                    ),
                    draft = RuntimeFixtures.supportedDraft().copy(
                        screenshotId = request.screenshotId,
                        screenshotPath = request.localPath
                    ),
                    reviewState = ReviewState.fromDraft(
                        RuntimeFixtures.supportedDraft().copy(
                            screenshotId = request.screenshotId,
                            screenshotPath = request.localPath
                        )
                    )
                )
            }
        )

        runtime.handlePickerResult("content://shots/1")

        val status = runtime.state.value.status
        assertTrue(status is ImportRuntimeStatus.ReviewReady)
        status as ImportRuntimeStatus.ReviewReady
        assertEquals("shot-1", status.draft.screenshotId)
        assertEquals("stored/shot-1-content__shots_1.png", status.draft.screenshotPath)
    }

    @Test
    fun `T3 cancelled picker keeps runtime in non-error idle state`() {
        val runtime = photoPickerRuntime()

        runtime.handlePickerResult(null)

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.value.status)
    }

    @Test
    fun `T4 unreadable and storage failures stay attributable in runtime state`() {
        val unreadable = photoPickerRuntime(
            uriStorage = FakeUriScreenshotStorage(unreadableUris = setOf("content://shots/unreadable"))
        )
        val storageFailure = photoPickerRuntime(
            uriStorage = FakeUriScreenshotStorage(copyFailUris = setOf("content://shots/copy-fails"))
        )

        unreadable.handlePickerResult("content://shots/unreadable")
        storageFailure.handlePickerResult("content://shots/copy-fails")

        assertEquals(
            ImportRuntimeStatus.SourceFailed("The selected screenshot could not be imported. Try another image."),
            unreadable.state.value.status
        )
        assertEquals(
            ImportRuntimeStatus.StorageFailed("The screenshot could not be saved locally. Try again."),
            storageFailure.state.value.status
        )
    }

    @Test
    fun `T5 unsupported or failed recognition becomes a retryable runtime failure`() {
        val unsupported = photoPickerRuntime(
            recognizeImportedScreenshot = { ImportResult.Unsupported("Unsupported screenshot.") }
        )
        val failed = photoPickerRuntime(
            recognizeImportedScreenshot = { ImportResult.ImportFailed("Could not extract screenshot data for review.") }
        )

        unsupported.handlePickerResult("content://shots/unsupported")
        failed.handlePickerResult("content://shots/failed")

        assertEquals(
            ImportRuntimeStatus.Unsupported("This screenshot isn't supported. Try another post-match personal stats screenshot."),
            unsupported.state.value.status
        )
        assertEquals(
            ImportRuntimeStatus.Failed("We couldn't read match data from this screenshot. Try another supported screenshot."),
            failed.state.value.status
        )
    }

    @Test
    fun `T6 unexpected recognition exception is converted into retryable runtime failure`() {
        val runtime = photoPickerRuntime(
            recognizeImportedScreenshot = { throw IllegalStateException("unexpected mapper failure") }
        )

        runtime.handlePickerResult("content://shots/crash")

        assertEquals(
            ImportRuntimeStatus.Failed("We couldn't read match data from this screenshot. Try another supported screenshot."),
            runtime.state.value.status
        )
    }

    @Test
    fun `IT1 import failure records diagnostics while preserving retryable runtime failure`() {
        val recorder = RecordingDiagnosticsRecorder()
        val runtime = photoPickerRuntime(
            diagnosticsRecorder = recorder,
            recognizeImportedScreenshot = {
                ImportResult.ImportFailed(
                    "Could not extract screenshot data for review.",
                    ocrText = "胜利\n数据 复盘\n对英雄出: 171.2k"
                )
            }
        )

        runtime.handlePickerResult("content://shots/failed")

        assertEquals(
            ImportRuntimeStatus.Failed("We couldn't read match data from this screenshot. Try another supported screenshot."),
            runtime.state.value.status
        )
        val diagnostics = recorder.events.single()
        assertEquals(DiagnosticsOutcome.RECOGNITION_FAILED, diagnostics.outcome)
        assertEquals("import", diagnostics.metadata["surface"])
        assertEquals("Could not extract screenshot data for review.", diagnostics.metadata["detail"])
        assertEquals("胜利\n数据 复盘\n对英雄出: 171.2k", diagnostics.metadata["ocrText"])
    }

    @Test
    fun `IT2 unsupported screenshot and recognition failure produce distinguishable diagnostics entries`() {
        val unsupportedRecorder = RecordingDiagnosticsRecorder()
        val failedRecorder = RecordingDiagnosticsRecorder()
        val unsupported = photoPickerRuntime(
            diagnosticsRecorder = unsupportedRecorder,
            recognizeImportedScreenshot = { ImportResult.Unsupported("Unsupported screenshot.") }
        )
        val failed = photoPickerRuntime(
            diagnosticsRecorder = failedRecorder,
            recognizeImportedScreenshot = { ImportResult.ImportFailed("Could not extract screenshot data for review.") }
        )

        unsupported.handlePickerResult("content://shots/unsupported")
        failed.handlePickerResult("content://shots/failed")

        assertEquals(DiagnosticsOutcome.UNSUPPORTED_SCREENSHOT, unsupportedRecorder.events.single().outcome)
        assertEquals(DiagnosticsOutcome.RECOGNITION_FAILED, failedRecorder.events.single().outcome)
    }

    @Test
    fun `IT7 diagnostics capture failure does not terminate the main import failure flow`() {
        val runtime = photoPickerRuntime(
            diagnosticsRecorder = ThrowingDiagnosticsRecorder(),
            recognizeImportedScreenshot = { ImportResult.ImportFailed("Could not extract screenshot data for review.") }
        )

        runtime.handlePickerResult("content://shots/failed")

        assertEquals(
            ImportRuntimeStatus.Failed("We couldn't read match data from this screenshot. Try another supported screenshot."),
            runtime.state.value.status
        )
    }
}

private fun photoPickerRuntime(
    uriStorage: UriScreenshotStorage = FakeUriScreenshotStorage(),
    importStarter: (ImportedScreenshotRequest) -> ImportResult = { ImportResult.Cancelled },
    recognizeImportedScreenshot: (ImportedScreenshotRequest) -> ImportResult = { ImportResult.Cancelled },
    diagnosticsRecorder: DiagnosticsRecorder = RecordingDiagnosticsRecorder()
): AndroidPhotoPickerRuntime {
    return AndroidPhotoPickerRuntime(
        adapter = AndroidPhotoPickerImportAdapter(
            uriStorage = uriStorage,
            importStarter = importStarter
        ),
        recognizeImportedScreenshot = recognizeImportedScreenshot,
        diagnosticsRecorder = diagnosticsRecorder
    )
}

private object RuntimeFixtures {
    private val parser = DraftParser()

    fun supportedDraft() = parser.createDraft(
        analysis = MlKitFixtures.supportedAnalysis(
            visibleFields = FieldKey.all
        ),
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png"
    )
}

class RecordingDiagnosticsRecorder : DiagnosticsRecorder {
    val events = mutableListOf<com.kingsmetric.diagnostics.DiagnosticsEvent>()

    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) {
        events += com.kingsmetric.diagnostics.DiagnosticsEvent(
            timestampMillis = events.size.toLong(),
            stage = stage,
            outcome = outcome,
            summary = summary,
            metadata = metadata
        )
    }

    override fun snapshot(): List<com.kingsmetric.diagnostics.DiagnosticsEvent> = events.toList()

    override fun export(): com.kingsmetric.diagnostics.DiagnosticsExport {
        return com.kingsmetric.diagnostics.DiagnosticsExport(
            exportedAtMillis = 0L,
            notice = "",
            entries = emptyList()
        )
    }
}

class ThrowingDiagnosticsRecorder : DiagnosticsRecorder {
    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) {
        throw IllegalStateException("recorder failed")
    }

    override fun snapshot(): List<com.kingsmetric.diagnostics.DiagnosticsEvent> = emptyList()

    override fun export(): com.kingsmetric.diagnostics.DiagnosticsExport {
        throw IllegalStateException("recorder failed")
    }
}
