package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateValidationTest {

    private val validator = TemplateValidator()

    @Test
    fun `T1 accept screenshot when all required anchors are present`() {
        val result = validator.validate(Fixtures.supportedFullAnalysis())
        assertTrue(result is TemplateValidationResult.Supported)
    }

    @Test
    fun `T2 reject screenshot when result header anchor is missing`() {
        val result = validator.validate(Fixtures.unsupportedCroppedAnalysis(anchors = setOf(Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED)))
        assertUnsupported(result, "result header")
    }

    @Test
    fun `T3 reject screenshot when selected data tab anchor is missing`() {
        val result = validator.validate(Fixtures.supportedFullAnalysis(anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD)))
        assertUnsupported(result, "data tab")
    }

    @Test
    fun `T4 reject screenshot when required stat sections are not all visible`() {
        val result = validator.validate(Fixtures.supportedFullAnalysis(visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY)))
        assertUnsupported(result, "team participation")
    }

    @Test
    fun `T5 reject screenshot from unsupported language or regional variant`() {
        val result = validator.validate(Fixtures.supportedOtherLanguageAnalysis())
        assertUnsupported(result, "supported personal-stats template")
    }
}

class ParsingAndDraftTest {

    private val parser = DraftParser()

    @Test
    fun `T6 normalize result into victory`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(resultText = "胜利"))
        assertEquals("victory", draft.require(FieldKey.RESULT).value)
    }

    @Test
    fun `T7 preserve score left-to-right exactly as displayed`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(score = "20-10"))
        assertEquals("20-10", draft.require(FieldKey.SCORE).value)
    }

    @Test
    fun `T8 preserve lane exactly as displayed on the screenshot`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(lane = "发育路"))
        assertEquals("发育路", draft.require(FieldKey.LANE).value)
    }

    @Test
    fun `T9 parse kda only in kills deaths assists form`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(kda = "11/1/5"))
        assertEquals("11/1/5", draft.require(FieldKey.KDA).value)
        assertTrue(draft.require(FieldKey.KDA).flags.isEmpty())
    }

    @Test
    fun `T10 distinguish required fields from optional fields in the draft schema`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis())
        assertEquals(FieldKey.required.size, draft.fields.values.count { it.required })
        assertEquals(FieldKey.optional.size, draft.fields.values.count { !it.required })
    }

    @Test
    fun `T11 mark optional field missing when supported but cannot be read`() {
        val draft = parser.createDraft(Fixtures.supportedOptionalMissingAnalysis())
        val field = draft.require(FieldKey.LAST_HITS)
        assertNull(field.value)
        assertTrue(field.flags.contains(ReviewFlag.MISSING))
    }

    @Test
    fun `T12 mark required field invalid when extraction cannot validate it`() {
        val draft = parser.createDraft(Fixtures.supportedRequiredMissingAnalysis())
        assertTrue(draft.require(FieldKey.KDA).flags.contains(ReviewFlag.MISSING))
    }

    @Test
    fun `T12a mark numeric-only hero placeholder invalid`() {
        val draft = parser.createDraft(
            Fixtures.supportedFullAnalysis().copy(
                rawValues = Fixtures.supportedFullAnalysis().rawValues + (FieldKey.HERO to "1")
            )
        )

        assertNull(draft.require(FieldKey.HERO).value)
        assertTrue(draft.require(FieldKey.HERO).flags.contains(ReviewFlag.INVALID))
    }

    @Test
    fun `T13 do not derive values not explicitly visible on the screenshot`() {
        val analysis = Fixtures.supportedFullAnalysis(visibleFields = FieldKey.required - FieldKey.DAMAGE_DEALT_TO_OPPONENTS)
        val draft = parser.createDraft(analysis)
        assertNull(draft.require(FieldKey.DAMAGE_DEALT_TO_OPPONENTS).value)
    }

    @Test
    fun `T14 preserve kill participation count as displayed numeric field rather than calculating it`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(killParticipationCount = "14"))
        assertEquals("14", draft.require(FieldKey.KILL_PARTICIPATION_COUNT).value)
    }

    @Test
    fun `T15 preserve damage dealt to opponents as direct labeled field`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis(damageDealt = "12345", damageDealtToOpponents = "10101"))
        assertEquals("10101", draft.require(FieldKey.DAMAGE_DEALT_TO_OPPONENTS).value)
    }

    @Test
    fun `T16 create a draft record for a supported screenshot before final save`() {
        val draft = parser.createDraft(Fixtures.supportedFullAnalysis())
        assertFalse(draft.isFinalized)
    }

    @Test
    fun `T17 flag low confidence OCR fields for review`() {
        val draft = parser.createDraft(Fixtures.supportedLowConfidenceNumericAnalysis())
        assertTrue(draft.require(FieldKey.GOLD_FROM_FARMING).flags.contains(ReviewFlag.LOW_CONFIDENCE))
    }

    @Test
    fun `T18 flag missing fields for review`() {
        val draft = parser.createDraft(Fixtures.supportedOptionalMissingAnalysis())
        assertTrue(draft.require(FieldKey.LAST_HITS).flags.contains(ReviewFlag.MISSING))
    }
}

