package com.example.healthogram.organization

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class AppointmentType {
    DOCTOR_IN_PERSON,
    DOCTOR_VIRTUAL,
    CLINIC_VISIT,
    HOSPITAL_ENCOUNTER,
    LAB_SAMPLE_COLLECTION
}

enum class AppointmentStatus {
    REQUESTED,
    PENDING,
    BOOKED,
    CONFIRMED,
    RESCHEDULED,
    CANCELLED,
    COMPLETED,
    NO_SHOW,
    REJECTED,
    EXPIRED
}

data class AppointmentBooking(
    val appointmentId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val providerUid: String,
    val organizationId: String? = null,
    val departmentId: String? = null,
    val serviceId: String? = null,
    val providerType: String = "DOCTOR", // DOCTOR, CLINIC, HOSPITAL, LABORATORY
    val appointmentType: AppointmentType = AppointmentType.DOCTOR_IN_PERSON,
    val scheduledStart: Long,
    val scheduledEnd: Long,
    val timezone: String = "Asia/Muscat",
    val country: String = "OM",
    val status: AppointmentStatus = AppointmentStatus.BOOKED,
    val location: String = "Clinic Room 204",
    val virtual: Boolean = false,
    val virtualCallId: String? = null,
    val priceReference: Long? = null,
    val paymentStatus: String = "UNPAID", // UNPAID, AUTHORIZED, PAID, WAIVED
    val notesReference: String? = null,
    val cancellationReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AppointmentSlot(
    val slotId: String = UUID.randomUUID().toString(),
    val providerUid: String,
    val startTime: Long,
    val endTime: Long,
    val isAvailable: Boolean = true
)

/**
 * AppointmentService 2.1
 * Privacy-preserving appointment booking, rescheduling, and status management.
 * Invariant: Notifications never disclose specific diagnoses or sensitive clinical history.
 */
class AppointmentService private constructor() {

    private val appointments = ConcurrentHashMap<String, AppointmentBooking>()
    private val providerSlots = ConcurrentHashMap<String, MutableList<AppointmentSlot>>()

    companion object {
        @Volatile
        private var instance: AppointmentService? = null

        fun getInstance(): AppointmentService {
            return instance ?: synchronized(this) {
                instance ?: AppointmentService().also { instance = it }
            }
        }
    }

    data class PrivacySafeNotification(val title: String, val body: String)

    fun bookAppointment(
        patientUid: String,
        patientName: String,
        providerUid: String,
        providerName: String,
        providerType: String,
        facilityName: String,
        scheduledStart: Long,
        durationMinutes: Int = 30,
        appointmentType: String = "IN_PERSON",
        clinicalReason: String? = null
    ): AppointmentBooking? {
        val hasOverlap = appointments.values.any {
            it.providerUid == providerUid &&
            it.status != AppointmentStatus.CANCELLED &&
            it.scheduledStart == scheduledStart
        }
        if (hasOverlap) return null

        val apptTypeEnum = if (appointmentType == "VIRTUAL") AppointmentType.DOCTOR_VIRTUAL else AppointmentType.DOCTOR_IN_PERSON
        val booking = AppointmentBooking(
            patientUid = patientUid,
            providerUid = providerUid,
            providerType = providerType,
            appointmentType = apptTypeEnum,
            scheduledStart = scheduledStart,
            scheduledEnd = scheduledStart + (durationMinutes * 60 * 1000L),
            status = AppointmentStatus.CONFIRMED,
            location = facilityName,
            notesReference = clinicalReason
        )
        appointments[booking.appointmentId] = booking
        return booking
    }

    fun generatePrivacySafeNotification(booking: AppointmentBooking): PrivacySafeNotification {
        val safeBody = "Confirmed appointment at ${booking.location} with provider ID ${booking.providerUid}."
        return PrivacySafeNotification(
            title = "Appointment Confirmed",
            body = safeBody
        )
    }

    /**
     * Set provider available appointment slots
     */
    fun setProviderSlots(providerUid: String, slots: List<AppointmentSlot>) {
        val list = providerSlots.computeIfAbsent(providerUid) { mutableListOf() }
        synchronized(list) {
            list.clear()
            list.addAll(slots)
        }
    }

    fun getAvailableSlots(providerUid: String): List<AppointmentSlot> {
        return providerSlots[providerUid]?.filter { it.isAvailable } ?: emptyList()
    }

    /**
     * Book an appointment
     */
    fun bookAppointment(
        patientUid: String,
        providerUid: String,
        organizationId: String?,
        appointmentType: AppointmentType,
        slotId: String,
        location: String = "Main Facility"
    ): AppointmentBooking {
        val slots = providerSlots[providerUid] ?: throw IllegalStateException("Provider has no schedule")
        val slot = synchronized(slots) {
            val idx = slots.indexOfFirst { it.slotId == slotId && it.isAvailable }
            if (idx == -1) throw IllegalStateException("Selected slot is no longer available")
            val selected = slots[idx]
            slots[idx] = selected.copy(isAvailable = false)
            selected
        }

        val booking = AppointmentBooking(
            patientUid = patientUid,
            providerUid = providerUid,
            organizationId = organizationId,
            appointmentType = appointmentType,
            scheduledStart = slot.startTime,
            scheduledEnd = slot.endTime,
            status = AppointmentStatus.CONFIRMED,
            location = location,
            virtualCallId = if (appointmentType == AppointmentType.DOCTOR_VIRTUAL) "call_${UUID.randomUUID()}" else null
        )

        appointments[booking.appointmentId] = booking
        return booking
    }

    /**
     * Reschedule appointment
     */
    fun rescheduleAppointment(
        appointmentId: String,
        patientUid: String,
        newSlotId: String
    ): AppointmentBooking {
        val existing = appointments[appointmentId] ?: throw IllegalArgumentException("Appointment not found")
        require(existing.patientUid == patientUid) { "Unauthorized access to appointment" }
        require(existing.status != AppointmentStatus.CANCELLED) { "Cannot reschedule a cancelled appointment" }

        val slots = providerSlots[existing.providerUid] ?: throw IllegalStateException("Provider has no schedule")
        val newSlot = synchronized(slots) {
            val idx = slots.indexOfFirst { it.slotId == newSlotId && it.isAvailable }
            if (idx == -1) throw IllegalStateException("New slot is not available")
            val chosen = slots[idx]
            slots[idx] = chosen.copy(isAvailable = false)
            chosen
        }

        val updated = existing.copy(
            scheduledStart = newSlot.startTime,
            scheduledEnd = newSlot.endTime,
            status = AppointmentStatus.RESCHEDULED,
            updatedAt = System.currentTimeMillis()
        )
        appointments[appointmentId] = updated
        return updated
    }

    /**
     * Cancel appointment
     */
    fun cancelAppointment(
        appointmentId: String,
        actorUid: String,
        reason: String
    ): AppointmentBooking {
        val existing = appointments[appointmentId] ?: throw IllegalArgumentException("Appointment not found")
        require(existing.patientUid == actorUid || existing.providerUid == actorUid) {
            "Unauthorized to cancel appointment"
        }

        val updated = existing.copy(
            status = AppointmentStatus.CANCELLED,
            cancellationReason = reason,
            updatedAt = System.currentTimeMillis()
        )
        appointments[appointmentId] = updated
        return updated
    }

    fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: AppointmentStatus,
        actorUid: String,
        reason: String? = null
    ): AppointmentBooking {
        val existing = appointments[appointmentId] ?: throw IllegalArgumentException("Appointment not found")
        require(existing.patientUid == actorUid || existing.providerUid == actorUid) {
            "Unauthorized to update appointment status"
        }
        val updated = existing.copy(
            status = newStatus,
            cancellationReason = reason ?: existing.cancellationReason,
            updatedAt = System.currentTimeMillis()
        )
        appointments[appointmentId] = updated
        return updated
    }

    fun getPatientAppointments(patientUid: String): List<AppointmentBooking> {
        return appointments.values.filter { it.patientUid == patientUid }.sortedBy { it.scheduledStart }
    }

    fun getProviderAppointments(providerUid: String): List<AppointmentBooking> {
        return appointments.values.filter { it.providerUid == providerUid }.sortedBy { it.scheduledStart }
    }

    /**
     * Builds safe, non-identifying notification content
     */
    fun buildSafeNotificationText(booking: AppointmentBooking): String {
        val timeStr = java.time.Instant.ofEpochMilli(booking.scheduledStart).toString()
        return when (booking.status) {
            AppointmentStatus.CONFIRMED -> "Your appointment is confirmed for $timeStr. Location: ${booking.location}."
            AppointmentStatus.RESCHEDULED -> "Your appointment has been rescheduled to $timeStr."
            AppointmentStatus.CANCELLED -> "Your appointment has been cancelled. Reason: ${booking.cancellationReason ?: "Not specified"}."
            else -> "Appointment status update: ${booking.status}"
        }
    }

    /**
     * Step 37 / REQ-016: Privacy-Preserving iCalendar (.ics) Export
     * Formats RFC 5545 iCalendar data for device calendar integration.
     * INVARIANT: Never contains sensitive clinical notes or diagnosis codes.
     */
    fun generateIcsCalendarData(booking: AppointmentBooking): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
            .withZone(java.time.ZoneOffset.UTC)
        val dtStart = formatter.format(java.time.Instant.ofEpochMilli(booking.scheduledStart))
        val dtEnd = formatter.format(java.time.Instant.ofEpochMilli(booking.scheduledEnd))
        val dtStamp = formatter.format(java.time.Instant.ofEpochMilli(booking.createdAt))

        return """
            BEGIN:VCALENDAR
            VERSION:2.0
            PRODID:-//Healthogram//Healthcare Calendar Sync 2.2//EN
            CALSCALE:GREGORIAN
            METHOD:PUBLISH
            BEGIN:VEVENT
            UID:${booking.appointmentId}@healthogram.com
            DTSTAMP:$dtStamp
            DTSTART:$dtStart
            DTEND:$dtEnd
            SUMMARY:Healthogram Healthcare Appointment
            DESCRIPTION:Medical consultation with accredited healthcare provider. Ref: ${booking.appointmentId}
            LOCATION:${booking.location}
            STATUS:CONFIRMED
            CLASS:PRIVATE
            END:VEVENT
            END:VCALENDAR
        """.trimIndent()
    }

    fun clear() {
        appointments.clear()
        providerSlots.clear()
    }
}

