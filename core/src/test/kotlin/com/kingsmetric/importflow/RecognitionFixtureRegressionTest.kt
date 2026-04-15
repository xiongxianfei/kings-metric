package com.kingsmetric.importflow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionFixtureRegressionTest {

    @Test
    fun `T1 fixture catalog includes one supported full fixture`() {
        val fixtures = RecognitionFixtureCatalog.all()

        assertTrue(fixtures.any { it.category == FixtureCategory.SUPPORTED_FULL })
    }

    @Test
    fun `T2 fixture catalog includes one supported optional-missing fixture`() {
        val fixtures = RecognitionFixtureCatalog.all()

        assertTrue(fixtures.any { it.category == FixtureCategory.SUPPORTED_OPTIONAL_MISSING })
    }

    @Test
    fun `T3 fixture catalog includes one supported required-missing fixture`() {
        val fixtures = RecognitionFixtureCatalog.all()

        assertTrue(fixtures.any { it.category == FixtureCategory.SUPPORTED_REQUIRED_UNRESOLVED })
    }

    @Test
    fun `T4 fixture catalog includes one unsupported fixture`() {
        val fixtures = RecognitionFixtureCatalog.all()

        assertTrue(fixtures.any { it.category == FixtureCategory.UNSUPPORTED })
    }

    @Test
    fun `T5 fixture catalog includes one low-confidence fixture`() {
        val fixtures = RecognitionFixtureCatalog.all()

        assertTrue(fixtures.any { it.category == FixtureCategory.LOW_CONFIDENCE_NUMERIC })
    }

    @Test
    fun `T6 supported full fixture remains accepted with expected parsed output`() {
        val report = RecognitionRegressionSuite().run(RecognitionFixtureCatalog.all())

        assertTrue(report.isSuccessful)
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.SUPPORTED_FULL }
        val result = report.results.first { it.fixtureId == fixture.id }
        assertTrue(result.outcome is FixtureEvaluationOutcome.Supported)
        result.outcome as FixtureEvaluationOutcome.Supported
        assertEquals("victory", result.outcome.draft.require(FieldKey.RESULT).value)
        assertEquals("11/1/5", result.outcome.draft.require(FieldKey.KDA).value)
    }

    @Test
    fun `T7 optional-missing fixture remains accepted with expected missing optional field`() {
        val report = RecognitionRegressionSuite().run(RecognitionFixtureCatalog.all())
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.SUPPORTED_OPTIONAL_MISSING }
        val result = report.results.first { it.fixtureId == fixture.id }

        assertTrue(result.outcome is FixtureEvaluationOutcome.Supported)
        result.outcome as FixtureEvaluationOutcome.Supported
        assertEquals(null, result.outcome.draft.require(FieldKey.LAST_HITS).value)
        assertTrue(ReviewFlag.MISSING in result.outcome.draft.require(FieldKey.LAST_HITS).flags)
    }

    @Test
    fun `T8 required-missing fixture remains accepted as draftable but unresolved`() {
        val report = RecognitionRegressionSuite().run(RecognitionFixtureCatalog.all())
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.SUPPORTED_REQUIRED_UNRESOLVED }
        val result = report.results.first { it.fixtureId == fixture.id }

        assertTrue(result.outcome is FixtureEvaluationOutcome.Supported)
        result.outcome as FixtureEvaluationOutcome.Supported
        assertEquals(null, result.outcome.draft.require(FieldKey.KDA).value)
        assertTrue(ReviewFlag.MISSING in result.outcome.draft.require(FieldKey.KDA).flags)
    }

    @Test
    fun `T9 unsupported fixture remains rejected`() {
        val report = RecognitionRegressionSuite().run(RecognitionFixtureCatalog.all())
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.UNSUPPORTED }
        val result = report.results.first { it.fixtureId == fixture.id }

        assertTrue(result.outcome is FixtureEvaluationOutcome.Unsupported)
    }

    @Test
    fun `T10 low-confidence fixture remains flagged for review`() {
        val report = RecognitionRegressionSuite().run(RecognitionFixtureCatalog.all())
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.LOW_CONFIDENCE_NUMERIC }
        val result = report.results.first { it.fixtureId == fixture.id }

        assertTrue(result.outcome is FixtureEvaluationOutcome.Supported)
        result.outcome as FixtureEvaluationOutcome.Supported
        assertTrue(ReviewFlag.LOW_CONFIDENCE in result.outcome.draft.require(FieldKey.GOLD_FROM_FARMING).flags)
    }

    @Test
    fun `IT1 regression suite reports which fixture changed when expected outcomes drift`() {
        val driftFixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.SUPPORTED_FULL }
        val suite = RecognitionRegressionSuite(
            parser = { analysis ->
                DraftParser().createDraft(
                    analysis.copy(rawValues = analysis.rawValues + (FieldKey.RESULT to "defeat"))
                )
            }
        )

        val report = suite.run(listOf(driftFixture))

        assertFalse(report.isSuccessful)
        val failure = report.failures.single()
        assertEquals(driftFixture.id, failure.fixtureId)
        assertTrue(failure.message.contains(FieldKey.RESULT.name))
    }

    @Test
    fun `IT2 validator change that affects acceptance or rejection behavior fails the suite`() {
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.UNSUPPORTED }
        val suite = RecognitionRegressionSuite(
            validator = { TemplateValidationResult.Supported }
        )

        val report = suite.run(listOf(fixture))

        assertFalse(report.isSuccessful)
        assertEquals(fixture.id, report.failures.single().fixtureId)
        assertTrue(report.failures.single().message.contains("unsupported"))
    }

    @Test
    fun `IT3 parser change that alters expected field mapping fails the suite`() {
        val fixture = RecognitionFixtureCatalog.all().first { it.category == FixtureCategory.SUPPORTED_OPTIONAL_MISSING }
        val suite = RecognitionRegressionSuite(
            parser = { analysis ->
                DraftParser().createDraft(
                    analysis.copy(rawValues = analysis.rawValues + (FieldKey.HERO to "Drifted Hero"))
                )
            }
        )

        val report = suite.run(listOf(fixture))

        assertFalse(report.isSuccessful)
        val failure = report.failures.single()
        assertEquals(fixture.id, failure.fixtureId)
        assertTrue(failure.message.contains(FieldKey.HERO.name))
        assertNotNull(report.results.singleOrNull())
    }
}
