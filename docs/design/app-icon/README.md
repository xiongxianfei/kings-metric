# Kings Metric Android App Icon Source Package

## Purpose

This folder is the source-of-truth asset package for the Android launcher icon
initiative defined in:

- [android-app-icon-refresh](../../../specs/android-app-icon-refresh.md)

It exists so launcher icon work stays reviewable and reproducible instead of
becoming a pile of opaque exported Android binaries.

## Canonical Source Format

The canonical source format for this first icon package is:

- one reproducible Python generator:
  - `generate_sources.py`
- one reproducible Android export generator:
  - `export_android_resources.py`
- generated layered 1024x1024 PNG masters under:
  - `source/`

This means the durable source of truth is not a single hand-edited PNG. The
generator defines the icon geometry, palette, and layer structure, and the
generated PNG masters are the reviewable outputs that later Android resource
exports should follow.

## Files

- `generate_sources.py`
  - Generates the icon source outputs in `source/`
- `export_android_resources.py`
  - Exports Android launcher resources into `app/src/main/res/`
- `source/kings-metric-icon-background-1024.png`
  - Full-canvas adaptive icon background source
- `source/kings-metric-icon-foreground-1024.png`
  - Transparent adaptive icon foreground source
- `source/kings-metric-icon-monochrome-1024.png`
  - Transparent monochrome source for themed icon support
- `source/kings-metric-icon-preview-1024.png`
  - Full-color composite preview for full-size review
- `source/kings-metric-icon-review-sheet-1600.png`
  - Composite review image for:
    - full-size review
    - small-size preview review
    - adaptive safe-zone review
    - light/dark launcher-surface review

## Visual Direction

The icon follows the approved direction from the spec:

- premium / polished
- performance / metrics oriented
- upward-growth signal
- one strong central mark
- text-free

The resulting mark is intentionally original to this project. It uses:

- a dark premium badge
- ascending gold metric bars
- an emerald growth arrow
- a diamond signal marker

It does not reuse the attached reference image as a source asset and does not
copy the reference composition literally.

## Export Mapping

Milestone 3 should treat these files as the Android-export inputs:

- `background-1024`
  - base for the adaptive icon background layer
- `foreground-1024`
  - base for the adaptive icon foreground layer
- `monochrome-1024`
  - base for Android themed icon output if that path is included in integration
- `preview-1024`
  - review-only composite, not an Android resource
- `review-sheet-1600`
  - review-only sheet, not an Android resource

Milestone 3 export targets:

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v33/ic_launcher_round.xml`
- `app/src/main/res/drawable-*/ic_launcher_background.png`
- `app/src/main/res/drawable-*/ic_launcher_foreground.png`
- `app/src/main/res/drawable-*/ic_launcher_monochrome.png`
- `app/src/main/res/mipmap-*/ic_launcher.png`
- `app/src/main/res/mipmap-*/ic_launcher_round.png`

## Regeneration

From the repo root:

```powershell
python docs/design/app-icon/generate_sources.py
```

The script is idempotent for this package. Re-running it replaces the generated
files in `source/` with the current canonical outputs.

To export Android launcher resources from the reviewed source package:

```powershell
python docs/design/app-icon/export_android_resources.py
```

That export writes:

- adaptive icon XML resources in `mipmap-anydpi-v26/` and `mipmap-anydpi-v33/`
- density-specific adaptive layers in `drawable-*`
- legacy launcher PNGs in `mipmap-*`

## Review Notes

The generated package is ready for the milestone-2 review gates:

- full-size source review:
  - `kings-metric-icon-preview-1024.png`
- small-size preview review:
  - `kings-metric-icon-review-sheet-1600.png`
- adaptive safe-zone review:
  - `kings-metric-icon-review-sheet-1600.png`
- originality review against the reference direction:
  - compare the generated icon to the attached reference direction, not as a
    source image to trace or reuse

## Reference Handling

The user-provided reference image is direction only. It is not part of this
source asset package and should not be treated as a ship-ready source file for
the project icon.
