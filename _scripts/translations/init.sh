#!/bin/bash
# init.sh - Initialize localization system

cd "$(dirname "$0")"

echo "Initializing localization system..."
echo "This will:"
echo "  1. Create data/ directory and state.json"
echo "  2. Scan all strings.xml files"
echo "  3. Calculate MD5 hashes for all strings"
echo ""

poetry run python i18n_sync.py init "$@"
