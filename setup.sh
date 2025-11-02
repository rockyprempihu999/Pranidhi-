#!/usr/bin/env bash
set -e
echo "[+] Validating environment..."
command -v python3 >/dev/null || { echo "Python3 missing"; exit 1; }
[ -d "app/src/main/assets/models" ] || mkdir -p app/src/main/assets/models
echo "[+] Checking gradle wrapper..."
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then echo "Missing gradle-wrapper.jar"; fi
echo "[+] Ready. Run ./gradlew assembleDebug to build."
