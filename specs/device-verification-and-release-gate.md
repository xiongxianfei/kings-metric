# Device Verification And Release Gate Spec

## Goal and Context

Define the minimum release-candidate verification matrix and the blocking gate
used before publishing the first GitHub alpha release.

This feature sits on top of the existing release metadata, README/changelog,
artifact-signing path, automated workflow, and alpha critical-path hardening.
It decides when the repository may treat a candidate as ready for publication.

## Concrete Examples

### Example 1: Candidate Is Ready For Alpha Publication

Input:
- The signed release artifact is ready.
- Release-facing docs are aligned.
- Automated JVM checks pass.
- Android critical-path verification passes on the intended alpha path.

Expected behavior:
- The release gate reports the candidate as ready.
- Maintainers can point to an explicit list of passed checks instead of relying
  on ad-hoc judgment.

### Example 2: Emulator Verification Is Skipped

Input:
- A maintainer evaluates a release candidate.
- JVM checks pass, but an emulator-backed verification step was skipped.

Expected behavior:
- The gate remains blocked.
- The skipped check is visible as an explicit blocker rather than silently
  disappearing from the report.

### Example 3: Signed Artifact Inputs Are Missing

Input:
- A maintainer evaluates a release candidate before publication.
- Release-signing readiness is still blocked.

Expected behavior:
- The release gate reports the candidate as blocked.
- The candidate is not treated as ready merely because debug or JVM checks
  passed.

### Example 4: Manual Device Pass Confirms The Supported Flow

Input:
- The candidate has already passed automated checks.
- A maintainer completes the supported import -> review -> save -> history flow
  on an intended alpha device.

Expected behavior:
- The release gate records that device confirmation as a required release input.
- The candidate becomes ready only when that device confirmation is present
  alongside the other required checks.

## Requirements

### Release Verification Matrix

- The repository MUST define a release-candidate verification matrix that is
  specific to the first GitHub alpha release, not only the generic Android app
  readiness bar.
- The matrix MUST include explicit checks for:
  - release-facing metadata and documentation alignment
  - signed release-artifact readiness
  - JVM verification
  - Android critical-path verification
  - manual device confirmation of the supported alpha flow
- The matrix MUST make each required check attributable by name.

### Release Gate Behavior

- The release gate MUST block publication when any required release-candidate
  check is missing, failed, or skipped.
- The release gate MUST treat skipped emulator or device verification as
  blocked, not as implicitly passed.
- The release gate MUST block publication when signed-artifact readiness is not
  confirmed.
- The release gate MUST block publication when release-facing docs or metadata
  are inconsistent with the supported alpha scope.
- The release gate MUST require confirmation of the supported
  import -> review -> save -> history flow on a real device before the first
  GitHub alpha release is treated as ready.

### Critical Path Coverage

- Android critical-path verification MUST cover the supported alpha path rather
  than unrelated or speculative product flows.
- The release gate MUST NOT require hero auto-extraction to be fully automatic
  if the documented alpha limitation still says hero may require manual entry.
- The release gate SHOULD surface actionable blocker text that tells a
  maintainer what is still missing.

## Interface Expectations

- A maintainer should be able to tell from the release-gate result:
  - whether the candidate is ready or blocked
  - which required checks passed
  - which required checks are still missing, failed, or skipped
  - whether the signed artifact and real-device confirmation are present

## Error-State Expectations

- If a required release check is skipped, the gate MUST report that skip
  explicitly.
- If only generic Android readiness passed but release-specific checks did not,
  the candidate MUST remain blocked.
- If docs and runtime scope diverge, the candidate MUST remain blocked instead
  of relying on manual interpretation.

## Edge Cases

- The signed artifact is ready, but the real-device alpha flow was never
  confirmed.
- Emulator-backed Android verification passed, but release docs still overclaim
  support.
- The manual device pass succeeded, but the signed artifact is still blocked.
- A maintainer tries to treat a skipped emulator check as equivalent to a pass.

## Non-Goals

- Publishing the GitHub release itself.
- Replacing the signing-path implementation.
- Expanding support to new screenshot templates, non-Chinese screenshots, or
  fully automatic hero recognition.
- Defining Play Store release criteria.

## Acceptance Criteria

- The repository has a release-specific verification matrix for the first
  GitHub alpha release.
- The release gate reports ready only when every required alpha release input
  is satisfied.
- Missing, failed, or skipped checks remain visible and blocking.
- Signed-artifact readiness and real-device critical-path confirmation are both
  required before the candidate is considered release-ready.
- The gate respects the documented alpha scope and known limitations instead of
  inventing stronger release promises.

## Gotchas

- Do not reuse generic app-readiness checks as if they were a complete release
  gate. A runnable app target and a publishable alpha release are related but
  not identical contracts.
- 2026-04-15: Keep skipped emulator or manual-device checks distinct from
  generic missing checks in the gate output. Maintainers need to see whether
  the alpha candidate was never verified or was explicitly deferred.
