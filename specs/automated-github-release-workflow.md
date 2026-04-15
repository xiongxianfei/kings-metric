# Automated GitHub Release Workflow Spec

## Goal and Context

Create a repeatable GitHub workflow that publishes the first-release artifact
and metadata through a controlled prerelease path instead of relying on a
manual, error-prone publication process.

This feature covers release automation behavior, not the upstream content of
the README, signing inputs, or release notes contract themselves.

## Concrete Examples

### Example 1: Valid Alpha Release Inputs Exist

Input:
- Release metadata is defined.
- The intended signed artifact is available through the release path.
- Required publication inputs are present.

Expected behavior:
- The GitHub workflow can publish the alpha prerelease using the intended
  artifact and metadata.
- The workflow produces a prerelease rather than a stable first release.

### Example 2: Signing Or Artifact Input Is Missing

Input:
- A maintainer triggers the automated release workflow.
- The intended signed artifact cannot be produced or located.

Expected behavior:
- The workflow does not publish a misleading release.
- Missing artifact/signing inputs remain a blocker instead of being silently
  bypassed.

### Example 3: Metadata Requests Stable Release Wording

Input:
- A maintainer prepares the first-release workflow inputs.
- The inputs imply a stable release rather than the planned alpha prerelease.

Expected behavior:
- The workflow does not publish the first release as stable by mistake.
- The first-release workflow remains aligned with the alpha-only release plan.

## Requirements

### Publication Behavior

- The repository MUST define an automated GitHub workflow for the first release
  path.
- The first automated GitHub release path MUST publish the first release as a
  prerelease rather than as a stable release.
- The workflow MUST use the intended signed user artifact, not an arbitrary
  build output.

### Safety And Input Validation

- The workflow MUST NOT publish the first release if required artifact or
  release inputs are missing.
- The workflow MUST fail closed when the intended signed artifact is unavailable
  or when release publication inputs are incomplete.
- The workflow MUST NOT silently publish an unsigned, unverified, or
  stable-labeled first release.

### Metadata And Traceability

- The workflow MUST apply the defined release metadata for the first release.
- The workflow MUST preserve traceability between the published release, the
  published artifact, and the repository version being released.
- The workflow SHOULD make publication mode explicit to maintainers, such as
  prerelease versus stable, when that affects safety.

## Interface Expectations

- A maintainer triggering the workflow should be able to understand:
  - whether the release is allowed to publish
  - whether required inputs are present
  - which artifact will be attached
  - that the first release remains alpha-only

## Error-State Expectations

- If the intended signed artifact is missing, the workflow MUST stop without
  publishing the release.
- If required release metadata is incomplete, the workflow MUST stop without
  publishing the release.
- If the workflow cannot preserve the planned prerelease channel, it MUST NOT
  silently publish a stable first release instead.

## Edge Cases

- The workflow is triggerable, but the repository version is not yet ready for
  publication.
- The workflow has artifact output, but it is not the defined signed user
  artifact.
- The workflow has complete artifact inputs, but release metadata still
  overclaims supported scope or maturity.
- A maintainer tries to reuse the workflow in a way that bypasses the alpha
  first-release policy.

## Non-Goals

- Writing the README or repository description content.
- Defining the signing implementation details themselves.
- Replacing the separate release gate or device verification definition.
- Play Store automation or store deployment.

## Acceptance Criteria

- The repository has an automated GitHub workflow for the first-release path.
- The workflow publishes only the intended alpha prerelease artifact and
  metadata.
- Missing required inputs block publication rather than producing a misleading
  release.
- The workflow aligns with the release-signing and GitHub release execution
  contracts.

## Gotchas

- GitHub release publication and GitHub repository-description updates may need
  different permissions. Keep repo-description sync optional so missing
  admin-scoped metadata credentials do not silently change the release
  artifact/publication contract.
