package io.github.youndie.plakat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.agit.PlakatPreset
import io.github.youndie.plakat.agit.PlakatPresetImage
import io.github.youndie.plakat.agit.PlakatRapportFill
import io.github.youndie.plakat.agit.PlakatRaskatDisc
import io.github.youndie.plakat.agit.PlakatSkeleton
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.print.PlakatRegisteredText
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val Nav =
    listOf(PlakatNavigationItem(PlakatIcons.Home, "HOME"), PlakatNavigationItem(PlakatIcons.Search, "FIND"))

private val Horn = PlakatProjectionForm.Horn(16.dp, 312.dp, 358.dp, 56.dp)

/** Every public component and picture, with what it needs and no more. */
private val Subjects: List<Pair<String, @Composable () -> Unit>> =
    listOf(
        "Button" to { PlakatButton("READ IT", {}) },
        "Outlined" to { PlakatOutlinedButton("READ IT", {}) },
        "TextButton" to { PlakatTextButton("READ IT", {}) },
        "Danger" to { PlakatDangerButton("READ IT", {}) },
        "Badge" to { PlakatBadgedBox(12) { PlakatIcon(PlakatIcons.Search, null) } },
        "Card" to { PlakatCard({}) { BasicText("Words") } },
        "Checkbox" to { PlakatCheckbox(true, {}) },
        "Radio" to { PlakatRadioButton(true, {}) },
        "Switch" to { PlakatSwitch(true, {}) },
        "Chip" to { PlakatChip("ALL", true, {}) },
        "Counter" to { PlakatCounter(47, "ARTICLES", legend = "= 5", perPictogram = 5) },
        "CounterStretched" to { PlakatCounter(47, "ARTICLES", perPictogram = 5, stretched = true) },
        "CounterColumn" to { PlakatCounterColumn(47, "ARTICLES") },
        "Dialog" to {
            Box(Modifier.size(390.dp, 500.dp)) {
                PlakatDialog(true, "TITLE", {}, text = "Words") { PlakatDialogAction("OK", {}) }
            }
        },
        "BottomSheet" to { Box(Modifier.size(390.dp, 500.dp)) { PlakatBottomSheet(true, {}) { BasicText("Words") } } },
        "Drawer" to { Box(Modifier.size(390.dp, 500.dp)) { PlakatNavigationDrawer(true, Nav, 0, {}, {}) } },
        "EmptyState" to { PlakatEmptyState("NOTHING", "Add one.", "ADD", {}) },
        "Fab" to { PlakatFab(PlakatIcons.Search, "S", {}) },
        "IconButton" to { PlakatIconButton(PlakatIcons.Search, "S", {}) },
        "ListItem" to {
            PlakatListItem("Row", supporting = "Support", trailing = { PlakatSwitch(true, {}) }, onClick = {})
        },
        "Divider" to { PlakatDivider() },
        "Table" to {
            PlakatTable(listOf(PlakatColumn("#", 32.dp, numeric = true), PlakatColumn("ITEM"))) {
                row(onClick = {}) {
                    cell("12")
                    cell("Ploughing")
                }
            }
        },
        "Menu" to {
            Box {
                BasicText("A")
                PlakatMenu(true, {}) { PlakatMenuItem("One", {}) }
            }
        },
        "NavigationBar" to { PlakatNavigationBar(Nav, 0, {}) },
        "PosterCircle" to { PlakatPoster(listOf("READ", "THIS"), "GO", {}, body = "Words", seed = 1) },
        "PosterBackground" to {
            PlakatPoster(
                listOf("READ", "THIS"),
                "GO",
                {},
                layout = PlakatPosterLayout.Background,
                body = "Words",
                seed = 1,
            )
        },
        "PosterBanner" to
            { PlakatPoster(listOf("READ", "THIS"), "GO", {}, layout = PlakatPosterLayout.Banner, seed = 1) },
        "PosterWide" to {
            PlakatPoster(listOf("READ", "THIS"), "GO", {}, layout = PlakatPosterLayout.Wide, body = "Words", seed = 1)
        },
        "PosterStretch" to {
            PlakatPoster(
                listOf("READ", "THIS"),
                "GO",
                {},
                layout = PlakatPosterLayout.Stretch,
                body = "Words",
                seed = 1,
            )
        },
        "LinearProgress" to { PlakatLinearProgress(.5f) },
        "CircularProgress" to { PlakatCircularProgress(.5f) },
        "Spinner" to { PlakatCircularProgress(null) },
        "ProjectionHorn" to { PlakatProjection(listOf("READ"), Horn, Modifier.size(390.dp, 392.dp)) },
        "ProjectionNoSize" to { PlakatProjection(listOf("READ"), Horn) },
        "ProjectionFill" to { PlakatProjection(listOf("READ"), Horn, Modifier.fillMaxSize()) },
        "PullToRefresh" to { PlakatPullToRefreshBox(true, {}) { BasicText("Words") } },
        "ScreenWipe" to { PlakatScreenWipe(1, 0) { BasicText("S$it") } },
        "Segmented" to { PlakatSegmentedButton(listOf("A", "B"), 0, {}) },
        "Slider" to { PlakatSlider(.5f, {}, steps = 3) },
        "Snackbar" to { PlakatSnackbar("Saved", actionLabel = "UNDO") },
        "Stretch" to { PlakatStretch("STRETCH", Modifier.fillMaxWidth().height(60.dp)) },
        "Tabs" to { PlakatTabs(listOf("A", "B").map { PlakatTab(it) }, 0, {}) },
        "TabContent" to { PlakatTabContent(2, 0) { BasicText("P$it") } },
        "TextField" to { PlakatTextField("Words", {}, label = "L", error = "E") },
        "Tooltip" to { PlakatTooltip("Caption") { BasicText("A") } },
        "TopAppBar" to { PlakatTopAppBar("SHEET", subtitle = "S") },
        "Masthead" to { PlakatMasthead("SHEET", startLabel = "A", endLabel = "B") },
        "Window" to { PlakatWindow(listOf(windowPanel()), 0) },
        "WindowLarge" to { PlakatWindow(listOf(windowPanel()), 0, large = true) },
        "Preset" to { PlakatPresetImage(PlakatPreset.entries[0]) },
        "Generated" to { PlakatGeneratedImage(1) },
        "Rapport" to { PlakatRapportFill(1, Modifier.fillMaxWidth().height(24.dp)) },
        "Skeleton" to { PlakatSkeleton(1, Modifier.fillMaxWidth().height(24.dp)) },
        "RaskatDisc" to { PlakatRaskatDisc(Modifier.size(120.dp)) },
        "Icon" to { PlakatIcon(PlakatIcons.Search, "S") },
        "Registered" to { PlakatRegisteredText("TEXT", PlakatTheme.typography.displayL.style) },
    )

