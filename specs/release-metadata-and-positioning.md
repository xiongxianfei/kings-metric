# Release Metadata And Positioning Spec

## Goal and Context

Define the first-release identity for the repository so GitHub-facing release
surfaces describe the Android app consistently and do not overstate product
maturity.

This feature covers version/channel wording, repository description/tagline,
and the shared supported-scope statements that appear across GitHub-facing
release surfaces.

## Concrete Examples

### Example 1: First Release Metadata Is Prepared

Input:
- The team is preparing the first public GitHub release.
- The app is usable but still alpha quality.

Expected behavior:
- The repository uses alpha prerelease wording instead of stable wording.
- The repository description/tagline reflects the actual product scope.
- The same supported-scope language is reused across release-facing GitHub
  surfaces.

### Example 2: Hero Extraction Is Still Limited

Input:
- The release candidate works on the supported path.
- Hero still often requires manual review.

Expected behavior:
- Release metadata does not imply fully automatic or stable extraction.
- Positioning language stays compatible with that limitation.

### Example 3: Maintainer Tries To Present The Release As Stable

Input:
- A maintainer prepares release-facing metadata for the first GitHub release.
- The metadata uses stable or production-complete wording.

Expected behavior:
- The first release metadata remains aligned to the alpha prerelease channel.
- The repository does not present the first release as broader or more mature
  than the current verified product state.

## Requirements

### Release Identity

- The first GitHub release MUST be positioned as an alpha prerelease rather
  than as a stable `1.0.0` product release.
- The repository MUST define a first-release version/tag shape that reflects
  prerelease status.
- The first-release metadata MUST identify the app as an Android app for local,
  on-device screenshot import and review.

### Supported Scope Messaging

- The repository metadata MUST describe only the currently supported scope:
  - one supported Simplified Chinese post-match detailed-data screenshot
  - local screenshot import
  - on-device processing
  - required review before final save
- The release-facing metadata MUST state or remain consistent with the fact
  that unsupported screenshots are rejected.
- The first-release metadata MUST NOT claim support for additional templates,
  non-Chinese screenshots, cloud sync, or server OCR.

### GitHub Surface Consistency

- The repository description/tagline, release positioning text, and other
  GitHub-facing summary metadata MUST describe the same supported scope and
  maturity level.
- The first-release metadata SHOULD make the intended audience clear, such as
  early testers or alpha users, when that affects expectations.

## Interface Expectations

- A user seeing the repository summary or release positioning should be able to
  understand that:
  - this is an Android app
  - it is local-first
  - it is currently alpha quality
  - it supports a narrow screenshot scope

## Error-State Expectations

- If release-facing metadata would make the repository look stable or broader
  than the verified product, that mismatch MUST be treated as a release blocker.
- If different GitHub-facing surfaces describe different support levels, the
  release metadata MUST be corrected before the first release is treated as
  ready.

## Edge Cases

- The repository description is short, but the release title or notes use much
  broader wording.
- The app is alpha quality, but the metadata uses stable-sounding language such
  as “complete” or “production ready.”
- The metadata describes “OCR match tracking” without stating the narrow
  screenshot scope.

## Non-Goals

- Writing the full README content.
- Defining the release artifact or signing implementation.
- Publishing the GitHub release itself.
- Expanding product scope beyond the current supported template.

## Acceptance Criteria

- The repository has defined first-release versioning and alpha positioning.
- GitHub-facing release metadata describes the actual supported scope without
  overclaiming maturity.
- Repository description/tagline and release-positioning text are aligned.
- The first-release positioning stays compatible with the current hero/manual
  review limitation.

## Gotchas

- None yet.
