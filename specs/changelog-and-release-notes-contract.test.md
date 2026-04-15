# Changelog And Release Notes Contract Test Spec

## Scope

This test spec covers root `CHANGELOG.md` presence, required release-entry
content, conventional changelog structure, per-release notes content,
change-summary usefulness, and consistency with README and release metadata.

## Unit Tests

- `T1` Repository contains root `CHANGELOG.md`.
- `T2` Root `CHANGELOG.md` includes a release-specific entry for the current
  release rather than only a vague rolling note.
- `T3` Root `CHANGELOG.md` current-release entry uses the agreed dated header
  format such as `## [vX.Y.Z] - YYYY-MM-DD`.
- `T4` Root `CHANGELOG.md` is ordered newest release first.
- `T5` Per-release notes identify the release version or release identifier.
- `T6` Root `CHANGELOG.md` entry and per-release notes identify the release
  channel or maturity level, such as alpha prerelease.
- `T7` Root `CHANGELOG.md` entry and per-release notes summarize what the
  release includes at a user-facing level.
- `T8` Root `CHANGELOG.md` entry and per-release notes describe the currently
  supported scope.
- `T9` Root `CHANGELOG.md` entry and per-release notes state that unsupported
  screenshots are rejected.
- `T10` Root `CHANGELOG.md` entry and per-release notes include the current
  hero manual-review limitation when that limitation still exists.
- `T11` Root `CHANGELOG.md` makes it possible to understand what is new in the
  release without reading full Git history.
- `T12` Root `CHANGELOG.md` uses stable release-facing section headings such as
  `Features`, `Bug Fixes`, and `Internal` when categories are present.
- `T13` Root `CHANGELOG.md` and per-release notes avoid unsupported claims such
  as extra templates, non-Chinese support, cloud sync, or server OCR.

## Integration Tests

- `IT1` Root `CHANGELOG.md` entry and release notes stay consistent with
  repository metadata/positioning on release maturity and supported scope.
- `IT2` Root `CHANGELOG.md` entry and release notes stay consistent with README
  supported scope and known limitations.
- `IT3` If root `CHANGELOG.md` has no entry for the current release, the
  release documentation check flags publication as blocked.
- `IT4` If root `CHANGELOG.md` entry format or ordering drifts from the agreed
  release structure, the release documentation check flags that release as not
  ready.
- `IT5` If root `CHANGELOG.md` entry or release notes omit alpha wording,
  supported scope, or material known limitations, the release documentation
  check flags that release as not ready.
- `IT6` If root `CHANGELOG.md` entry or release notes drift into aspirational
  roadmap claims that exceed the current verified product state, the release
  documentation check flags that mismatch as blocking.

## What Not To Test

- GitHub publication workflow mechanics.
- Signing-path implementation details.
- Full product documentation beyond release-facing change notes.

## Coverage Map

- Root `CHANGELOG.md` presence, entry coverage, and dated structure covered by
  `T1`, `T2`, `T3`, `T4`, `IT3`, `IT4`
- Release identity and maturity covered by `T5`, `T6`, `IT1`, `IT5`
- Included changes and supported scope covered by `T7`, `T8`, `T11`, `IT1`,
  `IT2`
- Unsupported rejection and hero limitation disclosure covered by `T9`, `T10`,
  `IT2`, `IT5`
- Stable changelog category structure covered by `T12`, `IT4`
- No overclaiming beyond verified scope covered by `T13`, `IT6`

## Not Directly Testable

- Exact writing quality beyond clarity, factual completeness, and alignment
  with the current verified release state.
