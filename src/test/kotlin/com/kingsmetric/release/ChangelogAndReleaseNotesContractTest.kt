package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogAndReleaseNotesContractTest {

    @Test
    fun `T1-T10 release notes source exists and contains release identity included changes supported scope and limitations`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val releaseNotesPath = repositoryRoot.resolve("docs/releases/${metadata.versionTag}.md")
        val releaseNotes = Files.readString(releaseNotesPath)

        assertTrue(Files.exists(releaseNotesPath))
        assertTrue(releaseNotesPath.fileName.toString().contains(metadata.versionTag))
        assertTrue(releaseNotes.contains(metadata.versionTag))
        assertTrue(releaseNotes.contains("alpha", ignoreCase = true))
        assertTrue(releaseNotes.contains("## What's Included", ignoreCase = true))
        metadata.supportedScopeStatements.forEach { statement ->
            assertTrue(releaseNotes.contains(statement))
        }
        assertTrue(releaseNotes.contains("unsupported screenshots are rejected", ignoreCase = true))
        metadata.knownLimitations.forEach { limitation ->
            assertTrue(releaseNotes.contains(limitation))
        }
        assertTrue(releaseNotes.contains("what changed", ignoreCase = true) || releaseNotes.contains("what's included", ignoreCase = true))
        assertTrue(!releaseNotes.contains("cloud sync", ignoreCase = true))
        assertTrue(!releaseNotes.contains("server OCR", ignoreCase = true))
    }
}

class ChangelogAndReleaseNotesContractIntegrationTest {

    @Test
    fun `IT1-IT5 release notes stay consistent with repository metadata and README`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val readme = Files.readString(repositoryRoot.resolve("README.md"))
        val releaseNotes = Files.readString(repositoryRoot.resolve("docs/releases/${metadata.versionTag}.md"))

        assertTrue(releaseNotes.contains(metadata.versionTag))
        assertTrue(releaseNotes.contains("alpha", ignoreCase = true))
        metadata.supportedScopeStatements.forEach { statement ->
            assertTrue(readme.contains(statement))
            assertTrue(releaseNotes.contains(statement))
        }
        metadata.knownLimitations.forEach { limitation ->
            assertTrue(readme.contains(limitation))
            assertTrue(releaseNotes.contains(limitation))
        }
        assertTrue(!releaseNotes.contains("production ready", ignoreCase = true))
        assertTrue(!releaseNotes.contains("stable release", ignoreCase = true))
    }
}
