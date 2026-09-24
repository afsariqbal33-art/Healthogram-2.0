# ADR-009: COUNTRY CONFIGURATION, REGIONAL COMPLIANCE & INTERNATIONAL COMMERCE GATING

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Global Expansion Architect, Legal & Compliance Lead, FinTech Architect  

---

## 1. Context
Healthcare regulations, telecommunications licensing, tax structures (VAT/GST), pharmaceutical sales laws, and data residency mandates vary significantly between countries (e.g., Saudi Arabia SFDA, UAE MOHAP, US HIPAA, EU GDPR). Deploying uniform features worldwide without jurisdictional controls risks severe legal sanctions and app store suspensions.

## 2. Problem
How do we enable multi-country platform expansion while enforcing strict sovereign compliance, independent country-level feature gating, and controlled international e-commerce rollouts?

## 3. Options Considered
- **Option A: Hardcoded Client-Side Regional Builds:** Build separate Android APK flavors for each operating country.
- **Option B: Uniform Global Deployment:** Run identical features everywhere and rely on terms of service disclaimers.
- **Option C: Centralized Dynamic Country Configuration Engine with Master Kill Switches (Selected):** Maintain authoritative country configuration documents in Firestore (`country_configs/{countryCode}`) controlling currency, tax rates, mandatory disclaimer strings, courier partners, and feature flags. International cross-border transactions are controlled by an explicit `international_marketplace_enabled` flag.

## 4. Decision
Adopt **Option C: Centralized Dynamic Country Configuration Engine with Master Kill Switches**. The client dynamically fetches its country config based on authenticated phone country code and GPS verification. By default, `international_marketplace_enabled = false` until explicitly authorized by the platform Owner.

## 5. Reason
Dynamic configuration allows instantaneous compliance updates (e.g., updating VAT rate or suspending a newly restricted medication category) without submitting new APK builds to Google Play. Setting international commerce to disabled by default prevents cross-border legal or customs violations during initial national rollouts.

## 6. Tradeoffs
- Client must initialize and cache the country configuration during startup.
- Multi-currency conversions and taxation must be strictly validated server-side during checkout.

## 7. Consequences
- Platform Owner can activate or deactivate specific features per country in real-time.
- Local currency formatting and tax calculations are completely deterministic.
- Cross-border marketplace transactions cannot occur without explicit Owner approval and certified customs clearing integrations.
