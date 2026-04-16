from __future__ import annotations

from pathlib import Path
import math

from PIL import Image
from PIL import ImageChops
from PIL import ImageColor
from PIL import ImageDraw
from PIL import ImageFilter


ROOT = Path(__file__).resolve().parent
OUTPUT_DIR = ROOT / "source"

SIZE = 1024
REVIEW_SHEET_WIDTH = 1600
REVIEW_SHEET_HEIGHT = 1200


def rgba(value: str, alpha: int = 255) -> tuple[int, int, int, int]:
    red, green, blue = ImageColor.getrgb(value)
    return red, green, blue, alpha


def ensure_output_dir() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def draw_vertical_gradient(
    image: Image.Image,
    box: tuple[int, int, int, int],
    top_color: tuple[int, int, int, int],
    bottom_color: tuple[int, int, int, int],
) -> None:
    draw = ImageDraw.Draw(image)
    left, top, right, bottom = box
    height = max(1, bottom - top)
    for offset in range(height):
        ratio = offset / max(1, height - 1)
        color = tuple(
            int(top_color[index] + (bottom_color[index] - top_color[index]) * ratio)
            for index in range(4)
        )
        draw.line((left, top + offset, right, top + offset), fill=color)


def add_radial_glow(
    image: Image.Image,
    center: tuple[int, int],
    radius: int,
    color: tuple[int, int, int, int],
) -> None:
    glow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    cx, cy = center
    for ring in range(radius, 0, -8):
        alpha_ratio = ring / radius
        ring_alpha = int(color[3] * (1 - alpha_ratio ** 1.7))
        ring_color = (color[0], color[1], color[2], max(0, ring_alpha))
        draw.ellipse((cx - ring, cy - ring, cx + ring, cy + ring), fill=ring_color)
    image.alpha_composite(glow)


