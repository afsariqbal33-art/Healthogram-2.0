# Root Cause Analysis (RCA) Template

**Document:** `docs/incidents/ROOT_CAUSE_ANALYSIS_TEMPLATE.md`  
**Standard:** Healthogram Clinical SRE Postmortem & Forensic Incident Standard  
**Mandate:** Mandatory for all P0 (Critical Emergency) and P1 (Major Outage) Incidents within 48 Hours  

---

## Incident Header

- **Incident ID:** `INC-YYYYMMDD-XXX`
- **Severity Level:** `[ P0 - Critical Emergency | P1 - Major Outage ]`
- **Incident Commander:** `[ Name & Role ]`
- **Lead Clinical / Security Investigator:** `[ Name & Role ]`
- **Date / Time of Outage Start (UTC):** `YYYY-MM-DD HH:MM:SS`
- **Date / Time of Mitigation (UTC):** `YYYY-MM-DD HH:MM:SS`
- **Total Time to Detect (TTD):** `XX minutes`
- **Total Time to Mitigate (TTM):** `XX minutes`

---

## 1. Executive Incident Summary
*A concise, non-jargon explanation of what occurred, what systems were impacted, and how the incident was contained.*

---

## 2. Detection Mechanism
*Specify how the incident was first identified (e.g., Cloud Monitoring SLI Burn Alert, Crashlytics Velocity Alert, Automated Health Passport Audit Trigger, Customer Support Ticket, Partner Webhook).*

---

## 3. Incident Timeline (UTC Millisecond Accuracy)
| Timestamp (UTC) | Source / Actor | Event / Action Description | System State |
| :--- | :--- | :--- | :--- |
| `HH:MM:SS.mmm` | External / System | Incident trigger occurs | Normal |
| `HH:MM:SS.mmm` | Cloud Monitoring | Alert paged On-Call SRE | Degraded |
| `HH:MM:SS.mmm` | Incident Commander | Incident response initiated; triage started | Degraded |
| `HH:MM:SS.mmm` | SRE Lead | Emergency Kill Switch / Rollback executed | Mitigating |
| `HH:MM:SS.mmm` | QA Lead | Verification probes confirm baseline restored | Stable |

---

## 4. Technical Root Cause
*Detailed architectural explanation of the failure mode. Use the 5-Whys methodology to reach the underlying software, configuration, or operational root cause.*

---

## 5. Contributing Factors
*Environmental, infrastructure, testing gap, or human factors that contributed to the occurrence or delayed detection/mitigation.*

---

## 6. User Impact Assessment
- **Total Users Exposed:**
- **Specific Account Types Affected (Individual, Doctor, Clinic, Hospital, Laboratory):**
- **User-Facing Error Symptoms:**

---

## 7. Clinical Data Impact (Zero Compromise Invariant)
- **Were any medical records modified or corrupted?** `[ YES / NO ]`
- **Were any diagnostic reports dropped or mismatched?** `[ YES / NO ]`
- **Data Reconciliation Findings:**

---

## 8. Security & Privacy Impact
- **Was there unauthorized access or disclosure of Health Passport data?** `[ YES / NO ]`
- **Were any App Check or encryption keystores bypassed?** `[ YES / NO ]`
- **GDPR Art 33 / HIPAA Breach Notification Triggered?** `[ YES / NO / NOT APPLICABLE ]`

---

## 9. Immediate Corrective Action Taken
*Steps executed during the incident to stop the bleeding and restore operational SLOs.*

---

## 10. Long-Term Preventive Actions
| Action Item | Description | Assignee | Target Sprint | JIRA / Issue ID |
| :--- | :--- | :--- | :--- | :--- |
| `ACT-01` | Add schema boundary validation probe | Lead Engineer | Sprint 36 | `ENG-1042` |
| `ACT-02` | Enhance Cloud Monitoring metric threshold | SRE Lead | Sprint 36 | `SRE-409` |

---

## 11. Regression Testing Plan
*Description of automated unit, integration, or Robolectric tests created to guarantee this failure mode can never recur.*

---

## 12. Monitoring & Alerting Improvements
*Specific modifications to Prometheus, Cloud Monitoring, or Crashlytics rules to detect similar precursors earlier.*
