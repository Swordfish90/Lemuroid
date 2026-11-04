# OMINOY build instructions

This archive contains the complete OMINOY Android project source with the updated app icon assets.

## Build a release APK

1. Install Android SDK Platform 35 and Build Tools 34.0.0.
2. Set `ANDROID_HOME` or create `local.properties` with:

   `sdk.dir=/path/to/your/android-sdk`

3. Build the free bundled release:

   `./gradlew :lemuroid-app:assembleFreeBundleRelease`

The release build expects a local `keystore.properties` file with `storeFile`, `keyAlias`, `storePassword`, and `keyPassword`. The signing keystore and credentials are intentionally not included in this source archive.
