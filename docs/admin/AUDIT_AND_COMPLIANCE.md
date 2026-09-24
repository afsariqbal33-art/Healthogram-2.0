# Healthogram Audit Logging & Regulatory Compliance (Step 16)

## 1. Append-Only Audit Trail Specification

Every operational mutation executed in the Admin Control Panel generates an immutable record stored in `admin_audit_logs/{logId}`.

### Schema:
* `log_id`: Unique identifier (`aud_XXXXXXXX`)
* `actor_uid`: Authenticated administrator UID
* `actor_role`: Assigned platform role at execution time
* `permission_used`: Specific permission invoked (e.g., `users.suspend`, `payments.refund`)
* `module`: Functional subsystem (`users`, `payments`, `delivery`, `verification`)
* `action`: Specific operation performed
* `target_type`: Entity category (`user`, `shipment`, `payment_transaction`)
* `target_id`: Identifier of modified entity
* `previous_state_reference`: State snapshot before mutation
* `new_state_reference`: State snapshot after mutation
* `reason`: Mandatory justification provided by administrator
* `request_id`: Tracing identifier
* `ip_reference`: Originating IP address
* `device_reference`: Admin terminal metadata
* `timestamp`: Epoch milliseconds
* `result`: Outcome (`SUCCESS` or `FAILURE`)

---

## 2. Retention & Legal Hold Policy

* **Default Retention Period**: 365 days.
* **Legal Hold Mode**: When legal hold is enabled, automatic archiving or deletion is suspended to satisfy regulatory and legal discovery requirements.
* **Tamper Resistance**: No client or standard administrative role possesses write or delete access to existing audit records.
