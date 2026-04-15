# GitHub Release Execution Test Spec

## Scope

This test spec covers publication readiness for the first GitHub alpha
prerelease after the signing path, docs contract, automated release workflow,
and release gate already exist.

## Unit Tests

- `T1` Release publication contract reports ready only when the release channel
  stays prerelease, the artifact is ready, release-facing docs are present, and
  the release gate is ready.
- `T2` Release publication contract blocks when the release gate is blocked.
- `T3` Release publication contract blocks when the release artifact is not
  ready or does not match the intended release artifact path.
- `T4` Release publication contract blocks when release notes or changelog
  linkage is missing.
- `T5` Release publication contract keeps the documented manual-hero alpha
  limitation non-blocking.

## Integration Tests

- `IT1` The current alpha metadata, artifact contract, release notes, and root
  changelog can form a publishable prerelease plan when the release gate is
  ready.
- `IT2` The GitHub release workflow requires explicit release-gate
  confirmation inputs before publication and preserves prerelease publication.
- `IT3` The release workflow stays aligned with the tracked metadata and
  release notes path for the current alpha tag.

## What Not To Test

- Actual GitHub API publication.
- Real signing-secret presence on CI.
- Play Store release behavior.

## Coverage Map

- Prerelease-only publication covered by `T1`, `IT2`
- Release-gate blocking covered by `T1`, `T2`
- Artifact readiness and path identity covered by `T1`, `T3`
- Release notes/changelog linkage covered by `T4`, `IT1`, `IT3`
- Alpha manual-hero limitation handling covered by `T5`, `IT1`
- Workflow gate-confirmation behavior covered by `IT2`

## Not Directly Testable

- Whether a maintainer’s manual device confirmation is truthful beyond the fact
  that the publication path requires that confirmation explicitly.
