package com.kingsmetric.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidVerificationMatrixTest {

    @Test
    fun `T1 verification matrix lists JVM instrumented and Compose UI test targets`() {
        val matrix = AndroidVerificationMatrix.default()

        assertTrue(matrix.targets.any { it.layer == VerificationLayer.Jvm })
        assertTrue(matrix.targets.any { it.layer == VerificationLayer.Instrumented })
        assertTrue(matrix.targets.any { it.layer == VerificationLayer.ComposeUi })
    }

    @Test
    fun `T2 release-readiness gate defines required checks explicitly`() {
        val gate = ReleaseReadinessGate.default()

        assertEquals(
            listOf(
                CriticalFlow.ImportIntakeAndStorage,
                CriticalFlow.RecognitionIntegration,
                CriticalFlow.ReviewAndSaveFlow,
                CriticalFlow.RoomPersistence,
                CriticalFlow.HistoryAndDashboardRendering
            ),
            gate.requiredFlows
        )
        assertTrue(gate.requiresDebugAssemble)
        assertTrue(gate.requiresJvmVerification)
    }
}

class AndroidVerificationMatrixIntegrationTest {

    @Test
    fun `IT1 CI matrix includes debug assemble`() {
        val matrix = AndroidVerificationMatrix.default()

        assertTrue(matrix.targets.any { target ->
            target.layer == VerificationLayer.Build &&
                target.gradleTask == ":app:assembleDebug"
        })
    }

    @Test
    fun `IT2 CI matrix includes JVM unit verification`() {
        val matrix = AndroidVerificationMatrix.default()

        assertTrue(matrix.targets.any { target ->
            target.layer == VerificationLayer.Jvm &&
                target.gradleTask == ":core:test"
        })
    }

    @Test
    fun `IT3 CI matrix includes emulator backed verification for critical flows`() {
        val matrix = AndroidVerificationMatrix.default()

        assertTrue(matrix.targets.any { target ->
            target.layer == VerificationLayer.Instrumented &&
                target.environment == VerificationEnvironment.Emulator
        })
        assertTrue(matrix.targets.any { target ->
            target.layer == VerificationLayer.ComposeUi &&
                target.environment == VerificationEnvironment.Emulator
        })
    }

    @Test
    fun `IT4 release report makes skipped Android verification visible`() {
        val report = ReleaseReadinessGate.default().evaluate(
            completed = setOf(
                VerificationCheck.DebugAssemble,
                VerificationCheck.JvmTests
            ),
            skipped = mapOf(
                VerificationCheck.InstrumentedTests to "Emulator capacity unavailable",
                VerificationCheck.ComposeUiTests to "Compose UI suite not configured"
            )
        )

        assertEquals(ReleaseReadinessStatus.Blocked, report.status)
        assertTrue(report.messages.any { it.contains("InstrumentedTests") })
        assertTrue(report.messages.any { it.contains("ComposeUiTests") })
    }
}
