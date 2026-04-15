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

## Privacy And Data

This app is local-first and processes screenshots on-device. It does not depend
on cloud sync or server OCR for the supported import path.

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
