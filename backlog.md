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

## Open (4)

| Task | | Priority | Size | Blocked by |
|---|---|---|---|---|
| [B-50](docs/backlog/B-50-pull-to-refresh.md) `[~]` | Pull-to-refresh: a red wedge grows with the finger, then a stepped sector | P2 | M | B-09, B-10, B-12, B-24 |
| [B-06](docs/backlog/B-06-frame-cost-measurement.md) `[ ]` | Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration | P3 | M | B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50, B-51 |
| [B-37](docs/backlog/B-37-public-repository-and-publishing.md) `[ ]` | Public repository and publishing to Reposilite on every main — last | P3 | S | B-06, B-35, B-36, B-38, B-39, B-44, B-45, B-46, B-47, B-48, B-49, B-50 |
| [B-43](docs/backlog/B-43-re-import-the-design.md) `[ ]` | Re-import the design project on a branch of its own | P3 | S | B-05, B-08 |

## Closed (49)

**Goldens and references regenerate deterministically; the generator and raskat ports match them on every target; the floor and the frame cost have measured numbers**

- [B-01](docs/backlog/B-01-repository-skeleton.md) `[x]` - Repository skeleton: plakat-core, sample, sborka conventions, four targets, CI on macOS
- [B-02](docs/backlog/B-02-fonts.md) `[x]` - Fonts: Rubik Mono One and static Golos Text 400/600, licences, coverage test
- [B-03](docs/backlog/B-03-generator-port-and-numeric-parity.md) `[x]` - Generator: commonMain port of generate/rapport held to Node goldens on every target
- [B-04](docs/backlog/B-04-raskat-port.md) `[x]` - Raskat: port the JS field to the CPU, goldens from Node, the roll animation
- [B-05](docs/backlog/B-05-pixel-references-and-floor.md) `[x]` - Pixel references rendered from the spec pages in headless Chrome; the parity floor
- [B-51](docs/backlog/B-51-frame-cost-on-desktop.md) `[x]` - Frame cost on the desktop JVM: raskat roll and the ten-card cascade, as a proxy until the device

**Tokens generated and checked; the theme, print primitives and icons match their cells**

- [B-07](docs/backlog/B-07-textures.md) `[x]` - Textures: render the three truncated tiles by a script, seamless, beside the five delivered
- [B-08](docs/backlog/B-08-token-generator.md) `[x]` - Tokens: generate PlakatTokens from plakat-tokens.json, --check in the gate
- [B-09](docs/backlog/B-09-theme-and-reduce-motion.md) `[x]` - Theme: PlakatTheme for paper and black paper, reduce motion as a property, print switches
- [B-10](docs/backlog/B-10-print-primitives.md) `[x]` - Print primitives: cut corner, registration, hatch, wipes, hard-stop and stepped curves, non-print mask
- [B-11](docs/backlog/B-11-icons.md) `[x]` - Icons: 16 PlakatIcons generated from plakat-icons.json
- [B-12](docs/backlog/B-12-motion-events.md) `[x]` - Motion events: the named events every animation reports, for sound later

**Each component matches its page's states in both themes and its motion row at the row's timestamps**

