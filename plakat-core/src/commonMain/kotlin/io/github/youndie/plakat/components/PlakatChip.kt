package io.github.youndie.plakat.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.floor

/** Inside the plate: the MVP page's `padding: 0 12px`. */
private val ChipPadding = 12.dp

/** The selected wedge, `width: 10px; height: 10px; clip-path: polygon(0 0, 100% 0, 100% 100%)`, 6 before the word. */
private val WedgeSize = 10.dp
private val WedgeGap = 6.dp

/** The focus ring's distance from the plate: `outline: 3px; outline-offset: 3px`. */
private val FocusOffset = PlakatLines.Rule

/** A chip's look, forced for the fixtures of the page's static cells. */
internal enum class ChipLook { Focused, Hovered }

/**
 * A chip, the stamp: [text] in capitals in a 3 dp ink frame, a 32 dp plate in a 48 dp touch target,
 * 12 dp in from its sides and as wide as its word. [selected] fills it ink with a red wedge before the
 * word (`stamp`), in one frame and under reduced motion alike, so the wedge and the fill say it, not
 * the red alone. Disabled it is a dashed muted hair with the word muted. Focused from the keyboard, a
 * 3 dp ink ring stands 3 dp off the plate.
 */
@Composable
public fun PlakatChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatChipFrame(text, selected, onClick, modifier, enabled, interactionSource)
}

@Composable
internal fun PlakatChipFrame(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    interactionSource: MutableInteractionSource?,
    look: ChipLook? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    var focused by remember { mutableStateOf(false) }
    var hoveredNow by remember { mutableStateOf(false) }
    LaunchedEffect(source) {
        source.interactions.collect {
            when (it) {
                is FocusInteraction.Focus -> focused = true
                is FocusInteraction.Unfocus -> focused = false
                is HoverInteraction.Enter -> hoveredNow = true
                is HoverInteraction.Exit -> hoveredNow = false
            }
        }
    }
    // Only the selecting is the spec row's `stamp`; a chip let go of says nothing.
    val events by rememberUpdatedState(PlakatTheme.events)
    var seen by remember { mutableStateOf(selected) }
    LaunchedEffect(selected) {
        if (selected && !seen) events.onEvent(PlakatEvent.Stamp)
        seen = selected
    }

    val filled = selected && enabled
    val label = PlakatTheme.typography.label
    val word = label.text(text)
    val style =
        label.style
            .copy(
                color =
                    if (!enabled) {
                        colors.inkMuted
                    } else if (filled) {
                        colors.paper
                    } else {
                        colors.ink
                    },
            ).cssLine(PlakatFaceMetrics.Body)
    // A disabled chip's hair adds to its width and moves its word in: `border: 1px dashed; box-sizing: border-box`.
    val hair = if (enabled) 0.dp else PlakatLines.Hair
    val mark = if (filled) WedgeSize + WedgeGap else 0.dp
    // As wide as its word's real width and its padding, on a whole pixel as Blink snaps a box: a layout's
    // own width is ceiled, a pixel too wide.
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val natural: Dp =
        remember(word, style, density, mark, hair) {
            with(density) {
                val inside = (ChipPadding + hair).toPx() * 2 + mark.toPx()
                floor(measurer.measure(word, style).getLineRight(0) + inside + .5f).toDp()
            }
        }
    val ring = look == ChipLook.Focused || (focused && keyboard)
    Box(
        modifier
            .defaultMinSize(minWidth = natural.coerceAtLeast(PlakatSizes.Touch), minHeight = PlakatSizes.Touch)
            .drawBehind { drawChip(colors, enabled, filled, ring, enabled && (look == ChipLook.Hovered || hoveredNow)) }
            .selectable(selected, source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Each centred on the plate by itself, as the page's flex items are; a Row would round the wedge a pixel low.
        if (filled) {
            Canvas(Modifier.padding(start = ChipPadding).size(WedgeSize)) {
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, size.height)
                        close()
                    },
                    colors.red,
                )
            }
        }
        BasicText(
            word,
            Modifier
                .padding(start = ChipPadding + hair + mark)
                // Room at its end when it must give way. Not a padding: the chip's width rounds its word's real width as Blink
                // does, up to a pixel under the word's ceiled one, so a word that fits has that pixel more here.
                .layout { measurable, constraints ->
                    val end = (ChipPadding + hair).roundToPx() - 1
                    val placeable =
                        measurable.measure(
                            if (constraints.hasBoundedWidth) {
                                constraints.copy(maxWidth = (constraints.maxWidth - end).coerceAtLeast(0))
                            } else {
                                constraints
                            },
                        )
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                },
            style = style,
            softWrap = false,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The 32 dp plate centred in the target: its frame, fill or dashed hair, and the focus ring round it. */
private fun DrawScope.drawChip(
    colors: PlakatColors,
    enabled: Boolean,
    filled: Boolean,
    ring: Boolean,
    hovered: Boolean,
) {
    val h = PlakatSizes.Chip.toPx()
    val top = (size.height - h) / 2
    val rule = PlakatLines.Rule.toPx()
    // Designed here (B-39): the outlined plate fills with concrete under its frame.
    if (hovered && !filled) drawRect(colors.concrete, Offset(0f, top), Size(size.width, h))
    when {
        !enabled -> {
            val w = PlakatLines.Hair.toPx()
            val half = w / 2
            dashedLine(colors.inkMuted, Offset(0f, top + half), Offset(size.width, top + half), w)
            dashedLine(colors.inkMuted, Offset(0f, top + h - half), Offset(size.width, top + h - half), w)
            dashedLine(colors.inkMuted, Offset(half, top), Offset(half, top + h), w)
            dashedLine(colors.inkMuted, Offset(size.width - half, top), Offset(size.width - half, top + h), w)
        }

        filled -> {
            drawRect(colors.ink, Offset(0f, top), Size(size.width, h))
            // And the filled one gets a frame that shows on ink.
            if (hovered) {
                drawRect(
                    colors.concrete,
                    Offset(rule / 2, top + rule / 2),
                    Size(size.width - rule, h - rule),
                    style = Stroke(rule),
                )
            }
        }

        else -> {
            drawRect(
                colors.ink,
                Offset(rule / 2, top + rule / 2),
                Size(size.width - rule, h - rule),
                style = Stroke(rule),
            )
        }
    }
    if (ring) {
        val o = FocusOffset.toPx() + rule / 2
        drawRect(colors.ink, Offset(-o, top - o), Size(size.width + 2 * o, h + 2 * o), style = Stroke(rule))
    }
}
