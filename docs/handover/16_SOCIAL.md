# 16 — SOCIAL DISCOVERY & CREATOR HEALTH PLATFORM

## 1. Social Features Overview
* **Media Feeds:** Paginated feed of health articles, infographics, verified practitioner advice, and community stories.
* **Health Reels & Stories:** Short-form educational video clips with hardware-accelerated playback via ExoPlayer.
* **Universal Create Entry (`+`):** Single unified bottom navigation launcher allowing users to create:
  - Text Post
  - Reel Video
  - Standard Video
  - Daily Story
  - Live Stream

## 2. Inviolable Air-Gap: Social vs. Health Passport
* **Strict Domain Isolation:** The social module has **zero architectural access** to the `/health_passports/` collection.
* **No Accidental Leakage:** Users cannot accidentally share Health Passport items to the public social feed without explicit multi-step confirmation and consent stripping.
* **Community Safety:** User blocking, content reporting, spam filters, and administrative moderation queues protect community integrity.
