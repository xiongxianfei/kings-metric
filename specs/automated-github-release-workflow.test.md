# Automated GitHub Release Workflow Test Spec

## Scope

This test spec covers the automated GitHub release workflow for the first
release path, including prerelease-only behavior, required-input validation,
artifact selection, and fail-closed publication safety.

## Unit Tests

- `T1` Repository defines an automated workflow dedicated to the first-release
  path.
- `T2` Workflow publication mode for the first release is prerelease rather
  than stable.
- `T3` Workflow consumes the defined signed user artifact rather than an
  arbitrary build output.
- `T4` Workflow requires release metadata inputs needed for first-release
  publication.
- `T5` Workflow blocks publication when the intended signed artifact is missing.
- `T6` Workflow blocks publication when required release metadata inputs are
  missing or incomplete.
- `T7` Workflow rejects unsigned, unverified, or stable-labeled first-release
  publication attempts.
- `T8` Workflow preserves traceability between published release, attached
  artifact, and repository version/tag being released.

## Integration Tests

- `IT1` Valid first-release inputs produce a prerelease-ready workflow outcome
  with the intended signed artifact selected for publication.
- `IT2` Missing signed artifact input causes the workflow to stop without
  publishing.
- `IT3` Stable-first-release metadata causes the workflow to stop without
  publishing.
- `IT4` Workflow does not accept artifact output that differs from the defined
  signed user artifact contract.
- `IT5` Workflow output is compatible with the GitHub release execution
  contract, including prerelease labeling and artifact traceability.

## What Not To Test

- README content itself.
- Signing implementation internals.
- Separate device verification matrix logic.

## Coverage Map

- Workflow existence covered by `T1`
- Prerelease-only behavior covered by `T2`, `T7`, `IT1`, `IT3`
- Artifact selection covered by `T3`, `T5`, `IT2`, `IT4`
- Input validation covered by `T4`, `T5`, `T6`, `IT2`, `IT3`
- Traceability covered by `T8`, `IT5`
- Fail-closed publication safety covered by `T5`, `T6`, `T7`, `IT2`, `IT3`,
  `IT4`

## Not Directly Testable

- GitHub-hosted runner reliability beyond whether the workflow definition
  encodes the intended safety and publication rules.
