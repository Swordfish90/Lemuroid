# PRD: Bug Fixes for the GB / GBC / GBA App

**Date:** 31 Oct 2026  
**Owner:** @Asg  
**Status:** Decisions confirmed; ready for implementation planning

## 1. Background

The app is intentionally being focused on Game Boy, Game Boy Color, and Game Boy Advance only. Several screens still expose items for other systems, and the cheats screen needs stronger, system-specific input support. The findings are based on the uploaded source tree and the screenshot of the current Add cheat dialog.

## 2. Scope

This PRD covers three fixes:

| # | Screen | Problem |
|---|---|---|
| 1 | Settings → BIOS | The list includes BIOS entries for other systems and omits the four supported GB-family BIOS files. |
| 2 | Settings → touch layout editor | The system dropdown lists every touch-controller layout; GBC has no separate layout. |
| 3 | In-game menu → Cheats | The dialog has no code-type selector, validation is generic, editing is hard to discover, and core errors are not surfaced. |

**Not in scope:** the library, save-folder layout, mGBA options screen, Android TV, cloud sync, or removing core-download code. Keep all changes in this PRD focused on GB, GBC, and GBA; do not restore support or UI for other systems.

## 3. Fix 1: BIOS screen

### Problem

The current BIOS catalog contains entries for systems such as PlayStation, Atari Lynx, Sega CD, and Nintendo DS, and does not list the four required Game Boy-family entries.

### Required behavior

The screen shows exactly these four optional BIOS files and no others:

| File name | Description | Used for | Expected MD5 |
|---|---|---|---|
| `gba_bios.bin` | Game Boy Advance BIOS | GBA games | `a860e8c0b6d573d191e4ec7db1b1e4f6` |
| `gb_bios.bin` | Game Boy BIOS | GB games | `32fbbd84168d3482956eb3c5051637f5` |
| `gbc_bios.bin` | Game Boy Color BIOS | GBC games | `dbfce9db9deaa2567f6a84fde55f9680` |
| `sgb_bios.bin` | Super Game Boy BIOS | Super Game Boy games | `d574d4f9c12f305074798f54c091a8b4` |

- All four files are optional. Games must run without them, and GB/GBC/GBA must not show a blocking missing-BIOS error.
- Keep the **Detected** and **Not detected** groups. A BIOS counts as **Detected** only when both its expected filename and MD5 match.
- If the expected filename exists but its MD5 is wrong, show a non-blocking warning: **“Found, checksum does not match”**. It is not counted as detected.
- Import also recognizes a correctly hashed BIOS under a different input filename and installs it under the expected mGBA filename.
- Include a short explanatory note: these files are used only when mGBA’s **Use BIOS file if found** behavior is enabled (on by default); **Skip BIOS intro** also requires a BIOS.
- Remove all other systems’ BIOS entries from the catalog and UI. Do not bundle or redistribute any BIOS files.
- Testers place fixtures in the app’s internal system folder (`files/system/`) using a test/debug installation method.

### Files to change

`BiosManager.kt`, `Bios.kt`, the BIOS settings screen and view model, plus any import/hash helper needed to validate MD5. Update relevant strings and translations.

### Acceptance criteria

- Exactly four rows appear across Detected and Not detected.
- A file with the expected name and correct MD5 moves to Detected.
- A file with the expected name and incorrect MD5 produces the non-blocking checksum warning and is not detected.
- A correctly hashed file supplied under a different name is imported and recognized under the expected mGBA filename.
- No PlayStation, Lynx, Sega CD, DS, or other non-GB-family BIOS entries appear on the screen.
- Missing optional BIOS files never prevent GB, GBC, or GBA games from launching.

## 4. Fix 2: Touch layout editor

### Problem

The dropdown currently iterates every `TouchControllerID`, so it includes layouts for NES, SNES, Genesis, N64, PlayStation, PSP, DS, and other systems. GB and GBA have layouts; GBC currently shares the GB controller ID.

### Required behavior

- The editor dropdown shows exactly **Game Boy**, **Game Boy Color**, and **Game Boy Advance**.
- Add a distinct GBC controller ID and controller configuration, and map the GBC system to it so gameplay and the editor use the same GBC layout.
- GBC starts from the same default control configuration as GB, but has its own preference key and independently persisted layout. Do not copy a user’s custom GB settings into GBC.
- Preserve the existing GB and GBA preference keys so their previously saved layouts continue to load.
- Remove other systems’ touch layouts only after references have been removed and the project still compiles. Saved settings for removed systems are ignored.
- Move, resize, live preview, Save, and Reset continue to work in portrait and landscape. Reset applies only to the selected system and orientation.

### Files to change

`TouchLayoutEditorScreen.kt`, `TouchControllerID.kt`, relevant touch-layout files, and the controller configuration/system mapping (including `ControllerConfigs.kt` and the GBC mapping in `GameSystem.kt`). Update labels and translations as needed.

### Acceptance criteria

- The dropdown shows exactly three entries and no others.
- GBC gameplay and the editor use the distinct GBC preference key and layout, with GB-equivalent defaults.
- A layout saved before the update still appears for GB and GBA.
- GBC edits do not alter GB settings.
- Move, resize, preview, Save, and Reset work in portrait and landscape; Reset affects only the selected system and orientation.
- The app builds after unused layouts are removed.

## 5. Fix 3: Cheats

### Problem

The Add cheat dialog has only Name and Cheat code. A generic validator accepts code groups of 4 or 8 hex digits regardless of system or code type. Editing exists but is difficult to discover, and core-side failures are currently only logged.

### 5.1 Code-type dropdown

