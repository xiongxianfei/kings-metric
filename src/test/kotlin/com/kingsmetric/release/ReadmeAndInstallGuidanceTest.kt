package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadmeAndInstallGuidanceTest {

    @Test
    fun `T1 repository contains a top-level README md`() {
        val readmePath = resolveRepositoryRoot().resolve("README.md")

        assertTrue(Files.exists(readmePath))
    }

    @Test
    fun `T2-T13 README covers app introduction build-run guidance install guidance supported scope local-first behavior and key limitations`() {
        val readme = Files.readString(resolveRepositoryRoot().resolve("README.md"))

        assertTrue(readme.contains("Honor of Kings Match Tracker"))
        assertTrue(readme.contains("Android", ignoreCase = true))
        assertTrue(readme.contains("local-first", ignoreCase = true))
        assertTrue(readme.contains("on-device", ignoreCase = true))
        assertTrue(readme.contains("alpha", ignoreCase = true))
        assertTrue(readme.contains("## Build", ignoreCase = true))
        assertTrue(readme.contains("gradlew.bat --no-daemon :core:test"))
        assertTrue(readme.contains("gradlew.bat --no-daemon :app:assembleDebug"))
        assertTrue(readme.contains("## Run", ignoreCase = true))
        assertTrue(readme.contains("Android Studio", ignoreCase = true))
        assertTrue(readme.contains("one supported Simplified Chinese post-match detailed-data screenshot"))
        assertTrue(readme.contains("unsupported screenshots are rejected", ignoreCase = true))
        assertTrue(readme.contains("Hero may still require manual entry during review", ignoreCase = true))
        assertTrue(readme.contains("GitHub Releases", ignoreCase = true))
        assertTrue(readme.contains("import", ignoreCase = true))
        assertTrue(readme.contains("review", ignoreCase = true))
        assertTrue(readme.contains("save", ignoreCase = true))
        assertTrue(readme.contains("## Not In This Release"))
        assertTrue(readme.contains("cloud sync", ignoreCase = true))
        assertTrue(readme.contains("server OCR", ignoreCase = true))
        assertTrue(readme.contains("non-Chinese", ignoreCase = true))
    }
}

class ReadmeAndInstallGuidanceIntegrationTest {

    @Test
    fun `IT1-IT5 README stays consistent with repository build-run path release metadata and release notes`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val readme = Files.readString(repositoryRoot.resolve("README.md"))
        val releaseNotes = Files.readString(
            repositoryRoot.resolve("docs/releases/${metadata.versionTag}.md")
        )

        assertTrue(readme.contains("settings.gradle.kts"))
        assertTrue(readme.contains("app/"))
        metadata.supportedScopeStatements.forEach { statement ->
            assertTrue(readme.contains(statement))
            assertTrue(releaseNotes.contains(statement))
        }
        metadata.knownLimitations.forEach { limitation ->
            assertTrue(readme.contains(limitation))
            assertTrue(releaseNotes.contains(limitation))
        }
        assertTrue(readme.contains("GitHub Releases", ignoreCase = true))
        assertTrue(releaseNotes.contains("prerelease", ignoreCase = true))
    }
}
