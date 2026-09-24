# DATABASE MIGRATION 002: MARKETPLACE & LOGISTICS SCHEMA

**Migration ID:** `002_marketplace_schema`  
**Target Environment:** Development, Staging, Production  
**Author:** Healthogram Marketplace Engineer  

---

## 1. Overview
Introduces marketplace collections for Customer and Seller roles, enforcing strict cross-seller isolation, server-side authoritative pricing, and 11-step sequential delivery state tracking.

## 2. Collections Created
* `/products/{productId}`: Public product catalog, verified medical category, stock, seller UID, and price in AED.
* `/orders/{orderId}`: Customer checkout orders, payment intent ID, and shipping address.
* `/delivery_orders/{orderId}`: Delivery tracking state, courier assignment, GPS coords, and proof of delivery.
* `/seller_inventory/{sellerUid_productId}`: Atomic stock tracking with decrement preconditions.

## 3. Backward Compatibility & Rollback Plan
* **Backward Compatibility**: Additive schema; orders collection decoupled from social feeds.
* **Rollback Procedure**: Execute `firebase firestore:delete orders --recursive` on staging if roll-forward fails.
