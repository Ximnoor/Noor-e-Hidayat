# Free Android distribution

## Current status (23 September 2026)

This is release preparation, not a published or phone-tested release.

- The source repository is private. Its releases are also private.
- The existing test-v1.0.0 APK was built successfully from commit 7392e2efd2b6b5e8eb14141d225600aad1ab0d62.
- That test build uses a temporary debug key; it is not a permanent release-signing setup.
- The application ID is com.aistudio.noorehidayat.nuigjs. Minimum Android version is 7.0 (API 24).
- app/src/main/res/raw/azan_madina.mp3 and azan_makkah.mp3 are empty placeholders.
- AzanBroadcastReceiver returns on BOOT_COMPLETED without restoring alarms.
- The previously reported installation failure remains undiagnosed. Obtain the phone model, Android version and exact installer error, or an adb install error, before claiming it is fixed.

## Free distribution route

Keep this repository private. Create a separate PUBLIC repository named Noor-e-Hidayat-Downloads that contains only the verified APK, checksums, release notes, screenshots and an accurate privacy notice. Upload the APK as a GitHub Release asset there. Do not copy source code, credentials or signing keys.

Anyone can then download that release without access to this source repository. A separate public download repository has not been created by this change.

Standard GitHub-hosted Actions runners have a monthly free allowance for private repositories; this is not unlimited. Confirm remaining allowance and keep paid usage disabled. The release workflow is manual and retains artifacts for seven days.

## One-time permanent signing setup

If a permanent signing key already exists for this app, reuse it. Do not replace it. The current Gradle configuration expects alias upload.

If no permanent key exists, use Android Studio: Build > Generate Signed Bundle / APK > APK > Create new. Choose a private keystore location outside the repository, alias upload, and strong passwords. Back up the keystore and passwords securely. Do not send them in chat or commit them.

Convert the keystore to Base64 locally using PowerShell (replace the path):

    [Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\Private\noor-release.jks')) | Set-Clipboard

In this PRIVATE source repository, open Settings > Secrets and variables > Actions and add:
- ANDROID_KEYSTORE_BASE64: the Base64 keystore.
- ANDROID_STORE_PASSWORD: the keystore password.
- ANDROID_KEY_PASSWORD: the upload key password.

Clear the clipboard afterward:

    Set-Clipboard -Value ''

The workflow decodes the key into the temporary runner directory, never uploads it as an artifact, and removes it afterward. These connected tools cannot configure repository secrets.

## Build and test

1. Merge the release-preparation PR into main after review.
2. Open Actions > Build signed release APK > Run workflow, selecting main.
3. The workflow requires signing secrets, runs unit tests and release lint, builds a signed release APK, verifies its signature and checks that it is not debuggable.
4. Download the resulting artifact ZIP and extract Noor-e-Hidayat.apk. The ZIP itself is not installable.
5. Transfer the APK to a phone running Android 7.0 or newer, allow installation from the chosen browser/file manager when Android asks, and test it.
6. Test launch, Quran and translation, prayer location and times, Qibla, Duas, notification permissions, alarms, offline behavior and reboot behavior. The empty custom Azan audio and reboot restoration still require implementation/content before a full release.
7. Keep package-info.txt, signature.txt and SHA256SUMS.txt with the build evidence.

Do not assume a signing change caused the owner's installation failure. If Android reports a signature conflict with an older test build, preserve needed data first; uninstalling the old app deletes its local data.

## Publish after successful phone testing

Create a release in the separate public download repository. Upload only Noor-e-Hidayat.apk, SHA256SUMS.txt and release notes. State supported Android versions, features actually tested and remaining limitations. Add an accurate privacy notice based on the app's actual data flows before general distribution.

For every later release, increment versionCode in app/build.gradle.kts, update versionName, and use the SAME permanent signing key. Never use the test workflow's changing debug key for public release builds.

This change does not create a public repository, configure signing secrets, validate an APK on a phone, or publish a new APK.
