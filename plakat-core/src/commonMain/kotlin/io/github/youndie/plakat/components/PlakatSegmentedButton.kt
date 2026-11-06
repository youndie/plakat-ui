package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.ceil
import kotlin.math.floor

/** The Demo page's settings: `height: 40px` in a target of 48, `padding: 0 12px`. */
private val DrawnHeight = 40.dp
private val WordInset = 12.dp

/** Its words: the label's, with `letter-spacing: .06em`. */
private const val WORD_TRACKING = .06f

/**
 * A segmented button, the Demo page's: one frame of 3 dp, 40 dp drawn in a target of 48, and each of [segments] as wide as its
 * word and 12 either side, in capitals; given more width, they share what is over. The selected one is filled with ink, its
 * word in paper. A new [selectedIndex] sends the fill across linearly in 200 ms (`rail`), the words inverting as it passes
 * them, at once under reduced motion. Pressed, a segment inverts (`stamp`); disabled, the frame is a muted hair and the words
 * and the fill muted.
 */
@Composable
public fun PlakatSegmentedButton(
    segments: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    PlakatSegmentedButtonFrame(segments, selectedIndex, onSelect, modifier, enabled)
}

@Composable
internal fun PlakatSegmentedButtonFrame(
    segments: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    forceAt: Float? = null,
    forcePressed: Int? = null,
    forceFocused: Int? = null,
) {
    val colors = PlakatTheme.colors
    val fill =
        rememberPlakatChoiceMark(
            selectedIndex,
            segments.size,
            PlakatEvent.Rail,
            PlakatDurations.NORMAL,
            PlakatEasings.Linear,
        )
    val chosen = selectedIndex in segments.indices
    val type = PlakatTheme.typography.label
    val style: TextStyle = type.style.copy(letterSpacing = WORD_TRACKING.em)
    val measurer = rememberTextMeasurer()
    val words = remember(segments, style) { segments.map { measurer.measure(type.text(it), style) } }
    val ground = if (enabled) colors.ink else colors.inkMuted
    val presses =
        segments.indices.map { i ->
            key(i) {
                val source = remember { MutableInteractionSource() }
                source to rememberPlakatPress(source)
            }
        }
    val shown = presses.map { (_, press) -> press.focusShown() }
    // Each segment's left, as the page's flex row has them, with a last one for the right end.
    var edges by remember { mutableStateOf(listOf(0f)) }
    Layout(
        content = {
            segments.forEachIndexed { i, word ->
                Box(
                    Modifier
                        .semantics { contentDescription = word }
                        .selectable(
                            i == selectedIndex,
                            presses[i].first,
                            null,
                            enabled = enabled,
                            role = Role.RadioButton,
                        ) { onSelect(i) },
                )
            }
        },
        modifier =
            modifier
                .selectableGroup()
                .drawBehind {
                    if (edges.size != segments.size + 1) return@drawBehind
                    val inset = (size.height - DrawnHeight.toPx()) / 2
                    val high = DrawnHeight.toPx()

                    fun edge(at: Float): Float {
                        val low = floor(at).toInt().coerceIn(0, segments.size)
                        val up = (low + 1).coerceAtMost(segments.size)
                        return floor(edges[low] + (edges[up] - edges[low]) * (at - low) + .5f)
                    }
                    val line = (if (enabled) PlakatLines.Rule else PlakatLines.Hair).toPx()
                    drawRect(
                        ground,
                        Offset(line / 2, inset + line / 2),
                        Size(size.width - line, high - line),
                        style = Stroke(line),
                    )
                    val at = forceAt ?: fill.at.value
                    val left = edge(at)
                    val right = edge(at + 1)
                    val filled = forceAt != null || (chosen && fill.placed)

                    fun word(
                        i: Int,
                        color: Color,
                    ) {
                        val x = floor(edges[i] + WordInset.toPx() + .5f)
                        drawText(words[i], color, Offset(x, floor((size.height - words[i].size.height) / 2f + .5f)))
                    }
                    val ink = ground
                    val on = colors.paper
                    segments.indices.forEach { word(it, ink) }
                    if (filled) {
                        drawRect(ground, Offset(left, inset), Size(right - left, high))
                        clipRect(left, inset, right, inset + high) { segments.indices.forEach { word(it, on) } }
                    }
                    val pressed =
                        presses.indices.firstOrNull { forcePressed == it || presses[it].second.held }
                    if (pressed != null) {
                        val l = edge(pressed.toFloat())
                        val r = edge(pressed + 1f)
                        val onFill = pressed == selectedIndex
                        drawRect(if (onFill) on else ground, Offset(l, inset), Size(r - l, high))
                        word(pressed, if (onFill) ground else on)
                    }
                    val ring = presses.indices.firstOrNull { forceFocused == it || shown[it] }
                    if (ring != null) {
                        val rule = PlakatLines.Rule.toPx()
                        val l = edge(ring.toFloat())
                        val r = edge(ring + 1f)
                        drawRect(
                            colors.ink,
                            Offset(l + rule / 2, rule / 2),
                            Size(r - l - rule, size.height - rule),
                            style = Stroke(rule),
                        )
                    }
                },
    ) { measurables, constraints ->
        val inset = WordInset.toPx()
        val natural = words.map { it.getLineRight(0) + 2 * inset }
        val whole = natural.sum()
        val width = ceil(whole).toInt().coerceIn(constraints.minWidth, constraints.maxWidth)
        val over = if (natural.isEmpty()) 0f else ((width - whole) / natural.size).coerceAtLeast(0f)
        val lefts = FloatArray(natural.size + 1)
        natural.forEachIndexed { i, w -> lefts[i + 1] = lefts[i] + w + over }
        edges = lefts.toList()
        val height = PlakatSizes.Touch.roundToPx()
        val targets =
            measurables.mapIndexed { i, target ->
                val l = floor(lefts[i] + .5f).toInt()
                val r = floor(lefts[i + 1] + .5f).toInt()
                l to target.measure(Constraints.fixed((r - l).coerceAtLeast(0), height))
            }
        layout(width, height) { targets.forEach { (l, target) -> target.placeRelative(l, 0) } }
    }
}
