package io.github.youndie.plakat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.agit.StripCell
import io.github.youndie.plakat.agit.StripMotif
import io.github.youndie.plakat.agit.StripPaint
import io.github.youndie.plakat.agit.plakatStrip
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlin.math.ceil
import kotlin.math.floor

/** The bar's two heights: the MVP page's expanded parts, 8 + 48, 4 + 32 and 12 + 24, and the collapsed `56` and its rule of 3. */
private val ExpandedHeight = 128.dp
private val RowHeight = 56.dp
private val CollapsedHeight = RowHeight + PlakatLines.Rule

/** A frame's default, as a fixture has no system's bar to stand under. */
internal val NoInsets = WindowInsets(0, 0, 0, 0)

/** The row's inset: `padding: 0 4px`, with `gap: 4px`. */
private val RowInset = 4.dp

/** The row's targets stand 8 from the top expanded (`padding: 8px 4px 0`) and centred, so 4, collapsed. */
private val ExpandedRowTop = 8.dp

/**
 * The big title: `padding: 4px 16px 0; font-size: 32px; line-height: 1`, under the row. A line shorter than
 * the face is beyond [cssLine], so it is laid out as a `normal` line of 40, its baseline 30 down, and
 * lifted the 4 that `line-height: 1` takes off the top: the baseline stands 26 under the padding, as in Blink.
 */
private const val TITLE_SP = 32
private val TitleTop = RowHeight

/** The subtitle shares the title's baseline (`align-items: baseline`): 30 down the title's line, 12 down its own. */
private val SubtitleDrop = 18.dp

/** The strip: `height: 24px; margin-top: 12px`, of 12 px cells. */
private val StripHeight = 24.dp
private val StripCellSize = 12.dp
private val StripTop = ExpandedHeight - StripHeight

/** The collapsed title: `font-size: 16px`. */
private const val COLLAPSED_TITLE_SP = 16

/**
 * How far a [PlakatTopAppBar] has folded: [fraction] goes linearly with the scroll, 0 expanded to 1
 * collapsed. Give [nestedScrollConnection] to the scrolling content's `nestedScroll`: it folds on the way
 * down before the list moves, and unfolds on the way up once the list is back at its top.
 */
@Stable
public class PlakatTopAppBarState {
    /** The fold's way in pixels, set by the scroll; the bar reports how far it can go. */
    internal var foldPx: Float by mutableFloatStateOf(0f)
    internal var rangePx: Float by mutableFloatStateOf(0f)

    public val fraction: Float get() = if (rangePx > 0f) (foldPx / rangePx).coerceIn(0f, 1f) else 0f

    /** Sets the fold outright, for a fixture of a state between the page's two. */
    internal fun foldTo(
        px: Float,
        range: Float,
    ) {
        rangePx = range
        foldPx = px
    }

    public val nestedScrollConnection: NestedScrollConnection =
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y >= 0f) return Offset.Zero
                val before = foldPx
                foldPx = (before - available.y).coerceAtMost(rangePx)
                return Offset(0f, before - foldPx)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y <= 0f) return Offset.Zero
                val before = foldPx
                foldPx = (before - available.y).coerceAtLeast(0f)
                return Offset(0f, before - foldPx)
            }
        }
}

/** The masthead, the Stretch page's: `padding: 16px 16px 0`, its labels, the title `margin-top: 8px`, 120 high, then the bar `margin-top: 12px` and 16 under it. */
private val MastheadTop = PlakatGrid.Margin
private val MastheadTitleGap = PlakatGrid.Step
private val MastheadTitleHeight = 120.dp
private val MastheadBarGap = 12.dp
private val MastheadFoot = PlakatGrid.Margin

/** 16 + the labels' line of 15 + 8 + 120 + 12 + 8 + 16. */
private val MastheadHeight = 195.dp

/** A state for a [PlakatTopAppBar], unfolded. */
@Composable
public fun rememberPlakatTopAppBarState(): PlakatTopAppBarState = remember { PlakatTopAppBarState() }

