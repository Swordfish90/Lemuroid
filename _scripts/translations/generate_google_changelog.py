#!/usr/bin/env python3
"""
Generate Google Play Store changelog in all supported languages.

This script translates a changelog text to all supported languages
and outputs in Google Play Store format.
"""

import sys
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from translation.ai_translator import AITranslator
from config import SUPPORTED_LANGUAGES

# Google Play Store locale mapping (API locale → Google Play locale)
GOOGLE_PLAY_LOCALES = {
    "en": "en-US",
    "zh-CN": "zh-CN",
    "zh-TW": "zh-TW",
    "hi": "hi-IN",
    "es": "es-ES",
    "ar": "ar",
    "pt-BR": "pt-BR",
    "pt-PT": "pt-PT",
    "id": "id",
    "ru": "ru-RU",
    "ja": "ja-JP",
    "de": "de-DE",
    "fr": "fr-FR",
    "ko": "ko-KR",
    "tr": "tr-TR",
    "vi": "vi",
    "it": "it-IT",
    "pl": "pl-PL",
    "uk": "uk",
    "cs": "cs-CZ",
    "da": "da-DK",
    "el": "el-GR",
    "fi": "fi-FI",
    "hu": "hu-HU",
    "he": "iw-IL",
    "nl": "nl-NL",
    "no": "no-NO",
    "ro": "ro",
    "sr": "sr",
    "sv": "sv-SE",
    "af": "af",
    "ca": "ca",
}

# Source file path (relative to project root)
SOURCE_FILE = Path(__file__).parent.parent.parent / "google-changelog-src.txt"


def build_translation_prompt(text: str, target_languages: list) -> str:
    """Build prompt for translating changelog to multiple languages."""

    languages_list = ", ".join(target_languages)

    prompt = f"""Translate the following Google Play Store changelog to these languages: {languages_list}

Source text (English):
{text}

Requirements:
- Keep the same structure and line breaks
- Translate naturally and professionally
- The tone should be professional and friendly

Return a JSON object where keys are language codes and values are the translated text.

Example format:
{{
  "ru": "...",
  "de": "..."
}}
"""
    return prompt


def generate_changelog():
    """Generate changelog file for Google Play Store."""

    print("Generating Google Play changelog in all languages...")
    print()

    # Read source changelog text
    if not SOURCE_FILE.exists():
        print(f"Source file not found: {SOURCE_FILE}")
        print(f"Please create the file with your changelog text.")
        sys.exit(1)

    try:
        english_changelog = SOURCE_FILE.read_text(encoding='utf-8').strip()
    except Exception as e:
        print(f"Error reading source file: {e}")
        sys.exit(1)

    if not english_changelog:
        print(f"Source file is empty: {SOURCE_FILE}")
        sys.exit(1)

    print(f"Loaded source changelog from: {SOURCE_FILE}")
    print()

    # Initialize translator
    translator = AITranslator()

    # Get all languages except English
    target_languages = [lang for lang in SUPPORTED_LANGUAGES if lang != "en"]

    print(f"Translating to {len(target_languages)} languages...")

    # Build custom prompt for changelog translation
    prompt = build_translation_prompt(english_changelog, target_languages)

    # Translate
    try:
        if translator.provider == 'openai':
            translations = translator._translate_with_openai(prompt, target_languages)
        elif translator.provider == 'anthropic':
            translations = translator._translate_with_anthropic(prompt, target_languages)
        else:
            raise Exception(f"Unknown provider: {translator.provider}")

        print(f"Translation complete!")
        print()

    except Exception as e:
        print(f"Translation failed: {e}")
        sys.exit(1)

    # Build output file content
    output_lines = []

    # Add English first
    google_locale = GOOGLE_PLAY_LOCALES["en"]
    output_lines.append(f"<{google_locale}>")
    output_lines.append(english_changelog)
    output_lines.append(f"</{google_locale}>")
    output_lines.append("")

    # Add other languages
    for lang in target_languages:
        if lang in translations:
            google_locale = GOOGLE_PLAY_LOCALES.get(lang, lang)
            translation = translations[lang].strip()

            output_lines.append(f"<{google_locale}>")
            output_lines.append(translation)
            output_lines.append(f"</{google_locale}>")
            output_lines.append("")

    # Write to file
    output_path = Path(__file__).parent.parent.parent / "google-changelog.txt"
    output_content = "\n".join(output_lines)

    output_path.write_text(output_content, encoding='utf-8')

    print(f"Changelog saved to: {output_path}")
    print(f"Total languages: {len(SUPPORTED_LANGUAGES)} (including English)")
    print()
    print("Sample output:")
    print("-" * 60)
    print("\n".join(output_lines[:10]))
    print("...")
    print()


if __name__ == '__main__':
    generate_changelog()
