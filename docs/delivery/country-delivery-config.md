# Country Delivery Configurations

## 1. Schema: `country_delivery_configs/{countryCode}`
Every supported country maintains a sovereign delivery configuration document:

| Field | Type | Description |
|---|---|---|
| `country_code` | string (ISO-2) | Primary key, e.g. "SA", "AE", "US", "GB", "DE" |
| `country_name` | string | Full name of country |
| `delivery_enabled` | boolean | Global kill-switch for delivery in this country |
| `marketplace_delivery_enabled`| boolean | Toggles marketplace orders delivery |
| `seller_delivery_enabled` | boolean | Allows seller self-delivery / local fleet |
| `platform_delivery_enabled` | boolean | Healthogram Express Fleet enablement |
| `third_party_delivery_enabled`| boolean | Enables 3PL carriers (Aramex, SMSA, DHL) |
| `customer_pickup_enabled` | boolean | Enables clinic / pharmacy store pickup |
| `scheduled_delivery_enabled` | boolean | Enables scheduled delivery time windows |
| `same_day_enabled` | boolean | Enables express same-day courier dispatch |
| `express_delivery_enabled` | boolean | Enables 24-hour express fulfillment |
| `standard_delivery_enabled` | boolean | Enables 2-4 day standard ground shipping |
| `return_delivery_enabled` | boolean | Enables customer return carrier pickups |
| `default_currency` | string | Sovereign currency (SAR, AED, USD, etc.) |
| `default_timezone` | string | Local timezone, e.g. "Asia/Riyadh" |
| `supported_delivery_providers`| array<string> | Provider identifiers configured in country |
| `default_delivery_provider` | string | Primary routing carrier |
| `fallback_delivery_provider` | string | Failover carrier if primary API is down |
| `max_delivery_distance_km` | number | Radius cap for local same-day courier |
| `max_package_weight_kg` | number | Weight threshold (default 30.0 kg) |
| `proof_of_delivery_required` | boolean | Mandatory OTP / signature for handoff |
| `cash_on_delivery_enabled` | boolean | Country-specific COD policy toggle |
| `international_delivery_enabled`| boolean | Locked to `false` initially |

## 2. Seeded Country Matrix
- **Saudi Arabia (`SA`)**: Currency `SAR`, Providers: Aramex, SMSA, Internal Fleet; Same-day and Contactless OTP enabled.
- **United Arab Emirates (`AE`)**: Currency `AED`, Providers: Aramex, DHL, Internal Fleet.
- **United States (`US`)**: Currency `USD`, Providers: FedEx, DHL, Internal Fleet.
- **United Kingdom (`GB`)**: Currency `GBP`, Providers: Royal Mail, DHL.
- **Germany (`DE`)**: Currency `EUR`, Providers: DHL, DPD.
