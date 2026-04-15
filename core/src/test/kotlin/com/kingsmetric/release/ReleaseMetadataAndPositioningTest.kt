package com.kingsmetric.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseMetadataAndPositioningTest {

    @Test
    fun `T1 first-release metadata model marks the first GitHub release as alpha prerelease rather than stable`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertEquals(ReleaseChannel.AlphaPrerelease, metadata.channel)
        assertFalse(metadata.isStableRelease())
    }

    @Test
    fun `T2 version-tag mapping for the first release uses a prerelease-compatible shape and rejects stable-first-release wording`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertEquals("v0.1.0-alpha.5", metadata.versionTag)
        val stableAttempt = metadata.copy(versionTag = "v1.0.0")

        assertTrue(stableAttempt.validate().any { issue ->
            issue.contains("prerelease", ignoreCase = true) ||
                issue.contains("stable", ignoreCase = true)
        })
    }

    @Test
    fun `T3 repository description-tagline mapping identifies the product as an Android app for local on-device screenshot import and review`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertTrue(metadata.repositoryDescription.contains("Android", ignoreCase = true))
        assertTrue(metadata.repositoryDescription.contains("local", ignoreCase = true))
        assertTrue(metadata.releasePositioning.contains("on-device", ignoreCase = true))
        assertTrue(metadata.releasePositioning.contains("review", ignoreCase = true))
    }

    @Test
    fun `T4 supported-scope metadata includes only the current supported screenshot scope and required review behavior`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertEquals(
            listOf(
                "one supported Simplified Chinese post-match detailed-data screenshot",
                "local screenshot import",
                "on-device processing",
                "required review before final save"
            ),
            metadata.supportedScopeStatements
        )
    }

    @Test
    fun `T5 metadata contract excludes unsupported claims such as multi-template non-Chinese cloud sync and server OCR support`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertTrue(metadata.validate().none { issue ->
            issue.contains("unsupported claim", ignoreCase = true)
        })

        val invalidMetadata = metadata.copy(
            repositoryDescription = metadata.repositoryDescription + " Supports cloud sync and multi-template OCR."
        )

        assertTrue(invalidMetadata.validate().any { issue ->
            issue.contains("unsupported claim", ignoreCase = true)
        })
    }

    @Test
    fun `T6 positioning contract remains compatible with the known hero manual-review limitation and does not imply fully automatic extraction`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertTrue(metadata.knownLimitations.any { limitation ->
            limitation.contains("hero", ignoreCase = true) &&
                limitation.contains("manual", ignoreCase = true)
        })
        assertTrue(metadata.validate().none { issue ->
            issue.contains("automatic hero", ignoreCase = true)
        })
    }
}

class ReleaseMetadataAndPositioningIntegrationTest {

    @Test
    fun `IT1 repository description-tagline and release-positioning metadata resolve to the same maturity level and supported-scope statement`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertTrue(metadata.validate().isEmpty())
    }

    @Test
    fun `IT2 if one GitHub-facing metadata surface uses stable or overbroad wording while another uses alpha-narrow-scope wording the release metadata check flags that mismatch as blocking`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())
        val mismatchedMetadata = metadata.copy(
            repositoryTagline = "Production-ready Android OCR tracker for every Honor of Kings screenshot."
        )

        val issues = mismatchedMetadata.validate()

        assertTrue(issues.any { issue ->
            issue.contains("stable", ignoreCase = true) ||
                issue.contains("unsupported claim", ignoreCase = true) ||
                issue.contains("scope mismatch", ignoreCase = true)
        })
    }

    @Test
    fun `IT3 metadata contract makes the intended audience explicit enough for an early tester to understand that the first release is alpha quality`() {
        val metadata = FirstReleaseMetadata.load(resolveRepositoryRoot())

        assertTrue(metadata.intendedAudience.contains("alpha", ignoreCase = true))
        assertTrue(metadata.intendedAudience.contains("tester", ignoreCase = true))
    }
}
