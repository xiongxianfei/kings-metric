# Honor of Kings Match Tracker Plan

## Objective

Keep the runnable Android app stable, then improve post-UX product quality in
the smallest reviewable slices:

- reduce avoidable manual correction during screenshot import
- harden the import -> review -> save path on real devices
- tighten release-quality verification without expanding v1 scope

The current priority is reliability and product hardening, not new template or
cloud scope.

## How To Read This Plan

Use this file for:

- current delivery status
- next implementation track
- PR-sized feature breakdown
- explicit non-goals

Use other docs for detail:

- runtime/system flow: [workflows.md](./workflows.md)
- feature contracts: [specs/](../specs/)

## Current Status

Runnable Android app foundation is `Done`:

1. Android bootstrap/build
2. app shell/navigation
3. photo picker/local storage
4. ML Kit recognition adapter
5. review screen/runtime
6. Room persistence
7. history/dashboard binding
8. Android test harness

UX polish phase is `Done`:

1. shared UX labels/state messaging
2. app shell navigation UX
3. import status and guidance UX
4. review field grouping and labels
5. review sticky actions and scroll safety
6. review input hints and editing assistance
7. history list readability
8. record detail usability
9. dashboard clarity
10. residual UX regression/device gap fill

Current product state:

- import -> review -> save works in the Android runtime
- review uses grouped user-facing labels and reachable save actions
- history, detail, and dashboard are readable and fallback-safe
- focused JVM and Android Compose regressions cover the main UX path

Current highest-value product gaps:

- hero extraction still often falls back to manual review on the supported
  screenshot
- failure/recovery behavior needs more real-device hardening than the current
  focused tests provide
- release confidence still depends on a relatively small emulator/device matrix

## Next Track

Design goals for the next phase:

- lower manual correction cost without inventing data
- make failed or interrupted flows easier to recover safely
- raise confidence that the shipped Android flow behaves the same on real
  devices as it does in focused emulator tests

## Feature List

### 1. Hero Extraction Reliability

- Scope: improve supported-template hero extraction so the main screenshot path
  requires manual hero entry less often, while preserving strict
  unsupported/low-confidence behavior
- Dependencies: existing ML Kit adapter and import workflow
- Risk: medium
- Size: medium

### 2. Import/Review Recovery Hardening

- Scope: harden interrupted, retried, and failed import/review flows so users
  can recover without losing context or seeing misleading states
- Dependencies: 1
- Risk: medium
- Size: medium

### 3. Persistence And Linkage Resilience

- Scope: verify and tighten record/screenshot linkage, failed-save recovery,
  and missing-file behavior now that the runtime flow is in regular use
- Dependencies: 2
- Risk: medium
- Size: small

### 4. Device Verification Expansion

- Scope: broaden Android runtime verification around the real supported
  screenshot path, blocker handling, save success, and history/detail
  continuity across more realistic device conditions
- Dependencies: 1, 2, 3
- Risk: medium
- Size: small

### 5. Release Readiness Tightening

- Scope: convert the now-stable runtime and verification path into a stricter
  release gate with clearer pass/fail criteria for local release candidates
- Dependencies: 4
- Risk: low
- Size: small

## Build Order

1. Hero Extraction Reliability
2. Import/Review Recovery Hardening
3. Persistence And Linkage Resilience
4. Device Verification Expansion
5. Release Readiness Tightening

## Architecture Decisions

- Keep business rules in workflow/repository layers; hardening work should not
  move validation or persistence rules into composables.
- Prefer targeted improvements to the existing supported-template recognition
  path over introducing a second parallel recognition system.
- Keep verification split:
  - JVM for parsing/validation/state logic
  - Compose/instrumented for Android/runtime behavior
- Every hardening feature should add the narrowest regression that proves the
  exact failure or recovery path it changes.

## Risks

- better hero extraction can accidentally overfit to one screenshot sample and
  become less reliable on real variants of the supported template
- recovery logic can silently weaken strict review/save constraints if retries
  are implemented too loosely
- expanding device verification can lengthen feedback cycles unless expensive
  coverage stays scoped and intentional
- linkage hardening can surface old assumptions in history/detail flows that
  were previously hidden by clean test data

## Edge Cases

- supported screenshot where hero text is unreadable but the rest of the draft
  is usable
- interrupted import or review flow where a draft exists but navigation state
  changes
- save failure after screenshot storage or after partial runtime progress
- history/detail open path where screenshot preview is missing but record data
  is still valid

## Non-Goals

- new screenshot templates or non-Chinese support
- cloud sync, accounts, or server OCR
- broad visual redesign as a new phase after UX completion
- advanced analytics beyond the current dashboard metric set

## Done When

This next track is done when:

- hero extraction quality is meaningfully better on the supported screenshot
  path without weakening unsupported/low-confidence handling
- interrupted and failed import/review/save flows recover predictably
- screenshot linkage and saved-record fallback behavior stay correct under
  failure conditions
- device/emulator verification covers the critical real-user path with a clear
  release gate
