package com.kingsmetric.importflow

enum class Anchor {
    RESULT_HEADER,
    SUMMARY_CARD,
    DATA_TAB_SELECTED
}

enum class Section {
    DAMAGE,
    DAMAGE_TAKEN,
    ECONOMY,
    TEAM_PARTICIPATION
}

enum class ReviewFlag {
    MISSING,
    INVALID,
    LOW_CONFIDENCE
}

enum class FieldKey(val required: Boolean) {
    RESULT(true),
    HERO(true),
    PLAYER_NAME(true),
    LANE(true),
    SCORE(true),
    KDA(true),
    DAMAGE_DEALT(true),
    DAMAGE_SHARE(true),
    DAMAGE_TAKEN(true),
    DAMAGE_TAKEN_SHARE(true),
    TOTAL_GOLD(true),
    GOLD_SHARE(true),
    PARTICIPATION_RATE(true),
    GOLD_FROM_FARMING(false),
    LAST_HITS(false),
    KILL_PARTICIPATION_COUNT(false),
    CONTROL_DURATION(false),
    DAMAGE_DEALT_TO_OPPONENTS(false);

    companion object {
        val all: Set<FieldKey> = entries.toSet()
        val required: Set<FieldKey> = entries.filter { it.required }.toSet()
        val optional: Set<FieldKey> = entries.filterNot { it.required }.toSet()
    }
}

data class ScreenshotAnalysis(
    val anchors: Set<Anchor>,
    val visibleSections: Set<Section>,
    val languageCode: String,
    val visibleFields: Set<FieldKey>,
    val rawValues: Map<FieldKey, String>,
    val lowConfidenceFields: Set<FieldKey>
)

sealed interface TemplateValidationResult {
    data object Supported : TemplateValidationResult
    data class Unsupported(val reason: String) : TemplateValidationResult
}

class TemplateValidator {

    fun validate(analysis: ScreenshotAnalysis): TemplateValidationResult {
        if (analysis.languageCode != "zh-CN") {
            return TemplateValidationResult.Unsupported(
                "Image does not match the supported personal-stats template."
            )
        }

        if (Anchor.RESULT_HEADER !in analysis.anchors) {
            return TemplateValidationResult.Unsupported("Missing result header anchor.")
        }

        if (Anchor.SUMMARY_CARD !in analysis.anchors) {
            return TemplateValidationResult.Unsupported("Missing hero/player summary card anchor.")
        }

        if (Anchor.DATA_TAB_SELECTED !in analysis.anchors) {
            return TemplateValidationResult.Unsupported("Missing data tab anchor.")
        }

        val missingSections = requiredSections - analysis.visibleSections
        if (missingSections.isNotEmpty()) {
            return TemplateValidationResult.Unsupported(
                "Missing ${missingSections.first().displayName} section."
            )
        }

        return TemplateValidationResult.Supported
    }

    private companion object {
        val requiredSections = setOf(
            Section.DAMAGE,
            Section.DAMAGE_TAKEN,
            Section.ECONOMY,
            Section.TEAM_PARTICIPATION
        )

        val Section.displayName: String
            get() = when (this) {
                Section.DAMAGE -> "damage"
                Section.DAMAGE_TAKEN -> "damage taken"
                Section.ECONOMY -> "economy"
                Section.TEAM_PARTICIPATION -> "team participation"
            }
    }
}

data class DraftField(
    val key: FieldKey,
    val required: Boolean,
    val value: String?,
    val flags: Set<ReviewFlag>
)

data class DraftRecord(
    val fields: Map<FieldKey, DraftField>,
    val screenshotId: String? = null,
    val screenshotPath: String? = null,
    val isFinalized: Boolean = false
) {
    fun require(fieldKey: FieldKey): DraftField = fields.getValue(fieldKey)

    fun requiredFields(): List<DraftField> = FieldKey.required.map(::require)
}

class DraftParser {

    fun createDraft(
        analysis: ScreenshotAnalysis,
        screenshotId: String? = null,
        screenshotPath: String? = null
    ): DraftRecord {
        val fields = FieldKey.all.associateWith { fieldKey ->
            val rawValue = analysis.rawValues[fieldKey]?.takeIf { fieldKey in analysis.visibleFields }
            val normalizedValue = normalize(fieldKey, rawValue)
            val flags = buildSet {
                if (fieldKey !in analysis.visibleFields || normalizedValue == null) {
                    add(ReviewFlag.MISSING)
                }
                if (fieldKey in analysis.lowConfidenceFields) {
                    add(ReviewFlag.LOW_CONFIDENCE)
                }
                if (rawValue != null && normalizedValue == null && fieldKey !in analysis.lowConfidenceFields) {
                    add(ReviewFlag.INVALID)
                }
            }

            DraftField(
                key = fieldKey,
                required = fieldKey.required,
                value = normalizedValue,
                flags = flags
            )
        }

        return DraftRecord(
            fields = fields,
            screenshotId = screenshotId,
            screenshotPath = screenshotPath
        )
    }

