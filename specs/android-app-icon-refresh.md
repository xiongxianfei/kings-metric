# Android App Icon Refresh Spec

## Goal and Context

Define the first branded Android launcher icon for the app so installed builds
no longer rely on a generic default icon.

This spec follows:

- [2026-04-16-android-app-icon-refresh](../docs/plans/2026-04-16-android-app-icon-refresh.md)

The current repo has no launcher-icon resources under `app/src/main/res/` and
`app/src/main/AndroidManifest.xml` does not declare explicit icon attributes.
So this feature is not a minor tweak. It defines the first complete icon
contract for:

- the source-of-truth icon asset package
- Android launcher resource outputs
- adaptive icon behavior
- themed/monochrome icon expectations

The user provided a reference direction: a premium, performance-oriented icon
with a clear upward-growth/analytics signal.

## Concrete Examples

### Example 1: Normal Home Screen Launcher Size

Input:

- the user installs the app on a typical Android launcher and views the home
  screen

Expected behavior:

- the app shows a distinct branded icon instead of a generic default
- the icon remains recognizable at launcher size
- the icon does not require text to communicate the app identity

### Example 2: Adaptive Masked Launcher Surface

Input:

- the launcher applies a round, squircle, or other adaptive icon mask

Expected behavior:

- the main icon subject remains visible and centered
- no critical part of the icon is clipped by the adaptive mask
- the icon still reads as one coherent mark

### Example 3: Android Themed Icon Surface

Input:

- the user enables themed icons on a supported Android version

Expected behavior:

- if themed icons are supported in this release, the icon still reads clearly
  in monochrome form
- if themed icons are intentionally scoped out, that decision is explicit in
  the source asset and Android resource outputs rather than silently missing

### Example 4: Source Asset Review

Input:

- a contributor reviews the icon work in the repo before Android export or a
  later refresh

Expected behavior:

- one source-of-truth asset package exists in the repo
- the source package clearly maps to the Android launcher outputs
- future edits do not depend on opaque hand-edited PNGs alone

## Inputs and Outputs

### Inputs

