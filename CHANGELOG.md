# Changelog

All notable changes to this project should be tracked here.

## [v0.1.0-alpha.15] - 2026-04-16

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Dashboard graph panels now stay reachable on phone-sized screens** - the
  loaded dashboard now scrolls vertically, so lower graph panels such as
  `Hero Usage` no longer clip below the fold when the dashboard grows taller
  than one portrait viewport.

### Internal

- added a constrained-height Compose regression that pins full `Hero Usage`
  reachability through normal dashboard scrolling
- tightened the dashboard graph contract and test contract around tall
  phone-sized viewport reachability
- captured the reusable Compose-screen reachability rule in `AGENTS.md`
- aligned the release metadata, artifact contract, changelog, and release
  notes to the `v0.1.0-alpha.15` cut
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.14`

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

## [v0.1.0-alpha.14] - 2026-04-16

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Dashboard hero UI now fails closed on unreadable placeholder values** - the
  dashboard no longer renders numeric-only placeholder heroes such as `1` or
  `2` as if they were real hero labels in `Most Played Hero` or `Hero Usage`.
- **New imports can no longer save numeric-only hero placeholders** - the
  import, review, and save flow now treats numeric-only `hero` values as
  unresolved required input, so new saved matches cannot persist those
  placeholders as final hero names.

### Internal

- added dashboard regressions for unreadable placeholder hero values in hero
  metrics and graph shaping
- added import/review/save regressions for numeric-only required `hero`
  placeholders
- aligned the release metadata, artifact contract, changelog, and release
  notes to the `v0.1.0-alpha.14` cut
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.13`

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

## [v0.1.0-alpha.13] - 2026-04-16

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Marksman lane insights now appear for imported marksman screenshots** - the
  OCR lane detector no longer misclassifies supported marksman screenshots as
  jungle because of metric labels such as `打野经济`, and the marksman insight
  layer now also recognizes older saved records that still use the legacy
  `Farm Lane` lane value.

### Internal

- added OCR regressions for supported readable-Chinese lane extraction and
  merged summary-card parsing
- added marksman-lane compatibility regressions for legacy saved `Farm Lane`
  records
- aligned the release metadata, artifact contract, changelog, and release
  notes to the `v0.1.0-alpha.13` cut
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.12`

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

## [v0.1.0-alpha.12] - 2026-04-16

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Record detail sections remain reachable on phone-sized screens** - the
  saved-match detail screen now scrolls vertically, so lower grouped sections
  such as `Damage Output` no longer clip below the fold on smaller viewports.

### Internal

- added a phone-sized Compose regression that pins access to lower detail
  fields instead of assuming all grouped sections fit in the initial viewport
- tightened the record-detail usability contract and test contract to require
  lower-section reachability
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.11`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.12` cut

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

## [v0.1.0-alpha.11] - 2026-04-16

Alpha prerelease replacement for early testers.

### Features

- **History rows now show more useful match context at a glance** - the History
  page now shows a bounded quick summary of `Result`, `Lane`, `KDA`, and
  `Score` when those values are available from the saved record, so users can
  distinguish adjacent matches without opening Detail first.

### Internal

- aligned the Room observation path, controller path, and history-screen
  binding to the same richer history-row contract
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.10`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.11` cut

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

## [v0.1.0-alpha.10] - 2026-04-16

Alpha prerelease replacement for early testers.

### Features

- **Shared visual polish across the core app flow** - the app now uses one
  consistent shell-level design language across Import, Review, History,
  Detail, Dashboard, and Diagnostics, with stronger title hierarchy, spacing,
  surfaces, and primary-action treatment.
- **Diagnostics now read like a support surface instead of raw exported text**
  - `Current Version`, export guidance, failure `Reason`, `Surface`, and OCR
  text are now presented in dedicated readable sections.

### Internal

- closed the `2026-04-15-ui-ux-polish` plan after milestone-by-milestone
  verification across import, review, history/detail, dashboard, diagnostics,
  and residual cross-screen UX coverage
- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.9`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.10` cut

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

## [v0.1.0-alpha.9] - 2026-04-15

Alpha prerelease replacement for early testers.

### Bug Fixes

- **Supported screenshot import no longer aborts when one OCR helper parse path
  throws** - the Android ML Kit mapper now degrades to a reviewable partial
  supported analysis when anchors and other visible values still prove the
  screenshot is on the supported template path, instead of collapsing the whole
  import into a generic recognition failure.

### Internal

- bumped Android release versioning so the replacement APK can be installed
  over `v0.1.0-alpha.8`
- aligned the release metadata, artifact contract, and release notes to the
  `v0.1.0-alpha.9` cut

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
