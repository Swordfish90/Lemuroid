"""
Global configuration for i18n_sync system.

This module contains all configuration constants including:
- Flavor definitions (retrograde-app-shared, lemuroid-app-ext-free, etc.)
- Supported languages
- AI provider settings
- Path configurations
"""

import os
from pathlib import Path
from typing import Dict, List

# Project root directory (two levels up from _scripts/translations)
PROJECT_ROOT = Path(__file__).parent.parent.parent

# Translations system directories
TRANSLATIONS_DIR = PROJECT_ROOT / "_scripts" / "translations"
DATA_DIR = TRANSLATIONS_DIR / "data"

# Supported languages (matching existing locales in the project)
# Format: API code (used for translation) -> exists in project
SUPPORTED_LANGUAGES: List[str] = [
    "en", "zh-CN", "zh-TW", "hi", "es", "ar", "pt-BR", "id",
    "ru", "ja", "de", "fr", "ko", "tr", "vi", "it", "pl", "uk",
    "cs", "da", "el", "fi", "hu", "he", "nl", "no", "pt-PT",
    "ro", "sr", "sv", "af", "ca"
]

# Locale mapping (API format -> Android format)
# Note: FullRoid uses full regional codes like values-ru-rRU
LOCALE_MAPPING: Dict[str, str] = {
    "en": "en",
    "zh-CN": "zh-rCN",           # Simplified Chinese
    "zh-TW": "zh-rTW",           # Traditional Chinese
    "pt-BR": "pt-rBR",           # Brazilian Portuguese
    "pt-PT": "pt-rPT",           # Portuguese Portugal
    "hi": "hi-rIN",              # Hindi
    "es": "es-rES",              # Spanish
    "ar": "ar-rSA",              # Arabic
    "id": "id-rID",              # Indonesian
    "ru": "ru-rRU",              # Russian
    "ja": "ja-rJP",              # Japanese
    "de": "de-rDE",              # German
    "fr": "fr-rFR",              # French
    "ko": "ko-rKR",              # Korean
    "tr": "tr-rTR",              # Turkish
    "vi": "vi-rVN",              # Vietnamese
    "it": "it-rIT",              # Italian
    "pl": "pl-rPL",              # Polish
    "uk": "uk-rUA",              # Ukrainian
    "cs": "cs-rCZ",              # Czech
    "da": "da-rDK",              # Danish
    "el": "el-rGR",              # Greek
    "fi": "fi-rFI",              # Finnish
    "hu": "hu-rHU",              # Hungarian
    "he": "iw-rIL",              # Hebrew (Android uses 'iw' instead of 'he')
    "nl": "nl-rNL",              # Dutch
    "no": "no-rNO",              # Norwegian
    "ro": "ro-rRO",              # Romanian
    "sr": "sr-rSP",              # Serbian
    "sv": "sv-rSE",              # Swedish
    "af": "af-rZA",              # Afrikaans
    "ca": "ca-rES",              # Catalan
}

# Human-readable locale names
LOCALE_NAMES: Dict[str, str] = {
    "en": "English",
    "zh-CN": "Chinese (Simplified)",
    "zh-TW": "Chinese (Traditional)",
    "hi": "Hindi",
    "es": "Spanish",
    "ar": "Arabic",
    "pt-BR": "Portuguese (Brazil)",
    "pt-PT": "Portuguese (Portugal)",
    "id": "Indonesian",
    "ru": "Russian",
    "ja": "Japanese",
    "de": "German",
    "fr": "French",
    "ko": "Korean",
    "tr": "Turkish",
    "vi": "Vietnamese",
    "it": "Italian",
    "pl": "Polish",
    "uk": "Ukrainian",
    "cs": "Czech",
    "da": "Danish",
    "el": "Greek",
    "fi": "Finnish",
    "hu": "Hungarian",
    "he": "Hebrew",
    "nl": "Dutch",
    "no": "Norwegian",
    "ro": "Romanian",
    "sr": "Serbian",
    "sv": "Swedish",
    "af": "Afrikaans",
    "ca": "Catalan",
}

