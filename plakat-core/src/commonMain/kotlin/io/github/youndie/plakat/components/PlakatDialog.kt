package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.youndie.plakat.print.PlakatCutShape
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.roundToInt

/** The sheet: the MVP page's `left: 24px; right: 24px; top: 96px`, on paper. */
private val SheetInset = 24.dp
private val SheetTop = 96.dp

/** How close a tall sheet comes to the foot of the window, designed here (D13) as its sides stand: B-62. */
private val SheetFoot = SheetInset

/** The padding of its three parts: `16px 20px`, `16px 20px` and `0 20px 20px`. */
private val PartX = 20.dp
private val PartY = 16.dp

/**
 * The title's plate: `font-size: 20px; line-height: 1.1`, 22 high. That is shorter than the face's 25, which
 * [cssLine] cannot do, so it is a `normal` line lifted 2 into its padding: the baseline stands where Blink's does.
 */
private const val TITLE_SP = 20
private val TitleLift = 2.dp
private val TitleLine = 22.dp
private val FaceLine = 25.dp

/** The body: `font-size: 16px; line-height: 1.45`. */
private const val BODY_LINE = 1.45f

/** The actions' words: `font-size: 13px` with no tracking, a 3 dp rule 3 under, in a target of 48. */
private const val ACTION_SP = 13
private val ActionUnderlineGap = 3.dp

/** The plain action's `padding: 0 12px`, and the danger word's plate `padding: 6px 8px`. */
private val ActionInset = 12.dp
private val DangerActionPlate = 8.dp

/** The sheet rides from `translateY(-320px)`, whatever its height. */
private val RideFrom = 320.dp

/** The screen's edge leans back this far over its height: `polygon(0 0, x 0, x − 120px 100%, 0 100%)`. */
private val ScreenSlant = 120.dp

/** Open and close under reduced motion: the spec row's `fade 100`. */
private const val FADE_MS = 100

/** The screen's pitch and its dots: `radial-gradient(circle, ink 0 1.6px, transparent 1.8px)`, `background-size: 4px 4px`. */
private val ScreenPitch = 4.dp
private const val DOT_SOLID = 1.6f
private const val DOT_EDGE = 1.8f

/** The screen's ink in the dark theme, darker than its paper: the page's `scrim: #0E0D0B`, no token of the design's. */
private val DarkScreen = Color(0xFF0E0D0B)

/**
 * A dialog, the announcement: place it last in a screen-filling box. A red title plate with the cut corner,
 * [text] under it, and [actions] at the end, usually [PlakatDialogAction]s; the background is closed by a
 * screen of ink dots, a danger's distinction being the hatch and word of its action.
 *
 * With [visible] true, whether from the start or set later, it opens (`announce`): the screen closes the
 * background by a diagonal wipe from the left and the sheet rides down from above, on one clock of 320 ms,
 * linear; set false it goes back the same way, and is gone from the composition after. Under reduced motion
 * both fade, 100 ms. A click on the screen, Escape or the system back gesture calls [onDismissRequest]; the sheet takes
 * its own clicks, and focus stays in the dialog while it is open. On closing, focus goes to [returnFocusTo], a requester on
 * whatever opened it, or with none is left to the window.
 *
 * [content], when given, stands between the text and the actions, padded at its sides as the text is (B-79): a choice
 * the dialog asks for, such as groups of radio rows. It scrolls together with the text where the two are taller than the
 * sheet leaves them, the title and the actions staying in place. Focus then enters at its first control, and a control
 * the keyboard reaches below the fold is scrolled into view.
 */
@Composable
public fun PlakatDialog(
    visible: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    returnFocusTo: FocusRequester? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    PlakatDialogFrame(
        visible,
        title,
        onDismissRequest,
        modifier,
        text,
        actions,
        returnFocusTo = returnFocusTo,
        content = content,
    )
}

