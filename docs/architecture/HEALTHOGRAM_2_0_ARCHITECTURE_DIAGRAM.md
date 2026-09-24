# HEALTHOGRAM 2.0 SYSTEM ARCHITECTURE DIAGRAM & DATA FLOW MAP

**Document Version:** 2.0.0  
**Classification:** Technical Architecture Diagram & Flow Topology  

---

## 1. High-Level Platform Topology

```text
                                  HEALTHOGRAM 2.0
                                         │
        ┌────────────────────────────────┼────────────────────────────────┐
        │                                │                                │
  CLIENT LAYER                     EDGE & GATEWAY                  IDENTITY & ACCESS
  • Android App (API 36+)          • Cloud Armor (WAF/DDoS)        • Firebase Phone Auth (SMS)
  • Tablet Dual-Pane UI            • App Check (Play Integrity)    • Custom Claims Engine
  • Foldable Adaptive Canvas       • Global Load Balancer (TLS 1.3)• 4-Device Session Limit
        │                                │                                │
        └────────────────────────────────┼────────────────────────────────┘
                                         ▼
                            DOMAIN SERVICE BOUNDARIES
   ┌──────────────────────┬──────────────────────┬──────────────────────┐
   │    HEALTH PASSPORT   │   MARKETPLACE CORE   │   FINANCIAL LEDGER   │
   │      (ZERO-TRUST)    │                      │                      │
   │ • Private Vaults     │ • Catalog & Stock    │ • Double-Entry Books │
   │ • Ephemeral QR Tokens│ • Multi-Seller Cart  │ • Escrow Custody     │
   │ • Patient Scoped Auth│ • Checkout Validation│ • Owner Disbursals   │
   │ • Immutable Logs     │ • Seller Isolation   │ • 0-Cent Drift Audit │
   └──────────┬───────────┴──────────┬───────────┴──────────┬───────────┘
              │                      │                      │
   ┌──────────┴───────────┬──────────┴───────────┬──────────┴───────────┐
   │    SOCIAL & REELS    │  REALTIME COMMS & RTC│   AI STUDIO PLATFORM │
   │                      │                      │   (AIR-GAPPED ENGINE)│
   │ • Hybrid Feed Fan-Out│ • E2EE Chat Channels │ • Provider Adapter   │
   │ • Video / Stories    │ • WebRTC Video P2P   │ • Prompt Firewall    │
   │ • Creator Analytics  │ • Ephemeral Presence │ • Safety Moderation  │
   │ • Content Moderation │ • Non-Record Telemed │ • Quotas & Budgets   │
   └──────────┬───────────┴──────────┬───────────┴──────────┬───────────┘
              │                      │                      │
   ┌──────────┴───────────┬──────────┴───────────┬──────────┴───────────┐
   │  DELIVERY LOGISTICS  │  SOVEREIGN TRANSLATE │  OWNER & GOVERNANCE  │
   │                      │                      │                      │
   │ • 11-State Fulfillment│ • Arabized GCC Focus │ • Emergency Switches │
   │ • Courier Dispatch   │ • Fallback Continuity│ • Multi-Country Rule │
   │ • GPS Real-time Track│ • Voice / Text / TTS │ • Tax & Currency     │
   │ • 6-Digit OTP Proof  │ • No Medical Claims  │ • Audit & Recertify  │
   └──────────────────────┴──────────┬───────────┴──────────────────────┘
                                     ▼
                        DATA, EVENT & PERSISTENCE LAYER
   ┌────────────────────────────────────────────────────────────────────────┐
   │ • Cloud Firestore (Multi-Region nam5) - Transactional Document Store   │
   │ • Cloud Storage (Private Encrypted Vaults + CDN Public Media)          │
   │ • Cloud Tasks - Asynchronous Queues, Exponential Backoff, Dead Letters │
   │ • Google Cloud Pub/Sub & Eventarc - Domain Event Streaming             │
   │ • Dedicated Search Index (Eventual Consistency Catalog Discovery)      │
   │ • Cloud KMS - Hardware-backed field-level key envelope encryption      │
   └─────────────────────────────────┬──────────────────────────────────────┘
                                     ▼
                        EXTERNAL INTEGRATION PROVIDERS
   ┌────────────────────────────────────────────────────────────────────────┐
   │ • Payments: Stripe API & Local Payment Rails (Apple Pay, Mada)         │
   │ • Telecom: Twilio SMS Verify, WebRTC STUN/TURN Signaling Nodes         │
   │ • Intelligence: Google Gemini Flash / Pro Multimodal Endpoints         │
   │ • Notifications: Firebase Cloud Messaging (FCM High-Priority Transport)│
   └────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Health Passport Zero-Trust Access Flow

```text
 PATIENT DEVICE                      HEALTHOGRAM BACKEND                   DOCTOR DEVICE
       │                                      │                                  │
       │─── 1. Patient taps "Share QR" ──────>│                                  │
       │    (Authenticates via Biometric)     │                                  │
       │                                      │── 2. Generates Ephemeral Token ──│
       │                                      │   (15-min TTL, Nonce, Scopes)    │
       │<── 3. Receives Ephemeral Token QR ───│                                  │
       │                                                                         │
       │ [Displays Single-Use QR on Screen]                                      │
       │                                      ┌──────────────────────────────────│
       │                                      │ 4. Doctor scans QR via Camera    │
       │                                      │    (Doctor must be Verified)     │
       │                                      ▼                                  │
       │                                      │── 5. Sends Token + Doctor UID ──>│
       │                                      │                                  │
       │                                      │── 6. Validates Token, Expiry, ───│
       │                                      │      Doctor License & App Check  │
       │                                      │                                  │
       │                                      │── 7. Records Access Grant in ────│
       │                                      │      `health_access_logs`        │
       │                                      │                                  │
       │                                      │── 8. Returns Scoped Records ────>│
       │                                      │      (Field-Level Decrypted)     │
       │                                      │                                  │
       │                                      │                                  │
       │─── 9. Patient revokes grant ────────>│                                  │
       │    (Or 15-min TTL naturally expires) │                                  │
       │                                      │── 10. Immediate Access Cutoff ───│
