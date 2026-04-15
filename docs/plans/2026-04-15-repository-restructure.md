# Repository Restructure

## Metadata

- Status: active
- Created: 2026-04-15
- Updated: 2026-04-15
- Owner: Codex
- Related spec(s): none; internal refactor only
- Supersedes / Superseded by: none
- Branch / PR: `chore/restructure-repository-layout`
- Last verified commands:
  - `./gradlew.bat --no-daemon :core:test`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

Align the repository with the new operating rules in `AGENTS.md` without
changing app behavior. The repository should have:

- a real `docs/plan.md` index plus a concrete plan file under `docs/plans/`
- shared Kotlin sources living inside the `core` module instead of a root
  `src/` tree wired through custom source sets
- local workspace noise ignored so reviewable changes stand out

Success means a new contributor can discover the active plan, the `core`
module looks like a normal Gradle module, and the app still builds/tests the
same way as before.

## Context and orientation

- `AGENTS.md` now says `docs/plan.md` is only an index and every approved
  initiative gets its own file under `docs/plans/`.
- `core/build.gradle.kts` currently remaps `sourceSets` to `../src/main/kotlin`
  and `../src/test/kotlin`, so the shared module still reads code from the
  repository root.
- `docs/roadmap.md` and `WORKFLOW.md` were added locally as part of the new
  repository-management direction.
- `docs/workflows.md` remains the runtime flow document and should stay in
  `docs/`.

Key files/components:

- [AGENTS.md](/D:/Data/20260413-kings-metric/AGENTS.md)
- [docs/plan.md](/D:/Data/20260413-kings-metric/docs/plan.md)
- [docs/workflows.md](/D:/Data/20260413-kings-metric/docs/workflows.md)
- [WORKFLOW.md](/D:/Data/20260413-kings-metric/WORKFLOW.md)
- [docs/roadmap.md](/D:/Data/20260413-kings-metric/docs/roadmap.md)
- [core/build.gradle.kts](/D:/Data/20260413-kings-metric/core/build.gradle.kts)
- [src](/D:/Data/20260413-kings-metric/src)

## Constraints

- Do not change externally observable app behavior.
- Do not rename packages unless required by the build; this is a layout
  restructure, not a domain rewrite.
- Keep the runtime flow document at `docs/workflows.md`.
- Keep the repository local-first; no telemetry or support features belong in
  this refactor.
- Preserve existing git history as much as practical through file moves.

## Done when

- `docs/plan.md` acts as an index and points to this active plan file.
- `core` sources and tests live under `core/src/...` instead of the root
  `src/` tree.
- `core/build.gradle.kts` no longer needs the `../src/...` source-set hack.
- local scratch/cache patterns that repeatedly appear in this workspace are
  ignored.
- `:core:test` and `:app:assembleDebug` still pass.

## Milestones

### Milestone 1: Plan-system alignment

Scope:
- add a concrete plan file for this initiative
- update `docs/plan.md` index

Files/components:
- `docs/plan.md`
- `docs/plans/2026-04-15-repository-restructure.md`

Dependencies: none
Risk: low
Validation commands: none beyond file inspection
Expected observable result: active plan can be found from the index

### Milestone 2: Core module source relocation

Scope:
- move shared Kotlin production/test sources from root `src/` into `core/src/`
- simplify `core/build.gradle.kts`

Files/components:
- `src/main/kotlin/**`
- `src/test/kotlin/**`
- `core/src/main/kotlin/**`
- `core/src/test/kotlin/**`
- `core/build.gradle.kts`

Dependencies: milestone 1
Risk: medium
Validation commands:
- `./gradlew.bat --no-daemon :core:test`
- `./gradlew.bat --no-daemon :app:assembleDebug`
Expected observable result: shared code builds from the `core` module directly

### Milestone 3: Workspace hygiene and documentation alignment

Scope:
- track the new roadmap/process files in their intended locations
- ignore repeated local scratch/cache artifacts

Files/components:
- `.gitignore`
- `docs/roadmap.md`
- `WORKFLOW.md`

Dependencies: milestone 1
Risk: low
Validation commands:
- `git status --short`
Expected observable result: local review noise is reduced and the new docs are
  part of the repository structure

## Progress

- [x] 2026-04-15: Inspected updated `AGENTS.md`, `docs/plan.md`, module build
  files, and current repository tree.
- [x] 2026-04-15: Added this concrete plan file and updated `docs/plan.md`
  to index it as active.
- [x] 2026-04-15: Moved shared Kotlin sources and tests from root `src/` into
  `core/src/`.
- [x] 2026-04-15: Removed the `core` source-set remap and verified
  `:core:test` plus `:app:assembleDebug`.
- [x] 2026-04-15: Aligned ignore/documentation files with the new structure by
  tracking `docs/roadmap.md`, keeping `docs/workflows.md` as the runtime flow
  doc, and ignoring local scratch/cache artifacts.

## Surprises & Discoveries

- The updated `AGENTS.md` still references `docs/workflows.md`, while the
  workspace also contains a new root `WORKFLOW.md` for process guidance. Both
  need to coexist without confusing the runtime flow doc.
- The root `src/` tree is still actively used by the `core` module through a
  custom `sourceSets` configuration, so the repository already has the right
  logical module but the wrong physical layout.

## Decision Log

- Decision: keep `docs/workflows.md` as the runtime-flow document and treat
  `WORKFLOW.md` as a separate process guide.
  - Rationale: this matches the new `AGENTS.md` precedence and avoids breaking
    existing references to runtime flow documentation.
  - Date/Author: 2026-04-15 / Codex

## Validation and Acceptance

Run from repository root:

- `./gradlew.bat --no-daemon :core:test`
- `./gradlew.bat --no-daemon :app:assembleDebug`

Success looks like:

- shared Kotlin logic compiles from `core/src/`
- app still resolves `project(":core")`
- no custom root `src/` remap is needed

## Validation Notes

- `./gradlew.bat --no-daemon :core:test` passed after moving shared sources
  into `core/src/`.
- `./gradlew.bat --no-daemon :app:assembleDebug` passed with the app still
  consuming `project(":core")` after the source relocation.

## Idempotence and Recovery

- File moves are safe to rerun by syncing to git state and repeating the move.
- If the build fails after source relocation, restore the previous file paths
  from git and reapply the move more narrowly.
- `:app:assembleDebug` is the minimum recovery check because the app depends on
  the `core` module output.

## Outcomes & Retrospective

- The repository now matches the updated plan-file policy: `docs/plan.md` is an
  index and this file is the concrete active plan.
- The `core` module now has a normal Gradle source layout and no longer relies
  on a root `src/` directory outside the module.
- Local scratch artifacts are ignored so future diffs should be easier to
  review.
