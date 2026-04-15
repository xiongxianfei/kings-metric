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
        maxDimensionPx: Int
    ): Bitmap {
        val bounds = boundsReader.read(path)
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw BitmapDecodeException()
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(
                width = bounds.width,
                height = bounds.height,
                maxDimensionPx = maxDimensionPx
            )
        }

        return BitmapFactory.decodeFile(path, options) ?: throw BitmapDecodeException()
    }
}

internal fun sampleSizeFor(
    width: Int,
    height: Int,
    maxDimensionPx: Int
): Int {
    var sampleSize = 1
    var sampledWidth = width
    var sampledHeight = height

    while (sampledWidth > maxDimensionPx || sampledHeight > maxDimensionPx) {
        sampleSize *= 2
        sampledWidth = width / sampleSize
        sampledHeight = height / sampleSize
    }

    return sampleSize.coerceAtLeast(1)
}
