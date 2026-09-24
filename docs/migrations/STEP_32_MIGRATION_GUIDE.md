# Step 32 Migration Guide: Healthogram 2.0 to 2.1

## 1. Overview
This migration guide outlines the operational steps, schema updates, and validation procedures required to upgrade an existing Healthogram production deployment from version 2.0 to 2.1.

---

## 2. Schema Migrations

### 2.1 Appointments (`appointments/{id}`)
- **Previous Schema**: Contained basic enum states (`SCHEDULED`, `COMPLETED`, `CANCELLED`).
- **New Schema**: Extends state machine to 10 states: `REQUESTED`, `PENDING`, `CONFIRMED`, `RESCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`, `REJECTED`, `EXPIRED`.
- **Migration Action**: Existing records in `SCHEDULED` are mapped to `CONFIRMED` via batch Firestore update script.

### 2.2 Health Records & Provenance (`health_timeline/{id}`)
- **New Field Added**: `provenance: HealthRecordProvenance`
- **Migration Action**: Backfill default `RecordSourceStatus.PROVIDER_ENTERED` or `PATIENT_ENTERED` on historical records based on `createdByAccountType`.

### 2.3 Currencies & Minor Units (`wallets/{uid}`)
- **Invariant**: All floating-point account balances are converted to integer minor units:
  - OMR: `Math.round(balance * 1000)` (Baiza)
  - SAR / AED / USD: `Math.round(balance * 100)` (Halala / Fils / Cents)

---

## 3. Database Indexes

Ensure the following composite indexes are deployed via `firestore.indexes.json`:
1. `appointments`: `providerUid` ASC, `status` ASC, `scheduledStart` ASC
2. `appointments`: `patientUid` ASC, `status` ASC, `scheduledStart` DESC
3. `health_timeline`: `patientUid` ASC, `timestamp` DESC, `category` ASC
4. `health_access_grants`: `patientUid` ASC, `status` ASC, `expiresAt` ASC
5. `paper_prescriptions`: `patientUid` ASC, `userReviewStatus` ASC, `uploadedAt` DESC

---

## 4. Rollout Strategy
Follow the 5-phase release playbook in `docs/rollouts/FEATURE_FLAG_ROLLOUT_PLAN.md`. Keep `killSwitchActive = false` under monitoring.
