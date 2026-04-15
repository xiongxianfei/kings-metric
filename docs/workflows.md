# System Workflows

Runtime data flows that span multiple components. Read this before
implementing any feature that connects to existing code.

For development process (how to work), see `CLAUDE.md` / `AGENTS.md`.
For feature requirements, see `specs/`.
For build order, see `docs/plan.md`.

---

## Android Build Verification

Before calling an Android integration runnable, verify the build path that
forces generated-code wiring to execute.

- If a feature introduces Room, Hilt, or another KSP-backed library, the
  owning Android module must declare both the runtime dependency and the
  matching compiler/plugin wiring.
- `:app:assembleDebug` is the minimum verification step for Android runtime
  wiring. Pure JVM tests do not prove that generated classes such as
  `*_Impl` or DI components exist.
- If route-scoped Android UI state must survive activity recreation, cover the
  state serializer or codec with a deterministic unit test. Emulator-side
  state-restoration tooling can fail for framework reasons that are unrelated
  to the actual save/restore logic.
- If an end-to-end Android flow depends on Photo Picker, permissions UI, or
  another system surface, keep the real business/runtime path intact but add a
  narrow shell-level test hook before the framework boundary so Compose
  instrumentation can still verify navigation and save behavior deterministically.
- For feature-scoped CI, prefer `pull_request` plus `push` on `main` only.
  Running the same workflow on feature-branch `push` and `pull_request`
  duplicates expensive emulator work and makes failures harder to read.
- Add `concurrency` to expensive workflows so stale branch runs are cancelled.
- Keep PR workflows fast. If an emulator-backed check consistently takes too
  long for normal review feedback, move it to a scheduled or manual smoke
  workflow and leave only the smaller assembly/unit checks on the PR path.
- Keep repository-specific Kotlin/KSP compatibility properties in place
  until the toolchain is upgraded and re-verified. Removing them can turn a
  compile-time wiring problem into a runtime crash.
- Treat "generated implementation does not exist" failures as build wiring
  bugs first, not runtime-only bugs.
- Keep Android instrumented test method names dex-safe on the current
  toolchain. Backtick test names with spaces can compile on the Kotlin side
  but still fail later during `dexBuilderDebugAndroidTest`.
- For Material 3 bottom navigation, prefer tagged route titles as the primary
  route-context assertion and use `useUnmergedTree = true` when interacting
  with `NavigationBarItem` semantics in Compose instrumentation. Selected or
  merged nav items can disappear from the default semantics tree even when the
  shell is behaving correctly.
- Treat emulator API level, profile, and post-boot setup options as explicit
  stability choices. If a hosted runner repeatedly fails during emulator boot
  or action-managed device setup, prefer a proven API/profile combination and
  narrow the connected test command to the relevant test class.

---

## Screenshot Import (v1)

Full pipeline from camera capture to saved record.

### Pipeline

```
Screenshot Intake → Template Validation → OCR/Field Mapping
→ Normalization → Draft Creation → Review State → Save Validation
→ Record Persistence + Screenshot Linkage
```

### Stage Details

**1. Screenshot Intake & Local Storage**

- User captures or selects screenshot
- Save to app-managed local storage (not external)
- Generate a stable reference ID for linkage through the pipeline
- **Output:** `ScreenshotRef(id, localPath, timestamp)`
- **Error:** Storage write fails → show error, do not proceed. Do not
  silently discard the screenshot.

**2. Template Validation**

- Match screenshot against known templates
- **Input:** `ScreenshotRef`
- **Output:** `TemplateMatch(templateId, confidence)` or rejection
- **Error:** No template matches → reject with clear message to user
  ("Unsupported screenshot format"). Do not attempt partial parsing.
- **Error:** Low confidence match → flag for user confirmation before
  proceeding. Do not auto-accept.

**3. OCR / Result Mapping**

- Extract field values from screenshot using matched template
- Map extracted text to structured fields
- **Input:** `ScreenshotRef` + `TemplateMatch`
- **Output:** `RawFieldMap(Map<FieldKey, ExtractedValue>)`
- **Rule:** Do not invent values not visible in the screenshot
- **Rule:** Mark fields with low extraction confidence as unresolved
- **Error:** OCR extraction fails entirely -> abort with error message.
  Do not create a draft with empty fields.
- **Error:** Unexpected OCR-mapper/runtime exceptions must also collapse into
  the same retryable import-failure path. The recognition stage must fail
  closed instead of letting Android runtime exceptions escape and kill the app.
