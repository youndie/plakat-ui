package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings

/** Open and close under reduced motion: the spec row's `fade 100`, as the kit's dialog. */
private const val SCRIM_FADE_MS = 100

/**
 * The screen of ink dots the kit's dialog, bottom sheet and drawer close the background with, for an overlay of the app's
 * own: place it in a screen-filling box, before the overlay. The same screen, on its own clock: with [visible] true it
 * closes the background by the dialog's diagonal wipe from the left over the scene's 320 ms, linear; set false it goes
 * back the same way and leaves the composition after. Under reduced motion it fades, 100 ms.
 *
 * Nothing under it is reached. A click on it calls [onClick], and with [onClick] it carries a reader's dismiss action. It
 * is a target for the pointer only, never a stop for the keyboard (B-63): keeping focus in the overlay and returning it on
 * close is the overlay's, as the kit's own do with `returnFocusTo`. The screen's ink in the dark theme is the page's
 * `scrim: #0E0D0B`, which the design's tokens do not hold (B-80).
 */
@Composable
public fun BoxScope.PlakatScrim(
    visible: Boolean,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val reduce = PlakatTheme.reduceMotion
    val click by rememberUpdatedState(onClick)
    val open = remember { Animatable(0f) }
    LaunchedEffect(visible, reduce) {
        val ms = if (reduce) SCRIM_FADE_MS else PlakatDurations.SCENE
        open.animateTo(if (visible) 1f else 0f, tween(ms, easing = PlakatEasings.Linear))
    }
    val gone by remember { derivedStateOf { open.value == 0f } }
    if (!visible && gone) return
    PlakatDotScreen(
        PlakatTheme.colors,
        reduce,
        { open.value },
        modifier,
        dismiss = onClick?.let { { click?.invoke() } },
    ) { click?.invoke() }
}
