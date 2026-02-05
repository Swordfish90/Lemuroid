# FullRoid Localization System

Unified localization management system for FullRoid Android emulator app.

## Quick Start

### 1. Install dependencies

```bash
cd _scripts/translations
poetry install
```

### 2. Set up API key

```bash
# For OpenAI (recommended)
export OPENAI_API_KEY="your-key-here"

# OR for Anthropic
export ANTHROPIC_API_KEY="your-key-here"
export AI_TRANSLATION_PROVIDER="anthropic"
```

### 3. Initialize the system

```bash
./init.sh
```

### 4. Sync translations

```bash
# Interactive sync
./sync.sh

# Auto-sync with translation
./sync_auto.sh

# Sync specific flavor
./sync.sh --flavor shared
```

## Commands

### init
Initialize or reinitialize the localization system.

```bash
poetry run python i18n_sync.py init
```

### sync
Synchronize changes between source strings and translations.

```bash
# Interactive mode
poetry run python i18n_sync.py sync

# With auto-translation
poetry run python i18n_sync.py sync --auto-translate

# Check and translate missing strings
poetry run python i18n_sync.py sync --translate-missing

# Remove obsolete strings from locales
poetry run python i18n_sync.py sync --cleanup-obsolete

# Non-interactive mode (note: -y goes BEFORE command)
poetry run python i18n_sync.py -y sync --auto-translate
```

### translate
Translate a specific string.

```bash
poetry run python i18n_sync.py translate --flavor shared --key local_storage
```

### stats
Show statistics about tracked strings.

```bash
poetry run python i18n_sync.py stats
```

## Flavors

The system manages translations for multiple modules:

| Flavor | Module | Description |
|--------|--------|-------------|
| shared | retrograde-app-shared | Core emulator settings and options |
| app-free | lemuroid-app-ext-free | Free version app strings |
| app-play | lemuroid-app-ext-play | Play Store version strings |
| touchinput | lemuroid-touchinput | Touch input overlay strings |

## Supported Languages

The system supports 32 languages including:
- Chinese (Simplified & Traditional)
- Russian, Japanese, Korean
- European languages (German, French, Spanish, Italian, etc.)
- And many more

## Directory Structure

```
_scripts/translations/
├── config.py           # Configuration (flavors, languages, API settings)
├── i18n_sync.py        # Main CLI entry point
├── pyproject.toml      # Python dependencies
├── core/               # Core modules
│   ├── xml_parser.py   # Android XML parsing
│   ├── state_manager.py # State tracking
│   ├── change_detector.py # Change detection
│   └── metadata_manager.py # String metadata
├── translation/        # Translation modules
│   ├── ai_translator.py # AI translation
│   ├── prompt_builder.py # Prompt generation
│   └── batch_processor.py # Batch processing
├── utils/              # Utility modules
│   ├── hash_utils.py   # MD5 hashing
│   ├── locale_utils.py # Locale utilities
│   └── interactive.py  # CLI prompts
├── data/               # Data files (auto-generated)
│   ├── state.json      # State tracking
│   └── *.json          # Metadata per flavor
└── *.sh                # Shell scripts
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| OPENAI_API_KEY | OpenAI API key | - |
| ANTHROPIC_API_KEY | Anthropic API key | - |
| AI_TRANSLATION_PROVIDER | AI provider (openai/anthropic) | openai |
