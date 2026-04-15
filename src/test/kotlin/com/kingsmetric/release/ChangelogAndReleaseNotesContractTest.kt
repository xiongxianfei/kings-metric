package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogAndReleaseNotesContractTest {

    @Test
    fun `T1-T13 root changelog and release notes use dated release format and contain required content`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val changelogPath = repositoryRoot.resolve("CHANGELOG.md")
        val changelog = Files.readString(changelogPath)
        val releaseNotesPath = repositoryRoot.resolve("docs/releases/${metadata.versionTag}.md")
        val releaseNotes = Files.readString(releaseNotesPath)
        val expectedHeaderPattern = Regex("""## \[${Regex.escape(metadata.versionTag)}\] - \d{4}-\d{2}-\d{2}""")

        assertTrue(Files.exists(changelogPath))
        assertTrue(changelog.contains(metadata.versionTag))
        assertTrue(changelog.contains("alpha", ignoreCase = true))
        assertTrue(expectedHeaderPattern.containsMatchIn(changelog))
        val releaseHeaders = Regex("""## \[v[^\]]+\] - \d{4}-\d{2}-\d{2}""").findAll(changelog).toList()
        assertTrue(releaseHeaders.isNotEmpty())
        assertTrue(releaseHeaders.first().value.contains(metadata.versionTag))
        assertTrue(changelog.contains("### Features"))
        assertTrue(changelog.contains("### Internal"))
        assertTrue(Files.exists(releaseNotesPath))
        assertTrue(releaseNotesPath.fileName.toString().contains(metadata.versionTag))
        assertTrue(releaseNotes.contains(metadata.versionTag))
        assertTrue(releaseNotes.contains("alpha", ignoreCase = true))
        assertTrue(releaseNotes.contains("## What's Included", ignoreCase = true))
        metadata.supportedScopeStatements.forEach { statement ->
            assertTrue(changelog.contains(statement))
            assertTrue(releaseNotes.contains(statement))
        }
        assertTrue(changelog.contains("unsupported screenshots are rejected", ignoreCase = true))
        assertTrue(releaseNotes.contains("unsupported screenshots are rejected", ignoreCase = true))
        metadata.knownLimitations.forEach { limitation ->
            assertTrue(changelog.contains(limitation))
            assertTrue(releaseNotes.contains(limitation))
        }
        assertTrue(changelog.contains("features", ignoreCase = true) || changelog.contains("what's included", ignoreCase = true))
        assertTrue(releaseNotes.contains("what changed", ignoreCase = true) || releaseNotes.contains("what's included", ignoreCase = true))
        assertTrue(!releaseNotes.contains("cloud sync", ignoreCase = true))
        assertTrue(!releaseNotes.contains("server OCR", ignoreCase = true))
    }
}

class ChangelogAndReleaseNotesContractIntegrationTest {

    @Test
    fun `IT1-IT5 root changelog and release notes stay consistent with repository metadata and README`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val readme = Files.readString(repositoryRoot.resolve("README.md"))
        val changelog = Files.readString(repositoryRoot.resolve("CHANGELOG.md"))
        val releaseNotes = Files.readString(repositoryRoot.resolve("docs/releases/${metadata.versionTag}.md"))

        assertTrue(changelog.contains(metadata.versionTag))
        assertTrue(changelog.contains("alpha", ignoreCase = true))
        assertTrue(releaseNotes.contains(metadata.versionTag))
        assertTrue(releaseNotes.contains("alpha", ignoreCase = true))
        metadata.supportedScopeStatements.forEach { statement ->
            assertTrue(readme.contains(statement))
            assertTrue(changelog.contains(statement))
            assertTrue(releaseNotes.contains(statement))
        }
        metadata.knownLimitations.forEach { limitation ->
            assertTrue(readme.contains(limitation))
            assertTrue(changelog.contains(limitation))
            assertTrue(releaseNotes.contains(limitation))
        }
        assertTrue(!changelog.contains("production ready", ignoreCase = true))
        assertTrue(!changelog.contains("stable release", ignoreCase = true))
        assertTrue(!releaseNotes.contains("production ready", ignoreCase = true))
        assertTrue(!releaseNotes.contains("stable release", ignoreCase = true))
    }
}
