# Honor of Kings Match Tracker Plan

## Overview

Build an Android app that tracks Honor of Kings match data without direct API access by extracting structured stats from screenshots. The first release should optimize for reliability over automation: the user selects one supported post-match personal-stats screenshot, the app performs on-device OCR plus rule-based parsing, the user reviews and corrects the extracted record, and the app saves both the screenshot and confirmed structured data locally.

This release supports one screenshot template only and one language only: Simplified Chinese personal-stats screenshots from the post-match detailed-data screen.

## Planning Assumptions

- Primary user: a single player who wants a personal match history and performance trends.
- Platform: native Android app.
- Language: Kotlin.
- Input source: one specific Chinese post-match personal-stats screenshot template.
- OCR/privacy constraint: extraction must run on-device only.
- Accuracy target: "good enough with review," not fully hands-free extraction.
- Persistence model: screenshots and extracted match data remain on-device in the first release.

## Technical Stack

- UI: Jetpack Compose with Material 3.
- Navigation: Navigation Compose.
- Architecture: MVVM with `ViewModel`, `StateFlow`, and unidirectional state flow.
- Dependency injection: Hilt with KSP.
- Structured local storage: Room.
- Small local config/state storage: Proto DataStore.
- Image selection: Android Photo Picker via `ActivityResultContracts.PickVisualMedia`.
- OCR: ML Kit Text Recognition v2, on-device only.
- Background tasks: WorkManager only for non-blocking follow-up work such as thumbnail generation or cleanup. The primary import-review-save flow stays foreground and user-driven.
- Testing:
  - JVM unit tests for template validation, parsing, normalization, and save rules
  - instrumented tests for Room and file persistence behavior
  - Compose UI tests for the import, review, and confirmation flow

## Architecture Decisions

- Use a local OCR pipeline and deterministic parsing rules for one supported Chinese screenshot template instead of any server-side recognition service.
- Reject unsupported languages, regional variants, and layouts early rather than trying to coerce them into partial matches.
- Treat screenshot ingestion, template validation, OCR, and parsing as one recognition pipeline because they share image geometry, fixture data, and error handling.
- Require a review/edit step before final save so OCR mistakes do not silently corrupt saved match history.
- Persist screenshots as app-managed local files and persist structured match records in Room with stable linkage to the stored screenshot.

## Data and Storage Model

- Store the original screenshot locally as soon as import begins.
- Keep a stable screenshot identifier and local file path so saved records can be rechecked later by the user.
- Save final match records in Room and link each record to its stored screenshot.
- Allow unresolved optional fields to remain empty in the saved record.
- Prevent final save when any required field remains missing or invalid.
- If screenshot storage succeeds but final record save fails, keep the screenshot and report the failure clearly so the app does not pretend the record was saved.

## Feature Breakdown

### 1. Match Data Contract and Supported Template Definition

- Scope: define the canonical match model, required fields, optional fields, the one supported Chinese screenshot template, required anchors, explicit rejection conditions, and unsupported cases.
- Dependencies: none.
- Risk: medium. If the data contract is weak, parsing, storage, and dashboard logic will need rework.
- Size: small.

### 2. Screenshot Import and Local File Intake

- Scope: let the user pick one screenshot from device storage, copy it into app-managed local storage, assign a stable screenshot identifier, and surface storage failures clearly.
- Dependencies: Feature 1.
- Risk: medium. File-copy behavior, duplicate imports, and storage failures need explicit handling.
- Size: small.

### 3. Recognition Pipeline: Template Validation, OCR, and Parsing

- Scope: validate the image against the supported template, normalize image regions, run on-device OCR, map OCR output into the fixed v1 field set, mark missing or ambiguous values, and produce a structured draft record.
- Dependencies: Features 1-2.
- Risk: high. This is the core delivery risk because OCR quality and template strictness determine whether the workflow is trustworthy.
- Size: large.

### 4. Review and Manual Correction Flow

- Scope: show the original screenshot and extracted fields together, highlight low-confidence or invalid fields, allow user edits, and require explicit confirmation before final save.
- Dependencies: Features 1 and 3.
- Risk: low. Standard form flow, but critical for product reliability.
- Size: medium.

### 5. Local Persistence and Screenshot Linkage

