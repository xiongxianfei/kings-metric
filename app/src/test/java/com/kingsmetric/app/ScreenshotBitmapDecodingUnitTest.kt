package com.kingsmetric.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenshotBitmapDecodingUnitTest {

    @Test
    fun portraitPreviewSampling_keepsEnoughWidthForTheReviewSurface() {
        val sampleSize = sampleSizeForPreview(
            width = 1080,
            height = 2400,
            targetWidthPx = 1080,
            targetHeightPx = 660
        )

        assertEquals(1, sampleSize)
    }

    @Test
    fun largePortraitPreviewSampling_stillDownsamplesWhenSourceFarExceedsSurface() {
        val sampleSize = sampleSizeForPreview(
            width = 4096,
            height = 8192,
            targetWidthPx = 1080,
            targetHeightPx = 660
        )

        assertEquals(2, sampleSize)
    }
}
