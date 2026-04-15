# Release Metadata And Positioning Test Spec

## Scope

This test spec covers first-release version/channel wording, repository
description/tagline alignment, and supported-scope consistency across
GitHub-facing release metadata.

## Unit Tests

- `T1` First-release metadata model marks the first GitHub release as alpha
  prerelease rather than stable.
- `T2` Version/tag mapping for the first release uses a prerelease-compatible
  shape and rejects stable-first-release wording.
- `T3` Repository description/tagline mapping identifies the product as an
  Android app for local, on-device screenshot import and review.
- `T4` Supported-scope metadata includes only the current supported screenshot
  scope and required review behavior.
- `T5` Metadata contract excludes unsupported claims such as multi-template,
  non-Chinese, cloud sync, and server OCR support.
- `T6` Positioning contract remains compatible with the known hero manual-review
  limitation and does not imply fully automatic extraction.

## Integration Tests

- `IT1` Repository description/tagline and release-positioning metadata resolve
  to the same maturity level and supported-scope statement.
- `IT2` If one GitHub-facing metadata surface uses stable or overbroad wording
  while another uses alpha/narrow-scope wording, the release metadata check
  flags that mismatch as blocking.
- `IT3` Metadata contract makes the intended audience explicit enough for an
  early tester to understand that the first release is alpha quality.

## What Not To Test

- README body content.
- Android artifact signing.
- GitHub release publication itself.

## Coverage Map

- Alpha prerelease identity covered by `T1`, `T2`, `IT1`
- Android/local-first product identity covered by `T3`
- Supported-scope messaging covered by `T4`, `T5`, `IT1`
- Hero/manual-review-safe positioning covered by `T6`
- Cross-surface consistency covered by `IT1`, `IT2`, `IT3`

## Not Directly Testable

- Exact marketing tone beyond factual scope, maturity, and consistency.
