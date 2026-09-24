# ADR-2.2-006: Media Ingestion, Client Compression & Storage Security

**Status:** ACCEPTED  
**Date:** 2026-09-20  
**Context:**  
Media uploads in Healthogram encompass three sensitive categories: medical lab reports/scans, marketplace product imagery, and user profile/social content. High-resolution raw image uploads from modern smartphones (5MB to 15MB per photo) create severe cloud storage and network egress costs. Furthermore, handling clinical attachments requires ironclad privacy.

**Decision:**  
1. **Zero-Permission Media Picker**: Use the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) for all image selections, eliminating legacy storage permission requests.
2. **Client-Side Transcoding**:
   - The Android client transcodes all uploaded images to WebP format (max 1080p resolution, 80% quality) using Kotlin Coroutines before transmission.
   - Reduces payload size by $> 90\%$ (from ~5MB to ~250KB).
3. **Storage Security & Bucket Isolation**:
   - Clinical documents are stored in dedicated private Google Cloud Storage buckets (`/clinical_docs/{patientUid}/`).
   - Access is restricted exclusively via Cloud Storage Signed URLs with an expiration TTL of 15 minutes. Public read permissions are strictly prohibited.

**Consequences:**  
- **Positive**: Slashes cloud egress and storage fees by $> 85\%$; eliminates Android broad storage permission warnings; ensures strict access control for medical files.
- **Negative**: Adds minor client-side CPU processing latency (~200ms) during image compression.
