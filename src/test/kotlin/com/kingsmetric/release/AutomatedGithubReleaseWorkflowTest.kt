package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomatedGithubReleaseWorkflowTest {

    @Test
    fun `T1-T8 automated workflow exists publishes prerelease and fails closed without required inputs`() {
        val repositoryRoot = resolveRepositoryRoot()
        val workflowPath = repositoryRoot.resolve(".github/workflows/release-alpha.yml")
        val workflow = Files.readString(workflowPath)
        val artifactContract = ReleaseArtifactContract.load(repositoryRoot)

        assertTrue(Files.exists(workflowPath))
        assertTrue(workflow.contains("workflow_dispatch:"))
        assertTrue(workflow.contains("prerelease: true"))
        assertTrue(workflow.contains(artifactContract.artifactTask))
        assertTrue(workflow.contains(artifactContract.artifactPath.replace("\\", "/")))
        assertTrue(workflow.contains(":core:test"))
        assertTrue(workflow.contains("ANDROID_KEYSTORE_BASE64"))
        assertTrue(workflow.contains("ANDROID_KEYSTORE_PASSWORD"))
        assertTrue(workflow.contains("ANDROID_KEY_ALIAS"))
        assertTrue(workflow.contains("ANDROID_KEY_PASSWORD"))
        assertTrue(workflow.contains("exit 1"))
    }
}

class AutomatedGithubReleaseWorkflowIntegrationTest {

    @Test
    fun `IT1-IT5 workflow aligns with metadata artifact contract and release notes`() {
        val repositoryRoot = resolveRepositoryRoot()
        val workflow = Files.readString(repositoryRoot.resolve(".github/workflows/release-alpha.yml"))
        val metadata = FirstReleaseMetadata.load(repositoryRoot)
        val artifactContract = ReleaseArtifactContract.load(repositoryRoot)

        assertTrue(workflow.contains(".github/release-metadata.properties"))
        assertTrue(workflow.contains(artifactContract.artifactPath.replace("\\", "/")))
        assertTrue(workflow.contains("docs/releases/\${VERSION_TAG}.md"))
        assertTrue(workflow.contains("prerelease: true"))
        assertTrue(workflow.contains("softprops/action-gh-release"))
        assertTrue(workflow.contains("gh api"))
        assertTrue(workflow.contains("repositoryDescription"))
    }
}
