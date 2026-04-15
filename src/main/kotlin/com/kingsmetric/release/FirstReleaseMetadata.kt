package com.kingsmetric.release

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

enum class ReleaseChannel(val propertyValue: String) {
    AlphaPrerelease("alpha"),
    Stable("stable");

    companion object {
        fun fromPropertyValue(value: String): ReleaseChannel {
            return entries.firstOrNull { channel ->
                channel.propertyValue.equals(value.trim(), ignoreCase = true)
            } ?: error("Unsupported release channel: $value")
        }
    }
}

data class FirstReleaseMetadata(
    val channel: ReleaseChannel,
    val versionTag: String,
    val repositoryDescription: String,
    val repositoryTagline: String,
    val releasePositioning: String,
    val intendedAudience: String,
    val supportedScopeStatements: List<String>,
    val rejectionStatement: String,
    val knownLimitations: List<String>
) {
    fun isStableRelease(): Boolean {
        return channel == ReleaseChannel.Stable || !versionTag.contains("-")
    }

    fun validate(): List<String> {
        val issues = mutableListOf<String>()
        if (channel != ReleaseChannel.AlphaPrerelease) {
            issues += "First release must use alpha prerelease channel."
        }
        if (!versionTag.matches(PRERELEASE_TAG_PATTERN)) {
            issues += "First release version tag must remain prerelease, not stable."
        }

        validateProductIdentity(issues)
        validateSupportedScope(issues)
        validateAudience(issues)
        validateKnownLimitations(issues)
        validateUnsupportedClaims(issues)

        return issues
    }

    private fun validateProductIdentity(issues: MutableList<String>) {
        if (!repositoryDescription.contains("Android", ignoreCase = true)) {
            issues += "Repository description must identify the app as Android."
        }
        if (!combinedSurfaces().contains("local", ignoreCase = true)) {
            issues += "GitHub-facing metadata must describe the app as local-first."
        }
        if (!combinedSurfaces().contains("on-device", ignoreCase = true)) {
            issues += "GitHub-facing metadata must mention on-device processing."
        }
        if (!combinedSurfaces().contains("review", ignoreCase = true)) {
            issues += "GitHub-facing metadata must mention review before save."
        }
    }

    private fun validateSupportedScope(issues: MutableList<String>) {
        if (supportedScopeStatements != EXPECTED_SUPPORTED_SCOPE) {
            issues += "GitHub-facing metadata must describe the current supported scope exactly."
        }
        if (!rejectionStatement.contains("unsupported screenshots are rejected", ignoreCase = true)) {
            issues += "GitHub-facing metadata must state that unsupported screenshots are rejected."
        }
    }

    private fun validateAudience(issues: MutableList<String>) {
        val normalizedAudience = intendedAudience.lowercase()
        if (!normalizedAudience.contains("alpha") || !normalizedAudience.contains("tester")) {
            issues += "First-release metadata should make the alpha tester audience explicit."
        }
    }

    private fun validateKnownLimitations(issues: MutableList<String>) {
        if (knownLimitations.none { limitation ->
                limitation.contains("hero", ignoreCase = true) &&
                    limitation.contains("manual", ignoreCase = true)
            }
        ) {
            issues += "Known limitations must keep the hero manual-review limitation explicit."
        }
    }

    private fun validateUnsupportedClaims(issues: MutableList<String>) {
        val combinedText = combinedSurfaces().lowercase()
        FORBIDDEN_CLAIMS.forEach { forbidden ->
            if (combinedText.contains(forbidden)) {
                issues += "Unsupported claim detected: $forbidden"
            }
        }
    }

    private fun combinedSurfaces(): String {
        return buildString {
            append(repositoryDescription)
            append(' ')
            append(repositoryTagline)
            append(' ')
            append(releasePositioning)
            append(' ')
            append(intendedAudience)
            append(' ')
            append(supportedScopeStatements.joinToString(" "))
            append(' ')
            append(rejectionStatement)
            append(' ')
            append(knownLimitations.joinToString(" "))
        }
    }

    companion object {
        private val PRERELEASE_TAG_PATTERN = Regex("""v\d+\.\d+\.\d+-[A-Za-z0-9.]+""")

        private val EXPECTED_SUPPORTED_SCOPE = listOf(
            "one supported Simplified Chinese post-match detailed-data screenshot",
            "local screenshot import",
            "on-device processing",
            "required review before final save"
        )

        private val FORBIDDEN_CLAIMS = listOf(
            "multi-template",
            "multiple templates",
            "non-chinese",
            "cloud sync",
            "server ocr",
            "production-ready",
            "production ready",
            "complete",
            "every honor of kings screenshot",
            "fully automatic hero extraction"
        )

        fun load(repositoryRoot: Path = resolveRepositoryRoot()): FirstReleaseMetadata {
            val propertiesPath = repositoryRoot.resolve(".github/release-metadata.properties")
            val properties = Properties()
            Files.newInputStream(propertiesPath).use { stream ->
                properties.load(stream)
            }
            return FirstReleaseMetadata(
                channel = ReleaseChannel.fromPropertyValue(properties.required("channel")),
                versionTag = properties.required("versionTag"),
                repositoryDescription = properties.required("repositoryDescription"),
                repositoryTagline = properties.required("repositoryTagline"),
                releasePositioning = properties.required("releasePositioning"),
                intendedAudience = properties.required("intendedAudience"),
                supportedScopeStatements = properties.required("supportedScopeStatements")
                    .split('|')
                    .map(String::trim)
                    .filter(String::isNotEmpty),
                rejectionStatement = properties.required("rejectionStatement"),
                knownLimitations = properties.required("knownLimitations")
                    .split('|')
                    .map(String::trim)
                    .filter(String::isNotEmpty)
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
        ?: error("Missing required release metadata property: $key")
}
