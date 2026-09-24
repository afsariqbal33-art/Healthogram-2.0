# 19 — AI STUDIO CREATIVE MEDIA SUITE

## 1. Scope & Creative Tools
AI Studio provides assistive creative tools for content creators and verified marketplace sellers:
* **Image Enhancement:** Color balance, clarity, and artifact cleanup for social posts and product images.
* **Smart Captions & Hashtags:** Contextual suggestion of health hashtags and educational post captions.
* **Seller Promotional Copy:** Assistive drafting of wellness product descriptions and usage guides.

## 2. Strict Healthcare Data Air-Gap
* **Zero PHI Egress:** Medical records, conditions, medications, and patient identifiers are **strictly prohibited** from ever being sent to external AI provider endpoints (Gemini API).
* **Server-Side Proxy:** All AI API calls route through a secure Cloud Functions proxy that strips metadata, validates input payloads, and enforces rate limits.
* **Graceful Degradation:** If the external AI service is unavailable or rate-limited, the application continues functioning normally with standard manual editing tools.
