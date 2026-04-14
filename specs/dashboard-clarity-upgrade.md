# Dashboard Clarity Upgrade Spec

## Goal and Context

Define the readability improvements for the dashboard so users can understand
the current metrics, sample size, and low-data states without deciphering the
screen structure.

This spec refines the current dashboard presentation, not the metric formulas.

## Concrete Examples

### Example 1: User Has Several Saved Matches

Input:
- Dashboard opens with enough saved data to compute current metrics.

Expected behavior:
- The most important metrics are visually obvious.
- The user can understand what the numbers refer to.

### Example 2: Very Little Data

Input:
- Dashboard opens with only one or two saved records.

Expected behavior:
- The screen remains informative without overstating confidence.
- Sparse data does not look like a broken dashboard.

### Example 3: No Saved Records

Input:
- Dashboard opens before the user has saved any matches.

Expected behavior:
- The empty state is clear and not visually confusing.

## Requirements

- The dashboard MUST present current metrics in a clear information hierarchy.
- The dashboard MUST make empty and sparse-data states understandable.
- The dashboard MUST use user-facing labels rather than low-context technical
  wording.
- The dashboard SHOULD help the user understand what time/sample context the
  metrics come from when that information is available.
- The dashboard MUST NOT change the underlying metric calculation rules.

## Interface Expectations

- The most valuable metrics should be easy to find at a glance.
- The dashboard should not require the user to infer whether missing numbers
  mean "not enough data" or "screen failure."

## Error-State Expectations

- If dashboard loading fails, the screen MUST still show a visible error state.
- If no metrics are available yet, the screen MUST still explain the empty
  state clearly.

## Edge Cases

- No saved matches.
- Very small sample size.
- Partial metric availability because some fields are missing in saved records.

## Non-Goals

- New metric formulas.
- Advanced charts or interactive analytics.
- Cross-account or cloud-synced trend analysis.

## Acceptance Criteria

- Dashboard metrics are easier to understand at a glance.
- Empty and sparse-data states remain informative.
- Existing metric logic remains unchanged.

## Gotchas

- 2026-04-15: Dashboard clarity can come from sample-context text and
  sparse-data notes without changing any metric formulas. Keep this work in
  presentation and messaging, not in the calculator.
