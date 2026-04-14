package com.kingsmetric

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AndroidUriScreenshotStorageTest {

    @Test
    fun copyFromUri_copiesReadableUriIntoAppManagedStorage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val source = File.createTempFile("picker-source", ".png", context.cacheDir).apply {
            writeText("fixture-image")
        }
        val storage = AndroidUriScreenshotStorage(
            context = context,
            screenshotDirectoryName = "test-imports",
            idProvider = { "shot-1" }
        )

        val result = storage.copyFromUri(Uri.fromFile(source).toString())

        assertEquals("shot-1", result.screenshotId)
        assertTrue(File(result.localPath).exists())
        assertEquals("fixture-image", File(result.localPath).readText())
        assertEquals(Uri.fromFile(source).toString(), result.originalUri)
        File(result.localPath).delete()
        source.delete()
    }

    @Test
    fun copyFromUri_throwsUnreadableWhenUriCannotBeOpened() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val storage = AndroidUriScreenshotStorage(
            context = context,
            screenshotDirectoryName = "test-imports",
            idProvider = { "shot-missing" }
        )

        try {
            storage.copyFromUri("file:///missing/runtime-import.png")
        } catch (_: UnreadableUriException) {
            assertTrue(true)
            return
        }

        assertFalse("Expected unreadable uri exception", true)
    }
}
