# OMINOY

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/come.asg.lemudroid/)
[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=come.asg.lemudroid)

## Description

OMINOY is an open-source Android emulation project based on Libretro, focused exclusively on Nintendo Game Boy, Game Boy Color, and Game Boy Advance. Its goal is to combine easy setup, Android integration, and a polished play experience.

It originated from a branch of [Retrograde](https://github.com/retrograde/retrograde-android) and integrates [LibretroDroid](https://github.com/Swordfish90/LibretroDroid).

### Supported systems

- Game Boy (GB) — mGBA
- Game Boy Color (GBC) — mGBA
- Game Boy Advance (GBA) — mGBA

### Features

- Android phone and Android TV support
- Automatic save-RAM and save-state handling
- ROM scanning and library indexing
- Optimized, customizable touch controls
- Quick save/load slots
- ZIP-compressed ROM support
- LCD/CRT display simulation
- Gamepad and local multiplayer support
- Tilt input
- Cloud save sync in supported builds
- Custom per-game thumbnails for ROM hacks and homebrew

### Custom thumbnails

Assign local artwork to individual games that do not have cover art in the Libretro thumbnail database. Long-press a game tile and choose **Set custom thumbnail**; choose an image from the Android gallery or file picker. OMINOY center-crops the image to a square, resizes it to a maximum of 512 × 512 pixels, and stores a private app copy. For existing artwork, choose **Change thumbnail** or **Remove thumbnail** from the same context menu.

Custom thumbnails are stored as app-side metadata and persist across restarts and library rescans. They are not embedded in ROM files or uploaded for cloud sync, and they are removed when the app is uninstalled.

### Security and reliability

- ZIP extraction rejects absolute and traversal paths and applies entry-count, output-size, per-entry-size, and compression-ratio limits.
- Core assets are extracted into a validated temporary directory before installation.
- Downloaded core files, extracted data, and synchronized files use atomic writes where applicable.
- Save, state, preview, and cache filenames are sanitized; cache locations include a SHA-256 source identity derived from the URI, filename, and system.
- Library cleanup runs only after a complete successful provider scan, preserving existing entries when a provider fails or becomes unavailable.
- Google Drive relative paths are validated before local file construction, and Room multi-instance invalidation is enabled for the shared database.

### Current OMINOY build

This repository contains the OMINOY Android build with the following available functionality:

- GB, GBC, and GBA emulation through the mGBA Libretro core
- ROM library scanning, indexing, ZIP support, and per-game thumbnails
- Save RAM, save states, quick save/load slots, and safe library cleanup
- Customizable touch controls, gamepad input, tilt input, and local multiplayer
- Android phone and Android TV support with LCD/CRT display simulation
- Optional cloud-save synchronization in supported builds
- Hardened ZIP/core extraction, filename sanitization, atomic writes, and validated cloud paths

The release APK is published from the GitHub Releases page for this repository.

### Building

Requirements: JDK 21, Android SDK Platform 35, Android Build Tools 34.0.0, and the repository submodules.

The project expects a root-level debug keystore for local debug builds. Generate a throwaway debug-only key (do not use it for production signing), then build the Free Dynamic debug APK:

```bash
keytool -genkeypair -noprompt -keystore debug.keystore \
  -storepass android -alias androiddebugkey -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
./gradlew assembleFreeDynamicDebug
```

The debug APK uses a `.debug` application ID suffix and installs separately from a production release. Free Dynamic keeps the APK small; mGBA is downloaded on demand when first needed, so internet access may be required. For a release build, create an ignored `keystore.properties` file with `storeFile`, `keyAlias`, `storePassword`, and `keyPassword` entries. Never commit a production keystore or its password.

### Languages

Translations are managed through [Crowdin](https://crowdin.com/project/lemuroid).
