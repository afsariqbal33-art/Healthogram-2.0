# Healthogram Security, Roles & Least-Privilege Model (Step 16)

## 1. Role Matrix & Functional Scoping

| Role ID | Scope | Permissions Granted | Prohibited Surfaces |
|---|---|---|---|
| `owner` | Global | Complete governance, feature flags, emergency kill switches, owner earnings | None |
| `super_admin` | Global | Operations, country configs, flags, emergency controls | Owner financial withdrawals |
| `country_admin` | Country Specific | Users, verifications, moderation, delivery within assigned country (e.g. "SA" or "AE") | Other sovereign countries, global emergency stops |
| `moderation_admin` | Global | Posts, reels, stories, comments moderation, appeals, warnings | Payments, banking, health records |
| `verification_admin` | Global / Regional | Medical licenses, institutional registrations, badges | Financial refunds, social post moderation |
| `finance_admin` | Global | Payment transactions, refunds, chargebacks, payout holds | Health passport records, social messages |
| `delivery_admin` | Global | Shipment fulfillment, rates, zone tables, provider logistics | Clinical records, user suspension |
| `health_security_admin` | Global | Access session logs, suspicious QR scan alerts, consent revocation | Direct medical diagnosis & clinical history |
| `read_only_admin` / `auditor`| Global | Dashboard read, reports read, system health, audit logs | Any mutation or execution |

---

## 2. Country Scoping Enforcement

Country Administrators are strictly confined to their national jurisdiction at the server engine level:
```kotlin
fun checkCountryScope(adminUid: String, countryCode: String?): Boolean {
    if (countryCode.isNullOrBlank()) return true
    val admin = repository.adminUsers.value[adminUid] ?: return false
    if (admin.countryScope.isEmpty()) return true // Global authority
    return admin.countryScope.contains(countryCode.uppercase())
}
```
If an administrator assigned to Saudi Arabia (`countryScope = ["SA"]`) attempts to review or modify a UAE user or shipment (`countryCode = "AE"`), the engine rejects the request with a `SecurityException`.

---

## 3. HIPAA & GDPR Compliance: Medical Data Isolation

In strict compliance with healthcare privacy regulations:
1. **Health Passport Medical Content is NOT Searchable**: Administrative global search only scans public profiles, order IDs, report IDs, and security event references.
2. **Access Log vs. Medical Content**: Administrators in the Health Security Center inspect security metadata (e.g., "Unverified clinic attempted QR scan at 14:02 UTC"), never medical history or diagnostic values.
3. **Restricted Storage Paths**: Sensitive licensing and accreditation documents are stored in isolated paths (`verification_private/{uid}/`) with short-lived presigned access URLs.
