# UI update notes

This source snapshot contains the requested home-screen, settings, navigation, and gamepad updates:

- Home artwork fills the background edge-to-edge with a softened blur and animated color wash. The top header respects the status-bar inset.
- The main activity uses transparent system bars, white status-bar icons, edge-to-edge layout, and disables the Android navigation-bar contrast scrim.
- Bottom navigation is an overlay floating compact pill with a translucent surface, shadow, and animated selected-item highlight; there is no full-width strip behind it. It retains Play, Library, and Settings routing and state restoration.
- Home, library, search, favorites, system/game lists, and settings provide bottom scroll room so their final content is not obscured by the floating pill.
- Widescreen L/R shoulder buttons are drawn as mirrored custom cubic-Bezier bean/kidney shapes, with two outlines, centered light labels, a brighter pressed fill, and corresponding touch hitboxes. Existing button IDs and input mapping are unchanged.
- The previous artwork-driven home refresh, game statistics, settings styling, and on-demand core installation changes remain included.

No new dependencies were added.

Build the debug APK with JDK 21, Android SDK Platform 35, and Build Tools 34.0.0:

```bash
./gradlew assembleFreeDynamicDebug
```

The APK is debug-signed, not production/release signed. Dynamic-core downloads require an internet connection the first time each core is used. Playtime totals begin accumulating from sessions in this version; older sessions are not backfilled.
