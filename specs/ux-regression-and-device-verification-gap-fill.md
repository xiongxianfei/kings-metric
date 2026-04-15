# UX Regression And Device Verification Gap Fill Spec

## Goal and Context

Define the final usability verification pass that closes any remaining
cross-screen Android and Compose coverage gaps after the UX-focused features
land.

This spec exists to verify the integrated experience, not to replace focused
feature-level testing.

## Concrete Examples

### Example 1: Import To Review To Save

Input:
- User completes the main screenshot import flow on device.

Expected behavior:
- Primary actions remain visible and reachable across the flow.
- Navigation continuity remains intact after save.

### Example 2: Review With Multiple Blockers

Input:
- User enters review with several unresolved required fields.

Expected behavior:
- Blocker messaging remains clear after the UX refactor.
- The user can still locate and resolve the blocked fields.

### Example 3: History And Detail With Missing Preview

Input:
- User opens saved records where screenshot preview is unavailable.

Expected behavior:
- History and detail remain usable.
- The UX refactor has not hidden or confused those fallback states.

## Requirements

- The final UX verification pass MUST cover critical cross-screen usability
  behaviors that are not already covered in the owning feature tests.
- The verification pass MUST include device or emulator coverage for the main
  import -> review -> save path.
- The verification pass MUST confirm that primary actions remain visible and
  reachable after the UX changes.
- The verification pass MUST confirm that fallback states remain clear after
  the UX changes.
- The verification pass SHOULD avoid duplicating feature-level tests when a
  focused owning-feature test already covers the same promise.

## Interface Expectations

- The integrated UX should still feel coherent when moving across screens, not
  only within isolated screen tests.

## Error-State Expectations

- Cross-screen fallback states such as unsupported import, blocked review, and
  missing preview MUST remain visible and understandable.

## Edge Cases

- Main import -> review -> save flow after multiple UX refactors.
- Blocked review state with several unresolved required fields.
- History/detail flow with missing screenshot preview.

## Non-Goals

- Replacing focused feature-level regressions with one giant end-to-end test.
- Broad performance benchmarking.
- Manual QA documentation.

## Acceptance Criteria

- Remaining cross-screen UX regression gaps are covered.
- The core user journey remains usable after the UX phase.
- The verification scope complements, rather than replaces, feature-specific
  tests.

## Gotchas

- 2026-04-15: This pass is intentionally residual. If focused app-shell or
  screen-specific tests already own the main import or unsupported-import path,
  keep this feature on the remaining blocker and fallback continuity cases
  instead of duplicating those flows again.
