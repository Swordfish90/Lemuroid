"""
Hash utilities for tracking string changes.

Uses MD5 hashing to detect when string content has changed.
"""

import hashlib
from typing import Dict


def calculate_text_hash(text: str) -> str:
    """
    Calculate MD5 hash of text content.

    Args:
        text: String content to hash

    Returns:
        First 16 characters of MD5 hash (sufficient for uniqueness in our use case)

    Example:
        >>> calculate_text_hash("Hello World")
        'b10a8db164e0754'
    """
    if not text:
        return ""

    md5_hash = hashlib.md5(text.encode('utf-8')).hexdigest()
    # Use first 16 characters for compactness while maintaining sufficient uniqueness
    return md5_hash[:16]


def calculate_strings_hashes(strings: Dict[str, str]) -> Dict[str, str]:
    """
    Calculate MD5 hashes for multiple strings.

    Args:
        strings: Dictionary mapping string keys to their text values

    Returns:
        Dictionary mapping string keys to their MD5 hashes

    Example:
        >>> calculate_strings_hashes({'key1': 'value1', 'key2': 'value2'})
        {'key1': 'cd42404d52ad55c', 'key2': '66ed76b22e4e9b9'}
    """
    return {key: calculate_text_hash(text) for key, text in strings.items()}


def has_text_changed(old_hash: str, new_text: str) -> bool:
    """
    Check if text has changed by comparing hashes.

    Args:
        old_hash: Previously stored MD5 hash
        new_text: New text content to check

    Returns:
        True if text has changed, False otherwise

    Example:
        >>> old_hash = calculate_text_hash("Hello")
        >>> has_text_changed(old_hash, "Hello")
        False
        >>> has_text_changed(old_hash, "World")
        True
    """
    new_hash = calculate_text_hash(new_text)
    return old_hash != new_hash
