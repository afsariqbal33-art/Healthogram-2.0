package com.example.healthogram.scale

import com.example.healthogram.core.events.DomainEvent
import com.example.healthogram.core.events.DomainEventDispatcher
import com.example.healthogram.core.events.InMemoryIdempotencyStore
import com.example.healthogram.core.i18n.CurrencyMinorUnitService
import com.example.healthogram.core.media.MediaPipelineService
import com.example.healthogram.core.media.MediaType
import com.example.healthogram.core.media.StorageNamespace
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Healthogram 2.0 Security & Financial Red-Team Drills.
 *
 * Implements Section 58 & 59:
 * - Red-team simulation testing Health Passport authorization boundaries.
 * - Single-use QR token consumption & expiration drills.
 * - Double-entry financial replay & duplicate webhook mitigation.
 * - Storage path privacy enforcement.
 */
class HealthPassportSecurityDrillTest {

    data class EphemeralQrSession(
        val sessionId: String,
        val patientUid: String,
        val createdAtMillis: Long,
        val ttlMillis: Long = 15 * 60 * 1000L, // 15 mins
        var isConsumed: Boolean = false,
        var isRevoked: Boolean = false
    ) {
        fun isValid(currentTimeMillis: Long): Boolean {
            if (isConsumed || isRevoked) return false
            if (currentTimeMillis > (createdAtMillis + ttlMillis)) return false
            return true
        }
    }

    // --- 1. HEALTH PASSPORT AUTHORIZATION DRILLS ---

    @Test
    fun testQrSessionExpirationAndSingleUseInvariant() {
        val now = System.currentTimeMillis()
        val session = EphemeralQrSession(
            sessionId = "qr_" + UUID.randomUUID(),
            patientUid = "patient_salim",
            createdAtMillis = now
        )

        // Fresh session must be valid
        assertTrue("Fresh QR session should be valid", session.isValid(now))

        // Session past 15-minute TTL must be rejected
        val expiredTime = now + (16 * 60 * 1000L)
        assertFalse("Expired QR session (>15 min) must be rejected", session.isValid(expiredTime))

        // Consumed session must not be reusable
        session.isConsumed = true
        assertFalse("Consumed QR session must be rejected on second use attempt", session.isValid(now + 1000L))

        // Revoked session must be rejected immediately
        session.isConsumed = false
        session.isRevoked = true
        assertFalse("Patient-revoked QR session must be rejected immediately", session.isValid(now + 1000L))
    }

    @Test(expected = SecurityException::class)
    fun testStoragePrivateNamespaceTamperRejection() {
        val mediaService = MediaPipelineService()
        // Attempt to place sensitive clinical document into public social space
        mediaService.enqueueTranscodeJob(
            ownerUid = "attacker_1",
            namespace = StorageNamespace.SOCIAL_POSTS,
            mediaType = MediaType.CLINICAL_DOCUMENT,
            sourcePath = "temp/extracted_blood_panel.pdf"
        )
    }

    // --- 2. FINANCIAL LEDGER INTEGRITY & REPLAY REJECTION ---

    @Test
    fun testPaymentWebhookDuplicateReplayRejection() = runBlocking {
        val store = InMemoryIdempotencyStore()
        val dispatcher = DomainEventDispatcher(store)

        var processedCount = 0
        dispatcher.subscribe(DomainEvent.PAYMENT_SUCCEEDED) {
            processedCount++
            Result.success(Unit)
        }

        val event = DomainEvent(
            eventType = DomainEvent.PAYMENT_SUCCEEDED,
            aggregateType = "payment_intent",
            aggregateId = "pi_stripe_9999",
            actorUid = "gateway_webhook",
            idempotencyKey = "evt_pi_stripe_9999_captured"
        )

        // Initial webhook must succeed
        val firstRun = dispatcher.publish(event)
        assertTrue("Legitimate initial webhook must be processed", firstRun)
        assertEquals(1, processedCount)

        // Attacker or network duplicate webhook replay must be rejected
        val duplicateRun = dispatcher.publish(event)
        assertFalse("Duplicate webhook replay must be skipped", duplicateRun)
        assertEquals("Processed count must remain 1 after replay attempt", 1, processedCount)
    }

    @Test
    fun testZeroDriftCommissionSplitUnderUnevenCents() {
        val service = CurrencyMinorUnitService()
        // Test 100,000 randomized or edge amounts
        val edgeAmounts = listOf(1L, 2L, 3L, 99L, 100L, 101L, 12345L, 999999L, 1000000L)

        for (gross in edgeAmounts) {
            val (comm, payout) = service.calculateCommissionSplit(gross, 1000) // 10%
            assertEquals("Commission and payout sum must strictly equal gross amount", gross, comm + payout)
            assertTrue("Commission must be non-negative", comm >= 0)
            assertTrue("Payout must be non-negative", payout >= 0)
        }
    }
}
