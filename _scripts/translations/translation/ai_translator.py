"""
AI translator using OpenAI and Anthropic APIs.

Provides context-aware translation with structured output.
"""

import json
import os
from typing import Dict, List, Optional
from pathlib import Path

from config import (
    AI_PROVIDER, OPENAI_API_KEY, ANTHROPIC_API_KEY,
    OPENAI_MODEL, ANTHROPIC_MODEL,
    get_locale_file_path
)
from utils.locale_utils import LOCALE_MAPPING, get_all_locales_except_english
from core.xml_parser import AndroidXmlParser, escape_android_string
from translation.prompt_builder import build_multilang_prompt


def normalize_newlines(text: str) -> str:
    """
    Normalize newlines in translated text for Android XML.

    Replaces real newlines (\\n, \\r\\n) with escaped \\n sequence,
    but only if the text doesn't already contain escaped \\n.

    Args:
        text: Translated text that may contain real newlines

    Returns:
        Text with newlines normalized for Android XML
    """
    if not text:
        return text

    # If text already contains escaped \n, don't modify
    if '\\n' in text:
        return text

    # Replace real newlines with escaped \n
    text = text.replace('\r\n', '\\n')  # Windows newlines first
    text = text.replace('\n', '\\n')     # Unix newlines
    text = text.replace('\r', '\\n')     # Old Mac newlines

    return text


class TranslationError(Exception):
    """Raised when translation fails."""
    pass


