# HEALTHOGRAM — VERSION 1.1.0 DATABASE & SCHEMA ARCHITECTURE

**Milestone:** v1.1.0  
**Target:** Cloud Firestore (`nam5`)  
**Backward Compatibility:** Strict N-1 Client Compatibility  

---

## 1. Planned Schema Additions

Version 1.1 introduces two dedicated collections without altering existing v1.0.0 collection contracts:

### A. Collection: `marketplace_coupons/{couponId}`
```json
{
  "coupon_id": "coupon_wellness_20",
  "seller_uid": "seller_abc123",
  "code": "HEALTH20",
  "discount_type": "PERCENTAGE", // "PERCENTAGE" or "FIXED_AMOUNT"
  "discount_value": 20,          // 20% or $20.00 in cents (2000)
  "min_order_cents": 5000,       // Minimum $50.00 cart to qualify
  "max_discount_cents": 2000,    // Cap discount at $20.00
  "usage_limit_total": 1000,
  "usage_count": 42,
  "is_active": true,
  "valid_from": "2026-10-01T00:00:00Z",
  "valid_until": "2026-12-31T23:59:59Z",
  "created_at": "2026-09-20T10:00:00Z"
}
```
*Security Rule:* Sellers can read/write their own coupons. Customers can only read active coupons where `code == request.query.code`. Direct client usage count updates are forbidden (managed by checkout Cloud Function).

### B. Collection: `creator_analytics_daily/{date_creatorUid}`
```json
{
  "creator_uid": "user_xyz789",
  "metric_date": "2026-10-15",
  "total_reach": 15400,
  "reels_watch_time_seconds": 98200,
  "profile_visits": 420,
  "shares_count": 890,
  "top_performing_post_id": "post_778899",
  "computed_at": "2026-10-16T02:00:00Z"
}
```
*Security Rule:* Read-only for authenticated `creator_uid`. Write-restricted strictly to trusted analytics Cloud Functions.

---

## 2. Backward Compatibility & Rollback Protocol

1. **Older Clients (v1.0.0):** v1.0.0 clients ignore coupons and continue checkout with standard subtotal pricing without crashing.
2. **Rollback Strategy:** If coupon processing encounters discrepancies, Remote Config parameter `coupons_feature_enabled = false` immediately disables the coupon UI across all clients.
