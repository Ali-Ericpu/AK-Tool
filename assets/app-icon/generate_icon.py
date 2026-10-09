#!/usr/bin/env python3
"""Draw the AK Tool application icon.

The artwork is original and generated entirely in code: a chibi mascot (mint hair,
cat ears) on a pastel gradient, with a soft inner glow and two sparkles. Every shape
is drawn at SS times the target resolution and downsampled with LANCZOS, which gives
the smooth edges a vector tool would.

Run:  python generate_icon.py
Outputs (this script): icon-master-1024.png and two previews.
The platform assets (Android mipmaps, Windows .ico) are emitted by build_assets.py.
"""

import math
import os

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

SIZE = 1024          # master size in pixels
SS = 4               # supersample factor
W = SIZE * SS        # working canvas
OUT_DIR = os.path.dirname(os.path.abspath(__file__))

OUTLINE = (58, 46, 79, 255)
BG_TOP = (255, 214, 232)
BG_BOTTOM = (196, 181, 255)
CREAM = (255, 247, 242, 255)
MINT = (127, 216, 210, 255)
MINT_DEEP = (86, 178, 176, 255)
BLUSH = (255, 138, 170, 170)
EYE = (43, 36, 64, 255)
SPARK = (255, 252, 235, 235)

# Which artwork main() renders: "symbol", "toolbox" or "mascot".
STYLE = "symbol"

# Which symbol is the master when STYLE == "symbol": "monogram" or "wrench".
SYMBOL = "wrench"

# toolbox palette: two navy tones, one muted gold accent, near-white ground
TOOL_BG_TOP = (252, 253, 255)
TOOL_BG_BOTTOM = (232, 237, 246)
TOOL_BODY = (38, 44, 60, 255)
TOOL_LID = (52, 60, 80, 255)
TOOL_ACCENT = (215, 168, 78, 255)
TOOL_SHADOW = (60, 70, 95, 46)

# Which gold detail sits on the front: "wrench" (a small tool) or "latch" (a clasp).
TOOL_VARIANT = "wrench"


def px(fraction: float) -> float:
    """Fraction of the canvas -> working pixels."""
    return fraction * W


def lw(pixels_at_1024: float) -> int:
    """Line width given in master pixels -> working pixels."""
    return max(1, round(pixels_at_1024 * SS))


def ellipse(draw, cx, cy, rx, ry, **kwargs):
    draw.ellipse([px(cx - rx), px(cy - ry), px(cx + rx), px(cy + ry)], **kwargs)


def polygon(draw, points, **kwargs):
    draw.polygon([(px(x), px(y)) for x, y in points], **kwargs)


def star(draw, cx, cy, r, color):
    """Four-point sparkle."""
    inner = r * 0.26
    pts = []
    for i in range(8):
        angle = math.pi / 4 * i - math.pi / 2
        radius = r if i % 2 == 0 else inner
        pts.append((px(cx + radius * math.cos(angle)), px(cy + radius * math.sin(angle))))
    draw.polygon(pts, fill=color)


def background(top=BG_TOP, bottom=BG_BOTTOM, glow_color=(255, 246, 252),
               glow_strength=0.5) -> Image.Image:
    img = Image.new("RGB", (W, W), top)
    draw = ImageDraw.Draw(img)
    for y in range(W):
        t = y / (W - 1)
        draw.line(
            [(0, y), (W, y)],
            fill=tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(3)),
        )

    # soft radial highlight behind the mascot's head
    yy, xx = np.mgrid[0:W, 0:W].astype(np.float32)
    radius = np.sqrt((xx - W * 0.50) ** 2 + (yy - W * 0.46) ** 2) / (W * 0.62)
    glow = np.clip(1.0 - radius, 0.0, 1.0) ** 1.5
    arr = np.asarray(img).astype(np.float32)
    arr += (np.array(glow_color, np.float32) - arr) * (glow[..., None] * glow_strength)
    return Image.fromarray(arr.clip(0, 255).astype(np.uint8)).convert("RGBA")


