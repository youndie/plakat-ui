---
id: B-52
title: "Empty state: a call rather than an apology, with the screen's one raskat wedge"
status: done
priority: P1
size: S
stage: stage-2-components
epic: feature-components
blocked_by: [B-04, B-13]
---

# B-52 — Empty state: a call rather than an apology, with the screen's one raskat wedge

Found in [B-29](B-29-window.md): the MVP page draws «Окно / Пустое состояние» in the Окно section, and no item owns it. It is not a panel of the window.

- **What to build.** A 120 dp band on concrete with a raskat wedge rising at −17° (red into concrete, the page's `drawRaskat` options), an ink bar and circle over it; a Display title, a body, and a 200 dp slogan button. The page draws the band on a canvas at 2×.
- AC: the state the page draws, in both themes, within the parity floor ([B-05](B-05-pixel-references-and-floor.md)); touch target ≥ 48 dp; a mutant in the geometry is caught.
- Anchors: `reference/design-system/plakat-ui Компоненты.dc.html`

## Findings (2026-11-05)

- **`PlakatEmptyState(title, body, action, onAction, modifier)`**, in the sample's components section. The band is the page's canvas: concrete, a wedge from the left foot to `W × tan 17°` up at the right, the raskat by the page's own options (`angle: −107`, `t0: .5`, `hard: 10`, `seed: 47`, `streak: .5`), the bar of 8 and the circle of 24.
- **Parity**: 3.20 % and 2.96 %. The band's difference is 11–12 % of its ink, all in the grain along the wedge's transition, with no shift better: the page's canvas is 2× and the browser scales it, and the fixture scales an image of the same field. Title 5 %, button 1 %.
- **Designed here** (D13), for the owner's review: at a width other than the page's 358, the wedge keeps its 17° and so its height grows with the width, and past about 392 it would rise out of the band's 120 and be cut; the circle keeps 58 from the right edge. A raskat prints at 2× at the least, as the page's canvas does, and at the screen's pixels above that. No print or roll: the page draws it still, and its section has no motion row for it.
- **Touch target**: the slogan is 48 high, 200 wide.
- **Mutants**: through the tests — padding 16, a hair for the bar, the circle's place, the circle's size, a slope of 25°, no wedge, no heading, a slogan of 240. Through the goldens — another seed, another `t0`, a title of 18, a body line of 1.6. All twelve killed; the circle's size survived the first round and got its own check.
