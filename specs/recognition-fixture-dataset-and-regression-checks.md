# Recognition Fixture Dataset And Regression Checks Spec

## Goal and Context

Define the first-release fixture dataset and regression policy used to keep supported-template validation and parsing behavior stable as recognition logic evolves.

This spec defines Feature 9 from `docs/plan.md`.

## Concrete Examples

### Example 1: Parser Change Before Release

Input:
- A parser rule changes for one supported field.

Expected behavior:
- Existing supported fixtures are rechecked.
- Known-good outputs remain stable unless the spec or fixture expectation changes intentionally.

### Example 2: Unsupported Fixture Added

Input:
- Team adds a cropped or wrong-screen screenshot fixture.

Expected behavior:
- Regression checks confirm the screenshot stays rejected.

### Example 3: Low-Confidence Numeric Fixture

Input:
- Fixture contains OCR ambiguity in one numeric field.

Expected behavior:
- Regression checks confirm the field remains flagged for review rather than silently normalized into a confident value.

## Requirements

- The regression dataset MUST include at least one supported full screenshot fixture.
- The regression dataset MUST include at least one supported fixture with an optional missing field.
- The regression dataset MUST include at least one supported fixture with a required unresolved field.
- The regression dataset MUST include at least one unsupported wrong-screen or cropped fixture.
- The regression dataset MUST include at least one low-confidence numeric fixture.
- Each fixture MUST map to an expected validation or parsing outcome.
- Regression checks MUST cover both acceptance and rejection behavior for the supported template.
- A parser or validator change MUST NOT silently alter expected fixture outcomes without an intentional fixture expectation update.

## Error-State Expectations

- If a fixture can no longer be parsed as expected, the regression suite MUST fail clearly enough to identify which fixture changed behavior.

## Edge Cases

- Supported screenshot with one optional unreadable field.
- Supported screenshot with one required unreadable field.
- Unsupported screenshot cropped below a required section.
- Low-confidence OCR numeric ambiguity.

## Non-Goals

- Large-scale benchmark dataset collection.
- Support for unsupported templates in fixture generation.
- Automatic relabeling of fixture expectations.

## Acceptance Criteria

- The repository contains a seed fixture set covering supported, unsupported, missing-field, and low-confidence cases.
- Each fixture has an explicit expected outcome.
- Regression checks fail when validator or parser behavior drifts unintentionally.

## Gotchas

- None yet.
