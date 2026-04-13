# Android Project Bootstrap And Build Setup Test Spec

## Scope

This test spec covers Android project sync/build viability and baseline
app launch readiness.

## Unit Tests

- `T1` Gradle configuration exposes an Android application module.
- `T2` Pure logic module or source set remains accessible to Android code.

## Integration Tests

- `IT1` Debug build assembles successfully.
- `IT2` App launches into `MainActivity` without crashing.
- `IT3` Existing JVM unit tests still run after Android bootstrap.

## What Not To Test

- OCR accuracy.
- Room persistence behavior.
- Feature-specific UI rendering.

## Coverage Map

- Android bootstrap viability covered by `T1`, `IT1`, `IT2`
- Existing logic preservation covered by `T2`, `IT3`
