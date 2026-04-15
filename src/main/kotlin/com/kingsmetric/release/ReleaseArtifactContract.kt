package com.kingsmetric.release

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

enum class ReleaseArtifactStatus {
    Ready,
    Blocked
}

data class ReleaseArtifactReadiness(
    val status: ReleaseArtifactStatus,
    val messages: List<String>
)

data class ReleaseArtifactContract(
    val releaseVersionName: String,
    val artifactTask: String,
    val artifactPath: String,
    val requiredSigningEnvVars: List<String>,
    val verificationTasks: List<String>,
    val releaseNotesPath: String
) {
    fun validate(): List<String> {
        val issues = mutableListOf<String>()
        if (!artifactTask.contains("Release")) {
            issues += "Release artifact task must use a release build path."
        }
        if (!artifactPath.replace('\\', '/').contains("/release/")) {
            issues += "Release artifact path must point to a release artifact path."
        }
        if (requiredSigningEnvVars.isEmpty()) {
            issues += "Release artifact contract must define required signing inputs."
        }
        if (!verificationTasks.contains(":core:test")) {
            issues += "Release artifact contract must include core JVM verification."
        }
        if (!releaseNotesPath.endsWith(".md")) {
            issues += "Release artifact contract must point to markdown release notes."
        }
        return issues
    }

    fun evaluateSigningInputs(values: Map<String, String?>): ReleaseArtifactReadiness {
        val missing = requiredSigningEnvVars.filter { name ->
            values[name].isNullOrBlank()
        }
        return if (missing.isEmpty()) {
            ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Ready,
                messages = listOf("All required signing inputs are present.")
            )
        } else {
            ReleaseArtifactReadiness(
                status = ReleaseArtifactStatus.Blocked,
                messages = missing.map { name -> "Missing signing input: $name" }
            )
        }
    }

    companion object {
        fun load(repositoryRoot: Path = resolveRepositoryRoot()): ReleaseArtifactContract {
            val properties = Properties()
            Files.newInputStream(repositoryRoot.resolve(".github/release-artifact.properties")).use { stream ->
                properties.load(stream)
            }
            return ReleaseArtifactContract(
                releaseVersionName = properties.required("releaseVersionName"),
                artifactTask = properties.required("artifactTask"),
                artifactPath = properties.required("artifactPath"),
                requiredSigningEnvVars = properties.required("requiredSigningEnvVars")
                    .split('|')
                    .map(String::trim)
                    .filter(String::isNotEmpty),
                verificationTasks = properties.required("verificationTasks")
                    .split('|')
                    .map(String::trim)
                    .filter(String::isNotEmpty),
                releaseNotesPath = properties.required("releaseNotesPath")
            )
        }

        private fun resolveRepositoryRoot(): Path {
            val start = Paths.get("").toAbsolutePath().normalize()
            return generateSequence(start) { current -> current.parent }
                .firstOrNull { candidate ->
                    Files.exists(candidate.resolve("settings.gradle.kts"))
                }
                ?: error("Could not locate repository root from $start")
        }
    }
}

private fun Properties.required(key: String): String {
    return getProperty(key)?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: error("Missing required release artifact property: $key")
}
