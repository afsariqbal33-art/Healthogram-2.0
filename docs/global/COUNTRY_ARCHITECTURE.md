# Global Country Architecture Specification

## 1. Multi-Country Strategy & Regional Separation

Healthogram does not hardcode country-specific rules (such as Saudi ZATCA or Oman VAT) into universal platform business logic. Instead, the platform is governed by data-driven country configurations located in Firestore at:
```
country_configs/{countryCode}
```

---

## 2. Country Schema & Field Reference

Every country document implements the following schema:
```json
{
  "country_code": "OM",
  "currency": "OMR",
  "supported_languages": ["ar", "en"],
  "rtl_enabled": true,
  "payment_methods": ["THAWANI", "OMAN_NET", "CARD", "APPLE_PAY"],
  "seller_requirements": ["MOCI_CR", "VAT_TIN"],
  "verification_requirements": ["OMAN_MEDICAL_SPECIALTY_BOARD", "CIVIL_ID"],
  "delivery_methods": ["LOCAL_EXPRESS", "STANDARD_DELIVERY"],
  "tax_configuration_reference": "TAX_OM_VAT_5",
  "marketplace_enabled": true,
  "international_marketplace_enabled": true,
  "health_features": ["HEALTH_PASSPORT", "FHIR_PORTABILITY", "PRESCRIPTION_DIGITIZATION", "EMERGENCY_CARD"],
  "appointment_features": ["IN_PERSON", "TELEHEALTH"],
  "translation_features": ["ARABIC_MEDICAL_GLOSSARY", "MULTILINGUAL_LAB_SUMMARY"],
  "supported_integrations": ["LOCAL_HOSPITAL_ADAPTER", "NATIONAL_HEALTH_EXCHANGE"],
  "legal_configuration_reference": "TERMS_OM_V2",
  "active": true
}
```

---

## 3. Supported Baseline Territories

| Country Code | Primary Currency | Default Tax Model | Payment Rails | Primary Medical Regulator |
| :--- | :--- | :--- | :--- | :--- |
| **OM (Oman)** | OMR (1000 Baiza) | 5% VAT (Health Exempt) | Thawani, OmanNet, Card | Oman Medical Specialty Board (OMSB) |
| **SA (Saudi Arabia)** | SAR (100 Halalas) | 15% VAT (Health Exempt) | Mada, STC Pay, Card | Saudi Commission for Health Specialties (SCFHS) |
| **AE (UAE)** | AED (100 Fils) | 5% VAT (Health Exempt) | Card, Apple Pay, Google Pay | MOHAP, DHA, DoH |
| **US (USA)** | USD (100 Cents) | State Sales Tax (Health Exempt) | Stripe Card, Apple Pay, Google Pay | State Medical Boards / NPI |
