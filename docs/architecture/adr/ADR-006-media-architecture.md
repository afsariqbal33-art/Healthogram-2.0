# ADR-006: SCALABLE MEDIA PROCESSING, HLS TRANSCODING & CDN DELIVERY

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Media Platform Architect, Android Performance Lead, SRE Lead  

---

## 1. Context
Healthogram users upload photos, short-form video reels, multi-image social carousels, and teleconsultation attachments. Serving unprocessed original media directly from Cloud Storage causes excessive mobile bandwidth consumption, slow reel playback buffering, and high storage egress costs.

## 2. Problem
How should media uploads be processed, transcoded, cached, and distributed across global mobile networks while maintaining strict privacy boundaries for medical attachments versus public social reels?

## 3. Options Considered
- **Option A: Direct Storage Serving:** Serve original uploaded files directly via Cloud Storage public download tokens.
- **Option B: Synchronous In-App Compression Only:** Rely entirely on mobile client compression before uploading.
- **Option C: Tiered Pipeline with Asynchronous Transcoding & Cloud CDN (Selected):** Client performs initial lightweight compression. Upload lands in an ingest bucket. An asynchronous worker generates WebP thumbnails and multi-bitrate HLS video streams. Public social media is served via Cloud CDN; clinical attachments are stored in private vaults accessible only via short-lived signed URLs.

## 4. Decision
Adopt **Option C: Tiered Pipeline with Asynchronous Transcoding & Cloud CDN**. Public media assets are served through Google Cloud CDN edge caches with strict separation from private medical attachments.

## 5. Reason
Mobile video feeds require adaptive bitrate streaming (HLS) to provide instant playback on varying cellular networks (3G/4G/5G). Offloading heavy video transcoding to asynchronous Cloud Run containers prevents Cloud Function timeout failures and ensures consistent video playback quality.

## 6. Tradeoffs
- Videos are not immediately viewable in high-definition; an initial preview thumbnail is displayed while background transcoding completes (typically 5–15 seconds).
- Increases storage volume slightly due to multi-bitrate resolutions (360p, 720p, 1080p).

## 7. Consequences
- Network egress costs are reduced by over 60% through Cloud CDN caching and modern WebP/HLS codecs.
- Video playback start times improve from ~1800ms to < 350ms.
- Clinical attachments remain private with zero caching at public CDN edge nodes.