class SaveValidationTest {

    private val parser = DraftParser()
    private val workflow = Fixtures.workflow()

    @Test
    fun `T19 allow final save when all required fields are valid and optional fields are complete`() {
        val validation = workflow.validateForSave(parser.createDraft(Fixtures.supportedFullAnalysis()))
        assertTrue(validation is SaveValidationResult.Allowed)
    }

    @Test
    fun `T20 allow final save when all required fields are valid and one optional field remains empty`() {
        val validation = workflow.validateForSave(parser.createDraft(Fixtures.supportedOptionalMissingAnalysis()))
        assertTrue(validation is SaveValidationResult.Allowed)
    }

    @Test
    fun `T21 reject final save when any required field remains missing or invalid`() {
        val validation = workflow.validateForSave(parser.createDraft(Fixtures.supportedRequiredMissingAnalysis()))
        assertTrue(validation is SaveValidationResult.Rejected)
        assertTrue((validation as SaveValidationResult.Rejected).reason.contains("required"))
    }

    @Test
    fun `T21a reject final save when required hero is a numeric-only placeholder`() {
        val editedDraft = workflow.updateField(
            draft = parser.createDraft(Fixtures.supportedFullAnalysis()),
            fieldKey = FieldKey.HERO,
            value = "2"
        )

        val validation = workflow.validateForSave(editedDraft)

        assertTrue(validation is SaveValidationResult.Rejected)
        assertTrue((validation as SaveValidationResult.Rejected).reason.contains("required"))
    }

    @Test
    fun `T22 persist linkage between saved record and original screenshot reference`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(stores = stores)
        val import = workflow.importScreenshot("fixture_supported_full.png") as ImportResult.DraftReady
        val save = workflow.confirmSave(import.draft) as SaveResult.Saved
        assertEquals(import.storedScreenshot.id, save.record.screenshotId)
        assertEquals(import.storedScreenshot.path, save.record.screenshotPath)
    }
}

class ImportWorkflowIntegrationTest {

    @Test
    fun `IT1 supported screenshot imports into a reviewable draft with all required fields populated`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(stores = stores)

        val result = workflow.importScreenshot("fixture_supported_full.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertEquals(1, stores.screenshotStore.stored.size)
        assertTrue(result.draft.requiredFields().all { it.value != null })
        assertEquals(result.storedScreenshot.path, result.reviewState.screenshotPath)
        assertNotNull(result.reviewState.fields[FieldKey.RESULT])
    }

    @Test
    fun `IT2 supported screenshot with low confidence optional field imports into a reviewable draft and highlights the field`() {
        val workflow = Fixtures.workflow(analysisByPath = mapOf("fixture_supported_low_confidence_numeric.png" to Fixtures.supportedLowConfidenceNumericAnalysis()))
        val result = workflow.importScreenshot("fixture_supported_low_confidence_numeric.png")

        assertTrue(result is ImportResult.DraftReady)
        result as ImportResult.DraftReady
        assertTrue(result.draft.require(FieldKey.GOLD_FROM_FARMING).flags.contains(ReviewFlag.LOW_CONFIDENCE))
    }

    @Test
    fun `IT3 unsupported screenshot is rejected before a saveable draft is created`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(
            stores = stores,
            analysisByPath = mapOf("fixture_unsupported_wrong_screen.png" to Fixtures.unsupportedWrongScreenAnalysis())
        )

        val result = workflow.importScreenshot("fixture_unsupported_wrong_screen.png")

