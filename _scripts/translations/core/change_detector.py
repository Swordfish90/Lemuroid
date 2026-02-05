"""
Change detector for identifying string modifications.

Compares current strings.xml state with saved state to detect:
- Added strings (new keys)
- Modified strings (changed text via MD5 hash)
- Removed strings (keys no longer in XML)
"""

from dataclasses import dataclass
from typing import Dict, List

from utils.hash_utils import calculate_text_hash


@dataclass
class StringChange:
    """Information about a changed string."""
    key: str
    old_text: str = ""
    new_text: str = ""
    old_hash: str = ""
    new_hash: str = ""


@dataclass
class ChangeSet:
    """Set of changes detected in strings."""
    added: List[StringChange]
    modified: List[StringChange]
    removed: List[StringChange]
    unchanged: List[str]

    def has_changes(self) -> bool:
        """Check if there are any changes."""
        return len(self.added) > 0 or len(self.modified) > 0 or len(self.removed) > 0

    def total_changes(self) -> int:
        """Get total number of changes."""
        return len(self.added) + len(self.modified) + len(self.removed)

    def summary(self) -> str:
        """
        Get summary string of changes.

        Returns:
            Human-readable summary (e.g., "+3 ~2 -1")
        """
        parts = []
        if self.added:
            parts.append(f"+{len(self.added)}")
        if self.modified:
            parts.append(f"~{len(self.modified)}")
        if self.removed:
            parts.append(f"-{len(self.removed)}")

        if not parts:
            return "No changes"

        return " ".join(parts)


class ChangeDetector:
    """Detects changes between current and saved string states."""

    @staticmethod
    def detect_changes(
        current_strings: Dict[str, str],
        saved_state: Dict[str, Dict[str, str]]
    ) -> ChangeSet:
        """
        Detect changes between current strings and saved state.

        Args:
            current_strings: Current strings from XML {key: text}
            saved_state: Saved state from state.json {key: {text_hash: ..., ...}}

        Returns:
            ChangeSet containing all detected changes

        Example:
            >>> current = {'key1': 'Hello', 'key2': 'World', 'key3': 'New'}
            >>> saved = {
            ...     'key1': {'text_hash': calculate_text_hash('Hello')},
            ...     'key2': {'text_hash': calculate_text_hash('Goodbye')},
            ...     'key4': {'text_hash': 'old_hash'}
            ... }
            >>> changes = ChangeDetector.detect_changes(current, saved)
            >>> len(changes.added)
            1
            >>> len(changes.modified)
            1
            >>> len(changes.removed)
            1
        """
        added: List[StringChange] = []
        modified: List[StringChange] = []
        removed: List[StringChange] = []
        unchanged: List[str] = []

        # Calculate hashes for current strings
        current_hashes = {key: calculate_text_hash(text) for key, text in current_strings.items()}

        # Find added and modified strings
        for key, current_text in current_strings.items():
            current_hash = current_hashes[key]

            if key not in saved_state:
                # New string (added)
                added.append(StringChange(
                    key=key,
                    new_text=current_text,
                    new_hash=current_hash
                ))
            else:
                saved_hash = saved_state[key].get('text_hash', '')

                if current_hash != saved_hash:
                    # String exists but text changed (modified)
                    modified.append(StringChange(
                        key=key,
                        old_text="",  # We don't store old text, only hash
                        new_text=current_text,
                        old_hash=saved_hash,
                        new_hash=current_hash
                    ))
                else:
                    # String unchanged
                    unchanged.append(key)

        # Find removed strings
        for key in saved_state.keys():
            if key not in current_strings:
                removed.append(StringChange(
                    key=key,
                    old_text="",  # We don't store old text, only hash
                    old_hash=saved_state[key].get('text_hash', '')
                ))

        return ChangeSet(
            added=sorted(added, key=lambda x: x.key),
            modified=sorted(modified, key=lambda x: x.key),
            removed=sorted(removed, key=lambda x: x.key),
            unchanged=sorted(unchanged)
        )

    @staticmethod
    def filter_changes_by_keys(changeset: ChangeSet, keys: List[str]) -> ChangeSet:
        """
        Filter a ChangeSet to only include specific keys.

        Args:
            changeset: Original ChangeSet
            keys: List of keys to keep

        Returns:
            Filtered ChangeSet

        Example:
            >>> changes = ChangeSet(...)
            >>> filtered = ChangeDetector.filter_changes_by_keys(changes, ['key1', 'key2'])
        """
        keys_set = set(keys)

        return ChangeSet(
            added=[c for c in changeset.added if c.key in keys_set],
            modified=[c for c in changeset.modified if c.key in keys_set],
            removed=[c for c in changeset.removed if c.key in keys_set],
            unchanged=[k for k in changeset.unchanged if k in keys_set]
        )

    @staticmethod
    def get_keys_requiring_translation(changeset: ChangeSet) -> List[str]:
        """
        Get list of keys that need translation (added + modified).

        Args:
            changeset: ChangeSet to analyze

        Returns:
            List of string keys needing translation

        Example:
            >>> changes = ChangeSet(...)
            >>> keys = ChangeDetector.get_keys_requiring_translation(changes)
            >>> # Keys from both added and modified lists
        """
        keys = []
        keys.extend([c.key for c in changeset.added])
        keys.extend([c.key for c in changeset.modified])
        return sorted(keys)

    @staticmethod
    def group_changes_by_type(changes: List[StringChange]) -> Dict[str, List[StringChange]]:
        """
        Group changes by some categorization.

        Currently a placeholder for future categorization needs.

        Args:
            changes: List of StringChange objects

        Returns:
            Dictionary grouping changes
        """
        # Placeholder for future categorization
        return {'all': changes}
