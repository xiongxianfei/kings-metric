package com.kingsmetric.diagnostics

import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Base64

private const val DEFAULT_RETENTION_LIMIT = 50
private const val DEFAULT_EXPORT_NOTICE =
    "This export does not include the original screenshot or full saved match data. It may include OCR text captured during a failed recognition attempt."
private val ALLOWED_EXPORT_METADATA_KEYS = setOf("appVersion", "buildType", "surface", "detail", "ocrText")

enum class DiagnosticsStage {
    IMPORT,
    RECOGNITION,
    REVIEW,
    SAVE,
    HISTORY,
    DETAIL,
    DASHBOARD
}

enum class DiagnosticsOutcome {
    IMPORT_SOURCE_FAILED,
    IMPORT_STORAGE_FAILED,
    UNSUPPORTED_SCREENSHOT,
    RECOGNITION_FAILED,
    REVIEW_BLOCKED,
    SAVE_FAILED,
    SAVE_SUCCEEDED
}

data class DiagnosticsEvent(
    val timestampMillis: Long,
    val stage: DiagnosticsStage,
    val outcome: DiagnosticsOutcome,
    val summary: String,
    val metadata: Map<String, String> = emptyMap()
)

data class DiagnosticsExportEntry(
    val timestampMillis: Long,
    val stage: DiagnosticsStage,
    val outcome: DiagnosticsOutcome,
    val summary: String,
    val metadata: Map<String, String>
)

data class DiagnosticsExport(
    val exportedAtMillis: Long,
    val notice: String,
    val entries: List<DiagnosticsExportEntry>
)

interface DiagnosticsRecorder {
    val requiresAccount: Boolean
    val uploadsAutomatically: Boolean

    fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String> = emptyMap()
    )

    fun snapshot(): List<DiagnosticsEvent>

    fun export(): DiagnosticsExport
}

object NoOpDiagnosticsRecorder : DiagnosticsRecorder {
    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) = Unit

    override fun snapshot(): List<DiagnosticsEvent> = emptyList()

    override fun export(): DiagnosticsExport {
        return DiagnosticsExport(
            exportedAtMillis = 0L,
            notice = DEFAULT_EXPORT_NOTICE,
            entries = emptyList()
        )
    }
}

class FileBackedDiagnosticsRecorder(
    private val storageFile: File,
    private val retentionLimit: Int = DEFAULT_RETENTION_LIMIT,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : DiagnosticsRecorder {

    override val requiresAccount: Boolean = false
    override val uploadsAutomatically: Boolean = false

    override fun record(
        stage: DiagnosticsStage,
        outcome: DiagnosticsOutcome,
        summary: String,
        metadata: Map<String, String>
    ) {
        synchronized(this) {
            val updatedEntries = (readEntries() + DiagnosticsEvent(
                timestampMillis = clock(),
                stage = stage,
                outcome = outcome,
                summary = summary,
                metadata = metadata
            )).takeLast(retentionLimit)
            writeEntries(updatedEntries)
        }
    }

    override fun snapshot(): List<DiagnosticsEvent> {
        synchronized(this) {
            return readEntries()
        }
    }

    override fun export(): DiagnosticsExport {
        val entries = snapshot().map { event ->
            DiagnosticsExportEntry(
                timestampMillis = event.timestampMillis,
                stage = event.stage,
                outcome = event.outcome,
                summary = event.summary,
                metadata = event.metadata.filterKeys { it in ALLOWED_EXPORT_METADATA_KEYS }
            )
        }
        return DiagnosticsExport(
            exportedAtMillis = clock(),
            notice = DEFAULT_EXPORT_NOTICE,
            entries = entries
        )
    }

    private fun readEntries(): List<DiagnosticsEvent> {
        if (!storageFile.exists()) {
            return emptyList()
        }
        return storageFile.readLines().mapNotNull(DiagnosticsCodec::decodeLine)
    }

    private fun writeEntries(entries: List<DiagnosticsEvent>) {
        storageFile.parentFile?.mkdirs()
        storageFile.writeText(
            entries.joinToString(separator = "\n", transform = DiagnosticsCodec::encode),
            UTF_8
        )
    }

}

fun DiagnosticsRecorder.recordSafely(
    stage: DiagnosticsStage,
    outcome: DiagnosticsOutcome,
    summary: String,
    metadata: Map<String, String> = emptyMap()
) {
    runCatching {
        record(stage = stage, outcome = outcome, summary = summary, metadata = metadata)
    }
}

private object DiagnosticsCodec {
    private val encoder = Base64.getEncoder()
    private val decoder = Base64.getDecoder()

    fun encode(event: DiagnosticsEvent): String {
        val metadata = event.metadata.entries.joinToString(";") { (key, value) ->
            "$key=${encodeText(value)}"
        }
        return listOf(
            event.timestampMillis.toString(),
            event.stage.name,
            event.outcome.name,
            encodeText(event.summary),
            metadata
        ).joinToString("|")
    }

    fun decodeLine(line: String): DiagnosticsEvent? {
        val parts = line.split("|", limit = 5)
        if (parts.size != 5) {
            return null
        }

        val timestamp = parts[0].toLongOrNull() ?: return null
        val stage = DiagnosticsStage.entries.find { it.name == parts[1] } ?: return null
        val outcome = DiagnosticsOutcome.entries.find { it.name == parts[2] } ?: return null
        val summary = decodeText(parts[3]) ?: return null
        val metadata = decodeMetadata(parts[4])

        return DiagnosticsEvent(
            timestampMillis = timestamp,
            stage = stage,
            outcome = outcome,
            summary = summary,
            metadata = metadata
        )
    }

    private fun decodeMetadata(encoded: String): Map<String, String> {
        if (encoded.isBlank()) {
            return emptyMap()
        }
        return buildMap {
            encoded.split(";")
                .filter { it.isNotBlank() }
                .forEach { item ->
                    val pair = item.split("=", limit = 2)
                    if (pair.size == 2) {
                        val key = pair[0]
                        val value = decodeText(pair[1])
                        if (key.isNotBlank() && value != null) {
                            put(key, value)
                        }
                    }
                }
        }
    }

    private fun encodeText(value: String): String {
        return encoder.encodeToString(value.toByteArray(UTF_8))
    }

    private fun decodeText(value: String): String? {
        return runCatching {
            String(decoder.decode(value), UTF_8)
        }.getOrNull()
    }
}
