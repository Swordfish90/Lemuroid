#!/bin/bash
# sync.sh - Interactive synchronization

cd "$(dirname "$0")"
poetry run python i18n_sync.py sync "$@"