- Scope: persist confirmed match records in Room, preserve linkage to the stored screenshot, block invalid saves, and handle record-write failure without falsely marking a save as successful.
- Dependencies: Features 1, 2, and 4.
- Risk: low.
- Size: medium.

### 6. Match History UI

- Scope: browse saved matches, open a saved record, and inspect its linked screenshot and extracted stats.
- Dependencies: Feature 5.
- Risk: low.
- Size: medium.

### 7. Metrics Dashboard

- Scope: compute and display aggregate metrics such as win rate, average KDA, hero usage, and recent performance trends from saved matches.
- Dependencies: Feature 5.
- Risk: low.
- Size: medium.

### 8. Unsupported-Case and Failure Handling

- Scope: present clear reasons for unsupported screenshots, blocked saves, and local storage failures, and provide a clean retry path.
- Dependencies: Features 2-5.
- Risk: medium. Trust depends on failure handling being precise rather than vague.
- Size: small.

### 9. Recognition Fixture Dataset and Regression Checks

- Scope: build a seed fixture set for the supported screenshot template, define expected parsed outputs, and add repeatable regression checks for validation and parsing behavior.
- Dependencies: Features 1 and 3.
- Risk: medium. Without this, recognition quality will drift and regress silently.
- Size: small.

## Build Order

1. Feature 1: Match Data Contract and Supported Template Definition
2. Feature 2: Screenshot Import and Local File Intake
3. Feature 3: Recognition Pipeline: Template Validation, OCR, and Parsing
4. Feature 9: Recognition Fixture Dataset and Regression Checks
5. Feature 4: Review and Manual Correction Flow
6. Feature 5: Local Persistence and Screenshot Linkage
7. Feature 8: Unsupported-Case and Failure Handling
8. Feature 6: Match History UI
9. Feature 7: Metrics Dashboard

## Key Risks

- Honor of Kings UI layout may drift after game updates even within Chinese screenshots.
- OCR may misread hero names, digits, separators, or similar-looking characters.
- One screenshot template may still vary by device resolution, aspect ratio, or compression quality.
- A too-lenient validator will accept bad screenshots; a too-strict validator will reject usable ones.
- Duplicate screenshot imports may create repeated records if the app has no duplicate-detection policy.
- Partial failure at the boundary between screenshot storage and record persistence can create confusing user-visible states if not defined up front.

## Edge Cases and Boundary Conditions

- The screenshot is from the correct screen but one stat row is partially cropped.
- The screenshot is Chinese but the selected data tab is not visible.
- The screenshot uses the right layout but image quality is poor due to blur or compression.
- OCR confuses similar numeric characters such as `0` and `O` or `1` and `I`.
- An optional field is unreadable but all required fields are valid.
- A required field is unreadable and the user must correct it before save.
- The same screenshot is imported more than once.
- Screenshot copy succeeds but final record persistence fails.

## Non-Goals

- Support for non-Chinese screenshots in the first release.
- Support for multiple screenshot templates in the first release.
- Direct integration with an official or unofficial game API.
- Real-time stat capture during live matches.
- Automatic reprocessing of all previously saved screenshots after parser updates.
- Cloud sync, account systems, or multi-device history in the first release.
- Perfect extraction for every screenshot variation before launch.

## Suggested First Release Scope

Focus the first release on one stable workflow:

- Support only the Chinese post-match personal detailed-stats screenshot template.
- Treat the screenshot as supported only when key anchors are present, including the result header, the hero/player summary card, the selected data tab, and the visible personal stat sections.
- Extract only this fixed v1 field set: result, hero, player name, lane, score, KDA, damage dealt, damage share, damage taken, damage taken share, total gold, gold share, gold from farming, last hits, participation rate, kill participation count, control duration, and damage dealt to opponents.
- Reject unsupported screenshots early with a clear explanation.
- Always require user review before final save.
- Save the original screenshot and confirmed structured data locally with stable linkage between them.
- Keep all OCR and parsing on-device.

## Acceptance Direction

The plan is implementation-ready when the team can answer these with concrete design docs or specs:

- What exact anchors define the supported Chinese screenshot template?
- How strict should template rejection be when some OCR regions are noisy but anchors are present?
- What duplicate-detection rule should be used for repeated imports?
- What file layout and cleanup policy should be used when storage and record persistence do not both succeed?
- What minimum regression fixture set is required before parser changes can ship?
