package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalPersistenceAndScreenshotLinkageTest {

    @Test
    fun `T4 missing screenshot linkage invalidates final save`() {
        val workflow = persistenceWorkflow()
        val draft = PersistenceFixtures.validDraft().copy(screenshotId = null)

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.StorageFailed)
        save as SaveResult.StorageFailed
        assertNotNull(save.draft)
    }

    @Test
    fun `IT1 confirmed valid draft saves a local record with screenshot linkage`() {
        val stores = PersistenceStores()
        val workflow = persistenceWorkflow(recordStore = stores.recordStore)
        val draft = PersistenceFixtures.validDraft()

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.Saved)
        save as SaveResult.Saved
        assertEquals(draft.screenshotId, save.record.screenshotId)
        assertEquals(draft.screenshotPath, save.record.screenshotPath)
    }

    @Test
    fun `IT2 save attempt with unresolved required field is blocked and no record is written`() {
        val stores = PersistenceStores()
        val workflow = persistenceWorkflow(recordStore = stores.recordStore)
        val draft = PersistenceFixtures.requiredMissingDraft()

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.Blocked)
        save as SaveResult.Blocked
        assertEquals(draft, save.draft)
        assertTrue(stores.recordStore.saved.isEmpty())
    }

    @Test
    fun `IT3 save attempt with only optional unresolved fields succeeds`() {
        val workflow = persistenceWorkflow()
        val draft = PersistenceFixtures.optionalMissingDraft()

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.Saved)
        save as SaveResult.Saved
        assertNull(save.record.fields[FieldKey.LAST_HITS])
    }

    @Test
    fun `IT4 record write failure reports failure and does not mark the record saved`() {
        val stores = PersistenceStores(recordStore = FakeRecordStore(shouldFail = true))
        val workflow = persistenceWorkflow(recordStore = stores.recordStore)
        val draft = PersistenceFixtures.validDraft()

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.StorageFailed)
        save as SaveResult.StorageFailed
        assertEquals(false, save.saved)
        assertEquals(draft, save.draft)
    }

    @Test
    fun `IT5 screenshot remains available after record persistence failure`() {
        val workflow = persistenceWorkflow(recordStore = FakeRecordStore(shouldFail = true))
        val draft = PersistenceFixtures.validDraft()

        val save = workflow.confirmSave(draft)

        assertTrue(save is SaveResult.StorageFailed)
        save as SaveResult.StorageFailed
        assertNotNull(save.draft)
        assertEquals("stored/1-fixture.png", save.draft?.screenshotPath)
    }
}

private data class PersistenceStores(
    val recordStore: FakeRecordStore = FakeRecordStore()
)

private fun persistenceWorkflow(
    recordStore: FakeRecordStore = FakeRecordStore()
): MatchImportWorkflow {
    return MatchImportWorkflow(
        screenshotStore = FakeScreenshotStore(),
        analyzer = FakeScreenshotAnalyzer(emptyMap()),
        recordStore = recordStore,
        validator = TemplateValidator(),
        parser = DraftParser()
    )
}

private object PersistenceFixtures {
    private val parser = DraftParser()

    fun validDraft(): DraftRecord {
        return parser.createDraft(
            analysis = analysis(),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )
    }

    fun optionalMissingDraft(): DraftRecord {
        return parser.createDraft(
            analysis = analysis(visibleFields = FieldKey.all - FieldKey.LAST_HITS),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )
    }

    fun requiredMissingDraft(): DraftRecord {
        return parser.createDraft(
            analysis = analysis(visibleFields = FieldKey.all - FieldKey.KDA),
            screenshotId = "shot-1",
            screenshotPath = "stored/1-fixture.png"
        )
    }

    private fun analysis(
        visibleFields: Set<FieldKey> = FieldKey.all
    ): ScreenshotAnalysis {
        val rawValues = mapOf(
            FieldKey.RESULT to "胜利",
            FieldKey.HERO to "孙尚香",
            FieldKey.PLAYER_NAME to "King",
            FieldKey.LANE to "对抗路",
            FieldKey.SCORE to "20-10",
            FieldKey.KDA to "11/1/5",
            FieldKey.DAMAGE_DEALT to "12345",
            FieldKey.DAMAGE_SHARE to "34%",
            FieldKey.DAMAGE_TAKEN to "9850",
            FieldKey.DAMAGE_TAKEN_SHARE to "28%",
            FieldKey.TOTAL_GOLD to "12543",
            FieldKey.GOLD_SHARE to "31%",
            FieldKey.PARTICIPATION_RATE to "76%",
            FieldKey.GOLD_FROM_FARMING to "3680",
            FieldKey.LAST_HITS to "71",
            FieldKey.KILL_PARTICIPATION_COUNT to "13",
            FieldKey.CONTROL_DURATION to "00:14",
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS to "10101"
        )
        return ScreenshotAnalysis(
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION),
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }
}
