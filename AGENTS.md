# AGENTS.md

## Mission

This repository builds an Android app that extracts Honor of Kings personal match data from screenshots. The app is local-first: users import a screenshot from device storage, the app processes it on-device, the user reviews the extracted data, and the app saves both the original screenshot and the confirmed structured record locally.

Agents working in this repository should optimize for correctness, explicitness, and spec compliance over speed or speculative feature work.

## Instruction Precedence

When instructions conflict, follow this order:

1. Direct user request
2. Current feature spec in `specs/`
3. Matching test spec in `specs/`
4. `docs/plan.md`
5. This file

Do not silently resolve conflicts between these sources. Call out the conflict and ask for direction if the correct behavior is not already implied by the higher-priority source.

## Repository Context

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

## Product Constraints

- Reliability over automation. If extraction is uncertain, surface it for review instead of guessing.
- Local-first by default. Do not add server-side OCR, sync, or cloud dependencies unless explicitly required by spec.
- Strict template support. Unsupported layouts, languages, or regional variants must be rejected clearly.
- User review is required before final save.
- Do not invent data. Only normalize values that are explicitly visible in the screenshot when the spec allows it.

## Required Working Flow

Before implementing a feature:

1. Read `docs/plan.md`.
2. Read the relevant feature spec in `specs/<feature>.md`.
3. Read the matching test spec in `specs/<feature>.test.md` when it exists.
4. Inspect the files that will be touched.
5. Confirm the request does not conflict with the current spec.

Implementation flow:

1. Write or update tests first.
2. Run the smallest relevant test scope and confirm failure for the expected reason.
3. Implement the minimum code required to satisfy the spec.
4. Re-run the relevant tests.
5. Expand verification only as needed.

If a required spec does not exist, do not invent a full feature contract in code. Ask for or create the missing spec first if the user requests that workflow.

## Spec Conventions

- `docs/plan.md` is the implementation plan and build-order source.
- `specs/<feature>.md` defines requirements, examples, edge cases, non-goals, and acceptance criteria.
- `specs/<feature>.test.md` defines the required coverage for that feature.
- Every MUST in a spec should map to at least one test.
- Every named edge case should map to at least one test.
- Code should follow the spec, not old chat context or inferred product wishes.

## Architecture Rules

- Keep business rules out of Compose UI code.
- Keep parsing, validation, normalization, and save rules in testable Kotlin classes.
- Expose UI state explicitly from `ViewModel`s.
- Prefer unidirectional data flow: events in, state out.
- Keep OCR/template recognition logic separate from persistence logic.
- Keep file storage separate from Room entities, DAOs, and repositories.
- Keep dashboard and metrics calculations out of UI code.
- Avoid premature module splitting, but preserve boundaries so modularization remains easy later.

## Recognition Pipeline Rules

- v1 supports only the approved Chinese screenshot template.
- Validation must verify required anchors before a screenshot is considered supported.
- Unsupported languages or regional variants must be rejected.
- Parsing must use only values visible on the screenshot.
- Low-confidence, missing, or invalid values must be flagged for review.
- Do not derive non-visible fields from other fields unless the spec explicitly permits that exact normalization.
- Do not silently coerce unsupported screenshots into partial supported records.

## Data and Persistence Rules

- Save the original screenshot locally when import begins.
- Preserve stable linkage between the stored screenshot and the saved record.
- Final records must not be saved if required fields are unresolved.
- Optional fields may remain empty only when the spec allows it.
- If screenshot storage succeeds and record persistence fails, report the failure clearly and keep state consistent with the spec.
- Do not add destructive cleanup behavior unless it is explicitly specified and tested.

## Testing Expectations

- Prefer JVM tests for parser, validator, normalizer, save validation, and other pure logic.
- Use instrumented tests for Room, file persistence integration, and Android framework-dependent behavior.
- Use Compose UI tests for import, review, edit, and confirmation flows.
- Add or update regression fixtures when changing validation or parsing behavior.
- When fixing a bug, add or update a regression test first when feasible.
- Do not report success without stating what was actually verified.

## Code Quality Rules

- Make the smallest change that fully satisfies the spec.
- Prefer explicit names over clever abstractions.
- Add comments only where they reduce real ambiguity.
- Avoid speculative generalization for unsupported templates, languages, or workflows.
- Do not add libraries without a clear need tied to the current plan or spec.
- Keep ASCII by default unless the file already requires non-ASCII content.

## Kotlin and Android Conventions

- Use `camelCase` for functions and variables.
- Use `PascalCase` for classes, objects, and Composables.
- Avoid wildcard imports.
- Prefer immutable data structures and explicit state models.
- Use `sealed` hierarchies for closed UI or domain state models when variants carry different data.
- Keep validation close to the domain or `ViewModel` layer, not buried in composables.

## Repository Layout Guidance

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

## Change Management

- Do not rewrite `docs/plan.md` or specs unless the user asks for it.
- Do not revert user changes unless explicitly asked.
- If a spec gap blocks safe implementation, state the gap explicitly.
- If a user request conflicts with the current spec, ask whether the spec should change or the implementation should intentionally diverge.
- Remove or challenge stale instructions when they no longer match the actual repository.

## Non-Goals for v1

- Multiple screenshot templates
- Non-Chinese screenshot support
- Server-side OCR
- Cloud sync or account systems
- Real-time match capture
- Automatic bulk reprocessing of old screenshots after parser changes

## Definition of Done

A feature is not done unless all of the following are true:

- The implementation matches the current spec.
- Relevant tests exist and pass, or any inability to run them is stated clearly.
- Named failure paths and edge cases from the spec are handled.
- The user-visible behavior does not silently exceed the agreed scope.
- Any meaningful assumptions or spec gaps are called out in the final response.

## Code Readability Rules

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
