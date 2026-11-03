# Backlog: plakat-ui

> Role of this document: the product backlog. **One file per item in
> [`docs/backlog/`](docs/backlog/)**, named `B-NN-<slug>.md`. What lives here is the index (generated)
> and everything that is not an item: the goal, the stages, and the order of work.
>
> New item: copy [`docs/templates/backlog-item.md`](docs/templates/backlog-item.md), take the next
> free `B-NN`, and run `make fix` after editing.

## Goal

Ship the plakat-ui design — direction 3a «Сборка»: 21 MVP components with their motion rows, the
poster, the ROSTA window and the Isotype counter, 16 icons, the agit graphics and both papers — as a
Compose Multiplatform library that **provably** matches the design. Pixels are checked against
references the repository renders from the spec pages in Chrome; the generator and raskat are
checked by numbers against goldens their own JS emits in Node (research D3–D5).

The design is frozen in [`reference/design-system/`](reference/design-system/) at the snapshot
research §1.1 names. It is evidence, not a document this repository maintains.

## Order of work, and why it is this order

1. **What can invalidate the architecture comes first** (stage 0): the skeleton, the fonts with
   their static Golos instances, the generator and raskat oracles, the pixel references with
   parity's floor, and the frame cost on a Pixel 6a. Each changes how every component is built or
   verified.
2. **The materials** (stage 1): the textures, tokens generated from `plakat-tokens.json`, the theme
   with reduce motion, the print primitives (cut corner, registration, hatch, wipes, curves), the
   icons and the motion events. Every component reads these.
3. **Components** (stage 2), the brief's MVP set and the kit's own: poster, window, counter, stretch
   text, agit graphics, the screen wipe; projection after the MVP.
4. **The demo screens** (stage 3): onboarding, feed and settings of «Лист», both papers, rebuilt from
   the components; then accessibility with the textures on.
5. **The library as a product** (stage 4): the owner's questions, publishing, the undesigned
   components, re-importing the design.

The owner answered on 2026-11-03: the licence is MIT ([B-40](docs/backlog/B-40-licence.md)); the
registration offset is 1 dp ([B-41](docs/backlog/B-41-registration-unit.md)); the three truncated
textures are rendered here by a script, seamless ([B-07](docs/backlog/B-07-textures.md)); RadioButton,
hover with a wide poster, and the v1 components are designed in this repository
([B-38](docs/backlog/B-38-radio-button.md), [B-39](docs/backlog/B-39-desktop-and-hover.md),
[B-42](docs/backlog/B-42-v1-components-designed-here.md)); the repository goes public and publishes
last ([B-37](docs/backlog/B-37-public-repository-and-publishing.md)). The loop works the backlog
locally and merges its own branches (`CLAUDE.md`, «Local workflow»).

## Stages

| Stage | What closes it |
|---|---|
| `stage-0-risks` | Goldens and references regenerate deterministically; the generator and raskat ports match them on every target; the floor and the frame cost have measured numbers |
| `stage-1-materials` | Tokens generated and checked; the theme, print primitives and icons match their cells |
| `stage-2-components` | Each component matches its page's states in both themes and its motion row at the row's timestamps |
| `stage-3-screens` | The three demo screens within the floor in both themes; contrast holds with the textures on |
| `stage-4-product` | The owner's questions answered; the design re-imported when it changes |

<!-- BEGIN INDEX -->

## Open (41)

