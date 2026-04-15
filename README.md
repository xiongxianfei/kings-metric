# Honor of Kings Match Tracker

Alpha Android app for local Honor of Kings screenshot import, review, and local
save.

## What It Does

This app imports one supported Honor of Kings post-match personal-stats
screenshot, extracts visible match data on-device, asks you to review the
result, and then saves the screenshot plus the confirmed record locally.

The current high-level flow is:

1. import a screenshot
2. review the extracted data
3. save the record locally

## Release Status

The first GitHub release is alpha quality. It is intended for early testers,
not as a stable `1.0.0` release.

Tracked release history lives in [CHANGELOG.md](./CHANGELOG.md).

## Build

Basic local build path:

1. open the project root that contains `settings.gradle.kts`
2. run `.\gradlew.bat --no-daemon :core:test`
3. run `.\gradlew.bat --no-daemon :app:assembleDebug`

These are the minimum local verification commands maintainers should use before
preparing a release candidate.

## Run

You can run the app locally from Android Studio or from the Android app module
in this repository:

1. open the project in Android Studio
2. select the `app/` Android application target
3. run the app on a connected device or emulator

The repository root and Android entry path are managed from the same project
that contains `settings.gradle.kts`.

## Supported Scope

The current release supports only:

- one supported Simplified Chinese post-match detailed-data screenshot
- local screenshot import
- on-device processing
- required review before final save

Unsupported screenshots are rejected.

## Install

Install the release artifact from the repository's GitHub Releases page.

Basic install path:

1. open `GitHub Releases`
2. download the attached Android release APK
3. install the APK on your Android device
4. launch the app and import a supported screenshot

If installation is blocked by Android, enable installation from the downloaded
source according to your device's normal APK install flow, then retry.

For the tracked release summary behind the GitHub release page, see
[CHANGELOG.md](./CHANGELOG.md).

## Privacy And Data

This app is local-first and processes screenshots on-device. It does not depend
on cloud sync or server OCR for the supported import path.

## Diagnostics And Support

If you hit a real-user problem on the supported path, open the in-app
`Diagnostics` screen from the main navigation.

Basic support flow:

1. reproduce the problem if possible
2. open `Diagnostics`
3. review the recent entries
4. tap `Copy Diagnostics`
5. paste the copied text into your support message or issue report

The diagnostics export is bounded and privacy-safe by default:

- it does not include the original screenshot
- it does not include raw OCR text dumps
- it does not include the full saved match payload

The diagnostics export is local-only. The app does not upload it automatically.

## Known Limitations

- Hero may still require manual entry during review.
- Unsupported screenshots are rejected.
- The app does not support additional templates or non-Chinese screenshots in
  this release.

## Not In This Release

- multiple screenshot templates
- non-Chinese screenshot support
- cloud sync
- server OCR
