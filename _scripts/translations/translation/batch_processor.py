"""
Batch processor for translating multiple strings efficiently.

Handles rate limiting, retries, and progress tracking.
"""

import time
from typing import Dict, List, Optional, Callable
from dataclasses import dataclass

from translation.ai_translator import AITranslator, TranslationError


@dataclass
class BatchResult:
    """Results from batch translation."""
    total: int
    successful: int
    failed: int
    skipped: int
    failed_keys: List[str]
    duration_seconds: float


class BatchProcessor:
    """Processes multiple translations with rate limiting and retries."""

    def __init__(
        self,
        translator: Optional[AITranslator] = None,
        max_retries: int = 3,
        retry_delay: float = 2.0,
        rate_limit_delay: float = 0.5
    ):
        """
        Initialize batch processor.

        Args:
            translator: AI translator instance (creates new if None)
            max_retries: Maximum retry attempts for failed translations
            retry_delay: Delay between retries in seconds
            rate_limit_delay: Delay between API calls to avoid rate limits

        Example:
            >>> processor = BatchProcessor(max_retries=2, rate_limit_delay=1.0)
        """
        self.translator = translator or AITranslator()
        self.max_retries = max_retries
        self.retry_delay = retry_delay
        self.rate_limit_delay = rate_limit_delay

    def process_batch(
        self,
        flavor: str,
        strings: Dict[str, str],
        metadata_dict: Optional[Dict[str, Dict]] = None,
        target_languages: Optional[List[str]] = None,
        progress_callback: Optional[Callable[[int, int, str], None]] = None,
        skip_existing: bool = False
    ) -> BatchResult:
        """
        Process a batch of strings for translation.

        Args:
            flavor: Flavor name ('shared', 'app-free', etc.)
            strings: Dictionary mapping keys to English text
            metadata_dict: Optional metadata for each string key
            target_languages: Target languages (defaults to all)
            progress_callback: Callback function(current, total, key) for progress updates
            skip_existing: If True, skip strings that already have translations

        Returns:
            BatchResult with statistics

        Example:
            >>> processor = BatchProcessor()
            >>> strings = {'key1': 'Hello', 'key2': 'World'}
            >>> metadata = {'key1': {'category': 'greetings'}}
            >>> result = processor.process_batch('shared', strings, metadata)
            >>> print(f"Successful: {result.successful}/{result.total}")
        """
        if metadata_dict is None:
            metadata_dict = {}

        total = len(strings)
        successful = 0
        failed = 0
        skipped = 0
        failed_keys = []

        start_time = time.time()

        for idx, (key, text) in enumerate(strings.items(), 1):
            # Progress callback
            if progress_callback:
                progress_callback(idx, total, key)

            # Skip if requested
            if skip_existing and self._has_translations(flavor, key, target_languages):
                skipped += 1
                continue

            # Get metadata for this string
            metadata = metadata_dict.get(key)

            # Translate with retries
            retry_count = 0
            success = False

            while retry_count <= self.max_retries and not success:
                try:
                    result = self.translator.translate_and_save(
                        flavor, key, text, metadata, target_languages
                    )

                    if result['success']:
                        successful += 1
                        success = True
                    else:
                        # Partial failure - some saves failed
                        print(f"Warning: Partial failure for {key}: "
                              f"{result['saved_count']}/{result['expected_count']} saved")
                        successful += 1  # Count as success if at least some saved
                        success = True

                except TranslationError as e:
                    retry_count += 1
                    if retry_count <= self.max_retries:
                        print(f"Retry {retry_count}/{self.max_retries} for {key}: {e}")
                        time.sleep(self.retry_delay)
                    else:
                        print(f"Failed after {self.max_retries} retries: {key}")
                        failed += 1
                        failed_keys.append(key)

            # Rate limiting delay between API calls
            if idx < total:  # Don't delay after last item
                time.sleep(self.rate_limit_delay)

        duration = time.time() - start_time

        return BatchResult(
            total=total,
            successful=successful,
            failed=failed,
            skipped=skipped,
            failed_keys=failed_keys,
            duration_seconds=duration
        )

    def _has_translations(
        self,
        flavor: str,
        key: str,
        target_languages: Optional[List[str]] = None
    ) -> bool:
        """
        Check if a string already has translations.

        Args:
            flavor: Flavor name
            key: String key
            target_languages: Languages to check (defaults to all)

        Returns:
            True if translations exist for all target languages

        Example:
            >>> processor = BatchProcessor()
            >>> has_trans = processor._has_translations('shared', 'local_storage')
        """
        from config import get_locale_file_path
        from core.xml_parser import AndroidXmlParser
        from utils.locale_utils import get_all_locales_except_english

        if target_languages is None:
            target_languages = get_all_locales_except_english()

        # Check if key exists in all locale files
        for locale in target_languages:
            locale_file = get_locale_file_path(flavor, locale)

            if not locale_file.exists():
                return False

            parser = AndroidXmlParser(locale_file)
            try:
                translation = parser.get_string(key)
                if not translation:
                    return False
            except:
                return False

        return True

    def retry_failed(
        self,
        flavor: str,
        failed_keys: List[str],
        strings: Dict[str, str],
        metadata_dict: Optional[Dict[str, Dict]] = None,
        target_languages: Optional[List[str]] = None,
        progress_callback: Optional[Callable[[int, int, str], None]] = None
    ) -> BatchResult:
        """
        Retry translation for previously failed strings.

        Args:
            flavor: Flavor name
            failed_keys: List of keys that failed previously
            strings: Original strings dictionary
            metadata_dict: Metadata dictionary
            target_languages: Target languages
            progress_callback: Progress callback

        Returns:
            BatchResult for retry attempt

        Example:
            >>> processor = BatchProcessor()
            >>> # After initial batch...
            >>> if result.failed > 0:
            ...     retry_result = processor.retry_failed(
            ...         'shared', result.failed_keys, strings, metadata
            ...     )
        """
        # Filter strings to only failed keys
        failed_strings = {k: strings[k] for k in failed_keys if k in strings}

        return self.process_batch(
            flavor,
            failed_strings,
            metadata_dict,
            target_languages,
            progress_callback,
            skip_existing=False  # Don't skip for retries
        )

    def estimate_cost(
        self,
        num_strings: int,
        cost_per_string: float = 0.04
    ) -> Dict[str, float]:
        """
        Estimate translation cost for a batch.

        Args:
            num_strings: Number of strings to translate
            cost_per_string: Estimated cost per string (default $0.04 for 31 languages)

        Returns:
            Dictionary with cost estimates

        Example:
            >>> processor = BatchProcessor()
            >>> estimate = processor.estimate_cost(100)
            >>> print(f"Estimated cost: ${estimate['total']:.2f}")
        """
        total_cost = num_strings * cost_per_string
        cost_per_language = cost_per_string / 31  # Approximate for 31 target languages

        return {
            'total': total_cost,
            'per_string': cost_per_string,
            'per_language_per_string': cost_per_language,
            'num_strings': num_strings
        }
