# AGENTS.md

This repository builds an Android app that extracts Honor of Kings personal match data from screenshots. The app is local-first: users import a screenshot from device storage, the app processes it on-device, the user reviews the extracted data, and the app saves both the original screenshot and the confirmed structured record locally.

Optimize for correctness, explicitness, and spec compliance over speed or speculative feature work.

## Instruction precedence

When instructions conflict, follow this order:

1. Direct user request
2. Approved feature spec in `specs/`
3. Matching test spec in `specs/`
4. Active execution plan file in `docs/plans/`
5. `docs/workflows.md`
6. This file

Do not silently resolve conflicts between higher-priority sources. Call out the conflict, state the impact, and ask for direction only if the higher-priority source does not already imply the answer.

## Repository context

- Platform: Android
- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Architecture: MVVM with `ViewModel` + `StateFlow`
- Dependency injection: Hilt with KSP
- Structured storage: Room
- Small local config/state: Proto DataStore
- OCR: ML Kit Text Recognition v2
- Processing mode: on-device only
- Initial locale support: Simplified Chinese screenshots only
- Initial template support: one post-match personal-stats detailed-data screenshot layout only

## Product constraints

- Reliability over automation. If extraction is uncertain, surface it for review instead of guessing.
- Local-first by default. Do not add server-side OCR, sync, or cloud dependencies unless a spec explicitly requires them.
- Strict template support. Unsupported layouts, languages, or regional variants must be rejected clearly.
- User review is required before final save.
- Do not invent data. Only normalize values that are explicitly visible in the screenshot when the spec allows it.
- Final saved records must preserve stable linkage to the original screenshot.
- Local persistence failures must fail closed and keep the user’s data intact.

## Planning and workflow

Use a plan first for work that is multi-file, risky, ambiguous, architecture-affecting, migration-heavy, or large enough that it should be split into reviewable PR-sized milestones.

Use the standard workflow for behavior-changing feature work:

`plan -> spec -> spec-review -> test-spec -> implement -> code-review`

Add `plan-review` before spec work when the task is risky, cross-cutting, or hard to sequence cleanly.

Add `learn` after implementation or review when the same mistake appears twice, a new durable convention emerges, or `docs/workflows.md` no longer matches reality.

Use `bugfix` for bugs. Use `ci` for scoped GitHub Actions work. Use `pr` only when the branch is already ready for review.

## Plan file policy

- `docs/roadmap.md` stores future ideas and unapproved work.
- `docs/plan.md` is an index of active and closed execution plans. It is not the body of the plan.
- Every approved initiative gets its own living plan file under `docs/plans/YYYY-MM-DD-slug.md`.
- Never overwrite an older plan when starting a new initiative.
- If a new plan replaces an older one, keep the old file and mark it as superseded.
- Specs and reviews must reference the concrete plan file they are based on.
- Execution plans should follow `.codex/PLANS.md`.

## Required reading before implementation

Before implementing a behavior-changing feature:

1. Read `docs/plan.md`, then open the concrete active plan file in `docs/plans/`.
2. Read the relevant feature spec in `specs/<feature>.md`.
3. Read the matching test spec in `specs/<feature>.test.md` when it exists.
4. Read `docs/workflows.md` when the feature touches an existing flow or handoff.
5. Inspect the files that will be touched.
6. Confirm the request does not conflict with the current spec or plan.

If a required spec does not exist and the work changes externally observable behavior, do not invent the contract in code. Create or request the missing spec first.

## Spec and test conventions

- `specs/<feature>.md` defines the contract: requirements, examples, edge cases, non-goals, compatibility expectations, and acceptance criteria.
- `specs/<feature>.test.md` defines requirement-to-test coverage for that feature.
- Every `MUST` in a spec should map to at least one test.
- Every named edge case should map to at least one test.
- The test spec does not override the feature spec; it operationalizes it.
- Code should follow the current spec, not old chat context or inferred product wishes.

## Implementation rules

- Work from the active execution plan milestone by milestone.
- Keep diffs scoped. Do not add non-goal features while implementing a milestone.
- Write or update tests first for the milestone you are changing.
- Run the smallest relevant verification scope first, then expand only as needed.
- If validation fails after a milestone, stop and fix the failure before moving on.
- Update the active plan’s `Progress`, `Decision Log`, `Surprises & Discoveries`, and `Validation Notes` as work proceeds.
- If a spec gap blocks safe implementation, state it explicitly instead of silently guessing.

## Architecture rules

- Keep business rules out of Compose UI code.
- Keep parsing, validation, normalization, and save rules in testable Kotlin classes.
- Expose UI state explicitly from `ViewModel`s.
- Prefer unidirectional data flow: events in, state out.
- Keep OCR and template recognition logic separate from persistence logic.
- Keep file storage separate from Room entities, DAOs, and repositories.
- Keep dashboard and metrics calculations out of UI code.
- Avoid premature module splitting, but preserve boundaries so future modularization stays easy.

## Recognition and persistence rules

- v1 supports only the approved Chinese screenshot template.
- Validation must verify required anchors before a screenshot is considered supported.
- Unsupported languages or regional variants must be rejected.
- Parsing must use only values visible on the screenshot.
- Low-confidence, missing, or invalid values must be flagged for review.
- Do not derive non-visible fields from other fields unless the spec explicitly permits that exact normalization.
- Do not silently coerce unsupported screenshots into partial supported records.
- Save the original screenshot locally when import begins.
- Final records must not be saved if required fields are unresolved.
- Optional fields may remain empty only when the spec allows it.
- Do not add destructive cleanup behavior unless it is explicitly specified and tested.

