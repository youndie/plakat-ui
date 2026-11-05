package io.github.youndie.plakat.components

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes

/**
 * The disc, designed here (D13), as no page draws a radio button: inside the ring of 24 and its rule of 3 there are
 * 18, and a gap of one rule all round leaves 12.
 */
private val DiscSize = 12.dp

/**
 * A radio button, designed here from the brief's row (D13), as the Checkbox is set: a ring of 24 dp in the 3 dp rule, in a
 * 48 dp touch zone, with a red disc in it when [selected]. The disc is there or not, with no motion (`lock`, 0 ms): a
 * button going out drops its disc at once and one coming in shows its own a frame later, so that two never stand
 * together. Under reduced motion both at once. Group them as Material's are, in a `Modifier.selectableGroup()`.
 * [onClick] null makes it a display only.
 */
@Composable
public fun PlakatRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatRadioButtonFrame(selected, onClick, modifier, enabled, interactionSource)
}

@Composable
internal fun PlakatRadioButtonFrame(
    selected: Boolean,
    onClick: (() -> Unit)?,
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

    // Set by the effect, so a frame after the composition that selected this button; the disc of the button going out
    // is off in that very composition, and the frame between is theirs.
    var settled by remember { mutableStateOf(selected) }
    LaunchedEffect(selected, reduce) {
        if (selected != settled) {
            if (selected) events.onEvent(PlakatEvent.Lock)
            settled = selected
        }
    }
    val disc = selected && (settled || reduce)

    val select =
        if (onClick != null) {
            Modifier.selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
        } else {
            Modifier
        }
    val ring = forceFocused || (focused && keyboard)
    Box(
        modifier
            .size(PlakatSizes.Touch)
            .then(select)
            .drawBehind { drawRadio(colors, enabled, disc, ring) },
    )
}

private fun DrawScope.drawRadio(
    colors: PlakatColors,
    enabled: Boolean,
    disc: Boolean,
    ring: Boolean,
) {
    val box = PlakatSizes.Checkbox.toPx()
    val line = (if (enabled) PlakatLines.Rule else PlakatLines.Hair).toPx()
    val rule = PlakatLines.Rule.toPx()
    if (disc) drawCircle(if (enabled) colors.red else colors.inkMuted, DiscSize.toPx() / 2)
    drawCircle(if (enabled) colors.ink else colors.inkMuted, (box - line) / 2, style = Stroke(line))
    // Keyboard focus: as the Checkbox's, `outline: 3px; outline-offset: -3px` on the touch zone.
    if (ring) {
        drawRect(
            colors.ink,
            Offset(rule / 2, rule / 2),
            Size(size.width - rule, size.height - rule),
            style = Stroke(rule),
        )
    }
}
