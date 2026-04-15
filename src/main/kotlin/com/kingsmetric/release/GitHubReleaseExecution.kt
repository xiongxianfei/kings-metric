package com.kingsmetric.release

enum class GitHubReleaseExecutionStatus {
    Ready,
    Blocked
}

data class GitHubReleasePublicationPlan(
    val status: GitHubReleaseExecutionStatus,
    val releaseTag: String,
    val artifactPath: String,
    val prerelease: Boolean,
    val messages: List<String>
)

data class GitHubReleaseExecutionContract(
    val expectedChannel: ReleaseChannel
) {
    fun evaluate(
        metadata: FirstReleaseMetadata,
        artifactContract: ReleaseArtifactContract,
        artifactReadiness: ReleaseArtifactReadiness,
        releaseGate: ReleaseGateReport,
        releaseNotesExists: Boolean,
        changelogContainsRelease: Boolean
    ): GitHubReleasePublicationPlan {
        val messages = mutableListOf<String>()

        if (expectedChannel != ReleaseChannel.AlphaPrerelease ||
            metadata.channel != ReleaseChannel.AlphaPrerelease ||
            metadata.isStableRelease()
        ) {
            messages += "GitHub release execution must stay prerelease for the first alpha release."
        }

        metadata.validate().forEach { issue ->
            messages += "metadata/docs issue: $issue"
        }

        artifactContract.validate().forEach { issue ->
            messages += "release artifact issue: $issue"
        }

        if (artifactReadiness.status != ReleaseArtifactStatus.Ready) {
            artifactReadiness.messages.forEach { issue ->
                messages += "release artifact blocked: $issue"
            }
        }

        if (!artifactContract.artifactPath.replace('\\', '/').contains("/release/")) {
            messages += "release artifact path must point to the release artifact."
        }

        if (!artifactContract.releaseNotesPath.endsWith("${metadata.versionTag}.md")) {
            messages += "Release notes path must match the release tag."
        }

        if (!releaseNotesExists) {
            messages += "Release notes are missing for the current release."
        }

        if (!changelogContainsRelease) {
            messages += "CHANGELOG does not contain the current release entry."
        }

        if (releaseGate.status != ReleaseGateStatus.Ready) {
            messages += "GitHub release execution is blocked because the release gate is not ready."
        }

        return if (messages.isEmpty()) {
            val successMessages = mutableListOf("GitHub alpha prerelease is ready for publication.")
            if (metadata.knownLimitations.any { limitation ->
                    limitation.contains("hero", ignoreCase = true) &&
                        limitation.contains("manual", ignoreCase = true)
                }
            ) {
                successMessages += "Known alpha limitation retained: hero may still require manual entry."
            }
            GitHubReleasePublicationPlan(
                status = GitHubReleaseExecutionStatus.Ready,
                releaseTag = metadata.versionTag,
                artifactPath = artifactContract.artifactPath,
                prerelease = true,
                messages = successMessages
            )
        } else {
            GitHubReleasePublicationPlan(
                status = GitHubReleaseExecutionStatus.Blocked,
                releaseTag = metadata.versionTag,
                artifactPath = artifactContract.artifactPath,
                prerelease = true,
                messages = messages
            )
        }
    }

    companion object {
        fun default(): GitHubReleaseExecutionContract {
            return GitHubReleaseExecutionContract(
                expectedChannel = ReleaseChannel.AlphaPrerelease
            )
        }
    }
}