def add_vignette(image: Image.Image, strength: int = 150) -> None:
    vignette = Image.new("L", image.size, 0)
    draw = ImageDraw.Draw(vignette)
    width, height = image.size
    for inset in range(0, width // 2, 10):
        ratio = inset / (width / 2)
        alpha = int(strength * ratio * ratio)
        draw.rounded_rectangle(
            (inset, inset, width - inset, height - inset),
            radius=max(24, 150 - inset // 8),
            outline=min(alpha, 255),
            width=14,
        )
    vignette = vignette.filter(ImageFilter.GaussianBlur(48))
    dark = Image.new("RGBA", image.size, (25, 10, 0, 0))
    dark.putalpha(vignette)
    image.alpha_composite(dark)


def add_rays(image: Image.Image) -> None:
    rays = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(rays)
    cx, cy = SIZE * 0.52, SIZE * 0.32
    outer = SIZE * 0.96
    for index in range(11):
        angle = -104 + index * 14
        spread = 6 if index % 2 == 0 else 8
        p1 = polar_point(cx, cy, outer, angle - spread)
        p2 = polar_point(cx, cy, outer, angle + spread)
        color = rgba("#ffe6a2", 32 if index % 2 == 0 else 22)
        draw.polygon(((cx, cy), p1, p2), fill=color)
    rays = rays.filter(ImageFilter.GaussianBlur(3))
    image.alpha_composite(rays)


def polar_point(cx: float, cy: float, radius: float, degrees: float) -> tuple[float, float]:
    radians = math.radians(degrees)
    return cx + math.cos(radians) * radius, cy + math.sin(radians) * radius


def add_sparkle(draw: ImageDraw.ImageDraw, center: tuple[int, int], size: int, alpha: int) -> None:
    cx, cy = center
    color = rgba("#fff4cf", alpha)
    draw.polygon(
        (
            (cx, cy - size),
            (cx + size * 0.28, cy - size * 0.28),
            (cx + size, cy),
            (cx + size * 0.28, cy + size * 0.28),
            (cx, cy + size),
            (cx - size * 0.28, cy + size * 0.28),
            (cx - size, cy),
            (cx - size * 0.28, cy - size * 0.28),
        ),
        fill=color,
    )


def build_background() -> Image.Image:
    background = Image.new("RGBA", (SIZE, SIZE), rgba("#2a1300"))
    draw_vertical_gradient(
        background,
        (0, 0, SIZE, SIZE),
        rgba("#ffcb67"),
        rgba("#4b2200"),
    )
    add_radial_glow(background, (int(SIZE * 0.52), int(SIZE * 0.28)), 380, rgba("#ffe7a0", 210))
    add_rays(background)

    halo = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    halo_draw = ImageDraw.Draw(halo)
    halo_draw.ellipse((180, 120, 840, 780), outline=rgba("#fff0b8", 42), width=18)
    halo = halo.filter(ImageFilter.GaussianBlur(8))
    background.alpha_composite(halo)

    sparkle_layer = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    sparkle_draw = ImageDraw.Draw(sparkle_layer)
    add_sparkle(sparkle_draw, (220, 356), 26, 180)
    add_sparkle(sparkle_draw, (792, 280), 22, 165)
    add_sparkle(sparkle_draw, (824, 704), 18, 140)
    sparkle_layer = sparkle_layer.filter(ImageFilter.GaussianBlur(1))
    background.alpha_composite(sparkle_layer)

    add_vignette(background)
    return background


def draw_badge_base(image: Image.Image) -> None:
    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        (110, 140, 914, 944),
        radius=148,
        fill=rgba("#120600", 150),
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(24))
    image.alpha_composite(shadow)

    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle((102, 132, 922, 952), radius=156, fill=rgba("#2d1304", 255))
    draw.rounded_rectangle((118, 148, 906, 936), radius=142, fill=rgba("#7b4300", 255))
    draw.rounded_rectangle((134, 164, 890, 920), radius=126, fill=rgba("#2e1806", 255))
    draw.rounded_rectangle((152, 182, 872, 902), radius=112, fill=rgba("#4a2504", 200))
    draw.rounded_rectangle((152, 182, 872, 902), radius=112, outline=rgba("#d08e2f", 165), width=6)


def add_badge_gloss(image: Image.Image) -> None:
    gloss = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(gloss)
    draw.rounded_rectangle((170, 188, 856, 430), radius=98, fill=rgba("#fff1ba", 28))
    gloss = gloss.filter(ImageFilter.GaussianBlur(28))
    image.alpha_composite(gloss)


def draw_metric_bar(
    image: Image.Image,
    box: tuple[int, int, int, int],
    top_color: str,
    bottom_color: str,
) -> None:
    left, top, right, bottom = box
    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle((left + 8, top + 12, right + 8, bottom + 18), radius=20, fill=rgba("#140700", 90))
    shadow = shadow.filter(ImageFilter.GaussianBlur(10))
    image.alpha_composite(shadow)

    bar = Image.new("RGBA", image.size, (0, 0, 0, 0))
    draw_vertical_gradient(bar, box, rgba(top_color), rgba(bottom_color))
    bar_mask = Image.new("L", image.size, 0)
    bar_mask_draw = ImageDraw.Draw(bar_mask)
    bar_mask_draw.rounded_rectangle(box, radius=20, fill=255)
    bar.putalpha(bar_mask)
    image.alpha_composite(bar)

    draw = ImageDraw.Draw(image)
    draw.rounded_rectangle(box, radius=20, outline=rgba("#8a4d10", 220), width=5)
    highlight = Image.new("RGBA", image.size, (0, 0, 0, 0))
    h_draw = ImageDraw.Draw(highlight)
    h_draw.rounded_rectangle((left + 10, top + 10, left + 24, bottom - 12), radius=10, fill=rgba("#fff8d1", 46))
    highlight = highlight.filter(ImageFilter.GaussianBlur(4))
    image.alpha_composite(highlight)


def draw_arrow(image: Image.Image) -> None:
    path_points = [
        (214, 652),
        (330, 596),
        (400, 610),
        (504, 510),
        (592, 520),
        (674, 420),
        (782, 366),
    ]

    top_points = []
    bottom_points = []
    for index, point in enumerate(path_points):
        x, y = point
        if index == 0:
            dx = path_points[index + 1][0] - x
            dy = path_points[index + 1][1] - y
        else:
            dx = x - path_points[index - 1][0]
            dy = y - path_points[index - 1][1]
        length = math.hypot(dx, dy) or 1
        offset_x = -dy / length * 28
        offset_y = dx / length * 28
        top_points.append((x + offset_x, y + offset_y))
        bottom_points.append((x - offset_x, y - offset_y))

    arrow_head = [(846, 320), (784, 294), (798, 356)]
    arrow_polygon = top_points + arrow_head + list(reversed(bottom_points))

    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_polygon = [(x + 8, y + 10) for x, y in arrow_polygon]
    shadow_draw.polygon(shadow_polygon, fill=rgba("#170900", 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(12))
    image.alpha_composite(shadow)

    outline = ImageDraw.Draw(image)
    outline.polygon(arrow_polygon, fill=rgba("#275700", 255))

    inner_polygon = inset_polygon(arrow_polygon, 8)
    inner = Image.new("RGBA", image.size, (0, 0, 0, 0))
    inner_draw = ImageDraw.Draw(inner)
    inner_draw.polygon(inner_polygon, fill=rgba("#67d81c", 255))
    image.alpha_composite(inner)

    highlight = Image.new("RGBA", image.size, (0, 0, 0, 0))
    h_draw = ImageDraw.Draw(highlight)
    h_draw.polygon(inset_polygon(top_points + arrow_head, 3), fill=rgba("#d3ff92", 70))
    highlight = highlight.filter(ImageFilter.GaussianBlur(5))
    image.alpha_composite(highlight)


def inset_polygon(points: list[tuple[float, float]], inset: float) -> list[tuple[float, float]]:
    cx = sum(point[0] for point in points) / len(points)
    cy = sum(point[1] for point in points) / len(points)
    inset_points = []
    for x, y in points:
        dx = x - cx
        dy = y - cy
        length = math.hypot(dx, dy) or 1
        inset_points.append((x - dx / length * inset, y - dy / length * inset))
    return inset_points


def draw_signal_diamond(image: Image.Image) -> None:
    shadow = Image.new("RGBA", image.size, (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    diamond = [(822, 240), (864, 282), (822, 324), (780, 282)]
    shadow_draw.polygon([(x + 8, y + 10) for x, y in diamond], fill=rgba("#100400", 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(10))
    image.alpha_composite(shadow)

    draw = ImageDraw.Draw(image)
    draw.polygon(diamond, fill=rgba("#f6b93d", 255), outline=rgba("#6b3400", 220))
    inner = [(822, 252), (852, 282), (822, 312), (792, 282)]
    draw.polygon(inner, fill=rgba("#ffe6a1", 220))
    add_sparkle(draw, (882, 244), 18, 170)


def build_foreground() -> Image.Image:
    foreground = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw_badge_base(foreground)
    add_badge_gloss(foreground)

    draw_metric_bar(foreground, (224, 626, 318, 826), "#d08d24", "#f6d577")
    draw_metric_bar(foreground, (352, 548, 446, 826), "#cc881b", "#f1cf6e")
    draw_metric_bar(foreground, (480, 476, 574, 826), "#c98319", "#f0ca65")
    draw_metric_bar(foreground, (608, 378, 702, 826), "#be7713", "#ebbe59")

    draw = ImageDraw.Draw(foreground)
    draw.rounded_rectangle((206, 824, 726, 846), radius=12, fill=rgba("#7b4300", 175))

    draw_arrow(foreground)
    draw_signal_diamond(foreground)

    sparkle_layer = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    sparkle_draw = ImageDraw.Draw(sparkle_layer)
    add_sparkle(sparkle_draw, (246, 344), 20, 155)
    add_sparkle(sparkle_draw, (768, 692), 15, 120)
    sparkle_layer = sparkle_layer.filter(ImageFilter.GaussianBlur(1))
    foreground.alpha_composite(sparkle_layer)
    return foreground


def build_monochrome() -> Image.Image:
    monochrome = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(monochrome)
    fill = rgba("#ffffff", 255)

    draw.rounded_rectangle((118, 148, 906, 936), radius=142, outline=fill, width=34)
    draw.rounded_rectangle((224, 626, 318, 826), radius=20, fill=fill)
    draw.rounded_rectangle((352, 548, 446, 826), radius=20, fill=fill)
    draw.rounded_rectangle((480, 476, 574, 826), radius=20, fill=fill)
    draw.rounded_rectangle((608, 378, 702, 826), radius=20, fill=fill)
    draw.rounded_rectangle((206, 824, 726, 846), radius=12, fill=fill)

    path_points = [
        (214, 652),
        (330, 596),
        (400, 610),
        (504, 510),
        (592, 520),
        (674, 420),
        (782, 366),
    ]
    top_points = []
    bottom_points = []
    for index, point in enumerate(path_points):
        x, y = point
        if index == 0:
            dx = path_points[index + 1][0] - x
            dy = path_points[index + 1][1] - y
        else:
            dx = x - path_points[index - 1][0]
            dy = y - path_points[index - 1][1]
        length = math.hypot(dx, dy) or 1
        offset_x = -dy / length * 28
        offset_y = dx / length * 28
        top_points.append((x + offset_x, y + offset_y))
        bottom_points.append((x - offset_x, y - offset_y))
    arrow_head = [(846, 320), (784, 294), (798, 356)]
    draw.polygon(top_points + arrow_head + list(reversed(bottom_points)), fill=fill)
    draw.polygon([(822, 240), (864, 282), (822, 324), (780, 282)], fill=fill)
    return monochrome


def composite_preview(background: Image.Image, foreground: Image.Image) -> Image.Image:
    preview = background.copy()
    preview.alpha_composite(foreground)
    return preview


def build_mask_preview(preview: Image.Image, shape: str, size: int, surface: str) -> Image.Image:
    surface_color = rgba("#f0e5d6") if surface == "light" else rgba("#2a2015")
    tile = Image.new("RGBA", (360, 360), surface_color)
    icon = preview.resize((size, size), Image.Resampling.LANCZOS)
    x = (tile.width - size) // 2
    y = (tile.height - size) // 2
    tile.alpha_composite(icon, (x, y))

    overlay = Image.new("RGBA", tile.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    if shape == "circle":
        draw.ellipse((x, y, x + size, y + size), outline=rgba("#ffffff", 110), width=4)
    else:
        draw.rounded_rectangle((x, y, x + size, y + size), radius=82, outline=rgba("#ffffff", 110), width=4)
    tile.alpha_composite(overlay)
    return tile


def build_small_preview_row(preview: Image.Image, surface: str) -> Image.Image:
    surface_color = rgba("#efe2cf") if surface == "light" else rgba("#25190f")
    row = Image.new("RGBA", (720, 220), surface_color)
    sizes = [168, 112, 72]
    positions = [40, 284, 526]
    for size, left in zip(sizes, positions):
        icon = preview.resize((size, size), Image.Resampling.LANCZOS)
        top = (row.height - size) // 2
        row.alpha_composite(icon, (left, top))
    return row


def build_review_sheet(preview: Image.Image) -> Image.Image:
    sheet = Image.new("RGBA", (REVIEW_SHEET_WIDTH, REVIEW_SHEET_HEIGHT), rgba("#1a1009"))

    panel = Image.new("RGBA", (REVIEW_SHEET_WIDTH, REVIEW_SHEET_HEIGHT), (0, 0, 0, 0))
    draw = ImageDraw.Draw(panel)
    draw.rounded_rectangle((40, 40, 760, 760), radius=36, fill=rgba("#352012"), outline=rgba("#a56a2d", 140), width=4)
    draw.rounded_rectangle((840, 40, 1560, 380), radius=36, fill=rgba("#352012"), outline=rgba("#a56a2d", 140), width=4)
    draw.rounded_rectangle((840, 420, 1560, 760), radius=36, fill=rgba("#352012"), outline=rgba("#a56a2d", 140), width=4)
    draw.rounded_rectangle((40, 820, 760, 1080), radius=36, fill=rgba("#352012"), outline=rgba("#a56a2d", 140), width=4)
    draw.rounded_rectangle((840, 820, 1560, 1080), radius=36, fill=rgba("#352012"), outline=rgba("#a56a2d", 140), width=4)
    sheet.alpha_composite(panel)

    large = preview.resize((620, 620), Image.Resampling.LANCZOS)
    sheet.alpha_composite(large, (90, 90))

    circle_light = build_mask_preview(preview, "circle", 220, "light")
    squircle_light = build_mask_preview(preview, "squircle", 220, "light")
    circle_dark = build_mask_preview(preview, "circle", 220, "dark")
    squircle_dark = build_mask_preview(preview, "squircle", 220, "dark")

    sheet.alpha_composite(circle_light, (880, 50))
    sheet.alpha_composite(squircle_light, (1190, 50))
    sheet.alpha_composite(circle_dark, (880, 430))
    sheet.alpha_composite(squircle_dark, (1190, 430))

    small_light = build_small_preview_row(preview, "light")
    small_dark = build_small_preview_row(preview, "dark")
    sheet.alpha_composite(small_light, (40, 840))
    sheet.alpha_composite(small_dark, (840, 840))

    return sheet


def save_image(image: Image.Image, name: str) -> None:
    image.save(OUTPUT_DIR / name)


def main() -> None:
    ensure_output_dir()
    background = build_background()
    foreground = build_foreground()
    monochrome = build_monochrome()
    preview = composite_preview(background, foreground)
    review_sheet = build_review_sheet(preview)

    save_image(background, "kings-metric-icon-background-1024.png")
    save_image(foreground, "kings-metric-icon-foreground-1024.png")
    save_image(monochrome, "kings-metric-icon-monochrome-1024.png")
    save_image(preview, "kings-metric-icon-preview-1024.png")
    save_image(review_sheet, "kings-metric-icon-review-sheet-1600.png")

    print(f"Generated icon sources in {OUTPUT_DIR}")


if __name__ == "__main__":
    main()
