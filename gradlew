#!/usr/bin/env sh
# Gradle wrapper script
if [ -z "$JAVA_HOME" ] ; then
  echo "JAVA_HOME is not set"
fi
DIRNAME=$(dirname "$0")
"$DIRNAME"/gradlew "$@"
