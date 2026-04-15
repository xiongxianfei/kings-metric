package com.kingsmetric.release

enum class VerificationLayer {
    Build,
    Jvm,
    Instrumented,
    ComposeUi
}

enum class VerificationEnvironment {
    Host,
    Emulator
}

data class VerificationTarget(
    val name: String,
    val layer: VerificationLayer,
    val environment: VerificationEnvironment,
    val gradleTask: String
)

data class AndroidVerificationMatrix(
    val targets: List<VerificationTarget>
) {
    companion object {
        fun default(): AndroidVerificationMatrix {
            return AndroidVerificationMatrix(
                targets = listOf(
                    VerificationTarget(
                        name = "Debug Assemble",
                        layer = VerificationLayer.Build,
                        environment = VerificationEnvironment.Host,
                        gradleTask = ":app:assembleDebug"
                    ),
                    VerificationTarget(
                        name = "Core JVM Tests",
                        layer = VerificationLayer.Jvm,
                        environment = VerificationEnvironment.Host,
                        gradleTask = ":core:test"
                    ),
                    VerificationTarget(
                        name = "Android Instrumented Smoke Test",
                        layer = VerificationLayer.Instrumented,
                        environment = VerificationEnvironment.Emulator,
                        gradleTask = ":app:connectedDebugAndroidTest"
                    ),
                    VerificationTarget(
                        name = "Compose UI Smoke Test",
                        layer = VerificationLayer.ComposeUi,
                        environment = VerificationEnvironment.Emulator,
                        gradleTask = ":app:connectedDebugAndroidTest"
                    )
                )
            )
        }
    }
}

enum class CriticalFlow {
    ImportIntakeAndStorage,
    RecognitionIntegration,
    ReviewAndSaveFlow,
    RoomPersistence,
    HistoryAndDashboardRendering
}

enum class VerificationCheck {
    DebugAssemble,
    JvmTests,
    InstrumentedTests,
    ComposeUiTests
}

enum class ReleaseReadinessStatus {
    Ready,
    Blocked
}

data class ReleaseReadinessReport(
    val status: ReleaseReadinessStatus,
    val messages: List<String>
)

data class ReleaseReadinessGate(
    val requiredFlows: List<CriticalFlow>,
    val requiresDebugAssemble: Boolean,
    val requiresJvmVerification: Boolean,
    val requiresInstrumentedVerification: Boolean,
    val requiresComposeUiVerification: Boolean
) {
    fun evaluate(
        completed: Set<VerificationCheck>,
        skipped: Map<VerificationCheck, String> = emptyMap()
    ): ReleaseReadinessReport {
        val missingMessages = mutableListOf<String>()

        requireCheck(
            required = requiresDebugAssemble,
            check = VerificationCheck.DebugAssemble,
            completed = completed,
            skipped = skipped,
            missingMessages = missingMessages
        )
        requireCheck(
            required = requiresJvmVerification,
            check = VerificationCheck.JvmTests,
            completed = completed,
            skipped = skipped,
            missingMessages = missingMessages
        )
        requireCheck(
            required = requiresInstrumentedVerification,
            check = VerificationCheck.InstrumentedTests,
            completed = completed,
            skipped = skipped,
            missingMessages = missingMessages
        )
        requireCheck(
            required = requiresComposeUiVerification,
            check = VerificationCheck.ComposeUiTests,
            completed = completed,
            skipped = skipped,
            missingMessages = missingMessages
        )

        return if (missingMessages.isEmpty()) {
            ReleaseReadinessReport(
                status = ReleaseReadinessStatus.Ready,
                messages = listOf("All required Android verification checks passed.")
            )
        } else {
            ReleaseReadinessReport(
                status = ReleaseReadinessStatus.Blocked,
                messages = missingMessages
            )
        }
    }

    private fun requireCheck(
        required: Boolean,
        check: VerificationCheck,
        completed: Set<VerificationCheck>,
        skipped: Map<VerificationCheck, String>,
        missingMessages: MutableList<String>
    ) {
        if (!required || check in completed) {
            return
        }
        val skipReason = skipped[check]
        if (skipReason != null) {
            missingMessages += "$check skipped: $skipReason"
        } else {
            missingMessages += "$check did not pass."
        }
    }

    companion object {
        fun default(): ReleaseReadinessGate {
            return ReleaseReadinessGate(
                requiredFlows = listOf(
                    CriticalFlow.ImportIntakeAndStorage,
                    CriticalFlow.RecognitionIntegration,
                    CriticalFlow.ReviewAndSaveFlow,
                    CriticalFlow.RoomPersistence,
                    CriticalFlow.HistoryAndDashboardRendering
                ),
                requiresDebugAssemble = true,
                requiresJvmVerification = true,
                requiresInstrumentedVerification = true,
                requiresComposeUiVerification = true
            )
        }
    }
}
