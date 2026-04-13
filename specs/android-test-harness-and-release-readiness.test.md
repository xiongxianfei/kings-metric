# Android Test Harness And Release Readiness Test Spec

## Scope

This test spec covers CI/build verification structure rather than app
feature behavior.

## Unit Tests

- `T1` Verification matrix lists JVM, instrumented, and Compose UI test
  targets.
- `T2` Release-readiness gate defines required checks explicitly.

## Integration Tests

- `IT1` CI can assemble the debug app.
- `IT2` CI can run JVM unit tests.
- `IT3` CI can run instrumented or emulator-backed tests for critical
  flows once Android module support exists.
- `IT4` CI output makes skipped Android verification visible.

## What Not To Test

- Feature business rules already covered by feature specs.
- Store publishing behavior.

## Coverage Map

- Verification structure covered by `T1`, `T2`
- Practical CI readiness covered by `IT1` to `IT4`
