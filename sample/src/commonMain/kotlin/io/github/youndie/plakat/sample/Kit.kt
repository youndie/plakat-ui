package io.github.youndie.plakat.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatHalftoneMode
import io.github.youndie.plakat.agit.PlakatPreset
import io.github.youndie.plakat.agit.PlakatPresetImage
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.PlakatRaskatDisc
import io.github.youndie.plakat.agit.PlakatSkeleton
import io.github.youndie.plakat.agit.plakatHalftone
import io.github.youndie.plakat.components.PlakatBadge
import io.github.youndie.plakat.components.PlakatBadgedBox
import io.github.youndie.plakat.components.PlakatBottomSheet
import io.github.youndie.plakat.components.PlakatButton
import io.github.youndie.plakat.components.PlakatCard
import io.github.youndie.plakat.components.PlakatCheckbox
import io.github.youndie.plakat.components.PlakatChip
import io.github.youndie.plakat.components.PlakatCircularProgress
import io.github.youndie.plakat.components.PlakatColumn
import io.github.youndie.plakat.components.PlakatCounter
import io.github.youndie.plakat.components.PlakatCounterColumn
import io.github.youndie.plakat.components.PlakatDangerButton
import io.github.youndie.plakat.components.PlakatDialog
import io.github.youndie.plakat.components.PlakatDialogAction
import io.github.youndie.plakat.components.PlakatDivider
import io.github.youndie.plakat.components.PlakatDividerWeight
import io.github.youndie.plakat.components.PlakatEmptyState
import io.github.youndie.plakat.components.PlakatFab
import io.github.youndie.plakat.components.PlakatIconButton
import io.github.youndie.plakat.components.PlakatLinearProgress
import io.github.youndie.plakat.components.PlakatListItem
import io.github.youndie.plakat.components.PlakatMasthead
import io.github.youndie.plakat.components.PlakatMenu
import io.github.youndie.plakat.components.PlakatMenuItem
import io.github.youndie.plakat.components.PlakatNavigationBar
import io.github.youndie.plakat.components.PlakatNavigationDrawer
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.components.PlakatOutlinedButton
import io.github.youndie.plakat.components.PlakatParagraph
import io.github.youndie.plakat.components.PlakatPictogram
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.components.PlakatPosterLayout
import io.github.youndie.plakat.components.PlakatProjection
import io.github.youndie.plakat.components.PlakatProjectionAxis
import io.github.youndie.plakat.components.PlakatProjectionForm
import io.github.youndie.plakat.components.PlakatPullToRefreshBox
import io.github.youndie.plakat.components.PlakatRadioButton
import io.github.youndie.plakat.components.PlakatScreenWipe
import io.github.youndie.plakat.components.PlakatScrim
import io.github.youndie.plakat.components.PlakatSegmentedButton
import io.github.youndie.plakat.components.PlakatSlider
import io.github.youndie.plakat.components.PlakatSnackbar
import io.github.youndie.plakat.components.PlakatStatus
import io.github.youndie.plakat.components.PlakatStatusKind
import io.github.youndie.plakat.components.PlakatStretch
import io.github.youndie.plakat.components.PlakatSwitch
import io.github.youndie.plakat.components.PlakatTab
import io.github.youndie.plakat.components.PlakatTabContent
import io.github.youndie.plakat.components.PlakatTable
import io.github.youndie.plakat.components.PlakatTabs
import io.github.youndie.plakat.components.PlakatTextButton
import io.github.youndie.plakat.components.PlakatTextField
import io.github.youndie.plakat.components.PlakatTooltip
import io.github.youndie.plakat.components.PlakatTooltipAxis
import io.github.youndie.plakat.components.PlakatTopAppBar
import io.github.youndie.plakat.components.PlakatWindow
import io.github.youndie.plakat.components.PlakatWindowPanel
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatTheme
import kotlinx.coroutines.delay

// Every component, grouped as the design's pages group them. [beat] moves what would otherwise only show on a click:
// it advances by itself while the sample runs, so a number ticks and a window turns with nobody touching them.

private val Width = 358.dp

