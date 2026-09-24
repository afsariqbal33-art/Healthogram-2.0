# ADR-2.2-007: AI Provider Adapter & Non-Diagnostic Healthcare AI

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Healthogram utilizes Generative AI for two distinct workloads: commercial content assistance (e.g. seller product descriptions, caption refinement) and assistive healthcare workflows (e.g. summarizing complex lab terminology for patients, transcribing OCR records). To avoid vendor lock-in and safeguard patient health, AI capabilities must be abstracted and bounded by strict clinical safety rails.

**Decision:**  
1. **`AIProviderAdapter` Abstraction**: All LLM and multimodal calls must route through an abstract `AIProviderAdapter` interface. The primary production implementation binds to Google Cloud Vertex AI (Gemini 3.8 Flash), but the interface allows transparent swapping to alternative enterprise models without modifying UI or business logic.
2. **Strict Domain Segregation**:
   - `Generic AI Studio`: Accessible for social creators and marketplace sellers. No access to health databases.
   - `Healthcare AI`: Strictly assistive. All healthcare prompt templates inject mandatory clinical safety boundaries forbidding prescriptive or diagnostic pronouncements.
3. **Mandatory Non-Diagnostic Disclaimer**: Every screen displaying AI-assisted text must render a prominent, unmissable banner: *"Non-Diagnostic AI Assistance — For Informational Purposes Only. Always consult your doctor."*
4. **Token Quotas**: All invocations pass through server-side token quota checking (`checkAndDeductAiQuota`).

**Consequences:**  
- **Positive**: High architectural flexibility; predictable AI operational expenditure; zero risk of unauthorized autonomous medical decisions.
- **Negative**: Adds server-side token accounting latency (~15ms) prior to upstream AI invocation.
