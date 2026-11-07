package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatStencil
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatShape
import kotlin.math.PI
import kotlin.math.tan

/** The circle: the MVP page's `width: 56px; height: 56px; border-radius: 50%`. */
private val FabSize = 56.dp

/** How far a pressed icon moves: the page's pressed cell, `translate(1px, 1px)`. */
private val PressShift = 1.dp

/** The focus ring's distance from the circle, as the Button's: the page draws no focused FAB. */
private val FocusOffset = PlakatLines.Rule

/**
 * A floating action button: the red circle, the only round shape on the screen, 56 dp, with a stencil
 * [icon] in on-red. Pressed (`stamp`) it turns ink with the icon in paper and 1 dp over, returning in
 * 60 ms hard-stop; disabled it is concrete with a muted icon. Focused from the keyboard, a 3 dp ink
 * ring stands 3 dp off it.
 *
 * It rides in as it enters the composition (`rail`): along the −17° axis from beyond the right edge
 * of the window, in 200 ms hard-stop; under reduced motion it is there at once. [rideIn] false shows
 * it in place, for a FAB that comes back with its screen.
 */
@Composable
public fun PlakatFab(
    icon: PlakatStencil,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rideIn: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatFabFrame(icon, contentDescription, onClick, modifier, enabled, rideIn, interactionSource)
}

@Composable
internal fun PlakatFabFrame(
    icon: PlakatStencil,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    rideIn: Boolean,
    interactionSource: MutableInteractionSource?,
    look: ButtonLook? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val state = rememberPlakatPress(source)
    val pressed = if (look == ButtonLook.Pressed) 1f else state.press.value

    // Its way along the rail, 0 beyond the edge to 1 in place.
    val ride = remember { Animatable(if (rideIn) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!rideIn) return@LaunchedEffect
        events.onEvent(PlakatEvent.Rail)
        if (reduce) {
            ride.snapTo(1f)
        } else {
            ride.animateTo(1f, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop))
        }
    }
    var place by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val ring = look == ButtonLook.Focused || state.focusShown()
    val tint =
        when {
            !enabled -> colors.inkMuted
            look == ButtonLook.Pressed || state.inked -> colors.paper
            else -> colors.onRed
        }
    Box(
        modifier
            .onPlaced { place = it }
            .graphicsLayer {
                // From where its left side is just past the window's right edge, on the axis: rising to the right.
                val at = place
                val beyond =
                    if (at != null &&
                        at.isAttached
                    ) {
                        at.findRootCoordinates().size.width - at.positionInRoot().x
                    } else {
                        0f
                    }
                val along = (1f - ride.value) * beyond
                translationX = along
                translationY = along * tan(PlakatShape.AXIS * PI.toFloat() / 180)
            }.size(FabSize)
            .drawBehind {
                drawCircle(if (enabled) srgbLerp(colors.red, colors.ink, pressed) else colors.concrete)
                if (ring) {
                    val w = PlakatLines.Rule.toPx()
                    drawCircle(colors.ink, size.minDimension / 2 + FocusOffset.toPx() + w / 2, style = Stroke(w))
                }
            }.clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        PlakatIcon(
            icon,
            contentDescription,
            Modifier.graphicsLayer {
                translationX = PressShift.toPx() * pressed
                translationY = translationX
            },
            tint,
        )
    }
}