```

---

## 3. Marketplace Double-Entry Escrow & Order Flow

```text
 CUSTOMER                         SERVER GATEWAY                      SELLER & OWNER
    │                                   │                                    │
    │── 1. Submits Checkout Intent ────>│                                    │
    │   (Items, Quantities, Address)    │                                    │
    │                                   │── 2. Authoritative Price Calc ─────│
    │                                   │   (Stock Locked, Taxes Added)      │
    │                                   │                                    │
    │<── 3. Returns Client Secret ──────│                                    │
    │                                   │                                    │
    │── 4. Completes Payment at PSP ───>│                                    │
    │                                   │                                    │
    │                                   │── 5. Stripe Webhook (Idempotent) ──│
    │                                   │   (Verifies Signature & Amount)    │
    │                                   │                                    │
    │                                   │── 6. Writes Double-Entry Ledger: ──│
    │                                   │   • DEBIT: Stripe Clearing House   │
    │                                   │   • CREDIT: Seller Escrow Custody  │
    │                                   │   • CREDIT: Owner Platform Fee     │
    │                                   │   (Net Balance Drift = $0.00)      │
    │                                   │                                    │
    │                                   │── 7. Emits `OrderCreated` Event ──>│
    │                                   │                                    │
    │<── 8. Order Status: ESCROW_HELD ──│                                    │
    │                                                                        │
    │ [Fulfillment & Delivery: Courier verifies 6-Digit Delivery OTP]        │
    │                                                                        │
    │                                   │── 9. Delivery OTP Confirmed: ──────│
    │                                   │   • DEBIT: Seller Escrow Custody   │
    │                                   │   • CREDIT: Seller Available Payout│
    │                                   │   • Settlement Released to Seller ─│
```
