#!/usr/bin/env python3
"""
fix_newlines.py - Fix newlines in all locale strings.xml files.

Replaces real newlines with escaped \n in Android string resources.
Only modifies strings that have real newlines but no escaped \n.
"""

import sys
import re
from pathlib import Path

# Add parent directory to path for imports
sys.path.insert(0, str(Path(__file__).parent))

from config import FLAVOR_CONFIG, get_flavor_path


def fix_newlines_in_file(file_path: Path, dry_run: bool = False) -> dict:
    """
    Fix newlines in a single strings.xml file.

    Args:
        file_path: Path to strings.xml
        dry_run: If True, don't write changes, just report

    Returns:
        Dict with statistics: {'fixed': int, 'strings': list}
    """
    if not file_path.exists():
        return {'fixed': 0, 'strings': []}

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern to match string elements
    # Captures: name attribute and content
    pattern = r'(<string\s+name="([^"]+)"[^>]*>)(.*?)(</string>)'

    fixed_count = 0
    fixed_strings = []

    def replace_newlines(match):
        nonlocal fixed_count, fixed_strings

        opening_tag = match.group(1)
        name = match.group(2)
        text = match.group(3)
        closing_tag = match.group(4)

        # Check if text has real newlines but no escaped \n
        has_real_newlines = '\n' in text or '\r' in text
        has_escaped_newlines = '\\n' in text

        if has_real_newlines and not has_escaped_newlines:
            # Fix the newlines
            new_text = text.replace('\r\n', '\\n')
            new_text = new_text.replace('\n', '\\n')
            new_text = new_text.replace('\r', '\\n')

            fixed_count += 1
            fixed_strings.append(name)

            return f'{opening_tag}{new_text}{closing_tag}'

        return match.group(0)

    new_content = re.sub(pattern, replace_newlines, content, flags=re.DOTALL)

    if fixed_count > 0 and not dry_run:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)

    return {'fixed': fixed_count, 'strings': fixed_strings}


def fix_all_locales(dry_run: bool = False) -> dict:
    """
    Fix newlines in all locale files for all flavors.

    Args:
        dry_run: If True, don't write changes, just report

    Returns:
        Dict with statistics per flavor and locale
    """
    stats = {}
    total_fixed = 0

    for flavor_name, flavor_config in FLAVOR_CONFIG.items():
        print(f"\nProcessing {flavor_name}...")
        stats[flavor_name] = {}

        locales_path = get_flavor_path(flavor_name, 'locales_path')

        if not locales_path.exists():
            print(f"  Locales path not found: {locales_path}")
            continue

        # Process all values-* directories
        for item in sorted(locales_path.iterdir()):
            if item.is_dir() and item.name.startswith('values'):
                strings_file = item / 'strings.xml'

                if strings_file.exists():
                    result = fix_newlines_in_file(strings_file, dry_run)

                    if result['fixed'] > 0:
                        locale = item.name.replace('values-', '') or 'default'
                        stats[flavor_name][locale] = result
                        total_fixed += result['fixed']

                        action = "Would fix" if dry_run else "Fixed"
                        print(f"  {action} {result['fixed']} strings in {item.name}/strings.xml")
                        for s in result['strings'][:3]:
                            print(f"    - {s}")
                        if len(result['strings']) > 3:
                            print(f"    ... and {len(result['strings']) - 3} more")

    return {'stats': stats, 'total_fixed': total_fixed}


def main():
    import argparse

    parser = argparse.ArgumentParser(
        description='Fix newlines in Android strings.xml files'
    )
    parser.add_argument(
        '--dry-run', '-n',
        action='store_true',
        help='Show what would be fixed without making changes'
    )
    parser.add_argument(
        '--flavor', '-f',
        choices=list(FLAVOR_CONFIG.keys()),
        help='Only process specific flavor'
    )

    args = parser.parse_args()

    print("=" * 60)
    print("Fix Newlines in Android Strings")
    print("=" * 60)

    if args.dry_run:
        print("\n[DRY RUN MODE - No changes will be made]\n")

    if args.flavor:
        # Process single flavor
        print(f"Processing flavor: {args.flavor}")
        locales_path = get_flavor_path(args.flavor, 'locales_path')
        total_fixed = 0

        for item in sorted(locales_path.iterdir()):
            if item.is_dir() and item.name.startswith('values'):
                strings_file = item / 'strings.xml'
                if strings_file.exists():
                    result = fix_newlines_in_file(strings_file, args.dry_run)
                    if result['fixed'] > 0:
                        total_fixed += result['fixed']
                        action = "Would fix" if args.dry_run else "Fixed"
                        print(f"  {action} {result['fixed']} strings in {item.name}/strings.xml")
                        for s in result['strings']:
                            print(f"    - {s}")

        print(f"\nTotal: {total_fixed} strings {'would be ' if args.dry_run else ''}fixed")
    else:
        # Process all flavors
        result = fix_all_locales(args.dry_run)
        print(f"\n{'=' * 60}")
        print(f"Total: {result['total_fixed']} strings {'would be ' if args.dry_run else ''}fixed")

    if not args.dry_run and result.get('total_fixed', 0) > 0:
        print("\nDone! Changes have been written to files.")


if __name__ == '__main__':
    main()
