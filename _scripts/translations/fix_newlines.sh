#!/bin/bash
# fix_newlines.sh - Fix newlines in all locale files
# Usage: ./fix_newlines.sh [-n|--dry-run] [-f|--flavor FLAVOR]

cd "$(dirname "$0")"

echo "Fixing newlines in Android strings..."
echo ""

poetry run python fix_newlines.py "$@"
