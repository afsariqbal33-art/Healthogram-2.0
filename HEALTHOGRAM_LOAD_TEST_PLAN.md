# HEALTHOGRAM — STEP 21: PRODUCTION LOAD TEST PLAN & CAPACITY TARGETS

## 1. Scope & Objective
This load test plan establishes load generation scenarios, target throughputs, stress limits, and automated pass/fail criteria to validate Healthogram under normal traffic, flash-sale spikes, and healthcare emergency surges.

---

## 2. Load Testing Profiles & Target Concurrency

| Scenario | Target Virtual Users (VU) | Target RPS (Requests/Sec) | Duration | Primary Focus Areas |
| :--- | :--- | :--- | :--- | :--- |
| **Baseline Daily Traffic** | 10,000 Concurrent | 2,500 RPS | 4 Hours | Feed browsing, chat messaging, product searches |
| **Flash Sale / Campaign Surge** | 50,000 Concurrent | 12,000 RPS | 45 Mins | Product detail page, cart locks, checkout API |
| **National Health Campaign** | 100,000 Concurrent | 25,000 RPS | 2 Hours | Health Passport QR scanning, clinic appointments |
| **Stress & Breaking Point Test**| 250,000+ Scaled Ramp | 50,000+ RPS | 30 Mins | Auto-scaling limits, Cloud Functions max instances |
| **Chaos & Network Degradation** | 5,000 Concurrent | 500 RPS (Loss 15%) | 1 Hour | Client offline caching, retry with jitter, error states |

---

## 3. Detailed Scenario Scripting (k6 / Locust / Artillery)

### 3.1 Scenario 1: Social & Content Consumption (60% Weight)
- User opens app -> fetches feature flags (cached) -> loads Home Feed page 1 (20 items).
- User scrolls feed -> triggers page 2 cursor request.
- User likes 2 posts (sharded counter increment).
- User views author profile and 3 reels.

### 3.2 Scenario 2: Secure Health Passport Clinic Visit (15% Weight)
- Doctor scans patient QR token via `/verifyPassportQr`.
- Cryptographic signature verified against public key.
- Doctor reads authorized category (Vaccinations, Diagnoses).
- Patient receives real-time access alert push notification.
- Session expires after 15 minutes.

### 3.3 Scenario 3: Marketplace Purchase & Fulfillment (15% Weight)
- Customer queries products by category: `countryCode=SA&category=supplements`.
- Customer adds item to cart -> initiates checkout.
- Backend locks inventory shard -> creates pending order -> issues payment intent.
- Webhook receives confirmation -> updates double-entry ledger -> schedules delivery job.

### 3.4 Scenario 4: Real-Time Communication & Messaging (10% Weight)
- User connects to Realtime DB presence node.
- User exchanges 10 text messages in direct conversation.
- User sends 1 voice message (metadata in Firestore, audio upload to Storage).
- WebRTC signaling exchanged for 1 audio call setup.

---

## 4. Acceptance Criteria & Pass/Fail Gates

1. **HTTP Error Rate**: Total 5xx server responses < 0.1% across all scenarios.
2. **Firestore Quota & Throttling**: Zero `RESOURCE_EXHAUSTED` errors during flash sale test.
3. **P95 Latency**:
   - Social feed query ≤ 500 ms
   - Health Passport QR verification ≤ 450 ms
   - Checkout execution ≤ 1,800 ms
   - Realtime presence latency ≤ 200 ms
4. **Data Integrity Invariant**: Zero duplicate orders, zero double charges, zero uncommitted ledger transactions.
5. **Cold Start Recovery**: Cloud Run / Cloud Functions warm pool responds to 10x traffic spike within 8 seconds without dropping connections.
