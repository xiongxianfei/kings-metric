# Release Signing And Artifact Path Test Spec

## Scope

This test spec covers release-artifact eligibility, signing-path behavior,
artifact verification, and fail-closed handling when the intended signed user
artifact is unavailable.

## Unit Tests

- `T1` Release artifact contract identifies exactly one Android artifact as the
  first-release user artifact.
- `T2` Release artifact contract marks release-build output, not debug-only
  output, as the eligible first-release artifact.
- `T3` Signing contract requires the intended user artifact to be signed.
- `T4` Signing contract rejects unsigned, debug-only, or otherwise unintended
  artifacts as substitutes for the first-release artifact.
- `T5` Missing signing inputs cause the signed release-artifact path to fail
  closed rather than silently downgrading to another artifact.
- `T6` Artifact verification contract records whether the intended signed
  artifact was produced successfully.
- `T7` Artifact metadata preserves traceability between the produced artifact
  and the repository version or release candidate being prepared.

## Integration Tests

- `IT1` When only debug output exists, the release-artifact path does not mark
  that output as eligible for GitHub publication.
- `IT2` When signing inputs are unavailable, the release-artifact path reports
  the signed user artifact as blocked rather than ready.
- `IT3` When multiple Android outputs exist, only the defined signed
  first-release artifact is passed downstream to release publication.
- `IT4` Release-artifact path provides enough traceability for the GitHub
  release execution step to match the published artifact to the verified
  candidate.

## What Not To Test

- GitHub release page content.
- README or repository description text.
- Play Store distribution behavior.

## Coverage Map

- Eligible artifact definition covered by `T1`, `T2`, `IT1`, `IT3`
- Signing requirement covered by `T3`, `T4`, `T5`, `IT2`
- Artifact verification covered by `T6`
- Traceability covered by `T7`, `IT4`
- Fail-closed behavior covered by `T4`, `T5`, `IT1`, `IT2`

## Not Directly Testable

- Strength of the signing key-management process beyond whether the release
  path requires and validates the intended signed artifact inputs.
