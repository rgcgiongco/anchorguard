#!/usr/bin/env bash
set -euo pipefail

# Copy Android release APK into app/release with AnchorGuard artifact naming and generate checksums.
# Usage: scripts/package-release-apk.sh [project-root]
# Env: ANDROID_MODULE=app, BUILD_TYPE=release

ROOT="${1:-$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd -P)}"
MODULE="${ANDROID_MODULE:-app}"
BUILD_TYPE="${BUILD_TYPE:-release}"
GRADLE_FILE="$ROOT/$MODULE/build.gradle.kts"
OUT_DIR="$ROOT/$MODULE/release"
APK_DIR="$ROOT/$MODULE/build/outputs/apk/$BUILD_TYPE"

[ -f "$GRADLE_FILE" ] || { echo "ERROR: Gradle file not found: $GRADLE_FILE" >&2; exit 1; }
[ -d "$APK_DIR" ] || { echo "ERROR: APK output directory not found: $APK_DIR (run assemble${BUILD_TYPE^} first)" >&2; exit 1; }

VERSION_CODE="$(sed -n 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$GRADLE_FILE" | head -n1)"
VERSION_NAME="$(sed -n 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$GRADLE_FILE" | head -n1)"
[ -n "$VERSION_CODE" ] && [ -n "$VERSION_NAME" ] || { echo "ERROR: Could not parse versionCode/versionName from $GRADLE_FILE" >&2; exit 1; }

APK_SRC="$(find "$APK_DIR" -maxdepth 1 -type f -name "*.apk" | sort | head -n1)"
[ -n "$APK_SRC" ] || { echo "ERROR: No APK found in $APK_DIR" >&2; exit 1; }

mkdir -p "$OUT_DIR"
DEST="$OUT_DIR/AnchorGuard-v${VERSION_CODE}-${VERSION_NAME}-${BUILD_TYPE}.apk"
cp -f "$APK_SRC" "$DEST"

if command -v sha256sum >/dev/null 2>&1; then
  (cd "$OUT_DIR" && sha256sum "$(basename "$DEST")" > "$(basename "$DEST").sha256")
elif command -v shasum >/dev/null 2>&1; then
  (cd "$OUT_DIR" && shasum -a 256 "$(basename "$DEST")" > "$(basename "$DEST").sha256")
else
  echo "WARN: sha256sum/shasum not found; skipping SHA-256" >&2
fi

if command -v sha512sum >/dev/null 2>&1; then
  (cd "$OUT_DIR" && sha512sum "$(basename "$DEST")" > "$(basename "$DEST").sha512")
elif command -v shasum >/dev/null 2>&1; then
  (cd "$OUT_DIR" && shasum -a 512 "$(basename "$DEST")" > "$(basename "$DEST").sha512")
else
  echo "WARN: sha512sum/shasum not found; skipping SHA-512" >&2
fi

echo "Packaged: $DEST"
ls -1 "$DEST" "$DEST.sha256" "$DEST.sha512" 2>/dev/null || true
