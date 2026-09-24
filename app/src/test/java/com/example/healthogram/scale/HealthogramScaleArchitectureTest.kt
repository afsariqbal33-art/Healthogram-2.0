package com.example.healthogram.scale

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.events.DomainEvent
import com.example.healthogram.core.events.DomainEventDispatcher
import com.example.healthogram.core.events.InMemoryIdempotencyStore
import com.example.healthogram.core.i18n.CountryRegistry
import com.example.healthogram.core.i18n.CurrencyMinorUnitService
import com.example.healthogram.core.media.MediaPipelineService
import com.example.healthogram.core.media.MediaType
import com.example.healthogram.core.media.StorageNamespace
import com.example.healthogram.core.search.InMemorySearchProvider
import com.example.healthogram.core.search.SearchDocument
import com.example.healthogram.core.search.SearchEntityType
import com.example.healthogram.core.search.SearchQuery
import com.example.healthogram.core.tasks.DeadLetterQueueService
import com.example.healthogram.core.tasks.TaskQueue
import com.example.healthogram.social.FanOutStrategy
import com.example.healthogram.social.FeedScalingService
import com.example.healthogram.social.RankingStrategy
import com.example.healthogram.social.ScaledFeedItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Healthogram 2.0 Architectural Invariant and Scalability Tests.
 * Verifies domain events, hybrid feed scaling, search abstraction,
 * high-precision minor-unit arithmetic, media storage isolation, and dead-letter handling.
 */
class HealthogramScaleArchitectureTest {

    // --- 1. DOMAIN EVENTS & IDEMPOTENCY ---

    @Test
    fun testDomainEventDispatchAndIdempotentDeduplication() = runBlocking {
        val idempotencyStore = InMemoryIdempotencyStore()
        val dispatcher = DomainEventDispatcher(idempotencyStore)

        var eventCount = 0
        dispatcher.subscribe(DomainEvent.ORDER_PLACED) {
            eventCount++
            Result.success(Unit)
        }

        val event = DomainEvent(
            eventType = DomainEvent.ORDER_PLACED,
            aggregateType = "order",
            aggregateId = "ord_1001",
            actorUid = "user_abc",
            idempotencyKey = "idemp_unique_key_99"
        )

        // First publication must succeed and trigger listener
        val firstPublish = dispatcher.publish(event)
        assertTrue("First event publish should succeed", firstPublish)
        assertEquals("Listener should have fired once", 1, eventCount)

        // Duplicate publication with same idempotencyKey must be skipped
        val duplicatePublish = dispatcher.publish(event)
        assertFalse("Duplicate event publish should be skipped", duplicatePublish)
        assertEquals("Listener should not fire a second time for duplicate", 1, eventCount)
    }

    // --- 2. HYBRID FEED SCALING & RANKING ---

    @Test
    fun testFeedScalingStrategyDetermination() {
        val feedService = FeedScalingService(highVolumeThreshold = 25_000)

        // Regular accounts use Fan-Out on Write
        val regularStrategy = feedService.determineFanOutStrategy(followerCount = 1_200)
        assertEquals(FanOutStrategy.FAN_OUT_ON_WRITE, regularStrategy)

        // Viral creator or major hospital uses Fan-Out on Read
        val highVolumeStrategy = feedService.determineFanOutStrategy(followerCount = 120_000)
        assertEquals(FanOutStrategy.FAN_OUT_ON_READ, highVolumeStrategy)
    }

    @Test
    fun testFeedRankingHealthcareBoost() {
        val feedService = FeedScalingService()
        val now = System.currentTimeMillis()

        val regularItem = ScaledFeedItem(
            postId = "post_1",
            authorUid = "user_1",
            authorAccountType = AccountType.INDIVIDUAL,
            authorVerified = false,
            publishedAtMillis = now - 3600_000L, // 1 hr ago
            likeCount = 100,
            commentCount = 20,
            shareCount = 5
        )

        val verifiedDoctorItem = ScaledFeedItem(
            postId = "post_2",
            authorUid = "dr_salim",
            authorAccountType = AccountType.DOCTOR,
            authorVerified = true,
            publishedAtMillis = now - 3600_000L, // 1 hr ago
            likeCount = 100,
            commentCount = 20,
            shareCount = 5
        )

        val regularScore = feedService.calculateRankingScore(
            regularItem,
            RankingStrategy.HEALTHCARE_VERIFIED_BOOST,
            currentTimeMillis = now
        )

        val doctorScore = feedService.calculateRankingScore(
            verifiedDoctorItem,
            RankingStrategy.HEALTHCARE_VERIFIED_BOOST,
            currentTimeMillis = now
        )

        assertTrue("Verified healthcare professional content should receive boost", doctorScore > regularScore)
    }

    // --- 3. SEARCH PROVIDER ABSTRACTION & SECURITY ISOLATION ---

