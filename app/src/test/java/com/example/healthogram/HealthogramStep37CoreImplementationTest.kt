package com.example.healthogram

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.FeatureFlagService
import com.example.healthogram.delivery.CustomerDeliveryAddress
import com.example.healthogram.delivery.DeliveryEngine
import com.example.healthogram.delivery.DeliveryRepository
import com.example.healthogram.healthpassport.HealthConnectRecordData
import com.example.healthogram.healthpassport.HealthConnectService
import com.example.healthogram.healthpassport.HealthConnectStatus
import com.example.healthogram.healthpassport.VitalTrendData
import com.example.healthogram.healthpassport.VitalTrendPoint
import com.example.healthogram.organization.AppointmentBooking
import com.example.healthogram.organization.AppointmentService
import com.example.healthogram.organization.AppointmentStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Healthogram Step 37: Core Implementation & Technical Debt Test Suite
 * Validates 2.2 foundations, OEM battery resilience, .ics calendar export,
 * multi-address delivery book, and strict clinical isolation invariants.
 */
class HealthogramStep37CoreImplementationTest {

    private lateinit var featureFlags: FeatureFlagService
    private lateinit var healthConnect: HealthConnectService
    private lateinit var appointmentService: AppointmentService
    private lateinit var deliveryEngine: DeliveryEngine

    @Before
    fun setUp() {
        featureFlags = FeatureFlagService.getInstance()
        healthConnect = HealthConnectService.getInstance()
        healthConnect.clear()
        appointmentService = AppointmentService.getInstance()
        appointmentService.clear()
        deliveryEngine = DeliveryEngine.getInstance()
    }

    @Test
    fun testHealthogram22FeatureFlags_RegisteredAndDefaultCanaryDisabled() {
        val flagsToCheck = listOf(
            FeatureFlagService.FLAG_HEALTH_PASSPORT_2_2,
            FeatureFlagService.FLAG_FHIR_2_2,
            FeatureFlagService.FLAG_HEALTH_CONNECT_2_2,
            FeatureFlagService.FLAG_APPOINTMENTS_2_2,
            FeatureFlagService.FLAG_MARKETPLACE_2_2,
            FeatureFlagService.FLAG_AI_2_2,
            FeatureFlagService.FLAG_TRANSLATION_2_2
        )

        for (key in flagsToCheck) {
            val flag = featureFlags.getFlag(key)
            assertNotNull("Feature flag $key must be registered", flag)
            assertFalse("Feature flag $key must default to disabled for canary rollout", flag!!.enabled)
            assertEquals("Default rollout phase must be PHASE_2_ALPHA", "PHASE_2_ALPHA", flag.rolloutPhase)
        }
    }

    @Test
    fun testInternationalMarketplace_PermanentlyDisabledInvariant() {
        val flag = featureFlags.getFlag(FeatureFlagService.FLAG_INTERNATIONAL_MARKETPLACE)
        assertNotNull("International marketplace flag must exist", flag)
        assertFalse("International marketplace must remain permanently disabled in v2.2", flag!!.enabled)
        assertEquals(0, flag.rolloutPercentage)
    }

    @Test
    fun testHealthConnect_OemBatteryExemptionGuidance_DEBT01() {
        val xiaomiGuidance = healthConnect.getOemExemptionGuidance("Xiaomi")
        assertEquals("Xiaomi", xiaomiGuidance.manufacturer)
        assertTrue(xiaomiGuidance.requiresManualExemption)
        assertTrue(xiaomiGuidance.stepInstructions.any { it.contains("Autostart") })

        val huaweiGuidance = healthConnect.getOemExemptionGuidance("HUAWEI P40")
        assertEquals("Huawei", huaweiGuidance.manufacturer)
        assertTrue(huaweiGuidance.requiresManualExemption)

        val samsungGuidance = healthConnect.getOemExemptionGuidance("Samsung Galaxy S24")
        assertEquals("Samsung", samsungGuidance.manufacturer)
        assertFalse(samsungGuidance.requiresManualExemption)
    }

