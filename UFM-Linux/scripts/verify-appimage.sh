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

# The extraction below cd's into a temp dir, so resolve to an absolute path
# first (the default discovery glob yields a repo-relative path).
APPIMAGE="$(cd "$(dirname "$APPIMAGE")" && pwd)/$(basename "$APPIMAGE")"

WORKDIR=$(mktemp -d)
trap 'rm -rf "$WORKDIR"' EXIT

( cd "$WORKDIR" && "$APPIMAGE" --appimage-extract >/dev/null )

# The payload binary is named after the Cargo package (`ufm-linux`), not the
# product name, so locate it by inspection instead of a hard-coded name.
BIN=$(find "$WORKDIR/squashfs-root/usr/bin" -maxdepth 1 -type f -perm -u+x | head -n1 || true)
if [[ -z "$BIN" ]]; then
  echo "verify-appimage: ERROR: payload binary not found in AppImage (usr/bin)" >&2
  exit 1
fi
echo "verify-appimage: payload binary: $(basename "$BIN")"

# Asset *keys* are stored as plaintext even though asset bodies are brotli-compressed.
# Dump the strings once: piping into `grep -q` closes the pipe early, and under
# `set -o pipefail` the upstream `strings` then dies with SIGPIPE, producing a
# false negative even when the assets are present.
STRINGS="$WORKDIR/strings.txt"
strings -n 4 "$BIN" > "$STRINGS"

if ! grep -q '/index.html' "$STRINGS"; then
  echo "verify-appimage: ERROR: '/index.html' asset key missing — frontend was not embedded" >&2
  exit 1
fi
if ! grep -qE 'assets/index' "$STRINGS"; then
  echo "verify-appimage: ERROR: hashed Vite bundle (assets/index-*.js) missing — dev build?" >&2
  exit 1
fi

echo "verify-appimage: OK — $APPIMAGE embeds the production frontend"
