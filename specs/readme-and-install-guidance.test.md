# README And Install Guidance Test Spec

## Scope

This test spec covers repository README presence, developer build/run guidance,
first-release user guidance, install instructions for the GitHub artifact,
supported-scope statements, and known-limitations documentation.

## Unit Tests

- `T1` Repository contains a top-level `README.md`.
- `T2` README introduction states what the app does and identifies it as an
  Android app.
- `T3` README states that processing is local-first and on-device.
- `T4` README states that the first release is alpha quality.
- `T5` README documents a basic local build path for developers.
- `T6` README documents a basic local run path for developers.
- `T7` README identifies the main verification commands or local validation
  path a maintainer is expected to use before release preparation.
- `T8` README documents the current supported screenshot scope and required
  review-before-save behavior.
- `T9` README explicitly states that unsupported screenshots are rejected.
- `T10` README includes the current hero manual-review limitation when that
  limitation still exists.
- `T11` README install guidance identifies where users obtain the release
  artifact and how to install it at a basic level.
- `T12` README excludes unsupported claims such as multi-template support,
  non-Chinese screenshots, cloud sync, and server OCR.
- `T13` README summarizes the high-level user flow: import screenshot, review
  extracted data, save locally.

## Integration Tests

- `IT1` README build/run guidance stays consistent with the actual repository
  build/run entry path instead of pointing to a stale or unrelated setup.
- `IT2` README supported-scope and limitation wording stays consistent with the
  release metadata/positioning contract.
- `IT3` README install guidance is specific to the intended GitHub release
  artifact rather than generic Android build output.
- `IT4` If README omits build guidance, run guidance, install guidance, alpha
  warning, or supported-scope information, the release documentation check
  flags the repository as not release-ready.
- `IT5` If README and release notes diverge on supported scope or limitations,
  the release documentation check flags that mismatch as blocking.

## What Not To Test

- Full developer contribution documentation.
- GitHub repository description content itself.
- GitHub release publication mechanics.

## Coverage Map

- README existence covered by `T1`
- App introduction and Android identity covered by `T2`
- Local-first/privacy behavior covered by `T3`
- Alpha-quality warning covered by `T4`, `IT4`
- Developer build/run guidance covered by `T5`, `T6`, `T7`, `IT1`, `IT4`
- Supported scope and unsupported rejection covered by `T8`, `T9`, `T12`,
  `IT2`, `IT5`
- Hero limitation disclosure covered by `T10`, `IT5`
- Install guidance covered by `T11`, `IT3`, `IT4`
- User flow summary covered by `T13`

## Not Directly Testable

- Writing style quality beyond clarity, completeness, and factual correctness.
