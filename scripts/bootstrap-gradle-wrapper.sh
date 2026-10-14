#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIR="$ROOT/gradle/wrapper"
JAR="$DIR/gradle-wrapper.jar"
EXPECTED='81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f'
URLS=(
  'https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar'
  'https://github.com/gradle/gradle/raw/refs/tags/v8.13.0/gradle/wrapper/gradle-wrapper.jar'
)
for cmd in curl sha256sum mktemp; do command -v "$cmd" >/dev/null || { echo "Missing $cmd" >&2; exit 1; }; done
mkdir -p "$DIR"
if [[ -f "$JAR" ]] && [[ "$(sha256sum "$JAR" | awk '{print $1}')" == "$EXPECTED" ]]; then
  echo 'SUCCESS: Existing Gradle 8.13 Wrapper JAR already verified.'
  exit 0
fi
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
for url in "${URLS[@]}"; do
  echo "Downloading from $url"
  if curl --fail --location --silent --show-error --retry 2 "$url" --output "$TMP"; then
    ACTUAL="$(sha256sum "$TMP" | awk '{print $1}')"
    if [[ "$ACTUAL" == "$EXPECTED" ]]; then
      cp "$TMP" "$JAR"
      echo "SUCCESS: Verified Gradle 8.13 Wrapper JAR (SHA-256: $ACTUAL)"
      exit 0
    fi
    echo "SHA-256 mismatch ($ACTUAL); refusing file." >&2
  fi
done
echo 'Unable to download a verified Wrapper JAR from the official Gradle GitHub repository.' >&2
exit 1
