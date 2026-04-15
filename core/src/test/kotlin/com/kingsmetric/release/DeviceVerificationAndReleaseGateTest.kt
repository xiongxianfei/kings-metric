package com.kingsmetric.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceVerificationAndReleaseGateTest {

    @Test
    fun `T1 release verification matrix lists all required alpha release checks`() {
        val matrix = ReleaseCandidateVerificationMatrix.default()

        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.MetadataAndDocsAligned })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.SignedArtifactReady })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.JvmVerification })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.AndroidCriticalPathVerified })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.ManualDeviceFlowConfirmed })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.DiagnosticsSupportVerified })
    }

    @Test
    fun `T2 release gate reports ready only when every required release check is completed`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = ReleaseGateCheck.entries.toSet()
        )

        assertEquals(ReleaseGateStatus.Ready, report.status)
    }

    @Test
    fun `T3-T7 release gate blocks when required artifact docs diagnostics device or skipped checks are missing`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = setOf(
                ReleaseGateCheck.JvmVerification,
                ReleaseGateCheck.AndroidCriticalPathVerified
            ),
            blocked = mapOf(
                ReleaseGateCheck.SignedArtifactReady to "Missing signing inputs",
                ReleaseGateCheck.MetadataAndDocsAligned to "README and changelog drift",
                ReleaseGateCheck.DiagnosticsSupportVerified to "Diagnostics viewer/export not verified"
            ),
            skipped = mapOf(
                ReleaseGateCheck.ManualDeviceFlowConfirmed to "No device confirmation recorded"
            )
        )

        assertEquals(ReleaseGateStatus.Blocked, report.status)
        assertTrue(report.messages.any { it.contains("SignedArtifactReady blocked") })
        assertTrue(report.messages.any { it.contains("MetadataAndDocsAligned blocked") })
        assertTrue(report.messages.any { it.contains("DiagnosticsSupportVerified blocked") })
        assertTrue(report.messages.any { it.contains("ManualDeviceFlowConfirmed skipped") })
    }

    @Test
    fun `T7 release gate keeps alpha hero manual review limitation non-blocking`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = ReleaseGateCheck.entries.toSet(),
            alphaKnownLimitations = listOf("Hero may still require manual entry during review.")
        )

        assertEquals(ReleaseGateStatus.Ready, report.status)
    }
}

class DeviceVerificationAndReleaseGateIntegrationTest {

    @Test
    fun `IT1 complete alpha candidate passes the release gate`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = ReleaseGateCheck.entries.toSet(),
            alphaKnownLimitations = listOf("Hero may still require manual entry during review.")
        )

        assertEquals(ReleaseGateStatus.Ready, report.status)
        assertTrue(report.messages.any { it.contains("All required alpha release checks passed") })
    }

    @Test
    fun `IT2 skipped emulator or device verification remains explicitly blocking`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = setOf(
                ReleaseGateCheck.MetadataAndDocsAligned,
                ReleaseGateCheck.SignedArtifactReady,
                ReleaseGateCheck.JvmVerification,
                ReleaseGateCheck.DiagnosticsSupportVerified
            ),
            skipped = mapOf(
                ReleaseGateCheck.AndroidCriticalPathVerified to "Emulator verification skipped",
                ReleaseGateCheck.ManualDeviceFlowConfirmed to "Manual device pass not recorded"
            )
        )

        assertEquals(ReleaseGateStatus.Blocked, report.status)
        assertTrue(report.messages.any { it.contains("AndroidCriticalPathVerified skipped") })
        assertTrue(report.messages.any { it.contains("ManualDeviceFlowConfirmed skipped") })
    }

    @Test
    fun `IT3 candidate with ready artifact but no real device confirmation remains blocked`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = setOf(
                ReleaseGateCheck.MetadataAndDocsAligned,
                ReleaseGateCheck.SignedArtifactReady,
                ReleaseGateCheck.JvmVerification,
                ReleaseGateCheck.AndroidCriticalPathVerified,
                ReleaseGateCheck.DiagnosticsSupportVerified
            )
        )

        assertEquals(ReleaseGateStatus.Blocked, report.status)
        assertTrue(report.messages.any { it.contains("ManualDeviceFlowConfirmed did not pass") })
    }

    @Test
    fun `IT4 candidate with real device confirmation but blocked artifact readiness remains blocked`() {
        val report = FirstAlphaReleaseGate.default().evaluate(
            completed = setOf(
                ReleaseGateCheck.MetadataAndDocsAligned,
                ReleaseGateCheck.JvmVerification,
                ReleaseGateCheck.AndroidCriticalPathVerified,
                ReleaseGateCheck.DiagnosticsSupportVerified,
                ReleaseGateCheck.ManualDeviceFlowConfirmed
            ),
            blocked = mapOf(
                ReleaseGateCheck.SignedArtifactReady to "Release signing readiness blocked"
            )
        )

        assertEquals(ReleaseGateStatus.Blocked, report.status)
        assertTrue(report.messages.any { it.contains("SignedArtifactReady blocked") })
    }

    @Test
    fun `IT5 release verification matrix stays compatible with release contracts`() {
        val matrix = ReleaseCandidateVerificationMatrix.default()

        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.MetadataAndDocsAligned })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.SignedArtifactReady })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.ManualDeviceFlowConfirmed })
        assertTrue(matrix.targets.any { it.check == ReleaseGateCheck.DiagnosticsSupportVerified })
    }
}
