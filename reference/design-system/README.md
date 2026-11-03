# reference/design-system — the frozen plakat-ui design

A snapshot of the Claude Design project `d99820b8-b707-4cf2-83fc-4ba96e771c30` ("First deliverable:
three treatments", type `PROJECT_TYPE_PROJECT`, owner Pavel), taken on **2026-11-03** with the
DesignSync tool. `plakat-tokens.json` names it `"direction": "3a", "version": "0.2.0"`.

Everything here is evidence for the code, not a document this repository maintains. **Nothing in it
is edited** — not to make a check pass, not to fix a typo. A newer version arrives by re-import on a
branch of its own ([B-43](../../docs/backlog/B-43-re-import-the-design.md)). What was verified in
these files, and what follows from it, is in
[docs/research/research-architecture.md](../../docs/research/research-architecture.md) §1.

## What is here

| Path | What it is |
|---|---|
| `plakat-ui Направления.dc.html` | Stage 1: three treatments (1a typography, 1b photomontage, 1c workshop), four mixed versions (2a–2d) and the chosen **3a «Сборка»** |
| `plakat-ui Токены.dc.html`, `plakat-tokens.json` | Stage 2: the seven 3a rules, both themes' colours, type scale, lines, grid, print layers, motion. The JSON is the machine-readable source |
| `plakat-ui Ядро.dc.html` | Button, TextField, Checkbox, Switch, Card, Snackbar and Плакат in every state, both themes, with a motion lab and its spec table |
| `plakat-ui Компоненты.dc.html` | The rest of the MVP: IconButton, FAB, Chip, Badge, Divider, Progress, TopAppBar, NavigationBar, Tabs, List item, Dialog, Окно, Счётчик, and the screen wipe |
| `plakat-ui Иконки.dc.html`, `plakat-icons.json`, `plakat-icons.js` | The redraw rule and 16 icons, one path each on a 24 dp grid |
| `plakat-ui Агит-графика.dc.html`, `agit/` | Six hand-drawn presets (light and dark SVG, `presets.json` with the title zones), four rapports, the photomontage rule, the authoring guide `agit/README.md` |
| `plakat-gen.js`, `plakat-gen.kt` | The generator: `generate(seed)`, `rapport(seed)`, the CPU halftone; the designer's Kotlin port |
| `plakat-ui Раскат.dc.html`, `plakat-raskat.js`, `plakat-raskat.kt` | «Раскат», the one two-ink transition allowed per screen |
| `plakat-ui Растяжка.dc.html`, `plakat-warp.js`, `plakat-warp.kt` | A Display line stretched to a box, and «Проекция»: text meshed onto a horn, cone, column, sphere, box or stairs |
| `plakat-ui Текстуры.dc.html`, `textures/` | Paper, concrete, non-print mask and halftone stand-in tiles |
| `plakat-ui Демо.dc.html` | Three screens of the demo magazine «Лист» (onboarding, feed, settings) in both themes |
| `plakat-ui Принципы.dc.html` | How to draw a new component (Slider worked through), eight do/don't pairs, the checklist |
| `support.js`, `image-slot.js` | The dc runtime and the drop-an-image placeholder. They load React 18.3.1 and Babel from unpkg and the fonts from Google Fonts |
| `uploads/plakat-ui - бриф для дизайнера.md` | The owner's brief (Russian). Copied from the owner's local file of the same name, not fetched from the project's `uploads/` |

## What is not here, and why

| Missing | Why | Where it is settled |
|---|---|---|
| `textures/paper-light-2x.png`, `paper-grain-2x.png`, `concrete-light-2x.png` | DesignSync's `get_file` caps a file at 256 KiB; these three came back truncated (no `IEND`) and were not kept | [B-07](../../docs/backlog/B-07-textures.md) |
| `icons/plakat-*.svg` (16 files) | Each SVG carries an ~8 KB C2PA manifest; the paths are the same strings as `plakat-icons.json`, which is here | [B-11](../../docs/backlog/B-11-icons.md) re-checks on re-import |
| `screenshots/` (≈70 PNG), `.thumbnail` | The designer's review captures, not part of the design | — |
| `.image-slots.state.json` | Photos dropped into image slots (the photomontage examples), truncated at 256 KiB; their licence is unknown | [B-33](../../docs/backlog/B-33-agit-presets-and-photomontage.md) |

The vendored SVGs keep their `<metadata><c2pa:manifest>` blocks as delivered.
