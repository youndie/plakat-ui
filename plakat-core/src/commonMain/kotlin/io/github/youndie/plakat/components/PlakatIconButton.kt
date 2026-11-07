package io.github.youndie.plakat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatStencil
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes

/** How far a pressed icon moves: the spec row's `иконка +1 dp`, right and down as the FAB's cell shows. */
private val PressShift = 1.dp

/**
 * An icon button: a 48 dp square with a stencil [icon]. Pressed (`stamp`) the square inverts and the
 * icon moves 1 dp, both returning in 60 ms hard-stop (`release`), at once under reduced motion;
 * [selected] prints it red with the icon on-red; disabled the icon is muted. Focused from the
 * keyboard, a 3 dp ink ring runs just inside its edge (`outline-offset: -3px`).
 */
@Composable
public fun PlakatIconButton(
    icon: PlakatStencil,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatIconButtonFrame(icon, contentDescription, onClick, modifier, enabled, selected, interactionSource)
}

@Composable
internal fun PlakatIconButtonFrame(
    icon: PlakatStencil,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    selected: Boolean,
    interactionSource: MutableInteractionSource?,
    look: ButtonLook? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val state = rememberPlakatPress(source)
    val forced = look == ButtonLook.Pressed
    val rest = if (selected && enabled) colors.red else colors.ink.copy(alpha = 0f)
    val held = state.held || forced
    val tint =
        when {
            !enabled -> colors.inkMuted
            forced || state.inked -> colors.paper
            selected -> colors.onRed
            else -> colors.ink
        }
    val ring = look == ButtonLook.Focused || state.focusShown()
    val hovered = enabled && (look == ButtonLook.Hovered || state.hovered) && !held
    Box(
        modifier
            .size(PlakatSizes.Touch)
            .semantics { this.selected = selected }
            .drawBehind {
                drawRect(srgbLerp(rest, colors.ink, if (forced) 1f else state.press.value))
                // Designed here (B-39): an empty square fills with concrete, a selected one gets the ink frame.
                if (hovered && !selected) drawRect(colors.concrete)
                if (hovered && selected) {
                    val w = PlakatLines.Rule.toPx()
                    drawRect(colors.ink, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), style = Stroke(w))
                }
                if (ring) {
                    val w = PlakatLines.Rule.toPx()
                    drawRect(colors.ink, Offset(w / 2, w / 2), Size(size.width - w, size.height - w), style = Stroke(w))
                }
            }.clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PlakatIcon(
            icon,
            contentDescription,
            // The page's pressed cell is drawn without the spec row's shift, so the forced look has none.
            Modifier.graphicsLayer {
                translationX = PressShift.toPx() * state.press.value
                translationY = translationX
            },
            tint,
        )
    }
}