/** A heading over its [content]. */
@Composable
internal fun KitGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val type = PlakatTheme.typography
    Column(
        Modifier.widthIn(max = 720.dp).testTag("group-$title"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BasicText(type.headline.text(title), style = type.headline.style.copy(color = PlakatTheme.colors.ink))
        PlakatDivider(weight = PlakatDividerWeight.Rule)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Wrap(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

/** The Core page: Button, TextField, Checkbox, Switch, Card, Snackbar, Poster. */
@Composable
fun KitCore() {
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Button") {
            Wrap {
                PlakatButton("READ", {})
                PlakatButton("LOADING", {}, loading = true)
                PlakatButton("DISABLED", {}, enabled = false)
                PlakatOutlinedButton("BOOKMARK", {})
                PlakatTextButton("LATER", {})
                PlakatDangerButton("DELETE", {})
            }
        }
        KitGroup("Text field") {
            var value by remember { mutableStateOf("") }
            PlakatTextField(
                value,
                { value = it },
                Modifier.width(Width),
                label = "Name",
                placeholder = "What is your name",
            )
            PlakatTextField("sheet", {}, Modifier.width(Width), label = "Address", error = "No such address")
        }
        KitGroup("Checkbox, radio, switch") {
            Wrap {
                var checked by remember { mutableStateOf(true) }
                var on by remember { mutableStateOf(true) }
                PlakatCheckbox(checked, { checked = it })
                PlakatCheckbox(false, null, enabled = false)
                PlakatSwitch(on, { on = it })
                PlakatSwitch(false, null, enabled = false)
                var radio by remember { mutableIntStateOf(0) }
                Row(Modifier.selectableGroup()) {
                    repeat(3) { i -> PlakatRadioButton(radio == i, { radio = i }) }
                }
                PlakatRadioButton(true, null, enabled = false)
            }
        }
        KitGroup("Card") {
            PlakatCard({}, Modifier.width(Width)) {
                PlakatGeneratedImage(71, Modifier.size(64.dp), frameWidth = 64.dp, frameHeight = 64.dp)
                BasicText(
                    "A city of plywood",
                    style =
                        PlakatTheme.typography.bodyS.style
                            .copy(color = LocalPlakatContentColor.current),
                )
            }
        }
        KitGroup("Snackbar") {
            PlakatSnackbar("Saved to the sheet", Modifier.width(Width), actionLabel = "UNDO")
        }
        KitGroup("Poster") {
            Wrap {
                PlakatPoster(
                    listOf("READ", "THIS", "NOVEMBER"),
                    "SUBSCRIBE",
                    {},
                    body = "12 new issues.",
                    seed = 1919,
                )
                PlakatPoster(listOf("READ", "THIS", "NOVEMBER"), "SUBSCRIBE", {
                }, body = "12 new issues.", seed = 1919, raskat = true)
                PlakatPoster(listOf("ISSUE", "No. 47"), "READ", {}, layout = PlakatPosterLayout.Banner, seed = 1925)
                PlakatPoster(
                    listOf("READ", "THIS", "NOVEMBER"),
                    "SUBSCRIBE",
                    {},
                    layout = PlakatPosterLayout.Stretch,
                    body = "12 new issues in one feed. Read them in order.",
                )
                PlakatPoster(
                    listOf("READ", "THIS", "NOVEMBER"),
                    "SUBSCRIBE",
                    {},
                    Modifier.width(720.dp),
                    layout = PlakatPosterLayout.Wide,
                    body = "12 new issues in one feed. Read them in order.",
                    seed = 1919,
                )
            }
        }
    }
}

/** The MVP page: IconButton to Counter and the change of screen. */
@Composable
fun KitComponents(beat: Int) {
    val icons = listOf(PlakatIcons.Home, PlakatIcons.Search, PlakatIcons.Share, PlakatIcons.Settings)
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Icon buttons") {
            Wrap {
                for (icon in PlakatIcons.All.take(6)) PlakatIconButton(icon, icon.name, {})
                PlakatFab(PlakatIcons.Add, "Add", {})
            }
        }
        KitGroup("Icons: the kit's own") {
            Wrap {
                PlakatIconButton(PlakatIcons.More, PlakatIcons.More.name, {})
                PlakatIconButton(PlakatIcons.OpensElsewhere, PlakatIcons.OpensElsewhere.name, {})
                PlakatFab(PlakatIcons.More, "More", {})
            }
        }
        KitGroup("Chips and badges") {
            Wrap {
                var selected by remember { mutableIntStateOf(0) }
                for ((i, word) in listOf("TYPE", "CITY", "DESIGN").withIndex()) {
                    PlakatChip(word, selected == i, { selected = i })
                }
                PlakatBadgedBox(beat) { PlakatIconButton(PlakatIcons.Share, "Share", {}) }
                PlakatBadge(null)
            }
        }
        KitGroup("Status") {
            Wrap {
                PlakatStatus(PlakatStatusKind.Primary, "Open")
                PlakatStatus(PlakatStatusKind.Danger, "Failing")
                PlakatStatus(PlakatStatusKind.Success, "Merged")
                PlakatStatus(PlakatStatusKind.Warning, "Stale")
                PlakatStatus(PlakatStatusKind.Info, "Draft")
            }
        }
        KitGroup("Table") {
            PlakatTable(
                listOf(
                    PlakatColumn("#", width = 32.dp, align = Alignment.End, numeric = true),
                    PlakatColumn("Item"),
                    PlakatColumn("Done", width = 72.dp),
                    PlakatColumn("Days", width = 40.dp, align = Alignment.End, numeric = true),
                ),
                Modifier.width(Width),
            ) {
                for ((i, item) in listOf("Ploughing", "Canal", "Turbine").withIndex()) {
                    row(onClick = {}) {
                        cell("${i + 1}")
                        cell(item)
                        cell { PlakatLinearProgress(((beat + i) % 5) / 4f) }
                        cell("${beat * (i + 1) % 30}")
                    }
                }
            }
        }
        KitGroup("Progress") {
            PlakatLinearProgress((beat % 5) / 4f, Modifier.width(Width))
            Wrap {
                PlakatCircularProgress(null)
                PlakatCircularProgress((beat % 5) / 4f)
            }
        }
        KitGroup("Top bar") {
            PlakatTopAppBar("SHEET", Modifier.width(Width), subtitle = "No. 47")
            PlakatMasthead("SHEET", Modifier.width(Width), startLabel = "No. 47", endLabel = "NOVEMBER 2026")
        }
        KitGroup("Slider") {
            Column(Modifier.width(Width).padding(horizontal = 16.dp)) {
                var stepped by remember { mutableFloatStateOf(.5f) }
                PlakatSlider(stepped, { stepped = it }, steps = 5)
                var free by remember { mutableFloatStateOf(.37f) }
                PlakatSlider(free, { free = it })
                PlakatSlider(.5f, {}, enabled = false, steps = 5)
            }
        }
        KitGroup("Menu") {
            var open by remember { mutableStateOf(false) }
            Box {
                PlakatOutlinedButton("MORE", { open = !open })
                PlakatMenu(open, { open = false }) {
                    PlakatMenuItem("Save", { open = false })
                    PlakatMenuItem("Share", { open = false })
                    PlakatMenuItem("Archive", { open = false }, enabled = false, divider = false)
                }
            }
        }
        KitGroup("Tooltip") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PlakatTooltip("Save to the sheet") { PlakatOutlinedButton("UP", {}) }
                PlakatTooltip(
                    "Save to the sheet",
                    axis = PlakatTooltipAxis.Level,
                ) { PlakatOutlinedButton("SIDEWAYS", {}) }
                PlakatTooltip(
                    "Save to the sheet",
                    axis = PlakatTooltipAxis.Tilted,
                ) { PlakatOutlinedButton("ON THE AXIS", {}) }
            }
        }
        KitGroup("Segments") {
            var segment by remember { mutableIntStateOf(0) }
            PlakatSegmentedButton(listOf("DAY", "WEEK", "MONTH"), segment, { segment = it })
            PlakatSegmentedButton(listOf("DAY", "WEEK", "MONTH"), 1, {}, enabled = false)
        }
        KitGroup("Pull to refresh") {
            var refreshing by remember { mutableStateOf(false) }
            LaunchedEffect(refreshing) {
                if (refreshing) {
                    delay(2000)
                    refreshing = false
                }
            }
            PlakatPullToRefreshBox(refreshing, { refreshing = true }, Modifier.width(Width).height(240.dp)) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    repeat(8) { PlakatListItem("Issue ${it + 1}") }
                }
            }
        }
        KitGroup("Projection") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PlakatProjection(
                    listOf("NOVEMBER"),
                    PlakatProjectionForm.Horn(16.dp, 312.dp, 358.dp, 56.dp),
                    Modifier.size(390.dp, 320.dp),
                )
                PlakatProjection(
                    listOf("NEW"),
                    PlakatProjectionForm.Box(16.dp, 152.dp, 260.dp, 160.dp, 96.dp, 184.dp, 288.dp),
                    Modifier.size(390.dp, 320.dp),
                )
                PlakatProjection(
                    listOf("LISTEN"),
                    PlakatProjectionForm
                        .Cone(0.dp, 0.dp, 320.dp, 20.dp, 92.dp, 16f, 62f, PlakatProjectionAxis.Tilted)
                        .fittedIn(DpRect(24.dp, 16.dp, 374.dp, 304.dp)),
                    Modifier.size(390.dp, 320.dp),
                )
            }
        }
        KitGroup("Navigation") {
            var nav by remember { mutableIntStateOf(0) }
            PlakatNavigationBar(
                icons.zip(listOf("FEED", "SEARCH", "SHARE", "OPTIONS")).map { (icon, word) ->
                    PlakatNavigationItem(icon, word)
                },
                nav,
                {
                    nav =
                        it
                },
                Modifier.width(Width),
            )
        }
        KitGroup("Tabs") {
            val tab = beat % 3
            PlakatTabs(
                listOf(PlakatTab("FEED"), PlakatTab("ARCHIVE"), PlakatTab("SAVED")),
                tab,
                {},
                Modifier.width(Width),
            )
            PlakatTabContent(3, tab, Modifier.width(Width).height(64.dp)) {
                PlakatRapportFill(1923 + it * 2, Modifier.fillMaxSize())
            }
        }
        KitGroup("List items") {
            Column(Modifier.width(Width)) {
                PlakatListItem("Type", supporting = "5 min", number = "01", onClick = {})
                PlakatListItem("Digest", trailing = { PlakatSwitch(true, {}) })
            }
        }
        KitGroup("Dialog") {
            var open by remember { mutableStateOf(false) }
            val opener = remember { FocusRequester() }
            PlakatOutlinedButton("OPEN", { open = true }, Modifier.focusRequester(opener).testTag("open-dialog"))
            Box(Modifier.width(Width).height(if (open) 420.dp else 1.dp)) {
                PlakatDialog(open, "DELETE", { open = false }, text = "It can be restored.", returnFocusTo = opener) {
                    PlakatDialogAction("CANCEL", { open = false })
                    PlakatDialogAction("DELETE", { open = false }, danger = true)
                }
            }
        }
        KitGroup("Scrim, under an overlay of the app's own") {
            var open by remember { mutableStateOf(false) }
            val opener = remember { FocusRequester() }
            PlakatOutlinedButton("OPEN", { open = true }, Modifier.focusRequester(opener).testTag("open-scrim"))
            Box(Modifier.width(Width).height(if (open) 240.dp else 1.dp)) {
                PlakatScrim(open, { open = false })
                if (open) {
                    PlakatCard(
                        { open = false },
                        Modifier.align(Alignment.Center).width(240.dp),
                    ) {
                        PlakatParagraph(
                            "Not the kit's dialog: a card of the app's, over the kit's scrim.",
                            PlakatTheme.typography.bodyM,
                        )
                    }
                }
            }
        }
        KitGroup("Bottom sheet") {
            var open by remember { mutableStateOf(false) }
            val opener = remember { FocusRequester() }
            PlakatOutlinedButton("OPEN", { open = true }, Modifier.focusRequester(opener).testTag("open-sheet"))
            Box(Modifier.width(Width).height(if (open) 420.dp else 1.dp)) {
                PlakatBottomSheet(open, { open = false }, paneTitle = "Actions", returnFocusTo = opener) {
                    PlakatListItem("Save", onClick = { open = false })
                    PlakatListItem("Share", onClick = { open = false }, divider = false)
                }
            }
        }
        KitGroup("Navigation drawer") {
            var open by remember { mutableStateOf(false) }
            var place by remember { mutableIntStateOf(0) }
            val opener = remember { FocusRequester() }
            PlakatOutlinedButton("OPEN", { open = true }, Modifier.focusRequester(opener).testTag("open-drawer"))
            Box(Modifier.width(Width).height(if (open) 420.dp else 1.dp)) {
                PlakatNavigationDrawer(
                    open,
                    icons.zip(listOf("FEED", "SEARCH", "SHARE", "OPTIONS")).map { (icon, word) ->
                        PlakatNavigationItem(icon, word)
                    },
                    place,
                    { place = it },
                    { open = false },
                    paneTitle = "Sections",
                    returnFocusTo = opener,
                )
            }
        }
        KitGroup("Window") {
            val panels =
                remember {
                    listOf(300, 317, 334).mapIndexed { i, seed ->
                        PlakatWindowPanel(listOf("READ", "SAVE", "SHARE")[i], "Panel ${i + 1}.") {
                            PlakatGeneratedImage(seed, Modifier.fillMaxSize(), 171.dp, 160.dp)
                        }
                    }
                }
            PlakatWindow(panels, beat % 2, Modifier.width(Width), panelsInView = 2)
        }
        KitGroup("Empty state") {
            PlakatEmptyState("SAVE YOUR FIRST ARTICLE", "Bookmarks appear here and open offline.", "TO THE FEED", {
            }, Modifier.width(Width))
        }
        KitGroup("Counter") {
            Row(Modifier.width(Width), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlakatCounter(39 + beat * 8, "ARTICLES", Modifier.weight(1f), legend = "= 5 articles", perPictogram = 5)
                PlakatCounterColumn(39 + beat, "DAYS", Modifier.weight(1f))
            }
        }
        KitGroup("Screen change") {
            val depth = beat % 2 + 1
            PlakatScreenWipe(depth, depth, Modifier.width(Width).height(240.dp)) {
                PlakatPresetImage(if (it == 1) PlakatPreset.Klin else PlakatPreset.Os, Modifier.fillMaxSize())
            }
        }
    }
}

