# Healthogram Admin Architecture & Operations Center (Step 16)

## 1. Executive Summary

The Healthogram Admin Control Panel serves as the centralized, multi-tenant, sovereign operational management system for Healthogram. It is built strictly on the principle of **least privilege**, ensuring that no single administrator has unrestricted access to the entire platform.

Crucially, **Health Passport clinical information (diagnoses, lab results, medications, imaging) is protected by strict isolation layers and is never displayed on administrative dashboards or exposed to general search.**

---

## 2. End-to-End Administrative Flow

```
Admin Web / Desktop / Mobile UI
               ↓
    Firebase Authentication
               ↓
 Admin Role & Permission Resolver
               ↓
      Firebase App Check
               ↓
   Secure Admin API Engine
               ↓
 Firestore / Storage / RTDB
               ↓
     Append-Only Audit Layer
```

For Sensitive Operations (Suspensions, Refunds, Payout Freezes, Emergency Stops, Commission Changes):
```
Admin UI
   ↓
Re-Authentication / MFA Challenge (≤ 15 min freshness)
   ↓
Server-Authoritative Permission & Country Scope Verification
   ↓
Operation Execution
   ↓
Immutable Audit Record (admin_audit_logs)
```

---

## 3. Account Hierarchy & Sovereign Separation

Healthogram distinguishes strictly between **User Account Categories**, **Marketplace Roles**, and **Platform Management Roles**:

### Healthogram Account Categories (Exclusive)
1. **Individual**
2. **Doctor**
3. **Clinic**
4. **Hospital**
5. **Laboratory**
*(Note: Pharmacy is never added as an account category.)*

### Marketplace Roles
1. **Customer**
2. **Seller**

### Platform Administrative Roles (Independent Management Personas)
* `owner`
* `super_admin`
* `operations_admin`
* `verification_admin`
* `moderation_admin`
* `marketplace_admin`
* `payment_admin`
* `delivery_admin`
* `support_admin`
* `health_security_admin`
* `ai_admin`
* `translation_admin`
* `notification_admin`
* `country_admin`
* `finance_admin`
* `auditor`
* `read_only_admin`

---

## 4. Firebase Custom Claims Guidelines

Firebase limits custom claims to **1,000 bytes**. Therefore, custom claims are reserved exclusively for coarse access gates:
```json
{
  "admin": true,
  "admin_role": "verification_admin"
}
```
All granular permissions, country scopes, department mappings, and audit histories are maintained server-side in Firestore collections (`admin_roles`, `admin_permissions`, `admin_users`), completely immune to client-side tampering.
