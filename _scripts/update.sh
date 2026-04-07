#!/usr/bin/env bash
#
# update.sh — Download (or use a local) zip of native libraries and install
#             them into a jniLibs directory.
#
# Usage:
#   # From a remote URL:
#   ./update.sh [--rename-from OLD_NAME --rename-to NEW_NAME] [--force] <url> <dest_path>
#
#   # From a local zip file:
#   ./update.sh --local <zip_path> [--rename-from OLD_NAME --rename-to NEW_NAME] [--force] <dest_path>
#
# Examples:
#   ./update.sh \
#     https://github.com/fulldiveVR/azahar/releases/download/1.0.0/libcitra-android-native.zip \
#     /path/to/lemuroid_core_citra/src/main/jniLibs
#
#   ./update.sh --local ./libcitra-android-native.zip \
#     /path/to/lemuroid_core_citra/src/main/jniLibs
#
# The zip is expected to contain .so files organised under ABI subdirectories:
#   arm64-v8a/libcitra_libretro_android.so
#   armeabi-v7a/libcitra_libretro_android.so
#   x86/libcitra_libretro_android.so
#   x86_64/libcitra_libretro_android.so
#
# A single top-level wrapper directory (e.g. "native-libs/") is stripped automatically.
#
# Cache:
#   After a successful install a file "$DEST/.update_cache" is written containing
#   the source URL (or local path), the SHA-256 of the zip, and the install
#   timestamp.  On the next run the cache is checked and the install is skipped
#   when the source and zip hash are unchanged.  Use --force to bypass the cache.

set -euo pipefail

# ── helpers ──────────────────────────────────────────────────────────────────

die() { echo "error: $*" >&2; exit 1; }

usage() {
    echo "Usage: $(basename "$0") [--local ZIP] [--rename-from OLD --rename-to NEW] [--force] <url|dest_path> [dest_path]" >&2
    exit 1
}

sha256_of() {
    local file="$1"
    if command -v sha256sum &>/dev/null; then
        sha256sum "$file" | awk '{print $1}'
    elif command -v shasum &>/dev/null; then
        shasum -a 256 "$file" | awk '{print $1}'
    else
        die "neither sha256sum nor shasum found — cannot compute checksum"
    fi
}

# ── arguments ────────────────────────────────────────────────────────────────

RENAME_FROM=""
RENAME_TO=""
LOCAL_ZIP=""
FORCE=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        --rename-from) RENAME_FROM="$2"; shift 2 ;;
        --rename-to)   RENAME_TO="$2";   shift 2 ;;
        --local)       LOCAL_ZIP="$2";   shift 2 ;;
        --force)       FORCE=1;          shift   ;;
        *) break ;;
    esac
done

