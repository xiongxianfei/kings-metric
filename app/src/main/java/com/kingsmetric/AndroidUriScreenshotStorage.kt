package com.kingsmetric

import android.content.Context
import android.net.Uri
import com.kingsmetric.app.StoredUriScreenshot
import com.kingsmetric.app.UnreadableUriException
import com.kingsmetric.app.UriScreenshotStorage
import java.io.File
import java.util.UUID

class AndroidUriScreenshotStorage(
    private val context: Context,
    private val screenshotDirectoryName: String = "imports",
    private val idProvider: () -> String = { UUID.randomUUID().toString() }
) : UriScreenshotStorage {

    override fun copyFromUri(uri: String): StoredUriScreenshot {
        val parsedUri = Uri.parse(uri)
        val inputStream = try {
            context.contentResolver.openInputStream(parsedUri)
        } catch (_: Exception) {
            throw UnreadableUriException()
        } ?: throw UnreadableUriException()
        inputStream.use { input ->
            val screenshotId = idProvider()
            val targetDirectory = File(context.filesDir, screenshotDirectoryName)
            if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                throw IllegalStateException("Could not create target directory.")
            }
            val targetFile = File(targetDirectory, "$screenshotId${fileExtension(parsedUri)}")
            try {
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } catch (error: IllegalStateException) {
                throw error
            } catch (_: Exception) {
                throw IllegalStateException("Could not copy uri into app storage.")
            }
            return StoredUriScreenshot(
                screenshotId = screenshotId,
                localPath = targetFile.absolutePath,
                originalUri = uri
            )
        }
    }

    private fun fileExtension(uri: Uri): String {
        val lastPathSegment = uri.lastPathSegment.orEmpty()
        val dotIndex = lastPathSegment.lastIndexOf('.')
        return if (dotIndex >= 0) {
            lastPathSegment.substring(dotIndex)
        } else {
            ".img"
        }
    }
}
