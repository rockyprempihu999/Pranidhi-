#!/bin/bash
set -e

echo "========================================"
echo "🚀 Starting Android Build (File Buffer Mode)"
echo "========================================"

# 1️⃣ Create virtual build buffer (file-based, not tmpfs)
TEMP_SWAP="/data/data/com.termux/files/usr/tmp/gradle_buffer"
if [ ! -d "$TEMP_SWAP" ]; then
  echo "[+] Creating virtual memory workspace..."
  mkdir -p "$TEMP_SWAP"
fi

# 2️⃣ Apply Termux-safe Gradle settings
echo "[+] Applying Gradle settings..."
cat > gradle.properties <<'GEOF'
org.gradle.daemon=false
org.gradle.parallel=false
org.gradle.configureondemand=false
org.gradle.jvmargs=-Xmx1024m -XX:+UseParallelGC -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
GEOF

# 3️⃣ Clean build
echo "[+] Cleaning project..."
./gradlew clean || true

# 4️⃣ Build APK using temporary buffer
echo "[+] Building debug APK..."
TMPDIR="$TEMP_SWAP" ./gradlew assembleDebug --no-daemon --no-parallel || {
  echo "[❌] Build failed! Check memory usage or dependencies."
  exit 1
}

# 5️⃣ Move built APK to Downloads
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
DEST="/storage/emulated/0/Download/Pranidhi_v1.apk"

if [ -f "$APK_PATH" ]; then
  echo "[+] Moving APK to internal storage..."
  cp "$APK_PATH" "$DEST"
  echo "[✅] APK copied to: $DEST"
  echo "[📦] Install it from your Downloads folder."
else
  echo "[❌] APK not found! Build may have failed."
fi

# 6️⃣ Cleanup
echo "[🧹] Cleaning virtual memory..."
rm -rf "$TEMP_SWAP"

echo "========================================"
echo "🏁 Build finished successfully (if no errors above)."
echo "========================================"