/**
 * The newspaper header. Expanded, under a row of [navigationIcon] and [actions], [title] large in the
 * display face with [subtitle] at the end of its line, then a 24 dp rapport strip seeded by [seed] (by
 * default from the title); 128 dp in all. As [state] folds, linearly with the scroll and the same under
 * reduced motion (`fold`), the title and the strip ride up under the row, [collapsedTitle] opens in the
 * row by a wipe from the left, and a 3 dp rule grows at the bottom: 59 dp collapsed. Without a [state] it
 * stands expanded.
 */
@Composable
public fun PlakatTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    collapsedTitle: String = title,
    seed: Int = plakatPosterSeed(listOf(title)),
    state: PlakatTopAppBarState? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val own = state ?: rememberPlakatTopAppBarState()
    PlakatTopAppBarFrame(title, modifier, subtitle, collapsedTitle, seed, own, navigationIcon, actions, windowInsets)
}

/** A title a reader hears as the bar's heading, or one it does not. */
private fun barTitleSemantics(heard: Boolean): Modifier =
    if (heard) Modifier.semantics { heading() } else Modifier.clearAndSetSemantics { }

@Composable
internal fun PlakatTopAppBarFrame(
    title: String,
    modifier: Modifier,
    subtitle: String?,
    collapsedTitle: String,
    seed: Int,
    state: PlakatTopAppBarState,
    navigationIcon: (@Composable () -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets = NoInsets,
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val events by rememberUpdatedState(PlakatTheme.events)
    // `fold` when the bar comes to rest at either end, not on each pixel of the way.
    LaunchedEffect(state) {
        snapshotFlow { state.fraction }
            .filter { it == 0f || it == 1f }
            .distinctUntilChanged()
            .drop(1)
            .collect { events.onEvent(PlakatEvent.Fold) }
    }
    val cells = remember(seed) { plakatStrip(seed, MAX_STRIP_COLUMNS) }
    // Its title stands at a fixed place on a sheet that folds by a fixed range of pixels, with the rapport a fixed distance
    // under it, so it keeps the design's size whatever the system's font size, as a poster's does.
    val density = LocalDensity.current
    val titleSize = with(density) { TITLE_SP.dp.toSp() }
    val collapsedSize = with(density) { COLLAPSED_TITLE_SP.dp.toSp() }
    // Both titles are drawn at any fold, but a reader hears one: whichever shows more.
    val folded by remember(state) { derivedStateOf { state.fraction >= .5f } }
    CompositionLocalProvider(LocalPlakatContentColor provides colors.ink) {
        Box(
            modifier
                .fillMaxWidth()
                .background(colors.paper)
                .windowInsetsPadding(windowInsets)
                .layout { measurable, constraints ->
                    val range = (ExpandedHeight - CollapsedHeight).toPx()
                    state.rangePx = range
                    val height = (ExpandedHeight.toPx() - range * state.fraction).toInt()
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(placeable.width, height) { placeable.place(0, 0) }
                }.clipToBounds()
                .background(colors.paper),
        ) {
            // What rides up under the row: the fold moves it by as much as the bar has shrunk.
            // It keeps its full height whatever the bar has shrunk to, or the strip would be squeezed out.
            Box(
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, -state.foldPx.toInt()) }
                    .wrapContentHeight(Alignment.Top, unbounded = true)
                    .height(ExpandedHeight),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = PlakatGrid.Margin, top = TitleTop, end = PlakatGrid.Margin),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // A long title gives way, and the subtitle keeps its place beside it.
                    BasicText(
                        title,
                        Modifier.weight(1f, fill = false).then(barTitleSemantics(!folded)),
                        style =
                            type.displayL.style
                                .copy(fontSize = titleSize, color = colors.ink)
                                .cssLine(PlakatFaceMetrics.Display),
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        BasicText(
                            type.label.text(subtitle),
                            Modifier.padding(top = SubtitleDrop),
                            style =
                                type.label.style
                                    .copy(color = colors.ink)
                                    .cssLine(PlakatFaceMetrics.Body),
                            softWrap = false,
                        )
                    }
                }
                Canvas(
                    Modifier
                        .padding(top = StripTop)
                        .fillMaxWidth()
                        .height(StripHeight)
                        .clipToBounds(),
                ) {
                    drawStrip(cells, colors)
                }
            }
            // The row over it, on its paper; its targets settle from 8 down to the 4 of a centred row.
            Box(Modifier.fillMaxWidth().height(RowHeight).background(colors.paper)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .offset {
                            val top =
                                ExpandedRowTop.toPx() -
                                    (ExpandedRowTop - (RowHeight - PlakatSizes.Touch) / 2).toPx() * state.fraction
                            IntOffset(0, top.toInt())
                        }.height(PlakatSizes.Touch)
                        .padding(horizontal = RowInset),
                    horizontalArrangement = Arrangement.spacedBy(RowInset),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    navigationIcon?.invoke()
                    BasicText(
                        collapsedTitle,
                        Modifier
                            .weight(
                                1f,
                            ).plakatWipe(PlakatWipe.Left) { state.fraction }
                            .then(barTitleSemantics(folded)),
                        style =
                            type.displayL.style
                                .copy(fontSize = collapsedSize, color = colors.ink)
                                .cssLine(PlakatFaceMetrics.Display),
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    actions()
                }
            }
            // The rule grows at the bottom as the strip leaves.
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val h = (PlakatLines.Rule.toPx() * state.fraction).toInt()
                        val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
                        layout(placeable.width, h) { placeable.place(0, 0) }
                    }.background(colors.ink),
            )
        }
    }
}