private fun windowPanel() =
    PlakatWindowPanel("CAP", "Body") {
        PlakatPresetImage(PlakatPreset.entries[0], Modifier.fillMaxSize())
    }

/** The constraints a consumer's layout can hand a component. */
private val Cells: List<Pair<String, @Composable (@Composable () -> Unit) -> Unit>> =
    listOf(
        "horizontal scroll" to { c -> Row(Modifier.horizontalScroll(rememberScrollState())) { c() } },
        "IntrinsicSize.Max width" to { c -> Box(Modifier.width(IntrinsicSize.Max)) { c() } },
        "IntrinsicSize.Max height" to { c -> Box(Modifier.width(300.dp).height(IntrinsicSize.Max)) { c() } },
        "unbounded width" to { c -> Box(Modifier.width(100.dp).wrapContentWidth(unbounded = true)) { c() } },
        "vertical scroll" to { c -> Column(Modifier.verticalScroll(rememberScrollState())) { c() } },
        "zero" to { c -> Box(Modifier.size(0.dp)) { c() } },
        "1 wide" to { c -> Box(Modifier.width(1.dp)) { c() } },
        "1 high" to { c -> Box(Modifier.width(300.dp).height(1.dp)) { c() } },
        "1920 at density 2" to { c ->
            CompositionLocalProvider(LocalDensity provides Density(2f)) { Box(Modifier.requiredWidth(1920.dp)) { c() } }
        },
        "density 2.75" to { c ->
            CompositionLocalProvider(LocalDensity provides Density(2.75f, 1f)) { Box(Modifier.width(390.dp)) { c() } }
        },
        "font scale 2" to { c ->
            CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) { Box(Modifier.width(390.dp)) { c() } }
        },
    )

