package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseExecutionTest {

    @Test
    fun `T1 release publication contract reports ready only when prerelease artifact docs and gate are ready`() {
        val metadata = alphaMetadata()
        val artifactContract = alphaArtifactContract()
        val artifactReadiness = ReleaseArtifactReadiness(
            status = ReleaseArtifactStatus.Ready,
            messages = listOf("All required signing inputs are present.")
        )
        val releaseGate = FirstAlphaReleaseGate.default().evaluate(
            completed = ReleaseGateCheck.entries.toSet(),
            alphaKnownLimitations = metadata.knownLimitations
        )

        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = metadata,
            artifactContract = artifactContract,
            artifactReadiness = artifactReadiness,
            releaseGate = releaseGate,
            releaseNotesExists = true,
            changelogContainsRelease = true
        )

        assertEquals(GitHubReleaseExecutionStatus.Ready, plan.status)
        assertTrue(plan.prerelease)
    }

    @Test
    fun `T2 release publication contract blocks when the release gate is blocked`() {
        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = alphaMetadata(),
            artifactContract = alphaArtifactContract(),
            artifactReadiness = ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Ready,
                messages = listOf("All required signing inputs are present.")
            ),
            releaseGate = FirstAlphaReleaseGate.default().evaluate(
                completed = setOf(
                    ReleaseGateCheck.MetadataAndDocsAligned,
                    ReleaseGateCheck.SignedArtifactReady,
                    ReleaseGateCheck.JvmVerification
                ),
                skipped = mapOf(
                    ReleaseGateCheck.ManualDeviceFlowConfirmed to "Manual device pass not recorded"
                )
            ),
            releaseNotesExists = true,
            changelogContainsRelease = true
        )

        assertEquals(GitHubReleaseExecutionStatus.Blocked, plan.status)
        assertTrue(plan.messages.any { it.contains("release gate") })
    }

    @Test
    fun `T3 release publication contract blocks when release artifact is blocked or mismatched`() {
        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = alphaMetadata(),
            artifactContract = alphaArtifactContract(artifactPath = "app/build/outputs/apk/debug/app-debug.apk"),
            artifactReadiness = ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Blocked,
                messages = listOf("Missing signing input: ANDROID_KEYSTORE_PATH")
            ),
            releaseGate = FirstAlphaReleaseGate.default().evaluate(
                completed = ReleaseGateCheck.entries.toSet()
            ),
            releaseNotesExists = true,
            changelogContainsRelease = true
        )

        assertEquals(GitHubReleaseExecutionStatus.Blocked, plan.status)
        assertTrue(plan.messages.any { it.contains("release artifact") })
    }

    @Test
    fun `T4 release publication contract blocks when release notes or changelog linkage is missing`() {
        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = alphaMetadata(),
            artifactContract = alphaArtifactContract(),
            artifactReadiness = ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Ready,
                messages = listOf("All required signing inputs are present.")
            ),
            releaseGate = FirstAlphaReleaseGate.default().evaluate(
                completed = ReleaseGateCheck.entries.toSet()
            ),
            releaseNotesExists = false,
            changelogContainsRelease = false
        )

        assertEquals(GitHubReleaseExecutionStatus.Blocked, plan.status)
        assertTrue(plan.messages.any { it.contains("Release notes") })
        assertTrue(plan.messages.any { it.contains("CHANGELOG") })
    }

    @Test
    fun `T5 release publication contract keeps manual hero alpha limitation non-blocking`() {
        val metadata = alphaMetadata(
            knownLimitations = listOf("Hero may still require manual entry during review.")
        )

        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = metadata,
            artifactContract = alphaArtifactContract(),
            artifactReadiness = ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Ready,
                messages = listOf("All required signing inputs are present.")
            ),
            releaseGate = FirstAlphaReleaseGate.default().evaluate(
                completed = ReleaseGateCheck.entries.toSet(),
                alphaKnownLimitations = metadata.knownLimitations
            ),
            releaseNotesExists = true,
            changelogContainsRelease = true
        )

        assertEquals(GitHubReleaseExecutionStatus.Ready, plan.status)
    }
}

class GitHubReleaseExecutionIntegrationTest {

