# Create A Branded Android App Icon

## Metadata

- Status: completed
- Created: 2026-04-16
- Updated: 2026-04-16
- Owner: Codex
- Related spec(s):
  - `specs/android-app-icon-refresh.md`
  - `specs/android-app-icon-refresh.test.md`
- Supersedes / Superseded by: none
- Branch / PR: TBD
- Last verified commands:
  - `python docs/design/app-icon/generate_sources.py`
  - `python docs/design/app-icon/export_android_resources.py`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.AndroidAppIconResourceContractTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`

## Purpose / Big picture

The app currently has no branded Android launcher icon resources in the repo.
That leaves the product without a recognizable launcher identity even though
the app has now reached repeated alpha release use.

This initiative creates a durable, reviewable app-icon pipeline so the app can
ship with a polished branded launcher icon instead of a generic default.
Success is visible when:

- the app shows a distinct launcher icon on Android home screens
- the icon reads clearly at small sizes and under Android adaptive masks
- the icon style matches the product direction implied by the attached
  reference example: premium, performance-oriented, and game-metrics related
- the repo keeps one source-of-truth icon asset and explicit exported Android
  outputs instead of ad hoc edited PNGs

## Context and orientation

Current repo state:

- `app/src/main/AndroidManifest.xml` does not declare `android:icon` or
  `android:roundIcon`
- `app/src/main/res/` currently only contains `values/`
- there are no `mipmap-*`, `drawable/`, or adaptive-icon resources for a
  launcher icon in the repo today
- the project already ships user-facing Android releases, so launcher branding
  now matters in the installed app experience

Important Android context:

- modern Android launchers expect adaptive icons
- Android 13+ also benefits from a monochrome icon layer for themed icons
- simply dropping one raster PNG into the repo is not best practice for this
  project because it creates weak scaling behavior and no clear source of truth

User-provided direction:

- the attached example suggests the desired visual language:
  - premium gold frame
  - upward growth/analytics signal
  - strong central silhouette
  - readable without text

Likely implementation touch points later:

- `app/src/main/AndroidManifest.xml`
- new resource folders under `app/src/main/res/`:
  - `mipmap-anydpi-v26/`
  - `mipmap-*/`
  - `drawable/`
  - possibly `values/colors.xml` if icon background color is tokenized there
- one source-of-truth asset location for design review and regeneration, likely:
  - `docs/design/app-icon/`
  - or another explicit repo-owned design folder chosen by the future spec

## Constraints

- The icon must remain readable at small launcher sizes.
- The icon must not rely on text, tiny numbers, or dense detail.
- The icon must fit Android adaptive icon safe zones and masking behavior.
- The first release of this work must focus on the Android launcher icon only.
- Do not silently expand this into a full branding redesign, splash screen
  redesign, or Play Store marketing refresh unless separately planned.
- The repo must keep one explicit source-of-truth asset or source package.
- Exported Android icon files must be reviewable and reproducible rather than
  hand-edited opaque binaries with no source context.
- The icon should be original to this project even if it follows the attached
  reference direction.

Non-goals for this initiative:

- new in-app illustrations or dashboard art
- splash-screen redesign
- notification icon redesign
- Play Store feature graphic or screenshot marketing pack
- a generalized asset pipeline for every future brand surface

## Done when

This initiative is done when all of the following are true:

- the repo contains a reviewed launcher-icon source asset package
- the Android app uses a branded adaptive launcher icon
- the app declares explicit launcher icon resources in the manifest
- the icon remains recognizable under round/adaptive masks and small launcher
  sizes
- a monochrome/themed icon path is handled explicitly for supported Android
  versions, either implemented or intentionally scoped out by the spec
- the icon work is verified by `:app:assembleDebug` plus manual emulator/device
  review on launcher and app info surfaces
- the branded icon is confirmed on real Android launcher surfaces rather than
  only existing as resource files in the repo

## Milestones

### Milestone 1: Define The Icon Contract

Scope:

- create a new feature spec and test/verification spec for the app icon
- define the visual direction, deliverables, safe-zone expectations, and
  Android-specific outputs
- define whether monochrome/themed icon support is in or out for the first cut
- choose the canonical source asset format and repo folder for the icon source
  package so later exports are reproducible

Files or components touched:

- `specs/android-app-icon-refresh.md`
- `specs/android-app-icon-refresh.test.md`
- possibly a small reference note under `docs/design/` if needed by the spec

Dependencies:

- this plan
- the attached user reference image

Risk:

- trying to implement art direction directly in code without a reviewable
  contract
- leaving Android icon outputs ambiguous
- silently expanding scope into broader branding work

Validation commands:

- planning/spec-review only in this milestone

Expected observable result:

- one approved contract defines:
  - visual direction
  - source-of-truth asset format and location
  - required Android resource outputs
  - adaptive icon and themed-icon expectations
  - manual review checklist

### Milestone 2: Create The Source Asset Package

Scope:

- create the icon source asset package based on the approved direction
- keep one canonical source asset and supporting notes in the repo
- prepare foreground/background/monochrome layers or equivalent approved source
  outputs

Files or components touched:

- a new source asset folder, likely under `docs/design/app-icon/`
- optional small README or notes file beside the source assets

Dependencies:

- milestone-1 approved spec

Risk:

- art that looks good full-size but collapses at launcher size
- no clear source-of-truth file for future edits
- generated imagery that is too noisy or too close to the reference example
- the final icon follows the attached reference too literally and fails the
  originality bar for project branding

Validation commands:

- source-asset review only in this milestone:
  - full-size source review
  - small-size preview review
  - adaptive safe-zone check
  - originality review against the reference direction

Expected observable result:

- the repo contains one reviewable icon source package that can be approved
  before Android integration, including the approved source format/folder and
  any required foreground, background, and monochrome source outputs

### Milestone 3: Export And Integrate Android Launcher Resources

Scope:

- add adaptive launcher icon resources and required raster fallbacks
- wire the manifest to explicit launcher icon resources
- add monochrome/themed icon resource if the spec requires it

Files or components touched:

- `app/src/main/AndroidManifest.xml`
- new resource files under `app/src/main/res/`:
  - `mipmap-anydpi-v26/`
  - `mipmap-*/`
  - `drawable/`
  - optionally `values/colors.xml`

Dependencies:

- milestone-2 approved source assets

Risk:

- clipped icon content under adaptive masks
- blurry or inconsistent raster exports
- manifest/resource wiring mistakes that break the Android build

Validation commands:

- `./gradlew.bat --no-daemon :app:assembleDebug`
- verify `app/src/main/AndroidManifest.xml` points `android:icon` and
  `android:roundIcon` at the branded launcher resources
- verify the expected branded `mipmap-*` / adaptive icon resource files exist
  in the repo for the chosen Android output set

Expected observable result:

- a debug build contains explicit branded launcher icon resources and the app
  no longer relies on the generic default icon path

### Milestone 4: Manual Launcher Review And Release Readiness

Scope:

- validate the icon on emulator and/or device launcher surfaces
- verify small-size readability, mask safety, and themed-icon behavior if
  included
- capture any durable asset-pipeline or Android-icon gotchas in docs if needed

Files or components touched:

- active plan file
- optionally `docs/workflows.md` only if a durable launcher-icon verification
  convention is discovered

Dependencies:

- milestone 3

Risk:

- icon looks polished in the asset file but muddy on the real launcher
- themed icon or round-mask behavior differs from expectation
- release builds ship before launcher review is completed

Validation commands:

- `./gradlew.bat --no-daemon :app:assembleDebug`
- manual emulator/device review on launcher and app info surfaces

Manual acceptance checklist:

- icon is recognizable on the home screen at normal launcher size
- icon remains readable under adaptive masking
- no critical part of the design is clipped at the edges
- icon still reads as this app without text
- icon is visible as the branded app icon on launcher and app info surfaces
- if monochrome/themed icon is in scope, it remains recognizable in themed mode

Expected observable result:

- the launcher icon is ready to ship in a release without looking generic,
  blurry, or clipped

## Progress

- [x] 2026-04-16: Confirmed the current repo has no explicit launcher icon
  resources and the manifest does not declare custom icon attributes.
- [x] 2026-04-16: Created this concrete plan for a branded Android icon
  initiative.
- [x] 2026-04-16: Milestone 1 complete.
  - done: `specs/android-app-icon-refresh.md`
  - done: `specs/android-app-icon-refresh.test.md`
  - note: spec-review tightened source-package observability, launcher/app-info
    visibility, and originality requirements before test-spec generation
- [x] 2026-04-16: Milestone 2 complete.
  - done: `docs/design/app-icon/README.md`
  - done: `docs/design/app-icon/generate_sources.py`
  - done: generated source outputs under `docs/design/app-icon/source/`
  - note: source package now includes full-color background/foreground,
    monochrome output, full-size preview, and review sheet assets
- [x] 2026-04-16: Milestone 3 complete.
  - done: `docs/design/app-icon/export_android_resources.py`
  - done: exported adaptive XML, density-specific adaptive layers, and legacy
    launcher PNGs under `app/src/main/res/`
  - done: `app/src/main/AndroidManifest.xml` now declares branded
    `android:icon` and `android:roundIcon`
- [x] 2026-04-16: Milestone 4 complete.
  - done: manual emulator review on launcher, app drawer, and app info surfaces
  - done: themed-icon mode verified through Pixel `Wallpaper & style`
  - done: review screenshots saved under `build/icon-review/`

## Surprises & Discoveries

- The app currently ships from a repo that has no launcher icon resources at
  all under `app/src/main/res/`.
- Because the manifest also omits explicit icon attributes, this is not a
  simple reskin; it is a full Android launcher-icon integration initiative.
- A generator-backed source package is a better fit than a single hand-edited
  PNG here because the repo had no prior icon pipeline or editable source
  format to build from.
- Density-specific adaptive drawables are a safer Android export target here
  than a single `drawable-nodpi` asset because they keep the launcher layers
  aligned with normal Android density selection.
- The Pixel emulator exposes the themed-icon toggle through
  `Wallpaper & style` on the `Home screen` tab, which is enough to verify the
  monochrome launcher icon path without needing a special adb-only setting.

## Decision Log

- Decision: treat the icon work as a full plan/spec initiative instead of a
  quick asset drop.
  - Rationale: there is no existing icon pipeline in the repo, and Android
    adaptive icon requirements need to be handled explicitly.
  - Date/Author: 2026-04-16 / Codex

- Decision: require one source-of-truth asset package before resource export.
  - Rationale: this avoids unmaintainable binary-only icon edits and keeps
    future refreshes reviewable.
  - Date/Author: 2026-04-16 / Codex

- Decision: keep the first icon initiative scoped to Android launcher branding,
  not a full product-brand overhaul.
  - Rationale: the user request is specifically about a beautiful app icon, and
    widening scope would slow delivery and muddy acceptance criteria.
  - Date/Author: 2026-04-16 / Codex

- Decision: use a repo-owned Python generator plus layered 1024px PNG masters
  as the initial source package format under `docs/design/app-icon/`.
  - Rationale: this keeps the icon source reproducible and reviewable without
    depending on an unavailable external design export tool in the repo.
  - Date/Author: 2026-04-16 / Codex

- Decision: include a monochrome source output in the first source package.
  - Rationale: the spec requires themed-icon behavior to be handled explicitly,
    and carrying a monochrome source layer now makes milestone 3 integration
    cleaner.
  - Date/Author: 2026-04-16 / Codex

- Decision: export adaptive icon layers as density-specific `drawable-*`
  resources and use `mipmap-anydpi-v26` / `mipmap-anydpi-v33` for adaptive XML.
  - Rationale: this keeps adaptive icon integration closer to normal Android
    launcher resource resolution and avoids relying on one `nodpi` raster layer.
  - Date/Author: 2026-04-16 / Codex

## Validation and Acceptance

Planning-time validation completed in this turn:

- reviewed `docs/plan.md`
- reviewed `app/src/main/AndroidManifest.xml`
- inspected `app/src/main/res/`
- confirmed there are currently no launcher icon resources in the repo

Implementation acceptance for this initiative should require:

- the icon spec and test-spec exist and are approved
- `./gradlew.bat --no-daemon :app:assembleDebug`
- manual emulator/device launcher review
- explicit confirmation that the manifest uses the branded icon resources
- explicit confirmation that the expected branded launcher resource files exist

## Validation Notes

 - Milestone 2 is source-asset work only, so no Gradle commands were required
   in this turn.
- The current repository state confirms that icon work must create both the
  asset pipeline and the Android integration path.
- The icon contract and test spec now exist:
  - `specs/android-app-icon-refresh.md`
  - `specs/android-app-icon-refresh.test.md`
- Spec review found the original contract was close but needed tighter
  observability for source-package regeneration, launcher/app-info visibility,
  and originality against the provided reference before test-spec generation.
- Source package validation completed with:
  - `python docs/design/app-icon/generate_sources.py`
  - file presence check under `docs/design/app-icon/source/`
  - visual inspection of:
    - `kings-metric-icon-preview-1024.png`
    - `kings-metric-icon-review-sheet-1600.png`
- The attached reference image was used as direction only and was not copied
  into the source package as a project icon asset.
- Milestone 3 validation completed with:
  - `python docs/design/app-icon/export_android_resources.py`
  - `./gradlew.bat --no-daemon :core:test --tests "com.kingsmetric.release.AndroidAppIconResourceContractTest"`
  - `./gradlew.bat --no-daemon :app:assembleDebug`
- Manual emulator review completed with:
  - `build/icon-review/home.png`
  - `build/icon-review/app-drawer.png`
  - `build/icon-review/app-info.png`
  - `build/icon-review/customization-home-tab.png`
  - `build/icon-review/home-themed.png`
- The launcher icon is visible and recognizable on launcher, app drawer, and
  app info surfaces.
- The themed-icon path was verified on the Pixel emulator by enabling
  `Themed icons` from `Wallpaper & style > Home screen`, and the home-screen
  capture confirms the monochrome icon remains recognizable.

## Idempotence and Recovery

- Re-running planning for this initiative is safe because it only adds a new
  concrete plan file and an index entry.
- Milestone 2 should keep the source asset package additive and reviewable so
  that later exports can be regenerated rather than hand-fixed repeatedly.
- If milestone 3 produces clipped or blurry launcher resources, the safe
  rollback is to keep the source assets and back out only the Android resource
  integration until exports are corrected.
- If the approved icon direction changes mid-work, keep the plan and source
  asset history instead of overwriting it silently.

## Outcomes & Retrospective

This plan establishes the best-practice path for launcher icon work in this
repo:

- define the contract first
- keep one source-of-truth asset package
- export proper Android launcher resources
- verify on real launcher surfaces before release

This icon initiative is now complete locally. The next correct step is to
package it into a PR for review.
