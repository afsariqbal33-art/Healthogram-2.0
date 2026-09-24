#!/usr/bin/env bash
# ==============================================================================
# HEALTHOGRAM ENVIRONMENT VALIDATION SCRIPT
# Validates developer environment against minimum toolchain specifications
# ==============================================================================
set -euo pipefail

echo "=========================================================="
echo " HEALTHOGRAM TOOLCHAIN VALIDATION CHECKPOINT"
echo "=========================================================="

# 1. Java / JDK Check
if command -v java >/dev/null 2>&1; then
    JAVA_VER=$(java -version 2>&1 | head -n 1)
    echo "✓ Java detected: $JAVA_VER"
else
    echo "✗ Java (JDK 17) not found in PATH."
    exit 1
fi

# 2. Node.js Check
if command -v node >/dev/null 2>&1; then
    NODE_VER=$(node -v)
    echo "✓ Node.js detected: $NODE_VER"
else
    echo "✗ Node.js not found in PATH."
    exit 1
fi

# 3. Git Check
if command -v git >/dev/null 2>&1; then
    GIT_VER=$(git --version)
    echo "✓ Git detected: $GIT_VER"
else
    echo "✗ Git not found in PATH."
    exit 1
fi

# 4. Environment Template Check
if [ -f ".env.example" ]; then
    echo "✓ .env.example exists."
else
    echo "✗ .env.example missing."
    exit 1
fi

echo "=========================================================="
echo " ALL ESSENTIAL TOOLCHAIN PREREQUISITES VERIFIED"
echo "=========================================================="
