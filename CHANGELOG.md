# Changelog

All notable changes to this project should be tracked here.

## [v0.1.0-alpha.8] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Supported screenshots no longer fail when share percentages stay on the
  section line** - the Android ML Kit mapper now treats readable Chinese OCR
  patterns such as `输出伤害 35.3%` and `承伤 20.3%` as valid fallback sources for
  output-share and damage-taken-share instead of collapsing to a generic
  recognition failure.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.7`
- aligned the release metadata, artifact contract, diagnostics appVersion, and
  release notes to the `v0.1.0-alpha.8` cut

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

## [v0.1.0-alpha.7] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Supported screenshots with readable OCR labels now stay on the supported
  path** - the Android ML Kit mapper now uses canonical readable Chinese label
  variants such as `对英雄輸出`, `输出占比`, `经济占比`, and `参团率` instead of
  relying on garbled mojibake parser constants.
- **Trailing OCR metrics no longer require same-line label/value layout** - the
  supported mapper now uses a later bounded scan for split-line metrics such as
  `打野经济`, `补刀数`, `控制时长`, and `对塔伤害`, so real flattened OCR dumps
  from the supported screenshot can still reach review.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.6`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.7` cut

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

## [v0.1.0-alpha.6] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Recognition failures now preserve OCR text for diagnostics** - the
  Android recognition path now carries OCR text through mapper-stage failures
  when ML Kit already produced text, instead of collapsing to only a generic
  import-failure message.

### Features

- **Diagnostics screen now shows OCR text directly** - when a failed import or
  recognition attempt includes OCR text, the Diagnostics screen now renders
  that OCR text in-app instead of hiding it only in the copied export.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.5`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.6` cut

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

## [v0.1.0-alpha.5] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Supported screenshots are more tolerant of real-device OCR variants** -
  the ML Kit mapper now accepts narrow traditional or truncated label variants
  such as `对英雄输出`, truncated `对英雄出`, and abbreviated `团率`, so supported screenshots are
  more likely to continue into review instead of falling into a generic import
  failure.

### Features

- **Diagnostics can now include OCR text for failed recognition attempts** -
  the in-app Diagnostics screen and clipboard export now preserve the OCR text
  alongside the bounded failure reason when import or recognition fails, which
  makes real-user OCR bugs much easier to diagnose.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.4`
- release diagnostics contract now explicitly allows OCR text in recognition
  failure exports while still excluding the screenshot binary

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

- **Screenshot import no longer crashes on large real-device screenshots** -
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
