# HEALTHOGRAM 2.2 REALTIME DATABASE PERFORMANCE AUDIT

**Engine:** Firebase Realtime Database (`europe-west1`)  
**Instance Tier:** Blaze Plan  
**Role:** Ephemeral Signaling, WebRTC DTLS Handshakes, Realtime Presence & Active Typing Indicators  

---

## 1. Workload Separation Strategy (Firestore vs. Realtime DB)

Healthogram 2.2 enforces strict architectural separation between permanent clinical/social records and ephemeral realtime events:

```text
Permanent Records (Firestore)          Ephemeral Realtime (Realtime Database)
---------------------------------      --------------------------------------
• Patient Health Passport               • Online / Offline Presence Heartbeats
• Financial Ledgers & Double-Entry      • Realtime Typing Indicators (2s throttle)
• Marketplace Products & Orders         • WebRTC SDP Offer / Answer Signaling
• Social Posts, Reels & Comments        • ICE Candidate Exchange
• Verified Healthcare Profiles          • Live Stream Chat Transient Bubbles
```

---

## 2. Bandwidth & Connection Sizing

* **Simultaneous Connections (Tested):** 5,000 concurrent sockets verified with 0 dropped frames.
* **Connection Cap:** 200,000 simultaneous connections per Realtime Database instance on Blaze; partitioned database shards available if regional saturation exceeds 150,000 conns.
* **Stale Connection Pruning:** `onDisconnect()` hooks instantly clean up active typing records and flip presence state to `OFFLINE` within 4,000ms of socket loss.
* **Payload Size Reduction:** Realtime signaling messages are stripped of rich profiles and contain only essential metadata (UUIDs, timestamps, and SDP hashes), maintaining payload sizes under 512 bytes per frame.
