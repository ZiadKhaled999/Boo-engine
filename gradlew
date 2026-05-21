#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
RUNTIME_DIR="$SCRIPT_DIR/runtime-android"

if [ -x "$RUNTIME_DIR/gradlew" ]; then
  exec "$RUNTIME_DIR/gradlew" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Error: Gradle wrapper is not initialized. Add runtime-android/gradlew (and wrapper files) or install 'gradle'." >&2
exit 1
