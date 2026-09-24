#!/usr/bin/env bash
# ==============================================================================
# HEALTHOGRAM LOCAL PRE-PR CI CHECKS RUNNER
# Runs linting, tests, and secret checks before opening a pull request
# ==============================================================================
set -euo pipefail

echo "=========================================================="
echo " RUNNING PRE-PR LOCAL VALIDATION CHECKS"
echo "=========================================================="

# 1. Secret Scanning
echo "[1/3] Scanning for accidental secrets..."
if grep -rInE --exclude-dir={.git,build,.gradle} "-----BEGIN [A-Z ]*PRIVATE KEY-----" .; then
    echo "CRITICAL: Private key detected in repository!"
    exit 1
fi
echo "✓ Secret scan passed."

# 2. Compile Check
echo "[2/3] Compiling Kotlin Android code..."
gradle :app:compileDebugKotlin

# 3. Test Execution
echo "[3/3] Running automated unit and QA tests..."
gradle :app:testDebugUnitTest

echo "=========================================================="
echo " ALL LOCAL CI CHECKS PASSED SUCCESSFULLY"
echo "=========================================================="