class AITranslator:
    """AI-powered translator for Android string resources."""

    def __init__(self, provider: str = AI_PROVIDER):
        """
        Initialize AI translator.

        Args:
            provider: AI provider ('openai' or 'anthropic')

        Raises:
            ValueError: If API keys are not configured
        """
        self.provider = provider.lower()

        if self.provider == 'openai':
            if not OPENAI_API_KEY:
                raise ValueError("OPENAI_API_KEY environment variable not set")
            self.api_key = OPENAI_API_KEY
            self.model = OPENAI_MODEL
        elif self.provider == 'anthropic':
            if not ANTHROPIC_API_KEY:
                raise ValueError("ANTHROPIC_API_KEY environment variable not set")
            self.api_key = ANTHROPIC_API_KEY
            self.model = ANTHROPIC_MODEL
        else:
            raise ValueError(f"Unknown provider: {provider}. Use 'openai' or 'anthropic'")

    def translate_string(
        self,
        key: str,
        text: str,
        metadata: Optional[Dict] = None,
        target_languages: Optional[List[str]] = None
    ) -> Dict[str, str]:
        """
        Translate a string to multiple languages.

        Args:
            key: String resource key
            text: English text to translate
            metadata: Optional metadata for context
            target_languages: Target languages (defaults to all except English)

        Returns:
            Dictionary mapping language codes to translations

        Raises:
            TranslationError: If translation fails

        Example:
            >>> translator = AITranslator()
            >>> translations = translator.translate_string(
            ...     'local_storage',
            ...     'Local Storage',
            ...     {'category': 'storage'}
            ... )
            >>> translations['ru']
            'Lokalnoe khranilische'
        """
        if target_languages is None:
            target_languages = get_all_locales_except_english()

        # Build prompt with context
        prompt = build_multilang_prompt(key, text, metadata, target_languages)

        # Call AI API
        try:
            if self.provider == 'openai':
                translations = self._translate_with_openai(prompt, target_languages)
            elif self.provider == 'anthropic':
                translations = self._translate_with_anthropic(prompt, target_languages)
            else:
                raise TranslationError(f"Unknown provider: {self.provider}")

            return translations

        except Exception as e:
            raise TranslationError(f"Translation failed for {key}: {e}")

    def _translate_with_openai(self, prompt: str, target_languages: List[str]) -> Dict[str, str]:
        """
        Translate using OpenAI API with structured output.

        Args:
            prompt: Translation prompt
            target_languages: Target language codes

        Returns:
            Dictionary of translations

        Raises:
            TranslationError: If API call fails
        """
        try:
            from openai import OpenAI
        except ImportError:
            raise TranslationError("openai package not installed. Run: pip install openai")

        client = OpenAI(api_key=self.api_key)

        # Define JSON schema for structured output
        response_schema = {
            "type": "object",
            "properties": {
                lang: {"type": "string"}
                for lang in target_languages
            },
            "required": target_languages,
            "additionalProperties": False
        }

        try:
            response = client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": "You are a professional translator specializing in mobile app localization. "
                                   "You provide accurate, natural, and culturally appropriate translations. "
                                   "This is for a retro gaming emulator app."
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ],
                response_format={
                    "type": "json_schema",
                    "json_schema": {
                        "name": "translation_response",
                        "strict": True,
                        "schema": response_schema
                    }
                },
                temperature=0.3  # Lower temperature for more consistent translations
            )

            # Parse JSON response
            content = response.choices[0].message.content
            translations = json.loads(content)

            # Validate we got all languages
            missing = set(target_languages) - set(translations.keys())
            if missing:
                raise TranslationError(f"Missing translations for: {missing}")

            return translations

        except Exception as e:
            raise TranslationError(f"OpenAI API error: {e}")

    def _translate_with_anthropic(self, prompt: str, target_languages: List[str]) -> Dict[str, str]:
        """
        Translate using Anthropic API.

        Args:
            prompt: Translation prompt
            target_languages: Target language codes

        Returns:
            Dictionary of translations

        Raises:
            TranslationError: If API call fails
        """
        try:
            import anthropic
        except ImportError:
            raise TranslationError("anthropic package not installed. Run: pip install anthropic")

        client = anthropic.Anthropic(api_key=self.api_key)

        try:
            message = client.messages.create(
                model=self.model,
                max_tokens=4096,
                temperature=0.3,
                system="You are a professional translator specializing in mobile app localization. "
                       "You provide accurate, natural, and culturally appropriate translations. "
                       "This is for a retro gaming emulator app. "
                       "Always respond with valid JSON.",
                messages=[
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )

            # Extract JSON from response
            content = message.content[0].text

            # Try to extract JSON from markdown code blocks if present
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()

            translations = json.loads(content)

            # Validate we got all languages
            missing = set(target_languages) - set(translations.keys())
            if missing:
                raise TranslationError(f"Missing translations for: {missing}")

            return translations

        except Exception as e:
            raise TranslationError(f"Anthropic API error: {e}")

    def save_translations(
        self,
        flavor: str,
        key: str,
        translations: Dict[str, str]
    ) -> int:
        """
        Save translations to locale XML files.

        Args:
            flavor: Flavor name ('shared', 'app-free', etc.)
            key: String resource key
            translations: Dictionary mapping language codes to translations

        Returns:
            Number of files updated

        Example:
            >>> translator = AITranslator()
            >>> translations = {'ru': 'Privet', 'fr': 'Bonjour'}
            >>> count = translator.save_translations('shared', 'greeting', translations)
            >>> count
            2
        """
        updated_count = 0

        for locale, translation in translations.items():
            try:
                # Get locale file path
                locale_file = get_locale_file_path(flavor, locale)

                # Normalize newlines: replace real newlines with \n escape sequence
                normalized_translation = normalize_newlines(translation)

                # Escape special characters for Android XML
                escaped_translation = escape_android_string(normalized_translation)

                # Update or create the locale file
                parser = AndroidXmlParser(locale_file)

                if locale_file.exists():
                    # Update existing file
                    parser.update_string(key, escaped_translation)
                else:
                    # Create new file with single string
                    parser.write_strings({key: escaped_translation})

                updated_count += 1

            except Exception as e:
                print(f"Warning: Failed to save {locale} translation for {key}: {e}")

        return updated_count

    def translate_and_save(
        self,
        flavor: str,
        key: str,
        text: str,
        metadata: Optional[Dict] = None,
        target_languages: Optional[List[str]] = None
    ) -> Dict[str, any]:
        """
        Translate a string and save to all locale files.

        Convenience method combining translate_string and save_translations.

        Args:
            flavor: Flavor name
            key: String resource key
            text: English text to translate
            metadata: Optional metadata for context
            target_languages: Target languages (defaults to all)

        Returns:
            Dictionary with results:
            - translations: Dict of translations
            - saved_count: Number of files updated
            - success: Boolean indicating if all saves succeeded

        Example:
            >>> translator = AITranslator()
            >>> result = translator.translate_and_save(
            ...     'shared',
            ...     'local_storage',
            ...     'Local Storage',
            ...     {'category': 'storage'}
            ... )
            >>> result['success']
            True
        """
        # Translate
        translations = self.translate_string(key, text, metadata, target_languages)

        # Save
        saved_count = self.save_translations(flavor, key, translations)

        expected_count = len(translations)
        success = saved_count == expected_count

        return {
            'translations': translations,
            'saved_count': saved_count,
            'expected_count': expected_count,
            'success': success
        }