/** Enough columns of 12 dp for the widest window: the tile does not depend on the count, only how far it goes on. */
private const val MAX_STRIP_COLUMNS = 400

/** The strip's cells, 12 dp each, as the page's `div`s: squares, halves, a bar, wedges and inset circles. */
private fun DrawScope.drawStrip(
    cells: List<StripCell>,
    colors: PlakatColors,
) {
    val c = StripCellSize.toPx()
    val across = ceil(size.width / c).toInt()
    for (cell in cells) {
        if (cell.column >= across) continue
        val x = cell.column * c
        val y = cell.row * c
        val paint: Color =
            when (cell.paint) {
                StripPaint.Red -> colors.red
                StripPaint.Ink -> colors.ink
                StripPaint.Concrete -> colors.concrete
            }
        when (cell.motif) {
            StripMotif.Square -> {
                drawRect(paint, Offset(x, y), Size(c, c))
            }

            StripMotif.Half -> {
                drawRect(paint, Offset(x, y), Size(c / 2, c))
            }

            StripMotif.Bar -> {
                drawRect(paint, Offset(x, y + c * .375f), Size(c, c * .25f))
            }

            StripMotif.Circle -> {
                // `left: 1.5px` of a 9 px box: Blink snaps the box to the pixel before it rounds its corners.
                val inset = c * .125f
                drawOval(paint, Offset(floor(x + inset + .5f), floor(y + inset + .5f)), Size(c * .75f, c * .75f))
            }

            StripMotif.WedgeLowerRight -> {
                drawPath(triangle(x, y + c, x + c, y, x + c, y + c), paint)
            }

            StripMotif.WedgeUpperLeft -> {
                drawPath(triangle(x, y, x + c, y, x, y + c), paint)
            }
        }
    }
}

private fun triangle(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float,
) = Path().apply {
    moveTo(x1, y1)
    lineTo(x2, y2)
    lineTo(x3, y3)
    close()
}

/**
 * The masthead: the title on the whole width in the stretch ([PlakatStretch], 120 high, one to a screen) under a line
 * of [startLabel] and [endLabel], over a bar of 8; 195 dp in all. As [state] folds, linearly with the scroll, it rides up
 * whole and is never scaled, and the ordinary collapsed bar comes down in its last 56 dp, with [collapsedTitle] and a
 * 3 dp rule at the end: 59 dp collapsed, as a [PlakatTopAppBar]'s. The arrival is designed here; the page says only that
 * the masthead rides up and an ordinary bar stays. Its paper runs under [windowInsets], the system's bar by default, with
 * the masthead inside them.
 */
