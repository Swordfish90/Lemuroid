"""
Locale utilities for mapping between API and Android formats.

Reexports locale constants from config and provides utility functions.
"""

from typing import List
from config import SUPPORTED_LANGUAGES, LOCALE_MAPPING, LOCALE_NAMES


# Re-export for convenience
__all__ = ['SUPPORTED_LANGUAGES', 'LOCALE_MAPPING', 'LOCALE_NAMES',
           'api_to_android_locale', 'android_to_api_locale', 'get_locale_name']


def api_to_android_locale(api_locale: str) -> str:
    """
    Convert API locale format to Android resource format.

    Args:
        api_locale: Locale in API format (e.g., 'zh-CN', 'pt-BR', 'en')

    Returns:
        Locale in Android format (e.g., 'zh-rCN', 'pt-rBR', 'en')

    Example:
        >>> api_to_android_locale('zh-CN')
        'zh-rCN'
        >>> api_to_android_locale('en')
        'en'
    """
    return LOCALE_MAPPING.get(api_locale, api_locale)


def android_to_api_locale(android_locale: str) -> str:
    """
    Convert Android resource format to API locale format.

    Args:
        android_locale: Locale in Android format (e.g., 'zh-rCN', 'pt-rBR')

    Returns:
        Locale in API format (e.g., 'zh-CN', 'pt-BR')

    Example:
        >>> android_to_api_locale('zh-rCN')
        'zh-CN'
        >>> android_to_api_locale('en')
        'en'
    """
    # Reverse lookup in LOCALE_MAPPING
    for api_locale, android_fmt in LOCALE_MAPPING.items():
        if android_fmt == android_locale:
            return api_locale
    return android_locale


def get_locale_name(locale: str) -> str:
    """
    Get human-readable name for a locale.

    Args:
        locale: Locale code in API format (e.g., 'zh-CN', 'pt-BR')

    Returns:
        Human-readable locale name (e.g., 'Chinese (Simplified)', 'Portuguese (Brazil)')

    Example:
        >>> get_locale_name('zh-CN')
        'Chinese (Simplified)'
        >>> get_locale_name('en')
        'English'
    """
    return LOCALE_NAMES.get(locale, locale)


def get_all_locales_except_english() -> List[str]:
    """
    Get all supported locales except English (which is the source language).

    Returns:
        List of locale codes excluding 'en'

    Example:
        >>> locales = get_all_locales_except_english()
        >>> 'en' in locales
        False
        >>> len(locales)
        31
    """
    return [loc for loc in SUPPORTED_LANGUAGES if loc != 'en']
