# Release Signing And Artifact Path Spec

## Goal and Context

Define the release artifact path for GitHub distribution so the first GitHub
release publishes a signed, user-intended Android artifact rather than an
ad-hoc debug or manually chosen file.

This feature covers the release artifact contract and signing expectations, not
the GitHub publication step itself.

## Concrete Examples

### Example 1: Maintainer Produces A Release Candidate

Input:
- A maintainer prepares the first alpha release candidate.

Expected behavior:
- The repository has a defined release artifact intended for users.
- The artifact is produced through the release path rather than a debug-only
  path.
- The artifact is signed according to the defined release contract.

### Example 2: Signing Inputs Are Missing

Input:
- A release build is requested.
- Required signing inputs are not available.

Expected behavior:
- The release path does not silently fall back to an unsigned or misleading
  user artifact.
- The missing signing inputs remain a blocker for the intended signed release
  artifact.

### Example 3: Debug Artifact Exists But Release Artifact Does Not

Input:
- The repository can build debug output.
- The intended signed release artifact is missing or unverified.

Expected behavior:
- The release process does not treat the debug output as the first release
  artifact by default.
- Only the defined release artifact path is eligible for first-release
  distribution.

## Requirements

### Release Artifact Contract

- The repository MUST define the Android artifact intended for first-release
  GitHub distribution.
- The intended user artifact MUST come from a release build path rather than a
  debug-only path.
- The release artifact contract MUST make it clear which output is eligible for
  GitHub release publication.

### Signing Expectations

- The intended first-release artifact MUST be signed according to the defined
  release contract.
- The release path MUST NOT silently substitute an unsigned, debug-only, or
  otherwise unintended artifact while presenting it as the signed release
  build.
- If signing inputs are missing, the release-signing path MUST fail closed for
  the intended signed artifact.

### Verification

- The release artifact path MUST include explicit verification that the intended
  artifact was produced successfully.
- The release artifact path SHOULD preserve traceability between the produced
  artifact and the repository version being prepared for release.

## Interface Expectations

- A maintainer should be able to tell:
  - which artifact is meant for users
  - whether it was produced through the release path
  - whether the signing path succeeded

## Error-State Expectations

- If signing inputs are unavailable, the first-release signed artifact MUST NOT
  be treated as ready.
- If the release build path produces a different artifact than the one intended
  for users, that mismatch MUST block first-release publication until resolved.
- If only a debug artifact is available, the repository MUST NOT present it as
  the intended first signed release.

## Edge Cases

- A maintainer can assemble debug successfully but cannot produce the signed
  release artifact.
- The signed artifact exists, but its origin is unclear relative to the
  repository version being released.
- Multiple Android outputs exist, but only one should be considered the
  first-release user artifact.

## Non-Goals

- Publishing the GitHub release page.
- Defining GitHub release notes content.
- Expanding runtime feature scope.
- Play Store signing or store distribution workflows.

## Acceptance Criteria

- The repository defines the intended signed first-release Android artifact.
- The release path does not treat debug or unsigned output as an acceptable
  substitute.
- Maintainers can identify whether the signed release artifact was produced
  successfully.
- The artifact path is suitable to feed the GitHub release publication step.

## Gotchas

- The signed first-release artifact should fail closed when signing inputs are
  missing. Do not silently downgrade to a debug or unsigned artifact just
  because `assembleDebug` still works.