@Composable
public fun PlakatMasthead(
    title: String,
    modifier: Modifier = Modifier,
    startLabel: String? = null,
    endLabel: String? = null,
    collapsedTitle: String = title,
    state: PlakatTopAppBarState = rememberPlakatTopAppBarState(),
    navigationIcon: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val events by rememberUpdatedState(PlakatTheme.events)
    LaunchedEffect(state) {
        snapshotFlow { state.fraction }
            .filter { it == 0f || it == 1f }
            .distinctUntilChanged()
            .drop(1)
            .collect { events.onEvent(PlakatEvent.Fold) }
    }
    val arrivalPx = with(LocalDensity.current) { (MastheadHeight - CollapsedHeight - RowHeight).toPx() }
    val arriving by remember(state, arrivalPx) { derivedStateOf { state.foldPx > arrivalPx } }
    val label =
        type.label.style
            .copy(color = colors.ink)
            .cssLine(PlakatFaceMetrics.Body)
    CompositionLocalProvider(LocalPlakatContentColor provides colors.ink) {
        Box(
            modifier
                .fillMaxWidth()
                .background(colors.paper)
                .windowInsetsPadding(windowInsets)
                .layout { measurable, constraints ->
                    val range = (MastheadHeight - CollapsedHeight).toPx()
                    state.rangePx = range
                    val height = (MastheadHeight.toPx() - range * state.fraction).toInt()
                    val placeable = measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                    layout(placeable.width, height) { placeable.place(0, 0) }
                }.clipToBounds()
                .background(colors.paper),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, -state.foldPx.toInt()) }
                    .wrapContentHeight(Alignment.Top, unbounded = true)
                    .height(MastheadHeight)
                    .padding(start = PlakatGrid.Margin, top = MastheadTop, end = PlakatGrid.Margin),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Each gives way to half of the row rather than running into the other.
                    BasicText(
                        type.label.text(startLabel.orEmpty()),
                        Modifier.weight(1f, fill = false),
                        style = label,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    BasicText(
                        type.label.text(endLabel.orEmpty()),
                        Modifier.weight(1f, fill = false),
                        style = label,
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(MastheadTitleGap))
                PlakatStretch(
                    title,
                    Modifier
                        .fillMaxWidth()
                        .height(MastheadTitleHeight)
                        .then(barTitleSemantics(!arriving)),
                )
                Spacer(Modifier.height(MastheadBarGap))
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(PlakatLines.Bar)
                        .background(colors.ink),
                )
                Spacer(Modifier.height(MastheadFoot))
            }
            // The ordinary bar comes down from above in the fold's last 56, on its own paper. Until then it is not there at
            // all: above the clip it would still be read, and its actions would still take the keyboard's focus.
            if (arriving) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(RowHeight)
                        .offset {
                            IntOffset(
                                0,
                                (state.foldPx - state.rangePx).toInt().coerceAtLeast(-RowHeight.roundToPx()),
                            )
                        }.background(colors.paper),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(RowHeight)
                            .padding(horizontal = RowInset),
                        horizontalArrangement = Arrangement.spacedBy(RowInset),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        navigationIcon?.invoke()
                        BasicText(
                            collapsedTitle,
                            Modifier.weight(1f).semantics { heading() },
                            style =
                                type.displayL.style
                                    .copy(fontSize = COLLAPSED_TITLE_SP.sp, color = colors.ink)
                                    .cssLine(PlakatFaceMetrics.Display),
                            softWrap = false,
                        )
                        actions()
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val h = (PlakatLines.Rule.toPx() * state.fraction).toInt()
                        val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
                        layout(placeable.width, h) { placeable.place(0, 0) }
                    }.background(colors.ink),
            )
        }
    }
}
