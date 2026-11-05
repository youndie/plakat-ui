package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.floor

/** The number's plate: the MVP page's `min-width: 20px; height: 20px; padding: 0 4px`. */
private val BadgeSize = 20.dp
private val BadgePadding = 4.dp

/** Its word: `font-size: 12px; font-weight: 600`, line `normal`. */
private const val BADGE_SP = 12

/** The dot: `width: 8px; height: 8px`, `plakat/line/bar`. */
private val DotSize = PlakatLines.Bar

/** Where each sits over the 48 dp box: the number `right: 4px; top: 6px`, the dot `right: 8px; top: 8px`. */
private val NumberRight = 4.dp
private val NumberTop = 6.dp
private val DotInset = 8.dp

/**
 * A badge: a red square with [value] in on-red, at least 20 dp, or for a null value an 8 dp red dot.
 * A new value ticks to it (`tick`): digit by digit, the lowest first, 40 ms a step and at most 400 ms
 * in all; at once under reduced motion. Red is not the only sign here: the number says it, and a dot
 * says only "something new" over an icon that names what.
 */
@Composable
public fun PlakatBadge(
    value: Int?,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    if (value == null) {
        Box(modifier.size(DotSize).background(colors.red))
        return
    }
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    var tick by remember { mutableStateOf(PlakatTick(value, value)) }
    var seen by remember { mutableStateOf(value) }
    val age = remember { Animatable(0f) }
    LaunchedEffect(value, reduce) {
        if (value == seen) return@LaunchedEffect
        // From what shows now, so a change in the middle of a tick does not jump.
        val shown = tick.valueAt(age.value.toDouble())
        seen = value
        tick = PlakatTick(shown, value)
        events.onEvent(PlakatEvent.Tick)
        if (reduce) {
            age.snapTo(Float.MAX_VALUE)
        } else {
            age.snapTo(0f)
            age.animateTo(tick.totalMs.toFloat(), tween(tick.totalMs, easing = PlakatEasings.Linear))
        }
    }
    val style =
        PlakatTheme.typography.bodyS.style
            .copy(fontSize = BADGE_SP.sp, fontWeight = FontWeight.SemiBold, color = colors.onRed)
            .cssLine(PlakatFaceMetrics.Body)
    // As wide as the number's real width and its padding, on a whole pixel as Blink snaps a box: a
    // layout's own width is ceiled, a pixel too wide. The target's, so the square stands through a tick.
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val width =
        remember(value, style, density) {
            val text = measurer.measure(value.coerceAtLeast(0).toString(), style).getLineRight(0)
            with(density) { (floor(text + 2 * BadgePadding.toPx() + .5f)).toDp() }.coerceAtLeast(BadgeSize)
        }
    Box(
        modifier.size(width, BadgeSize).background(colors.red),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(tick.textAt(age.value.toDouble()), style = style, softWrap = false)
    }
}

/**
 * A 48 dp box holding [content] (an icon, an icon button) centred, with a [PlakatBadge] of [value]
 * over its corner where the page puts it.
 */
@Composable
public fun PlakatBadgedBox(
    value: Int?,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.size(PlakatSizes.Touch), contentAlignment = Alignment.Center) {
        content()
        val place =
            if (value == null) {
                Modifier.align(Alignment.TopEnd).absoluteOffset(-DotInset, DotInset)
            } else {
                Modifier.align(Alignment.TopEnd).absoluteOffset(-NumberRight, NumberTop)
            }
        PlakatBadge(value, place)
    }
}
