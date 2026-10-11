#!/usr/bin/env bash
# UFM Linux companion — AppImage sanity check.
#
# A Tauri "dev" build embeds no frontend assets and instead loads the Vite
# dev server (http://localhost:1420). Such an AppImage shows a blank window
# ("Could not connect to localhost: Connection refused") for end users.
# This script fails the build if that happened.
#
# Usage: bash scripts/verify-appimage.sh [path/to/app.AppImage]
set -euo pipefail

APPIMAGE="${1:-}"
if [[ -z "$APPIMAGE" ]]; then
  APPIMAGE=$(ls src-tauri/target/release/bundle/appimage/*.AppImage 2>/dev/null | head -n1 || true)
fi
if [[ -z "$APPIMAGE" || ! -f "$APPIMAGE" ]]; then
  echo "verify-appimage: ERROR: AppImage not found" >&2
  exit 1
fi

WORKDIR=$(mktemp -d)
trap 'rm -rf "$WORKDIR"' EXIT

( cd "$WORKDIR" && "$APPIMAGE" --appimage-extract >/dev/null )

BIN=$(find "$WORKDIR/squashfs-root/usr/bin" -type f -name 'ufm-companion' | head -n1 || true)
if [[ -z "$BIN" ]]; then
  echo "verify-appimage: ERROR: payload binary 'ufm-companion' not found in AppImage" >&2
  exit 1
fi

# Asset *keys* are stored as plaintext even though asset bodies are brotli-compressed.
if ! strings -n 4 "$BIN" | grep -q '/index.html'; then
  echo "verify-appimage: ERROR: '/index.html' asset key missing — frontend was not embedded" >&2
  exit 1
fi
if ! strings -n 4 "$BIN" | grep -qE 'assets/index'; then
  echo "verify-appimage: ERROR: hashed Vite bundle (assets/index-*.js) missing — dev build?" >&2
  exit 1
fi

echo "verify-appimage: OK — $APPIMAGE embeds the production frontend"
