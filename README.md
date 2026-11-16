<div align="center">
<img src="docs/ominoy-icon.png" alt="OMINOY app icon" width="160">

# OMINOY

**A focused, open-source Game Boy emulator for Android.**
GB · GBC · GBA — powered by Libretro and mGBA.

![Platform](https://img.shields.io/badge/platform-Android%20%7C%20Android%20TV-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Libretro](https://img.shields.io/badge/built%20on-Libretro-7B61FF?style=for-the-badge)
![Core](https://img.shields.io/badge/core-mGBA-E74C3C?style=for-the-badge)
![JDK](https://img.shields.io/badge/JDK-21-F89820?style=for-the-badge&logo=openjdk&logoColor=white)

[Getting started](#-getting-started) · [How features work](#-how-features-work) · [RetroAchievements](#-retroachievements) · [Screenshots](#-screenshots) · [Building](#-building)
</div>

---

## About OMINOY

OMINOY is an Android emulator for Nintendo **Game Boy**, **Game Boy Color**, and **Game Boy Advance** games. It is designed to keep the experience simple: choose a ROM folder, let OMINOY build your library, and launch a game from the home screen.

OMINOY does not include copyrighted games or ROM files. Use ROMs that you are legally allowed to use.

## Supported systems

| System | Emulator core |
| :-- | :-- |
| Game Boy (GB) | mGBA |
| Game Boy Color (GBC) | mGBA |
| Game Boy Advance (GBA) | mGBA |

## ✅ Feature status

### Completed

- [x] Game Boy, Game Boy Color, and Game Boy Advance emulation through mGBA
- [x] ROM folder selection, library scanning, indexing, rescanning, and ZIP-compressed ROM support
- [x] Library search, recent games, favorites, custom game names, custom artwork, and launcher shortcuts
- [x] Per-game playtime, session count, and last-played information
- [x] In-game save data, save states, quick save/load, and optional exit autosave
- [x] Configurable save and OMINOY data folders
- [x] Cloud save backup and sync, including manual sync and optional Wi-Fi auto-sync
- [x] Cheats stored and managed separately for each game
- [x] Customizable on-screen controls with layout, size, opacity, and sensitivity settings
- [x] External gamepad and keyboard input with configurable bindings
- [x] Tilt-sensor input, rumble, and local multiplayer input support
- [x] Fast-forward, mute, immersive mode, AMOLED theme, and low-latency audio option
- [x] Display filters, HD upscaling, LCD/CRT presentation, and viewport alignment
- [x] Android phone, tablet, and Android TV support
- [x] RetroAchievements sign-in, achievement lists, earned-progress tracking, and profile viewing
- [x] Offline RetroAchievements preparation, local unlock recording, durable pending uploads, and later synchronization
- [x] Safe ZIP extraction, validated core installation, sanitized file handling, and protected library cleanup

### Pending

- [ ] More display shader options and ready-made presets
- [ ] Additional app themes
- [ ] Custom UI skins and button layout themes

## 🚀 Getting started

1. Install OMINOY on an Android phone, tablet, or Android TV device.
2. On the first launch, choose the folder that contains your ROMs.
3. OMINOY scans that folder and adds supported games to the library. ZIP-compressed ROMs can also be scanned.
4. Tap a game to start playing. Long-press a game for its actions, such as adding it to favorites, changing its artwork, renaming it, or creating a launcher shortcut.
5. Open **Settings** to configure controls, display options, saves, cloud sync, and RetroAchievements.

If you add more games later, use **Rescan** from the library or home screen.

## ✨ How features work

### Library and game organization

- **Automatic ROM scanning:** Select a ROM directory once and OMINOY indexes the supported games it finds. A later rescan picks up new files and removes entries that no longer exist, while protecting the library if a storage provider temporarily fails.
- **Search:** Use the search button to find a game by name instead of browsing the whole library.
- **Recents:** Recently played games are shown so you can quickly continue where you left off.
- **Favorites:** Long-press a game and choose **Add to favorites**. Favorite games appear in their own section for quick access.
- **Custom names:** Set a display name without changing the ROM filename. You can restore the original name at any time.
- **Custom artwork and thumbnails:** Long-press a game and choose the artwork or thumbnail action. OMINOY keeps a private copy, crops artwork for the library, and preserves it across restarts and rescans. Artwork is app data; it is not written into the ROM.
- **Launcher shortcuts:** Pin an individual game to the Android launcher so it can be opened directly.
- **Play history:** OMINOY keeps per-game playtime, session count, and last-played information.

### Playing a game

- **Touch controls:** Play with the on-screen D-pad and buttons. The touch layout editor lets you move controls and adjust their size, opacity, and sensitivity for each supported console and orientation.
- **External controllers and keyboards:** Connect a compatible gamepad or keyboard. Bind the controls from the external-device settings, and reset bindings whenever needed.
- **Tilt input:** On supported games, map the device’s tilt sensor to the D-pad, an analog stick, or shoulder buttons. Sensitivity can be adjusted in settings.
- **Local multiplayer:** Connect the supported local input devices and configure the player controls in the game settings.
- **Rumble:** Enable controller rumble where the game and connected device support it. If no controller is connected, device vibration can be used instead.
- **Fast-forward and mute:** Open the in-game menu to temporarily speed through a section or mute audio.
- **Game menu:** While playing, open the menu to save, load, restart, quit, change controls, change display settings, manage cheats, or view achievements.

### Saving and continuing

OMINOY supports the two kinds of progress used by most Game Boy games:

- **In-game saves:** The game writes its normal battery-backed save data. This is the most portable way to save and is the recommended option for long-term progress.
- **Save states:** Save the exact moment of play to a state slot and return to it later. Use the in-game menu to save or load a state, or use quick save/load where available.
- **Autosave state:** When enabled, OMINOY saves a state when you exit through the game menu, making it easy to resume your last session.
- **Save folders:** Choose where ROMs, in-game saves, save states, and state previews are stored. Existing data can be moved when changing the OMINOY data folder.
- **Cloud saves:** Configure cloud save sync in Settings. You can manually sync, or enable automatic sync before and after a game session while connected to Wi-Fi. In-game saves and save states can be selected separately for syncing.

### Cheats

Open a game, choose **Cheats** from the game menu, and add a name and code. Cheats are stored for that game, so they do not affect other titles. You can edit or delete saved cheats later. Changes are applied when you return to the game.

### Display and sound

- **Display filters:** Choose a clean, sharp, smooth, CRT, or LCD-style presentation.
- **HD mode:** Apply post-processing upscaling and choose its quality level.
- **Viewport alignment:** Preview and position the game screen at the center, top, bottom, or a custom position.
- **Immersive mode:** Let OMINOY adapt the background to the game’s visuals.
- **AMOLED theme:** Use true-black backgrounds on OLED displays.
- **Audio:** Prefer low-latency audio on supported devices, with the trade-off that some devices may produce more audio glitches.

### Android and Android TV

The same library and emulator are available on Android phones and Android TV. TV navigation is designed for a remote or gamepad, and TV gameplay requires a connected gamepad.

## 🏆 RetroAchievements

OMINOY integrates [RetroAchievements](https://retroachievements.org/) for supported GB, GBC, and GBA games. This adds challenge goals to games, such as completing a milestone, finding an item, or finishing a task under specific conditions.

### First-time setup

1. Create or use a RetroAchievements account.
2. Open **Settings → RetroAchievements** and sign in.
3. Run the RetroAchievements sync for your library while you have an internet connection.
4. OMINOY matches each supported ROM to its RetroAchievements game set and downloads the achievement definitions, progress information, and the data needed to run that set offline.

A game must have a matching RetroAchievements set. Games without a set are skipped and can still be played normally.

### How offline achievements work

Offline support is prepared per game; it is not automatic for a game that has never been synced.

1. **Load the game once while online:** After signing in, sync the game before going offline. OMINOY stores the game’s achievement set and the server responses needed to identify and evaluate it.
2. **Play without internet:** Start the prepared game with no connection. The achievement logic runs locally while you play, so eligible achievements can still trigger offline.
3. **Unlocks are saved on the device:** When an achievement triggers, OMINOY shows the unlock notification and records the achievement locally, along with the game and account it belongs to. The achievement list and earned progress remain available from the game menu.
4. **Unlocks wait safely for a connection:** Offline unlocks are placed in a durable pending queue. Closing the game or restarting the device does not discard them.
5. **Sync when internet returns:** When OMINOY can reach RetroAchievements again, it uploads pending unlocks and reconciles the local progress with the account. You can also trigger a sync manually from the RetroAchievements settings.

This means you can prepare a game once, travel or play somewhere without service, continue earning its achievements, and have the results uploaded later. Offline unlocks are handled as **softcore/casual** achievements; online account synchronization still requires a valid RetroAchievements sign-in.

### Achievement progress

Open a running game’s menu and choose **Achievements** to see the cached set, descriptions, points, and the number already unlocked. The profile screen shows the account’s achievement information after it has been synchronized.

## 📸 Screenshots

<div align="center">

**Home · Library · Settings**

<img src="photos/homescreen.jpg" alt="Home screen" width="30%">
<img src="photos/library.jpg" alt="Library" width="30%">
<img src="photos/settings.jpg" alt="Settings" width="30%">

**RetroAchievements — Achievements · Earned · Profile**

<img src="photos/achivements.jpg" alt="RetroAchievements achievements" width="30%">
<img src="photos/earned achivements.jpg" alt="Earned achievements" width="30%">
<img src="photos/retroachivements profile.jpg" alt="RetroAchievements profile" width="30%">

</div>

## 🔒 Reliability and data safety

OMINOY is designed to keep game data usable and avoid destructive cleanup:

- ZIP extraction rejects unsafe paths and limits the number and size of extracted files.
- Core assets are checked in a temporary location before installation.
- Save, state, preview, cache, and offline achievement data use sanitized filenames and safe writes.
- A failed or incomplete library scan does not wipe the existing library.
- Resetting app settings preserves saves and save states.

## 🛠️ Building

**Requirements:** JDK 21, Android SDK Platform 35, Android Build Tools 34.0.0, and the repository submodules.

To build the Free Dynamic debug APK, create a throwaway debug-only keystore and run:

```bash
keytool -genkeypair -noprompt -keystore debug.keystore \
  -storepass android -alias androiddebugkey -keypass android \
  -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
./gradlew assembleFreeDynamicDebug
```

> [!NOTE]
> The debug APK uses a `.debug` application ID suffix and installs separately from a production release. In the Free Dynamic build, mGBA is downloaded on demand when first needed, so internet access may be required.

### Release builds

Create an ignored `keystore.properties` file with `storeFile`, `keyAlias`, `storePassword`, and `keyPassword` entries.

> [!WARNING]
> Never commit a production keystore or its password.

## Planned features

- More shader options and presets
- Additional app themes
- Custom UI skins and button layout themes

---

<div align="center">
Built on [Retrograde](https://github.com/retrograde/retrograde-android) and [LibretroDroid](https://github.com/Swordfish90/LibretroDroid).
</div>
