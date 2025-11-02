#!/bin/bash
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
DEST="/storage/emulated/0/Download/Pranidhi_v1.apk"

if [ -f "$APK_PATH" ]; then
  echo "[+] Moving APK to internal storage..."
  cp "$APK_PATH" "$DEST"
  echo "[✓] APK copied to: $DEST"
  echo "[📦] Open your file manager → Downloads → Install Pranidhi_v1.apk"
else
  echo "[!] APK not found! Build might have failed or been cleaned."
fi
