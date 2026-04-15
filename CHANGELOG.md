# Changelog

All notable changes to this project should be tracked here.

## [v0.1.0-alpha.2] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Screenshot import no longer crashes on large real-device screenshots** —
  the app now uses bounds-only bitmap probes for OCR pre-validation and a
  bounded downsampled bitmap for the review preview instead of decoding the
  original screenshot at full resolution on the import path.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.1`
- published replacement release notes and metadata for the fixed alpha build

### Supported Scope

- one supported Simplified Chinese post-match detailed-data screenshot
- local screenshot import
- on-device processing
- required review before final save

Unsupported screenshots are rejected.

### Known Limitations

- Hero may still require manual entry during review.
- The app does not support additional templates or non-Chinese screenshots in
  this release.

## [v0.1.0-alpha.1] - 2026-04-15

Alpha prerelease for early testers.

### Features

- local screenshot import, review, and local save on the supported path
- history, detail, and dashboard browsing for saved records

### Internal

- first runnable GitHub alpha release path for the Android app
- tracked release metadata for alpha positioning and supported scope

### Supported Scope

- one supported Simplified Chinese post-match detailed-data screenshot
- local screenshot import
- on-device processing
- required review before final save

Unsupported screenshots are rejected.

### Known Limitations

- Hero may still require manual entry during review.
- The app does not support additional templates or non-Chinese screenshots in
  this release.
