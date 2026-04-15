package com.kingsmetric

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kingsmetric.app.AndroidBitmapLoader
import com.kingsmetric.app.AndroidMlKitTextRecognizer
import com.kingsmetric.app.MlKitRecognitionAdapter
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class AndroidMlKitRecognitionAdapterTest {

    @Test
    fun generatedSupportedChineseStatsImage_isRecognizedIntoDraftableResult() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createScreenshot(
            context = context,
            name = "supported-mlkit.png",
            lines = listOf(
                "胜利",
                "20 vs 10",
                "数据",
                "不败、菜鸟 发育路 11/1/5",
                "对英雄输出 171.2k 输出占比 35.3%",
                "承受英雄伤害 82.1k 承伤占比 20.3%",
                "经济 13.1k 经济占比 24%",
                "打野经济 1.4k 补刀数 50",
                "参团率 80.0% 控制时长 3s",
                "对塔伤害 10.9k"
            )
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)

        assertTrue(result.toString(), result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
        assertEquals("11/1/5", result.draft.require(FieldKey.KDA).value)
        assertEquals("35.3%", result.draft.require(FieldKey.DAMAGE_SHARE).value)
        assertEquals("80.0%", result.draft.require(FieldKey.PARTICIPATION_RATE).value)
        assertTrue(result.reviewState.highlightedFields.contains(FieldKey.HERO))
    }

    @Test
    fun missingImageFile_returnsClearImportFailure() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val adapter = adapter(context)

        val result = adapter.recognize(File(context.cacheDir, "missing-mlkit.png").absolutePath)

        assertTrue(result is ImportResult.ImportFailed)
    }

    @Test
    fun croppedChineseImage_withoutRequiredSections_isRejected() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createScreenshot(
            context = context,
            name = "cropped-mlkit.png",
            lines = listOf(
                "胜利",
                "数据",
                "不败、菜鸟 发育路 11/1/5"
            )
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)

        assertTrue(result is ImportResult.Unsupported)
    }

    @Test
    fun realSupportedFixtureImage_isRecognizedIntoReviewableDraft() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = copyAssetToCache(
            context = context,
            assetName = "detailed-data.jpg"
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)

        if (result !is ImportResult.DraftReady) {
            throw AssertionError("Expected DraftReady but was $result")
        }
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
        assertEquals("20 vs 10", result.draft.require(FieldKey.SCORE).value)
        assertEquals("11/1/5", result.draft.require(FieldKey.KDA).value)
        assertEquals("35.3%", result.draft.require(FieldKey.DAMAGE_SHARE).value)
        assertEquals("24%", result.draft.require(FieldKey.GOLD_SHARE).value)
        assertEquals("80.0%", result.draft.require(FieldKey.PARTICIPATION_RATE).value)
        assertTrue(result.reviewState.highlightedFields.contains(FieldKey.HERO))
        assertTrue(result.reviewState.blockingFields.contains(FieldKey.HERO))
        assertTrue(result.reviewState.blockingFields.toString(), result.reviewState.blockingFields.all { it == FieldKey.HERO })
    }

    @Test
    fun generatedSupportedDeviceStyleImage_withRealChineseLabels_isRecognizedIntoReviewableDraft() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createScreenshot(
            context = context,
            name = "supported-device-style.png",
            lines = listOf(
                "胜利",
                "20 vs 10",
                "总览 数据 复盘",
                "不败、菜鸟 13.1 11/1/5",
                "对英雄输出 171.2k 输出占比 35.3%",
                "承受英雄伤害 82.1k 承伤占比 20.3%",
                "经济 13.1k 经济占比 24%",
                "打野经济 1.4k 补刀数 50",
                "参团率 80.0% 控制时长 3s",
                "对塔伤害 10.9k"
            )
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)
        assertTrue(result.toString(), result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
        assertEquals("20 vs 10", result.draft.require(FieldKey.SCORE).value)
        assertEquals("11/1/5", result.draft.require(FieldKey.KDA).value)
        assertEquals("35.3%", result.draft.require(FieldKey.DAMAGE_SHARE).value)
        assertEquals("24%", result.draft.require(FieldKey.GOLD_SHARE).value)
        assertEquals("80.0%", result.draft.require(FieldKey.PARTICIPATION_RATE).value)
    }

    @Test
    fun readableChineseSupportedScreenshot_isRecognizedIntoReviewableDraft() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = createScreenshot(
            context = context,
            name = "supported-readable-chinese.png",
            lines = listOf(
                "胜利",
                "20 vs 10",
                "总览 数据 复盘",
                "不吹、菜鸟 发育路 13.1 11/1/5",
                "对英雄输出 171.2k 输出占比 35.3%",
                "承受英雄伤害 82.1k 承伤占比 20.3%",
                "经济 13.1k 经济占比 24%",
                "打野经济 1.4k 补刀数 50",
                "参团率 80.0% 控制时长 3s",
                "对塔伤害 10.9k"
            )
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)

        assertTrue(result.toString(), result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
        assertEquals("11/1/5", result.draft.require(FieldKey.KDA).value)
        assertEquals("35.3%", result.draft.require(FieldKey.DAMAGE_SHARE).value)
        assertEquals("24%", result.draft.require(FieldKey.GOLD_SHARE).value)
        assertEquals("80.0%", result.draft.require(FieldKey.PARTICIPATION_RATE).value)
    }

    @Test
    fun downscaledSupportedFixtureImage_isRecognizedIntoReviewableDraft() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val screenshot = downscaleAssetToCache(
            context = context,
            assetName = "detailed-data.jpg",
            targetWidth = 384,
            targetHeight = 683
        )
        val adapter = adapter(context)

        val result = adapter.recognize(screenshot.absolutePath)

        assertTrue(result.toString(), result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals("victory", result.draft.require(FieldKey.RESULT).value)
        assertEquals("11/1/5", result.draft.require(FieldKey.KDA).value)
        assertTrue(result.reviewState.blockingFields.contains(FieldKey.DAMAGE_SHARE))
    }
}

private fun adapter(context: android.content.Context): MlKitRecognitionAdapter {
    return MlKitRecognitionAdapter(
        bitmapLoader = AndroidBitmapLoader(),
        recognizer = AndroidMlKitTextRecognizer(context)
    )
}

private fun createScreenshot(
    context: android.content.Context,
    name: String,
    lines: List<String>
): File {
    val bitmap = Bitmap.createBitmap(1400, 2200, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    val paint = Paint().apply {
        color = Color.BLACK
        textSize = 72f
        isAntiAlias = true
    }

    var y = 140f
    lines.forEach { line ->
        canvas.drawText(line, 80f, y, paint)
        y += 140f
    }

    val file = File(context.cacheDir, name)
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    return file
}

private fun copyAssetToCache(
    context: android.content.Context,
    assetName: String
): File {
    val target = File(context.cacheDir, assetName)
    InstrumentationRegistry.getInstrumentation().context.assets.open(assetName).use { input ->
        FileOutputStream(target).use { output ->
            input.copyTo(output)
        }
    }
    return target
}

private fun downscaleAssetToCache(
    context: android.content.Context,
    assetName: String,
    targetWidth: Int,
    targetHeight: Int
): File {
    val target = File(context.cacheDir, "scaled-$assetName")
    InstrumentationRegistry.getInstrumentation().context.assets.open(assetName).use { input ->
        val original = BitmapFactory.decodeStream(input)
        val scaled = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, true)
        FileOutputStream(target).use { output ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 95, output)
        }
        original.recycle()
        scaled.recycle()
    }
    return target
}
