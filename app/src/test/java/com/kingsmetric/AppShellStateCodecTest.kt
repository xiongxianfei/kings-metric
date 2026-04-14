package com.kingsmetric

import com.kingsmetric.importflow.DraftParser
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShellStateCodecTest {

    @Test
    fun reviewDraftCodec_roundTrips_supported_draft() {
        val draft = CodecFixtures.supportedDraft()

        val restored = ReviewDraftStateCodec.restore(
            ReviewDraftStateCodec.save(draft)
        )

        requireNotNull(restored)
        assertEquals(draft.screenshotId, restored.screenshotId)
        assertEquals(draft.screenshotPath, restored.screenshotPath)
        assertEquals(
            draft.require(FieldKey.HERO).value,
            restored.require(FieldKey.HERO).value
        )
        assertEquals(
            draft.require(FieldKey.KDA).value,
            restored.require(FieldKey.KDA).value
        )
        assertEquals(
            draft.require(FieldKey.KDA).flags,
            restored.require(FieldKey.KDA).flags
        )
    }

    @Test
    fun reviewDraftCodec_roundTrips_null_draft() {
        val restored = ReviewDraftStateCodec.restore(
            ReviewDraftStateCodec.save(null)
        )

        assertNull(restored)
    }
}

private object CodecFixtures {
    private val parser = DraftParser()

    fun supportedDraft(): DraftRecord {
        return parser.createDraft(
            analysis = com.kingsmetric.app.MlKitFixtures.supportedAnalysis(),
            screenshotId = "shot-1",
            screenshotPath = "/data/user/0/com.kingsmetric/files/imports/shot-1.png"
        )
    }
}