# Flavor configurations for FullRoid
FLAVOR_CONFIG: Dict[str, Dict[str, str]] = {
    'app': {
        'name': 'app',
        'display_name': 'Lemuroid App (Main)',
        'source_path': 'lemuroid-app/src/main/res/values/strings.xml',
        'locales_path': 'lemuroid-app/src/main/res',
        'metadata_file': 'data/app.json',
        'description': 'Main app UI strings'
    },
    'shared': {
        'name': 'shared',
        'display_name': 'Retrograde App Shared',
        'source_path': 'retrograde-app-shared/src/main/res/values/strings.xml',
        'locales_path': 'retrograde-app-shared/src/main/res',
        'metadata_file': 'data/shared.json',
        'description': 'Shared strings for emulator settings and core options'
    },
    'app-free': {
        'name': 'app-free',
        'display_name': 'Lemuroid App Free',
        'source_path': 'lemuroid-app-ext-free/src/main/res/values/strings.xml',
        'locales_path': 'lemuroid-app-ext-free/src/main/res',
        'metadata_file': 'data/app-free.json',
        'description': 'Free version app strings'
    },
    'app-play': {
        'name': 'app-play',
        'display_name': 'Lemuroid App Play',
        'source_path': 'lemuroid-app-ext-play/src/main/res/values/strings.xml',
        'locales_path': 'lemuroid-app-ext-play/src/main/res',
        'metadata_file': 'data/app-play.json',
        'description': 'Play Store version app strings'
    },
    'touchinput': {
        'name': 'touchinput',
        'display_name': 'Lemuroid Touch Input',
        'source_path': 'lemuroid-touchinput/src/main/res/values/strings.xml',
        'locales_path': 'lemuroid-touchinput/src/main/res',
        'metadata_file': 'data/touchinput.json',
        'description': 'Touch input overlay strings'
    }
}

# AI Provider Configuration
AI_PROVIDER = os.getenv('AI_TRANSLATION_PROVIDER', 'openai')
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
ANTHROPIC_API_KEY = os.getenv('ANTHROPIC_API_KEY')
OPENAI_MODEL = 'gpt-4o'
ANTHROPIC_MODEL = 'claude-sonnet-4-20250514'

# State file configuration
STATE_FILE = DATA_DIR / "state.json"
STATE_VERSION = "2.0"

# Lock file for concurrent access prevention
LOCK_FILE = TRANSLATIONS_DIR / ".sync.lock"

# Categories for metadata
COMMON_CATEGORIES = [
    'emulator', 'settings', 'controls', 'display', 'audio', 'system',
    'storage', 'ui', 'navigation', 'errors', 'general'
]

# Tone options for translations
TONE_OPTIONS = ['friendly', 'formal', 'casual', 'professional', 'technical']

# Brand names and terms that should NEVER be translated or transliterated
DO_NOT_TRANSLATE = [
    'Lemuroid',
    'FullRoid',
    'Fulldive',
    'RetroArch',
    'Libretro',
    'DualShock',
    'JIT',
    'IR JIT',
    'PPSSPP',
    'mGBA',
    'Mupen64Plus',
    'Gambatte',
    'Stella',
    'melonDS',
    'DeSmuME',
    'Citra',
    'PCSX-ReARMed',
    'FB Neo',
    'Genesis Plus GX',
]


def get_flavor_path(flavor: str, path_type: str) -> Path:
    """
    Get absolute path for a flavor's resource.

    Args:
        flavor: Flavor name
        path_type: Type of path ('source_path', 'locales_path', 'metadata_file')

    Returns:
        Absolute Path object

    Raises:
        ValueError: If flavor or path_type is invalid
    """
    if flavor not in FLAVOR_CONFIG:
        raise ValueError(f"Unknown flavor: {flavor}. Available: {list(FLAVOR_CONFIG.keys())}")

    flavor_cfg = FLAVOR_CONFIG[flavor]

    if path_type not in flavor_cfg:
        raise ValueError(f"Unknown path type: {path_type}")

    rel_path = flavor_cfg[path_type]

    # metadata_file is relative to TRANSLATIONS_DIR, others are relative to PROJECT_ROOT
    if path_type == 'metadata_file':
        return TRANSLATIONS_DIR / rel_path
    else:
        return PROJECT_ROOT / rel_path


def get_locale_file_path(flavor: str, locale: str) -> Path:
    """
    Get path to a locale's strings.xml file.

    Args:
        flavor: Flavor name
        locale: Locale code (e.g., 'ru', 'zh-CN')

    Returns:
        Path to strings.xml for the locale

    Example:
        >>> get_locale_file_path('shared', 'ru')
        Path('.../retrograde-app-shared/src/main/res/values-ru-rRU/strings.xml')
    """
    locales_path = get_flavor_path(flavor, 'locales_path')
    android_locale = LOCALE_MAPPING.get(locale, locale)

    if android_locale == 'en':
        return locales_path / 'values' / 'strings.xml'
    else:
        return locales_path / f'values-{android_locale}' / 'strings.xml'


def ensure_directories():
    """Create necessary directories if they don't exist."""
    DATA_DIR.mkdir(parents=True, exist_ok=True)
