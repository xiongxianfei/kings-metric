package com.kingsmetric.importflow

enum class FixtureCategory {
    SUPPORTED_FULL,
    SUPPORTED_OPTIONAL_MISSING,
    SUPPORTED_REQUIRED_UNRESOLVED,
    UNSUPPORTED,
    LOW_CONFIDENCE_NUMERIC
}

data class RecognitionFixture(
    val id: String,
    val category: FixtureCategory,
    val analysis: ScreenshotAnalysis,
    val expected: FixtureExpectation
)

sealed interface FixtureExpectation {
    data class Supported(
        val expectedValues: Map<FieldKey, String?>,
        val expectedFlags: Map<FieldKey, Set<ReviewFlag>> = emptyMap()
    ) : FixtureExpectation

    data class Unsupported(val reasonContains: String) : FixtureExpectation
}

sealed interface FixtureEvaluationOutcome {
    data class Supported(val draft: DraftRecord) : FixtureEvaluationOutcome
    data class Unsupported(val reason: String) : FixtureEvaluationOutcome
}

data class FixtureEvaluationResult(
    val fixtureId: String,
    val outcome: FixtureEvaluationOutcome
)

data class FixtureRegressionFailure(
    val fixtureId: String,
    val message: String
)

data class RecognitionRegressionReport(
    val results: List<FixtureEvaluationResult>,
    val failures: List<FixtureRegressionFailure>
) {
    val isSuccessful: Boolean
        get() = failures.isEmpty()
}

object RecognitionFixtureCatalog {

    fun all(): List<RecognitionFixture> = listOf(
        supportedFullFixture(),
        supportedOptionalMissingFixture(),
        supportedRequiredUnresolvedFixture(),
        unsupportedFixture(),
        lowConfidenceNumericFixture()
    )

    private fun supportedFullFixture(): RecognitionFixture =
        RecognitionFixture(
            id = "supported-full",
            category = FixtureCategory.SUPPORTED_FULL,
            analysis = baseAnalysis(),
            expected = FixtureExpectation.Supported(
                expectedValues = mapOf(
                    FieldKey.RESULT to "victory",
                    FieldKey.HERO to "Sun Shangxiang",
                    FieldKey.KDA to "11/1/5"
                )
            )
        )

    private fun supportedOptionalMissingFixture(): RecognitionFixture =
        RecognitionFixture(
            id = "supported-optional-missing-last-hits",
            category = FixtureCategory.SUPPORTED_OPTIONAL_MISSING,
            analysis = baseAnalysis(
                visibleFields = FieldKey.all - FieldKey.LAST_HITS
            ),
            expected = FixtureExpectation.Supported(
                expectedValues = mapOf(
                    FieldKey.HERO to "Sun Shangxiang",
                    FieldKey.LAST_HITS to null
                ),
                expectedFlags = mapOf(
                    FieldKey.LAST_HITS to setOf(ReviewFlag.MISSING)
                )
            )
        )

    private fun supportedRequiredUnresolvedFixture(): RecognitionFixture =
        RecognitionFixture(
            id = "supported-required-unresolved-kda",
            category = FixtureCategory.SUPPORTED_REQUIRED_UNRESOLVED,
            analysis = baseAnalysis(
                visibleFields = FieldKey.all - FieldKey.KDA
            ),
            expected = FixtureExpectation.Supported(
                expectedValues = mapOf(
                    FieldKey.KDA to null
                ),
                expectedFlags = mapOf(
                    FieldKey.KDA to setOf(ReviewFlag.MISSING)
                )
            )
        )

    private fun unsupportedFixture(): RecognitionFixture =
        RecognitionFixture(
            id = "unsupported-cropped-team-participation",
            category = FixtureCategory.UNSUPPORTED,
            analysis = baseAnalysis(
                visibleSections = setOf(
                    Section.DAMAGE,
                    Section.DAMAGE_TAKEN,
                    Section.ECONOMY
                )
            ),
            expected = FixtureExpectation.Unsupported(
                reasonContains = "Missing team participation section."
            )
        )

    private fun lowConfidenceNumericFixture(): RecognitionFixture =
        RecognitionFixture(
            id = "supported-low-confidence-gold-farming",
            category = FixtureCategory.LOW_CONFIDENCE_NUMERIC,
            analysis = baseAnalysis(
                lowConfidenceFields = setOf(FieldKey.GOLD_FROM_FARMING),
                rawValues = baseRawValues() + (FieldKey.GOLD_FROM_FARMING to "3B80")
            ),
            expected = FixtureExpectation.Supported(
                expectedValues = mapOf(
                    FieldKey.GOLD_FROM_FARMING to "3B80"
                ),
                expectedFlags = mapOf(
                    FieldKey.GOLD_FROM_FARMING to setOf(ReviewFlag.LOW_CONFIDENCE)
                )
            )
        )