if [[ -n "$LOCAL_ZIP" ]]; then
    # Local mode: only dest_path remains
    [[ $# -ge 1 ]] || usage
    URL=""
    DEST="$1"
    [[ -f "$LOCAL_ZIP" ]] || die "local zip not found: $LOCAL_ZIP"
else
    # Remote mode: url + dest_path
    [[ $# -ge 2 ]] || usage
    URL="$1"
    DEST="$2"
    [[ -n "$URL" ]] || die "url must not be empty"
fi

[[ -n "$DEST" ]] || die "dest_path must not be empty"

# ── cache helpers ─────────────────────────────────────────────────────────────

CACHE_FILE="$DEST/.update_cache"

# For remote mode the cache key is the URL; for local mode it is the zip path.
CACHE_KEY="${URL:-$LOCAL_ZIP}"

cache_read() {
    local field="$1"
    [[ -f "$CACHE_FILE" ]] || return 1
    grep "^${field}=" "$CACHE_FILE" | cut -d= -f2- | head -1
}

cache_matches() {
    local cached_key cached_hash current_hash
    cached_key="$(cache_read source 2>/dev/null)" || return 1
    cached_hash="$(cache_read zip_sha256 2>/dev/null)" || return 1

    [[ "$cached_key" == "$CACHE_KEY" ]] || return 1

    if [[ -n "$LOCAL_ZIP" ]]; then
        # For local zips verify that the zip itself has not changed.
        current_hash="$(sha256_of "$LOCAL_ZIP")"
        [[ "$cached_hash" == "$current_hash" ]] || return 1
    fi
    # For remote URLs: matching URL is sufficient (versioned release assets are immutable).
    return 0
}

cache_write() {
    local zip_file="$1"
    local hash
    hash="$(sha256_of "$zip_file")"
    mkdir -p "$DEST"
    cat > "$CACHE_FILE" <<EOF
source=$CACHE_KEY
zip_sha256=$hash
installed_at=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
EOF
}

# ── early-exit if already up to date ─────────────────────────────────────────

if [[ "$FORCE" -eq 0 ]] && cache_matches; then
    echo "Already up to date (cache hit). Use --force to reinstall."
    exit 0
fi

# ── temp workspace ───────────────────────────────────────────────────────────

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

EXTRACT_DIR="$TMP_DIR/extracted"
mkdir -p "$EXTRACT_DIR"

# ── download or use local ────────────────────────────────────────────────────

if [[ -n "$LOCAL_ZIP" ]]; then
    echo "Using local archive: $LOCAL_ZIP ($(du -sh "$LOCAL_ZIP" | cut -f1))"
    ZIP_FILE="$LOCAL_ZIP"
else
    ZIP_FILE="$TMP_DIR/libs.zip"
    echo "Downloading: $URL"
    if command -v curl &>/dev/null; then
        curl -fsSL --retry 3 -o "$ZIP_FILE" "$URL"
    elif command -v wget &>/dev/null; then
        wget -q --tries=3 -O "$ZIP_FILE" "$URL"
    else
        die "neither curl nor wget found — install one and retry"
    fi
    echo "Download complete ($(du -sh "$ZIP_FILE" | cut -f1))."
fi

# ── extract ──────────────────────────────────────────────────────────────────

unzip -q "$ZIP_FILE" -d "$EXTRACT_DIR"

# If the archive has a single top-level wrapper directory that is not an ABI
# directory (e.g. "native-libs/"), descend into it so that ABI subdirectories
# appear directly under EXTRACT_DIR.
TOP_ENTRIES=("$EXTRACT_DIR"/*/)
if [[ ${#TOP_ENTRIES[@]} -eq 1 && -d "${TOP_ENTRIES[0]}" ]]; then
    TOP_NAME="$(basename "${TOP_ENTRIES[0]%/}")"
    case "$TOP_NAME" in
        arm64-v8a|armeabi-v7a|x86|x86_64) ;;
        *) EXTRACT_DIR="${TOP_ENTRIES[0]%/}" ;;
    esac
fi

# ── install ──────────────────────────────────────────────────────────────────

mkdir -p "$DEST"

# Collect all .so files from the extracted tree
SO_FILES=()
while IFS= read -r -d '' f; do
    SO_FILES+=("$f")
done < <(find "$EXTRACT_DIR" -name "*.so" -print0)

[[ ${#SO_FILES[@]} -gt 0 ]] || die "no .so files found in the downloaded archive"

echo "Installing ${#SO_FILES[@]} library file(s) to: $DEST"

INSTALLED=0
for SO in "${SO_FILES[@]}"; do
    REL="${SO#"$EXTRACT_DIR/"}"
    BASENAME="$(basename "$REL")"
    RELDIR="$(dirname "$REL")"

    if [[ -n "$RENAME_FROM" && "$BASENAME" == "$RENAME_FROM" ]]; then
        BASENAME="$RENAME_TO"
    fi

    if [[ "$RELDIR" == "." ]]; then
        TARGET="$DEST/$BASENAME"
    else
        TARGET="$DEST/$RELDIR/$BASENAME"
    fi

    mkdir -p "$(dirname "$TARGET")"
    cp "$SO" "$TARGET"
    echo "  installed: ${RELDIR%/}/$BASENAME"
    (( INSTALLED++ )) || true
done

echo "Done — $INSTALLED file(s) updated."

# ── write cache ───────────────────────────────────────────────────────────────

cache_write "$ZIP_FILE"
echo "Cache saved: $CACHE_FILE"
