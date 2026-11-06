package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/** A word's margin on each side, designed here (D13), when equal slots are given no width to divide (B-58). */
private val SlotMargin = PlakatGrid.Margin

/**
 * What a row of [count] equal slots is across: all it is given, or, given no limit, enough for its widest word.
 * Each slot is still a target of at least 48 dp.
 */
internal fun Density.equalSlotsWidth(
    constraints: Constraints,
    widest: Float,
    count: Int,
): Int =
    if (constraints.hasBoundedWidth) {
        constraints.maxWidth
    } else {
        val slot = max(PlakatSizes.Touch.roundToPx(), ceil(widest).toInt() + SlotMargin.roundToPx() * 2)
        (slot * count).coerceAtLeast(constraints.minWidth)
    }

/** One tab of [PlakatTabs]: a rubric; disabled, it is muted and takes no click. */
@Immutable
public class PlakatTab(
    public val label: String,
    public val enabled: Boolean = true,
)

/**
 * Rubric tabs in equal slots, 48 dp over a 1 dp ink hairline, the words in capitals. A red 8 dp bar stands
 * under the selected one, overlapping the hairline; a new [selectedIndex] sends it across linearly in
 * 200 ms (`rail`), at once under reduced motion. Put what the tabs choose in [PlakatTabContent], which moves
 * on the same switch.
 */
@Composable
public fun PlakatTabs(
    tabs: List<PlakatTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val bar = remember { Animatable(selectedIndex.toFloat()) }
    var seen by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex, reduce) {
        if (selectedIndex != seen) events.onEvent(PlakatEvent.Rail)
        seen = selectedIndex
        val target = selectedIndex.toFloat()
        if (reduce) {
            bar.snapTo(target)
        } else {
            bar.animateTo(target, tween(PlakatDurations.NORMAL, easing = PlakatEasings.Linear))
        }
    }
    val label = PlakatTheme.typography.label
    val style = label.style.cssLine(PlakatFaceMetrics.Body)
    // A word is centred by its real width: a layout's own is ceiled, which leaves it up to a pixel left of the page's.
    val measurer = rememberTextMeasurer()
    val widths = remember(tabs, style) { tabs.map { measurer.measure(label.text(it.label), style).getLineRight(0) } }
    val count = tabs.size.coerceAtLeast(1)
    Layout(
        content = {
            tabs.forEachIndexed { i, tab ->
                val source = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .semantics { contentDescription = tab.label }
                        .plakatFocusFrame(source, colors.ink)
                        .selectable(
                            i == selectedIndex,
                            source,
                            null,
                            enabled = tab.enabled,
                            role = Role.Tab,
                        ) { onSelect(i) },
                )
                BasicText(
                    label.text(tab.label),
                    Modifier.clearAndSetSemantics {},
                    style = style.copy(color = if (tab.enabled) colors.ink else colors.inkMuted),
                    softWrap = false,
                )
            }
        },
        modifier =
            modifier.drawBehind {
                val hair = PlakatLines.Hair.toPx()
                drawRect(colors.ink, Offset(0f, size.height - hair), Size(size.width, hair))
                // The page's `bottom: -1px`: over the hairline. Blink snaps the box to the pixel as `left` moves it.
                val slot = size.width / count
                val h = PlakatLines.Bar.toPx()
                val left = floor(bar.value * slot + .5f)
                val right = floor((bar.value + 1) * slot + .5f)
                drawRect(colors.red, Offset(left, size.height - h), Size(right - left, h))
            },
    ) { measurables, constraints ->
        val width = equalSlotsWidth(constraints, widths.maxOrNull() ?: 0f, count)
        val row = PlakatSizes.Touch.roundToPx()
        val height = row + PlakatLines.Hair.roundToPx()
        // Equal slots keep their half pixels: each child is placed from the true slot.
        val slot = width.toFloat() / count
        val placed =
            measurables.chunked(2).mapIndexed { i, (target, word) ->
                val left = floor(i * slot + .5f).toInt()
                val right = floor((i + 1) * slot + .5f).toInt()
                Triple(target.measure(Constraints.fixed(right - left, row)), left, word.measure(Constraints()))
            }
        layout(width, height) {
            placed.forEachIndexed { i, (target, left, word) ->
                target.placeRelative(left, 0)
                val x = (i + .5f) * slot - widths[i] / 2f
                word.placeRelative(floor(x + .5f).toInt(), floor((row - word.height) / 2f + .5f).toInt())
            }
        }
    }
}

/**
 * What the tabs choose: the page at [selectedIndex] fills it, and a new index shifts the pages across in
 * 200 ms hard-stop (the spec row's second half), at once under reduced motion. Only the pages in sight
 * are composed.
 */
@Composable
public fun PlakatTabContent(
    pageCount: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    page: @Composable (Int) -> Unit,
) {
    val reduce = PlakatTheme.reduceMotion
    val shift = remember { Animatable(selectedIndex.toFloat()) }
    LaunchedEffect(selectedIndex, reduce) {
        val target = selectedIndex.toFloat()
        if (reduce) {
            shift.snapTo(target)
        } else {
            shift.animateTo(target, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop))
        }
    }
    val first by remember { derivedStateOf { floor(shift.value).toInt() } }
    val last by remember { derivedStateOf { ceil(shift.value).toInt() } }
    val shown = (first..last).filter { it in 0 until pageCount }
    Layout(
        content = {
            for (i in shown) {
                key(i) { Box(Modifier.graphicsLayer { translationX = (i - shift.value) * size.width }) { page(i) } }
            }
        },
        modifier = modifier.clipToBounds(),
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.maxOfOrNull { it.height } ?: constraints.minHeight
        layout(width, height) { placeables.forEach { it.placeRelative(0, 0) } }
    }
}
