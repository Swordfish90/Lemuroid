#!/usr/bin/env python3
"""
Patch ELF shared library for 16KB page size alignment.

Required for Android 15+ and Google Play Store compliance (November 2025+).

Usage:
    python3 patch_elf_16kb.py <input.so> <output.so>
    python3 patch_elf_16kb.py <input.so>  # patches in-place

The script adds padding between LOAD segments to ensure:
    (p_offset % p_align) == (p_vaddr % p_align)

This is required for the library to load correctly on devices with 16KB page size.
"""

import struct
import os
import sys
import shutil


def align_up(value, alignment):
    """Round up value to the next multiple of alignment."""
    return (value + alignment - 1) & ~(alignment - 1)


def check_alignment(input_path, align=0x4000):
    """Check if ELF file has correct alignment for given page size."""
    with open(input_path, 'rb') as f:
        data = f.read()

    if data[:4] != b'\x7fELF':
        print(f"Error: {input_path} is not an ELF file")
        return False

    is_64bit = data[4] == 2
    if not is_64bit:
        print(f"Error: Only 64-bit ELF files are supported")
        return False

    e_phoff = struct.unpack_from('<Q', data, 32)[0]
    e_phentsize = struct.unpack_from('<H', data, 54)[0]
    e_phnum = struct.unpack_from('<H', data, 56)[0]

    PT_LOAD = 1
    all_ok = True

    for i in range(e_phnum):
        ph_off = e_phoff + i * e_phentsize
        p_type = struct.unpack_from('<I', data, ph_off)[0]

        if p_type == PT_LOAD:
            p_offset = struct.unpack_from('<Q', data, ph_off + 8)[0]
            p_vaddr = struct.unpack_from('<Q', data, ph_off + 16)[0]
            p_align = struct.unpack_from('<Q', data, ph_off + 48)[0]

            offset_mod = p_offset % align
            vaddr_mod = p_vaddr % align

            if p_align < align:
                print(f"  LOAD segment: align={hex(p_align)} < {hex(align)} - needs patching")
                all_ok = False
            elif offset_mod != vaddr_mod:
                print(f"  LOAD segment: offset%align={hex(offset_mod)} != vaddr%align={hex(vaddr_mod)} - needs patching")
                all_ok = False

    return all_ok


