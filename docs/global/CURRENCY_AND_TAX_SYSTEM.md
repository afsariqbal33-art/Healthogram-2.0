# Currency & Integer Minor Unit Tax System

## 1. Financial Arithmetic Invariants

To eliminate floating-point rounding errors across international currencies, all monetary transactions in Healthogram are strictly executed in **Integer Minor Units**:

| Currency Code | Major Unit | Minor Unit | Multiplier | Formatting (English) | Formatting (Arabic) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **OMR** | Omani Rial | Baiza | `1000` | `OMR 15.500` | `15.500 ر.ع.` |
| **SAR** | Saudi Riyal | Halala | `100` | `SAR 150.00` | `150.00 ر.س.` |
| **AED** | UAE Dirham | Fils | `100` | `AED 150.00` | `150.00 د.إ.` |
| **USD** | US Dollar | Cent | `100` | `$ 45.00` | `$ 45.00` |
| **EUR** | Euro | Cent | `100` | `€ 40.00` | `€ 40.00` |

### Critical Invariant:
**Financial ledger entries retain the original transaction currency.** Converting amounts for reporting displays uses timestamped exchange quotes, but balance mutations never overwrite historical transaction currency records.

---

## 2. Server-Side Reproducible Tax Engine (`TaxService`)

Taxes are calculated deterministically via basis points (`1 basis point = 0.01%`):
```kotlin
val taxAmountMinorUnits = (taxableAmountMinorUnits * taxRateBasisPoints) / 10000L
```

### Healthcare Exemption Rules:
Under regional regulations (e.g. Oman VAT Executive Regulations, Saudi ZATCA Healthcare Exemptions), qualified clinical consultations, laboratory diagnostic services, and approved prescription medications are **zero-rated (0% VAT)**.
- Digital marketing promotions and non-medical marketplace merchandise are subject to standard rates (5% OM/AE, 15% SA).
