# Shipment Tracking & Real-Time Events

## 1. Tracking Event Schema (`shipment_tracking_events/{trackingEventId}`)
Each milestone in a package journey is appended as an immutable event record:
- `shipmentId`: Healthogram internal shipment reference
- `trackingNumber`: Carrier barcode / tracking number
- `provider`: Carrier code (`aramex`, `smsa`, `dhl`, `internal_fleet`)
- `status`: High-level status enum
- `statusCode`: Raw carrier status code (e.g. `PU`, `IT`, `OFD`, `DLV`)
- `locationText`: City, district, or terminal facility
- `eventTime`: Timestamp in milliseconds
- `description`: User-friendly milestone description

## 2. Customer Privacy in Live Tracking
- Approximate Driver Location: Precise GPS coordinates of courier vehicles are never exposed directly to client applications.
- Precision is degraded to 500-meter district-level bounding boxes (`APPROXIMATE_500M`) to preserve courier physical safety.
- Tracking sessions automatically expire within 2 hours of dispatch.
