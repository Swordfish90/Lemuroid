"""
Metadata manager for simplified JSON format.

Manages simplified metadata stored in data/*.json files.
"""

import json
from pathlib import Path
from typing import Dict, Optional, List

from config import get_flavor_path


class MetadataManager:
    """Manages simplified metadata JSON files."""

    def __init__(self, flavor: str):
        """
        Initialize metadata manager for a flavor.

        Args:
            flavor: Flavor name ('shared', 'app-free', etc.)
        """
        self.flavor = flavor
        self.metadata_file = get_flavor_path(flavor, 'metadata_file')
        self._metadata: Optional[Dict] = None

    def load_metadata(self) -> Dict[str, Dict]:
        """
        Load metadata from JSON file.

        Returns:
            Dictionary mapping string keys to their metadata

        Raises:
            FileNotFoundError: If metadata file doesn't exist

        Example:
            >>> manager = MetadataManager('shared')
            >>> metadata = manager.load_metadata()
            >>> metadata['local_storage']
            {'category': 'storage', 'purpose': '...', ...}
        """
        if not self.metadata_file.exists():
            raise FileNotFoundError(f"Metadata file not found: {self.metadata_file}")

        with open(self.metadata_file, 'r', encoding='utf-8') as f:
            data = json.load(f)

        self._metadata = data.get('metadata', {})
        return self._metadata

    def save_metadata(self, metadata: Optional[Dict[str, Dict]] = None) -> None:
        """
        Save metadata to JSON file.

        Args:
            metadata: Metadata dictionary to save. If None, saves cached metadata.

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> manager.save_metadata()
        """
        if metadata is None:
            if self._metadata is None:
                raise ValueError("No metadata to save. Either provide metadata or load it first.")
            metadata = self._metadata

        data = {
            'version': '2.0',
            'flavor': self.flavor,
            'metadata': metadata
        }

        # Ensure parent directory exists
        self.metadata_file.parent.mkdir(parents=True, exist_ok=True)

        # Write with pretty printing
        with open(self.metadata_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)

        self._metadata = metadata

    def get_string_metadata(self, key: str) -> Optional[Dict]:
        """
        Get metadata for a specific string.

        Args:
            key: String resource key

        Returns:
            Metadata dictionary or None if not found

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> meta = manager.get_string_metadata('local_storage')
            >>> meta['category']
            'storage'
        """
        if self._metadata is None:
            self.load_metadata()

        return self._metadata.get(key)

    def add_string_metadata(self, key: str, metadata: Dict) -> None:
        """
        Add metadata for a new string.

        Args:
            key: String resource key
            metadata: Metadata dictionary

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> manager.add_string_metadata('new_key', {
            ...     'category': 'settings',
            ...     'purpose': 'Test purpose',
            ...     'max_length': 50,
            ...     'tone': 'technical',
            ...     'technical': {'format_specifiers': False, 'html': False, 'emoji': False}
            ... })
            >>> manager.save_metadata()
        """
        if self._metadata is None:
            self.load_metadata()

        self._metadata[key] = metadata

    def update_string_metadata(self, key: str, metadata: Dict) -> None:
        """
        Update metadata for an existing string.

        Args:
            key: String resource key
            metadata: New metadata dictionary (replaces existing)

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> manager.update_string_metadata('existing_key', new_metadata)
            >>> manager.save_metadata()
        """
        self.add_string_metadata(key, metadata)

    def remove_string_metadata(self, key: str) -> bool:
        """
        Remove metadata for a string.

        Args:
            key: String resource key

        Returns:
            True if metadata was removed, False if key didn't exist

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> removed = manager.remove_string_metadata('old_key')
            >>> if removed:
            ...     manager.save_metadata()
        """
        if self._metadata is None:
            self.load_metadata()

        if key in self._metadata:
            del self._metadata[key]
            return True

        return False

    def get_all_keys(self) -> List[str]:
        """
        Get list of all string keys with metadata.

        Returns:
            List of string keys

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> keys = manager.get_all_keys()
            >>> len(keys)
            144
        """
        if self._metadata is None:
            self.load_metadata()

        return list(self._metadata.keys())

    def has_metadata(self, key: str) -> bool:
        """
        Check if a string has metadata.

        Args:
            key: String resource key

        Returns:
            True if metadata exists, False otherwise

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> manager.has_metadata('local_storage')
            True
        """
        if self._metadata is None:
            self.load_metadata()

        return key in self._metadata

    def init_empty_metadata(self) -> None:
        """
        Initialize an empty metadata file.

        Creates a new metadata file with empty metadata dictionary.

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.init_empty_metadata()
            >>> manager.save_metadata()
        """
        self._metadata = {}
        self.save_metadata()

    def metadata_exists(self) -> bool:
        """
        Check if metadata file exists.

        Returns:
            True if file exists, False otherwise

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.metadata_exists()
            True
        """
        return self.metadata_file.exists()

    def get_metadata_count(self) -> int:
        """
        Get count of strings with metadata.

        Returns:
            Number of strings with metadata

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> manager.get_metadata_count()
            144
        """
        if self._metadata is None:
            self.load_metadata()

        return len(self._metadata)

    def get_strings_by_category(self, category: str) -> List[str]:
        """
        Get all string keys in a specific category.

        Args:
            category: Category name (e.g., 'settings', 'emulator')

        Returns:
            List of string keys in that category

        Example:
            >>> manager = MetadataManager('shared')
            >>> manager.load_metadata()
            >>> settings_strings = manager.get_strings_by_category('settings')
            >>> len(settings_strings)
            50
        """
        if self._metadata is None:
            self.load_metadata()

        return [
            key for key, meta in self._metadata.items()
            if meta.get('category') == category
        ]
