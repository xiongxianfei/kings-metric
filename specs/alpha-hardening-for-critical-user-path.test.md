# Alpha Hardening For Critical User Path Test Spec

## Scope

This test spec covers runtime hardening for the supported alpha user path:
import, review, save, interruption recovery, and post-save continuity.

## Unit Tests

- `T1` Critical-path state model exposes only explicit alpha outcomes:
  review-ready, blocked-for-review, unsupported, or actionable failure.
- `T2` Supported-draft state with unresolved required hero remains reviewable
  and marks hero as blocking rather than collapsing into a generic failure.
- `T3` Save-failure state preserves reviewed field data and retry eligibility.
- `T4` Save-success transition clears stale in-progress draft/import state.
- `T5` Recovery/state-ownership logic preserves enough progress across
  interruption or recreation to continue or explicitly retry.
- `T6` Failure messaging mapper distinguishes unsupported screenshot outcomes
  from local save failure outcomes.
- `T7` Critical-path state never marks a record as saved before confirmed local
  persistence success.

## Integration Tests

- `IT1` Supported screenshot on the current alpha path reaches review instead
  of leaving the user stuck on import, even when hero still requires manual
  completion.
- `IT2` User can complete hero manually on the review path and proceed to a
  successful save.
- `IT3` Save failure keeps the user on review with reviewed edits intact and a
  visible retry path.
- `IT4` After successful save, history shows the persisted record and the shell
  does not reopen a stale draft by mistake.
- `IT5` Activity recreation or temporary interruption during import/review
  resumes in a deterministic continue-or-retry state rather than an ambiguous
  blank or false-success state.
- `IT6` Unsupported screenshot result stays on the import path, does not create
  a misleading partial saved record, and does not reuse stale review state from
  a previous attempt.

## What Not To Test

- New hero-recognition techniques or portrait matching.
- GitHub release publication workflow behavior.
- Full release-gate device matrix policy.

## Coverage Map

- Explicit critical-path outcomes covered by `T1`, `IT1`, `IT6`
- Manual hero review continuity covered by `T2`, `IT1`, `IT2`
- Save resilience covered by `T3`, `T7`, `IT3`
- Post-save cleanup and history continuity covered by `T4`, `IT4`
- Recovery/interruption behavior covered by `T5`, `IT5`
- Failure-message distinction covered by `T6`, `IT3`, `IT6`

## Not Directly Testable

- General user-perceived clarity beyond whether the UI exposes explicit state,
  actionable next steps, and distinct supported-vs-failure outcomes.
