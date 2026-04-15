package com.kingsmetric.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory

data class ImageBounds(
    val width: Int,
    val height: Int
)

interface ImageBoundsReader {
    fun read(path: String): ImageBounds
}

class AndroidImageBoundsReader : ImageBoundsReader {
    override fun read(path: String): ImageBounds {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        return ImageBounds(
            width = options.outWidth,
            height = options.outHeight
        )
    }
}

class AndroidPreviewBitmapLoader(
    private val boundsReader: ImageBoundsReader = AndroidImageBoundsReader()
) {
    fun load(
        path: String,
        targetWidthPx: Int,
        targetHeightPx: Int
    ): Bitmap {
        val bounds = boundsReader.read(path)
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw BitmapDecodeException()
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeForPreview(
                width = bounds.width,
                height = bounds.height,
                targetWidthPx = targetWidthPx,
                targetHeightPx = targetHeightPx
            )
        }

        return BitmapFactory.decodeFile(path, options) ?: throw BitmapDecodeException()
    }
}

internal fun sampleSizeForPreview(
    width: Int,
    height: Int,
    targetWidthPx: Int,
    targetHeightPx: Int
): Int {
    if (targetWidthPx <= 0 || targetHeightPx <= 0) {
        return 1
    }

    var sampleSize = 1

    while ((width / (sampleSize * 2)) >= targetWidthPx &&
        (height / (sampleSize * 2)) >= targetHeightPx
    ) {
        sampleSize *= 2
    }

    return sampleSize.coerceAtLeast(1)
}
