#!/usr/bin/env bash
set -euo pipefail

# Root-level shim for repositories that are not yet fully Gradle-wrapper scaffolded.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

echo "Error: 'gradle' is not installed and no Gradle wrapper distribution is available." >&2
echo "Install Gradle or add a full Gradle wrapper (gradle/wrapper + wrapper jar/properties)." >&2
exit 1
