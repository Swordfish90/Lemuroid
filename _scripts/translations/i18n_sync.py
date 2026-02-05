#!/usr/bin/env python3
"""
i18n_sync.py - Unified localization management system for FullRoid.

Main CLI entry point for managing Android string translations.
"""

import click
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from config import FLAVOR_CONFIG, get_flavor_path, ensure_directories, get_locale_file_path, SUPPORTED_LANGUAGES, LOCALE_MAPPING
from core.state_manager import StateManager
from core.xml_parser import AndroidXmlParser
from core.change_detector import ChangeDetector
from core.metadata_manager import MetadataManager
from translation.ai_translator import AITranslator
from translation.batch_processor import BatchProcessor
from utils.interactive import *
from utils.hash_utils import calculate_strings_hashes


def get_string_keys_from_xml(xml_path: Path) -> set:
    """Get all string keys from an XML file."""
    if not xml_path.exists():
        return set()
    tree = ET.parse(xml_path)
    root = tree.getroot()
    return {elem.get('name') for elem in root.findall('string') if elem.get('name')}


def find_missing_translations(flavor: str, source_strings: dict) -> set:
    """Find strings that exist in source but are missing in Russian locale."""
    ru_path = get_locale_file_path(flavor, 'ru')
    ru_keys = get_string_keys_from_xml(ru_path)
    source_keys = set(source_strings.keys())
    return source_keys - ru_keys


def find_obsolete_strings_in_locale(source_keys: set, locale_path: Path) -> set:
    """Find strings that exist in locale but not in source (obsolete strings)."""
    locale_keys = get_string_keys_from_xml(locale_path)
    return locale_keys - source_keys


