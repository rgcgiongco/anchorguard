#!/usr/bin/env bash
set -euo pipefail

# Verify AnchorGuard sideload APK checksums.
# Usage:
#   scripts/verify-apk-checksum.sh <apk-file> [checksum-file]
#   scripts/verify-apk-checksum.sh --dir <directory>
#
# If checksum-file is omitted, expects <apk-file>.sha256 and/or <apk-file>.sha512.
# Checksum files may contain "<hash>  <filename>" or just "<hash>".

usage() {
  cat <<'EOF'
Usage:
  scripts/verify-apk-checksum.sh <apk-file> [checksum-file]
  scripts/verify-apk-checksum.sh --dir <directory>

Examples:
  scripts/verify-apk-checksum.sh app/release/AnchorGuard-v1-0.1.0-release.apk
  scripts/verify-apk-checksum.sh app/release/AnchorGuard-v1-0.1.0-release.apk app/release/AnchorGuard-v1-0.1.0-release.apk.sha256
  scripts/verify-apk-checksum.sh --dir app/release
EOF
}

fail() { echo "ERROR: $*" >&2; exit 1; }

normalize_hash() {
  tr '[:upper:]' '[:lower:]' | tr -d '[:space:]'
}

extract_expected() {
  local file="$1"
  local hash
  hash="$(awk '{print $1; exit}' "$file")"
  [ -n "$hash" ] || fail "No hash found in $file"
  printf '%s' "$hash" | normalize_hash
}

verify_one() {
  local apk="$1"
  local sum_file="$2"
  [ -f "$apk" ] || fail "APK not found: $apk"
  [ -f "$sum_file" ] || fail "Checksum file not found: $sum_file"

  local algo expected actual
  case "$sum_file" in
    *.sha256) algo="sha256" ;;
    *.sha512) algo="sha512" ;;
    *) fail "Unsupported checksum file extension (expected .sha256 or .sha512): $sum_file" ;;
  esac

  expected="$(extract_expected "$sum_file")"
  if [ "$algo" = "sha256" ]; then
    command -v sha256sum >/dev/null 2>&1 || fail "sha256sum not found"
    actual="$(sha256sum "$apk" | awk '{print $1}' | normalize_hash)"
  else
    command -v sha512sum >/dev/null 2>&1 || fail "sha512sum not found"
    actual="$(sha512sum "$apk" | awk '{print $1}' | normalize_hash)"
  fi

  if [ "$expected" = "$actual" ]; then
    echo "OK  [$algo] $apk"
  else
    echo "FAIL [$algo] $apk" >&2
    echo "  expected: $expected" >&2
    echo "  actual:   $actual" >&2
    return 1
  fi
}

verify_apk_auto() {
  local apk="$1"
  local found=0
  local rc=0
  for ext in sha256 sha512; do
    if [ -f "$apk.$ext" ]; then
      found=1
      verify_one "$apk" "$apk.$ext" || rc=1
    fi
  done
  [ "$found" -eq 1 ] || fail "No checksum files found for $apk (expected $apk.sha256 and/or $apk.sha512)"
  return "$rc"
}

if [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ] || [ "$#" -eq 0 ]; then
  usage
  [ "$#" -eq 0 ] && exit 1 || exit 0
fi

if [ "${1:-}" = "--dir" ]; then
  [ "$#" -eq 2 ] || { usage; exit 1; }
  dir="$2"
  [ -d "$dir" ] || fail "Directory not found: $dir"
  shopt -s nullglob
  apks=("$dir"/*.apk)
  [ "${#apks[@]}" -gt 0 ] || fail "No APK files found in $dir"
  rc=0
  for apk in "${apks[@]}"; do
    verify_apk_auto "$apk" || rc=1
  done
  exit "$rc"
fi

if [ "$#" -eq 1 ]; then
  verify_apk_auto "$1"
  exit $?
fi

if [ "$#" -eq 2 ]; then
  verify_one "$1" "$2"
  exit $?
fi

usage
exit 1
