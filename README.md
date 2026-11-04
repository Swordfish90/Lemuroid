# OMINOY

<p align="center"><img src="docs/ominoy-icon.png" alt="OMINOY app icon" width="160"></p>

## Description

OMINOY is an open-source Android emulation app built on Libretro. It focuses exclusively on Nintendo Game Boy, Game Boy Color, and Game Boy Advance; other systems are not supported.

It originated from a branch of [Retrograde](https://github.com/retrograde/retrograde-android) and integrates [LibretroDroid](https://github.com/Swordfish90/LibretroDroid).

## Supported systems

- Game Boy (GB) — mGBA
- Game Boy Color (GBC) — mGBA
- Game Boy Advance (GBA) — mGBA

## Features

- ROM scanning and library indexing, with in-app search and favorites
- ZIP-compressed ROM support
- Automatic save-RAM and save-state handling, plus quick save/load slots
- Per-game cheat-code management for supported systems and code types
- Custom per-game thumbnails: choose artwork from the Android gallery or file picker, then change or remove it later
- Customizable on-screen controls, gamepad input, tilt input, and local multiplayer
- Screen filters, HD display options, LCD/CRT simulation, and viewport alignment (center, top, bottom, or custom)
- Android phone and Android TV support
- Per-game playtime, session-count, and last-played information
- An in-app link to OMINOY's GitHub updates

### Custom thumbnails

Long-press a game tile and choose **Set custom thumbnail** to select artwork from the Android gallery or file picker. OMINOY center-crops the image to a square, resizes it to a maximum of 512 × 512 pixels, and stores a private app copy. Use **Change thumbnail** or **Remove thumbnail** from the same context menu to manage existing artwork. Custom thumbnails persist across restarts and library rescans; they are not embedded in ROM files and are removed when the app is uninstalled.

### Reliability

- ZIP extraction rejects absolute and traversal paths and applies entry-count, output-size, per-entry-size, and compression-ratio limits.
- Core assets are extracted into a validated temporary directory before installation.
- Save, state, preview, and cache filenames are sanitized; applicable file writes are atomic.
- Library cleanup runs only after a complete successful provider scan, preserving existing entries when a provider fails or becomes unavailable.

## Building

Requirements: JDK 21, Android SDK Platform 35, Android Build Tools 34.0.0, and the repository submodules.

To build the Free Dynamic debug APK, create a throwaway debug-only keystore (do not use it for a production release) and run:

```bash
keytool -genkeypair -noprompt -keystore debug.keystore \
  -storepass android -alias androiddebugkey -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
./gradlew assembleFreeDynamicDebug
```

The debug APK uses a `.debug` application ID suffix and installs separately from a production release. In the Free Dynamic build, mGBA is downloaded on demand when first needed, so internet access may be required. For a release build, create an ignored `keystore.properties` file with `storeFile`, `keyAlias`, `storePassword`, and `keyPassword` entries. Never commit a production keystore or its password.

## Languages

Translations are managed through [Crowdin](https://crowdin.com/project/lemuroid).
