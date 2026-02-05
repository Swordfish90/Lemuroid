"""Core modules for i18n_sync system."""

from .xml_parser import AndroidXmlParser, escape_android_string, unescape_android_string, detect_format_specifiers
from .state_manager import StateManager
from .change_detector import ChangeDetector, ChangeSet, StringChange
from .metadata_manager import MetadataManager

__all__ = [
    'AndroidXmlParser',
    'escape_android_string',
    'unescape_android_string',
    'detect_format_specifiers',
    'StateManager',
    'ChangeDetector',
    'ChangeSet',
    'StringChange',
    'MetadataManager'
]
