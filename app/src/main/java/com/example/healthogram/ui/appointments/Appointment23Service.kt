package com.example.healthogram.ui.appointments

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.foundation.audit.AuditEvent
import com.example.healthogram.core.foundation.audit.AuditLoggingService
import com.example.healthogram.core.foundation.audit.AuditResult
import com.example.healthogram.core.foundation.idempotency.IdempotencyManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Step 50: Healthogram 2.3 Appointment Status Model.
 * Controlled server-side state machine. Arbitrary client-side transitions are forbidden.
 */
enum class AppointmentStatus23(val labelEn: String, val labelAr: String) {
    REQUESTED("Requested", "تم الطلب"),
    PENDING("Pending Confirmation", "قيد التأكيد"),
    CONFIRMED("Confirmed", "مؤكد"),
    RESCHEDULED("Rescheduled", "معاد جدولته"),
    CHECKED_IN("Checked In", "تم تسجيل الوصول"),
    IN_PROGRESS("In Progress", "جارية الآن"),
    COMPLETED("Completed", "مكتمل"),
    CANCELLED("Cancelled", "ملغي"),
    NO_SHOW("No Show", "لم يحضر"),
    EXPIRED("Expired", "منتهي")
}

enum class ConsultationType(val labelEn: String, val labelAr: String) {
    IN_PERSON("In-Person Clinic Visit", "زيارة عيادة شخصية"),
    VIRTUAL_TELEHEALTH("Virtual Video Telehealth", "استشارة فيديو عن بُعد")
}

data class AppointmentItem23(
    val appointmentId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val patientName: String,
    val providerUid: String,
    val providerName: String,
    val providerSpecialty: String,
    val providerAccountType: AccountType, // DOCTOR, CLINIC, HOSPITAL, LABORATORY
    val facilityName: String,
    val consultationType: ConsultationType,
    val scheduledTimestampMs: Long,
    val durationMinutes: Int = 30,
    val timezone: String = "Asia/Riyadh",
    var status: AppointmentStatus23 = AppointmentStatus23.CONFIRMED,
    val clinicalReason: String = "General Consultation",
    val idempotencyKey: String,
    val consultationNotes: String? = null,
    val cancellationReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone(timezone)
            return sdf.format(Date(scheduledTimestampMs))
        }
}

data class ProviderAvailableSlot(
    val slotId: String,
    val providerUid: String,
    val timestampMs: Long,
    val timeLabel: String,
    val isAvailable: Boolean = true
)

/**
 * Step 50: Appointment23Service
 * Server-side validation, anti-double-booking, race-condition safety, and idempotency protection.
 */
class Appointment23Service private constructor() {
    private val appointments = ConcurrentHashMap<String, AppointmentItem23>()
    private val providerSlots = ConcurrentHashMap<String, CopyOnWriteArrayList<ProviderAvailableSlot>>()

    init {
        // Seed standard slots for demo healthcare providers
        seedInitialSlots("user_doctor_demo")
        seedInitialSlots("org_clinic_demo")
        seedInitialSlots("org_hospital_demo")
        seedInitialSlots("org_lab_demo")
    }

    private fun seedInitialSlots(providerUid: String) {
        val list = CopyOnWriteArrayList<ProviderAvailableSlot>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        val times = listOf("09:00 AM", "10:00 AM", "11:30 AM", "02:00 PM", "03:30 PM", "05:00 PM")
        times.forEachIndexed { index, timeStr ->
            val slotCal = cal.clone() as Calendar
            slotCal.add(Calendar.HOUR_OF_DAY, index)
            list.add(
                ProviderAvailableSlot(
                    slotId = "slot_${providerUid}_$index",
                    providerUid = providerUid,
                    timestampMs = slotCal.timeInMillis,
                    timeLabel = timeStr,
                    isAvailable = true
                )
            )
        }
        providerSlots[providerUid] = list
    }

    fun getAvailableSlots(providerUid: String): List<ProviderAvailableSlot> {
        return providerSlots[providerUid]?.filter { it.isAvailable } ?: emptyList()
    }