- [B-13](docs/backlog/B-13-button.md) `[x]` - Button: filled, outlined, text, danger; stamp, release, engage
- [B-14](docs/backlog/B-14-text-field.md) `[x]` - TextField: underline to bar, hatch error, square caret
- [B-15](docs/backlog/B-15-checkbox.md) `[x]` - Checkbox: the red wedge in and out
- [B-16](docs/backlog/B-16-switch.md) `[x]` - Switch: rail, carriage and the red segment
- [B-17](docs/backlog/B-17-card.md) `[x]` - Card: sheet of concrete paper, print appear, tear dismiss
- [B-18](docs/backlog/B-18-snackbar.md) `[x]` - Snackbar: the telegram
- [B-19](docs/backlog/B-19-poster.md) `[x]` - Плакат: Круг, Баннер, Фон — generator, raskat roll, diagonal title
- [B-20](docs/backlog/B-20-icon-button-and-badge.md) `[x]` - IconButton and Badge
- [B-21](docs/backlog/B-21-fab.md) `[x]` - FAB: the one round shape, riding in on the axis
- [B-22](docs/backlog/B-22-chip.md) `[x]` - Chip: the stamp
- [B-23](docs/backlog/B-23-list-item-and-divider.md) `[x]` - List item and Divider
- [B-24](docs/backlog/B-24-progress.md) `[x]` - Progress: the wedge filling a bar, the stepped sector
- [B-25](docs/backlog/B-25-top-app-bar.md) `[x]` - TopAppBar: newspaper header, rapport strip, folding on scroll
- [B-26](docs/backlog/B-26-navigation-bar.md) `[x]` - NavigationBar: black strip, red square behind the active icon
- [B-27](docs/backlog/B-27-tabs.md) `[x]` - Tabs: rubrics with a red bar riding linearly
- [B-28](docs/backlog/B-28-dialog.md) `[x]` - Dialog: the announcement
- [B-29](docs/backlog/B-29-window.md) `[x]` - Окно: onboarding and empty-state panels in the manner of ROSTA windows
- [B-30](docs/backlog/B-30-counter.md) `[x]` - Счётчик: the Isotype stat tile with a mechanical counter
- [B-31](docs/backlog/B-31-stretch-text.md) `[x]` - Растяжка: one Display line filling its box from cap top to baseline
- [B-32](docs/backlog/B-32-projection.md) `[x]` - Проекция: text meshed onto a horn, cone, column, sphere, box or stairs
- [B-33](docs/backlog/B-33-agit-presets-and-photomontage.md) `[x]` - Agit graphics: the six presets, generated compositions, rapports, halftone photomontage
- [B-34](docs/backlog/B-34-screen-wipe.md) `[x]` - Screen transition: the red diagonal wipe, push and pop
- [B-38](docs/backlog/B-38-radio-button.md) `[x]` - RadioButton: a ring with a red disc that locks in one frame
- [B-39](docs/backlog/B-39-desktop-and-hover.md) `[x]` - Hover on pointer devices and a wide poster layout
- [B-44](docs/backlog/B-44-slider.md) `[x]` - Slider: rail, ticks and a carriage that stops on them
- [B-45](docs/backlog/B-45-menu.md) `[x]` - Menu / Dropdown: a list on paper in a bar frame, opened by a wipe
- [B-46](docs/backlog/B-46-bottom-sheet.md) `[x]` - BottomSheet: a sheet riding up on rails, the handle is a bar
- [B-47](docs/backlog/B-47-navigation-drawer.md) `[x]` - NavigationDrawer: black panel with cream capital items
- [B-48](docs/backlog/B-48-tooltip.md) `[x]` - Tooltip: a caption with a straight leader line, as on a drawing
- [B-49](docs/backlog/B-49-segmented.md) `[x]` - Segmented button: one frame, the black fill rides linearly
- [B-52](docs/backlog/B-52-empty-state.md) `[x]` - Empty state: a call rather than an apology, with the screen's one raskat wedge
- [B-53](docs/backlog/B-53-stretch-in-place.md) `[x]` - Stretch in place: the poster's fourth layout and the app bar's masthead

**The three demo screens within the floor in both themes; contrast holds with the textures on**

- [B-35](docs/backlog/B-35-demo-screens-and-sample.md) `[x]` - Demo screens and the sample kit: onboarding, feed, settings in both themes
- [B-36](docs/backlog/B-36-accessibility.md) `[x]` - Accessibility: contrast with textures on, grayscale check, touch targets, semantics

**The owner's questions answered; the design re-imported when it changes**

- [B-40](docs/backlog/B-40-licence.md) `[x]` - Licence: MIT
- [B-41](docs/backlog/B-41-registration-unit.md) `[x]` - Registration offset: 1 dp
- [B-42](docs/backlog/B-42-v1-components-designed-here.md) `[x]` - v1 components: designed here, one item each

<!-- END INDEX -->