| Task | | Priority | Size | Blocked by |
|---|---|---|---|---|
| [B-10](docs/backlog/B-10-print-primitives.md) `[ ]` | Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask | P0 | M | B-08, B-05 |
| [B-07](docs/backlog/B-07-textures.md) `[ ]` | Textures: render the three truncated tiles by a script, seamless, beside the five delivered | P1 | M | B-01 |
| [B-11](docs/backlog/B-11-icons.md) `[ ]` | Icons: 16 PlakatIcons generated from plakat-icons.json | P1 | S | B-01, B-05 |
| [B-12](docs/backlog/B-12-motion-events.md) `[ ]` | Motion events: the named events every animation reports, for sound later | P1 | S | B-09 |
| [B-13](docs/backlog/B-13-button.md) `[ ]` | Button: filled, outlined, text, danger; stamp, release, engage | P1 | M | B-09, B-10, B-12 |
| [B-14](docs/backlog/B-14-text-field.md) `[ ]` | TextField: underline to bar, hatch error, square caret | P1 | M | B-09, B-10, B-12 |
| [B-15](docs/backlog/B-15-checkbox.md) `[ ]` | Checkbox: the red wedge in and out | P1 | M | B-09, B-10, B-12 |
| [B-16](docs/backlog/B-16-switch.md) `[ ]` | Switch: rail, carriage and the red segment | P1 | M | B-09, B-10, B-12 |
| [B-17](docs/backlog/B-17-card.md) `[ ]` | Card: sheet of concrete paper, print appear, tear dismiss | P1 | M | B-09, B-10, B-12 |
| [B-18](docs/backlog/B-18-snackbar.md) `[ ]` | Snackbar: the telegram | P1 | M | B-09, B-10, B-12 |
| [B-19](docs/backlog/B-19-poster.md) `[ ]` | Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title | P1 | L | B-03, B-04, B-09, B-10, B-12, B-13 |
| [B-20](docs/backlog/B-20-icon-button-and-badge.md) `[ ]` | IconButton and Badge | P1 | M | B-09, B-10, B-11, B-12 |
| [B-21](docs/backlog/B-21-fab.md) `[ ]` | FAB: the one round shape, riding in on the axis | P1 | M | B-09, B-10, B-11, B-12 |
| [B-22](docs/backlog/B-22-chip.md) `[ ]` | Chip: the stamp | P1 | M | B-09, B-10, B-12 |
| [B-23](docs/backlog/B-23-list-item-and-divider.md) `[ ]` | List item and Divider | P1 | S | B-09, B-10, B-12 |
| [B-24](docs/backlog/B-24-progress.md) `[ ]` | Progress: the wedge filling a bar, the stepped sector | P1 | M | B-09, B-10, B-12 |
| [B-25](docs/backlog/B-25-top-app-bar.md) `[ ]` | TopAppBar: newspaper header, rapport strip, folding on scroll | P1 | M | B-03, B-09, B-10, B-11, B-12 |
| [B-26](docs/backlog/B-26-navigation-bar.md) `[ ]` | NavigationBar: black strip, red square behind the active icon | P1 | M | B-09, B-10, B-11, B-12 |
| [B-27](docs/backlog/B-27-tabs.md) `[ ]` | Tabs: rubrics with a red bar riding linearly | P1 | M | B-09, B-10, B-12 |
| [B-28](docs/backlog/B-28-dialog.md) `[ ]` | Dialog: the announcement | P1 | M | B-09, B-10, B-12 |
| [B-29](docs/backlog/B-29-window.md) `[ ]` | Окно: onboarding and empty-state panels in the manner of ROSTA windows | P1 | M | B-03, B-09, B-10, B-12, B-13 |
| [B-30](docs/backlog/B-30-counter.md) `[ ]` | Счётчик: the Isotype stat tile with a mechanical counter | P1 | M | B-09, B-10, B-12, B-31 |
| [B-31](docs/backlog/B-31-stretch-text.md) `[ ]` | Растяжка: one Display line filling its box from cap top to baseline | P1 | S | B-02, B-05 |
| [B-33](docs/backlog/B-33-agit-presets-and-photomontage.md) `[ ]` | Agit graphics: the six presets, generated compositions, rapports, halftone photomontage | P1 | M | B-03, B-05 |
| [B-34](docs/backlog/B-34-screen-wipe.md) `[ ]` | Screen transition: the red diagonal wipe, push and pop | P1 | S | B-09, B-10, B-12 |
| [B-35](docs/backlog/B-35-demo-screens-and-sample.md) `[ ]` | Demo screens and the sample kit: onboarding, feed, settings in both themes | P1 | M | B-13, B-14, B-16, B-17, B-19, B-25, B-26, B-27, B-29, B-30 |
| [B-36](docs/backlog/B-36-accessibility.md) `[ ]` | Accessibility: contrast with textures on, grayscale check, touch targets, semantics | P1 | S | B-07, B-35 |
| [B-38](docs/backlog/B-38-radio-button.md) `[ ]` | RadioButton: a ring with a red disc that locks in one frame | P1 | S | B-09, B-10, B-12, B-15 |
| [B-39](docs/backlog/B-39-desktop-and-hover.md) `[ ]` | Hover on pointer devices and a wide poster layout | P1 | M | B-13, B-19, B-20, B-22, B-23 |
| [B-51](docs/backlog/B-51-frame-cost-on-desktop.md) `[ ]` | Frame cost on the desktop JVM: raskat roll and the ten-card cascade, as a proxy until the device | P1 | S | B-04, B-10 |
| [B-32](docs/backlog/B-32-projection.md) `[ ]` | Проекция: text meshed onto a horn, cone, column, sphere, box or stairs | P2 | M | B-02, B-31 |
| [B-43](docs/backlog/B-43-re-import-the-design.md) `[ ]` | Re-import the design project on a branch of its own | P2 | S | B-05, B-08 |
| [B-44](docs/backlog/B-44-slider.md) `[ ]` | Slider: rail, ticks and a carriage that stops on them | P2 | M | B-09, B-10, B-12, B-16 |
| [B-45](docs/backlog/B-45-menu.md) `[ ]` | Menu / Dropdown: a list on paper in a bar frame, opened by a wipe | P2 | M | B-09, B-10, B-12, B-23 |
| [B-46](docs/backlog/B-46-bottom-sheet.md) `[ ]` | BottomSheet: a sheet riding up on rails, the handle is a bar | P2 | M | B-09, B-10, B-12, B-28 |
| [B-47](docs/backlog/B-47-navigation-drawer.md) `[ ]` | NavigationDrawer: black panel with cream capital items | P2 | M | B-09, B-10, B-12, B-26 |
| [B-48](docs/backlog/B-48-tooltip.md) `[ ]` | Tooltip: a caption with a straight leader line, as on a drawing | P2 | M | B-09, B-10, B-12 |
| [B-49](docs/backlog/B-49-segmented.md) `[ ]` | Segmented button: one frame, the black fill rides linearly | P2 | M | B-09, B-10, B-12, B-22 |
| [B-50](docs/backlog/B-50-pull-to-refresh.md) `[ ]` | Pull-to-refresh: a red wedge grows with the finger, then a stepped sector | P2 | M | B-09, B-10, B-12, B-24 |
| [B-06](docs/backlog/B-06-frame-cost-measurement.md) `[ ]` | Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration | P3 | M | B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50, B-51 |
| [B-37](docs/backlog/B-37-public-repository-and-publishing.md) `[ ]` | Public repository and publishing to Reposilite on every main — last | P3 | S | B-06, B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50 |

