# Unsupported Case And Failure Handling Test Spec

## Scope

This test spec covers unsupported screenshot rejection, blocked save behavior, screenshot storage failures, record persistence failures, and retryable user recovery paths.

## Unit Tests

- `T1` Unsupported screenshot message identifies template mismatch intent.
- `T2` Save blocked state retains unresolved required field context.
- `T3` Storage failure result distinguishes screenshot save failure from record save failure.

## Integration Tests

- `IT1` Unsupported screenshot rejects and returns user to retryable import state.
- `IT2` Unsupported language variant rejects and returns user to retryable import state.
- `IT3` Blocked save keeps review state and user edits intact.
- `IT4` Screenshot storage failure stops downstream processing and shows error.
- `IT5` Record persistence failure shows save failure without marking success.
- `IT6` Screenshot remains consistent when record persistence fails after intake succeeded.

## Edge Case Coverage

- Wrong screen covered by `IT1`
- Unsupported language covered by `IT2`
- Long review before save failure covered by `IT3`, `IT5`
- Screenshot-storage-success but record-save-failure covered by `IT6`

## What Not To Test

- Exact finalized copy text
- Cloud retry behavior
- Localization completeness

## Coverage Map

- Clear unsupported rejection covered by `T1`, `IT1`, `IT2`
- Preserve draft during blocked save covered by `T2`, `IT3`
- Distinguish storage failure categories covered by `T3`, `IT4`, `IT5`
- Keep state consistent after partial failure covered by `IT6`
