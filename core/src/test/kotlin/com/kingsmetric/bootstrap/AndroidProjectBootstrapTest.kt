package com.kingsmetric.bootstrap

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidProjectBootstrapTest {

    @Test
    fun `T1 gradle configuration exposes an Android application module`() {
        val settings = repoFile("settings.gradle.kts").readText()
        val appBuild = repoFile("app/build.gradle.kts")

        assertTrue(settings.contains("include(\":app\")"))
        assertTrue(Files.exists(appBuild))
        assertTrue(appBuild.readText().contains("com.android.application"))
    }

    @Test
    fun `T2 pure logic source set lives inside the core module`() {
        val coreBuild = repoFile("core/build.gradle.kts")
        val buildText = coreBuild.readText()
        val mainSourceDir = repoFile("core/src/main/kotlin")
        val testSourceDir = repoFile("core/src/test/kotlin")

        assertTrue(Files.exists(coreBuild))
        assertTrue(Files.exists(mainSourceDir))
        assertTrue(Files.exists(testSourceDir))
        assertTrue(!buildText.contains("../src/main/kotlin"))
        assertTrue(!buildText.contains("../src/test/kotlin"))
    }

    @Test
    fun `IT2 app manifest defines application and MainActivity launcher`() {
        val manifest = repoFile("app/src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android:name=\".KingsMetricApplication\""))
        assertTrue(manifest.contains("android:name=\".MainActivity\""))
        assertTrue(manifest.contains("android.intent.action.MAIN"))
        assertTrue(manifest.contains("android.intent.category.LAUNCHER"))
    }

    private fun repoFile(relativePath: String): Path {
        val workingDir = Path.of(System.getProperty("user.dir"))
        val repoRoot = if (workingDir.fileName.toString() == "core") {
            workingDir.parent
        } else {
            workingDir
        }
        return repoRoot.resolve(relativePath)
    }
}
