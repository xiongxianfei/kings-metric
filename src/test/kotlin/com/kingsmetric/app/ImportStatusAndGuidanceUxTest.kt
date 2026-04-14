package com.kingsmetric.app

import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportStatusAndGuidanceUxTest {

    private val mapper = ImportScreenUiStateMapper()

    @Test
    fun `T1 idle state exposes one clear primary import action`() {
        val model = mapper.map(ImportRuntimeStatus.Idle)

        assertEquals("Import Screenshot", model.primaryActionLabel)
        assertTrue(model.supportedScreenshotHint.contains("Supported"))
        assertFalse(model.showContinueReview)
    }

    @Test
    fun `T2 state mapper returns distinct user-facing states for import outcomes`() {
        val states = listOf(
            mapper.map(ImportRuntimeStatus.Idle),
            mapper.map(ImportRuntimeStatus.InProgress),
            mapper.map(ImportRuntimeStatus.Unsupported("unsupported")),
            mapper.map(ImportRuntimeStatus.SourceFailed("source failed")),
            mapper.map(ImportRuntimeStatus.StorageFailed("storage failed")),
            mapper.map(ImportRuntimeStatus.Failed("failed")),
            mapper.map(ImportRuntimeStatus.ReviewReady(ImportUxFixtures.supportedDraft()))
        )

        assertEquals(7, states.map { it.title to it.guidance }.toSet().size)
    }

    @Test
    fun `T3 unsupported source and storage outcomes map to distinct guidance and retry actions`() {
        val unsupported = mapper.map(ImportRuntimeStatus.Unsupported("unsupported"))
        val source = mapper.map(ImportRuntimeStatus.SourceFailed("source failed"))
        val storage = mapper.map(ImportRuntimeStatus.StorageFailed("storage failed"))

        assertEquals("Unsupported Screenshot", unsupported.title)
        assertEquals("Can't Read Selected Screenshot", source.title)
        assertEquals("Couldn't Save Screenshot", storage.title)
        assertEquals("Import Screenshot", unsupported.primaryActionLabel)
        assertEquals("Import Screenshot", source.primaryActionLabel)
        assertEquals("Import Screenshot", storage.primaryActionLabel)
    }

    @Test
    fun `T4 review ready state exposes explicit continue to review action`() {
        val model = mapper.map(ImportRuntimeStatus.ReviewReady(ImportUxFixtures.supportedDraft()))

        assertTrue(model.showContinueReview)
        assertEquals("Continue Review", model.continueReviewLabel)
    }

    @Test
    fun `T5 picker cancellation maps to a non-error usable import state`() {
        val runtime = AndroidPhotoPickerRuntime(
            adapter = AndroidPhotoPickerImportAdapter(
                uriStorage = FakeUriScreenshotStorage(),
                importStarter = { com.kingsmetric.importflow.ImportResult.Cancelled }
            ),
            recognizeImportedScreenshot = { com.kingsmetric.importflow.ImportResult.Cancelled }
        )

        runtime.beginImport()
        runtime.handlePickerResult(null)

        assertEquals(ImportRuntimeStatus.Idle, runtime.state.value.status)
    }
}

private object ImportUxFixtures {
    private val parser = DraftParser()

    fun supportedDraft() = parser.createDraft(
        analysis = MlKitFixtures.supportedAnalysis(visibleFields = FieldKey.all),
        screenshotId = "shot-1",
        screenshotPath = "stored/shot-1.png"
    )
}
