# Android App Icon Refresh Test Spec

## Scope

This test spec covers the first branded Android launcher icon initiative
defined in [android-app-icon-refresh](./android-app-icon-refresh.md).

It verifies that:

- the repo gains one reviewable source-of-truth icon asset package
- the source package clearly maps to exported Android launcher outputs
- the Android app declares explicit branded launcher icon resources
- adaptive launcher integration is present and build-safe
- themed/monochrome behavior is handled explicitly
- manual Android surface review covers launcher, app info, masking, and
  originality against the provided reference direction

It does not expand into a splash-screen redesign, notification icon redesign,
or wider product-brand overhaul.

## Static And Resource Tests

- `T1` The repo contains one explicit source-of-truth icon asset package in a
  stable design-asset location rather than only generated Android outputs.
- `T2` The source asset package makes the chosen source format and export
  mapping explicit enough for later regeneration without guesswork.
- `T3` The source asset package remains text-free and does not depend on tiny
  letters, words, or micro-detail for recognition instructions.
- `T4` `app/src/main/AndroidManifest.xml` declares explicit branded
  `android:icon` and `android:roundIcon` attributes.
- `T5` The Android resource outputs include adaptive launcher icon resources
  rather than only one generic raster file.
- `T6` Required branded launcher resource outputs exist under
  `app/src/main/res/` for the chosen icon integration path.
- `T7` If themed/monochrome support is included, the explicit monochrome
  resource output exists and is wired consistently with the chosen output set.
- `T8` If themed/monochrome support is excluded, the source package or notes
  make that exclusion explicit instead of silently omitting it.
- `T9` Launcher icon work stays scoped to launcher branding and does not alter
  notification-icon, splash, or in-app art resources as part of this feature.

## Build And Android Integration Tests

- `IT1` `./gradlew.bat --no-daemon :app:assembleDebug` succeeds after the
  launcher icon resources are integrated.
- `IT2` Android resource linking succeeds with the branded launcher resources
  and manifest wiring in place.
- `IT3` The adaptive icon resource set remains present after a clean rebuild
  and is not dependent on a missing external runtime generator step.
- `IT4` The icon integration works within the current Android app structure
  without Room, schema, or business-logic migration side effects.

## Manual Android Surface Review

- `M1` On a normal home-screen launcher surface, the app shows a distinct
  branded icon instead of the generic default.
- `M2` At normal launcher size, the icon remains recognizable without text.
- `M3` Under adaptive round and non-round masks, the main icon subject remains
  centered and no critical part is clipped.
- `M4` On Android app info surfaces, the branded icon remains visible and
  recognizable.
- `M5` If themed icons are included, the monochrome icon remains recognizable
  in themed mode.
- `M6` If themed icons are excluded, that exclusion is documented explicitly
  and the absence is not treated as a missing implementation bug.
- `M7` The icon remains visually original to this project and is not a direct
  copy of the provided reference.
- `M8` The final icon still reads as premium, performance-oriented, and
  progress/metrics related without becoming noisy or blurry.

## Example Coverage

- Example 1 from the feature spec is covered by `M1` and `M2`.
- Example 2 is covered by `M3`.
- Example 3 is covered by `T7`, `T8`, `M5`, and `M6`.
- Example 4 is covered by `T1`, `T2`, and `IT3`.

## Edge-Case Coverage

- no existing launcher icon resources in the repo -> `T4`, `T5`, `T6`, `IT1`
- adaptive round mask -> `M3`
- adaptive squircle or non-round mask -> `M3`
- small launcher-grid icon size -> `M2`, `M8`
- light launcher background -> `M1`, `M2`
- dark launcher background -> `M1`, `M2`
- themed icon mode on supported Android versions -> `T7`, `T8`, `M5`, `M6`
- Android app info surface -> `M4`
- source review against the provided reference direction -> `M7`
- future icon refresh requiring re-export from the source asset package ->
  `T1`, `T2`, `IT3`

## Requirement Coverage Map

- `R1` -> `T4`, `T5`, `T6`, `IT1`, `M1`
- `R2` -> `T3`, `M2`
- `R3` -> `M7`, `M8`
- `R4` -> `M2`, `M8`
- `R5` -> `T1`
- `R6` -> `T1`
- `R6a` -> `T2`, `IT3`
- `R7` -> `T5`, `IT1`
- `R8` -> `M3`
- `R9` -> `T4`
- `R10` -> `T6`, `IT2`, `IT3`
- `R11` -> `T9`, `IT4`
- `R12` -> `T7`, `M5`
- `R13` -> `T8`, `M6`
- `R14` -> `IT1`, `IT2`
- `R15` -> `M1`, `M4`
- `R16` -> `M7`

## What Not To Test

- splash-screen artwork changes
- notification icon behavior
- Play Store listing graphics
- in-app dashboard illustration systems
- subjective pixel-perfect beauty beyond the explicit launcher readability,
  masking, branding, and originality checks above

## Not Directly Testable

- purely subjective taste within the approved visual direction beyond the
  bounded manual review checks
- exact art-creation technique, because the contract defines the observable
  source package, Android outputs, and launcher behavior rather than a specific
  drawing workflow
