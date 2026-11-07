package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatStencil
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** The strip: the MVP page's `height: 64px`. */
private val BarHeight = 64.dp

/** Behind each icon: `width: 40px; height: 32px`, the square that moves. */
private val SquareWidth = 40.dp
private val SquareHeight = 32.dp

/** Between the icon's box and the label: `gap: 4px`. */
private val LabelGap = 4.dp

/** The label: `font-size: 11px; font-weight: 600; letter-spacing: .06em`, line `normal`, 13 high. */
private const val LABEL_SP = 11
private const val LABEL_TRACKING = .06f
private val LabelHeight = 13.dp

/** One place in a [PlakatNavigationBar]: a stencil and a word, always both. */
@Immutable
public class PlakatNavigationItem(
    public val icon: PlakatStencil,
    public val label: String,
)

/**
 * A navigation bar: a 64 dp strip (ink in the light theme, concrete in the dark: whichever is the darker)
 * of [items], each a stencil over its word, in on-red. The active one is told by a red square behind its
 * icon rather than by a red icon, which would fail contrast on the strip (the Icons page: on-red on red
 * is 4.64:1). A new [selectedIndex] sends the square across in 120 ms hard-stop (`lock`), at once under
 * reduced motion; an index outside them, -1 for none, shows no square. Each item's target is its whole slot. Its ground runs under [windowInsets], the system's bar by default,
 * with the strip inside them.
 */
@Composable
public fun PlakatNavigationBar(
    items: List<PlakatNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val colors = PlakatTheme.colors
    val square =
        rememberPlakatChoiceMark(
            selectedIndex,
            items.size,
            PlakatEvent.Lock,
            PlakatDurations.QUICK,
            PlakatEasings.HardStop,
        )
    val chosen = selectedIndex in items.indices
    val strip = navigationGround(colors)
    val word: Color = colors.onRed
    val type = PlakatTheme.typography
    val style =
        type.label.style
            .copy(
                fontSize = LABEL_SP.sp,
                letterSpacing = LABEL_TRACKING.em,
                fontWeight = FontWeight.SemiBold,
                color = word,
            ).cssLine(PlakatFaceMetrics.Body)
    // A word is centred by its real width: a layout's own is ceiled, which leaves it up to a pixel left of the page's.
    val measurer = rememberTextMeasurer()
    val widths =
        remember(items, style) { items.map { measurer.measure(type.label.text(it.label), style).getLineRight(0) } }
    Layout(
        content = {
            items.forEachIndexed { i, item ->
                val source = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .semantics { contentDescription = item.label }
                        .plakatFocusFrame(source, word)
                        .selectable(
                            i == selectedIndex,
                            source,
                            null,
                            role = Role.Tab,
                        ) { onSelect(i) },
                )
                Box(Modifier.clearAndSetSemantics {}) { PlakatIcon(item.icon, null, tint = word) }
                BasicText(
                    type.label.text(item.label),
                    Modifier.clearAndSetSemantics {},
                    style = style,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        modifier =
            modifier.background(strip).windowInsetsPadding(windowInsets).drawBehind {
                drawRect(strip)
                if (chosen && square.placed) {
                    val slot = size.width / items.size.coerceAtLeast(1)
                    val w = SquareWidth.toPx()
                    // Blink snaps the box to the pixel as `left: n × 25%` moves it.
                    val x = floor(square.at.value * slot + (slot - w) / 2 + .5f)
                    val y = floor((size.height - contentHeight().toPx()) / 2 + .5f)
                    drawRect(colors.red, Offset(x, y), Size(w, SquareHeight.toPx()))
                }
            },
    ) { measurables, constraints ->
        val count = items.size.coerceAtLeast(1)
        val width = equalSlotsWidth(constraints, widths.maxOrNull() ?: 0f, count)
        // The page's 64, and more only where a large font's label would not fit in it.
        val tall =
            measurables.chunked(3).maxOfOrNull { (_, _, label) -> label.maxIntrinsicHeight(Constraints.Infinity) } ?: 0
        val height = max(BarHeight.roundToPx(), ((SquareHeight + LabelGap).toPx() + tall + 2 * LabelGap.toPx()).toInt())
        // Equal slots keep their half pixels, as the page's grid does: each child is placed from the true slot.
        val slot = width.toFloat() / count
        val top = (height - contentHeight().toPx()) / 2
        val iconBoxHeight = SquareHeight.toPx()
        val placed =
            measurables.chunked(3).mapIndexed { i, (target, icon, label) ->
                val left = floor(i * slot + .5f).toInt()
                val right = floor((i + 1) * slot + .5f).toInt()
                Triple(
                    target.measure(Constraints.fixed(right - left, height)) to left,
                    icon.measure(Constraints()),
                    label.measure(slotWord(right - left)),
                )
            }
        layout(width, height) {
            placed.forEachIndexed { i, (target, icon, label) ->
                target.first.placeRelative(target.second, 0)
                val centre = (i + .5f) * slot
                val iconTop = top + (iconBoxHeight - icon.height) / 2
                icon.placeRelative(floor(centre - icon.width / 2f + .5f).toInt(), floor(iconTop + .5f).toInt())
                val labelTop = top + iconBoxHeight + LabelGap.toPx()
                val across = min(widths[i], label.width.toFloat())
                label.placeRelative(floor(centre - across / 2f + .5f).toInt(), floor(labelTop + .5f).toInt())
            }
        }
    }
}

/** The ground of the kit's navigation, under on-red words: ink in the light theme, concrete in the dark, whichever is the darker. */
internal fun navigationGround(colors: PlakatColors): Color =
    if (colors.ink.luminance() < colors.concrete.luminance()) colors.ink else colors.concrete

/** The icon's box, the gap and the label: what the page centres in the strip. */
private fun contentHeight() = SquareHeight + LabelGap + LabelHeight
