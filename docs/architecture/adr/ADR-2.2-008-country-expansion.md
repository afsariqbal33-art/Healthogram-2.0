# ADR-2.2-008: Country Expansion Governance & International Marketplace Status

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram operates successfully in Oman (`OM`), Saudi Arabia (`SA`), the UAE (`AE`), and the United States (`US`). Expanding into additional territories introduces complex legal, healthcare regulatory, taxation (VAT), and customs hurdles. Premature international marketplace enablement risks severe regulatory penalties and cross-border delivery failures.

**Decision:**  
1. **17-Step Country Expansion Framework**: No country may be activated in production without completing the mandatory 17-step qualification checklist (legal entity registration, health ministry licensing, local payment gateway integration, currency minor-unit definition, Arabic/local localization, and local courier partnerships).
2. **International Marketplace Decision in 2.2**:
   - The international marketplace feature flag (`FLAG_INTERNATIONAL_MARKETPLACE`) **remains permanently disabled (`false`)** throughout the entire Healthogram 2.2 release cycle.
   - Marketplace commerce is restricted strictly to domestic fulfillment within the user's registered home country.
3. **Cross-Border Telehealth**: Permitted only between verified international specialists and patients where local teleconsultation regulations explicitly allow cross-border second opinions.

**Consequences:**  
- **Positive**: 100% regulatory and tax compliance; prevents customs seizure of cross-border wellness goods; protects platform financial integrity.
- **Negative**: Users cannot purchase items from sellers located in other countries during the 2.2 cycle.
