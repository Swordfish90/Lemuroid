"""
XML parser for Android strings.xml files.

Handles reading and writing Android string resources with proper escaping.
"""

import xml.etree.ElementTree as ET
from pathlib import Path
from typing import Dict, Optional
import re


def escape_android_string(text: str) -> str:
    r"""
    Escape special characters for Android XML string resources.

    Android requires:
    - Apostrophes (') must be escaped as \'
    - Quotes (") must be escaped as \"
    - Newlines (\n) and tabs (\t) should be kept as-is (already escaped)
    - Backslashes (\) must be escaped as \\
    - @ at the start must be escaped as \@
    - ? at the start must be escaped as \?

    Args:
        text: String to escape

    Returns:
        Escaped string safe for Android XML

    Example:
        >>> escape_android_string("It's a test")
        "It\\'s a test"
        >>> escape_android_string('@string/ref')
        '\\@string/ref'
    """
    if not text:
        return text

    # Escape backslashes first (must be done before other escapes)
    text = text.replace('\\', '\\\\')

    # Escape apostrophes
    text = text.replace("'", "\\'")

    # Escape quotes
    text = text.replace('"', '\\"')

    # Escape @ and ? at the start
    if text.startswith('@'):
        text = '\\' + text
    elif text.startswith('?'):
        text = '\\' + text

    return text


def unescape_android_string(text: str) -> str:
    r"""
    Unescape Android XML string for display/editing.

    Reverses the escaping done by escape_android_string.

    Args:
        text: Escaped Android string

    Returns:
        Unescaped string

    Example:
        >>> unescape_android_string("It\\'s a test")
        "It's a test"
    """
    if not text:
        return text

    # Unescape in reverse order
    if text.startswith('\\@') or text.startswith('\\?'):
        text = text[1:]

    text = text.replace('\\"', '"')
    text = text.replace("\\'", "'")
    text = text.replace('\\\\', '\\')

    return text


def detect_format_specifiers(text: str) -> tuple[bool, list[str]]:
    """
    Detect format specifiers in a string.

    Finds patterns like %s, %d, %1$s, etc.

    Args:
        text: String to analyze

    Returns:
        Tuple of (has_specifiers, list_of_specifiers)

    Example:
        >>> detect_format_specifiers("Hello %s, you have %d messages")
        (True, ['%s', '%d'])
    """
    # Pattern: %[position$][flags][width][.precision]type
    # Common types: s (string), d (decimal), f (float), etc.
    pattern = r'%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[hlL]?[sdfioxXeEgGcbBp]'
    specifiers = re.findall(pattern, text)
    return (len(specifiers) > 0, specifiers)


class AndroidXmlParser:
    """Parser for Android strings.xml files."""

    def __init__(self, xml_path: Path):
        """
        Initialize parser for a strings.xml file.

        Args:
            xml_path: Path to strings.xml file
        """
        self.xml_path = Path(xml_path)

    def parse_strings(self, include_non_translatable: bool = False) -> Dict[str, str]:
        """
        Parse strings.xml and extract all string resources.

        Args:
            include_non_translatable: If False (default), skip strings with translatable="false"

        Returns:
            Dictionary mapping string keys to their text values

        Raises:
            FileNotFoundError: If XML file doesn't exist
            ET.ParseError: If XML is malformed

        Example:
            >>> parser = AndroidXmlParser(Path('strings.xml'))
            >>> strings = parser.parse_strings()
            >>> strings['app_name']
            'My App'
        """
        if not self.xml_path.exists():
            raise FileNotFoundError(f"strings.xml not found: {self.xml_path}")

        tree = ET.parse(self.xml_path)
        root = tree.getroot()

        strings = {}
        for string_elem in root.findall('.//string'):
            name = string_elem.get('name')
            if name:
                # Skip non-translatable strings unless explicitly requested
                if not include_non_translatable:
                    translatable = string_elem.get('translatable', 'true')
                    if translatable.lower() == 'false':
                        continue
                # Get text, handling None case
                text = string_elem.text or ''
                strings[name] = text

        return strings

    def write_strings(self, strings: Dict[str, str], pretty: bool = True) -> None:
        """
        Write strings to XML file.

        Args:
            strings: Dictionary of string key-value pairs
            pretty: If True, format XML with indentation

        Example:
            >>> parser = AndroidXmlParser(Path('strings.xml'))
            >>> parser.write_strings({'app_name': 'My App', 'welcome': 'Welcome!'})
        """
        # Create root element
        root = ET.Element('resources')

        # Add string elements
        for key in sorted(strings.keys()):
            string_elem = ET.SubElement(root, 'string', name=key)
            string_elem.text = strings[key]

        # Pretty print
        if pretty:
            self._indent(root)

        # Write to file
        tree = ET.ElementTree(root)
        self.xml_path.parent.mkdir(parents=True, exist_ok=True)
        tree.write(self.xml_path, encoding='utf-8', xml_declaration=True)

    def update_string(self, key: str, value: str) -> None:
        """
        Update a single string in the XML file.

        If the string doesn't exist, it will be added.
        If it exists, it will be updated.

        Args:
            key: String resource key
            value: New value for the string

        Example:
            >>> parser = AndroidXmlParser(Path('strings.xml'))
            >>> parser.update_string('app_name', 'New App Name')
        """
        if not self.xml_path.exists():
            # Create new file with single string
            self.write_strings({key: value})
            return

        tree = ET.parse(self.xml_path)
        root = tree.getroot()

        # Try to find existing string
        found = False
        for string_elem in root.findall('.//string'):
            if string_elem.get('name') == key:
                string_elem.text = value
                found = True
                break

        # Add new string if not found
        if not found:
            string_elem = ET.SubElement(root, 'string', name=key)
            string_elem.text = value

        # Pretty print and save
        self._indent(root)
        tree.write(self.xml_path, encoding='utf-8', xml_declaration=True)

    def get_string(self, key: str) -> Optional[str]:
        """
        Get a single string value by key.

        Args:
            key: String resource key

        Returns:
            String value or None if not found

        Example:
            >>> parser = AndroidXmlParser(Path('strings.xml'))
            >>> parser.get_string('app_name')
            'My App'
        """
        if not self.xml_path.exists():
            return None

        tree = ET.parse(self.xml_path)
        root = tree.getroot()

        for string_elem in root.findall('.//string'):
            if string_elem.get('name') == key:
                return string_elem.text or ''

        return None

    @staticmethod
    def _indent(elem, level=0):
        """
        Add pretty-printing indentation to XML tree.

        Args:
            elem: XML element
            level: Current indentation level
        """
        indent = "\n" + "  " * level
        if len(elem):
            if not elem.text or not elem.text.strip():
                elem.text = indent + "  "
            if not elem.tail or not elem.tail.strip():
                elem.tail = indent
            for child in elem:
                AndroidXmlParser._indent(child, level + 1)
            if not child.tail or not child.tail.strip():
                child.tail = indent
        else:
            if level and (not elem.tail or not elem.tail.strip()):
                elem.tail = indent
