# HEALTHOGRAM — VERSION 1.1.0 PRODUCT & ENGINEERING ROADMAP

**Milestone:** Healthogram v1.1.0  
**Target Delivery Window:** Q4 2026  
**Target Android Version:** API 36+ (Android 16)  
**Theme:** Sovereign Identity, Creator & Seller Empowerment, and Performance Tuning  

---

## 1. Feature Roadmap Matrix (Now / Next / Later)

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        VERSION 1.1.0 PRODUCT ROADMAP                   │
├──────────────────────┬──────────────────────┬──────────────────────────┤
│ NOW (Sprint 1-2)     │ NEXT (Sprint 3-4)    │ LATER (Sprint 5-6)       │
├──────────────────────┼──────────────────────┼──────────────────────────┤
│ • Biometric Prompt   │ • Seller Coupons &   │ • Scheduled Post Publish │
│   for Health Passport  Promotional Bundles  │ • Advanced Telemedicine  │
│ • Dark Mode Polish   │ • Creator Analytics    Audio Equalizer          │
│ • Tablet Dual-Pane   │   Dashboard v2       │ • Multi-Currency Ledger  │
│   Adaptive Layouts   │ • Offline Draft Sync │   Pre-Configuration      │
│ • Search Index Tuning│ • Smart Lab Report   │ • Cross-Border Pilot     │
│                      │   Timeline Visualizer│   (Restricted Sandbox)   │
└──────────────────────┴──────────────────────┴──────────────────────────┘
```

---

## 2. Core Functional Improvements Breakdown

### A. Health Passport Enhancements
- **Biometric Authentication Integration:** Local Android BiometricPrompt for instantaneous patient authorization prior to generating ephemeral QR tokens.
- **Smart Timeline Visualizer:** Interactive graphical timeline mapping patient diagnoses, lab results, and prescriptions chronologically with zero server-side PHI exposure.
- **Access Scope Presets:** One-tap presets for patients during QR generation: *"Emergency Overview"* (allergies + blood type only) vs *"Full Clinical History"*.

### B. Marketplace & Seller Ecosystem
- **Seller Promotional Engine:** Configurable percentage/fixed coupons (`coupons/{couponId}`) with strict minimum order requirements and coupon budget caps.
- **Inventory Threshold Alerts:** Automated in-app alerts to sellers when stock drops below minimum safety thresholds.
- **Enhanced Product Discovery:** Multi-attribute filtering (organic, certified allergen-free, clinical grade) and full-text token search.

### C. Creator & Social Platform
- **Creator Studio Analytics v2:** Deep engagement insights (retention curve, audience demographics, top re-shared clips) rendered locally with zero external trackers.
- **Drafts & Offline Composing:** Local Room persistence for offline post, reel, and story drafting with automatic sync upon network reconnection.

### D. Adaptive Screen Experiences
- **Expanded Tablet & Foldable Experience:** Side-by-side List-Detail views for messaging and healthcare records leveraging Jetpack Compose adaptive scaffolds.
