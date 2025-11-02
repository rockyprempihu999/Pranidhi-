#!/bin/bash
# Lightweight Gradle builder for Termux / low-RAM Ubuntu
# Author: ChatGPT (Optimized for Pranidhi)

echo "[+] Checking environment..."
MEM=$(free -m | awk '/Mem:/ {print $2}')
echo "[+] Detected RAM: ${MEM}MB"

# Dynamic memory allocation
if [ "$MEM" -lt 2000 ]; then
  JVM_MEM=512m
else
  JVM_MEM=1024m
fi

echo "[+] Using JVM memory limit: $JVM_MEM"

# Create or update gradle.properties
echo "[+] Configuring Gradle properties..."
cat > gradle.properties <<EOL
org.gradle.daemon=false
org.gradle.parallel=false
org.gradle.jvmargs=-Xmx$JVM_MEM -XX:+UseParallelGC -Dfile.encoding=UTF-8
org.gradle.configureondemand=true
EOL

# Clean up old builds
echo "[+] Cleaning build cache..."
./gradlew --stop >/dev/null 2>&1
./gradlew clean --no-daemon >/dev/null 2>&1

echo "[+] Starting build..."
GRADLE_OPTS="-Xmx$JVM_MEM -Dorg.gradle.workers.max=1" ./gradlew assembleDebug --no-daemon --info | tee build.log

if [ $? -ne 0 ]; then
  echo "[!] Build failed or Gradle daemon crashed — retrying with safer settings..."
  sleep 5
  GRADLE_OPTS="-Xmx384m -Dorg.gradle.workers.max=1" ./gradlew assembleDebug --no-daemon --offline --stacktrace | tee -a build.log
fi

echo "[✓] Build process complete."
echo "[✓] Check app/build/outputs/apk/debug/ for your APK file."
