# 13 — PAYMENT GATEWAYS & TRANSACTION INTEGRITY

## 1. Security Architecture & PCI-DSS Scoping
* **Tokenized Processing:** Healthogram mobile applications and servers **never** handle, transmit, or store Primary Account Numbers (PAN) or CVVs.
* **Payment Modalities Supported:**
  - Credit / Debit Cards (via tokenized gateway elements)
  - Google Pay (Android native intent)
  - Local Payment Networks (Mada, Apple Pay, Fawry, UPI where configured by country)
  - Sandbox Test Mode (Dedicated reviewer mock environment)

## 2. Server-Side Payment Settlement Invariant
* **Never Trust Client Status:** A client-side "payment success" callback never marks an order as paid.
* **Webhook Signature Verification:** Orders transition to `PAID` exclusively upon receipt of a cryptographically signed webhook from the payment provider (e.g. Stripe signature validation).
* **Idempotency:** Webhook processing uses unique transaction IDs to guarantee duplicate webhooks cannot credit orders twice.