    private fun normalize(fieldKey: FieldKey, rawValue: String?): String? {
        val value = rawValue?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return when (fieldKey) {
            FieldKey.RESULT -> when (value) {
                "胜利", "victory" -> "victory"
                "失败", "defeat" -> "defeat"
                else -> null
            }
            FieldKey.KDA -> value.takeIf { kdaPattern.matches(it) }
            else -> value
        }
    }

    private companion object {
        val kdaPattern = Regex("""\d+/\d+/\d+""")
    }
}

sealed interface SaveValidationResult {
    data object Allowed : SaveValidationResult
    data class Rejected(val reason: String) : SaveValidationResult
}

data class StoredScreenshot(
    val id: String,
    val path: String,
    val originalSourcePath: String
)

data class ReviewState(
    val screenshotPath: String,
    val fields: Map<FieldKey, DraftField>,
    val highlightedFields: Set<FieldKey>,
    val blockingFields: Set<FieldKey>,
    val editableFields: Set<FieldKey>,
    val screenshotAvailable: Boolean,
    val canConfirm: Boolean
) {
    companion object {
        fun fromDraft(draft: DraftRecord): ReviewState {
            val highlightedFields = draft.fields
                .filterValues { it.flags.isNotEmpty() }
                .keys
            val blockingFields = draft.fields
                .filterValues { field ->
                    field.required && (field.value == null || ReviewFlag.MISSING in field.flags || ReviewFlag.INVALID in field.flags)
                }
                .keys
            return ReviewState(
                screenshotPath = draft.screenshotPath.orEmpty(),
                fields = draft.fields,
                highlightedFields = highlightedFields,
                blockingFields = blockingFields,
                editableFields = FieldKey.all,
                screenshotAvailable = draft.screenshotPath != null,
                canConfirm = blockingFields.isEmpty()
            )
        }
    }
}

data class SavedMatchRecord(
    val screenshotId: String,
    val screenshotPath: String,
    val fields: Map<FieldKey, String?>
)

enum class FailureAction {
    RETRY_IMPORT,
    CORRECT_REVIEW_FIELDS,
    RETRY_SAVE
}

sealed interface ImportResult {
    data class DraftReady(
        val storedScreenshot: StoredScreenshot,
        val draft: DraftRecord,
        val reviewState: ReviewState
    ) : ImportResult

    data class Unsupported(
        val reason: String,
        val nextAction: FailureAction = FailureAction.RETRY_IMPORT,
        val ocrText: String? = null
    ) : ImportResult

    data class StorageFailed(val message: String, val nextAction: FailureAction = FailureAction.RETRY_IMPORT) : ImportResult
    data class ImportFailed(
        val message: String,
        val nextAction: FailureAction = FailureAction.RETRY_IMPORT,
        val ocrText: String? = null
    ) : ImportResult
    data object Cancelled : ImportResult
}

sealed interface ScreenshotSelectionResult {
    data class Accepted(val sourcePath: String) : ScreenshotSelectionResult
    data object Cancelled : ScreenshotSelectionResult
}

class SingleScreenshotSelectionPolicy {

    fun accept(sourcePaths: List<String>): ScreenshotSelectionResult {
        return sourcePaths.firstOrNull()
            ?.let { ScreenshotSelectionResult.Accepted(it) }
            ?: ScreenshotSelectionResult.Cancelled
    }
}

sealed interface SaveResult {
    data class Saved(val record: SavedMatchRecord) : SaveResult
    data class Blocked(
        val reason: String,
        val draft: DraftRecord,
        val nextAction: FailureAction = FailureAction.CORRECT_REVIEW_FIELDS
    ) : SaveResult

    data class StorageFailed(
        val message: String,
        val saved: Boolean = false,
        val draft: DraftRecord? = null,
        val nextAction: FailureAction = FailureAction.RETRY_SAVE
    ) : SaveResult
}

interface ScreenshotStore {
    fun storeOriginal(sourcePath: String): StoredScreenshot
}

interface ScreenshotAnalyzer {
    fun analyze(sourcePath: String): ScreenshotAnalysis
}

class OcrExtractionException(
    message: String,
    val ocrText: String? = null
) : IllegalStateException(message)

interface RecordStore {
    fun save(record: SavedMatchRecord): SavedMatchRecord
}

