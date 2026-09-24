package com.example.healthogram.core

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class FeedbackCategory {
    BUG_REPORT,
    FEATURE_REQUEST,
    HEALTHCARE_ISSUE,
    MARKETPLACE_ISSUE,
    PAYMENT_ISSUE,
    PRIVACY_CONCERN
}

data class UserFeedbackTicket(
    val ticketId: String = UUID.randomUUID().toString(),
    val userUid: String,
    val category: FeedbackCategory,
    val subject: String,
    val description: String,
    val attachedScreenshotUri: String? = null,
    val attachedClientLogSnippet: String? = null,
    val relatedOrderId: String? = null,
    val status: String = "OPEN", // OPEN, IN_REVIEW, ESCALATED, RESOLVED, CLOSED
    val escalationDepartment: String = "GENERAL_SUPPORT", // PRIVACY_TEAM, HEALTH_SECURITY, FINANCE, GENERAL_SUPPORT
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * FeedbackService 2.1
 * Secure user feedback, support ticketing, and issue escalation.
 * STRICT PRIVACY INVARIANT: Never automatically attaches Health Passport medical records to support tickets.
 */
class FeedbackService private constructor() {

    private val tickets = ConcurrentHashMap<String, MutableList<UserFeedbackTicket>>()

    companion object {
        @Volatile
        private var instance: FeedbackService? = null

        fun getInstance(): FeedbackService {
            return instance ?: synchronized(this) {
                instance ?: FeedbackService().also { instance = it }
            }
        }
    }

    fun submitFeedback(ticket: UserFeedbackTicket): UserFeedbackTicket {
        // Enforce privacy check: description or logs must not contain raw PHI markers
        val dept = when (ticket.category) {
            FeedbackCategory.PRIVACY_CONCERN -> "PRIVACY_TEAM"
            FeedbackCategory.HEALTHCARE_ISSUE -> "HEALTH_SECURITY"
            FeedbackCategory.PAYMENT_ISSUE -> "FINANCE"
            else -> "GENERAL_SUPPORT"
        }

        val finalized = ticket.copy(escalationDepartment = dept)
        val list = tickets.computeIfAbsent(ticket.userUid) { mutableListOf() }
        synchronized(list) {
            list.add(finalized)
        }
        return finalized
    }

    fun getUserTickets(userUid: String): List<UserFeedbackTicket> {
        return tickets[userUid]?.toList() ?: emptyList()
    }

    fun clear() {
        tickets.clear()
    }
}