def draw_mascot(img: Image.Image) -> None:
    o = lw(11)
    draw = ImageDraw.Draw(img, "RGBA")

    # --- ears, then their pink inner triangles on top ---
    polygon(draw, [(0.285, 0.205), (0.215, 0.425), (0.415, 0.365)],
            fill=CREAM, outline=OUTLINE, width=o)
    polygon(draw, [(0.715, 0.205), (0.785, 0.425), (0.585, 0.365)],
            fill=CREAM, outline=OUTLINE, width=o)
    polygon(draw, [(0.307, 0.252), (0.266, 0.392), (0.386, 0.364)], fill=(255, 196, 214, 255))
    polygon(draw, [(0.693, 0.252), (0.734, 0.392), (0.614, 0.364)], fill=(255, 196, 214, 255))

    # --- head ---
    ellipse(draw, 0.500, 0.565, 0.300, 0.260, fill=CREAM, outline=OUTLINE, width=o)

    # --- hair fringe, clipped to the head ---
    head_mask = Image.new("L", (W, W), 0)
    ImageDraw.Draw(head_mask).ellipse(
        [px(0.200), px(0.305), px(0.800), px(0.825)], fill=255
    )
    fringe = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    fd = ImageDraw.Draw(fringe)  # opaque: blending here would let the background tint the hair
    ellipse(fd, 0.500, 0.300, 0.300, 0.150, fill=MINT_DEEP)
    ellipse(fd, 0.500, 0.352, 0.284, 0.130, fill=MINT)
    ellipse(fd, 0.322, 0.432, 0.088, 0.074, fill=MINT)
    ellipse(fd, 0.500, 0.446, 0.094, 0.078, fill=MINT)
    ellipse(fd, 0.678, 0.432, 0.088, 0.074, fill=MINT)
    ellipse(fd, 0.500, 0.288, 0.286, 0.140, fill=MINT)
    fringe.putalpha(Image.composite(fringe.getchannel("A"), Image.new("L", (W, W), 0), head_mask))
    img.alpha_composite(fringe)

    # --- eyes ---
    for cx in (0.398, 0.602):
        ellipse(draw, cx, 0.585, 0.064, 0.088, fill=EYE)
        ellipse(draw, cx - 0.017, 0.556, 0.026, 0.032, fill=(255, 255, 255, 245))
        ellipse(draw, cx + 0.019, 0.612, 0.014, 0.016, fill=(255, 255, 255, 210))
        ellipse(draw, cx - 0.006, 0.628, 0.020, 0.020, fill=(150, 232, 226, 150))

    # --- blush (blurred layer so it reads as soft colour, not a sticker) ---
    blush = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    bd = ImageDraw.Draw(blush, "RGBA")
    for cx in (0.312, 0.688):
        ellipse(bd, cx, 0.648, 0.052, 0.030, fill=BLUSH)
    blush = blush.filter(ImageFilter.GaussianBlur(px(0.012)))
    img.alpha_composite(blush)

    # --- mouth: a small cat-like double arc ---
    draw = ImageDraw.Draw(img, "RGBA")
    for cx in (0.470, 0.530):
        draw.arc(
            [px(cx - 0.032), px(0.672), px(cx + 0.032), px(0.712)],
            start=200, end=340, fill=OUTLINE, width=lw(7),
        )

    # --- sparkles (radius is a fraction of the canvas, like every other helper here) ---
    star(draw, 0.800, 0.212, 0.062, SPARK)
    star(draw, 0.882, 0.318, 0.034, (255, 252, 235, 200))
    star(draw, 0.148, 0.268, 0.028, (255, 252, 235, 180))
    ellipse(draw, 0.862, 0.152, 0.011, 0.011, fill=(255, 255, 255, 190))
    ellipse(draw, 0.196, 0.196, 0.009, 0.009, fill=(255, 255, 255, 170))


def draw_toolbox(img: Image.Image, variant: str = TOOL_VARIANT) -> None:
    """A restrained toolbox: two navy tones, no outline, one gold accent, lots of margin."""
    draw = ImageDraw.Draw(img, "RGBA")

    # soft contact shadow
    shadow = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    ellipse(ImageDraw.Draw(shadow, "RGBA"), 0.500, 0.800, 0.245, 0.030, fill=TOOL_SHADOW)
    img.alpha_composite(shadow.filter(ImageFilter.GaussianBlur(px(0.018))))

    # handle: a thin arch whose ends disappear under the lid
    draw.arc([px(0.408), px(0.268), px(0.592), px(0.452)], start=180, end=360,
             fill=TOOL_LID, width=lw(26))

    # body and lid: two tones only, no stroke
    draw.rounded_rectangle([px(0.222), px(0.452), px(0.778), px(0.782)],
                           radius=px(0.030), fill=TOOL_BODY)
    draw.rounded_rectangle([px(0.204), px(0.392), px(0.796), px(0.532)],
                           radius=px(0.026), fill=TOOL_LID)

    # the only accent colour in the icon
    if variant == "wrench":
        # a small upright wrench: open ring head over a shaft
        draw.arc([px(0.458), px(0.542), px(0.542), px(0.626)], start=300, end=240,
                 fill=TOOL_ACCENT, width=lw(18))
        draw.rounded_rectangle([px(0.487), px(0.606), px(0.513), px(0.726)],
                               radius=px(0.010), fill=TOOL_ACCENT)
    else:
        # a single centred clasp straddling the lid seam
        draw.rounded_rectangle([px(0.466), px(0.498), px(0.534), px(0.556)],
                               radius=px(0.012), fill=TOOL_ACCENT)

    # hairline highlight on the lid's top edge for a hint of material
    draw.rounded_rectangle([px(0.226), px(0.404), px(0.774), px(0.416)],
                           radius=px(0.006), fill=(255, 255, 255, 30))


