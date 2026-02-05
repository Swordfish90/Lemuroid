#!/bin/bash
# sync_auto.sh - Automatic sync with translation (for CI/CD)
# Usage: ./sync_auto.sh [--flavor app|shared|app-free|app-play|touchinput]

cd "$(dirname "$0")"

echo "Running automatic sync with translation..."
echo "Make sure OPENAI_API_KEY or ANTHROPIC_API_KEY is set"
echo ""

# --auto-translate: translate new/modified strings
# --translate-missing: also translate strings missing in some locales
poetry run python i18n_sync.py -y sync --auto-translate --translate-missing "$@"
