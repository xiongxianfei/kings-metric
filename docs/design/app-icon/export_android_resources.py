from __future__ import annotations

from pathlib import Path

from PIL import Image

from generate_sources import ROOT, main as generate_source_package


REPOSITORY_ROOT = ROOT.parents[2]
RES_DIR = REPOSITORY_ROOT / "app" / "src" / "main" / "res"
SOURCE_DIR = ROOT / "source"

SOURCE_BACKGROUND = SOURCE_DIR / "kings-metric-icon-background-1024.png"
SOURCE_FOREGROUND = SOURCE_DIR / "kings-metric-icon-foreground-1024.png"
SOURCE_MONOCHROME = SOURCE_DIR / "kings-metric-icon-monochrome-1024.png"
SOURCE_PREVIEW = SOURCE_DIR / "kings-metric-icon-preview-1024.png"

LEGACY_MIPMAP_SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}

ADAPTIVE_DRAWABLE_SIZES = {
    "mdpi": 108,
    "hdpi": 162,
    "xhdpi": 216,
    "xxhdpi": 324,
    "xxxhdpi": 432,
}


def ensure_source_package() -> None:
    generate_source_package()


def ensure_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def load_rgba(path: Path) -> Image.Image:
    return Image.open(path).convert("RGBA")


def save_resized_png(source: Image.Image, size: int, destination: Path) -> None:
    ensure_directory(destination.parent)
    resized = source.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(destination)


def write_text(path: Path, contents: str) -> None:
    ensure_directory(path.parent)
    path.write_text(contents, encoding="utf-8")


def export_adaptive_layers() -> None:
    background = load_rgba(SOURCE_BACKGROUND)
    foreground = load_rgba(SOURCE_FOREGROUND)
    monochrome = load_rgba(SOURCE_MONOCHROME)

    for density, size in ADAPTIVE_DRAWABLE_SIZES.items():
        drawable_dir = RES_DIR / f"drawable-{density}"
        save_resized_png(background, size, drawable_dir / "ic_launcher_background.png")
        save_resized_png(foreground, size, drawable_dir / "ic_launcher_foreground.png")
        save_resized_png(monochrome, size, drawable_dir / "ic_launcher_monochrome.png")


def export_legacy_mipmap_icons() -> None:
    preview = load_rgba(SOURCE_PREVIEW)

    for density, size in LEGACY_MIPMAP_SIZES.items():
        mipmap_dir = RES_DIR / f"mipmap-{density}"
        save_resized_png(preview, size, mipmap_dir / "ic_launcher.png")
        save_resized_png(preview, size, mipmap_dir / "ic_launcher_round.png")


def export_adaptive_icon_xml() -> None:
    adaptive_v26 = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""

    adaptive_v33 = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
"""

    write_text(RES_DIR / "mipmap-anydpi-v26" / "ic_launcher.xml", adaptive_v26)
    write_text(RES_DIR / "mipmap-anydpi-v26" / "ic_launcher_round.xml", adaptive_v26)
    write_text(RES_DIR / "mipmap-anydpi-v33" / "ic_launcher.xml", adaptive_v33)
    write_text(RES_DIR / "mipmap-anydpi-v33" / "ic_launcher_round.xml", adaptive_v33)


def main() -> None:
    ensure_source_package()
    export_adaptive_layers()
    export_legacy_mipmap_icons()
    export_adaptive_icon_xml()
    print(f"Exported Android launcher icon resources into {RES_DIR}")


if __name__ == "__main__":
    main()
