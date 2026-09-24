# CHANGELOG

All notable changes to the Healthogram platform will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0-rc.1] - 2026-09-16 (Step 22 QA Production Acceptance)

### Added
- Comprehensive 501-checkpoint Production Acceptance Test Matrix.
- Automated `HealthogramProductionAcceptanceSuiteTest.kt` covering session eviction, QR access, financial reconciliation, and delivery state transitions.
- Interactive `QAAcceptanceDashboardPage` integrated into Owner Control Panel.
- Healthogram Bug Register (`HEALTHOGRAM_BUG_REGISTER.md`) with 12 identified, fixed, and verified issues (0 open P0/P1 blockers).
- Security QA and Penetration Test Report (`HEALTHOGRAM_SECURITY_QA_REPORT.md`).
- Multi-tier device compatibility matrix across low-end, mid-range, high-end, and foldables.

### Security
- Verified AES-GCM-256 field-level encryption for all Health Passport clinical summaries.
- Enforced 4-device session ceiling with real-time push eviction of oldest device.
- Hardened dynamic QR code access with 15-minute NTP expiration and instantaneous patient revocation.
- Verified zero PHI transmission in push notification payloads.

---

## [0.1.0] - 2026-09-16 (Step 23 CI/CD & GitHub Architecture)

### Added
- Complete GitHub Actions CI/CD pipeline (`ci.yml`, `android_build.yml`, `firebase_deploy.yml`, `security_scan.yml`, `release.yml`).
- Branching strategy protecting `main` and establishing `develop`, `gemini-3.8-flash`, `release/*`, and `feature/*`.
- Standardized Pull Request template (`.github/pull_request_template.md`) with safety and rollback checklists.
- Issue templates for bug reporting and feature requests with non-negotiable scope reminders.
- Database migration documentation covering initial schema, marketplace, health passport security, owner earnings, and notifications (`docs/database/migrations/`).
- 6-Pillar Backup and Disaster Recovery Policy (`docs/backups/BACKUP_POLICY.md`).
- Subsystem and Module Ownership Map (`docs/architecture/MODULE_OWNERSHIP.md`).
- Environment validation, source backup, and pre-PR CI check helper scripts (`scripts/`).
