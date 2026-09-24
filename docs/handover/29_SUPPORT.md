# 29 — CUSTOMER SUPPORT & ESCALATION ARCHITECTURE

## 1. Support Routing Categories
User inquiries are categorized and routed to specialized support teams:
* `ACCOUNT`: Login issues, 2FA recovery, session management.
* `SECURITY`: Suspected unauthorized access, lost device reports.
* `HEALTH_PASSPORT`: Document upload assistance, consent management inquiries.
* `HEALTHCARE`: Doctor appointment cancellations, clinic rescheduling.
* `MARKETPLACE`: Order delivery tracking, missing packages, return requests.
* `PAYMENTS`: Billing disputes, refund status, seller payout questions.
* `PRIVACY`: Data export requests, account deletion verification.

## 2. Tiered Escalation Hierarchy
* **Level 1 (General Support):** Basic troubleshooting, order status checks, FAQ guidance.
* **Level 2 (Technical Specialist):** Device-specific bugs, app re-installation, payment gateway tokenization errors.
* **Level 3 (Engineering Lead):** Cloud Functions errors, Firestore rule rejections, database anomalies.
* **Level 4 (Executive Escalation):** Security incidents, legal inquiries, HIPAA/GDPR data subject requests.