class MatchImportWorkflow(
    private val screenshotStore: ScreenshotStore,
    private val analyzer: ScreenshotAnalyzer,
    private val recordStore: RecordStore,
    private val validator: TemplateValidator,
    private val parser: DraftParser
) {

    fun importSelection(sourcePaths: List<String>): ImportResult {
        return when (val selection = SingleScreenshotSelectionPolicy().accept(sourcePaths)) {
            is ScreenshotSelectionResult.Accepted -> importScreenshot(selection.sourcePath)
            ScreenshotSelectionResult.Cancelled -> ImportResult.Cancelled
        }
    }

    fun importScreenshot(sourcePath: String): ImportResult {
        val storedScreenshot = try {
            screenshotStore.storeOriginal(sourcePath)
        } catch (_: UnreadableSourceException) {
            return ImportResult.ImportFailed("Could not import screenshot from the selected source.")
        } catch (_: IllegalStateException) {
            return ImportResult.StorageFailed("Could not save screenshot locally.")
        }

        val analysis = try {
            analyzer.analyze(sourcePath)
        } catch (_: OcrExtractionException) {
            return ImportResult.ImportFailed("Could not extract screenshot data for review.")
        }
        return when (val validation = validator.validate(analysis)) {
            TemplateValidationResult.Supported -> {
                val draft = parser.createDraft(
                    analysis = analysis,
                    screenshotId = storedScreenshot.id,
                    screenshotPath = storedScreenshot.path
                )
                ImportResult.DraftReady(
                    storedScreenshot = storedScreenshot,
                    draft = draft,
                    reviewState = ReviewState.fromDraft(draft)
                )
            }
            is TemplateValidationResult.Unsupported -> ImportResult.Unsupported(
                "Image does not match the supported personal-stats template. ${validation.reason}"
            )
        }
    }

    fun updateField(draft: DraftRecord, fieldKey: FieldKey, value: String): DraftRecord {
        val updatedField = draft.require(fieldKey).copy(
            value = value.trim().ifEmpty { null },
            flags = emptySet()
        )
        return draft.copy(fields = draft.fields + (fieldKey to updatedField))
    }

    fun validateForSave(draft: DraftRecord): SaveValidationResult {
        val unresolvedRequiredFields = draft.requiredFields().filter { field ->
            field.value == null || ReviewFlag.MISSING in field.flags || ReviewFlag.INVALID in field.flags
        }
        if (unresolvedRequiredFields.isNotEmpty()) {
            return SaveValidationResult.Rejected("One or more required fields remain unresolved.")
        }
        return SaveValidationResult.Allowed
    }

    fun confirmSave(draft: DraftRecord): SaveResult {
        return when (val validation = validateForSave(draft)) {
            SaveValidationResult.Allowed -> {
                val screenshotId = draft.screenshotId
                    ?: return SaveResult.StorageFailed("Could not save record: screenshot link missing.", draft = draft)
                val screenshotPath = draft.screenshotPath
                    ?: return SaveResult.StorageFailed("Could not save record: screenshot link missing.", draft = draft)
                val record = SavedMatchRecord(
                    screenshotId = screenshotId,
                    screenshotPath = screenshotPath,
                    fields = draft.fields.mapValues { it.value.value }
                )
                try {
                    SaveResult.Saved(recordStore.save(record))
                } catch (_: IllegalStateException) {
                    SaveResult.StorageFailed("Could not save record locally.", draft = draft)
                }
            }
            is SaveValidationResult.Rejected -> SaveResult.Blocked(validation.reason, draft)
        }
    }
}

class FakeScreenshotAnalyzer(
    private val analysisByPath: Map<String, ScreenshotAnalysis>
) : ScreenshotAnalyzer {

    override fun analyze(sourcePath: String): ScreenshotAnalysis {
        return analysisByPath[sourcePath]
            ?: error("No fixture analysis configured for $sourcePath")
    }
}

class FakeScreenshotStore(
    private val failPaths: Set<String> = emptySet(),
    private val unreadablePaths: Set<String> = emptySet()
) : ScreenshotStore {

    val stored = mutableListOf<StoredScreenshot>()

    override fun storeOriginal(sourcePath: String): StoredScreenshot {
        if (sourcePath in unreadablePaths) {
            throw UnreadableSourceException()
        }
        if (sourcePath in failPaths) {
            throw IllegalStateException("storage failed")
        }

        val storedScreenshot = StoredScreenshot(
            id = "shot-${stored.size + 1}",
            path = "stored/${stored.size + 1}-$sourcePath",
            originalSourcePath = sourcePath
        )
        stored += storedScreenshot
        return storedScreenshot
    }
}

class UnreadableSourceException : IllegalStateException("source unreadable")

class FakeRecordStore(
    private val shouldFail: Boolean = false
) : RecordStore {

    val saved = mutableListOf<SavedMatchRecord>()

    override fun save(record: SavedMatchRecord): SavedMatchRecord {
        if (shouldFail) {
            throw IllegalStateException("record save failed")
        }
        saved += record
        return record
    }
}
