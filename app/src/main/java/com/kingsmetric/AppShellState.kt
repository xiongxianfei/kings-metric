package com.kingsmetric

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.kingsmetric.importflow.DraftField
import com.kingsmetric.importflow.DraftRecord
import com.kingsmetric.importflow.FieldKey
import com.kingsmetric.importflow.ReviewFlag

@Composable
internal fun rememberReviewDraftState(
    initialReviewDraft: DraftRecord?
): MutableState<DraftRecord?> {
    return rememberSaveable(saver = reviewDraftMutableStateSaver()) {
        mutableStateOf(initialReviewDraft)
    }
}

private fun reviewDraftMutableStateSaver(): Saver<MutableState<DraftRecord?>, Any> {
    return Saver(
        save = { state ->
            ReviewDraftStateCodec.save(state.value)
        },
        restore = { restored ->
            mutableStateOf(ReviewDraftStateCodec.restore(restored))
        }
    )
}

internal object ReviewDraftStateCodec {
    fun save(draft: DraftRecord?): Any {
        if (draft == null) {
            return mapOf("present" to false)
        }
        return buildMap<String, Any?> {
            put("present", true)
            put("screenshotId", draft.screenshotId)
            put("screenshotPath", draft.screenshotPath)
            put("isFinalized", draft.isFinalized)
            FieldKey.entries.forEach { fieldKey ->
                val field = draft.fields.getValue(fieldKey)
                put("field_${fieldKey.name}_value", field.value)
                put(
                    "field_${fieldKey.name}_flags",
                    ArrayList(field.flags.map(ReviewFlag::name))
                )
            }
        }
    }

    fun restore(restored: Any): DraftRecord? {
        val restoredMap = restored as Map<*, *>
        val present = restoredMap["present"] as? Boolean ?: false
        if (!present) {
            return null
        }
        val fields = mutableMapOf<FieldKey, DraftField>()
        FieldKey.entries.forEach { fieldKey ->
            val savedFlags = restoredMap["field_${fieldKey.name}_flags"] as? List<*> ?: emptyList<Any?>()
            val flags = savedFlags.mapNotNull { name ->
                val flagName = name as? String ?: return@mapNotNull null
                ReviewFlag.valueOf(flagName)
            }.toSet()
            fields[fieldKey] = DraftField(
                key = fieldKey,
                required = fieldKey.required,
                value = restoredMap["field_${fieldKey.name}_value"] as String?,
                flags = flags
            )
        }
        return DraftRecord(
            fields = fields,
            screenshotId = restoredMap["screenshotId"] as String?,
            screenshotPath = restoredMap["screenshotPath"] as String?,
            isFinalized = restoredMap["isFinalized"] as? Boolean ?: false
        )
    }
}
