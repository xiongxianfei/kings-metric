# Changelog And Release Notes Contract Test Spec

## Scope

This test spec covers tracked release-notes source presence, required
release-note content, change-summary usefulness, and consistency with README
and release metadata.

## Unit Tests

- `T1` Each release candidate has a tracked release-notes source file.
- `T2` Release-notes source is release-specific or version-specific rather than
  an unbounded rolling note.
- `T3` Release notes identify the release version or release identifier.
- `T4` Release notes identify the release channel or maturity level, such as
  alpha prerelease.
- `T5` Release notes summarize what the release includes at a user-facing
  level.
- `T6` Release notes describe the currently supported scope.
- `T7` Release notes state that unsupported screenshots are rejected.
- `T8` Release notes include the current hero manual-review limitation when
  that limitation still exists.
- `T9` Release notes make it possible to understand what is new in the release
  without reading full Git history.
- `T10` Release notes avoid unsupported claims such as extra templates,
  non-Chinese support, cloud sync, or server OCR.

## Integration Tests

- `IT1` Release notes stay consistent with repository metadata/positioning on
  release maturity and supported scope.
- `IT2` Release notes stay consistent with README supported scope and known
  limitations.
- `IT3` If a release has no tracked release-notes source, the release
  documentation check flags publication as blocked.
- `IT4` If release notes omit alpha wording, supported scope, or material known
  limitations, the release documentation check flags that release as not ready.
- `IT5` If release notes drift into aspirational roadmap claims that exceed the
  current verified product state, the release documentation check flags that
  mismatch as blocking.

## What Not To Test

- GitHub publication workflow mechanics.
- Signing-path implementation details.
- Full product documentation beyond release-facing change notes.

## Coverage Map

- Tracked release-notes source covered by `T1`, `T2`, `IT3`
- Release identity and maturity covered by `T3`, `T4`, `IT1`, `IT4`
- Included changes and supported scope covered by `T5`, `T6`, `T9`, `IT1`,
  `IT2`
- Unsupported rejection and hero limitation disclosure covered by `T7`, `T8`,
  `IT2`, `IT4`
- No overclaiming beyond verified scope covered by `T10`, `IT5`

## Not Directly Testable

- Exact writing quality beyond clarity, factual completeness, and alignment
  with the current verified release state.
