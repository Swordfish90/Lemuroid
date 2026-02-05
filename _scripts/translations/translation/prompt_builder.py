"""
Prompt builder for context-aware AI translation.

Generates prompts with metadata context for high-quality translations.
"""

import sys
from pathlib import Path
from typing import Dict, List, Optional

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from config import DO_NOT_TRANSLATE
from utils.locale_utils import LOCALE_NAMES


def build_multilang_prompt(
    key: str,
    text: str,
    metadata: Optional[Dict] = None,
    target_languages: Optional[List[str]] = None
) -> str:
    """
    Build prompt for translating a string to multiple languages.

    Uses simplified metadata schema to provide context to AI.

    Args:
        key: String resource key
        text: English text to translate
        metadata: Simplified metadata dictionary
        target_languages: List of target language codes (defaults to all)

    Returns:
        Formatted prompt string for AI

    Example:
        >>> prompt = build_multilang_prompt(
        ...     'local_storage',
        ...     'Local Storage',
        ...     {'category': 'storage', 'purpose': 'Storage type label'}
        ... )
    """
    if metadata is None:
        metadata = {}

    if target_languages is None:
        target_languages = list(LOCALE_NAMES.keys())

    # Remove 'en' since it's the source language
    target_languages = [lang for lang in target_languages if lang != 'en']

    # Build language list for prompt
    lang_names = [LOCALE_NAMES.get(lang, lang) for lang in target_languages]

    # Extract metadata fields
    category = metadata.get('category', 'general')
    ui_context = metadata.get('ui_context', 'N/A')
    purpose = metadata.get('purpose', 'N/A')
    max_length = metadata.get('max_length')
    tone = metadata.get('tone', 'neutral')

    technical = metadata.get('technical', {})
    has_format_specifiers = technical.get('format_specifiers', False)
    has_html = technical.get('html', False)
    has_emoji = technical.get('emoji', False)

    # Build constraints section
    constraints = []
    if max_length:
        constraints.append(f"- Maximum length: {max_length} characters")
    if has_format_specifiers:
        specifiers = technical.get('specifiers', [])
        if specifiers:
            constraints.append(f"- Must preserve format specifiers: {', '.join(specifiers)}")
        else:
            constraints.append("- Must preserve format specifiers (%s, %d, etc.) in exact positions")
    if has_html:
        constraints.append("- Contains HTML formatting - preserve all HTML tags")
    if has_emoji:
        constraints.append("- Contains emoji - preserve them appropriately for each language")

    constraints_text = "\n".join(constraints) if constraints else "- No special constraints"

    # Build prompt
    prompt = f"""Translate the following Android app string to multiple languages with high quality.

**String Information:**
- Key: {key}
- English Text: "{text}"

**Context:**
- Category: {category}
- UI Location: {ui_context}
- Purpose: {purpose}
- Tone: {tone}

**Constraints:**
{constraints_text}

**Target Languages:**
{', '.join(lang_names)}

**Requirements:**
1. Maintain the same tone and style as the English text
2. Keep translations natural and idiomatic for each language
3. Preserve any format specifiers (%s, %d, etc.) in the exact same positions
4. Ensure translations fit within character limits if specified
5. Adapt UI terminology appropriately for each language/culture
6. This is a retro gaming emulator app - use appropriate gaming/emulation terminology
7. NEVER translate or transliterate these brand names - keep them exactly as written: {', '.join(DO_NOT_TRANSLATE)}

**Output Format:**
Return a JSON object with language codes as keys and translations as values.

Example format:
{{
  "ru": "Translation in Russian",
  "fr": "Traduction en francais",
  "es": "Traduccion al espanol",
  ...
}}

IMPORTANT: Do not include "en" in the output since it's the source language."""

    return prompt


def build_simple_prompt(text: str, target_languages: List[str]) -> str:
    """
    Build simple prompt without metadata context.

    Useful for strings without metadata or quick translations.

    Args:
        text: English text to translate
        target_languages: List of target language codes

    Returns:
        Simple prompt string

    Example:
        >>> prompt = build_simple_prompt('Hello', ['ru', 'fr'])
    """
    # Remove 'en' from targets
    target_languages = [lang for lang in target_languages if lang != 'en']

    lang_names = [LOCALE_NAMES.get(lang, lang) for lang in target_languages]

    prompt = f"""Translate the following text to multiple languages:

English Text: "{text}"

Target Languages: {', '.join(lang_names)}

Context: This is for a retro gaming emulator app. Use appropriate gaming/technical terminology.

IMPORTANT: NEVER translate or transliterate these brand names - keep them exactly as written: {', '.join(DO_NOT_TRANSLATE)}

Return JSON format:
{{
  "ru": "translation",
  "fr": "translation",
  ...
}}

Do not include "en" in output."""

    return prompt


def build_validation_prompt(
    key: str,
    english_text: str,
    translations: Dict[str, str],
    metadata: Optional[Dict] = None
) -> str:
    """
    Build prompt for validating existing translations.

    Args:
        key: String resource key
        english_text: Original English text
        translations: Dictionary of existing translations {lang: text}
        metadata: Optional metadata for context

    Returns:
        Validation prompt

    Example:
        >>> prompt = build_validation_prompt(
        ...     'local_storage',
        ...     'Local Storage',
        ...     {'ru': 'Lokalnoe khranilische', 'fr': 'Stockage local'}
        ... )
    """
    translations_text = "\n".join([
        f"  - {LOCALE_NAMES.get(lang, lang)}: \"{text}\""
        for lang, text in translations.items()
    ])

    context_section = ""
    if metadata:
        category = metadata.get('category', 'N/A')
        purpose = metadata.get('purpose', 'N/A')
        tone = metadata.get('tone', 'neutral')

        context_section = f"""
**Context:**
- Category: {category}
- Purpose: {purpose}
- Tone: {tone}
"""

    prompt = f"""Review and validate the following translations for quality and accuracy.

**String:** {key}
**English:** "{english_text}"
{context_section}
**Translations:**
{translations_text}

**Check for:**
1. Accuracy - Does the translation convey the same meaning?
2. Naturalness - Does it sound natural to native speakers?
3. Consistency - Is terminology consistent?
4. Format specifiers - Are they preserved correctly?
5. Length - Is the translation appropriate length?
6. Gaming terminology - Is appropriate emulator/gaming terminology used?

**Output Format:**
Return JSON with validation results:
{{
  "valid": true/false,
  "issues": {{
    "ru": ["issue 1", "issue 2"],
    "fr": []
  }},
  "suggestions": {{
    "ru": "suggested improvement",
    "fr": "suggested improvement"
  }}
}}"""

    return prompt
