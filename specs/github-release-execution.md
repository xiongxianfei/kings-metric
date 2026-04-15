# GitHub Release Execution Spec

## Goal and Context

Publish the first GitHub release for the Android app in a way that matches the
actual product state, gives early users a usable install artifact, and does
not overstate feature maturity.

This feature covers the release publication step after the release build path,
release notes, and release gate are already defined. It is about safely
publishing the release, not about expanding product scope.

## Concrete Examples

### Example 1: First Alpha Release Is Ready

Input:
- The repository has a verified Android release artifact.
- The release gate for the critical import -> review -> save -> history path
  has passed.
- Known limitations have been documented.

Expected behavior:
- A GitHub release is created with a prerelease marker.
- The release includes the intended user-installable artifact.
- The release notes describe supported scope, install guidance, and known
  limitations.

### Example 2: Release Gate Has Not Passed

Input:
- A maintainer attempts to publish the first GitHub release.
- The candidate artifact exists, but required release-gate verification is
  incomplete or failed.

Expected behavior:
- The first GitHub release is not published as if it were ready.
- The missing or failed gate remains a blocker rather than being hidden by the
  release process.

### Example 3: Hero Still Requires Manual Review

Input:
- The release candidate is otherwise valid.
- Hero auto-extraction is still not reliable enough to remove manual review on
  common supported screenshots.

Expected behavior:
- The GitHub release still may be published as an alpha prerelease.
- The release notes clearly state that hero may still require manual entry
  during review.
- The release does not claim stable or fully automatic hero extraction.

## Requirements

### Release Channel And Versioning

- The first GitHub release MUST be published as a prerelease, not as a stable
  `1.0.0` release.
- The first GitHub release MUST use the documented alpha release channel rather
  than stable version wording.
- The published release title and tag MUST make the prerelease nature visible
  to users before they install the build.

### Release Artifact

- The GitHub release MUST include the user-intended Android install artifact.
- The published artifact MUST be the same candidate that passed the defined
  release gate.
- The release MUST NOT attach a placeholder, debug-only, or unverified artifact
  while presenting it as the intended release build.

### Release Notes And User Guidance

- The GitHub release MUST include user-facing notes that describe:
  - supported screenshot scope
  - local-first / on-device processing behavior
  - basic install guidance for the attached artifact
  - known limitations that matter on the main user path
- The release notes MUST explicitly state that unsupported screenshots are
  rejected.
- If hero still commonly requires manual review, the release notes MUST say so.
- The release notes MUST NOT claim support for new templates, non-Chinese
  screenshots, cloud sync, or other out-of-scope features.

### Publication Safety

- The first GitHub release MUST NOT be published until the defined release gate
  has passed.
- The release process MUST preserve traceability between the published release,
  the release artifact, and the repository version being released.
- The release SHOULD make the intended audience clear, such as early testers or
  alpha users, when that context affects user expectations.

## Interface Expectations

- A user landing on the GitHub release page should be able to understand:
  - that this is an alpha prerelease
  - what the app currently supports
  - how to install the provided artifact
  - what limitations to expect before using it
- A maintainer should be able to tell which artifact belongs to the published
  release and whether it satisfied the release gate.

## Error-State Expectations

- If the verified release artifact is missing, the first GitHub release MUST
  NOT be published as complete.
- If the release notes are missing supported-scope or known-limitation details,
  the first GitHub release MUST NOT be presented as ready for users.
- If a planned release candidate fails the defined release gate, the failure
  MUST block publication rather than being silently accepted.

## Edge Cases

- The artifact exists, but release notes still describe behavior that was not
  verified on the candidate build.
- The release candidate passes the critical path but still requires manual hero
  entry on common supported screenshots.
- The release page includes an artifact, but install guidance is missing or too
  vague for an early tester to use.
- A maintainer attempts to publish the release as stable wording even though
  the plan still requires an alpha prerelease.

## Non-Goals

- Defining the Android signing implementation details.
- Expanding support to additional screenshot templates or non-Chinese images.
- Play Store publication, store listing content, or store review workflows.
- Solving hero portrait matching before the first alpha release.

## Acceptance Criteria

- The first GitHub release is published as an alpha prerelease with a visible
  prerelease tag/title.
- The release page includes the verified user-intended Android artifact.
- The release notes clearly describe supported scope, install guidance, and the
  key known limitations for the current alpha build.
- The release does not overclaim hero extraction quality or general product
  maturity.
- The published release can be traced back to the verified candidate that
  passed the release gate.

## Gotchas

- The GitHub release page can be correct while the repository description is
  stale, because repository metadata is an external GitHub setting. Keep a
  tracked source of truth and an explicit sync path instead of assuming the
  release page updates that metadata automatically.
