# ADR-2.2-001: Health Data Boundary & Airgap Isolation

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram combines personal health management, teleconsultations, social networking, and a wellness marketplace within a unified Android client application. The platform handles Protected Health Information (PHI) subject to strict global data privacy regulations (GDPR, HIPAA, Omani Personal Data Protection Law). A clear architectural boundary must prevent clinical health data from leaking into social discovery feeds, advertising algorithms, or commercial analytics.

**Decision:**  
We enforce a strict physical and logical airgap between the Health Passport enclave and all other platform domains:
1. **Firestore Security Rules**: Clinical health records (`/users/{uid}/health_records/{recordId}`) are strictly isolated. Security rules forbid any read access from public or social client contexts. No cross-collection joins between health records and social posts or marketplace products are permitted.
2. **Client-Side Storage**: Biometric telemetry and clinical documents are cached in a dedicated Room database encrypted using SQLCipher with 256-bit AES keys derived from the Android Keystore. Social and marketplace caches reside in separate unencrypted databases that can be pruned freely.
3. **No Commercial Usage**: Under no circumstances may Health Connect vitals or clinical diagnoses be analyzed by content recommendation engines or promotional ad targeting services.

**Consequences:**  
- **Positive**: Complete clinical data confidentiality; 100% compliance with international health privacy standards; zero risk of algorithmic profiling based on medical conditions.
- **Negative**: Feature interactions between social posts and health records (e.g. sharing a recovery milestone) must require explicit, deliberate patient export and confirmation rather than automated synchronization.