@Composable
internal fun PlakatDialogFrame(
    visible: Boolean,
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    text: String?,
    actions: @Composable RowScope.() -> Unit,
    playIn: Boolean = true,
    returnFocusTo: FocusRequester? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    ReturnFocus(visible, returnFocusTo)
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val dismiss by rememberUpdatedState(onDismissRequest)
    val open = remember { Animatable(if (visible && !playIn) 1f else 0f) }
    // A dialog that starts visible opens: its first `announce` is the change from what stood before it.
    var seen by remember { mutableStateOf(visible && !playIn) }
    LaunchedEffect(visible, reduce) {
        if (visible != seen) events.onEvent(PlakatEvent.Announce)
        seen = visible
        val ms = if (reduce) FADE_MS else PlakatDurations.SCENE
        open.animateTo(if (visible) 1f else 0f, tween(ms, easing = PlakatEasings.Linear))
    }
    // Closed, nothing of it is composed or takes input.
    val gone by remember { derivedStateOf { open.value == 0f } }
    if (!visible && gone) return
    val focus = remember { FocusRequester() }
    LaunchedEffect(visible) { if (visible) focus.requestFocus() }
    NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = visible) { dismiss() }
    val type = PlakatTheme.typography
    Box(
        modifier
            .fillMaxSize()
            // Focus stays in here while it is open, and what is under the screen cannot be reached by the keys.
            .focusProperties { onExit = { if (visible) cancelFocusChange() } }
            .focusGroup()
            .focusRequester(focus)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                    dismiss()
                    true
                } else {
                    false
                }
            }.focusable(),
    ) {
        PlakatDotScreen(colors, reduce, { open.value }) { if (visible) dismiss() }
        Column(
            Modifier
                .padding(start = SheetInset, top = SheetTop, end = SheetInset, bottom = SheetFoot)
                .fillMaxWidth()
                .graphicsLayer {
                    if (reduce) {
                        alpha = open.value
                    } else {
                        translationY = (open.value - 1f) * RideFrom.toPx()
                    }
                }.pointerInput(Unit) {}
                // The sheet is cut as its title plate is, so the screen shows through the corner: the design leaves
                // paper there, under a plate that only looks cut (the owner's remark of 2026-11-07, research D13).
                .background(colors.paper, PlakatCutShape())
                .semantics {
                    dialog()
                    paneTitle = title
                    dismiss {
                        dismiss()
                        true
                    }
                },
        ) {
            Box(Modifier.fillMaxWidth().background(colors.red, PlakatCutShape())) {
                BasicText(
                    title,
                    Modifier
                        .padding(
                            start = PartX,
                            top = PartY - TitleLift,
                            end = PartX,
                            bottom = PartY - (FaceLine - TitleLine - TitleLift),
                        ).semantics { heading() },
                    style =
                        type.displayL.style
                            .copy(fontSize = TITLE_SP.sp, color = colors.onRed)
                            .cssLine(PlakatFaceMetrics.Display),
                )
            }
            if (text != null || content != null) DialogBody(text, content, Modifier.weight(1f, fill = false))
            // Actions too long for one line go onto a next, each with its own width, and neither leaves the sheet.
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .padding(start = PartX, end = PartX, bottom = PartX),
                horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Step, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(PlakatGrid.Step),
                content = actions,
            )
        }
    }
}

/**
 * Gives focus to [target] when an overlay that held it closes. Compose offers no way to read where focus stood before the
 * overlay took it, so the opener has to be named, or focus goes nowhere. A [target] that has left the composition with the
 * overlay is passed over, as `requestFocus` does nothing without a node.
 */
@Composable
internal fun ReturnFocus(
    visible: Boolean,
    target: FocusRequester?,
) {
    var opened by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) {
            opened = true
        } else if (opened) {
            opened = false
            target?.requestFocus()
        }
    }
}

/**
 * The text and the content between the title and the actions, which keep their height: longer than what they leave, the
 * two scroll by wheel, finger and keys. No bar and no fade, which the design draws neither of, and no stretch, as it has no
 * overshoot. It clips only when it overflows, and so is not a `verticalScroll`: a clip moves the edges of the letters off
 * the reference's (B-62), and a text that fits needs none.
 *
 * Text alone that overflows is a stop for the keyboard, so the arrows can move it. With [content], its controls are the
 * stops (B-79), and the one the keyboard reaches is brought inside the visible part.
 */
@Composable
private fun DialogBody(
    text: String?,
    content: (@Composable ColumnScope.() -> Unit)?,
    modifier: Modifier,
) {
    val colors = PlakatTheme.colors
    val style =
        PlakatTheme.typography.bodyM.style
            .copy(fontWeight = FontWeight.Normal)
    var offset by remember { mutableFloatStateOf(0f) }
    var most by remember { mutableIntStateOf(0) }
    val scroll =
        rememberScrollableState { delta ->
            val to = (offset - delta).coerceIn(0f, most.toFloat())
            val used = offset - to
            offset = to
            used
        }
    val scope = rememberCoroutineScope()
    val source = remember { MutableInteractionSource() }
    val line = with(LocalDensity.current) { style.fontSize.toPx() * BODY_LINE }
    var high by remember { mutableIntStateOf(0) }
    // Under the text, the content starts where the text's own padding ends; alone, it is padded as the text would be.
    val contentTop = if (text == null) PartY else 0.dp
    Box(
        modifier
            .plakatFocusFrame(source, colors.ink)
            .onKeyEvent {
                val by =
                    when (it.key) {
                        Key.DirectionDown -> -line
                        Key.DirectionUp -> line
                        else -> return@onKeyEvent false
                    }
                if (it.type == KeyEventType.KeyDown) scope.launch { scroll.scrollBy(by) }
                true
            }.scrollable(scroll, Orientation.Vertical, enabled = most > 0, overscrollEffect = null)
            .semantics { verticalScrollAxisRange = ScrollAxisRange({ offset }, { most.toFloat() }) }
            .drawWithContent {
                if (most > 0) clipRect { this@drawWithContent.drawContent() } else drawContent()
            }.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(maxHeight = Constraints.Infinity))
                high = placeable.height.coerceAtMost(constraints.maxHeight)
                most = placeable.height - high
                offset = offset.coerceIn(0f, most.toFloat())
                layout(placeable.width, high) { placeable.place(0, -offset.roundToInt()) }
            }.focusable(most > 0 && content == null, source),
    ) {
        Column(
            Modifier.then(
                BringIntoView { top, bottom ->
                    // The control the keyboard reached, kept inside the visible part: `scrollable` brings nothing into view.
                    offset =
                        when {
                            top < offset -> top
                            bottom > offset + high -> bottom - high
                            else -> offset
                        }.coerceIn(0f, most.toFloat())
                },
            ),
        ) {
            if (text != null) {
                CssParagraph(text, style, BODY_LINE, colors.ink, Modifier.padding(horizontal = PartX, vertical = PartY))
            }
            if (content != null) {
                Column(
                    Modifier.padding(
                        start = PartX,
                        end = PartX,
                        top = contentTop,
                        bottom = PartY,
                    ),
                    content = content,
                )
            }
        }
    }
}