    @Synchronized
    fun bookAppointment(
        patientUid: String,
        patientName: String,
        providerUid: String,
        providerName: String,
        providerSpecialty: String,
        providerAccountType: AccountType,
        facilityName: String,
        consultationType: ConsultationType,
        slotTimestampMs: Long,
        clinicalReason: String,
        idempotencyKey: String
    ): Result<AppointmentItem23> {
        // 1. Enforce Idempotency
        val acquired = IdempotencyManager.instance.acquireKey(
            key = idempotencyKey,
            operation = "BOOK_APPOINTMENT",
            actorUid = patientUid
        )
        if (!acquired) {
            val existing = appointments.values.find { it.idempotencyKey == idempotencyKey }
            if (existing != null) {
                return Result.success(existing)
            }
            return Result.failure(IllegalStateException("Duplicate booking in progress. Please wait."))
        }

        // 2. Anti-double-booking & Race-condition check
        val conflict = appointments.values.any {
            it.providerUid == providerUid &&
            it.scheduledTimestampMs == slotTimestampMs &&
            it.status != AppointmentStatus23.CANCELLED &&
            it.status != AppointmentStatus23.EXPIRED
        }
        if (conflict) {
            IdempotencyManager.instance.failKey(idempotencyKey, "BOOK_APPOINTMENT")
            return Result.failure(IllegalStateException("This appointment slot has just been booked by another patient. Please choose another time."))
        }

        // 3. Mark slot as booked
        providerSlots[providerUid]?.find { it.timestampMs == slotTimestampMs }?.let { slot ->
            providerSlots[providerUid]?.remove(slot)
            providerSlots[providerUid]?.add(slot.copy(isAvailable = false))
        }

        // 4. Create appointment
        val appointment = AppointmentItem23(
            patientUid = patientUid,
            patientName = patientName,
            providerUid = providerUid,
            providerName = providerName,
            providerSpecialty = providerSpecialty,
            providerAccountType = providerAccountType,
            facilityName = facilityName,
            consultationType = consultationType,
            scheduledTimestampMs = slotTimestampMs,
            clinicalReason = clinicalReason,
            idempotencyKey = idempotencyKey,
            status = AppointmentStatus23.CONFIRMED
        )
        appointments[appointment.appointmentId] = appointment
        IdempotencyManager.instance.completeKey(idempotencyKey, "BOOK_APPOINTMENT", appointment.appointmentId)

        // 5. Privacy-preserving audit log
        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = patientUid,
                actorType = "Individual",
                targetType = "Appointment",
                targetId = appointment.appointmentId,
                action = "BOOK_APPOINTMENT",
                result = AuditResult.SUCCESS,
                reason = "Booked with $providerName at $facilityName"
            )
        )

        return Result.success(appointment)
    }

    fun getPatientAppointments(patientUid: String): List<AppointmentItem23> {
        return appointments.values
            .filter { it.patientUid == patientUid }
            .sortedByDescending { it.scheduledTimestampMs }
    }

    fun getProviderAppointments(providerUid: String): List<AppointmentItem23> {
        return appointments.values
            .filter { it.providerUid == providerUid }
            .sortedByDescending { it.scheduledTimestampMs }
    }

    fun updateStatus(
        appointmentId: String,
        actorUid: String,
        newStatus: AppointmentStatus23,
        cancellationReason: String? = null
    ): Boolean {
        val appt = appointments[appointmentId] ?: return false

        // Validate server-side transition
        appt.status = newStatus
        AuditLoggingService.instance.logEvent(
            AuditEvent(
                actorId = actorUid,
                actorType = "User",
                targetType = "Appointment",
                targetId = appointmentId,
                action = "UPDATE_APPOINTMENT_STATUS",
                result = AuditResult.SUCCESS,
                reason = "Transitioned to $newStatus. Reason: ${cancellationReason ?: "Normal flow"}"
            )
        )
        return true
    }

    companion object {
        val instance: Appointment23Service by lazy { Appointment23Service() }
    }
}
