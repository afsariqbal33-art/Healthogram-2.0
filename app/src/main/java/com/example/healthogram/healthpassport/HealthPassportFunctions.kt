package com.example.healthogram.healthpassport

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Healthogram Step 06 - Deterministic Custom Functions for UI formatting and evaluation.
 * Note: These do NOT substitute for server-side security authorization.
 */
object HealthPassportFunctions {

    fun getHealthRecordTypeLabel(type: String): String {
        return when (type.uppercase(Locale.ROOT)) {
            "DOCTOR_VISIT", "VISIT" -> "Doctor Visit & Consultation"
            "DIAGNOSIS" -> "Clinical Diagnosis"
            "DISEASE", "CONDITION" -> "Medical Condition"
            "ALLERGY" -> "Allergy Advisory"
            "MEDICATION" -> "Prescribed Medication"
            "PRECAUTION" -> "Lifestyle & Clinical Precaution"
            "TEST" -> "Diagnostic Test Order"
            "LAB_REPORT" -> "Laboratory Report"
            "PRESCRIPTION" -> "Digital Prescription"
            "PAPER_PRESCRIPTION" -> "Paper Prescription Document"
            "MEDICAL_DOCUMENT", "DOCUMENT" -> "Medical Document"
            "MEDICAL_BILL", "BILL" -> "Medical Expense Bill"
            "NOTE", "HEALTH_NOTE" -> "Health Note"
            else -> type.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    fun getAccessStatusLabel(status: String): String {
        return when (status.lowercase(Locale.ROOT)) {
            "pending" -> "Pending Patient Review"
            "approved", "active" -> "Active Authorization"
            "rejected" -> "Declined by Patient"
            "expired" -> "Expired Authorization"
            "revoked" -> "Revoked by Patient"
            "cancelled" -> "Cancelled by Requester"
            "used" -> "One-Time Session Used"
            else -> status.uppercase(Locale.ROOT)
        }
    }

    fun getPermissionExpiryLabel(expiresAt: Long): String {
        val now = System.currentTimeMillis()
        if (now > expiresAt) {
            return "Expired"
        }
        val diffMs = expiresAt - now
        val hours = diffMs / (1000 * 60 * 60)
        val minutes = (diffMs / (1000 * 60)) % 60
        return if (hours > 24) {
            val days = hours / 24
            "Expires in $days day${if (days > 1) "s" else ""}"
        } else if (hours > 0) {
            "Expires in ${hours}h ${minutes}m"
        } else {
            "Expires in ${minutes}m"
        }
    }

    fun isAccessExpired(expiresAt: Long): Boolean {
        return System.currentTimeMillis() >= expiresAt
    }

    fun formatHealthId(rawId: String): String {
        val clean = rawId.uppercase(Locale.ROOT).trim()
        if (clean.startsWith("HG-")) return clean
        return "HG-$clean"
    }

    fun getHealthTimelineIcon(category: String): ImageVector {
        return when (category.uppercase(Locale.ROOT)) {
            "DOCTOR_VISIT", "VISIT", "CLINIC" -> Icons.Default.MedicalServices
            "PRESCRIPTION", "MEDICATION" -> Icons.Default.Medication
            "LAB_REPORT", "TEST" -> Icons.Default.Biotech
            "ALLERGY" -> Icons.Default.WarningAmber
            "DIAGNOSIS", "CONDITION" -> Icons.Default.FactCheck
            "BILL", "MEDICAL_BILL" -> Icons.Default.ReceiptLong
            "DOCUMENT", "PAPER_PRESCRIPTION" -> Icons.Default.Description
            "NOTE" -> Icons.Default.Notes
            else -> Icons.Default.HealthAndSafety
        }
    }

    fun getPermissionDurationLabel(durationHours: Int): String {
        return when (durationHours) {
            1 -> "1 Hour (Emergency/Single Visit)"
            24 -> "24 Hours (Day Consultation)"
            168 -> "7 Days (Post-Op Followup)"
            720 -> "30 Days (Ongoing Treatment)"
            else -> "$durationHours Hours"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