Place the dropdown between Name and Cheat code.

- **GB and GBC:** GameShark and Game Genie.
- **GBA:** GameShark, Action Replay, and CodeBreaker.
- Remember the last selected type separately for each system. The first time, default to the first available option (GameShark).
- There is no blank option for new cheats. Save remains disabled until a type is selected and the code passes that type’s validation.
- Changing the type immediately revalidates the code and updates the example/help line.
- Show the selected type as a small tag beside the cheat name.
- Type is a label and a validation choice only. It is not sent to the core and does not force the core to interpret the code as that type. Help text must say this clearly; the core receives only the code text.

### 5.2 Planned validation rules

| Type | System | Shape of each code line | Example shape |
|---|---|---|---|
| GameShark | GB/GBC | 8 hexadecimal digits | `XXXXXXXX` |
| Game Genie | GB/GBC | 6 or 9 hexadecimal digits; optional dashes in the conventional grouping | `XXX-XXX` or `XXX-XXX-XXX` |
| GameShark | GBA | 8 hexadecimal digits followed by 8 hexadecimal digits | `XXXXXXXX YYYYYYYY` |
| Action Replay | GBA | 8 hexadecimal digits followed by 8 hexadecimal digits | `XXXXXXXX YYYYYYYY` |
| CodeBreaker | GBA | 8 hexadecimal digits followed by 4 hexadecimal digits | `32028642 00FE` |

- Spaces, commas, plus signs, and line breaks separate groups. For Game Genie only, remove dashes before validating.
- Ignore blank lines. A cheat has at most **8 non-empty lines**, with at most **24 characters per trimmed line**.
- Normalize saved letters to uppercase. Compact pasted codes may be split into groups according to the selected type.
- Invalid input shows a red message naming the selected type and an example. It cannot be saved or sent to the core.
- Validation is a format check, not proof that a code is supported or will work. Confirm the planned patterns with real codes and real-game tests.

### 5.3 Edit cheat

- Tapping a cheat card or its edit button opens the same dialog populated with the saved name, type, and code.
- The user can edit the name, type, and code. Editing does not change the enabled/disabled state.
- Changing type revalidates immediately; Save is disabled for invalid input.
- Cancel or Back discards changes. Save updates the card and returns to the list.
- Changes take effect when the user returns to the game.

### 5.4 Existing cheats and other behavior

- Existing cheats from earlier versions remain loadable and continue to use the existing application path. Their new nullable type displays as **Type not set**.
- The user must pick a type before saving edits to an older cheat.
- Support multi-line codes, one code line per row. Show a hint that some GBA codes need an enable code on the first line.
- **Auto-detect is not part of v1.** Reconsider only if testing shows it is needed.

### 5.5 Data and code changes

- Add nullable `codeType` to `GameCheat.kt`.
- Add a Room migration from version 11 to 12 in `Migrations.kt`; register it in `LemuroidApplicationModule.kt`; set `RetrogradeDatabase.kt` to version 12; update the exported Room schema. Preserve every existing cheat row, with `codeType = null` for existing rows.
- Replace or extend `CheatCodeFormatter.kt` with system- and type-specific validation/normalization.
- Update `GameCheatsScreen.kt` with the dropdown, type tag, help, validation messages, and edit behavior.
- Add/update default and translated string resources.

### 5.6 Optional follow-up items (not required for v1 acceptance)

- Delete confirmation with a **Cheat deleted / Undo** message for five seconds.
- A user-facing warning only if the core actually reports that it cannot use a code. If the core provides no signal, do not show a speculative warning.
- Name limit (40 characters) and duplicate-code warning (allowed after warning).

### 5.7 Acceptance criteria

- A tested working code of each supported type can be added, enabled, and verified in a real game.
- A wrongly formatted code cannot be saved and shows an error naming the selected type.
- The type list contains only the options for the active game system.
- Tapping a card or its edit button opens the populated editor; edits preserve the enabled state and are applied after returning to the game.
- Existing cheats survive the database upgrade and still load; their type displays as **Type not set**.
- Cheats over the 8-line or 24-character-per-trimmed-line limit are rejected with a clear message.

## 6. Test plan

1. Add and test at least one working code of every supported type in a real game.
2. Enter a wrong-format code for each type and confirm it is rejected with the appropriate message.
3. Test whitespace, commas, plus signs, line breaks, compact forms, and Game Genie dashes; test 8/9-line and 24/25-character boundaries.
4. Edit name, type, and code; verify enabled state is unchanged and behavior updates after returning to the game. Cancel and Back must discard edits.
5. Upgrade a database with existing cheats and confirm they remain and still load with **Type not set**.
6. Test BIOS detection for correct name+MD5, wrong MD5, and correct MD5 under an alternate filename.
7. Test GB, GBC, and GBA touch layouts in both orientations, including existing GB/GBA settings, isolated GBC edits, and per-system reset.

## 7. Risks and open questions

- **Core interpretation:** mGBA receives code text but not the UI’s selected type. Reported support for encrypted GameShark Advance, decrypted CodeBreaker, and decrypted Action Replay MAX codes is not sufficient evidence for every format. Validate with real codes. The selected type tag must not imply that the core was instructed to use that type.
- **Game Genie punctuation:** dashes are removed before format validation; verify the normalized text accepted by the active mGBA core with actual codes.
- **BIOS behavior:** confirm the four supplied names/MD5s and the default mGBA BIOS behavior against the intended mGBA documentation/core version before release.
- **GBC layout:** decision confirmed: it has its own saved layout with GB defaults.
- **Auto-detect:** excluded from v1 unless test results show a need.
