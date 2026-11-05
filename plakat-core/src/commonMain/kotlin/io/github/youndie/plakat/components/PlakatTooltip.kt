package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** The leader's length, designed here (D13): on the grid, 3 × 8. */
private val LeaderLength = 24.dp

/** The plate: `padding` a step of 8 all round, a caption's measure of 240. Designed here (D13). */
private val PlateMostWidth = 240.dp

/** A pointer rests this long before the caption shows, designed here (D13). */
private const val HOVER_MS = 400L

/** The axis of a [PlakatTooltip]'s leader, all there are: 90°, 0° and the kit's −17°. */
public enum class PlakatTooltipAxis {
    /** 90°: up from the anchor's top, or down from its foot where there is no room above. */
    Upright,

    /** 0°: right from the anchor's side, or left where there is no room to the right. */
    Level,

    /** −17°: up and right from the anchor's side, or on down the same line to the left. One axis, so never its mirror. */
    Tilted,
}

/** From which side a caption is wiped in: the side its leader starts. */
internal enum class TooltipFrom { Left, Right, Top, Bottom }

/** Where a caption goes: its box's corner in the window, its plate in that box, its leader, and the wipe's side. */
internal class TooltipPlacement(
    val box: IntRect,
    val plate: IntOffset,
    val lineFrom: Offset,
    val lineTo: Offset,
    val from: TooltipFrom,
)

/**
 * The placement, all in pixels. The plate stands at the leader's far end: past it across an upright leader, reaching right
 * unless the window stops it; centred on a level or tilted one's end, beyond it.
 */
internal fun tooltipPlacement(
    anchor: IntRect,
    window: IntSize,
    plate: IntSize,
    axis: PlakatTooltipAxis,
    leader: Float,
): TooltipPlacement {
    val centreX = floor((anchor.left + anchor.right) / 2f)
    val centreY = floor((anchor.top + anchor.bottom) / 2f)
    val start: Offset
    val end: Offset
    val plateLeft: Float
    val plateTop: Float
    val from: TooltipFrom
    when (axis) {
        PlakatTooltipAxis.Upright -> {
            val up = anchor.top - leader - plate.height >= 0
            start = Offset(centreX, (if (up) anchor.top else anchor.bottom).toFloat())
            end = Offset(centreX, if (up) anchor.top - leader else anchor.bottom + leader)
            plateTop = if (up) end.y - plate.height else end.y
            plateLeft = if (centreX + plate.width <= window.width) centreX else centreX - plate.width + 1
            from = if (up) TooltipFrom.Bottom else TooltipFrom.Top
        }

        PlakatTooltipAxis.Level, PlakatTooltipAxis.Tilted -> {
            val tilted = axis == PlakatTooltipAxis.Tilted
            val rad = if (tilted) -PlakatShape.AXIS * PI.toFloat() / 180f else 0f
            val dx = leader * cos(rad)
            val dy = leader * sin(rad)
            val right = anchor.right + dx + plate.width <= window.width
            start = Offset((if (right) anchor.right else anchor.left).toFloat(), centreY)
            end = if (right) Offset(start.x + dx, start.y - dy) else Offset(start.x - dx, start.y + dy)
            plateLeft = if (right) end.x else end.x - plate.width
            plateTop = end.y - plate.height / 2f
            from = if (right) TooltipFrom.Left else TooltipFrom.Right
        }
    }
    val left = min(min(start.x, end.x), plateLeft).roundToInt()
    val top = min(min(start.y, end.y), plateTop).roundToInt()
    val right = max(max(start.x, end.x), plateLeft + plate.width).roundToInt()
    val bottom = max(max(start.y, end.y), plateTop + plate.height).roundToInt()
    val corner = Offset(left.toFloat(), top.toFloat())
    return TooltipPlacement(
        IntRect(left, top, right, bottom),
        IntOffset((plateLeft - left).roundToInt(), (plateTop - top).roundToInt()),
        start - corner,
        end - corner,
        from,
    )
}

/** What the anchor knows and the caption's position provider reads. */
@Stable
internal class TooltipHolder(
    var anchor: IntRect = IntRect.Zero,
    var placement: TooltipPlacement? = null,
)

/** Puts the caption by the anchor's own bounds as this provider is told them, whatever origin they have. */
private class TooltipPosition(
    val holder: TooltipHolder,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val box = holder.placement?.box ?: return IntOffset.Zero
        return IntOffset(
            anchorBounds.left + box.left - holder.anchor.left,
            anchorBounds.top + box.top - holder.anchor.top,
        )
    }
}

/**
 * A tooltip, from the brief's row and designed here (D13): [content] is its anchor, and [text] a concrete caption plate at the
 * far end of a hair leader of 24 dp, straight, as on a drawing, along [axis].
 *
 * It shows while a pointer rests on the anchor, after 400 ms, until the pointer leaves or presses; while a finger holds it; and
 * while it has the keyboard's focus. It appears by a wipe along its leader, from the anchor's end, 120 ms linear (`wipe`), and
 * goes back the same way; at once under reduced motion. It takes no input and no focus. A screen reader has [text] as the
 * anchor's description, without its showing.
 */
