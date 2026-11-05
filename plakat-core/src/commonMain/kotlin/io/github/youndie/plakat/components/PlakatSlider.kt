package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.round

/** The Principles page's live slider: the ticks at `top: 32px`, `height: 8px`, every third 12. */
private val TickTop = 32.dp
private val TickShort = 8.dp
private val TickTall = 12.dp
private const val TALL_EVERY = 3

/** The focus ring, «рамка 3 с отступом», about the carriage, designed here (D13), at the Switch's offset of 4. */
private val FocusOffset = 4.dp

/** A continuous slider's keyboard step, as a share of the range, designed here (D13). */
private const val KEY_STEP = .01f

/**
 * A slider, a rule with a carriage on it, from the Principles page. A 3 dp rail, a red 8 dp fill up to the value, a 20 × 32
 * carriage; with [steps], Material's number of stops between the ends, 3 × 8 ticks under the rail, every third 12, and the
 * carriage stands only on them. Moved, it runs in 120 ms hard-stop, with no overshoot and `notch` heard on arriving at a
 * new tick; at once under reduced motion. A continuous one has no ticks and follows the finger. Pressed, its carriage
 * inverts. Disabled, the rail is a muted hair and the carriage concrete, with no fill.
 *
 * As tall as its touch target, 48 dp, and as wide as it is given. The carriage is centred on the value, as the page's is, so
 * at the ends it stands 10 dp past the slider's box: leave that room, and do not clip it. The arrows move it a stop, or a
 * hundredth of the range, and Home and End to the ends.
 */
@Composable
public fun PlakatSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatSliderFrame(
        value,
        onValueChange,
        modifier,
        enabled,
        valueRange,
        steps,
        onValueChangeFinished,
        interactionSource,
    )
}

/** [value]'s place in [range], 0 to 1, on a stop if there are [steps]. */
private fun fractionOf(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
): Float {
    val span = range.endInclusive - range.start
    val f = if (span > 0f) ((value - range.start) / span).coerceIn(0f, 1f) else 0f
    return onStop(f, steps)
}

/** The nearest stop to [f], `Math.round` of the page's `… * (N - 1)`: a half goes up. */
private fun onStop(
    f: Float,
    steps: Int,
): Float {
    if (steps <= 0) return f
    val n = steps + 1
    return floor(f * n + .5f) / n
}

@Composable
internal fun PlakatSliderFrame(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
    interactionSource: MutableInteractionSource?,
    forcePressed: Boolean = false,
    forceFocused: Boolean = false,
) {
    require(steps >= 0) { "A slider has no negative steps: $steps" }
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(source) {
        source.interactions.collect {
            when (it) {
                is FocusInteraction.Focus -> focused = true
                is FocusInteraction.Unfocus -> focused = false
                is PressInteraction.Press -> pressed = true
                is PressInteraction.Release, is PressInteraction.Cancel -> pressed = false
            }
        }
    }

    val target = fractionOf(value, valueRange, steps)
    val carriage = remember { Animatable(target) }
    var seen by remember { mutableStateOf(target) }
    LaunchedEffect(target, reduce) {
        if (steps > 0 && target != seen) events.onEvent(PlakatEvent.Notch)
        seen = target
        if (reduce || (steps == 0 && dragging)) {
            carriage.snapTo(target)
        } else {
            carriage.animateTo(target, tween(PlakatDurations.QUICK, easing = PlakatEasings.HardStop))
        }
    }

    val change by rememberUpdatedState(onValueChange)
    val finished by rememberUpdatedState(onValueChangeFinished)
    val current by rememberUpdatedState(value)

    fun setFraction(f: Float) {
        val on = onStop(f.coerceIn(0f, 1f), steps)
        val next = valueRange.start + (valueRange.endInclusive - valueRange.start) * on
        if (next != current) change(next)
    }

    val input =
        if (enabled) {
            Modifier
                .pointerInput(source, steps, valueRange) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val press = PressInteraction.Press(down.position)
                        scope.launch { source.emit(press) }
                        // A mouse sets it as it lands, as the page does; a finger may be scrolling what is around it.
                        if (down.type != PointerType.Touch) setFraction(down.position.x / size.width)
                        val moved =
                            awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                        var cancelled = false
                        if (moved != null) {
                            dragging = true
                            setFraction(moved.position.x / size.width)
                            horizontalDrag(moved.id) {
                                it.consume()
                                setFraction(it.position.x / size.width)
                            }
                            dragging = false
                        } else {
                            val up = currentEvent.changes.firstOrNull { it.id == down.id }
                            if (up != null && !up.pressed && !up.isConsumed) {
                                setFraction(up.position.x / size.width)
                            } else {
                                cancelled = true
                            }
                        }
                        scope.launch {
                            source.emit(
                                if (cancelled) PressInteraction.Cancel(press) else PressInteraction.Release(press),
                            )
                        }
                        if (!cancelled) finished?.invoke()
                    }
                }.onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    val stop = if (steps > 0) 1f / (steps + 1) else KEY_STEP
                    val at = fractionOf(current, valueRange, steps)
                    val to =
                        when (event.key) {
                            Key.DirectionRight, Key.DirectionUp -> at + stop
                            Key.DirectionLeft, Key.DirectionDown -> at - stop
                            Key.MoveHome -> 0f
                            Key.MoveEnd -> 1f
                            else -> return@onKeyEvent false
                        }
                    setFraction(to)
                    finished?.invoke()
                    true
                }.focusable(interactionSource = source)
        } else {
            Modifier
        }
    val ring = forceFocused || (focused && keyboard)
    val inverted = forcePressed || pressed
    Box(
        modifier
            .fillMaxWidth()
            .height(PlakatSizes.Touch)
            .semantics {
                progressBarRangeInfo =
                    ProgressBarRangeInfo(value.coerceIn(valueRange), valueRange, steps)
                if (enabled) {
                    setProgress { asked ->
                        val span = valueRange.endInclusive - valueRange.start
                        if (span <= 0f) return@setProgress false
                        setFraction((asked - valueRange.start) / span)
                        finished?.invoke()
                        true
                    }
                } else {
                    disabled()
                }
            }.then(input)
            .drawBehind { drawSlider(colors, enabled, steps, carriage.value, inverted, ring) },
    )
}