- the user-provided visual reference direction
- the app's Android launcher integration points:
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/res/`

### Outputs

The feature may add:

- one repo-owned source-of-truth icon asset package
- full-color Android adaptive launcher icon outputs
- required launcher raster outputs or fallback resources
- a monochrome/themed icon layer if in scope
- explicit launcher icon manifest wiring

## Requirements

- `R1` The app MUST ship with an explicit branded Android launcher icon rather
  than relying on the generic default icon path.
- `R2` The launcher icon MUST remain text-free. It must not depend on letters,
  words, or tiny numeric detail for recognition.
- `R3` The launcher icon MUST communicate a bounded visual direction consistent
  with the provided reference:
  - premium / polished
  - performance or metrics oriented
  - upward-growth or progress signal
  - readable as one strong central mark
- `R4` The icon design MUST remain recognizable at normal Android launcher
  size and MUST NOT rely on dense micro-detail that collapses when scaled down.
- `R5` The repo MUST contain one explicit source-of-truth icon asset package
  for future review and regeneration.
- `R6` The source-of-truth package MUST be stored in a stable repo location
  dedicated to icon design assets rather than being implied by generated Android
  outputs alone.
- `R6a` The source-of-truth package MUST make the chosen source format and the
  mapping from source assets to exported Android launcher outputs clear enough
  for a later contributor to regenerate the launcher resources without guessing.
- `R7` The Android app MUST use adaptive launcher icon resources for the
  branded icon integration.
- `R8` The adaptive icon content MUST fit Android launcher mask-safe behavior
  so the icon remains readable under round and non-round masks.
- `R9` `app/src/main/AndroidManifest.xml` MUST declare explicit launcher icon
  attributes that point to the branded app icon resources.
- `R10` The launcher icon integration MUST include explicit Android resource
  outputs under `app/src/main/res/` rather than relying on an external
  generator step at runtime.
- `R11` The first-release icon work MUST stay scoped to launcher branding only.
  It MUST NOT silently change notification icons, splash branding, Play Store
  marketing art, or in-app illustration systems.
- `R12` If monochrome/themed icon support is included in this release, the
  icon MUST remain recognizable in that monochrome form and the resource output
  MUST be explicit.
- `R13` If monochrome/themed icon support is intentionally excluded in this
  release, that exclusion MUST be an explicit scoped decision in the source
  asset/output set rather than an accidental omission.
- `R14` The icon resource integration MUST NOT break app packaging. A normal
  debug build must still assemble successfully after the launcher resources are
  wired.
- `R15` After integration, the branded icon MUST be visibly identifiable on
  normal launcher surfaces and Android app info surfaces rather than only
  existing as resource files in the repo.
- `R16` The final icon direction MUST remain original to this project. It may
  follow the provided reference direction, but it MUST NOT be a direct copy of
  the attached example.

## Invariants

- The app icon remains an Android launcher concern, not a broader branding
  overhaul.
- The icon must be recognizable without text.
- The icon must stay compatible with Android adaptive launcher behavior.
- The repo must preserve a reviewable source-of-truth asset package.

## Error Handling And Boundary Behavior

- If the source asset package exists but Android launcher outputs are not yet
  generated, the work is not ready for release.
- If Android resource outputs exist without a reviewable source asset package,
  the work is incomplete and should not be treated as the durable final state.
- If the icon looks correct in the source file but fails adaptive masking on
  emulator or device launcher review, the Android integration is not accepted.
- If themed/monochrome behavior is not supported in the first cut, that must
  be explicit rather than silently missing from the contract.
- If the icon follows the reference too literally during source review, the
  work is not accepted until the project icon is made more original.

## Compatibility And Migration Expectations

- This feature MUST work within the current Android app structure.
- This feature MUST NOT require data-schema, Room, or business-logic
  migrations.
- Existing app runtime behavior outside launcher branding remains unchanged.

## Observability Expectations

- The icon should be visibly distinct on the launcher and app info surfaces.
- The icon should still read as one strong mark when reduced to launcher size.
- The source asset package and Android outputs should be easy to locate in the
  repo for future review.
- The source asset package should make the chosen asset format and export
  mapping obvious enough that later regeneration does not depend on guesswork.

## Edge Cases

- no existing launcher icon resources in the repo
- adaptive round mask
- adaptive squircle mask
- small launcher-grid icon size
- light launcher background
- dark launcher background
- themed icon mode on supported Android versions
- Android app info surface
- source review against the provided reference direction
- future icon refresh requiring re-export from the source asset package

## Non-Goals

- splash-screen redesign
- notification icon redesign
- Play Store feature graphic or screenshot pack
- in-app dashboard illustrations or decorative artwork
- a generalized asset system for all future brand surfaces

## Acceptance Criteria

- The repo contains a reviewed source-of-truth icon asset package.
- The source asset package makes the chosen source format and export mapping
  clear for future regeneration.
- The Android app declares explicit branded launcher icon resources.
- The launcher icon is adaptive-icon compatible and remains readable under
  launcher masks.
- The icon remains recognizable at normal home-screen size without text.
- The icon is confirmed on launcher and app info surfaces during manual review.
- Themed/monochrome behavior is handled explicitly, either implemented or
  intentionally scoped out.
- The icon direction remains original to this project rather than a direct copy
  of the provided reference.
- `./gradlew.bat --no-daemon :app:assembleDebug` succeeds after icon
  integration.
- Manual emulator/device launcher review confirms the icon is not generic,
  blurry, or clipped.

## Gotchas

- 2026-04-16: This repo currently has no launcher icon resources at all, so
  icon work must create both the source asset path and the Android launcher
  resource path. It is not just a reskin of existing `ic_launcher` files.
