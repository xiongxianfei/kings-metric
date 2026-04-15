# Application Version Visibility Test Spec

## Scope

This test spec covers the user-visible Diagnostics version field, the runtime
version source, and alignment between the visible value and copied diagnostics
export.

## Unit Tests

- `T1` The diagnostics screen state model exposes a labeled current-version
  field from a provided runtime version source.
  - Covers: `R1`, `R2`, `R3`
- `T2` When diagnostics entry history is empty, the diagnostics screen state
  still includes the current-version field.
  - Covers: `R1`, `R4`, edge case `empty diagnostics history`
- `T3` When runtime version lookup fails, the state falls back to a safe
  readable value such as `Unknown` without failing the rest of the screen
  state.
  - Covers: `R5`, edge case `lookup fails`
- `T4` Export formatting uses the same app-version value as the visible
  diagnostics screen state.
  - Covers: `R2`, `R4`

## Integration Tests

- `IT1` The Diagnostics screen renders a clearly labeled current-version field
  in the visible UI.
  - Covers: `R1`, `R3`
- `IT2` The Diagnostics screen still renders the current-version field when the
  diagnostics list is empty.
  - Covers: `R1`, `R4`, edge case `before any failure`
- `IT3` Copying diagnostics preserves the same version value shown in the
  Diagnostics screen.
  - Covers: `R2`, `R4`
- `IT4` After a build-version change, the runtime-provided visible version
  updates without requiring a diagnostics failure path.
  - Covers: `R2`, compatibility expectation `APK upgrade`

## What Not To Test

- GitHub release publication itself
- README wording
- Full changelog rendering
- Play Store or update prompts

## Coverage Map

- `R1` -> `T1`, `T2`, `IT1`, `IT2`
- `R2` -> `T1`, `T4`, `IT3`, `IT4`
- `R3` -> `T1`, `IT1`
- `R4` -> `T2`, `T4`, `IT2`, `IT3`
- `R5` -> `T3`

## Concrete Fixtures And Scenarios

- Use one explicit sample version string such as `0.1.0-alpha.8` in unit and
  Compose tests.
- Use an empty diagnostics recorder fixture to verify version visibility
  without any captured failures.
- Use a fake export callback to capture copied diagnostics text and assert the
  same version appears there.

## Gaps

- None currently. The feature contract is concrete enough for focused
  Diagnostics-surface tests.
