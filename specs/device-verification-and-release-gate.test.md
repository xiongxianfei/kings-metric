# Device Verification And Release Gate Test Spec

## Scope

This test spec covers the release-specific verification matrix and blocking
gate for the first GitHub alpha release.

## Unit Tests

- `T1` Release verification matrix lists all required release checks:
  metadata/docs, signed artifact, JVM, Android critical-path verification, and
  manual device confirmation.
- `T2` Release gate reports `Ready` only when every required release check is
  completed.
- `T3` Release gate blocks when signed-artifact readiness is missing.
- `T4` Release gate blocks when manual device confirmation is missing.
- `T5` Release gate blocks when metadata/docs consistency is missing.
- `T6` Release gate exposes skipped checks distinctly from failed checks.
- `T7` Release gate does not require fully automatic hero extraction when the
  alpha limitation still documents manual hero review.

## Integration Tests

- `IT1` A complete alpha candidate with all required checks passes the release
  gate.
- `IT2` A candidate with passed JVM/build checks but skipped emulator or device
  verification remains blocked with explicit blocker text.
- `IT3` A candidate with ready artifact inputs but missing real-device flow
  confirmation remains blocked.
- `IT4` A candidate with real-device confirmation but blocked signed-artifact
  readiness remains blocked.
- `IT5` Release verification matrix stays compatible with the release metadata,
  changelog/release-notes, artifact, and alpha-hardening contracts.

## What Not To Test

- GitHub release publication mechanics.
- Play Store release behavior.
- New OCR or hero-recognition logic.

## Coverage Map

- Release-specific matrix definition covered by `T1`, `IT5`
- Ready-only-when-complete behavior covered by `T2`, `IT1`
- Signed-artifact blocking covered by `T3`, `IT4`
- Manual device confirmation requirement covered by `T4`, `IT3`
- Metadata/docs consistency blocking covered by `T5`, `IT5`
- Skipped-check visibility covered by `T6`, `IT2`
- Alpha-scope limitation handling covered by `T7`, `IT5`

## Not Directly Testable

- Actual quality of human-run device verification beyond whether the gate
  requires and records that confirmation explicitly.
