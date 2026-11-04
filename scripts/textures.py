#!/usr/bin/env python3
"""B-07: the kit's eight texture tiles, five as the designer delivered them and three made here.

    python3 -m venv .venv && .venv/bin/pip install pillow
    make textures                 # or: .venv/bin/python scripts/textures.py [--check]

DesignSync truncated `paper-light-2x.png`, `paper-grain-2x.png` and `concrete-light-2x.png` at its
256 KiB cap (research §1.1), and the owner chose to make them here (D16). The delivered dark paper is
the designer's grain itself: the token colour #1F1D1A with a mottle of ±1 and short light fibres and
specks up to +19, seamless across its edges. Each missing tile is derived from that grain, pixel for
pixel, so it has the designer's texture, the right tone, and no seam (the source has none). The
Tokens page's `feTurbulence` recipe was the first plan (D16 as first written); its noise has a spread
of tens of levels where the delivered paper has one, so the delivered tiles were not made from it:

- paper-light, concrete-light: the token colour minus the dark tile's deviation from its own token
  (the fibres print dark on light paper, as they print light on dark), opaque;
- paper-grain: the overlay for any flat ink, `multiply` at the token's opacity (0.45): the Tokens
  page's grain colour (.42, .34, .20 of white), its alpha set so that over the light paper at that
  opacity it darkens each pixel by the dark tile's deviation.

The five delivered tiles are copied unchanged. Output: plakat-core's compose resources, 512 px = 256
dp. --check rebuilds in memory and fails when a committed tile's pixels differ.
"""
import io
import json
import pathlib
import sys

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "reference/design-system/textures"
OUT = ROOT / "plakat-core/src/commonMain/composeResources/drawable"
TOKENS = json.loads((ROOT / "reference/design-system/plakat-tokens.json").read_text())


def token(name, theme):
    h = TOKENS["plakat/color"][name][theme]
    return tuple(int(h[i : i + 2], 16) for i in (1, 3, 5))


DARK = token("paper", "dark")
GRAIN_INK = (round(0.42 * 255), round(0.34 * 255), round(0.20 * 255))
OPACITY = TOKENS["plakat/print"]["paper-texture"]["opacity"]


def clamp(v):
    return max(0, min(255, round(v)))


def deviation():
    dark = Image.open(SRC / "paper-dark-2x.png").convert("RGB")
    px = dark.tobytes()
    return dark.size, [tuple(px[i + c] - DARK[c] for c in range(3)) for i in range(0, len(px), 3)]


def opaque(base, size, dev):
    im = Image.new("RGB", size)
    im.putdata([tuple(clamp(base[c] - d[c]) for c in range(3)) for d in dev])
    return im


def grain(size, dev):
    # Multiply at `OPACITY` with alpha a darkens channel v by v * (1 - ink / 255) * a * OPACITY;
    # over the light paper that is to equal the deviation's luminance.
    light = token("paper", "light")
    lum = lambda t: 0.299 * t[0] + 0.587 * t[1] + 0.114 * t[2]
    per_alpha = lum(light) * (1 - lum(GRAIN_INK) / 255) * OPACITY / 255
    im = Image.new("RGBA", size)
    im.putdata([GRAIN_INK + (clamp(max(0.0, lum(d)) / per_alpha),) for d in dev])
    return im


def build():
    size, dev = deviation()
    tiles = {
        "paper_light": opaque(token("paper", "light"), size, dev),
        "concrete_light": opaque(token("concrete", "light"), size, dev),
        "paper_grain": grain(size, dev),
    }
    for f in ["paper-dark-2x", "nonprint-mask-2x", "halftone-screen-25-2x", "halftone-screen-50-2x", "halftone-screen-75-2x"]:
        tiles[f.replace("-2x", "").replace("-", "_")] = Image.open(SRC / f"{f}.png")
    return tiles


def main():
    tiles = build()
    if "--check" in sys.argv:
        stale = [n for n, im in tiles.items() if not (OUT / f"{n}.png").exists() or Image.open(OUT / f"{n}.png").convert(im.mode).tobytes() != im.tobytes()]
        if stale:
            sys.exit(f"texture tiles differ from what scripts/textures.py makes: {', '.join(stale)}")
        print(f"{len(tiles)} texture tiles are up to date")
        return
    OUT.mkdir(parents=True, exist_ok=True)
    for name, im in tiles.items():
        buf = io.BytesIO()
        im.save(buf, "PNG", optimize=True)
        (OUT / f"{name}.png").write_bytes(buf.getvalue())
        print(f"{name}.png  {im.size[0]}x{im.size[1]} {im.mode}  {len(buf.getvalue())} bytes")


if __name__ == "__main__":
    main()