private fun DrawScope.drawSlider(
    colors: PlakatColors,
    enabled: Boolean,
    steps: Int,
    at: Float,
    inverted: Boolean,
    ring: Boolean,
) {
    val middle = size.height / 2
    val rule = PlakatLines.Rule.toPx()
    val hair = PlakatLines.Hair.toPx()
    // The page's positions are percentages of a box in whole pixels, which the browser snaps.
    val x = round(size.width * at)
    if (enabled) {
        drawRect(colors.ink, Offset(0f, floor(middle - rule / 2)), Size(size.width, rule))
    } else {
        drawRect(colors.inkMuted, Offset(0f, middle - hair), Size(size.width, hair))
    }
    if (steps > 0) {
        // The tick's `width: 3px; margin-left: -1px`; disabled, a hair on the same place, designed here.
        val width = if (enabled) rule else hair
        val color = if (enabled) colors.ink else colors.inkMuted
        for (k in 0..steps + 1) {
            val tall = k % TALL_EVERY == 0
            drawRect(
                color,
                Offset(round(size.width * k / (steps + 1)) - if (enabled) 1.dp.toPx() else 0f, TickTop.toPx()),
                Size(width, (if (tall) TickTall else TickShort).toPx()),
            )
        }
    }
    val box = Size(PlakatSizes.SwitchCarriageWidth.toPx(), PlakatSizes.SwitchCarriageHeight.toPx())
    val corner = Offset(x - box.width / 2, middle - box.height / 2)
    if (enabled) {
        val bar = PlakatLines.Bar.toPx()
        if (x > 0f) drawRect(colors.red, Offset(0f, middle - bar / 2), Size(x, bar))
        if (inverted) {
            drawRect(colors.paper, corner, box)
            drawRect(
                colors.ink,
                corner + Offset(rule / 2, rule / 2),
                Size(box.width - rule, box.height - rule),
                style = Stroke(rule),
            )
        } else {
            drawRect(colors.ink, corner, box)
        }
    } else {
        drawRect(colors.concrete, corner, box)
        drawRect(
            colors.inkMuted,
            corner + Offset(hair / 2, hair / 2),
            Size(box.width - hair, box.height - hair),
            style = Stroke(hair),
        )
    }
    if (ring) {
        val o = FocusOffset.toPx() + rule / 2
        drawRect(
            colors.ink,
            corner - Offset(o, o),
            Size(box.width + 2 * o, box.height + 2 * o),
            style = Stroke(rule),
        )
    }
}
