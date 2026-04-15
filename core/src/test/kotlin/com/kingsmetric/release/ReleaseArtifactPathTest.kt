package com.kingsmetric.release

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseArtifactPathTest {

    @Test
    fun `T1-T7 release artifact contract defines one signed release artifact and fails closed when signing inputs are missing`() {
        val contract = ReleaseArtifactContract.load(resolveRepositoryRoot())

        assertEquals(6, contract.releaseVersionCode)
        assertEquals(":app:assembleRelease", contract.artifactTask)
        assertEquals("app/build/outputs/apk/release/app-release.apk", contract.artifactPath)
        assertTrue(contract.artifactTask.contains("Release"))
        assertTrue(contract.artifactPath.contains("/release/"))
        assertEquals(
            listOf(
                "ANDROID_KEYSTORE_PATH",
                "ANDROID_KEYSTORE_PASSWORD",
                "ANDROID_KEY_ALIAS",
                "ANDROID_KEY_PASSWORD"
            ),
            contract.requiredSigningEnvVars
        )
        assertTrue(contract.validate().isEmpty())
        val readiness = contract.evaluateSigningInputs(emptyMap())
        assertEquals(ReleaseArtifactStatus.Blocked, readiness.status)
        assertTrue(readiness.messages.all { it.contains("Missing signing input") })
    }

    @Test
    fun `T4 release artifact contract rejects debug or unsigned substitutes`() {
        val contract = ReleaseArtifactContract.load(resolveRepositoryRoot())
        val invalidContract = contract.copy(
            artifactTask = ":app:assembleDebug",
            artifactPath = "app/build/outputs/apk/debug/app-debug.apk"
        )

        val issues = invalidContract.validate()

        assertTrue(issues.any { it.contains("release build path", ignoreCase = true) })
        assertTrue(issues.any { it.contains("release artifact path", ignoreCase = true) })
    }
}

class ReleaseArtifactPathIntegrationTest {

    @Test
    fun `IT1-IT4 release artifact contract is wired into the Android build and traceable to the release version`() {
        val repositoryRoot = resolveRepositoryRoot()
        val contract = ReleaseArtifactContract.load(repositoryRoot)
        val buildGradle = Files.readString(repositoryRoot.resolve("app/build.gradle.kts"))

        assertTrue(buildGradle.contains("verifyReleaseSigningInputs"))
        assertTrue(buildGradle.contains("ANDROID_KEYSTORE_PATH"))
        assertTrue(buildGradle.contains("assembleRelease"))
        assertTrue(buildGradle.contains(".github/release-artifact.properties"))
        assertTrue(buildGradle.contains("releaseVersionCode"))
        assertTrue(buildGradle.contains("releaseVersionName"))
    }
}