        assertTrue(result is ImportResult.Unsupported)
        result as ImportResult.Unsupported
        assertTrue(result.reason.contains("supported personal-stats template"))
        assertEquals(1, stores.screenshotStore.stored.size)
        assertTrue(stores.recordStore.saved.isEmpty())
    }

    @Test
    fun `IT4 user edits a low confidence field during review and then confirms successfully`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(
            stores = stores,
            analysisByPath = mapOf("fixture_supported_low_confidence_numeric.png" to Fixtures.supportedLowConfidenceNumericAnalysis())
        )
        val import = workflow.importScreenshot("fixture_supported_low_confidence_numeric.png") as ImportResult.DraftReady

        val updatedDraft = workflow.updateField(import.draft, FieldKey.GOLD_FROM_FARMING, "3821")
        val save = workflow.confirmSave(updatedDraft)

        assertTrue(save is SaveResult.Saved)
        save as SaveResult.Saved
        assertEquals("3821", save.record.fields[FieldKey.GOLD_FROM_FARMING])
        assertEquals(import.storedScreenshot.id, save.record.screenshotId)
    }

    @Test
    fun `IT5 user attempts to save a draft with unresolved required fields and is blocked`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(
            stores = stores,
            analysisByPath = mapOf("fixture_supported_required_missing.png" to Fixtures.supportedRequiredMissingAnalysis())
        )
        val import = workflow.importScreenshot("fixture_supported_required_missing.png") as ImportResult.DraftReady

        val save = workflow.confirmSave(import.draft)

        assertTrue(save is SaveResult.Blocked)
        assertTrue(stores.recordStore.saved.isEmpty())
    }

    @Test
    fun `IT6 user saves a reviewed partial record with unresolved optional fields`() {
        val stores = Fixtures.stores()
        val workflow = Fixtures.workflow(
            stores = stores,
            analysisByPath = mapOf("fixture_supported_optional_missing.png" to Fixtures.supportedOptionalMissingAnalysis())
        )
        val import = workflow.importScreenshot("fixture_supported_optional_missing.png") as ImportResult.DraftReady

        val save = workflow.confirmSave(import.draft)

        assertTrue(save is SaveResult.Saved)
        save as SaveResult.Saved
        assertNull(save.record.fields[FieldKey.LAST_HITS])
    }

    @Test
    fun `IT7 import flow informs the user when local screenshot storage fails at import start`() {
        val workflow = Fixtures.workflow(
            stores = Fixtures.stores(screenshotStore = FakeScreenshotStore(failPaths = setOf("fixture_storage_failure.png")))
        )

        val result = workflow.importScreenshot("fixture_storage_failure.png")

        assertTrue(result is ImportResult.StorageFailed)
        assertTrue((result as ImportResult.StorageFailed).message.contains("save"))
    }

    @Test
    fun `IT8 save flow informs the user when final record persistence fails`() {
        val stores = Fixtures.stores(recordStore = FakeRecordStore(shouldFail = true))
        val workflow = Fixtures.workflow(stores = stores)
        val import = workflow.importScreenshot("fixture_supported_full.png") as ImportResult.DraftReady

        val save = workflow.confirmSave(import.draft)

        assertTrue(save is SaveResult.StorageFailed)
        assertFalse((save as SaveResult.StorageFailed).saved)
    }

    @Test
    fun `IT9 review screen always shows the original screenshot alongside extracted fields`() {
        val result = Fixtures.workflow().importScreenshot("fixture_supported_full.png") as ImportResult.DraftReady
        assertEquals(result.storedScreenshot.path, result.reviewState.screenshotPath)
        assertTrue(result.reviewState.fields.isNotEmpty())
    }
}

private fun assertUnsupported(result: TemplateValidationResult, expected: String) {
    assertTrue(result is TemplateValidationResult.Unsupported)
    result as TemplateValidationResult.Unsupported
    assertTrue(result.reason.contains(expected))
}

