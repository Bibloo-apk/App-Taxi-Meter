#!/usr/bin/env sh
# Gradle wrapper script for Linux/macOS/GitHub Actions

DIRNAME=$(dirname "$0")
if [ -f "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" ]; then
  java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  echo "ERROR: gradle-wrapper.jar not found. Please make sure you uploaded 'gradle/wrapper/gradle-wrapper.jar'"
  exit 1
fi