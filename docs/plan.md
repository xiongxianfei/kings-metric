# Honor of Kings Match Tracker Plan

## Objective

Ship the first GitHub release using the smallest safe scope:

- publish a usable Android build for real users
- keep the release local-first and within the current supported screenshot path
- document known limitations instead of hiding them
- use a prerelease channel until runtime confidence is stronger
- make release publication repeatable instead of manual-only

## Release Recommendation

The first GitHub release should be an `alpha` prerelease, not `v1.0.0`.

Why:

- the main import -> review -> save path works
- the app is already runnable on Android and manually testable
- hero extraction is still not reliably automatic on the supported fixture
- real-device coverage is still limited enough that a stable release would
  overstate confidence

Recommended first tag shape:

- `v0.1.0-alpha.1`

## How To Read This Plan

Use this file for:

- current release status
- release-scope features
- build order
- release gates
- explicit non-goals

Use other docs for detail:

- runtime/system flow: [workflows.md](./workflows.md)
- feature contracts: [specs/](../specs/)

## Current Status

Done:

1. runnable Android app foundation
2. import -> review -> save runtime flow
3. Room-backed local persistence and history/dashboard screens
4. first-pass UX polish across import, review, history, detail, and dashboard
5. focused JVM, Compose, and selected emulator regression coverage

Current release blockers:

- README does not yet promise both developer build/run instructions and
  first-release user install guidance as one maintained contract
- no explicit GitHub repo description/tagline source or update path exists yet
- no release-signing path for the distributed Android artifact is defined yet
- no automated GitHub release workflow exists yet
- no explicit changelog/release-notes policy is defined for releases
- hero extraction still commonly falls back to manual review on the supported
  screenshot
- device verification is good enough for alpha planning, not yet for a stable
  public release claim

## Release Scope

The first GitHub release should promise only:

- one supported Simplified Chinese post-match detailed-data screenshot
- local screenshot import
- on-device processing only
- required user review before final save
- local history/detail/dashboard browsing

The first GitHub release should explicitly document:

- hero may still require manual entry during review
- unsupported screenshots are rejected
- this is an alpha-quality release intended for early validation

## Feature List

### 1. Release Metadata And Positioning

- Scope: define first-release versioning, prerelease wording, repository
  description/tagline, and the supported-scope statements that must stay
  consistent across GitHub surfaces
- Dependencies: none
- Risk: low
- Size: small

### 2. README Build Run And Install Guidance

- Scope: maintain a repository README that explains what the app does, who the
  alpha release is for, how to build and run the project locally, how to
  install the release artifact, privacy/local-first behavior, and current known
  limitations
- Dependencies: 1
- Risk: low
- Size: small

### 3. Root Changelog And Release Notes Contract

- Scope: define root `CHANGELOG.md` as the primary tracked changelog source
  using a conventional release-entry format such as
  `## [v0.1.0-alpha.1] - YYYY-MM-DD`, with clear sections like `Features`,
  `Bug Fixes`, and `Internal`; derive per-release notes from that source so
  each GitHub release has a user-facing change summary and known limitations
  statement
- Dependencies: 1
- Risk: low
- Size: small

### 4. Release Signing And Artifact Path

- Scope: define the signed release artifact path for GitHub distribution,
  including signing inputs, release-build verification, and the exact artifact
  intended for users
- Dependencies: 1
- Risk: medium
- Size: medium

### 5. Automated GitHub Release Workflow

- Scope: add a repeatable GitHub workflow that builds the intended release
  artifact, applies the release metadata, and publishes a prerelease only when
  the required inputs are present
- Dependencies: 1, 3, 4
- Risk: medium
- Size: medium

### 6. Alpha Hardening For Critical User Path

- Scope: fix the highest-risk release-facing issues on the supported
  import -> review -> save -> history flow without expanding product scope
- Dependencies: 1, 4
- Risk: medium
- Size: medium

### 7. Device Verification And Release Gate

- Scope: define and run a small release-candidate verification matrix, then
  codify the minimum pass criteria that the automated/manual release path must
  respect before publishing the alpha build
- Dependencies: 2, 3, 4, 6
- Risk: medium
- Size: small

### 8. GitHub Release Execution

- Scope: publish the first prerelease with the signed user artifact, final
  changelog/release notes, and matching repository metadata after the release
  gate passes
- Dependencies: 2, 3, 5, 7
- Risk: low
- Size: small

## Build Order

1. Release Metadata And Positioning
2. README Build Run And Install Guidance
3. Changelog And Release Notes Contract
4. Release Signing And Artifact Path
5. Automated GitHub Release Workflow
6. Alpha Hardening For Critical User Path
7. Device Verification And Release Gate
8. GitHub Release Execution

## Architecture Decisions

- Treat the first GitHub release as a distribution milestone, not as permission
  to expand template, language, or cloud scope.
- Keep alpha hardening focused on the existing supported path instead of mixing
  it with new recognition systems or broad refactors.
- Prefer explicit known-limitation documentation over weak or hidden fallback
  behavior.
- Keep repository metadata, README messaging, root `CHANGELOG.md`, and GitHub
  release notes aligned to one supported scope statement so release surfaces do
  not drift.
- Keep `CHANGELOG.md` human-scannable and release-oriented:
  - one dated entry per release
  - newest release first
  - stable section headings for feature, fix, and internal changes when used
  - avoid burying release-facing changes only in PR history
- Treat the GitHub repo description as managed release metadata even though it
  is not stored as app runtime code.
- Keep emulator/device verification intentional and limited to the critical
  path so release gating stays reviewable.

## Release Gates

Do not publish the first GitHub release until all of these are true:

- a signed release build can be produced repeatably from the repository
- the artifact intended for users is verified before publishing
- the repository README explains developer build/run steps, user install steps,
  supported scope, and known limitations
- repository description/tagline, root `CHANGELOG.md`, and release notes do not
  overclaim maturity or support and follow the agreed release-entry format
- the critical import -> review -> save -> history path passes the release
  candidate verification matrix
- the app does not claim stable support for hero auto-extraction if manual hero
  review is still required in common cases
- the automated release workflow cannot silently publish an unsigned,
  unverified, or stable-labeled first release

## Risks

- trying to force a stable `1.0.0` release too early will overpromise runtime
  confidence and hero extraction quality
- release signing/build work can expose Android configuration gaps that normal
  debug flows do not hit
- GitHub metadata, README copy, root `CHANGELOG.md`, and release notes can
  drift if they are not treated as one release contract
- changelog quality can degrade into inconsistent ad-hoc notes unless release
  entry format is kept explicit and stable from the first release onward
- broad hardening can sprawl unless it stays limited to the critical release
  path
- release automation can accidentally publish the wrong artifact or wrong
  channel if safeguards are too weak

## Non-Goals

- making the first GitHub release a stable `v1.0.0`
- adding new screenshot templates or non-Chinese support
- cloud sync, accounts, or server OCR
- broad recognition redesign to solve hero portrait matching before alpha
- Play Store publishing in the same phase as the first GitHub release

## Done When

This plan is done when:

- the repo can produce a verified signed first-release Android artifact
- the repository has a clear README with build/run/install guidance and
  release-facing GitHub metadata
- GitHub has a published prerelease with root changelog coverage, release
  notes, and known limitations
- root `CHANGELOG.md` uses a conventional dated release format that can scale
  across future releases without restructuring
- the supported import -> review -> save -> history flow has passed the defined
  release gate
- the automated release workflow respects the alpha-only first-release policy
