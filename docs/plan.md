# Honor of Kings Match Tracker Runnable Android Plan

## Objective

Turn the current repository from a Kotlin/JVM logic-and-contract codebase
into a real runnable Android application that can:

- launch on device
- let the user pick one supported screenshot
- run on-device recognition
- show a review/edit flow
- save the confirmed record plus screenshot locally
- browse saved history
- show dashboard metrics from local data

The target remains the same v1 product scope:

- one supported Simplified Chinese personal-stats screenshot template
- on-device only OCR
- local-only persistence
- required user review before final save

## Current State

The repository already has strong coverage for pure business logic and
feature contracts:

- import/validation/parsing/save workflow rules
- history and dashboard domain logic
- failure handling and regression fixtures
- JVM-testable adapter contracts for app shell, picker, ML Kit, and review

The repository is not yet a runnable Android app because it still lacks:

- Android Gradle application/module setup
- AndroidManifest and resources
- Compose, Navigation, Hilt, Room, DataStore, and ML Kit dependencies
- real Android implementations for picker, bitmap decode, OCR, Room, and UI
- instrumented tests and Compose UI tests

## Delivery Strategy

Ship the app in vertical slices that progressively replace JVM-only
contracts with real Android implementations while preserving the existing
pure logic tests.

Rules for this plan:

- keep the existing pure Kotlin business rules as the source of truth
- add Android wiring around those rules instead of rewriting them
- prefer one-way replacement of fake adapters with real implementations
- add Android tests only after the app module and runtime wiring exist

## Phase Breakdown

### Phase 0. Android Project Bootstrap

- Create an Android application module.
- Add Android Gradle configuration, manifest, namespaces, SDK levels,
  Compose compiler setup, and baseline resources.
- Add Hilt, Navigation Compose, Room, DataStore, and ML Kit dependencies.
- Add `Application` and `MainActivity` entry points.

Outcome:
- the repo builds an Android app and can launch a placeholder shell

### Phase 1. App Shell And Navigation

- Wire a real Compose root screen and navigation graph.
- Preserve the current route structure for import, review, history,
  dashboard, and record detail.
- Move the existing app-shell navigation contract into actual Android code.

Outcome:
- app launches and can navigate between placeholder destinations safely

### Phase 2. Screenshot Intake And Local Storage

- Replace the JVM picker adapter with real Photo Picker wiring.
- Implement app-managed file copy from `Uri` to local storage.
- Preserve stable screenshot identifiers and screenshot/file metadata.

Outcome:
- user can select one image and the app stores it locally for downstream work

### Phase 3. Recognition Pipeline Integration

- Replace fake bitmap/OCR adapters with Android bitmap decoding and ML Kit.
- Feed recognized results into the existing validator/parser workflow.
- Preserve unsupported, low-confidence, and unresolved-field behavior.

Outcome:
- app can produce a real draft from a supported screenshot on device

### Phase 4. Review Screen And Save Flow

- Replace the JVM review screen contract with real Compose UI plus
  `ViewModel` and `StateFlow`.
- Bind field editing, blocker visibility, screenshot preview, and save.
- Preserve edits across save validation failures and save errors.

Outcome:
- user can review, edit, and attempt save in a real Android UI

### Phase 5. Room Persistence And Repositories

- Add Room entities, DAO, database, and repositories.
- Persist confirmed records and screenshot linkage.
- Add observable flows for history/detail/dashboard consumers.

Outcome:
- confirmed records are stored locally and can be queried by the app

### Phase 6. History And Dashboard Screens

- Bind the existing history and dashboard logic to Room-backed
  repositories and Compose screens.
- Add detail screen behavior for linked screenshot viewing and
  missing-file degradation.

Outcome:
- saved records and metrics are visible in real app screens

### Phase 7. Android Test Harness And Release Readiness

- Add instrumented storage and Room tests.
- Add Compose UI tests for import/review/history/dashboard flows.
- Add baseline CI for Android unit, instrumented, and UI verification.

