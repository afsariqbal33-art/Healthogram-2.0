# HEALTHOGRAM — DEPLOYMENT STRATEGY

## 1. Environment Topology
- **Development (`healthogram-dev`)**: Developer testing, mock integration mocks, zero real patient data.
- **Staging (`healthogram-staging`)**: Pre-release regression testing, automated Robolectric & Roborazzi runs, staging Firestore.
- **Production (`healthogram-production`)**: Live environment with Google Play signed AABs, Play Integrity App Check, and Cloud Firestore replication.

## 2. Release Pipeline
1. Compile & Lint Verification.
2. Local JVM Unit Tests & Screenshot regression verification.
3. Gradle AAB Build with upload signing key.
4. Google Play Console internal test track deployment.
