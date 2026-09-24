# INCIDENT POSTMORTEM TEMPLATE

**Incident ID:** INC-YYYYMMDD-XXX  
**Date of Incident:** YYYY-MM-DD  
**Severity:** SEV-0 / SEV-1 / SEV-2  
**Incident Commander:** [Name]  
**Lead SRE / Authors:** [Names]  
**Status:** DRAFT / FINAL  

---

## 1. Executive Summary
- Brief 2-3 sentence overview of what broke, customer impact, duration, and final resolution.

## 2. Impact Analysis
- **User Impact:** Number of affected active users / accounts.
- **Financial Impact:** Any delayed checkouts or refunded transactions.
- **Healthcare Impact:** Any delayed consultations or expired QR tokens (Must explicitly state if zero data leakage occurred).
- **Duration:** Detection time, mitigation time, total outage duration.

## 3. Timeline (UTC)
- **HH:MM:** Anomaly introduced (e.g. faulty deployment, third-party provider failure).
- **HH:MM:** Automated alert fired.
- **HH:MM:** Incident Commander triaged and declared severity level.
- **HH:MM:** Mitigation action applied (e.g. circuit breaker tripped, rollback).
- **HH:MM:** Telemetry confirmed recovery; all invariants verified.

## 4. Root Cause Analysis (5 Whys)
1. *Why?*
2. *Why?*
3. *Why?*
4. *Why?*
5. *Why?*

## 5. What Went Well / What Went Wrong
- **Went Well:** Fast alerting, automatic failover, circuit breaker protected database.
- **Went Poorly:** Monitoring gaps, slow dashboard rendering.

## 6. Corrective Action Items
| Action Item | Type | Owner | Target Date | Tracking Ticket |
|---|---|---|---|---|
| Implement automated circuit trip threshold | Prevention | Lead SRE | YYYY-MM-DD | SRE-104 |
| Add synthetic health QR probe | Detection | QA Lead | YYYY-MM-DD | QA-209 |