/**
 * Answers a child's request to be seen (a control taking focus asks for it) with the top and bottom of what it asks to
 * show, in the coordinates of the node this modifier stands on.
 */
private data class BringIntoView(
    val show: (top: Float, bottom: Float) -> Unit,
) : ModifierNodeElement<BringIntoViewNode>() {
    override fun create() = BringIntoViewNode(show)

    override fun update(node: BringIntoViewNode) {
        node.show = show
    }
}

private class BringIntoViewNode(
    var show: (top: Float, bottom: Float) -> Unit,
) : Modifier.Node(),
    BringIntoViewModifierNode {
    override suspend fun bringIntoView(
        childCoordinates: LayoutCoordinates,
        boundsProvider: () -> Rect?,
    ) {
        val self = requireLayoutCoordinates()
        if (!childCoordinates.isAttached) return
        val rect = boundsProvider() ?: Rect(Offset.Zero, childCoordinates.size.toSize())
        val top = self.localPositionOf(childCoordinates, rect.topLeft).y
        show(top, top + rect.height)
    }
}

/** The screen over the background: a click on it asks to dismiss, and nothing under it is reached. */
@Composable
internal fun BoxScope.PlakatDotScreen(
    colors: PlakatColors,
    reduce: Boolean,
    open: () -> Float,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val ink = if (colors.ink.luminance() < colors.paper.luminance()) colors.ink else DarkScreen
    val tile = remember(ink, density) { screenTile(ink, density) }
    val brush = remember(tile) { ShaderBrush(ImageShader(tile, TileMode.Repeated, TileMode.Repeated)) }
    Box(
        Modifier
            .matchParentSize()
            .clearAndSetSemantics {}
            .then(
                if (reduce) {
                    Modifier.graphicsLayer { alpha = open() }
                } else {
                    Modifier.plakatWipe(PlakatWipe.Diagonal(ScreenSlant), open)
                },
            )
            // A target for the pointer and no stop for the keyboard: what cannot be seen as a control takes no focus.
            .focusProperties { canFocus = false }
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .drawBehind { drawRect(brush, Offset.Zero, Size(size.width, size.height)) },
    )
}

/**
 * One tile of the screen: a dot of solid ink to 1.6 dp fading out at 1.8, evaluated at each pixel's centre as
 * Chrome evaluates a gradient, which at scale 1 is 12 solid pixels in the tile of 16 and bare corners.
 */
private fun screenTile(
    ink: Color,
    density: Density,
): ImageBitmap {
    val px = ceil(ScreenPitch.value * density.density).toInt()
    val image = ImageBitmap(px, px)
    val canvas = Canvas(image)
    val paint = Paint().apply { isAntiAlias = false }
    val centre = px / 2f
    for (y in 0 until px) {
        for (x in 0 until px) {
            val at = hypot(x + .5f - centre, y + .5f - centre) / density.density
            val cover = ((DOT_EDGE - at) / (DOT_EDGE - DOT_SOLID)).coerceIn(0f, 1f)
            if (cover > 0f) {
                paint.color = ink.copy(alpha = cover)
                canvas.drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, paint)
            }
        }
    }
    return image
}

/**
 * An action of a [PlakatDialog]: a word over a 3 dp rule in a target of 48, or with [danger] on its hatch, which
 * tells it from a plain one by form and word, not colour. It presses as the kit's buttons do.
 */
@Composable
public fun PlakatDialogAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    enabled: Boolean = true,
) {
    PlakatButtonFrame(
        if (danger) PlakatButtonKind.Danger else PlakatButtonKind.Text,
        text,
        onClick,
        modifier,
        enabled,
        loading = false,
        interactionSource = null,
        metrics =
            ButtonMetrics(
                labelSize = ACTION_SP.sp,
                tracked = false,
                dangerPlate = DangerActionPlate,
                underlineGap = ActionUnderlineGap,
                inset = ActionInset,
                wordWide = true,
            ),
    )
}
