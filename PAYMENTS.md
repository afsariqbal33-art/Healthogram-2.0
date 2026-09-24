# HEALTHOGRAM — COUNTRY-WISE PAYMENTS ARCHITECTURE

## 1. Provider-Agnostic Abstraction
The client communicates solely with `PaymentAbstractionService`, which routes requests to backend payment microservices configured for each country:
- **North America**: Stripe, Apple Pay, Google Pay (USD, CAD)
- **United Kingdom & EU**: Stripe, Apple Pay, Google Pay, SEPA (GBP, EUR)
- **Gulf Cooperation Council (GCC)**: Mada, Apple Pay, Benefit, KNET (SAR, AED, BHD, KWD)
- **South Asia**: Razorpay, UPI, NetBanking (INR)

## 2. Split Settlement & Escrow
1. Customer initiates order/consultation payment.
2. Server validates funds and holds payment in escrow.
3. Automatically deducts platform commission (e.g. 5%) and payment processing fees.
4. Settles remaining net balance into merchant/doctor pending balance.
5. Payout initiated according to country banking compliance schedules.