    private fun baseAnalysis(
        visibleFields: Set<FieldKey> = FieldKey.all,
        visibleSections: Set<Section> = setOf(
            Section.DAMAGE,
            Section.DAMAGE_TAKEN,
            Section.ECONOMY,
            Section.TEAM_PARTICIPATION
        ),
        lowConfidenceFields: Set<FieldKey> = emptySet(),
        rawValues: Map<FieldKey, String> = baseRawValues()
    ): ScreenshotAnalysis {
        return ScreenshotAnalysis(
            anchors = setOf(Anchor.RESULT_HEADER, Anchor.SUMMARY_CARD, Anchor.DATA_TAB_SELECTED),
            visibleSections = visibleSections,
            languageCode = "zh-CN",
            visibleFields = visibleFields,
            rawValues = rawValues.filterKeys { it in visibleFields },
            lowConfidenceFields = lowConfidenceFields
        )
    }

    private fun baseRawValues(): Map<FieldKey, String> = mapOf(
        FieldKey.RESULT to "victory",
        FieldKey.HERO to "Sun Shangxiang",
        FieldKey.PLAYER_NAME to "King",
        FieldKey.LANE to "Clash Lane",
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
}

class RecognitionRegressionSuite(
    private val validator: (ScreenshotAnalysis) -> TemplateValidationResult = TemplateValidator()::validate,
    private val parser: (ScreenshotAnalysis) -> DraftRecord = { DraftParser().createDraft(it) }
) {

    fun run(fixtures: List<RecognitionFixture>): RecognitionRegressionReport {
        val results = mutableListOf<FixtureEvaluationResult>()
        val failures = mutableListOf<FixtureRegressionFailure>()

        fixtures.forEach { fixture ->
            when (val validation = validator(fixture.analysis)) {
                TemplateValidationResult.Supported -> {
                    val draft = parser(fixture.analysis)
                    val outcome = FixtureEvaluationOutcome.Supported(draft)
                    results += FixtureEvaluationResult(fixture.id, outcome)
                    failures += compareSupportedExpectation(fixture, draft)
                }
                is TemplateValidationResult.Unsupported -> {
                    val outcome = FixtureEvaluationOutcome.Unsupported(validation.reason)
                    results += FixtureEvaluationResult(fixture.id, outcome)
                    failures += compareUnsupportedExpectation(fixture, validation.reason)
                }
            }
        }

        return RecognitionRegressionReport(
            results = results,
            failures = failures
        )
    }

    private fun compareSupportedExpectation(
        fixture: RecognitionFixture,
        draft: DraftRecord
    ): List<FixtureRegressionFailure> {
        val expectation = fixture.expected as? FixtureExpectation.Supported
            ?: return listOf(
                FixtureRegressionFailure(
                    fixture.id,
                    "Expected unsupported fixture but validator/parser produced a supported draft."
                )
            )

        val failures = mutableListOf<FixtureRegressionFailure>()
        expectation.expectedValues.forEach { (fieldKey, expectedValue) ->
            val actualValue = draft.require(fieldKey).value
            if (actualValue != expectedValue) {
                failures += FixtureRegressionFailure(
                    fixture.id,
                    "Field ${fieldKey.name} changed. Expected=$expectedValue actual=$actualValue"
                )
            }
        }
        expectation.expectedFlags.forEach { (fieldKey, expectedFlags) ->
            val actualFlags = draft.require(fieldKey).flags
            if (actualFlags != expectedFlags) {
                failures += FixtureRegressionFailure(
                    fixture.id,
                    "Flags for ${fieldKey.name} changed. Expected=$expectedFlags actual=$actualFlags"
                )
            }
        }
        return failures
    }

    private fun compareUnsupportedExpectation(
        fixture: RecognitionFixture,
        actualReason: String
    ): List<FixtureRegressionFailure> {
        val expectation = fixture.expected as? FixtureExpectation.Unsupported
            ?: return listOf(
                FixtureRegressionFailure(
                    fixture.id,
                    "Expected supported fixture but validator rejected it: $actualReason"
                )
            )

        if (!actualReason.contains(expectation.reasonContains)) {
            return listOf(
                FixtureRegressionFailure(
                    fixture.id,
                    "Unsupported reason changed. Expected fragment='${expectation.reasonContains}' actual='$actualReason'"
                )
            )
        }
        return emptyList()
    }
}
