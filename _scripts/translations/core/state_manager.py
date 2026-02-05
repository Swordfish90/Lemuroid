"""
State manager for tracking string changes via state.json.

Manages the state file that contains MD5 hashes of all strings for change detection.
"""

import json
from pathlib import Path
from datetime import datetime
from typing import Dict, Optional, List
from dataclasses import dataclass, asdict

from config import STATE_FILE, STATE_VERSION, SUPPORTED_LANGUAGES, FLAVOR_CONFIG


@dataclass
class StringState:
    """State information for a single string."""
    text_hash: str
    last_modified: str
    has_metadata: bool
    is_translated: bool  # Whether translations exist for this string


@dataclass
class FlavorState:
    """State information for a flavor."""
    name: str
    display_name: str
    source_path: str
    metadata_file: str
    locales_path: str
    string_count: int
    last_scan: str
    strings: Dict[str, StringState]


class StateManager:
    """Manages the state.json state file."""

    def __init__(self, state_file: Path = STATE_FILE):
        """
        Initialize state manager.

        Args:
            state_file: Path to state.json
        """
        self.state_file = state_file
        self._state: Optional[Dict] = None

    def load_state(self) -> Dict:
        """
        Load state from state.json.

        Returns:
            State dictionary

        Raises:
            FileNotFoundError: If state file doesn't exist
            json.JSONDecodeError: If state file is malformed
        """
        if not self.state_file.exists():
            raise FileNotFoundError(f"State file not found: {self.state_file}")

        with open(self.state_file, 'r', encoding='utf-8') as f:
            self._state = json.load(f)

        return self._state

    def save_state(self, state: Optional[Dict] = None) -> None:
        """
        Save state to state.json.

        Args:
            state: State dictionary to save. If None, saves cached state.
        """
        if state is None:
            if self._state is None:
                raise ValueError("No state to save. Either provide state or load it first.")
            state = self._state

        # Update last_updated timestamp
        state['last_updated'] = datetime.now().isoformat()

        # Ensure parent directory exists
        self.state_file.parent.mkdir(parents=True, exist_ok=True)

        # Write with pretty printing
        with open(self.state_file, 'w', encoding='utf-8') as f:
            json.dump(state, f, indent=2, ensure_ascii=False)

        self._state = state

    def init_state(self) -> Dict:
        """
        Initialize a new empty state structure.

        Returns:
            New state dictionary

        Example:
            >>> manager = StateManager()
            >>> state = manager.init_state()
            >>> state['version']
            '2.0'
        """
        state = {
            'version': STATE_VERSION,
            'last_updated': datetime.now().isoformat(),
            'system_info': {
                'created_by': 'i18n_sync.py v2.0',
                'python_version': '3.10+',
                'total_tracked_strings': 0
            },
            'flavors': {},
            'supported_languages': SUPPORTED_LANGUAGES
        }

        self._state = state
        return state

    def get_flavor_state(self, flavor: str) -> Optional[Dict]:
        """
        Get state for a specific flavor.

        Args:
            flavor: Flavor name

        Returns:
            Flavor state dictionary or None if not found

        Example:
            >>> manager = StateManager()
            >>> manager.load_state()
            >>> flavor_state = manager.get_flavor_state('shared')
            >>> flavor_state['string_count']
            144
        """
        if self._state is None:
            self.load_state()

        return self._state.get('flavors', {}).get(flavor)

    def init_flavor(self, flavor: str, strings_with_hashes: Dict[str, str], translated_keys: Optional[set] = None) -> Dict:
        """
        Initialize state for a new flavor.

        Args:
            flavor: Flavor name
            strings_with_hashes: Dict mapping string keys to their MD5 hashes
            translated_keys: Set of keys that already have translations

        Returns:
            Initialized flavor state dictionary

        Example:
            >>> manager = StateManager()
            >>> manager.load_state()
            >>> strings = {'key1': 'hash1', 'key2': 'hash2'}
            >>> flavor_state = manager.init_flavor('shared', strings, {'key1'})
        """
        if self._state is None:
            self.load_state()

        if flavor not in FLAVOR_CONFIG:
            raise ValueError(f"Unknown flavor: {flavor}")

        if translated_keys is None:
            translated_keys = set()

        config = FLAVOR_CONFIG[flavor]
        now = datetime.now().strftime('%Y-%m-%d')

        # Build strings state
        strings_state = {}
        for key, text_hash in strings_with_hashes.items():
            strings_state[key] = {
                'text_hash': text_hash,
                'last_modified': now,
                'has_metadata': False,  # Will be updated during migration
                'is_translated': key in translated_keys
            }

        # Create flavor state
        flavor_state = {
            'name': config['name'],
            'display_name': config['display_name'],
            'source_path': config['source_path'],
            'metadata_file': config['metadata_file'],
            'locales_path': config['locales_path'],
            'string_count': len(strings_with_hashes),
            'last_scan': datetime.now().isoformat(),
            'strings': strings_state
        }

        self._state['flavors'][flavor] = flavor_state

        # Update total count
        total = sum(
            f['string_count']
            for f in self._state['flavors'].values()
        )
        self._state['system_info']['total_tracked_strings'] = total

        return flavor_state

    def update_string_hash(self, flavor: str, key: str, text_hash: str, has_metadata: bool = False, is_translated: bool = False) -> None:
        """
        Update hash for a string.

        Args:
            flavor: Flavor name
            key: String resource key
            text_hash: New MD5 hash
            has_metadata: Whether string has metadata
            is_translated: Whether translations exist

        Example:
            >>> manager = StateManager()
            >>> manager.load_state()
            >>> manager.update_string_hash('shared', 'app_name', 'abc123def456', True, True)
            >>> manager.save_state()
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            raise ValueError(f"Flavor not initialized: {flavor}")

        now = datetime.now().strftime('%Y-%m-%d')

        flavor_state['strings'][key] = {
            'text_hash': text_hash,
            'last_modified': now,
            'has_metadata': has_metadata,
            'is_translated': is_translated
        }

        flavor_state['last_scan'] = datetime.now().isoformat()

    def add_string(self, flavor: str, key: str, text_hash: str, has_metadata: bool = False, is_translated: bool = False) -> None:
        """
        Add a new string to state.

        Args:
            flavor: Flavor name
            key: String resource key
            text_hash: MD5 hash of the text
            has_metadata: Whether string has metadata
            is_translated: Whether translations exist
        """
        self.update_string_hash(flavor, key, text_hash, has_metadata, is_translated)

        # Update string count
        if self._state:
            flavor_state = self._state['flavors'].get(flavor)
            if flavor_state:
                flavor_state['string_count'] = len(flavor_state['strings'])

                # Update total
                total = sum(
                    f['string_count']
                    for f in self._state['flavors'].values()
                )
                self._state['system_info']['total_tracked_strings'] = total

    def remove_string(self, flavor: str, key: str) -> None:
        """
        Remove a string from state.

        Args:
            flavor: Flavor name
            key: String resource key

        Example:
            >>> manager = StateManager()
            >>> manager.load_state()
            >>> manager.remove_string('shared', 'old_key')
            >>> manager.save_state()
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            raise ValueError(f"Flavor not initialized: {flavor}")

        if key in flavor_state['strings']:
            del flavor_state['strings'][key]

            # Update string count
            flavor_state['string_count'] = len(flavor_state['strings'])
            flavor_state['last_scan'] = datetime.now().isoformat()

            # Update total
            total = sum(
                f['string_count']
                for f in self._state['flavors'].values()
            )
            self._state['system_info']['total_tracked_strings'] = total

    def get_string_hash(self, flavor: str, key: str) -> Optional[str]:
        """
        Get hash for a string.

        Args:
            flavor: Flavor name
            key: String resource key

        Returns:
            MD5 hash or None if not found
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            return None

        string_state = flavor_state['strings'].get(key)
        if not string_state:
            return None

        return string_state['text_hash']

    def get_all_string_keys(self, flavor: str) -> List[str]:
        """
        Get all string keys for a flavor.

        Args:
            flavor: Flavor name

        Returns:
            List of string keys
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            return []

        return list(flavor_state['strings'].keys())

    def state_exists(self) -> bool:
        """
        Check if state file exists.

        Returns:
            True if state file exists, False otherwise
        """
        return self.state_file.exists()

    def is_string_translated(self, flavor: str, key: str) -> bool:
        """
        Check if a string is marked as translated.

        Args:
            flavor: Flavor name
            key: String resource key

        Returns:
            True if string is translated, False otherwise
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            return False

        string_state = flavor_state['strings'].get(key)
        if not string_state:
            return False

        return string_state.get('is_translated', False)

    def mark_as_translated(self, flavor: str, key: str) -> None:
        """
        Mark a string as translated.

        Args:
            flavor: Flavor name
            key: String resource key
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            raise ValueError(f"Flavor not initialized: {flavor}")

        if key in flavor_state['strings']:
            flavor_state['strings'][key]['is_translated'] = True

    def get_untranslated_keys(self, flavor: str) -> List[str]:
        """
        Get all keys that are not yet translated.

        Args:
            flavor: Flavor name

        Returns:
            List of untranslated string keys
        """
        if self._state is None:
            self.load_state()

        flavor_state = self._state['flavors'].get(flavor)
        if not flavor_state:
            return []

        return [
            key for key, state in flavor_state['strings'].items()
            if not state.get('is_translated', False)
        ]