    @Test
    fun `IT1 current alpha release surfaces can form a publishable prerelease plan when release gate is ready`() {
        val repositoryRoot = resolveRepositoryRoot()
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val artifactContract = ReleaseArtifactContract.load(repositoryRoot)

        val plan = GitHubReleaseExecutionContract.default().evaluate(
            metadata = metadata,
            artifactContract = artifactContract,
            artifactReadiness = ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Ready,
                messages = listOf("All required signing inputs are present.")
            ),
            releaseGate = FirstAlphaReleaseGate.default().evaluate(
                completed = ReleaseGateCheck.entries.toSet(),
                alphaKnownLimitations = metadata.knownLimitations
            ),
            releaseNotesExists = Files.exists(repositoryRoot.resolve(artifactContract.releaseNotesPath)),
            changelogContainsRelease = Files.readString(repositoryRoot.resolve("CHANGELOG.md"))
                .contains(metadata.versionTag)
        )

        assertEquals(GitHubReleaseExecutionStatus.Ready, plan.status)
        assertEquals(metadata.versionTag, plan.releaseTag)
        assertEquals(artifactContract.artifactPath, plan.artifactPath)
    }

    @Test
    fun `IT2 release workflow requires explicit release gate confirmation inputs before publication`() {
        val repositoryRoot = resolveRepositoryRoot()
        val workflow = Files.readString(repositoryRoot.resolve(".github/workflows/release-alpha.yml"))

        assertTrue(workflow.contains("workflow_dispatch:"))
        assertTrue(workflow.contains("confirm_release_gate_ready"))
        assertTrue(workflow.contains("confirm_manual_device_flow"))
        assertTrue(workflow.contains("confirm_diagnostics_support_ready"))
        assertTrue(workflow.contains("prerelease: true"))
    }

    @Test
    fun `IT3 release workflow stays aligned with tracked metadata and release notes path`() {
        val repositoryRoot = resolveRepositoryRoot()
        val workflow = Files.readString(repositoryRoot.resolve(".github/workflows/release-alpha.yml"))
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val artifactContract = ReleaseArtifactContract.load(repositoryRoot)

        assertTrue(workflow.contains(".github/release-metadata.properties"))
        assertTrue(workflow.contains("docs/releases/\${VERSION_TAG}.md"))
        assertTrue(artifactContract.releaseNotesPath.endsWith("${metadata.versionTag}.md"))
    }
}

private fun alphaMetadata(
    knownLimitations: List<String> = listOf("Hero may still require manual entry during review.")
): FirstReleaseMetadata {
    return FirstReleaseMetadata(
        channel = ReleaseChannel.AlphaPrerelease,
        versionTag = "v0.1.0-alpha.3",
        repositoryDescription = "Alpha Android app for local Honor of Kings screenshot import, review, and local save.",
        repositoryTagline = "Alpha local-first Honor of Kings tracker for one supported Chinese results screenshot.",
        releasePositioning = "Alpha prerelease for early testers. Imports one supported Simplified Chinese post-match detailed-data screenshot, processes it on-device, requires review before final save, and rejects unsupported screenshots.",
        intendedAudience = "Early alpha testers validating the first GitHub release.",
        supportedScopeStatements = listOf(
            "one supported Simplified Chinese post-match detailed-data screenshot",
            "local screenshot import",
            "on-device processing",
            "required review before final save"
        ),
        rejectionStatement = "Unsupported screenshots are rejected.",
        knownLimitations = knownLimitations
    )
}

private fun alphaArtifactContract(
    artifactPath: String = "app/build/outputs/apk/release/app-release.apk"
): ReleaseArtifactContract {
    return ReleaseArtifactContract(
        releaseVersionCode = 3,
        releaseVersionName = "0.1.0-alpha.3",
        artifactTask = ":app:assembleRelease",
        artifactPath = artifactPath,
        requiredSigningEnvVars = listOf(
            "ANDROID_KEYSTORE_PATH",
            "ANDROID_KEYSTORE_PASSWORD",
            "ANDROID_KEY_ALIAS",
            "ANDROID_KEY_PASSWORD"
        ),
        verificationTasks = listOf(":core:test", ":app:assembleRelease"),
        releaseNotesPath = "docs/releases/v0.1.0-alpha.3.md"
    )
}
