package com.kingsmetric

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingsmetric.app.AndroidImageBoundsReader
import com.kingsmetric.app.AndroidPreviewBitmapLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ScreenshotBitmapDecodingTest {

    @Test
    fun largeScreenshotBounds_areReadWithoutNeedingFullPreviewDecode() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createLargeScreenshot(context, "large-bounds.png", 4096, 8192)

        val bounds = AndroidImageBoundsReader().read(screenshot.absolutePath)

        assertEquals(4096, bounds.width)
        assertEquals(8192, bounds.height)
    }

    @Test
    fun reviewPreviewLoader_downsamplesLargeScreenshotToBoundedDimensions() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createLargeScreenshot(context, "large-preview.png", 4096, 8192)

        val bitmap = AndroidPreviewBitmapLoader().load(
            path = screenshot.absolutePath,
            maxDimensionPx = 1080
        )

        assertTrue(bitmap.width <= 1080)
        assertTrue(bitmap.height <= 1080)
        bitmap.recycle()
    }
}

private fun createLargeScreenshot(
    context: android.content.Context,
    name: String,
    width: Int,
    height: Int
): File {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(Color.WHITE)
    val file = File(context.cacheDir, name)
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    bitmap.recycle()
    return file
}
