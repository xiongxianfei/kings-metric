# Import Status And Guidance UX Test Spec

## Scope

This test spec covers the import screen idle, in-progress, unsupported,
unreadable-source, local-save-failure, and review-ready UX states plus the
user's next-step guidance.

## Unit Tests

- `T1` Import screen state mapper exposes one clear primary import action in the
  idle state.
- `T2` State mapper returns distinct user-facing states for idle, in-progress,
  unsupported, unreadable-source, local-save-failure, and review-ready.
- `T3` Unsupported, unreadable-source, and local-save-failure outcomes map to
  distinct guidance and retry actions.
- `T4` Review-ready state exposes an explicit continue-to-review action even if
  automatic navigation does not occur.
- `T5` Picker cancellation maps to a non-error outcome that preserves a usable
  import screen state.

## Integration Tests

- `IT1` First opening the import screen explains the supported screenshot type
  and shows the import action prominently.
- `IT2` Unsupported screenshot result shows clear mismatch guidance and an
  obvious retry path.
- `IT3` Unreadable-source failure shows source-use failure guidance without
  mislabeling the problem as unsupported-template failure.
- `IT4` Local-save failure shows local-storage guidance without presenting the
  outcome as recognition failure.
- `IT5` Review-ready success exposes a visible continue-to-review path if
  automatic navigation does not fire.
- `IT6` Repeated failures still leave the import action obvious.
- `IT7` Picker cancellation leaves the user on import without showing a false
  failure state.

## What Not To Test

- Photo picker internals.
- OCR field parsing accuracy.
- Review field editing behavior.

## Coverage Map

- Supported-input orientation covered by `T1`, `IT1`
- Distinct import outcomes covered by `T2`, `T3`, `IT2`, `IT3`, `IT4`
- Actionable success state covered by `T4`, `IT5`
- Cancel and retry safety covered by `T5`, `IT6`, `IT7`

## Not Directly Testable

- None.