def patch_elf_16kb(input_path, output_path, new_align=0x4000):
    """
    Rebuild ELF with proper 16KB alignment by adding padding between LOAD segments.

    Args:
        input_path: Path to input .so file
        output_path: Path to output .so file
        new_align: New alignment value (default 0x4000 = 16KB)

    Returns:
        True if successful, False otherwise
    """

    with open(input_path, 'rb') as f:
        original_data = f.read()

    # Verify ELF magic
    if original_data[:4] != b'\x7fELF':
        print(f"Error: {input_path} is not an ELF file")
        return False

    # Check 64-bit
    is_64bit = original_data[4] == 2
    if not is_64bit:
        print(f"Error: Only 64-bit ELF files are supported")
        return False

    data = bytearray(original_data)

    # Parse ELF header
    e_phoff = struct.unpack_from('<Q', data, 32)[0]
    e_shoff = struct.unpack_from('<Q', data, 40)[0]
    e_phentsize = struct.unpack_from('<H', data, 54)[0]
    e_phnum = struct.unpack_from('<H', data, 56)[0]

    # Parse all program headers
    PT_LOAD = 1
    phdrs = []

    for i in range(e_phnum):
        ph_off = e_phoff + i * e_phentsize
        phdr = {
            'idx': i,
            'ph_off': ph_off,
            'p_type': struct.unpack_from('<I', data, ph_off)[0],
            'p_flags': struct.unpack_from('<I', data, ph_off + 4)[0],
            'p_offset': struct.unpack_from('<Q', data, ph_off + 8)[0],
            'p_vaddr': struct.unpack_from('<Q', data, ph_off + 16)[0],
            'p_paddr': struct.unpack_from('<Q', data, ph_off + 24)[0],
            'p_filesz': struct.unpack_from('<Q', data, ph_off + 32)[0],
            'p_memsz': struct.unpack_from('<Q', data, ph_off + 40)[0],
            'p_align': struct.unpack_from('<Q', data, ph_off + 48)[0],
        }
        phdrs.append(phdr)

    # Find and sort LOAD segments by offset
    load_phdrs = [p for p in phdrs if p['p_type'] == PT_LOAD]
    load_phdrs.sort(key=lambda x: x['p_offset'])

    if not load_phdrs:
        print("Error: No LOAD segments found")
        return False

    # Check if patching is needed
    needs_patching = False
    for load in load_phdrs:
        if load['p_align'] < new_align:
            needs_patching = True
            break
        offset_mod = load['p_offset'] % new_align
        vaddr_mod = load['p_vaddr'] % new_align
        if offset_mod != vaddr_mod:
            needs_patching = True
            break

    if not needs_patching:
        print(f"File already has correct {new_align // 1024}KB alignment")
        if input_path != output_path:
            shutil.copy(input_path, output_path)
        return True

    # Build new file with proper alignment
    output = bytearray()

    # Copy first LOAD segment as-is (contains ELF header and program headers)
    first_load = load_phdrs[0]
    first_end = first_load['p_offset'] + first_load['p_filesz']
    output.extend(original_data[:first_end])

    # Update first LOAD's p_align in output
    struct.pack_into('<Q', output, first_load['ph_off'] + 48, new_align)

    current_file_offset = first_end
    total_padding = 0

    # Process remaining LOAD segments
    for load in load_phdrs[1:]:
        # Calculate required file offset for correct alignment
        # Rule: (p_offset % p_align) == (p_vaddr % p_align)
        vaddr_mod = load['p_vaddr'] % new_align

        # Find next file offset that satisfies alignment requirement
        new_offset = current_file_offset
        current_mod = new_offset % new_align

        if current_mod != vaddr_mod:
            if vaddr_mod > current_mod:
                padding = vaddr_mod - current_mod
            else:
                padding = new_align - current_mod + vaddr_mod
            new_offset += padding
        else:
            padding = 0

        # Add padding
        if padding > 0:
            output.extend(b'\x00' * padding)
            total_padding += padding

        # Copy segment data
        seg_data = original_data[load['p_offset']:load['p_offset'] + load['p_filesz']]
        output.extend(seg_data)

        # Update program header in output
        struct.pack_into('<Q', output, load['ph_off'] + 8, new_offset)  # p_offset
        struct.pack_into('<Q', output, load['ph_off'] + 48, new_align)  # p_align

        print(f"  LOAD: vaddr={hex(load['p_vaddr'])}, offset {hex(load['p_offset'])} -> {hex(new_offset)} (+{padding} bytes padding)")

        current_file_offset = new_offset + load['p_filesz']

    # Copy data after last LOAD segment (section headers, etc.)
    last_load = load_phdrs[-1]
    after_loads = last_load['p_offset'] + last_load['p_filesz']

    remaining = original_data[after_loads:]
    if remaining:
        # Align to 8 bytes for section headers
        padding = align_up(current_file_offset, 8) - current_file_offset
        if padding > 0:
            output.extend(b'\x00' * padding)
            current_file_offset += padding

        # Update section header offset if it was after LOAD segments
        if e_shoff >= after_loads:
            new_shoff = current_file_offset + (e_shoff - after_loads)
            struct.pack_into('<Q', output, 40, new_shoff)

        output.extend(remaining)

    # Write output file
    with open(output_path, 'wb') as f:
        f.write(output)
    os.chmod(output_path, 0o755)

    print(f"\nOriginal: {len(original_data):,} bytes")
    print(f"Patched:  {len(output):,} bytes (+{total_padding:,} bytes padding)")
    print(f"Output:   {output_path}")

    return True


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        print("\nExamples:")
        print("  # Check if library needs patching:")
        print("  python3 patch_elf_16kb.py --check libfoo.so")
        print("")
        print("  # Patch library (create new file):")
        print("  python3 patch_elf_16kb.py libfoo.so libfoo_patched.so")
        print("")
        print("  # Patch library in-place:")
        print("  python3 patch_elf_16kb.py libfoo.so")
        sys.exit(1)

    if sys.argv[1] == '--check':
        if len(sys.argv) < 3:
            print("Error: --check requires a file path")
            sys.exit(1)
        input_path = sys.argv[2]
        print(f"Checking {input_path}...")
        if check_alignment(input_path):
            print("✅ File has correct 16KB alignment")
            sys.exit(0)
        else:
            print("❌ File needs patching for 16KB alignment")
            sys.exit(1)

    input_path = sys.argv[1]
    output_path = sys.argv[2] if len(sys.argv) > 2 else input_path

    if not os.path.exists(input_path):
        print(f"Error: File not found: {input_path}")
        sys.exit(1)

    print(f"Patching {input_path} for 16KB page size alignment...")

    if patch_elf_16kb(input_path, output_path):
        print("\n✅ Success!")
        sys.exit(0)
    else:
        print("\n❌ Failed!")
        sys.exit(1)


if __name__ == '__main__':
    main()
