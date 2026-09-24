# HEALTHOGRAM — USER FEEDBACK & FEATURE TRIAGE PROCESS

**Classification:** Product Operations & Quality Engineering  
**Channels:** In-App Feedback, Play Store Reviews, Customer Support  

---

## 1. Feedback Categorization Schema

Incoming user and provider feedback must be categorized into one of six distinct channels:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        FEEDBACK CLASSIFICATION                         │
├──────────────────────┬──────────────────────┬──────────────────────────┤
│ 1. Defect / Bug      │ 2. Feature Request   │ 3. User Complaint        │
│ • Routing to QA & SRE│ • Routing to Product │ • Routing to Tier 2      │
│ • P0-P3 SLA assigned │ • Prioritized in     │   Support for resolution │
│                      │   Roadmap backlog    │                          │
├──────────────────────┼──────────────────────┼──────────────────────────┤
│ 4. Security Report   │ 5. Privacy Request   │ 6. Store Review          │
│ • Immediate intake by│ • GDPR / HIPAA Data  │ • Public Play Store      │
│   AppSec Lead (< 1h) │   Export or Deletion │   Review response within │
│                      │   request (< 24h)    │   48 hours               │
└──────────────────────┴──────────────────────┴──────────────────────────┘
```

---

## 2. Play Store Review Monitoring & SLA

- **5-Star & 4-Star Reviews:** Express gratitude, note appreciation for healthcare transparency, encourage continued community engagement.
- **3-Star Reviews:** Identify specific usability pain points; provide friendly guidance or direct to support.
- **1-Star & 2-Star Reviews (Mandatory Response < 24 Hours):**
  - Acknowledge the user's issue empathetically.
  - Never request passwords, payment card numbers, or medical records in public review responses.
  - Provide direct contact: `support@healthogram.app` with an escalation reference code.
  - Tag the issue in `HEALTHOGRAM_BUG_REGISTER.md` for investigation and resolution.
