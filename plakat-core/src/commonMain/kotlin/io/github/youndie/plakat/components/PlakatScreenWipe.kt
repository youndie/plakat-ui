package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings

/**
 * The strip's geometry, from the MVP page's lab of 180 × 320 and its strip of frames of 60 × 106, which agree to a
 * pixel as shares of the screen: the edge leans back by `100 / 320` of the height, close to the −17° axis; the strip is
 * `40 / 180` of the width; it starts `10 / 180` of the width past the screen and ends `60 / 180` of it short.
 */
private const val SLANT = 100f / 320f
private const val BAND = 40f / 180f
private const val PAST = 10f / 180f
private const val SHORT = 60f / 180f

/** The spec row's `fade 100`. */
private const val FADE_MS = 100

/** Where the edge's top stands across a screen [size] at [progress] of a push, 0 before it and 1 after. */
internal fun wipeEdge(
    progress: Float,
    size: Size,
): Float {
    val from = size.width + size.height * SLANT + size.width * PAST
    val to = -size.width * SHORT
    return from + (to - from) * progress
}

/** What the screen on top shows at [progress] of a push: all that is right of the edge. */
private class ScreenEdge(
    val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val edge = wipeEdge(progress, size)
        return Outline.Generic(
            Path().apply {
                moveTo(edge, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(edge - size.height * SLANT, size.height)
                close()
            },
        )
    }
}

/** Clips a screen to what shows at [progress] of a push (`clip-path` of the page's `b`). */
internal fun Modifier.screenEdge(progress: () -> Float): Modifier =
    graphicsLayer {
        clip = true
        shape = ScreenEdge(progress())
    }

/** The red strip at [progress] of a push (`clip-path` of the page's `band`). */
internal fun DrawScope.drawWipeStrip(
    progress: Float,
    color: Color,
) {
    val edge = wipeEdge(progress, size)
    val slant = size.height * SLANT
    val band = size.width * BAND
    drawPath(
        Path().apply {
            moveTo(edge, 0f)
            lineTo(edge + band, 0f)
            lineTo(edge + band - slant, size.height)
            lineTo(edge - slant, size.height)
            close()
        },
        color,
    )
}

/**
 * The change of screen, for a navigation host to wrap around what it shows (`wipe`): a red strip crosses on the
 * diagonal in 320 ms, linear. A [screen] of a greater [depth], or the same, is a push, and comes in behind the strip
 * from the right; a smaller [depth] is a pop, and the strip goes back the other way, taking the screen on top with it.
 * Under reduced motion the new screen fades in, 100 ms, with no strip. [content] is called for a screen for as long as it
 * shows, and keeps its state through the change; a screen that has left is composed anew if it comes back, so what must
 * outlive that is the host's to keep. A change in the middle of a change finishes that one at once.
 */
@Composable
public fun <S> PlakatScreenWipe(
    screen: S,
    depth: Int,
    modifier: Modifier = Modifier,
    content: @Composable (S) -> Unit,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val progress = remember { Animatable(0f) }
    var settled by remember { mutableStateOf(screen) }
    var settledDepth by remember { mutableIntStateOf(depth) }
    var running by remember { mutableStateOf<Running<S>?>(null) }
    // A new screen stands at 0 from the composition that first has it, before the effect below gets to run.
    val interrupted = running?.takeIf { it.screen != screen }
    val from = interrupted?.screen ?: settled
    val fromDepth = interrupted?.depth ?: settledDepth
    val moving = from != screen
    val pop = depth < fromDepth && !reduce

    // Read at drawing, which may come before the composition that drops these layers once the change is over.
    fun at() =
        when {
            running?.screen == screen -> progress.value
            settled == screen -> 1f
            else -> 0f
        }
    LaunchedEffect(screen, depth) {
        running?.let {
            settled = it.screen
            settledDepth = it.depth
            running = null
        }
        if (screen == settled) return@LaunchedEffect
        progress.snapTo(0f)
        running = Running(screen, depth)
        events.onEvent(PlakatEvent.Wipe)
        progress.animateTo(1f, tween(if (reduce) FADE_MS else PlakatDurations.SCENE, easing = PlakatEasings.Linear))
        settled = screen
        settledDepth = depth
        running = null
    }
    val top = if (pop) from else screen
    val bottom = if (pop) screen else from
    Box(modifier.clipToBounds()) {
        for (s in if (moving) listOf(bottom, top) else listOf(screen)) {
            key(s) {
                val layer =
                    when {
                        !moving || s != top -> Modifier
                        reduce -> Modifier.graphicsLayer { alpha = at() }
                        pop -> Modifier.screenEdge { 1 - at() }
                        else -> Modifier.screenEdge { at() }
                    }
                Box(Modifier.fillMaxSize().then(layer).background(colors.paper)) { content(s) }
            }
        }
        if (moving && !reduce) {
            Spacer(
                Modifier.matchParentSize().drawBehind {
                    drawWipeStrip(if (pop) 1 - at() else at(), colors.red)
                },
            )
        }
    }
}

private class Running<S>(
    val screen: S,
    val depth: Int,
)
