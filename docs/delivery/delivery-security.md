# Delivery Security & Medical Privacy Standards

## 1. Zero Exposure of Sensitive Health Data (HIPAA / GDPR)
Healthogram strictly enforces medical data isolation between patient health records and third-party logistics carriers:
- **No Diagnosis or Prescription Details**: Carrier labels, manifests, and electronic tracking webhooks are sanitized via `DeliveryCustomActions.sanitizePackageLabel()`.
- **Generic Categorization**: Packages containing medical items are labeled with generic terms:
  - `"Cold-Chain Healthcare Package"`
  - `"Nutritional & Dietary Wellness Supplies"`
  - `"Personal Care & Clinical Supplies"`
- **Health Passport Isolation**: The user's Health Passport vault remains completely detached from marketplace order fulfillment records.

## 2. Address Encryption & Telemetry Minimization
- Customer delivery addresses use randomized internal references (`phoneReference`) rather than transmitting raw mobile numbers in unencrypted carrier query parameters.
- Driver GPS telemetry is blurred to 500-meter district radius coordinates to prevent exact residential stalking.

## 3. Role-Based Access Controls (RBAC)
- **Customer**: Access strictly limited to own orders, active OTP codes, and timeline milestones.
- **Seller**: Access limited to assigned suborder items, shipping labels, and pickup schedules. Cannot view customer payment cards or unrelated suborders.
- **Carrier Driver**: Sees destination address and phone mask; cannot view customer medical history.
- **Compliance Admin**: Full audit log visibility, dispute resolution authority, and emergency kill switches.