def wf(fraction: float) -> int:
    """Stroke width given as a fraction of the canvas."""
    return max(1, round(fraction * W))


def stroke(draw, p1, p2, thickness, color=TOOL_BODY) -> None:
    """A thick line with rounded caps."""
    draw.line([(px(p1[0]), px(p1[1])), (px(p2[0]), px(p2[1]))], fill=color, width=wf(thickness))
    half = wf(thickness) / 2
    for p in (p1, p2):
        draw.ellipse([px(p[0]) - half, px(p[1]) - half, px(p[0]) + half, px(p[1]) + half], fill=color)


def draw_symbol_monogram(img: Image.Image) -> None:
    """A geometric AK monogram: two legs with a crossbar, then a stem with two diagonals."""
    draw = ImageDraw.Draw(img, "RGBA")
    t = 0.078
    stroke(draw, (0.251, 0.775), (0.371, 0.248), t)
    stroke(draw, (0.371, 0.248), (0.491, 0.775), t)
    stroke(draw, (0.295, 0.614), (0.447, 0.614), t * 0.90)
    stroke(draw, (0.612, 0.248), (0.612, 0.775), t)
    stroke(draw, (0.630, 0.512), (0.752, 0.248), t * 0.90)
    stroke(draw, (0.630, 0.512), (0.752, 0.775), t * 0.90)


def draw_symbol_wrench(img: Image.Image) -> None:
    """An open-end / box-end wrench, drawn upright then turned 45 degrees.

    The identity of a wrench comes from its two ends: an open slot jaw on one side and a
    hex socket on the other. A closed ring with a straight shaft reads as a key instead.
    """
    layer = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)  # direct writes, so the punches below really cut through

    # tapered shaft
    d.polygon([(px(0.4480), px(0.262)), (px(0.5520), px(0.262)),
               (px(0.5405), px(0.742)), (px(0.4595), px(0.742))], fill=TOOL_BODY)
    # open-end head: a disc that the slot is cut out of
    ellipse(d, 0.500, 0.268, 0.138, 0.138, fill=TOOL_BODY)
    # box end: a ring that the hex socket is cut out of
    ellipse(d, 0.500, 0.742, 0.112, 0.112, fill=TOOL_BODY)

    # cut the open jaw (a slot running out past the head) ...
    d.polygon([(px(0.4575), px(0.062)), (px(0.5425), px(0.062)),
               (px(0.5425), px(0.300)), (px(0.4575), px(0.300))], fill=(0, 0, 0, 0))
    # ... and the hex socket
    d.polygon([(px(0.500 + 0.062 * math.cos(math.pi / 3 * i + math.pi / 6)),
                 px(0.742 + 0.062 * math.sin(math.pi / 3 * i + math.pi / 6)))
               for i in range(6)], fill=(0, 0, 0, 0))

    img.alpha_composite(layer.rotate(45, resample=Image.BICUBIC))


def render_symbol(kind: str) -> Image.Image:
    img = background(TOOL_BG_TOP, TOOL_BG_BOTTOM, (255, 255, 255), 0.18)
    if kind == "wrench":
        draw_symbol_wrench(img)
    else:
        draw_symbol_monogram(img)
    return img


def render_toolbox(variant: str) -> Image.Image:
    img = background(TOOL_BG_TOP, TOOL_BG_BOTTOM, (255, 255, 255), 0.18)
    draw_toolbox(img, variant)
    return img


def rounded_mask(size: int, radius_fraction: float) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size - 1, size - 1],
                                           radius=radius_fraction * size, fill=255)
    return mask


def circular_mask(size: int) -> Image.Image:
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse([0, 0, size - 1, size - 1], fill=255)
    return mask


def symbol_layer(kind: str) -> Image.Image:
    """The mark alone on transparency, at master size (used for the adaptive foreground)."""
    layer = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    if kind == "wrench":
        draw_symbol_wrench(layer)
    else:
        draw_symbol_monogram(layer)
    return layer.resize((SIZE, SIZE), Image.LANCZOS)