@Composable
public fun PlakatTooltip(
    text: String,
    modifier: Modifier = Modifier,
    axis: PlakatTooltipAxis = PlakatTooltipAxis.Upright,
    content: @Composable () -> Unit,
) {
    val holder = remember { TooltipHolder() }
    val scope = rememberCoroutineScope()
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    var rests by remember { mutableStateOf(false) }
    var held by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    // A press focuses what it lands on, which is not the keyboard's focus whatever the input mode says.
    var pressing by remember { mutableStateOf(false) }
    val visible = rests || held || (focused && keyboard)
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val open = remember { Animatable(0f) }
    var seen by remember { mutableStateOf(false) }
    LaunchedEffect(visible, reduce) {
        if (visible != seen) events.onEvent(PlakatEvent.Wipe)
        seen = visible
        val to = if (visible) 1f else 0f
        if (reduce) {
            open.snapTo(to)
        } else {
            open.animateTo(to, tween(PlakatDurations.QUICK, easing = PlakatEasings.Linear))
        }
    }
    Box(
        modifier
            .semantics { contentDescription = text }
            .onGloballyPositioned { c ->
                val b = c.boundsInWindow()
                holder.anchor =
                    IntRect(b.left.roundToInt(), b.top.roundToInt(), b.right.roundToInt(), b.bottom.roundToInt())
            }.onFocusChanged { focused = it.hasFocus && !pressing }
            .pointerInput(scope) {
                val slop = viewConfiguration.touchSlop
                val wait = viewConfiguration.longPressTimeoutMillis
                var job: Job? = null
                var at = Offset.Zero
                // On the initial pass, and consuming nothing: what is under it presses as if it were not here.
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.first()
                        val mouse = change.type == PointerType.Mouse
                        when (event.type) {
                            PointerEventType.Enter -> {
                                if (mouse) {
                                    job?.cancel()
                                    job =
                                        scope.launch {
                                            delay(HOVER_MS)
                                            rests = true
                                        }
                                }
                            }

                            PointerEventType.Exit -> {
                                job?.cancel()
                                rests = false
                            }

                            PointerEventType.Press -> {
                                pressing = true
                                job?.cancel()
                                rests = false
                                if (!mouse) {
                                    at = change.position
                                    job =
                                        scope.launch {
                                            delay(wait)
                                            held = true
                                        }
                                }
                            }

                            PointerEventType.Move -> {
                                if (!mouse && (change.position - at).getDistance() > slop && !held) job?.cancel()
                            }

                            PointerEventType.Release -> {
                                pressing = false
                                job?.cancel()
                                held = false
                            }
                        }
                    }
                }
            },
    ) {
        content()
        // Inside the anchor's box, as a popup stands by its parent.
        if (visible || open.value != 0f) {
            Popup(
                remember(holder) { TooltipPosition(holder) },
                properties =
                    PopupProperties(
                        focusable = false,
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false,
                    ),
            ) {
                PlakatTooltipCaption(text, axis, holder, LocalWindowInfo.current.containerSize) { open.value }
            }
        }
    }
}

/** The caption, which measures its plate and places itself from what [holder] knows of the anchor. */
@Composable
internal fun PlakatTooltipCaption(
    text: String,
    axis: PlakatTooltipAxis,
    holder: TooltipHolder,
    window: IntSize,
    progress: () -> Float,
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    Layout(
        content = {
            Box(
                Modifier
                    .widthIn(max = PlateMostWidth)
                    .background(colors.concrete)
                    .padding(PlakatGrid.Step),
            ) {
                BasicText(
                    text,
                    style =
                        type.bodyS.style
                            .copy(color = colors.ink)
                            .cssLine(PlakatFaceMetrics.Body),
                )
            }
        },
        modifier =
            Modifier.drawWithContent {
                val placed = holder.placement ?: return@drawWithContent
                val p = progress().coerceIn(0f, 1f)
                val shown =
                    when (placed.from) {
                        TooltipFrom.Left -> Rect(0f, 0f, size.width * p, size.height)
                        TooltipFrom.Right -> Rect(size.width * (1 - p), 0f, size.width, size.height)
                        TooltipFrom.Top -> Rect(0f, 0f, size.width, size.height * p)
                        TooltipFrom.Bottom -> Rect(0f, size.height * (1 - p), size.width, size.height)
                    }
                clipRect(shown.left, shown.top, shown.right, shown.bottom) {
                    this@drawWithContent.drawContent()
                    val hair = PlakatLines.Hair.toPx()
                    val half = hair / 2
                    drawLine(
                        colors.ink,
                        placed.lineFrom + Offset(half, half),
                        placed.lineTo + Offset(half, half),
                        hair,
                    )
                }
            },
    ) { measurables, _ ->
        val plate = measurables[0].measure(Constraints())
        val placed =
            tooltipPlacement(
                holder.anchor,
                window,
                IntSize(plate.width, plate.height),
                axis,
                LeaderLength.toPx(),
            )
        holder.placement = placed
        layout(placed.box.width, placed.box.height) { plate.place(placed.plate) }
    }
}
