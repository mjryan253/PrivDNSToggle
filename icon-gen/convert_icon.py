#!/usr/bin/env python3
"""
convert_icon.py  --  Generate all Android icon assets from funnel-crop-OLED.png.

Usage:
    pip install -r requirements.txt
    python convert_icon.py

Reads  ../funnel-crop-OLED.png  (relative to this script).
Writes output/res/  with mipmap-* and drawable-* folders ready to copy
into app/src/main/res/.
"""

from pathlib import Path
from PIL import Image, ImageDraw

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
SCRIPT_DIR = Path(__file__).resolve().parent
SOURCE_PNG = SCRIPT_DIR / ".." / "funnel-crop-OLED.png"
OUTPUT_RES = SCRIPT_DIR / "output" / "res"

# ---------------------------------------------------------------------------
# Density definitions
# ---------------------------------------------------------------------------
# Android density multipliers (mdpi = 1x baseline)
DENSITIES = {
    "mdpi":    1.0,
    "hdpi":    1.5,
    "xhdpi":   2.0,
    "xxhdpi":  3.0,
    "xxxhdpi": 4.0,
}

LAUNCHER_BASE_DP   = 48   # standard launcher icon base size in dp
FOREGROUND_BASE_DP = 108  # adaptive-icon full canvas size in dp
TILE_BASE_DP       = 24   # Quick Settings tile icon base size in dp

# Background color that matches the source PNG background (dark gray)
BG_COLOR_HEX = "#3D3D3D"

# Luminance threshold for separating foreground from background when
# creating the monochrome QS tile icon.  Pixels with luminance below this
# value are treated as background and made transparent.
LUMINANCE_THRESHOLD = 80


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def ensure_rgba(img: Image.Image) -> Image.Image:
    """Convert image to RGBA if it isn't already."""
    if img.mode != "RGBA":
        return img.convert("RGBA")
    return img


def resize(img: Image.Image, size: int) -> Image.Image:
    """High-quality resize to (size x size)."""
    return img.resize((size, size), Image.LANCZOS)


def make_round(img: Image.Image) -> Image.Image:
    """Apply a circular mask so corners become transparent."""
    img = ensure_rgba(img)
    w, h = img.size
    mask = Image.new("L", (w, h), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, w, h), fill=255)
    out = img.copy()
    out.putalpha(mask)
    return out


def make_adaptive_foreground(img: Image.Image, canvas_px: int) -> Image.Image:
    """
    Place the icon inside the 72/108 safe-zone of an adaptive-icon
    foreground layer.  The result is a transparent canvas of
    *canvas_px x canvas_px* with the icon centered in the inner 66.7 %.
    """
    img = ensure_rgba(img)
    safe_zone_px = int(round(canvas_px * 72 / 108))
    icon_resized = resize(img, safe_zone_px)
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    offset = (canvas_px - safe_zone_px) // 2
    canvas.paste(icon_resized, (offset, offset), icon_resized)
    return canvas


def make_monochrome_silhouette(img: Image.Image) -> Image.Image:
    """
    Produce a white-on-transparent silhouette suitable for a Quick Settings
    tile icon.  The dark background is removed; all remaining foreground
    pixels become solid white while preserving their opacity shape.
    """
    img = ensure_rgba(img)
    pixels = img.load()
    w, h = img.size
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    out_pixels = out.load()

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            # Perceived luminance
            lum = 0.299 * r + 0.587 * g + 0.114 * b
            if a > 0 and lum >= LUMINANCE_THRESHOLD:
                # Keep as white with original alpha
                out_pixels[x, y] = (255, 255, 255, a)
            # else: stays transparent (0,0,0,0)

    return out


def write_png(img: Image.Image, path: Path) -> None:
    """Save an RGBA image as PNG, creating parent dirs as needed."""
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(str(path), "PNG")
    print(f"  wrote {path.relative_to(SCRIPT_DIR)}")


def write_text(text: str, path: Path) -> None:
    """Write a text file, creating parent dirs as needed."""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    print(f"  wrote {path.relative_to(SCRIPT_DIR)}")


# ---------------------------------------------------------------------------
# Main generation
# ---------------------------------------------------------------------------

def generate_launcher_icons(src: Image.Image) -> None:
    """Legacy full-bleed square launcher icons at every density."""
    print("\n[1/5] Legacy launcher icons (ic_launcher.png)")
    for density, scale in DENSITIES.items():
        size = int(round(LAUNCHER_BASE_DP * scale))
        icon = resize(src, size)
        write_png(icon, OUTPUT_RES / f"mipmap-{density}" / "ic_launcher.png")


def generate_round_icons(src: Image.Image) -> None:
    """Circular launcher icons at every density."""
    print("\n[2/5] Round launcher icons (ic_launcher_round.png)")
    for density, scale in DENSITIES.items():
        size = int(round(LAUNCHER_BASE_DP * scale))
        icon = make_round(resize(src, size))
        write_png(icon, OUTPUT_RES / f"mipmap-{density}" / "ic_launcher_round.png")


def generate_foreground_icons(src: Image.Image) -> None:
    """Adaptive-icon foreground layer at every density."""
    print("\n[3/5] Adaptive foreground icons (ic_launcher_foreground.png)")
    for density, scale in DENSITIES.items():
        canvas_px = int(round(FOREGROUND_BASE_DP * scale))
        fg = make_adaptive_foreground(src, canvas_px)
        write_png(fg, OUTPUT_RES / f"mipmap-{density}" / "ic_launcher_foreground.png")


def generate_tile_icons(src: Image.Image) -> None:
    """Monochrome QS tile icons at every density."""
    print("\n[4/5] Quick Settings tile icons (ic_dns.png)")
    mono = make_monochrome_silhouette(src)
    for density, scale in DENSITIES.items():
        size = int(round(TILE_BASE_DP * scale))
        icon = resize(mono, size)
        write_png(icon, OUTPUT_RES / f"drawable-{density}" / "ic_dns.png")


def generate_adaptive_xml() -> None:
    """Write the adaptive-icon XML descriptors."""
    print("\n[5/5] Adaptive icon XML files")

    launcher_xml = """\
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
"""
    write_text(
        launcher_xml,
        OUTPUT_RES / "mipmap-anydpi-v26" / "ic_launcher.xml",
    )
    write_text(
        launcher_xml,
        OUTPUT_RES / "mipmap-anydpi-v26" / "ic_launcher_round.xml",
    )


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    if not SOURCE_PNG.exists():
        raise FileNotFoundError(
            f"Source image not found at {SOURCE_PNG}\n"
            "Make sure funnel-crop-OLED.png is in the repository root."
        )

    print(f"Source: {SOURCE_PNG.resolve()}")
    print(f"Output: {OUTPUT_RES.resolve()}")

    src = ensure_rgba(Image.open(SOURCE_PNG))
    print(f"Loaded {src.size[0]}x{src.size[1]} RGBA image")

    generate_launcher_icons(src)
    generate_round_icons(src)
    generate_foreground_icons(src)
    generate_tile_icons(src)
    generate_adaptive_xml()

    print("\nDone!  Copy the contents of output/res/ into app/src/main/res/")


if __name__ == "__main__":
    main()