Outcome:
- app is runnable, testable, and stable enough for internal release

## Feature Breakdown

### 1. Android Project Bootstrap And Build Setup

- Scope: create the actual Android app module and dependency baseline.
- Dependencies: none.
- Risk: high.
- Size: medium.

### 2. Android App Shell And Navigation

- Scope: real Compose root, navigation graph, Hilt entry wiring.
- Dependencies: Feature 1.
- Risk: medium.
- Size: medium.

### 3. Android Photo Picker And File Storage Integration

- Scope: real Photo Picker result handling and local file persistence.
- Dependencies: Features 1-2.
- Risk: medium.
- Size: medium.

### 4. ML Kit Recognition Adapter And Image Region Integration

- Scope: real bitmap decode, ML Kit invocation, template-region mapping,
  and workflow handoff.
- Dependencies: Features 1-3.
- Risk: high.
- Size: large.

### 5. Compose Review Screen And ViewModel Integration

- Scope: real Compose review UI, `ViewModel`, `StateFlow`, preview state,
  editing, and save orchestration.
- Dependencies: Features 1-4.
- Risk: medium.
- Size: medium.

### 6. Room Repositories And Observed History Dashboard Integration

- Scope: Room schema, DAO, repositories, observable flows for history,
  detail, and dashboard.
- Dependencies: Features 1 and 5.
- Risk: medium.
- Size: large.

### 7. History And Dashboard Screen Binding

- Scope: real Compose screens backed by repository flows and existing
  domain logic.
- Dependencies: Features 2, 5, and 6.
- Risk: medium.
- Size: medium.

### 8. Android Test Harness And Release Readiness

- Scope: instrumented tests, Compose tests, emulator CI strategy, and
  release-blocking verification.
- Dependencies: Features 1-7.
- Risk: medium.
- Size: medium.

## Build Order

1. Android Project Bootstrap And Build Setup
2. Android App Shell And Navigation
3. Android Photo Picker And File Storage Integration
4. ML Kit Recognition Adapter And Image Region Integration
5. Compose Review Screen And ViewModel Integration
6. Room Repositories And Observed History Dashboard Integration
7. History And Dashboard Screen Binding
8. Android Test Harness And Release Readiness

## Mapping To Current Specs

Existing logic specs remain valid for business rules:

- `personal-stats-screenshot-import`
- `screenshot-import-local-file-intake`
- `recognition-pipeline-template-validation-ocr-and-parsing`
- `review-and-manual-correction-flow`
- `local-persistence-and-screenshot-linkage`
- `match-history-ui`
- `metrics-dashboard`
- `unsupported-case-and-failure-handling`
- `recognition-fixture-dataset-and-regression-checks`

Android delivery specs now refine those rules into runnable-app work:

- `android-project-bootstrap-and-build-setup`
- `android-app-shell-and-navigation`
- `android-photo-picker-and-file-storage-integration`
- `ml-kit-recognition-adapter-and-image-region-integration`
- `compose-review-screen-and-viewmodel-integration`
- `room-repositories-and-observed-history-dashboard-integration`
- `history-and-dashboard-screen-binding`
- `android-test-harness-and-release-readiness`

## Key Risks

- Gradle/app-module migration may destabilize the current JVM test setup.
- ML Kit and bitmap decode behavior may differ materially from the current
  fake adapter assumptions.
- Room/entity design may force some reshaping of current saved-record
  domain types.
- Compose UI and navigation state can easily leak business rules into UI
  code if not disciplined.
- Emulator/instrumented CI may be slow or flaky without careful scoping.

## Acceptance Direction

The plan is complete when the repository can answer these concretely:

- What exact Android module layout and dependency graph will be used?
- How will the current pure import workflow be reused from Android code?
- What Room schema and repository surfaces back history and dashboard?
- What Android tests must pass before a runnable build is considered ready?
- What remains JVM-only after the runnable app target is reached?