@OptIn(ExperimentalTestApi::class)
class PlakatConstraintSweepTest {
    @Test
    fun `nothing throws in any cell`() {
        val thrown = mutableListOf<String>()
        for ((name, subject) in Subjects) {
            for ((cell, wrap) in Cells) {
                runCatching {
                    runComposeUiTest {
                        mainClock.autoAdvance = false
                        setContent { PlakatTheme { Box(Modifier.size(760.dp)) { wrap { subject() } } } }
                        mainClock.advanceTimeBy(700)
                        onAllNodes(isRoot())[0].captureToImage()
                    }
                }.exceptionOrNull()?.let { thrown += "$name in $cell: ${it.message?.lineSequence()?.firstOrNull()}" }
            }
        }
        assertTrue(thrown.isEmpty(), thrown.joinToString("\n"))
    }

    @Test
    fun `tabs given no limit take slots for their widest word`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatTabs(listOf("ANNOUNCEMENTS", "A").map { PlakatTab(it) }, 0, {})
                    }
                }
            }
            val long = onNodeWithContentDescription("ANNOUNCEMENTS").getBoundsInRoot().width
            assertEquals(long, onNodeWithContentDescription("A").getBoundsInRoot().width)
            assertTrue(long in 120.dp..300.dp, "a slot of $long")
        }

    @Test
    fun `tabs given no limit still have targets of 48`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatTabs(listOf(PlakatTab("I")), 0, {})
                    }
                }
            }
            assertEquals(48.dp, onNodeWithContentDescription("I").getBoundsInRoot().width)
        }

    @Test
    fun `a navigation bar given no limit takes slots for its widest word`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatNavigationBar(
                            listOf(
                                PlakatNavigationItem(PlakatIcons.Home, "RECOMMENDATIONS"),
                                PlakatNavigationItem(PlakatIcons.Search, "A"),
                            ),
                            0,
                            {},
                        )
                    }
                }
            }
            val long = onNodeWithContentDescription("RECOMMENDATIONS").getBoundsInRoot().width
            assertEquals(long, onNodeWithContentDescription("A").getBoundsInRoot().width)
            assertTrue(long in 120.dp..300.dp, "a slot of $long")
        }

    @Test
    fun `a window given no limit is as wide as its words and its picture fills that`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatWindow(
                            listOf(PlakatWindowPanel("A CAPTION") { Box(Modifier.fillMaxSize().testTag("picture")) }),
                            0,
                        )
                    }
                }
            }
            val picture = onNodeWithTag("picture").getBoundsInRoot().width
            assertTrue(picture in 40.dp..300.dp, "a picture of $picture")
        }

    @Test
    fun `a counter given no limit is as wide as its content`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        PlakatCounter(47, "ARTICLES", Modifier.testTag("tile"), perPictogram = 5)
                    }
                }
            }
            val tile = onNodeWithTag("tile").getBoundsInRoot().width
            assertTrue(tile in 60.dp..240.dp, "a tile of $tile")
        }

    @Test
    fun `an empty state given no limit keeps its band`() =
        runComposeUiTest {
            setContent {
                PlakatTheme(colors = PlakatPalettes.Paper, reduceMotion = true) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).background(PlakatTheme.colors.paper)) {
                        PlakatEmptyState("NOTHING", "Add one.", "ADD", {}, Modifier.testTag("state"))
                    }
                }
            }
            val image = onNodeWithTag("state").captureToImage().toAwtImage()
            val concrete = PlakatPalettes.Paper.concrete.toArgb()
            val pixels =
                (0 until image.width).count { x ->
                    (0 until image.height).any { y ->
                        image.getRGB(x, y) ==
                            concrete
                    }
                }
            assertEquals(image.width, pixels, "columns with the band's concrete in them")
        }
}

private operator fun ClosedRange<Dp>.contains(value: Dp): Boolean = value >= start && value <= endInclusive
