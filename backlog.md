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
through pull requests and merges its own on green (`CLAUDE.md`, «Workflow»).

## Stages

| Stage | What closes it |
|---|---|
| `stage-0-risks` | Goldens and references regenerate deterministically; the generator and raskat ports match them on every target; the floor and the frame cost have measured numbers |
| `stage-1-materials` | Tokens generated and checked; the theme, print primitives and icons match their cells |
| `stage-2-components` | Each component matches its page's states in both themes and its motion row at the row's timestamps |
| `stage-3-screens` | The three demo screens within the floor in both themes; contrast holds with the textures on |
| `stage-4-product` | The owner's questions answered; the design re-imported when it changes |

<!-- BEGIN INDEX -->

## Open (5)

| Task | | Priority | Size | Blocked by |
|---|---|---|---|---|
| [B-76](docs/backlog/B-76-table.md) `[ ]` | PlakatTable: rows of fixed columns under a header | P2 | M | - |
| [B-77](docs/backlog/B-77-more-icons-and-own-stencils.md) `[ ]` | Icons: More and Opens elsewhere, and stencils a consumer can make | P2 | S | - |
| [B-79](docs/backlog/B-79-dialog-content-slot.md) `[ ]` | PlakatDialog: a content slot between the text and the actions | P2 | S | - |
| [B-80](docs/backlog/B-80-public-scrim.md) `[ ]` | The dot screen under the kit's overlays, public for a consumer's own | P2 | S | - |
| [B-81](docs/backlog/B-81-banner.md) `[ ]` | PlakatBanner: a warning that stays in the screen until its condition passes | P2 | S | B-75 |

## Closed (76)

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
- [B-50](docs/backlog/B-50-pull-to-refresh.md) `[x]` - Pull-to-refresh: a red wedge grows with the finger, then a stepped sector
- [B-52](docs/backlog/B-52-empty-state.md) `[x]` - Empty state: a call rather than an apology, with the screen's one raskat wedge
- [B-53](docs/backlog/B-53-stretch-in-place.md) `[x]` - Stretch in place: the poster's fourth layout and the app bar's masthead
- [B-54](docs/backlog/B-54-window-insets.md) `[x]` - Window insets: the kit's edges under a status bar and a gesture bar
- [B-55](docs/backlog/B-55-masthead-throws-by-width.md) `[x]` - A masthead throws on a phone's width: the stretch's refusal at drawing
- [B-58](docs/backlog/B-58-unbounded-width.md) `[x]` - Nothing throws under an unbounded width
- [B-59](docs/backlog/B-59-textfield-caret.md) `[x]` - TextField: the caret stays at the insertion point, inside the field
- [B-60](docs/backlog/B-60-visible-keyboard-focus.md) `[x]` - Keyboard focus shows on every control it lands on
- [B-61](docs/backlog/B-61-hidden-snackbar-takes-no-input.md) `[x]` - A hidden Snackbar takes no input
- [B-62](docs/backlog/B-62-dialog-long-text.md) `[x]` - Dialog: long text scrolls and the actions stay reachable
- [B-63](docs/backlog/B-63-overlay-focus-and-input.md) `[x]` - Overlays and the screen change: where the keyboard and clicks go
- [B-64](docs/backlog/B-64-textfield-semantics-jolt-selection.md) `[x]` - TextField: label and error on the editable node, the jolt, the selection colour
- [B-65](docs/backlog/B-65-long-content.md) `[x]` - Long content: what a component does with text that does not fit
- [B-66](docs/backlog/B-66-hostile-values.md) `[x]` - Values a consumer can compute: NaN, zero, out of range
- [B-67](docs/backlog/B-67-qa-polish.md) `[x]` - Small remainders of the QA pass
- [B-68](docs/backlog/B-68-release-ink-question.md) `[x]` - On release a word was ink on ink for its first frame
- [B-69](docs/backlog/B-69-real-window-pass.md) `[x]` - Check what only a real window shows
- [B-70](docs/backlog/B-70-fixed-sheets-under-a-large-font.md) `[x]` - Fixed sheets under a large font and a long title: TopAppBar and Poster
- [B-72](docs/backlog/B-72-tooltip-plate-takes-a-press.md) `[x]` - A tooltip's plate takes a press meant for what is under it
- [B-73](docs/backlog/B-73-pull-by-a-mouse.md) `[x]` - Pull-to-refresh does nothing with a mouse
- [B-74](docs/backlog/B-74-dialog-cut-shows-the-screen.md) `[x]` - The dialog's cut corner shows paper, not the screen

**The three demo screens within the floor in both themes; contrast holds with the textures on**

- [B-35](docs/backlog/B-35-demo-screens-and-sample.md) `[x]` - Demo screens and the sample kit: onboarding, feed, settings in both themes
- [B-36](docs/backlog/B-36-accessibility.md) `[x]` - Accessibility: contrast with textures on, grayscale check, touch targets, semantics

**The owner's questions answered; the design re-imported when it changes**

- [B-06](docs/backlog/B-06-frame-cost-measurement.md) `[x]` - Frame cost on a Pixel 6a: raskat roll, ten cards cascading with texture and registration
- [B-37](docs/backlog/B-37-public-repository-and-publishing.md) `[x]` - Public repository and publishing to Reposilite on every main — last
- [B-40](docs/backlog/B-40-licence.md) `[x]` - Licence: MIT
- [B-41](docs/backlog/B-41-registration-unit.md) `[x]` - Registration offset: 1 dp
- [B-42](docs/backlog/B-42-v1-components-designed-here.md) `[x]` - v1 components: designed here, one item each
- [B-43](docs/backlog/B-43-re-import-the-design.md) `[-]` - Re-import the design project on a branch of its own
- [B-71](docs/backlog/B-71-wasm-test-time-limit.md) `[x]` - Wasm browser tests go red on a runner's speed: Mocha's limit of 2 s
- [B-75](docs/backlog/B-75-status-mark.md) `[x]` - PlakatStatus: a word with its sign, for the semantic kinds
- [B-78](docs/backlog/B-78-public-css-text.md) `[x]` - A consumer's text in the kit's styles sits where Blink puts it

**stage-3-demo**

- [B-56](docs/backlog/B-56-card-press-words.md) `[x]` - A pressed card's words stay dark in the kit and the demo
- [B-57](docs/backlog/B-57-english-kit.md) `[x]` - The kit speaks English: the sample, the site and the default strings

<!-- END INDEX -->