def emit_platform_assets(master: Image.Image) -> None:
    """Write the per-platform copies. Resources only: nothing in the build refers to them yet."""
    repo = os.path.dirname(os.path.dirname(OUT_DIR))
    res = os.path.join(repo, "androidApp", "src", "main", "res")
    mark_master = symbol_layer(SYMBOL)

    for folder, icon_px, layer_px in (("mdpi", 48, 108), ("hdpi", 72, 162), ("xhdpi", 96, 216),
                                      ("xxhdpi", 144, 324), ("xxxhdpi", 192, 432)):
        target = os.path.join(res, f"mipmap-{folder}")
        os.makedirs(target, exist_ok=True)

        square = master.resize((icon_px, icon_px), Image.LANCZOS)
        square.save(os.path.join(target, "ic_launcher.png"))
        round_icon = square.copy()
        round_icon.putalpha(circular_mask(icon_px))
        round_icon.save(os.path.join(target, "ic_launcher_round.png"))

        side = int(layer_px * 0.72)
        mark = mark_master.resize((side, side), Image.LANCZOS)
        layer = Image.new("RGBA", (layer_px, layer_px), (0, 0, 0, 0))
        offset = (layer_px - side) // 2
        layer.alpha_composite(mark, (offset, offset))
        layer.save(os.path.join(target, "ic_launcher_foreground.png"))

    anydpi = os.path.join(res, "mipmap-anydpi-v26")
    os.makedirs(anydpi, exist_ok=True)
    adaptive = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background" />\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />\n'
        '</adaptive-icon>\n'
    )
    for name in ("ic_launcher.xml", "ic_launcher_round.xml"):
        with open(os.path.join(anydpi, name), "w", encoding="utf-8") as fh:
            fh.write(adaptive)

    values = os.path.join(res, "values")
    os.makedirs(values, exist_ok=True)
    with open(os.path.join(values, "ic_launcher_background.xml"), "w", encoding="utf-8") as fh:
        fh.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
                 '    <color name="ic_launcher_background">#F5F7FB</color>\n</resources>\n')

    # Windows: one multi-size .ico with rounded corners, plus a PNG for the desktop window
    tile = master.copy()
    tile.putalpha(rounded_mask(SIZE, 0.22))
    tile.save(os.path.join(OUT_DIR, "icon.ico"),
              sizes=[(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
    desktop_res = os.path.join(repo, "desktopApp", "src", "main", "resources")
    os.makedirs(desktop_res, exist_ok=True)
    tile.resize((256, 256), Image.LANCZOS).save(os.path.join(desktop_res, "icon.png"))


def main() -> None:
    if STYLE == "symbol":
        master = render_symbol(SYMBOL).resize((SIZE, SIZE), Image.LANCZOS)
        master.save(os.path.join(OUT_DIR, "icon-master-1024.png"))
        master.resize((256, 256), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-256.png"))
        master.resize((72, 72), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-72.png"))
        other = "wrench" if SYMBOL == "monogram" else "monogram"
        render_symbol(other).resize((256, 256), Image.LANCZOS).save(
            os.path.join(OUT_DIR, f"preview-alt-{other}-256.png"))
        print(f"symbol '{SYMBOL}': icon-master-1024.png, preview-256.png, preview-72.png, "
              f"preview-alt-{other}-256.png")
        emit_platform_assets(master)
        return
    if STYLE == "toolbox":
        master = render_toolbox(TOOL_VARIANT).resize((SIZE, SIZE), Image.LANCZOS)
        master.save(os.path.join(OUT_DIR, "icon-master-1024.png"))
        master.resize((256, 256), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-256.png"))
        master.resize((72, 72), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-72.png"))
        other = "wrench" if TOOL_VARIANT == "latch" else "latch"
        render_toolbox(other).resize((256, 256), Image.LANCZOS).save(
            os.path.join(OUT_DIR, f"preview-alt-{other}-256.png"))
        print(f"wrote icon-master-1024.png, preview-256.png, preview-72.png, "
              f"preview-alt-{other}-256.png")
        return
    else:
        img = background()
        draw = ImageDraw.Draw(img, "RGBA")
        # faint dotted ring in the corner for a little texture
        for i in range(26):
            angle = math.pi * 2 * i / 26
            ellipse(draw, 0.5 + 0.455 * math.cos(angle), 0.5 + 0.455 * math.sin(angle),
                    0.006, 0.006, fill=(255, 255, 255, 46))
        draw_mascot(img)

    master = img.resize((SIZE, SIZE), Image.LANCZOS)
    master.save(os.path.join(OUT_DIR, "icon-master-1024.png"))
    master.resize((256, 256), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-256.png"))
    master.resize((72, 72), Image.LANCZOS).save(os.path.join(OUT_DIR, "preview-72.png"))
    print("wrote icon-master-1024.png, preview-256.png, preview-72.png")


if __name__ == "__main__":
    main()
