# Noor-e-Hidayat

Noor-e-Hidayat is an Android Islamic companion app project built with Kotlin and Jetpack Compose.

> **Project status:** Under development. Features, device compatibility, prayer notifications, audio playback, and release builds must be tested before this app is considered production-ready.

## Planned and implemented areas

The project is intended to bring together Islamic learning and daily-practice tools, including:

- Quran reading and translation
- Duas and Ahadith
- Prayer times and Azan notifications
- Qibla direction
- Islamic learning and discovery content

Availability and completeness of individual features may vary by build. See the open issues and pull requests for current work.

## Requirements

- Android Studio with a compatible Android Gradle Plugin and Kotlin toolchain
- Android SDK for the configured compile SDK
- Android 7.0 (API 24) or newer for installation

## Configuration

1. Clone this repository and open the project in Android Studio.
2. Create a local `.env` file from `.env.example` if you use the Gemini-powered features.
3. Add your own API credentials locally. Never commit `.env`, signing keys, passwords, or private tokens.
4. Configure Firebase for your own test project if you enable Firebase-backed features. Do not reuse production credentials.
5. Sync Gradle, then run the app on an emulator or test device.

Some features may require additional service configuration. Keep credentials in local files or repository secrets; do not paste them into source code or issues.

## Build

Use Android Studio to sync and build the project. For a local debug build, you can also run:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

A signed release build requires a private keystore and signing credentials configured outside the repository. Never commit the keystore or passwords.

## Testing and known limitations

This project is not yet represented as a fully tested public release. Before distributing a build, verify installation, app startup, Quran content, prayer-time calculations, location permissions, Qibla behavior, notifications, alarms, audio playback, offline behavior, and reboot behavior on real devices.

The release-preparation notes identify outstanding work, including permanent release signing, phone installation testing, Azan audio assets, and alarm restoration after reboot.

## Content, attribution, and licensing

The source repository currently has no project-wide open-source license. Until a license is added, no general permission is granted to copy, modify, or redistribute this project's code.

Third-party Quran translations, recitations, audio, images, fonts, and other content may have separate copyright and attribution requirements. Their source and redistribution permissions must be documented individually before public distribution. Do not assume that the app's source-code license would cover third-party content.

## Contributions

Contributions and issue reports are welcome once the project contribution and licensing terms are finalized. Please include device model, Android version, app build, and clear reproduction steps when reporting a problem.

## Disclaimer

This app is a software project and is not a substitute for checking prayer times and religious information with trusted local sources. Users should verify location, calculation method, and content accuracy for their circumstances.
