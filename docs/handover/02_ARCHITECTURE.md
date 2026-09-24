# 02 — SYSTEM ARCHITECTURE

## 1. High-Level Architectural Diagram
```text
+--------------------------------------------------------------------------+
|                       ANDROID 16 CLIENT APPLICATION                      |
|  +--------------------------------------------------------------------+  |
|  |     Jetpack Compose M3 UI (Adaptive Split-Pane, Native Arabic RTL)  |  |
|  +--------------------------------------------------------------------+  |
|  |   ViewModels & StateFlow (MVVM, Clean Architecture, Unidirectional) |  |
|  +--------------------------------------------------------------------+  |
|  | Core Domain Engines:                                               |  |
|  | • HealthPassportVault (AES-GCM-256) • SecurityHardeningEngine     |  |
|  | • FinancialLedgerEngine             • WebRtcConsultationEngine     |  |
|  | • FhirMappingEngine                 • HealthConnectSyncManager     |  |
|  +--------------------------------------------------------------------+  |
+--------------------------------------------------------------------------+
                                    │
                                    │ TLS 1.3 / Certificate Pinning
                                    │ Firebase App Check (Play Integrity)
                                    ▼
+--------------------------------------------------------------------------+
|                         BACKEND CLOUD SERVICES                           |
|  +----------------------+ +---------------------+ +--------------------+  |
|  |  Firebase Auth & MFA | | Cloud Firestore v2.3| | Cloud Storage v2.3 |  |
|  |  (4-Device Ceiling)  | | (Zero-Trust RBAC)   | | (Private Vaults)   |  |
|  +----------------------+ +---------------------+ +--------------------+  |
|  |  Cloud Functions v2  | | Firebase Remote Conf| | Gemini AI Proxy    |  |
|  |  (Node.js 20 micro)  | | (Emergency Switches)| | (Air-gapped)       |  |
|  +----------------------+ +---------------------+ +--------------------+  |
+--------------------------------------------------------------------------+
```

## 2. Layered Architecture Principles
* **Presentation Layer:** Jetpack Compose Material 3 components, strict 48dp touch targets, TalkBack accessibility descriptions, responsive window size classes (`Compact`, `Medium`, `Expanded`).
* **Domain Layer:** Pure Kotlin business models, cryptographic vault primitives, state machines for appointments, escrow, and delivery.
* **Data Layer:** Local encrypted cache via Room/Keystore + remote cloud synchronization via Cloud Firestore.
* **Security Layer:** Envelope encryption for medical records, session tracking via `SecurityHardeningEngine`, hardware-attested App Check tokens.
