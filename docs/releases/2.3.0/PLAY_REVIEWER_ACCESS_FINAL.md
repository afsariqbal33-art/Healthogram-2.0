# HEALTHOGRAM 2.3.0 GOOGLE PLAY REVIEWER ACCESS CREDENTIALS & DEMO WALKTHROUGH

**Target Release:** 2.3.0 (versionCode 23000)  
**Evaluation Environment:** Closed Internal Testing Track / Reviewer Sandbox  
**Security Notice:** All reviewer accounts are provisioned exclusively with synthetic test data. Zero real patient identifiable information (PII/PHI), live medical records, or live payment gateways are involved.

---

## 1. Reviewer Test Credentials Matrix

| Role | Account Category | Email | Password | OTP / 2FA Code | Test Purpose |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **Individual User** | `INDIVIDUAL` | `reviewer-individual@healthogram-demo.internal` | `HealthPass2026!Demo` | `882194` | Health Passport, QR generation, booking appointments, profile settings |
| **Licensed Doctor** | `DOCTOR` | `reviewer-doctor@healthogram-demo.internal` | `DoctorPlay2026!Rev` | `882194` | Doctor profile, appointment management, consent-gated QR record review |
| **Medical Clinic** | `CLINIC` | `reviewer-clinic@healthogram-demo.internal` | `ClinicPlay2026!Demo` | `882194` | Clinic profile, department management, multi-doctor scheduling |
| **General Hospital** | `HOSPITAL` | `reviewer-hospital@healthogram-demo.internal` | `Hospital2026!Review` | `882194` | Hospital profile, inpatient units, appointment roster |
| **Diagnostics Lab** | `LABORATORY` | `reviewer-lab@healthogram-demo.internal` | `LabPlay2026!Access` | `882194` | Diagnostic tests catalog, synthetic lab result reporting |
| **Marketplace Shopper**| `CUSTOMER` | `reviewer-customer@healthogram-demo.internal` | `Customer2026!Shop` | `882194` | Browsing wellness catalog, sandbox cart checkout (no real money) |
| **Verified Merchant** | `SELLER` | `reviewer-seller@healthogram-demo.internal` | `SellerPlay2026!Shop` | `882194` | Seller dashboard, product inventory, synthetic escrow ledger |

---

## 2. Feature-by-Feature Reviewer Walkthrough Guide

### Journey A: Health Passport & Sovereign QR Consent (Individual)
1. Log in with `reviewer-individual@healthogram-demo.internal`.
2. Tap the **Health Passport** bottom navigation tab or home widget.
3. Observe synthetic conditions (e.g., "Seasonal Allergic Rhinitis"), synthetic vitals (Heart Rate: 72 bpm, BP: 120/80 mmHg), and medications.
4. Tap **"Share via QR"**:
   - A time-limited, cryptographically signed token is generated.
   - Observe countdown timer (60-second TTL).
   - Zero raw medical text is encoded in the QR matrix (only a cryptographic session ticket).
5. Open **Consent Log**: Verify that past provider review sessions are listed with timestamp and revocation status.

### Journey B: Clinical Appointment Booking (Doctor / Clinic)
1. In the search or care finder, search for "Dr. Reviewer" or "Metro Health Clinic".
2. View available synthetic time slots.
3. Book an appointment. Notice instant confirmation and appointment ledger entry.
4. Log in as `reviewer-doctor@healthogram-demo.internal` to view the booked slot in the Doctor's active appointments schedule.

### Journey C: Safe Marketplace Sandbox Demonstration
1. Switch to `reviewer-customer@healthogram-demo.internal`.
2. Tap the **Marketplace** tab.
3. Browse approved wellness and health support items (e.g., Digital Blood Pressure Cuff, Ergonomic Lumbar Support).
4. Add item to cart and proceed to Checkout.
5. In the Payment selector, choose **"Google Play Sandbox Test Mode"** or **"Demo Credit"**.
6. Complete order: Observe instant order confirmation, synthetic tracking ID, and double-entry escrow ledger entry. **No real payment method or money is charged.**

### Journey D: Account Deletion Walkthrough
1. Go to `Settings -> Privacy & Security -> Account Management -> Delete Account`.
2. Review the transparent deletion notice explaining the permanent destruction of personal credentials and encrypted vaults.
3. For review purposes, tapping "Cancel" safely aborts deletion; tapping "Confirm" deletes the test account cleanly.
