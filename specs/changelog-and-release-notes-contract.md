# Changelog And Release Notes Contract Spec

## Goal and Context

Define a tracked changelog / release-notes contract so each GitHub release has
an explicit, user-facing summary of what changed, what is supported, and what
limitations still apply.

This feature covers the content contract and tracked source-of-truth for root
`CHANGELOG.md` plus the per-release notes derived from it. It does not cover
the GitHub publication mechanics themselves.

## Concrete Examples

### Example 1: First Alpha Release Is Prepared

Input:
- The team is preparing `v0.1.0-alpha.1`.

Expected behavior:
- Root `CHANGELOG.md` contains an entry for that version.
- The release notes summarize what the release includes and what limitations
  still apply.
- Both stay aligned with the current alpha positioning and supported scope.

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
- Root `CHANGELOG.md` makes it clear what changed in the new release instead of
  forcing the maintainer to reconstruct changes from merged PRs or memory.

## Requirements

### Root Changelog Source

- The repository MUST keep `CHANGELOG.md` at the repository root as the primary
  tracked changelog source.
- Root `CHANGELOG.md` MUST contain release-specific entries rather than a vague
  rolling note with no release boundary.
- Root `CHANGELOG.md` MUST be suitable for deriving the GitHub release notes
  for a specific release.

### Per-Release Notes Output

- Each GitHub release MUST also have release-specific notes suitable for the
  GitHub release page.
- The per-release notes MUST stay consistent with the corresponding
  `CHANGELOG.md` entry.

### Required Release Notes Content

- Root `CHANGELOG.md` release entries and per-release notes MUST describe:
  - the release identifier
  - the release maturity/channel, such as alpha prerelease
  - what the release includes at a user-facing level
  - the currently supported scope
  - known limitations that materially affect the main user path
- If hero still commonly requires manual review, both MUST say so.
- Both MUST state that unsupported screenshots are rejected.

### Changelog Expectations

- Root `CHANGELOG.md` MUST make it possible to understand what is new in the
  release without reading the full Git history.
- The changelog/release-notes contract SHOULD distinguish release scope from
  future work or out-of-scope plans.
- Neither root `CHANGELOG.md` nor per-release notes MUST overclaim support
  beyond the current verified product state.

### Consistency

- Root `CHANGELOG.md` and release notes MUST stay consistent with:
  - repository metadata/positioning
  - README supported scope
  - the current release gate and known limitations

## Interface Expectations

- A user landing on a release page should be able to understand:
  - what this specific release is
  - whether it is alpha or stable
  - what changed or is included
  - what limitations to expect
- A maintainer should be able to find:
  - root `CHANGELOG.md` as the canonical changelog source
  - the release-specific notes that back the GitHub release page

## Error-State Expectations

- If root `CHANGELOG.md` has no entry for a release, that release MUST NOT be
  treated as properly prepared.
- If either root `CHANGELOG.md` or release notes overstate support or omit a
  material known limitation, the release MUST NOT be treated as properly
  documented.
- If root `CHANGELOG.md`, release notes, README, or metadata disagree on
  supported scope, that mismatch MUST be corrected before release publication.

## Edge Cases

- Root `CHANGELOG.md` exists, but there is no entry for the current release.
- The release notes describe the correct version but do not say the release is
  alpha.
- The release notes describe installation but not what changed or what is
  included.
- Root `CHANGELOG.md` or release notes include broad aspirational roadmap
  language that makes the current release sound more capable than it is.

## Non-Goals

- Implementing the GitHub release publication workflow.
- Defining the signing path or artifact contract.
- Writing full long-form product documentation beyond release-facing change
  notes.
- Solving runtime feature limitations in this documentation feature.

## Acceptance Criteria

- The repository has root `CHANGELOG.md` with a tracked entry for the first
  release.
- The release notes clearly describe release identity, supported scope, what is
  included, and key known limitations.
- Root `CHANGELOG.md` makes release-to-release change summaries possible.
- Root `CHANGELOG.md` and release notes stay aligned with the alpha release
  positioning and current verified scope.

## Gotchas

- None yet.
