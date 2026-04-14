package com.kingsmetric.app

import com.kingsmetric.importflow.FieldKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedUxCopyTest {

    @Test
    fun `T1 shared field labels return user-facing labels for all structured fields`() {
        val labels = FieldKey.entries.associateWith { SharedUxCopy.field(it) }

        assertEquals(FieldKey.entries.size, labels.size)
        assertEquals("Hero", labels.getValue(FieldKey.HERO).label)
        assertEquals("Damage Share", labels.getValue(FieldKey.DAMAGE_SHARE).label)
        assertEquals("Team Participation", labels.getValue(FieldKey.PARTICIPATION_RATE).label)
        assertFalse(labels.values.any { field -> field.label == field.key.name })
    }

    @Test
    fun `T2 shared state messages return non-technical copy for common app states`() {
        val loading = SharedUxCopy.message(SharedMessageKey.APP_LOADING)
        val blockedSave = SharedUxCopy.message(SharedMessageKey.REVIEW_BLOCKED_SAVE)
        val success = SharedUxCopy.message(SharedMessageKey.REVIEW_SAVE_SUCCESS)

        assertEquals("Loading your matches...", loading.text)
        assertEquals("Complete the required fields before saving.", blockedSave.text)
        assertEquals("Match saved.", success.text)
    }

    @Test
    fun `T3 shared copy preserves required and blocking semantics in metadata`() {
        val heroField = SharedUxCopy.field(FieldKey.HERO)
        val lastHitsField = SharedUxCopy.field(FieldKey.LAST_HITS)
        val blockedSave = SharedUxCopy.message(SharedMessageKey.REVIEW_BLOCKED_SAVE)
        val reviewNeeded = SharedUxCopy.message(SharedMessageKey.REVIEW_NEEDS_ATTENTION)

        assertTrue(heroField.required)
        assertFalse(lastHitsField.required)
        assertTrue(blockedSave.blocking)
        assertFalse(reviewNeeded.blocking)
    }

    @Test
    fun `T4 missing preview message explains that field data remains available`() {
        val message = SharedUxCopy.message(SharedMessageKey.MISSING_SCREENSHOT_PREVIEW)

        assertEquals(
            "Screenshot preview unavailable. Match data is still available below.",
            message.text
        )
    }

    @Test
    fun `T5 unsupported screenshot message is retry-oriented`() {
        val message = SharedUxCopy.message(SharedMessageKey.IMPORT_UNSUPPORTED)

        assertEquals(
            "This screenshot isn't supported. Try another post-match personal stats screenshot.",
            message.text
        )
        assertTrue(message.suggestsRetry)
    }

    @Test
    fun `T6 blocked save and local save failure remain distinct`() {
        val blocked = SharedUxCopy.message(SharedMessageKey.REVIEW_BLOCKED_SAVE)
        val failed = SharedUxCopy.message(SharedMessageKey.REVIEW_SAVE_FAILED)

        assertEquals("Complete the required fields before saving.", blocked.text)
        assertEquals("Could not save this match locally. Try again.", failed.text)
        assertTrue(blocked.blocking)
        assertFalse(failed.blocking)
        assertTrue(failed.suggestsRetry)
    }

    @Test
    fun `T7 import copy uses user-facing action labels and status wording`() {
        val action = SharedUxCopy.message(SharedMessageKey.IMPORT_ACTION)
        val idle = SharedUxCopy.message(SharedMessageKey.IMPORT_IDLE)

        assertEquals("Import Screenshot", action.text)
        assertEquals("Select one supported screenshot to start review.", idle.text)
    }

    @Test
    fun `T8 shared labels and messages come from centralized mappings`() {
        assertEquals(FieldKey.entries.toSet(), SharedUxCopy.fieldCopy.keys)
        assertTrue(SharedMessageKey.entries.all { it in SharedUxCopy.stateCopy.keys })
    }
}
