package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes

/** The focus ring's distance from the track: the Core page's `outline: 3px; outline-offset: 4px`. */
private val FocusOffset = 4.dp

/**
 * A switch: a 3 dp rail as long as the track (`plakat/size/switch`, 56 dp), a 20 × 32 carriage on
 * it, and when on a red 8 dp segment filling the rail behind the carriage. Toggled, the carriage runs
 * the rail to its stop in 120 ms hard-stop and the segment follows it (`lever`); under reduced motion
 * it is at once at the other end. Disabled, the rail is a muted hair and the carriage concrete.
 * The touch target is the 56 × 48 track. [onCheckedChange] null makes it a display only.
 */
@Composable
public fun PlakatSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatSwitchFrame(checked, onCheckedChange, modifier, enabled, interactionSource)
}

@Composable
internal fun PlakatSwitchFrame(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
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

    // The carriage's way along the rail, 0 off to 1 on; the segment's length follows it.
    val lever = remember { Animatable(if (checked) 1f else 0f) }
    var seen by remember { mutableStateOf(checked) }
    LaunchedEffect(checked, reduce) {
        if (checked != seen) events.onEvent(PlakatEvent.Lever)
        seen = checked
        val target = if (checked) 1f else 0f
        if (reduce) {
            lever.snapTo(
                target,
            )
        } else {
            lever.animateTo(target, tween(PlakatDurations.QUICK, easing = PlakatEasings.HardStop))
        }
    }

    val toggle =
        if (onCheckedChange != null) {
            Modifier.toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
        } else {
            Modifier
        }
    val ring = forceFocused || (focused && keyboard)
    Box(
        modifier
            .size(PlakatSizes.SwitchWidth, PlakatSizes.Touch)
            .then(toggle)
            .drawBehind { drawSwitch(colors, enabled, lever.value, ring) },
    )
}

private fun DrawScope.drawSwitch(
    colors: PlakatColors,
    enabled: Boolean,
    lever: Float,
    ring: Boolean,
) {
    val carriage = Size(PlakatSizes.SwitchCarriageWidth.toPx(), PlakatSizes.SwitchCarriageHeight.toPx())
    val travel = size.width - carriage.width
    val x = travel * lever
    val middle = size.height / 2
    if (enabled) {
        val rule = PlakatLines.Rule.toPx()
        // The rail half a pixel above the track's middle (the page's `top: 22px; height: 3px` in 48, not
        // 22.5), on whole pixels; the segment over it.
        drawRect(colors.ink, Offset(0f, kotlin.math.floor(middle - rule / 2)), Size(size.width, rule))
        val bar = PlakatLines.Bar.toPx()
        if (x > 0f) drawRect(colors.red, Offset(0f, middle - bar / 2), Size(x, bar))
        drawRect(colors.ink, Offset(x, middle - carriage.height / 2), carriage)
    } else {
        val hair = PlakatLines.Hair.toPx()
        drawRect(colors.inkMuted, Offset(0f, middle - hair), Size(size.width, hair))
        val at = Offset(x, middle - carriage.height / 2)
        drawRect(colors.concrete, at, carriage)
        drawRect(
            colors.inkMuted,
            at + Offset(hair / 2, hair / 2),
            Size(carriage.width - hair, carriage.height - hair),
            style = Stroke(hair),
        )
    }
    if (ring) {
        val w = PlakatLines.Rule.toPx()
        val o = FocusOffset.toPx() + w / 2
        drawRect(colors.ink, Offset(-o, -o), Size(size.width + 2 * o, size.height + 2 * o), style = Stroke(w))
    }
}
