package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.plakatHatchBrush
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes

/** The error hatch inside the box: the Core page's `repeating-linear-gradient(45deg, red 0 2px, transparent 2px 5px)`. */
private val ErrorHatchPeriod = 5.dp
private const val ERROR_HATCH_INK = 2f / 5f

/**
 * A checkbox (`plakat/size/checkbox`, 24 dp, in a 48 dp touch zone): a square in a 3 dp frame whose
 * mark is a red wedge, not a tick. Checked, the wedge comes in from the top-right corner to its stop
 * in 120 ms hard-stop (`wedge-in`); unchecked, it leaves the same way in 60 ms linear (`wedge-out`).
 * Under reduced motion it is there or not at once. [error] hatches the box in red inside a red frame,
 * so the error is a form, not only a colour; the word beside it is the caller's.
 * [onCheckedChange] null makes it a display only, as Material's.
 */
@Composable
public fun PlakatCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatCheckboxFrame(checked, onCheckedChange, modifier, enabled, error, interactionSource)
}

/** How far the wedge is in: 0 out at the top-right, 1 at its stop. */
internal fun wedgeOffset(
    side: Float,
    progress: Float,
): Offset = Offset((1 - progress) * side, -(1 - progress) * side)

@Composable
internal fun PlakatCheckboxFrame(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    error: Boolean,
    interactionSource: MutableInteractionSource?,
    forceFocused: Boolean = false,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(source) {
        source.interactions.collect {
            when (it) {
                is FocusInteraction.Focus -> focused = true
                is FocusInteraction.Unfocus -> focused = false
            }
        }
    }

    val wedge = remember { Animatable(if (checked) 1f else 0f) }
    var seen by remember { mutableStateOf(checked) }
    LaunchedEffect(checked, reduce) {
        if (checked != seen) events.onEvent(if (checked) PlakatEvent.WedgeIn else PlakatEvent.WedgeOut)
        seen = checked
        val target = if (checked) 1f else 0f
        when {
            reduce -> wedge.snapTo(target)
            checked -> wedge.animateTo(target, tween(PlakatDurations.QUICK, easing = PlakatEasings.HardStop))
            else -> wedge.animateTo(target, tween(PlakatDurations.SNAP, easing = LinearEasing))
        }
    }

    val toggle =
        if (onCheckedChange != null) {
            Modifier.toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
        } else {
            Modifier
        }
    val ring = forceFocused || (focused && keyboard)
    val errorWord = PlakatTheme.strings.error
    Box(
        modifier
            .size(PlakatSizes.Touch)
            .semantics { if (error) error(errorWord) }
            .then(toggle)
            .drawBehind { drawCheckbox(colors, enabled, error, wedge.value, ring) },
    )
}

private fun DrawScope.drawCheckbox(
    colors: PlakatColors,
    enabled: Boolean,
    error: Boolean,
    wedge: Float,
    ring: Boolean,
) {
    val box = PlakatSizes.Checkbox.toPx()
    val origin = Offset((size.width - box) / 2, (size.height - box) / 2)
    val frame = (if (enabled) PlakatLines.Rule else PlakatLines.Hair).toPx()
    val rule = PlakatLines.Rule.toPx()
    val line =
        when {
            !enabled -> colors.inkMuted
            error -> colors.red
            else -> colors.ink
        }
    if (error && enabled) {
        val hatch =
            plakatHatchBrush(
                Size(box, box),
                colors.red,
                Color.Transparent,
                ErrorHatchPeriod.toPx(),
                inkShare = ERROR_HATCH_INK,
            )
        translate(origin.x, origin.y) { drawRect(hatch, size = Size(box, box)) }
    }
    // The wedge lives in the box inside a 3 dp inset and is clipped to it as it comes and goes.
    if (wedge > 0f) {
        val inner = box - 2 * rule
        val at = origin + Offset(rule, rule)
        clipRect(at.x, at.y, at.x + inner, at.y + inner) {
            val shift = wedgeOffset(inner, wedge)
            val path =
                Path().apply {
                    moveTo(at.x + shift.x, at.y + shift.y)
                    lineTo(at.x + inner + shift.x, at.y + shift.y)
                    lineTo(at.x + inner + shift.x, at.y + inner + shift.y)
                    close()
                }
            drawPath(path, if (enabled) colors.red else colors.inkMuted)
        }
    }
    drawRect(line, origin + Offset(frame / 2, frame / 2), Size(box - frame, box - frame), style = Stroke(frame))
    // Keyboard focus: the page's `outline: 3px; outline-offset: -3px` on the touch zone.
    if (ring) {
        drawRect(
            colors.ink,
            Offset(rule / 2, rule / 2),
            Size(size.width - rule, size.height - rule),
            style = Stroke(rule),
        )
    }
}
