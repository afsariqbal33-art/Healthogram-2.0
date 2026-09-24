# ADR-014: ASYNCHRONOUS MEDIA PROCESSING PIPELINE & PRIVATE STORAGE NAMESPACES

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Media Infrastructure Engineer, Security Architect  

---

## 1. Context
User-generated content (images, reels, stories, product photos) requires malware scanning, metadata stripping (EXIF GPS removal for privacy), multi-resolution thumbnail generation, and video transcoding.

## 2. Problem
How do we process high volumes of multimedia efficiently while guaranteeing strict separation between public social assets and private medical documents?

## 3. Options Considered
- **Option A: Synchronous Client-Side Processing Only:** Mobile device handles all compression and transcoding before upload.
- **Option B: Single Shared Storage Bucket:** Store all media in one bucket and distinguish privacy solely via Firestore document flags.
- **Option C: Decoupled Asynchronous Media Pipeline with Strict Storage Namespaces (Selected):** Isolated private storage paths (`health_private/{uid}/`, `verification_private/{uid}/`, `documents_private/{uid}/`) secured by strict Firebase Storage Rules. Public media lands in ingest buckets and triggers asynchronous Cloud Tasks for transcoding, generating WebP and HLS variants.

## 4. Decision
Adopt **Option C: Decoupled Asynchronous Media Pipeline with Strict Storage Namespaces**. Never mix private health storage with public social media.

## 5. Reason
Prevents unauthorized access to clinical data through storage URL leakage while enabling high-throughput adaptive video delivery for social feeds.

## 6. Tradeoffs & Consequences
- Processing delays (5–15 seconds) for high-definition video variants.
- Strict storage security rules reject any unauthorized cross-namespace reads.