/** The Agit page and the Stretch page. */
@Composable
fun KitAgit() {
    val colors = PlakatTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
        KitGroup("Presets") {
            Wrap { for (preset in PlakatPreset.entries) PlakatPresetImage(preset, Modifier.size(180.dp, 120.dp)) }
        }
        KitGroup("Generator") {
            Wrap { for (seed in 1919..1924) PlakatGeneratedImage(seed, Modifier.size(180.dp, 120.dp)) }
        }
        KitGroup("Repeats") {
            Wrap { for (seed in listOf(1923, 1925, 1927, 1929)) PlakatRapportFill(seed, Modifier.size(180.dp, 96.dp)) }
            PlakatRapportFill(1923, Modifier.width(Width).height(24.dp), cell = 4.dp)
            PlakatSkeleton(1923, Modifier.size(64.dp))
        }
        KitGroup("Photomontage") {
            val source = remember { standIn() }
            Wrap {
                Box(
                    Modifier
                        .size(
                            180.dp,
                        ).clip(
                            androidx.compose.foundation.shape.CircleShape,
                        ).plakatHalftone(source, colors.red, colors.paper),
                )
                Box(Modifier.size(180.dp).plakatHalftone(source, colors.ink, colors.paper, PlakatHalftoneMode.Mask))
                PlakatRaskatDisc(Modifier.size(180.dp))
            }
        }
        KitGroup("Stretch") {
            PlakatStretch("SHEET", Modifier.width(Width).height(120.dp))
        }
    }
}

/** A picture to print, in place of the photographs the kit does not ship. */
private fun standIn(): ImageBitmap {
    val image = ImageBitmap(340, 420)
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), Size(340f, 420f)) {
        drawRect(
            Brush.radialGradient(
                0f to Color(0xFF151515),
                .55f to Color(0xFF8A8A8A),
                1f to Color(0xFFF4F4F4),
                center = Offset(187f, 168f),
                radius = 210f,
            ),
        )
    }
    return image
}