    @Test
    fun testHealthConnect_OpportunisticSyncExecution() {
        val uid = "patient_step37_test_001"
        healthConnect.requestAndGrantPermissions(uid, setOf("STEPS", "HEART_RATE"))
        assertEquals(HealthConnectStatus.CONNECTED, healthConnect.getConnection(uid).connectionStatus)

        val records = listOf(
            HealthConnectRecordData(type = "STEPS", numericValue = 6500.0, unit = "count", timestamp = System.currentTimeMillis()),
            HealthConnectRecordData(type = "HEART_RATE", numericValue = 72.0, unit = "bpm", timestamp = System.currentTimeMillis())
        )

        val job = healthConnect.triggerOpportunisticSync(uid, records)
        assertEquals("COMPLETED", job.status)
        assertEquals(2, job.recordsProcessed)
        assertEquals(0, job.recordsFailed)
    }

    @Test
    fun testHealthConnect_NegativeCase_UnsupportedDataTypeRejected() {
        val uid = "patient_unsupported_test"
        try {
            healthConnect.requestAndGrantPermissions(uid, setOf("NON_EXISTENT_METRIC"))
            fail("Should fail when requesting unsupported health data type")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Must request at least one valid"))
        }
    }

    @Test
    fun testAppointmentCalendarSync_IcsFormatGeneration() {
        val booking = AppointmentBooking(
            patientUid = "patient_cal_01",
            providerUid = "doctor_cal_01",
            location = "Sultan Qaboos Hospital, Suite 302",
            scheduledStart = 1758362400000L, // UTC fixed epoch
            scheduledEnd = 1758364200000L,
            status = AppointmentStatus.CONFIRMED
        )

        val icsData = appointmentService.generateIcsCalendarData(booking)
        assertTrue("Must contain VCALENDAR opening tag", icsData.contains("BEGIN:VCALENDAR"))
        assertTrue("Must contain VEVENT opening tag", icsData.contains("BEGIN:VEVENT"))
        assertTrue("Must contain UID with appointment ID", icsData.contains("UID:${booking.appointmentId}@healthogram.com"))
        assertTrue("Must declare privacy as PRIVATE", icsData.contains("CLASS:PRIVATE"))
        assertFalse("Must NOT leak sensitive diagnosis", icsData.contains("diagnosis"))
        assertTrue("Must contain VEVENT closing tag", icsData.contains("END:VEVENT"))
        assertTrue("Must contain VCALENDAR closing tag", icsData.contains("END:VCALENDAR"))
    }

    @Test
    fun testDeliveryEngine_CustomerMultiAddressBook() {
        val customerUid = "cust_multi_addr_01"
        val homeAddress = CustomerDeliveryAddress(
            customerUid = customerUid,
            countryCode = "SA",
            fullName = "Tariq Al-Farsi",
            phoneReference = "+966501234567",
            addressLine1 = "King Fahd Road 104",
            city = "Riyadh",
            region = "Riyadh Province",
            postalCode = "11564",
            addressType = "home",
            isDefault = true
        )

        val workAddress = CustomerDeliveryAddress(
            customerUid = customerUid,
            countryCode = "SA",
            fullName = "Tariq Al-Farsi",
            phoneReference = "+966501234567",
            addressLine1 = "Digital City Building 3",
            city = "Riyadh",
            region = "Riyadh Province",
            postalCode = "11564",
            addressType = "work",
            isDefault = false
        )

        deliveryEngine.saveCustomerAddress(customerUid, homeAddress)
        deliveryEngine.saveCustomerAddress(customerUid, workAddress)

        val addressBook = deliveryEngine.getCustomerAddressBook(customerUid)
        assertEquals(2, addressBook.size)
        assertTrue(addressBook.any { it.addressType == "home" && it.isDefault })
        assertTrue(addressBook.any { it.addressType == "work" && !it.isDefault })
    }

    @Test
    fun testVitalTrendData_CanvasModelValidation() {
        val points = listOf(
            VitalTrendPoint(timestamp = 1000L, value = 118f, label = "08:00"),
            VitalTrendPoint(timestamp = 2000L, value = 122f, label = "12:00"),
            VitalTrendPoint(timestamp = 3000L, value = 120f, label = "18:00")
        )

        val trendData = VitalTrendData(
            metricName = "Systolic Blood Pressure",
            unit = "mmHg",
            points = points,
            normalRangeMin = 90f,
            normalRangeMax = 120f
        )

        assertEquals("Systolic Blood Pressure", trendData.metricName)
        assertEquals(3, trendData.points.size)
        assertEquals(120f, trendData.normalRangeMax)
        assertEquals("mmHg", trendData.unit)
    }
}