private object Fixtures {
    private val allSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN, Section.ECONOMY, Section.TEAM_PARTICIPATION)
    private val allAnchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED)

    fun workflow(
        stores: Stores = stores(),
        analysisByPath: Map<String, ScreenshotAnalysis> = defaultAnalysisByPath()
    ): MatchImportWorkflow {
        return MatchImportWorkflow(
            screenshotStore = stores.screenshotStore,
            analyzer = FakeScreenshotAnalyzer(analysisByPath),
            recordStore = stores.recordStore,
            validator = TemplateValidator(),
            parser = DraftParser()
        )
    }

    fun stores(
        screenshotStore: FakeScreenshotStore = FakeScreenshotStore(),
        recordStore: FakeRecordStore = FakeRecordStore()
    ) = Stores(screenshotStore, recordStore)

    fun defaultAnalysisByPath(): Map<String, ScreenshotAnalysis> = mapOf(
        "fixture_supported_full.png" to supportedFullAnalysis(),
        "fixture_supported_optional_missing.png" to supportedOptionalMissingAnalysis(),
        "fixture_supported_required_missing.png" to supportedRequiredMissingAnalysis(),
        "fixture_supported_low_confidence_numeric.png" to supportedLowConfidenceNumericAnalysis(),
        "fixture_supported_other_language.png" to supportedOtherLanguageAnalysis(),
        "fixture_unsupported_wrong_screen.png" to unsupportedWrongScreenAnalysis(),
        "fixture_unsupported_cropped.png" to unsupportedCroppedAnalysis()
    )

    fun supportedFullAnalysis(
        anchors: Set<Anchor> = allAnchors,
        visibleSections: Set<Section> = allSections,
        visibleFields: Set<FieldKey> = FieldKey.all,
        resultText: String = "胜利",
        score: String = "20-10",
        lane: String = "对抗路",
        kda: String = "11/1/5",
        damageDealt: String = "12345",
        damageDealtToOpponents: String = "10101",
        killParticipationCount: String = "13"
    ): ScreenshotAnalysis {
        val extracted = mutableMapOf(
            FieldKey.RESULT to resultText,
            FieldKey.HERO to "孙尚香",
            FieldKey.PLAYER_NAME to "King",
            FieldKey.LANE to lane,
            FieldKey.SCORE to score,
            FieldKey.KDA to kda,
            FieldKey.DAMAGE_DEALT to damageDealt,
            FieldKey.DAMAGE_SHARE to "34%",
            FieldKey.DAMAGE_TAKEN to "9850",
            FieldKey.DAMAGE_TAKEN_SHARE to "28%",
            FieldKey.TOTAL_GOLD to "12543",
            FieldKey.GOLD_SHARE to "31%",
            FieldKey.PARTICIPATION_RATE to "76%",
            FieldKey.GOLD_FROM_FARMING to "3680",
            FieldKey.LAST_HITS to "71",
            FieldKey.KILL_PARTICIPATION_COUNT to killParticipationCount,
            FieldKey.CONTROL_DURATION to "00:14",
            FieldKey.DAMAGE_DEALT_TO_OPPONENTS to damageDealtToOpponents
        )

        return ScreenshotAnalysis(
            anchors = anchors,
            visibleSections = visibleSections,
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = extracted.filterKeys { it in visibleFields },
            lowConfidenceFields = emptySet()
        )
    }

    fun supportedOptionalMissingAnalysis(): ScreenshotAnalysis {
        return supportedFullAnalysis(
            visibleFields = FieldKey.all - FieldKey.LAST_HITS
        )
    }

    fun supportedRequiredMissingAnalysis(): ScreenshotAnalysis {
        return supportedFullAnalysis(
            visibleFields = FieldKey.all - FieldKey.KDA
        )
    }

    fun supportedLowConfidenceNumericAnalysis(): ScreenshotAnalysis {
        return supportedFullAnalysis().copy(lowConfidenceFields = setOf(FieldKey.GOLD_FROM_FARMING))
    }

    fun supportedOtherLanguageAnalysis(): ScreenshotAnalysis {
        return supportedFullAnalysis().copy(languageCode = "en-US")
    }

    fun unsupportedWrongScreenAnalysis(): ScreenshotAnalysis {
        return supportedFullAnalysis(
            anchors = setOf(Anchor.SUMMARY_CARD),
            visibleSections = setOf(Section.DAMAGE)
        )
    }

    fun unsupportedCroppedAnalysis(
        anchors: Set<Anchor> = setOf(Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED)
    ): ScreenshotAnalysis {
        return supportedFullAnalysis(
            anchors = anchors,
            visibleSections = setOf(Section.DAMAGE, Section.DAMAGE_TAKEN)
        )
    }
}

private data class Stores(
    val screenshotStore: FakeScreenshotStore,
    val recordStore: FakeRecordStore
)
