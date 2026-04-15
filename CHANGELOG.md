# Changelog

All notable changes to this project should be tracked here.

## [v0.1.0-alpha.4] - 2026-04-15

Alpha prerelease replacement for early testers.

### Features

- **In-app diagnostics support** - the app now keeps bounded local diagnostics
  for import, recognition, and save outcomes, and exposes a `Diagnostics`
  screen that lets users copy a redacted diagnostics export without adb.

### Bug Fixes

- **Supported screenshots no longer fall through to generic read-failure as
  often on real devices** - the ML Kit mapper now extracts required first-value
  metrics from narrow label-local line windows so flattened OCR column ordering
  does not hide required fields like damage dealt.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.3`
- release gating now requires explicit diagnostics-support readiness before
  publication

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

## [v0.1.0-alpha.3] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Import now fails safely when recognition hits an unexpected runtime
  exception** - the Android recognition path now collapses unexpected
  OCR-mapper/runtime exceptions into the same retryable import-failure state as
  explicit OCR failures instead of letting the app quit during screenshot
  import.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.2`
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