    @Test
    fun testSearchProviderIndexingAndFiltering() = runBlocking {
        val search = InMemorySearchProvider()

        val clinicDoc = SearchDocument(
            documentId = "clinic_muscat",
            entityType = SearchEntityType.CLINIC,
            title = "Muscat Specialized Eye Clinic",
            subtitle = "Ophthalmology & Surgery",
            tags = listOf("lasik", "eye", "vision"),
            country = "OM",
            city = "Muscat",
            isVerified = true,
            rating = 4.9
        )

        val productDoc = SearchDocument(
            documentId = "prod_wheelchair",
            entityType = SearchEntityType.PRODUCT,
            title = "Ergonomic Wheelchair Mobility Pro",
            subtitle = "Medical Mobility Equipment",
            tags = listOf("wheelchair", "mobility"),
            country = "OM",
            city = "Muscat",
            isVerified = true,
            priceInCents = 15000L,
            rating = 4.7
        )

        search.indexDocument(clinicDoc)
        search.indexDocument(productDoc)

        // Query for clinic
        val queryClinic = SearchQuery(
            term = "eye clinic",
            entityTypes = setOf(SearchEntityType.CLINIC),
            country = "OM"
        )
        val clinicResults = search.search(queryClinic)
        assertEquals(1, clinicResults.totalHits)
        assertEquals("clinic_muscat", clinicResults.documents.first().documentId)

        // Query for product with price bounds
        val queryProduct = SearchQuery(
            term = "wheelchair",
            minPriceInCents = 10000L,
            maxPriceInCents = 20000L
        )
        val productResults = search.search(queryProduct)
        assertEquals(1, productResults.totalHits)
        assertEquals("prod_wheelchair", productResults.documents.first().documentId)
    }

    @Test(expected = SecurityException::class)
    fun testSearchProviderRejectsClinicalHealthData() = runBlocking {
        val search = InMemorySearchProvider()
        val clinicalLeakDoc = SearchDocument(
            documentId = "leak_01",
            entityType = SearchEntityType.USER,
            title = "Patient Health_Passport Diagnosis Record",
            description = "Prescription and lab_result"
        )
        // Must throw SecurityException
        search.indexDocument(clinicalLeakDoc)
        Unit
    }

    // --- 4. MULTI-COUNTRY & MINOR-UNIT ARITHMETIC ---

    @Test
    fun testCountryRegistryConfigs() {
        val oman = CountryRegistry.getCountry("OM")
        assertEquals("OMR", oman.defaultCurrency)
        assertEquals(1000, oman.subunitFactor)
        assertEquals(500, oman.vatBasisPoints)
        assertTrue(oman.isMarketplaceEnabled)

        val saudi = CountryRegistry.getCountry("SA")
        assertEquals("SAR", saudi.defaultCurrency)
        assertEquals(100, saudi.subunitFactor)
        assertEquals(1500, saudi.vatBasisPoints)
    }

    @Test
    fun testZeroDriftCommissionSplit() {
        val currencyService = CurrencyMinorUnitService()

        // Test with 50.000 OMR (50,000 Baiza)
        val grossBaiza = 50_000L
        val (commission, sellerPayout) = currencyService.calculateCommissionSplit(grossBaiza, 1000)

        assertEquals(5_000L, commission)
        assertEquals(45_000L, sellerPayout)
        assertEquals(grossBaiza, commission + sellerPayout)

        // Test with uneven odd number to verify zero penny drift
        val oddGross = 12_347L
        val (oddCommission, oddPayout) = currencyService.calculateCommissionSplit(oddGross, 1000)
        assertEquals(oddGross, oddCommission + oddPayout)
    }

    // --- 5. MEDIA PIPELINE & STORAGE NAMESPACE ISOLATION ---

    @Test
    fun testMediaStorageNamespaceIsolation() {
        val mediaService = MediaPipelineService()

        // Public video in public namespace should succeed
        val reelJob = mediaService.enqueueTranscodeJob(
            ownerUid = "creator_1",
            namespace = StorageNamespace.SOCIAL_REELS,
            mediaType = MediaType.REEL_VIDEO,
            sourcePath = "temp/raw_reel_1.mp4"
        )
        assertNotNull(reelJob.jobId)
        assertTrue(reelJob.isExifScrubbed)

        // Clinical document in private namespace should succeed
        val clinicalJob = mediaService.enqueueTranscodeJob(
            ownerUid = "patient_1",
            namespace = StorageNamespace.HEALTH_PRIVATE,
            mediaType = MediaType.CLINICAL_DOCUMENT,
            sourcePath = "temp/lab_report.pdf"
        )
        assertNotNull(clinicalJob.jobId)
    }

    @Test(expected = SecurityException::class)
    fun testMediaServiceBarsClinicalDocumentsFromPublicNamespace() {
        val mediaService = MediaPipelineService()
        // Attempting to upload clinical doc to social reels must fail
        mediaService.enqueueTranscodeJob(
            ownerUid = "patient_1",
            namespace = StorageNamespace.SOCIAL_REELS,
            mediaType = MediaType.CLINICAL_DOCUMENT,
            sourcePath = "temp/lab_report.pdf"
        )
    }

    // --- 6. TASK QUEUES & DEAD-LETTER RECOVERY ---

    @Test
    fun testDeadLetterQueueAndBackoff() {
        val taskService = DeadLetterQueueService()

        val backoff1 = taskService.calculateBackoffMillis(1, baseDelayMillis = 1000L)
        val backoff3 = taskService.calculateBackoffMillis(3, baseDelayMillis = 1000L)
        assertTrue("Higher retry attempt should have greater backoff", backoff3 > backoff1)

        val deadLetter = taskService.recordDeadLetter(
            jobId = "transcode_fail_1",
            queue = TaskQueue.VIDEO_PROCESSING,
            payloadJson = "{\"video_id\": \"vid_99\"}",
            attemptCount = 3,
            lastError = "Codec unsupported"
        )

        assertFalse(deadLetter.isResolved)
        assertEquals(1, taskService.getUnresolvedDeadLetters().size)

        // Operator replay
        val replayed = taskService.replayJob(deadLetter.deadLetterId)
        assertTrue(replayed)
        assertEquals(0, taskService.getUnresolvedDeadLetters().size)
    }
}
