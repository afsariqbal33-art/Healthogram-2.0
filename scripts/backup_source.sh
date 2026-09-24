#!/usr/bin/env bash
# ==============================================================================
# HEALTHOGRAM SOURCE ARCHIVE BACKUP GENERATOR
# Packages source code without secrets, build artifacts, or keystores
# ==============================================================================
set -euo pipefail

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
OUTPUT_ZIP="healthogram_source_backup_${TIMESTAMP}.zip"

echo "Creating clean source code backup archive: $OUTPUT_ZIP..."

if command -v git >/dev/null 2>&1 && git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
    git archive --format=zip --output="$OUTPUT_ZIP" HEAD
    echo "✓ Successfully created Git-tracked source archive: $OUTPUT_ZIP"
else
    zip -r "$OUTPUT_ZIP" . -x "*.git*" "*build/*" "*.gradle/*" "*coverage/*" "*.keystore*" "*.env"
    echo "✓ Successfully created standard archive: $OUTPUT_ZIP"
fi
