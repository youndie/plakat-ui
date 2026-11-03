#!/usr/bin/env python3
"""B-02: fetch the kit's two families from google/fonts at one commit and cut Golos Text's statics.

    python3 -m venv .venv && .venv/bin/pip install fonttools
    .venv/bin/python scripts/fonts.py

Rubik Mono One ships as fetched. Golos Text is published only as a variable font (`wght` 400-900),
and a `wght` setting needs Android API 26: under it the file's default instance (400) draws, so
Title and Label (600) would fall back to Regular at minSdk 24 (research §1.4, D7). The two weights
the kit uses are therefore cut here with fontTools' instancer. The OFL permits modified versions,
and neither family declares a Reserved Font Name, so the instances keep the family name; each OFL
text ships beside its font.

Every fetched file is checked against `scripts/fonts.lock`, so a changed upstream file stops the
script instead of quietly changing the kit. The instances keep the upstream `head.modified`, so a
second run is byte-identical.
"""
import hashlib
import io
import pathlib
import sys
import urllib.request

from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

COMMIT = "2fe1e8c6b137fb13041a7cfca6fdcd8a910ea4dc"
BASE = f"https://raw.githubusercontent.com/google/fonts/{COMMIT}/ofl"
RUBIK = "rubikmonoone/RubikMonoOne-Regular.ttf"
RUBIK_OFL = "rubikmonoone/OFL.txt"
GOLOS = "golostext/GolosText%5Bwght%5D.ttf"
GOLOS_OFL = "golostext/OFL.txt"
ROOT = pathlib.Path(__file__).resolve().parent.parent
RES = ROOT / "plakat-core/src/commonMain/composeResources"
FONTS = RES / "font"
LICENCES = RES / "files/licences"
LOCK = ROOT / "scripts/fonts.lock"
# The kit's two Golos weights (plakat-tokens.json: body 400, title and label 600).
STATICS = [(400, "golos_text_regular"), (600, "golos_text_semibold")]


def fetch(path: str) -> bytes:
    with urllib.request.urlopen(f"{BASE}/{path}") as r:
        return r.read()


def main() -> None:
    lock = {}
    if LOCK.exists():
        for line in LOCK.read_text().splitlines():
            digest, path = line.split("  ", 1)
            lock[path] = digest
    data = {}
    for path in (RUBIK, RUBIK_OFL, GOLOS, GOLOS_OFL):
        body = fetch(path)
        digest = hashlib.sha256(body).hexdigest()
        if path in lock and lock[path] != digest:
            sys.exit(f"{path}: upstream changed ({digest} != {lock[path]}); review it, then delete scripts/fonts.lock")
        lock[path] = digest
        data[path] = body
    LOCK.write_text("".join(f"{d}  {p}\n" for p, d in sorted(lock.items())))

    FONTS.mkdir(parents=True, exist_ok=True)
    LICENCES.mkdir(parents=True, exist_ok=True)
    (FONTS / "rubik_mono_one_regular.ttf").write_bytes(data[RUBIK])
    (LICENCES / "rubik-mono-one-OFL.txt").write_bytes(data[RUBIK_OFL])
    (LICENCES / "golos-text-OFL.txt").write_bytes(data[GOLOS_OFL])

    for weight, name in STATICS:
        variable = TTFont(io.BytesIO(data[GOLOS]))
        modified = variable["head"].modified
        static = instancer.instantiateVariableFont(variable, {"wght": weight}, updateFontNames=True)
        static["OS/2"].usWeightClass = weight
        static["head"].modified = modified
        static.recalcTimestamp = False
        out = io.BytesIO()
        static.save(out, reorderTables=True)
        (FONTS / f"{name}.ttf").write_bytes(out.getvalue())
        print(f"{name}.ttf  wght {weight}  {len(out.getvalue())} bytes")


if __name__ == "__main__":
    main()