## Verification expectations

- Prefer JVM tests for parser, validator, normalizer, save validation, and other pure logic.
- Use instrumented tests for Room, file persistence integration, and Android framework-dependent behavior.
- Use Compose UI tests for import, review, edit, and confirmation flows.
- When fixing a bug, add or update a regression test first when feasible.
- Do not report success without stating what was actually verified.
- For generated-code Android features, verify at least `./gradlew :app:assembleDebug` so missing `_Impl` or generated DI classes fail before runtime.
- If the exact command set is unclear, inspect existing Gradle tasks or GitHub Actions workflows and record the concrete commands in the active plan before proceeding.

## Android and Kotlin conventions

- Use `camelCase` for functions and variables.
- Use `PascalCase` for classes, objects, and Composables.
- Avoid wildcard imports.
- Prefer immutable data structures and explicit state models.
- Use `sealed` hierarchies for closed UI or domain state models when variants carry different data.
- Keep validation close to the domain or `ViewModel` layer, not buried in composables.
- If an Android route depends on in-memory user progress, do not keep that state in plain `remember` alone. Use `rememberSaveable`, `SavedStateHandle`, or another explicit state owner that survives activity recreation.
- If a route-scoped `ViewModel` edits a draft or form state that the shell also needs to recover after activity recreation, push those edits back into the shell-owned saveable state as they happen.
- If an Android flow depends on framework UI that is not deterministic in tests, add the narrowest possible test seam at the app-shell boundary. Do not bypass the business flow itself just to make instrumentation easier.
- For Android screenshot flows, do not fully decode the original screenshot bitmap just to validate file readability or render a small preview. Use bounds-only decode for validation and downsampled decode for previews.
- If a loaded Android screen can grow taller than one phone-sized portrait
  viewport, do not render it in a plain `Column` with no vertical scroll path.
  Use `verticalScroll` or a lazy container and keep one focused constrained-
  height Compose regression that proves lower sections remain reachable.

## CI conventions

- Prefer `pull_request` plus `push` on `main` only for feature-scoped GitHub Actions workflows.
- Add `concurrency` for expensive Android jobs so stale emulator runs are cancelled.
- Keep PR workflows fast. If an emulator-backed check materially exceeds the normal review feedback budget, move it to a scheduled or manual smoke workflow unless the feature explicitly requires it on the PR path.
- When adding Room, Hilt, or other generated-code Android integrations, wire the required KSP or compiler dependency in the owning Android module before treating the feature as runnable.
- If the current toolchain uses AGP built-in Kotlin with KSP, preserve compatibility properties until the toolchain is upgraded and re-verified without them.

## Repository layout guidance

Preferred direction as the app grows:

- `app/` for Android app wiring, entry points, and navigation
- `core/model/` for shared domain models
- `core/recognition/` for template validation, OCR orchestration, parsing, and confidence handling
- `data/local/` for Room entities, DAOs, repositories, and file storage
- `feature/import/` for import and review flows
- `feature/history/` for saved match browsing
- `feature/dashboard/` for metrics
- `docs/` for planning documentation
- `specs/` for feature specs and test specs

Agents may keep a simpler structure while the codebase is small, but should preserve boundaries that match this direction.

## Change management

- Do not rewrite a plan, spec, or workflow file unless the task actually requires it.
- Do not revert user changes unless explicitly asked.
- If a user request conflicts with the current spec, ask whether the spec should change or the implementation should intentionally diverge.
- Remove or challenge stale instructions when they no longer match the actual repository.
- Keep `AGENTS.md` practical. When guidance becomes flow-specific, prefer `docs/workflows.md`. When it becomes feature-specific, prefer the relevant spec.

## Non-goals for v1

- Multiple screenshot templates
- Non-Chinese screenshot support
- Server-side OCR
- Cloud sync or account systems
- Real-time match capture
- Automatic bulk reprocessing of old screenshots after parser changes

## Definition of done

A feature is not done unless all of the following are true:

- The implementation matches the current spec.
- Relevant tests exist and pass, or any inability to run them is stated clearly.
- Named failure paths and edge cases from the spec are handled.
- The user-visible behavior does not silently exceed the agreed scope.
- The active plan is updated to reflect what actually happened.
- Any meaningful assumptions or spec gaps are called out in the final response.

## Code readability rules

### Functions

- Prefer small functions that do one thing clearly.
- Treat roughly 50 lines as a review signal, not a hard limit. Extract only when it improves clarity.
- Avoid boolean parameters when they obscure intent. Prefer separate named functions or a small sealed/value type when that reads better.
- Name functions after the action they perform, not the implementation detail.

### Naming

- Variables should describe the value they hold, not repeat the type.
- Prefer `sessions` over `sessionList`, `isValid` over `validFlag`.
- Prefer clear verb-led function names such as `validateInput()` over vague names such as `check()`.
- Avoid abbreviations unless they are standard in the codebase or broadly universal, such as `id`, `url`, or `db`.

### Structure

- Prefer one primary class, interface, or object per file when practical.
- File names should match the main type they contain.
- Keep public API near the top and private helpers near the bottom when that improves scanability.
- Group related behavior together. Add a short section comment only when it reduces real ambiguity.

### Comments

- Do not add comments that merely restate the code.
- Add comments for non-obvious business rules, invariants, edge-case handling, or justified workarounds.
- When documenting a workaround or limitation, include the reason and a concrete reference when one exists.
