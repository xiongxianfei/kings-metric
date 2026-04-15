# Changelog And Release Notes Contract Spec

## Goal and Context

Define a tracked changelog / release-notes contract so each GitHub release has
an explicit, user-facing summary of what changed, what is supported, and what
limitations still apply.

This feature covers the content contract and tracked source-of-truth for
release notes. It does not cover the GitHub publication mechanics themselves.

## Concrete Examples

### Example 1: First Alpha Release Is Prepared

Input:
- The team is preparing `v0.1.0-alpha.1`.

Expected behavior:
- A tracked release-notes document exists for that version.
- The release notes summarize what the release includes and what limitations
  still apply.
- The notes stay aligned with the current alpha positioning and supported scope.

### Example 2: README And Release Notes Drift

Input:
- The README says the app supports one screenshot template and manual hero
  review may still be required.
- The release notes claim broader support or omit that limitation.

Expected behavior:
- The release-notes contract treats that mismatch as a release-blocking
  documentation error.

### Example 3: Maintainer Wants To Know What Changed

Input:
- A maintainer prepares a new GitHub release.

Expected behavior:
- The changelog / release-notes source makes it clear what changed in the new
  release instead of forcing the maintainer to reconstruct changes from merged
  PRs or memory.

## Requirements

### Tracked Release Notes Source

- Each GitHub release MUST have a tracked release-notes source file in the
  repository.
- The release-notes source MUST be version-specific or release-specific rather
  than a vague rolling note with no release boundary.
- The release-notes source MUST be suitable for reuse by the GitHub release
  publication step.

### Required Release Notes Content

- Release notes MUST describe:
  - the release identifier
  - the release maturity/channel, such as alpha prerelease
  - what the release includes at a user-facing level
  - the currently supported scope
  - known limitations that materially affect the main user path
- If hero still commonly requires manual review, the release notes MUST say so.
- The release notes MUST state that unsupported screenshots are rejected.

### Changelog Expectations

- The release-notes contract MUST make it possible to understand what is new in
  the release without reading the full Git history.
- The release-notes contract SHOULD distinguish release scope from future work
  or out-of-scope plans.
- The release-notes contract MUST NOT overclaim support beyond the current
  verified product state.

### Consistency

- Release notes MUST stay consistent with:
  - repository metadata/positioning
  - README supported scope
  - the current release gate and known limitations

## Interface Expectations

- A user landing on a release page should be able to understand:
  - what this specific release is
  - whether it is alpha or stable
  - what changed or is included
  - what limitations to expect
- A maintainer should be able to find the tracked source file that backs the
  GitHub release notes.

## Error-State Expectations

- If a release has no tracked release-notes source, that release MUST NOT be
  treated as properly prepared.
- If release notes overstate support or omit a material known limitation, the
  release MUST NOT be treated as properly documented.
- If the release notes and README or metadata disagree on supported scope, that
  mismatch MUST be corrected before release publication.

## Edge Cases

- The release notes describe the correct version but do not say the release is
  alpha.
- The release notes describe installation but not what changed or what is
  included.
- The release notes include broad aspirational roadmap language that makes the
  current release sound more capable than it is.
- The changelog exists only as PR history with no release-specific tracked
  summary.

## Non-Goals

- Implementing the GitHub release publication workflow.
- Defining the signing path or artifact contract.
- Writing full long-form product documentation beyond release-facing change
  notes.
- Solving runtime feature limitations in this documentation feature.

## Acceptance Criteria

- The repository has a tracked release-notes source for the first release.
- The release notes clearly describe release identity, supported scope, what is
  included, and key known limitations.
- The release-notes contract makes release-to-release change summaries possible.
- The release notes stay aligned with the alpha release positioning and current
  verified scope.

## Gotchas

- None yet.
