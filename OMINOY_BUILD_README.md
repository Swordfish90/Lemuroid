# OMINOY Android build

This source tree is the updated OMINOY Android app. The visible app name is **OMINOY** and the supplied launcher/TV icon assets have been applied. The application ID remains `come.asg.lemudroid` so existing installations and app data remain compatible.

## Signed release build

Requirements: JDK 21, Android SDK platform 35, and Android build-tools 34.0.0.

```bash
./gradlew :lemuroid-app:assembleFreeBundleRelease
```

The release signing configuration is in `keystore.properties`, using the supplied `airstix-release.jks` keystore and alias `airstix`. The keystore/store password and key password are `948150`.

The APK output is:

```text
lemuroid-app/build/outputs/apk/freeBundle/release/lemuroid-app-free-bundle-release.apk
```

## Repository hygiene
Production signing keys and passwords are intentionally excluded from version control. Use local ignored signing configuration for release builds.
