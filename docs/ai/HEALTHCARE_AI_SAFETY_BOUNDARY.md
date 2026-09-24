# Healthcare AI Safety Boundary & Airgap Architecture

## 1. Physical & Architectural Airgap

Healthogram enforces a strict boundary between social/creator AI capabilities and clinical medical intelligence:

```
┌────────────────────────────────────────────────────────┐
│                   Generic Social Layer                 │
│         [AIStudioService] ─── Social & Marketplace     │
│   (Captions, Creator Content, Product Descriptions)    │
└──────────────────────────┬─────────────────────────────┘
                           │
             ═════════════════════════════
             PHYSICAL & ARCHITECTURAL AIRGAP
             ═════════════════════════════
                           │
┌──────────────────────────▼─────────────────────────────┐
│                 Clinical Healthcare Layer               │
│      [HealthcareAIService] ─── Private Medical Vault   │
│   (Document Structuring, Terminology, Timeline Prep)   │
└────────────────────────────────────────────────────────┘
```

---

## 2. Strict Healthcare AI Prohibitions

1. **No Autonomous Diagnosis**: The AI is mathematically prohibited from emitting definitive diagnostic conclusions or staging (e.g. *"You have Stage 2 Diabetes"*).
2. **No Prescription or Medication Changes**: The AI cannot recommend altering drug dosages, stopping medications, or initiating pharmaceutical therapy.
3. **No Clinical Overwrites**: The AI cannot directly insert, update, or delete records in `health_passport` without a human review confirmation gate.
4. **No Training on Private Vault Data**: Medical records ingested through Health Passport are excluded from public LLM fine-tuning pools.

---

## 3. Approved Lower-Risk Assistance Capabilities

The `HealthcareAIService` is restricted to patient education and clinical administrative assistance:
- **Document Summarization**: Plain-language summaries of multi-page lab reports.
- **Medical Terminology Translation**: Explaining complex clinical terms (e.g. explaining what "dyslipidemia" means in simple words).
- **Appointment Preparation**: Assisting patients in structuring questions to ask their doctor during consultations.
- **Missing Field Detection**: Alerting the patient when an uploaded lab report is missing reference ranges or units.
- **Duplicate Detection**: Highlighting potential duplicate medication entries in the timeline for doctor review.
