# 17 — REAL-TIME MESSAGING & ENCRYPTED TELECONSULTATIONS

## 1. Secure Messaging Architecture
* **Real-Time Data Layer:** Cloud Firestore real-time listeners for active conversations.
* **Ephemeral Presence:** Typing indicators and online presence use ephemeral memory caches (throttled at 2000ms) to eliminate wasteful database writes.
* **Attachment Security:** Documents and images sent in medical chats are stored in private Cloud Storage buckets with signed URL access.

## 2. WebRTC Peer-to-Peer Audio & Video Teleconsultations
* **Direct Encrypted Streams:** Video and audio streams are encrypted end-to-end using DTLS/SRTP directly between patient and clinician.
* **Strictly Enforced No-Auto-Record Policy:** In compliance with HIPAA and global patient privacy laws, Healthogram **never** automatically records teleconsultation audio or video streams.
* **Signaling Engine:** Lightweight Firebase signaling for SDP offer/answer and ICE candidate exchange.

## 3. Communication Privacy Toggles
Every user and doctor can independently control their communication availability in `Settings -> Communication Privacy`:
* Allow In-App Messaging: `Everyone` / `Verified Care Providers Only` / `Nobody`
* Allow Audio Calls: `ON` / `OFF`
* Allow Video Calls: `ON` / `OFF`
