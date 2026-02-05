#!/bin/bash
# stats.sh - Show statistics

cd "$(dirname "$0")"
poetry run python i18n_sync.py stats "$@"
