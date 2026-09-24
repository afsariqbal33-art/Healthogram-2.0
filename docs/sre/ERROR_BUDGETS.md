# HEALTHOGRAM ERROR BUDGET POLICY & DEPLOYMENT GATEKEEPING

**Document Version:** 2.0.0-SRE  
**Policy Owner:** Principal Architect & SRE Lead  
**Classification:** Operational Governance  

---

## 1. Error Budget Philosophy

Error budgets define the acceptable amount of unreliability a service can accumulate over a 30-day rolling window. They provide an objective, mathematical metric that balances feature velocity with platform stability.

---

## 2. Budget Burn Rate & Automated Action Rules

| Error Budget Consumption | Status | Permitted Engineering Activities | Automated System Actions |
|---|---|---|---|
| **0% to 50% Burned** | **HEALTHY** | Normal feature releases, A/B experiments, schema migrations. | Standard CI/CD automated canary rollouts. |
| **51% to 75% Burned** | **ELEVATED RISK** | Normal feature releases permitted; requires Senior SRE signoff for database migrations. | Increased telemetry sampling, daily review. |
| **76% to 99% Burned** | **BUDGET WARNING** | Non-critical releases restricted; changes must focus on reliability and bug fixes. | Staging soak time doubled (24h $\rightarrow$ 48h). |
| **$\ge$ 100% (Exhausted)** | **BUDGET EXHAUSTED** | **FEATURE FREEZE ENFORCED**. Only critical security fixes, SEV-0 hotfixes, or reliability fixes allowed. | CI/CD deployment gatekeeper blocks feature deployments automatically (`HealthogramObservabilityService.canDeployRiskyRelease() == false`). |

---

## 3. Critical Services Protected by Hard Deployment Gatekeeping

1. **Health Passport & Consent Infrastructure**
2. **Payments & Double-Entry Financial Ledger**
3. **Identity, Authentication & App Check Attestation**

If any of these three services exhausts its monthly error budget, all production feature deployments across the application are locked until the rolling availability recovers above target.
