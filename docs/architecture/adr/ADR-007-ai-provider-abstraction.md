# ADR-007: AI PROVIDER ABSTRACTION, PROMPT FIREWALL & SAFETY GOVERNANCE

**Status:** ACCEPTED  
**Date:** 2026-09-17  
**Deciders:** AI Platform Architect, Security Lead, Privacy Engineer  

---

## 1. Context
Healthogram integrates generative AI capabilities via AI Studio to assist creators with social captions, hashtags, and visual enhancements, as well as helping sellers generate product marketing descriptions. Over-reliance on a single AI provider or unchecked data flow creates severe vendor lock-in, cost overrun risks, and potential healthcare compliance violations.

## 2. Problem
How do we provide generative AI tooling while strictly preventing vendor lock-in, controlling inference spending, and mathematically prohibiting patient medical records from entering third-party model prompts?

## 3. Options Considered
- **Option A: Direct Client SDK Integration:** Android app imports vendor SDKs directly and calls AI APIs from the device using hardcoded keys.
- **Option B: Single Backend Endpoint Binding:** Cloud Function directly invokes Google Gemini API with fixed prompts and schemas.
- **Option C: Abstracted Provider Router with Prompt Firewall & Spend Guard (Selected):** All AI operations route through a backend `AIProviderAdapter` interface supporting multiple models (Gemini Flash, Gemini Pro, Vertex AI, Anthropic/OpenAI fallbacks). A pre-flight prompt firewall inspects and denies any prompt containing PHI or unauthorized medical claims.

## 4. Decision
Adopt **Option C: Abstracted Provider Router with Prompt Firewall & Spend Guard**. AI capabilities are decoupled behind a unified server-side contract with automated token spend tracking, per-user quotas, and prompt safety validation.

## 5. Reason
Direct client integration exposes API credentials and prevents server-side quota enforcement. The provider adapter ensures Healthogram can switch or load-balance between underlying models without changing mobile application code. The prompt firewall provides enforceable guarantees against PHI leakage.

## 6. Tradeoffs
- Slight latency overhead (~20ms) for server-side prompt validation and token routing.
- Maintaining adapter interfaces across evolving model capabilities requires ongoing schema alignment.

## 7. Consequences
- Patient Health Passport data is programmatically blocked from entering any AI prompt context (`Health Passport -> AI = DENY`).
- Automated spend guards shut off AI generation if daily token burn exceeds defined budget limits.
- Zero client-side API keys; credentials reside securely in Google Secret Manager.
