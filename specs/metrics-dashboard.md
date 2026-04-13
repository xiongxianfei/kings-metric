# Metrics Dashboard Spec

## Goal and Context

Define the first-release dashboard that computes and displays aggregate metrics from locally saved match records so the player can review performance trends over time.

This spec defines Feature 7 from `docs/plan.md` and depends on persisted local match records.

## Concrete Examples

### Example 1: History With Multiple Matches

Input:
- User has several saved records with wins, losses, and hero usage variation.

Expected behavior:
- The dashboard shows aggregate metrics such as win rate and recent performance using the saved local dataset.

### Example 2: Empty History

Input:
- User has no saved records.

Expected behavior:
- The dashboard shows an explicit empty state rather than misleading zero-value metrics presented as meaningful trends.

### Example 3: Record With Optional Missing Fields

Input:
- Some saved records are valid but have optional fields left empty.

Expected behavior:
- Metrics that do not require those optional fields still compute correctly.
- Metrics that require missing data remain unavailable or clearly partial rather than fabricated.

## Requirements

- The system MUST compute dashboard metrics from locally saved records only.
- The dashboard MUST show an explicit empty state when there is not enough saved data to compute meaningful metrics.
- The dashboard MUST NOT invent missing metric inputs from unresolved optional fields.
- The dashboard SHOULD support at least win rate, average KDA, hero usage, and recent performance summary because those are called out in `docs/plan.md`.
- Metrics calculations MUST remain outside Compose UI code.
- The dashboard MUST update when the underlying saved record set changes.

## Error-State Expectations

- If metric computation fails for one metric because of invalid source data, the system SHOULD keep other independent metrics visible when they remain valid.
- If history loading fails, the dashboard MUST show an error state rather than stale fabricated values.

## Edge Cases

- No saved records exist.
- Only one saved record exists.
- Optional fields needed for one metric are missing in some records.
- Saved records change while the dashboard is visible.

## Non-Goals

- Server analytics or remote leaderboard comparisons.
- Advanced filtering by arbitrary date ranges in the first release.
- Predictive recommendations or coaching features.

## Acceptance Criteria

- Dashboard metrics are derived from local saved records.
- Empty history produces an explicit empty state.
- Missing optional fields do not cause invented metrics.
- Metric logic remains outside UI code.

## Gotchas

- None yet.
