package com.kingsmetric.release

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidAppIconResourceContractTest {

    @Test
    fun `T4-T8 launcher icon contract declares branded manifest entries and exported resources`() {
        val repositoryRoot = repositoryRoot()
        val manifest = Files.readString(repositoryRoot.resolve("app/src/main/AndroidManifest.xml"))

        assertTrue(manifest.contains("android:icon=\"@mipmap/ic_launcher\""))
        assertTrue(manifest.contains("android:roundIcon=\"@mipmap/ic_launcher_round\""))

        val requiredResources = listOf(
            "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
            "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
            "app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml",
            "app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml",
            "app/src/main/res/drawable-mdpi/ic_launcher_background.png",
            "app/src/main/res/drawable-mdpi/ic_launcher_foreground.png",
            "app/src/main/res/drawable-mdpi/ic_launcher_monochrome.png",
            "app/src/main/res/drawable-hdpi/ic_launcher_background.png",
            "app/src/main/res/drawable-hdpi/ic_launcher_foreground.png",
            "app/src/main/res/drawable-hdpi/ic_launcher_monochrome.png",
            "app/src/main/res/drawable-xhdpi/ic_launcher_background.png",
            "app/src/main/res/drawable-xhdpi/ic_launcher_foreground.png",
            "app/src/main/res/drawable-xhdpi/ic_launcher_monochrome.png",
            "app/src/main/res/drawable-xxhdpi/ic_launcher_background.png",
            "app/src/main/res/drawable-xxhdpi/ic_launcher_foreground.png",
            "app/src/main/res/drawable-xxhdpi/ic_launcher_monochrome.png",
            "app/src/main/res/drawable-xxxhdpi/ic_launcher_background.png",
            "app/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.png",
            "app/src/main/res/drawable-xxxhdpi/ic_launcher_monochrome.png",
            "app/src/main/res/mipmap-mdpi/ic_launcher.png",
            "app/src/main/res/mipmap-mdpi/ic_launcher_round.png",
            "app/src/main/res/mipmap-hdpi/ic_launcher.png",
            "app/src/main/res/mipmap-hdpi/ic_launcher_round.png",
            "app/src/main/res/mipmap-xhdpi/ic_launcher.png",
            "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png",
            "app/src/main/res/mipmap-xxhdpi/ic_launcher.png",
            "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png",
            "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png",
            "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png",
        )

        requiredResources.forEach { relativePath ->
            assertTrue("Missing launcher icon resource: $relativePath", Files.exists(repositoryRoot.resolve(relativePath)))
        }

        val adaptiveIconV26 = Files.readString(repositoryRoot.resolve("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml"))
        val adaptiveRoundIconV33 = Files.readString(repositoryRoot.resolve("app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml"))

        assertTrue(adaptiveIconV26.contains("@drawable/ic_launcher_background"))
        assertTrue(adaptiveIconV26.contains("@drawable/ic_launcher_foreground"))
        assertFalse(adaptiveIconV26.contains("monochrome"))
        assertTrue(adaptiveRoundIconV33.contains("@drawable/ic_launcher_background"))
        assertTrue(adaptiveRoundIconV33.contains("@drawable/ic_launcher_foreground"))
        assertTrue(adaptiveRoundIconV33.contains("@drawable/ic_launcher_monochrome"))
    }

    @Test
    fun `IT2-IT4 launcher icon outputs stay repo-owned and reproducible`() {
        val repositoryRoot = repositoryRoot()
        val iconReadme = repositoryRoot.resolve("docs/design/app-icon/README.md")
        val exportScript = repositoryRoot.resolve("docs/design/app-icon/export_android_resources.py")
        val sourceGenerator = repositoryRoot.resolve("docs/design/app-icon/generate_sources.py")

        assertTrue(Files.exists(iconReadme))
        assertTrue(Files.exists(exportScript))
        assertTrue(Files.exists(sourceGenerator))

        val readmeText = Files.readString(iconReadme)
        assertTrue(readmeText.contains("export_android_resources.py"))
        assertTrue(readmeText.contains("ic_launcher_background"))
        assertTrue(readmeText.contains("ic_launcher_foreground"))
        assertTrue(readmeText.contains("ic_launcher_monochrome"))
    }

    private fun repositoryRoot(): Path {
        val start = Paths.get("").toAbsolutePath().normalize()
        return generateSequence(start) { current -> current.parent }
            .firstOrNull { candidate ->
                Files.exists(candidate.resolve("settings.gradle.kts"))
            }
            ?: error("Could not locate repository root from $start")
    }
}
