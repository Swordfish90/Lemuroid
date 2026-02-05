#!/bin/bash
# translate.sh - Translate a specific string

cd "$(dirname "$0")"
poetry run python i18n_sync.py translate "$@"
