package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import kotlinx.coroutines.launch

/**
 * The stamp every pressable control shares (the spec rows `stamp` and `release`): [press] is 1 the
 * frame the finger lands and returns to 0 on the hard-stop in 60 ms, at once under reduced motion;
 * [held] is the finger itself, for what is no CSS transition and so changes the moment it lifts.
 */
@Stable
internal class PlakatPress {
    val press: Animatable<Float, AnimationVector1D> = Animatable(0f)
    var held: Boolean by mutableStateOf(false)
    var focused: Boolean by mutableStateOf(false)

    /** A pointer over it: never under a finger, which presses at once (B-39). */
    var hovered: Boolean by mutableStateOf(false)
}

/**
 * Follows [source], reporting `stamp` and `release` once per gesture. [sameUnderReducedMotion] keeps
 * the return's 60 ms under reduced motion, where a spec row's word is «то же» (the list item's).
 */
@Composable
internal fun rememberPlakatPress(
    source: InteractionSource,
    sameUnderReducedMotion: Boolean = false,
): PlakatPress {
    val state = remember { PlakatPress() }
    val events by rememberUpdatedState(PlakatTheme.events)
    val reduce by rememberUpdatedState(PlakatTheme.reduceMotion && !sameUnderReducedMotion)
    LaunchedEffect(source) {
        source.interactions.collect { interaction: Interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    state.held = true
                    events.onEvent(PlakatEvent.Stamp)
                    launch { state.press.snapTo(1f) }
                }

                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    state.held = false
                    events.onEvent(PlakatEvent.Release)
                    launch {
                        if (reduce) {
                            state.press.snapTo(0f)
                        } else {
                            state.press.animateTo(0f, tween(PlakatDurations.SNAP, easing = PlakatEasings.HardStop))
                        }
                    }
                }

                is HoverInteraction.Enter -> {
                    state.hovered = true
                }

                is HoverInteraction.Exit -> {
                    state.hovered = false
                }

                is FocusInteraction.Focus -> {
                    state.focused = true
                }

                is FocusInteraction.Unfocus -> {
                    state.focused = false
                }
            }
        }
    }
    return state
}

/** Whether focus shows now: a click focuses too and Compose never blurs, so only from the keyboard. */
@Composable
internal fun PlakatPress.focusShown(): Boolean =
    focused && LocalInputModeManager.current.inputMode == InputMode.Keyboard