- **Rule:** On Android, avoid a second full-size decode just to prove the file
  is readable before OCR starts. Use a bounds-only probe for validation and
  let the real recognizer own the actual image load, or large screenshots can
  crash the import path on lower-memory devices.
- **Rule:** Keep at least one Android regression test for this stage on a real
  supported screenshot fixture. Synthetic text-image fixtures alone can miss
  ML Kit ordering differences, punctuation, and character-variant output that
  break field mapping on the actual screenshot template.

**4. Field Normalization**

- Clean and normalize extracted values (trim whitespace, standardize
  number formats, parse dates)
- Apply field-type-specific rules
- **Input:** `RawFieldMap`
- **Output:** `NormalizedFieldMap(Map<FieldKey, NormalizedValue>)`
- **Rule:** Normalization must be a pure function — testable without
  Android framework dependencies (JVM test target)
- **Error:** Normalization fails for a field → keep raw value, flag
  field as requiring manual review

**5. Draft Creation**

- Assemble normalized fields into a draft record
- Attach the `ScreenshotRef` for linkage
- **Input:** `NormalizedFieldMap` + `ScreenshotRef`
- **Output:** `DraftRecord(fields, screenshotRef, unresolvedFields, flags)`
- **Rule:** `unresolvedFields` must list every field that failed OCR,
  failed normalization, or had low confidence
- **Rule:** Draft is not yet persisted to Room — it exists in memory only

**6. Review State Generation**

- Generate review UI state from the draft
- Flag fields that need user attention
- **Input:** `DraftRecord`
- **Output:** `ReviewState(displayFields, editableFields, blockers)`
- **Rule:** If `unresolvedFields` is non-empty, at least one blocker
  must exist — the user must resolve before saving
- **Rule:** User can edit any field, not just flagged ones
- **Rule:** If Android shell recovery depends on the current draft, mirror user
  edits from the route-scoped review `ViewModel` back into the shell-owned
  saveable draft state as edits happen. Saving only the original draft snapshot
  will lose unsaved manual corrections after activity recreation.
- **Rule:** For long grouped review forms, keep the blocker summary and save
  availability visible near the top of the screen. Lower sections may sit
  below the fold on phone-sized devices, so instrumentation should verify
  visible top guidance plus the existence of grouped content instead of
  assuming every section is simultaneously visible.
- **Rule:** When the review form becomes long, prefer a single scrolling
  content surface plus an anchored save action outside that scroll region.
  Phone-sized screens are much more reliable when the primary action is not
  buried inside the form content.
- **Rule:** Screenshot preview rendering on Android should use a bounded,
  downsampled bitmap sized for the preview surface, not a full-resolution
  decode of the original screenshot. Large imported screenshots can otherwise
  crash as soon as review opens even if the rest of the flow is valid.
- **Rule:** When history, detail, or dashboard presentation moves to grouped
  cards, keep Compose assertions on visible labels, fallback copy, and
  separate value nodes. Avoid brittle tests that depend on old flat
  `"Label: Value"` concatenation text.

**7. Save Validation**

- Validate the complete record before persistence
- **Input:** `ReviewState` (after user edits)
- **Output:** `ValidationResult(isValid, errors)`
- **Rule:** Block save when required fields are empty or invalid
- **Rule:** Validate field types, ranges, and required-field presence
- **Rule:** Save validation is a pure function (JVM test target)
- **Error:** Validation fails → show field-level errors in review UI.
  Do not navigate away. Do not clear user edits.

**8. Record Persistence & Screenshot Linkage**

- Write validated record to Room
- Maintain linkage between record and screenshot file
- **Input:** Validated `DraftRecord`
- **Output:** `SavedRecord(id, screenshotRef)` persisted in Room
- **Rule:** Persistence and screenshot linkage happen in a single
  transaction — if record save fails, do not orphan the screenshot
  reference
- **Rule:** After successful save, navigate to record list / confirmation
- **Rule:** When the review-save path is tested through the Android shell,
  prefer a narrow shell success hook plus repository/history assertions over a
  brittle full-destination assertion if the nested NavHost transition itself is
  framework-sensitive in instrumentation. Keep the real save flow intact.
- **Rule:** When Room backs this stage on Android, do not execute the final
  record write from the UI thread. A main-thread Room violation can surface as
  a misleading generic local-save error even when the reviewed data is valid.
- **Error:** Room write fails → show error, keep user on review screen
  with all data intact. Do not lose edits.

### Cross-Cutting Rules

- Screenshot reference must be preserved from stage 1 through stage 8.
  If linkage breaks at any point, the record is invalid.
