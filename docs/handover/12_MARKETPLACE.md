# 12 — HEALTHCARE WELLNESS MARKETPLACE

## 1. Product Scope & Categorization
The Healthogram Marketplace is strictly restricted to health, wellness, and medical support products:
* **Approved Categories:**
  - Mobility Aids & Orthopedic Supports
  - Diagnostic & Monitoring Devices (BP monitors, pulse oximeters, glucometers)
  - Personal Hygiene & Sanitization
  - Fitness & Posture Recovery Equipment
  - Certified Dietary Supplements & Vitamins
* **Strictly Prohibited Items:** Prescription pharmaceuticals, controlled substances, non-medical consumer electronics, unapproved curative concoctions.

## 2. Inventory & Concurrency Management
* **Atomic Stock Reservation:** Items added to checkout are locked in Firestore for 15 minutes via a distributed mutex transaction.
* **Double-Booking / Overselling Prevention:** Stock count is checked and atomically decremented before payment tokenization is issued.

## 3. Domestic vs. International Trade Isolation
* **Domestic Only by Default:** Cross-border trade is disabled by default (`international_marketplace_enabled: false`) to ensure compliance with national pharmaceutical and medical device import regulations.
* **Country Scoping:** Customers only see inventory stocked by sellers legally verified in their country of residence.