## Closed (10)

**Goldens and references regenerate deterministically; the generator and raskat ports match them on every target; the floor and the frame cost have measured numbers**

- [B-01](docs/backlog/B-01-repository-skeleton.md) `[x]` - Repository skeleton: plakat-core, sample, sborka conventions, four targets, CI on macOS
- [B-02](docs/backlog/B-02-fonts.md) `[x]` - Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test
- [B-03](docs/backlog/B-03-generator-port-and-numeric-parity.md) `[x]` - Generator: commonMain port of generate/rapport held to Node goldens on every target
- [B-04](docs/backlog/B-04-raskat-port.md) `[x]` - Raskat: port the JS field to the CPU, goldens from Node, the roll animation
- [B-05](docs/backlog/B-05-pixel-references-and-floor.md) `[x]` - Pixel references rendered from the spec pages in headless Chrome; the parity floor

**Tokens generated and checked; the theme, print primitives and icons match their cells**

- [B-08](docs/backlog/B-08-token-generator.md) `[x]` - Tokens: generate PlakatTokens from plakat-tokens.json, --check in the gate
- [B-09](docs/backlog/B-09-theme-and-reduce-motion.md) `[x]` - Theme: PlakatTheme for paper and black paper, reduce motion as a property, print switches

**The owner's questions answered; the design re-imported when it changes**

- [B-40](docs/backlog/B-40-licence.md) `[x]` - Licence: MIT
- [B-41](docs/backlog/B-41-registration-unit.md) `[x]` - Registration offset: 1 dp
- [B-42](docs/backlog/B-42-v1-components-designed-here.md) `[x]` - v1 components: designed here, one item each

<!-- END INDEX -->