def remove_strings_from_xml(xml_path: Path, keys_to_remove: set) -> int:
    """
    Remove specified string keys from an XML file.

    Returns the number of strings removed.
    """
    if not xml_path.exists() or not keys_to_remove:
        return 0

    tree = ET.parse(xml_path)
    root = tree.getroot()

    removed_count = 0
    elements_to_remove = []

    for elem in root.findall('string'):
        name = elem.get('name')
        if name in keys_to_remove:
            elements_to_remove.append(elem)

    for elem in elements_to_remove:
        root.remove(elem)
        removed_count += 1

    if removed_count > 0:
        # Write back with proper formatting
        tree.write(xml_path, encoding='UTF-8', xml_declaration=True)

        # Fix formatting (ElementTree doesn't preserve nice formatting)
        with open(xml_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Add newlines after xml declaration if missing
        if '?><' in content:
            content = content.replace('?><', '?>\n<')

        with open(xml_path, 'w', encoding='utf-8') as f:
            f.write(content)

    return removed_count


def cleanup_obsolete_strings(flavor: str, source_keys: set, auto_yes: bool = False) -> dict:
    """
    Remove obsolete strings from all locale files for a flavor.

    Returns dict with stats per locale.
    """
    locales_path = get_flavor_path(flavor, 'locales_path')
    stats = {}
    total_removed = 0

    # Find all values-* directories
    for item in locales_path.iterdir():
        if item.is_dir() and item.name.startswith('values-'):
            locale_code = item.name.replace('values-', '')
            strings_file = item / 'strings.xml'

            if strings_file.exists():
                obsolete_keys = find_obsolete_strings_in_locale(source_keys, strings_file)

                if obsolete_keys:
                    stats[locale_code] = {
                        'obsolete_count': len(obsolete_keys),
                        'obsolete_keys': sorted(obsolete_keys)
                    }
                    total_removed += len(obsolete_keys)

    return stats, total_removed


def check_existing_translations(flavor: str, source_keys: set, min_locales: int = 3) -> set:
    """
    Check which strings already have translations.

    A string is considered translated if it exists in at least min_locales locale files.

    Args:
        flavor: Flavor name
        source_keys: Set of string keys from source
        min_locales: Minimum number of locales a string must exist in to be considered translated

    Returns:
        Set of keys that have existing translations
    """
    locales_path = get_flavor_path(flavor, 'locales_path')
    key_locale_count = {key: 0 for key in source_keys}

    # Count how many locales each key exists in
    for item in locales_path.iterdir():
        if item.is_dir() and item.name.startswith('values-') and item.name != 'values':
            strings_file = item / 'strings.xml'
            if strings_file.exists():
                locale_keys = get_string_keys_from_xml(strings_file)
                for key in source_keys:
                    if key in locale_keys:
                        key_locale_count[key] += 1

    # Return keys that exist in at least min_locales
    return {key for key, count in key_locale_count.items() if count >= min_locales}


def get_missing_locales_for_key(flavor: str, key: str) -> list:
    """
    Get list of locales where translation is missing for a specific key.

    Args:
        flavor: Flavor name
        key: String key to check

    Returns:
        List of API locale codes (e.g., ['ru', 'de', 'fr']) where translation is missing
    """
    from utils.locale_utils import get_all_locales_except_english

    locales_path = get_flavor_path(flavor, 'locales_path')
    all_locales = get_all_locales_except_english()
    missing_locales = []

    for api_locale in all_locales:
        android_locale = LOCALE_MAPPING.get(api_locale, api_locale)
        locale_dir = locales_path / f'values-{android_locale}'
        strings_file = locale_dir / 'strings.xml'

        if not strings_file.exists():
            missing_locales.append(api_locale)
        else:
            locale_keys = get_string_keys_from_xml(strings_file)
            if key not in locale_keys:
                missing_locales.append(api_locale)

    return missing_locales


def count_existing_translations(flavor: str, key: str) -> int:
    """
    Count how many locales have translation for a specific key.

    Args:
        flavor: Flavor name
        key: String key to check

    Returns:
        Number of locales with existing translation
    """
    from utils.locale_utils import get_all_locales_except_english

    all_locales = get_all_locales_except_english()
    missing = get_missing_locales_for_key(flavor, key)
    return len(all_locales) - len(missing)


@click.group()
@click.option('--yes', '-y', is_flag=True, help='Auto-answer yes to all prompts')
@click.pass_context
def cli(ctx, yes):
    """i18n_sync - Unified localization management system for FullRoid."""
    ctx.ensure_object(dict)
    ctx.obj['auto_yes'] = yes


@cli.command()
@click.pass_context
def init(ctx):
    """
    Initialize the localization system.

    Creates state.json and scans all strings.xml files.
    """
    auto_yes = ctx.obj['auto_yes']

    show_header("Initializing Localization System")

    state_manager = StateManager()

    # Check if already initialized
    if state_manager.state_exists():
        if not confirm_action(
            "System already initialized. Reinitialize?",
            default=False,
            auto_yes=auto_yes
        ):
            show_info("Initialization cancelled")
            return

    # Ensure directories exist
    ensure_directories()

    # Step 1: Create empty state
    show_info("Creating state structure...")
    state = state_manager.init_state()

    # Step 2: Scan and index strings for each flavor
    for flavor in FLAVOR_CONFIG.keys():
        show_info(f"Scanning {flavor} strings...")

        source_path = get_flavor_path(flavor, 'source_path')

        if not source_path.exists():
            show_warning(f"Source file not found: {source_path}")
            continue

        # Parse strings
        parser = AndroidXmlParser(source_path)
        strings = parser.parse_strings()

        show_success(f"Found {len(strings)} strings in {flavor}")

        # Calculate hashes
        hashes = calculate_strings_hashes(strings)

        # Check which strings already have FULL translations (all locales)
        show_info(f"Checking existing translations for {flavor}...")
        source_keys = set(strings.keys())

        # Count total locales
        from utils.locale_utils import get_all_locales_except_english
        total_locales = len(get_all_locales_except_english())

        # A string is fully translated only if it exists in ALL locales
        fully_translated_keys = set()
        partially_translated_keys = set()

        for key in source_keys:
            missing = get_missing_locales_for_key(flavor, key)
            if len(missing) == 0:
                fully_translated_keys.add(key)
            elif len(missing) < total_locales:
                partially_translated_keys.add(key)

        show_success(f"Fully translated: {len(fully_translated_keys)}, partially: {len(partially_translated_keys)}, no translations: {len(source_keys) - len(fully_translated_keys) - len(partially_translated_keys)}")

        # Initialize flavor in state with translation info
        state_manager.init_flavor(flavor, hashes, fully_translated_keys)

    # Step 3: Save state
    show_info("Saving state...")
    state_manager.save_state()
    show_success(f"State saved to {state_manager.state_file}")

    # Summary
    show_panel(
        f"Initialization complete!\n\n"
        f"State file: {state_manager.state_file}\n"
        f"Total strings tracked: {state_manager._state['system_info']['total_tracked_strings']}",
        title="Success",
        style="green"
    )


@cli.command()
@click.option('--flavor', type=click.Choice(list(FLAVOR_CONFIG.keys())), help='Specific flavor to sync')
@click.option('--auto-translate', is_flag=True, help='Automatically translate changes')
@click.option('--translate-missing', is_flag=True, help='Also translate strings missing in locales')
@click.option('--cleanup-obsolete', is_flag=True, help='Remove obsolete strings from locale files')
@click.pass_context
def sync(ctx, flavor, auto_translate, translate_missing, cleanup_obsolete):
    """
    Synchronize changes and detect added/modified/removed strings.

    Detects changes in strings.xml and prompts for actions interactively.
    """
    auto_yes = ctx.obj['auto_yes']

    show_header("Synchronizing Translations")

    state_manager = StateManager()

    if not state_manager.state_exists():
        show_error("System not initialized. Run 'i18n_sync.py init' first.")
        sys.exit(1)

    state_manager.load_state()

    # Determine which flavors to process
    flavors_to_process = [flavor] if flavor else list(FLAVOR_CONFIG.keys())

    for current_flavor in flavors_to_process:
        show_info(f"Processing {current_flavor}...")

        # Parse current strings
        source_path = get_flavor_path(current_flavor, 'source_path')

        if not source_path.exists():
            show_warning(f"Source file not found: {source_path}")
            continue

        parser = AndroidXmlParser(source_path)
        current_strings = parser.parse_strings()
        source_keys = set(current_strings.keys())

        # Cleanup obsolete strings from locales if requested
        if cleanup_obsolete:
            show_separator()
            show_info("Checking for obsolete strings in locale files...")

            obsolete_stats, total_obsolete = cleanup_obsolete_strings(current_flavor, source_keys)

            if total_obsolete > 0:
                show_warning(f"Found {total_obsolete} obsolete strings across {len(obsolete_stats)} locales")

                # Show preview
                for locale, info in list(obsolete_stats.items())[:3]:
                    preview_keys = info['obsolete_keys'][:5]
                    show_info(f"  {locale}: {info['obsolete_count']} strings")
                    for key in preview_keys:
                        show_info(f"    - {key}")
                    if len(info['obsolete_keys']) > 5:
                        show_info(f"    ... and {len(info['obsolete_keys']) - 5} more")

                if len(obsolete_stats) > 3:
                    show_info(f"  ... and {len(obsolete_stats) - 3} more locales")

                if auto_yes or confirm_action(
                    f"Remove {total_obsolete} obsolete strings from all locales?",
                    default=True,
                    auto_yes=auto_yes
                ):
                    locales_path = get_flavor_path(current_flavor, 'locales_path')
                    removed_total = 0

                    for locale, info in obsolete_stats.items():
                        strings_file = locales_path / f'values-{locale}' / 'strings.xml'
                        removed = remove_strings_from_xml(strings_file, set(info['obsolete_keys']))
                        removed_total += removed
                        show_info(f"  Removed {removed} strings from {locale}")

                    show_success(f"Removed {removed_total} obsolete strings from locale files")
            else:
                show_success("No obsolete strings found in locale files")

        # Get saved state
        flavor_state = state_manager.get_flavor_state(current_flavor)
        if not flavor_state:
            show_warning(f"No state found for {current_flavor}. Skipping.")
            continue

        saved_state = flavor_state['strings']

        # Detect changes
        changes = ChangeDetector.detect_changes(current_strings, saved_state)

        if not changes.has_changes():
            show_success(f"No changes detected in {current_flavor}")
            # Don't skip if we need to check for missing translations
            if not translate_missing:
                continue

        if changes.has_changes():
            show_info(f"Changes detected: {changes.summary()}")

        # Process changes interactively
        metadata_mgr = MetadataManager(current_flavor)

        try:
            metadata_mgr.load_metadata()
        except FileNotFoundError:
            show_warning(f"No metadata file for {current_flavor}. Creating empty.")
            metadata_mgr.init_empty_metadata()

        translator = AITranslator() if auto_translate else None

        # Process added strings
        for change in changes.added:
            show_separator()
            show_change('added', change.key, new_text=change.new_text)

            # Check which locales are missing translation for this key
            missing_locales = get_missing_locales_for_key(current_flavor, change.key)
            existing_count = count_existing_translations(current_flavor, change.key)

            if existing_count > 0:
                show_info(f"String '{change.key}' has {existing_count} existing translations, missing {len(missing_locales)}")

            is_fully_translated = len(missing_locales) == 0

            if confirm_action("Generate metadata?", default=True, auto_yes=auto_yes):
                # Interactive metadata collection
                category = prompt_text(
                    "Category",
                    default="general",
                    auto_yes=auto_yes,
                    auto_value="general"
                )

                max_length_str = prompt_text(
                    "Max length (or Enter for none)",
                    default="",
                    auto_yes=auto_yes,
                    auto_value=""
                )
                max_length = int(max_length_str) if max_length_str else None

                tone = prompt_choice(
                    "Tone",
                    ["friendly", "formal", "casual", "professional", "technical"],
                    default="technical",
                    auto_yes=auto_yes
                )

                # Detect format specifiers
                from core.xml_parser import detect_format_specifiers
                has_specifiers, specifiers = detect_format_specifiers(change.new_text)

                metadata = {
                    'category': category,
                    'ui_context': 'N/A',
                    'purpose': '',
                    'max_length': max_length,
                    'tone': tone,
                    'technical': {
                        'format_specifiers': has_specifiers,
                        'specifiers': specifiers,
                        'html': False,
                        'emoji': False
                    }
                }

                metadata_mgr.add_string_metadata(change.key, metadata)
                show_success("Metadata added")

            # Translate only missing locales
            if missing_locales and (auto_translate or confirm_action(
                f"Translate to {len(missing_locales)} missing languages?",
                default=True,
                auto_yes=auto_yes
            )):
                if translator:
                    try:
                        show_info(f"Translating {change.key} to {len(missing_locales)} languages...")
                        metadata = metadata_mgr.get_string_metadata(change.key)
                        result = translator.translate_and_save(
                            current_flavor,
                            change.key,
                            change.new_text,
                            metadata,
                            target_languages=missing_locales  # Only translate to missing locales
                        )
                        show_success(f"Translated to {result['saved_count']} languages")
                        is_fully_translated = True
                    except Exception as e:
                        show_error(f"Translation failed: {e}")
            elif not missing_locales:
                show_info(f"String '{change.key}' is fully translated, skipping")

            # Update state with translation status
            state_manager.add_string(
                current_flavor,
                change.key,
                change.new_hash,
                has_metadata=metadata_mgr.has_metadata(change.key),
                is_translated=is_fully_translated
            )

        # Process modified strings
        for change in changes.modified:
            show_separator()
            show_change('modified', change.key, new_text=change.new_text)

            # Modified strings need re-translation
            was_translated = False
            if auto_translate or confirm_action(
                "Re-translate?",
                default=True,
                auto_yes=auto_yes
            ):
                if translator or not auto_translate:
                    if not translator:
                        translator = AITranslator()

                    try:
                        show_info(f"Translating {change.key}...")
                        metadata = metadata_mgr.get_string_metadata(change.key)
                        result = translator.translate_and_save(
                            current_flavor,
                            change.key,
                            change.new_text,
                            metadata
                        )
                        show_success(f"Translated to {result['saved_count']} languages")
                        was_translated = True
                    except Exception as e:
                        show_error(f"Translation failed: {e}")

            # Update state with translation status
            state_manager.update_string_hash(
                current_flavor,
                change.key,
                change.new_hash,
                has_metadata=metadata_mgr.has_metadata(change.key),
                is_translated=was_translated
            )

        # Process removed strings
        for change in changes.removed:
            show_separator()
            show_change('removed', change.key)

            if confirm_action("Delete metadata?", default=True, auto_yes=auto_yes):
                if metadata_mgr.remove_string_metadata(change.key):
                    show_success("Metadata removed")

            # Remove from state
            state_manager.remove_string(current_flavor, change.key)

        # Check for missing translations if requested
        if translate_missing:
            show_separator()
            show_info("Checking for missing translations...")

            missing_keys = find_missing_translations(current_flavor, current_strings)

            if missing_keys:
                show_warning(f"Found {len(missing_keys)} strings without translations")

                for i, key in enumerate(sorted(missing_keys)[:3]):
                    text = current_strings.get(key, '')
                    preview = text[:50] + '...' if text and len(text) > 50 else text
                    show_info(f"  {key}: {preview}")

                if len(missing_keys) > 3:
                    show_info(f"  ... and {len(missing_keys) - 3} more")

                if auto_translate or confirm_action(
                    f"Translate {len(missing_keys)} missing strings?",
                    default=True,
                    auto_yes=auto_yes
                ):
                    if not translator:
                        translator = AITranslator()

                    success_count = 0
                    for i, key in enumerate(sorted(missing_keys), 1):
                        show_info(f"[{i}/{len(missing_keys)}] Translating {key}...")
                        try:
                            metadata = metadata_mgr.get_string_metadata(key) if metadata_mgr.has_metadata(key) else None
                            translator.translate_and_save(current_flavor, key, current_strings[key], metadata)
                            success_count += 1
                            # Mark as translated in state
                            state_manager.mark_as_translated(current_flavor, key)
                        except Exception as e:
                            show_error(f"Failed: {e}")

                    show_success(f"Translated {success_count}/{len(missing_keys)} missing strings")
            else:
                show_success("No missing translations found")

        # Save metadata and state
        metadata_mgr.save_metadata()
        state_manager.save_state()

        show_success(f"Sync complete for {current_flavor}")

    show_panel("Synchronization complete!", style="green")


@cli.command()
@click.option('--flavor', required=True, type=click.Choice(list(FLAVOR_CONFIG.keys())))
@click.option('--key', required=True, help='String key to translate')
@click.option('--all-languages', is_flag=True, default=True, help='Translate to all languages')
@click.pass_context
def translate(ctx, flavor, key, all_languages):
    """Translate a specific string."""
    show_header(f"Translating {key}")

    # Get string text
    source_path = get_flavor_path(flavor, 'source_path')
    parser = AndroidXmlParser(source_path)
    strings = parser.parse_strings()

    if key not in strings:
        show_error(f"String not found: {key}")
        sys.exit(1)

    text = strings[key]

    # Get metadata
    metadata_mgr = MetadataManager(flavor)
    try:
        metadata_mgr.load_metadata()
        metadata = metadata_mgr.get_string_metadata(key)
    except:
        metadata = None

    # Translate
    translator = AITranslator()
    try:
        result = translator.translate_and_save(flavor, key, text, metadata)
        show_success(f"Translated to {result['saved_count']} languages")
    except Exception as e:
        show_error(f"Translation failed: {e}")
        sys.exit(1)


@cli.command()
@click.option('--flavor', type=click.Choice(list(FLAVOR_CONFIG.keys())), help='Specific flavor')
def stats(flavor):
    """Show statistics."""
    show_header("Statistics")

    state_manager = StateManager()

    if not state_manager.state_exists():
        show_error("System not initialized.")
        sys.exit(1)

    state_manager.load_state()

    flavors_to_show = [flavor] if flavor else list(FLAVOR_CONFIG.keys())

    data = []
    for f in flavors_to_show:
        flavor_state = state_manager.get_flavor_state(f)
        if flavor_state:
            data.append({
                'Flavor': flavor_state['display_name'],
                'Strings': str(flavor_state['string_count']),
                'Last Scan': flavor_state.get('last_scan', 'N/A')[:10]
            })

    show_summary_table("Flavors", data)


def main():
    """Main entry point."""
    cli(obj={})


if __name__ == '__main__':
    main()
