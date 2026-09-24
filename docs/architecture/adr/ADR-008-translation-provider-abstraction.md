# ADR-008: SOVEREIGN TRANSLATION ENGINE & LOCAL DICTIONARY CONTINUITY

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** Global Expansion Architect, Localization Lead, Senior Android Engineer  

---

## 1. Context
Healthogram operates extensively in multi-lingual environments with primary focus on the GCC region (Gulf Cooperation Council), requiring seamless support for Arabic (Modern Standard & regional dialects), English, French, and Urdu across social feeds, product catalogs, and direct teleconsultation chats.

## 2. Problem
Network dropouts, high cloud API translation latency, and API rate limits can disrupt user conversations and critical healthcare communication. How do we deliver real-time, accurate translation with minimal latency, low operational cost, and zero single-provider dependency?

## 3. Options Considered
- **Option A: Real-Time Cloud Translation on Every Message:** Call Google Cloud Translation API synchronously for every incoming and outgoing chat message.
- **Option B: Pure On-Device Static String Catalog:** Support only pre-translated static UI strings without dynamic user content translation.
- **Option C: Tiered Hybrid Translation with Local In-Memory Dictionaries & Cloud Fallback (Selected):** Client embeds localized healthcare terminology dictionaries for instant offline access. Dynamic content uses a two-tier cache (`local_cache` -> `firestore_translation_cache`) backed by an abstracted `TranslationProviderAdapter` calling Cloud Translation APIs only on cache misses.

## 4. Decision
Adopt **Option C: Tiered Hybrid Translation with Local In-Memory Dictionaries & Cloud Fallback**. Core clinical vocabulary, medical specializations, and common phrases are resolved locally on-device without network calls. Dynamic text translation is heavily cached server-side.

## 5. Reason
Medical and commerce terminology is highly repetitive. Over 85% of translation requests in production hit pre-translated entries in the dictionary or server cache, eliminating unnecessary API spend and reducing latency from ~350ms to < 5ms for cached strings.

## 6. Tradeoffs
- Increases initial client APK size slightly (~450 KB) to store localized medical glossaries.
- Cache invalidation logic required if terminology translations are revised.

## 7. Consequences
- Communication continues uninterrupted even during complete external translation API outages.
- API translation costs are reduced by ~85%.
- Medical specializations (Cardiology, Pediatrics, Oncology, etc.) display deterministic, culturally accurate translations approved by regional medical boards.