- No stage may introduce values not derived from the screenshot or
  user input. No invented data.
- No stage may introduce cloud, sync, or server-side OCR behavior.
  This is a local-first pipeline.
- Each stage should fail explicitly. Silent fallbacks hide bugs.

### Test Coverage Map

| Stage | Test Type | What to Test |
|-------|-----------|-------------|
| 1. Intake | Instrumented | File storage, path generation |
| 2. Template validation | JVM | Match logic, rejection, confidence thresholds |
| 3. OCR mapping | JVM | Field extraction, unresolved marking |
| 4. Normalization | JVM | Pure transform logic, edge cases |
| 5. Draft creation | JVM | Field assembly, flag generation |
| 6. Review state | JVM + Compose | State generation, blocker logic, UI rendering |
| 7. Save validation | JVM | Required fields, type checks, range checks |
| 8. Persistence | Instrumented | Room write, transaction integrity, linkage |

---

## Record List Display

Flow from app launch to showing saved records.

1. App starts → `MainActivity` launches `RecordListScreen`
2. `RecordListViewModel` observes Room DAO via `Flow<List<SavedRecord>>`
3. Records load → UI shows list sorted by `createdAt` descending
4. Empty state → show empty message (not a blank screen)
5. User taps a record → navigate to detail screen with `record.id`

**Error path:**
- Room query fails → show error state in UI, allow retry
- Record has broken screenshot linkage → show record without thumbnail,
  do not crash

---

## Record Detail / Edit

Flow for viewing and editing a saved record.

1. `RecordDetailScreen` receives `record.id` via navigation
2. `RecordDetailViewModel` loads record from Room by ID
3. Display all fields + linked screenshot thumbnail
4. User taps Edit → switch to edit mode (same screen or sheet)
5. User modifies fields → local state only, not yet persisted
6. User taps Save → `SaveValidation.validate(editedRecord)`
7. Validation passes → update Room record
8. Success → exit edit mode, show updated record
9. Validation fails → show field-level errors, keep edit mode

**Error path:**
- Record not found by ID → navigate back, show error toast
- Screenshot file missing on disk → show record without image,
  do not block access to field data
- Room update fails → show error, keep edits intact

---

## GitHub Release (Alpha)

Flow for publishing the first GitHub prerelease without drifting away from the
verified Android artifact and documented scope.

1. Load release-facing metadata from the tracked repository source
2. Resolve the first-release version/tag and release notes path
3. Optionally sync the GitHub repository description from the same tracked
   metadata source
4. Validate release signing inputs
5. Produce the signed release artifact through the release build path
6. Verify the release gate inputs used for publication
7. Publish the GitHub release as a prerelease with the verified user artifact
   and release notes

**Error path:**
- Signing inputs missing -> fail closed before treating any artifact as the
  signed release build
- Release metadata incomplete -> stop before publication
- Repository description sync token missing -> keep release publication
  independent; repo description sync is optional and needs a separate
  admin-scoped token
- Release artifact missing or wrong -> do not publish a misleading release

**Rules:**
- The first GitHub release stays alpha-only until a later plan changes that
  policy explicitly.
- Release metadata, README wording, and release notes should stay aligned to
  one supported-scope statement.
- The published artifact must come from the release path, not a debug fallback.
- Keep the release-candidate gate separate from generic Android readiness.
  `:app:assembleDebug` and generic smoke checks are necessary inputs, but they
  are not enough to treat the first GitHub alpha as publishable.
- If emulator-backed verification or manual device confirmation is skipped,
  keep that skip visible and blocking in the release-gate result. Do not let a
  skipped release check disappear into a generic "not ready" bucket.
- If GitHub publication is triggered manually, require explicit release-gate
  confirmation inputs in the workflow before building or publishing. A manual
  dispatch alone is not evidence that the real-device flow and release gate
  were actually cleared for the candidate.

---

## UX Regression Coverage

- Focused feature tests should own their primary screen or route promises.
- The residual UX regression pass should cover only cross-screen gaps that are
  still uncovered after those focused tests land.
- Do not create a second broad Android suite that repeats the same
  import/review/history assertions already owned by another feature.

---

## Document Maintenance

Update this file when:

- A new end-to-end flow is implemented that spans 2+ components
- An error path is discovered that isn't documented here
- A handoff between stages changes (types, method signatures)
- A stage is added, removed, or reordered

Do not update this file for:
- Process changes (update CLAUDE.md / AGENTS.md)
- Feature requirements (update the spec)
- Build order changes (update docs/plan.md)
