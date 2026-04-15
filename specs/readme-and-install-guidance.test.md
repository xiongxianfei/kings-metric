# README And Install Guidance Test Spec

## Scope

This test spec covers repository README presence, first-release user guidance,
install instructions for the GitHub artifact, supported-scope statements, and
known-limitations documentation.

## Unit Tests

- `T1` Repository contains a top-level `README.md`.
- `T2` README introduction states what the app does and identifies it as an
  Android app.
- `T3` README states that processing is local-first and on-device.
- `T4` README states that the first release is alpha quality.
- `T5` README documents the current supported screenshot scope and required
  review-before-save behavior.
- `T6` README explicitly states that unsupported screenshots are rejected.
- `T7` README includes the current hero manual-review limitation when that
  limitation still exists.
- `T8` README install guidance identifies where users obtain the release
  artifact and how to install it at a basic level.
- `T9` README excludes unsupported claims such as multi-template support,
  non-Chinese screenshots, cloud sync, and server OCR.
- `T10` README summarizes the high-level user flow: import screenshot, review
  extracted data, save locally.

## Integration Tests

- `IT1` README supported-scope and limitation wording stays consistent with the
  release metadata/positioning contract.
- `IT2` README install guidance is specific to the intended GitHub release
  artifact rather than generic Android build output.
- `IT3` If README omits install guidance, alpha warning, or supported-scope
  information, the release documentation check flags the repository as not
  release-ready.
- `IT4` If README and release notes diverge on supported scope or limitations,
  the release documentation check flags that mismatch as blocking.

## What Not To Test

- Full developer contribution documentation.
- GitHub repository description content itself.
- GitHub release publication mechanics.

## Coverage Map

- README existence covered by `T1`
- App introduction and Android identity covered by `T2`
- Local-first/privacy behavior covered by `T3`
- Alpha-quality warning covered by `T4`, `IT3`
- Supported scope and unsupported rejection covered by `T5`, `T6`, `T9`, `IT1`,
  `IT4`
- Hero limitation disclosure covered by `T7`, `IT4`
- Install guidance covered by `T8`, `IT2`, `IT3`
- User flow summary covered by `T10`

## Not Directly Testable

- Writing style quality beyond clarity, completeness, and factual correctness.
