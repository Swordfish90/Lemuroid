# Prerequisites
- Android Studio (latest stable) or JDK 17+ installed
- Android SDK with API 35 (compileSdkVersion = 35)
- NDK (needed for native Libretro core .so files)
- `git submodule update --init --recursive` - The submodule points to https://github.com/Swordfish90/LemuroidCores (branch master) and contains the bundled-cores project that Gradle needs. It was never cloned because a plain git clone doesn't pull submodules by default.

# Build Steps
1. Set ANDROID_HOME (if not already set)

export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

2. Build the APK
From the project root (./Lemuroid):


#### The "free bundle" variant bundles cores directly into the APK (no Play dynamic delivery)
`./gradlew :lemuroid-app:assembleFreeBundle`

#### Or for a debug build (faster, no signing required):
`./gradlew :lemuroid-app:assembleFreeBundleDebug`


The APK will be at:
`lemuroid-app/build/outputs/apk/freeBundle/debug/lemuroid-app-freeBundle-debug.apk`
Flavor explanation
The project has two dimensions:

free vs play — free is fully open-source, play uses Google Play dynamic features
bundle vs other — bundle includes all cores inside the APK; play uses dynamic feature modules
Use freeBundle for a self-contained sideloadable APK.

3. Install directly to a connected device (optional)

./gradlew :lemuroid-app:installFreeBundleDebug
Tips
First build will be slow — it downloads Gradle dependencies and compiles native cores
If you hit NDK errors, install NDK via Android Studio → SDK Manager → SDK Tools → NDK (Side by side)
Signing: debug builds use the debug.keystore already in the repo root

# Git Dependencies (via JitPack)

All resolved through https://jitpack.io (configured in build.gradle.kts).
Definitions are in buildSrc/src/main/java/deps.kt.

| Library | Package | Version |
|---------|---------|---------|
| LibretroDroid (github.com/Swordfish90/LibretroDroid) | com.github.Swordfish90:LibretroDroid | 0.13.2 |
| compose-settings ui-tiles (github.com/alorma/compose-settings) | com.github.alorma.compose-settings:ui-tiles | 2.1.0 |
| compose-settings ui-tiles-extended (github.com/alorma/compose-settings) | com.github.alorma.compose-settings:ui-tiles-extended | 2.1.0 |
| compose-settings storage-disk (github.com/alorma/compose-settings) | com.github.alorma:compose-settings-storage-disk | 2.0.0 |
| compose-settings storage-memory (github.com/alorma/compose-settings) | com.github.alorma:compose-settings-storage-memory | 2.0.0 |
| PadKit (github.com/Swordfish90/padkit) | io.github.swordfish90:padkit | 1.0.0-beta1 |