package com.kingsmetric.release

enum class ReleaseGateCheck {
    MetadataAndDocsAligned,
    SignedArtifactReady,
    JvmVerification,
    AndroidCriticalPathVerified,
    ManualDeviceFlowConfirmed
}

enum class ReleaseGateStatus {
    Ready,
    Blocked
}

data class ReleaseGateTarget(
    val check: ReleaseGateCheck,
    val label: String
)

data class ReleaseCandidateVerificationMatrix(
    val targets: List<ReleaseGateTarget>
) {
    companion object {
        fun default(): ReleaseCandidateVerificationMatrix {
            return ReleaseCandidateVerificationMatrix(
                targets = listOf(
                    ReleaseGateTarget(
                        check = ReleaseGateCheck.MetadataAndDocsAligned,
                        label = "Release-facing metadata and docs aligned"
                    ),
                    ReleaseGateTarget(
                        check = ReleaseGateCheck.SignedArtifactReady,
                        label = "Signed release artifact ready"
                    ),
                    ReleaseGateTarget(
                        check = ReleaseGateCheck.JvmVerification,
                        label = "JVM verification passed"
                    ),
                    ReleaseGateTarget(
                        check = ReleaseGateCheck.AndroidCriticalPathVerified,
                        label = "Android critical path verified"
                    ),
                    ReleaseGateTarget(
                        check = ReleaseGateCheck.ManualDeviceFlowConfirmed,
                        label = "Manual device flow confirmed"
                    )
                )
            )
        }
    }
}

data class ReleaseGateReport(
    val status: ReleaseGateStatus,
    val passedChecks: Set<ReleaseGateCheck>,
    val messages: List<String>
)

data class FirstAlphaReleaseGate(
    val matrix: ReleaseCandidateVerificationMatrix
) {
    fun evaluate(
        completed: Set<ReleaseGateCheck>,
        blocked: Map<ReleaseGateCheck, String> = emptyMap(),
        skipped: Map<ReleaseGateCheck, String> = emptyMap(),
        alphaKnownLimitations: List<String> = emptyList()
    ): ReleaseGateReport {
        val messages = mutableListOf<String>()
        val requiredChecks = matrix.targets.map { it.check }
        val passedChecks = completed.intersect(requiredChecks.toSet())

        requiredChecks.forEach { check ->
            val blockedReason = blocked[check]
            when {
                blockedReason != null -> messages += "$check blocked: $blockedReason"
                check in skipped -> messages += "$check skipped: ${skipped.getValue(check)}"
                check !in completed -> messages += "$check did not pass."
            }
        }

        val relevantKnownLimitations = alphaKnownLimitations.filter { limitation ->
            limitation.contains("hero", ignoreCase = true) &&
                limitation.contains("manual", ignoreCase = true)
        }

        if (messages.isEmpty()) {
            if (relevantKnownLimitations.isEmpty()) {
                messages += "All required alpha release checks passed."
            } else {
                messages += "All required alpha release checks passed."
                messages += "Known alpha limitation retained: ${relevantKnownLimitations.joinToString("; ")}"
            }
            return ReleaseGateReport(
                status = ReleaseGateStatus.Ready,
                passedChecks = passedChecks,
                messages = messages
            )
        }

        return ReleaseGateReport(
            status = ReleaseGateStatus.Blocked,
            passedChecks = passedChecks,
            messages = messages
        )
    }

    companion object {
        fun default(): FirstAlphaReleaseGate {
            return FirstAlphaReleaseGate(
                matrix = ReleaseCandidateVerificationMatrix.default()
            )
        }
    }
}
