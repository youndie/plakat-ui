package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlin.math.roundToInt

/** Around the card's content: the Core page's `padding: 12px`, not a grid step. */
private val CardPadding = 12.dp

/** Between the card's children: the page's `gap: 16px`, `plakat/grid/margin`. */
private val CardGap = PlakatGrid.Margin

/** The selected card's bar on its left edge (`width: 8px`, `plakat/line/bar`); the content moves right by it. */
private val SelectedBar = PlakatLines.Bar

/** The focus ring's distance from the sheet: the page's `outline: 3px; outline-offset: 3px`. */
private val FocusOffset = PlakatLines.Rule

/** The print wipe's edge leans back this far over the card's height: `polygon(0 0, x 0, x − 60px 100%, 0 100%)`. */
private val PrintSlant = 60.dp

/** Print under reduced motion: the spec row's `fade 100`, linear, no cascade. */
private const val PRINT_FADE_MS = 100

/** How far a torn card rides along its rail: `translateX(110%)` of its own width. */
private const val TEAR_RIDE = 1.1f

/**
 * A card: a sheet of concrete paper (a change of paper, no shadow, no border) laying its [content]
 * out in a row, 12 dp in from its edges, 16 dp apart, centred vertically; at least 48 dp tall.
 *
 * Pressed, the sheet turns ink and its content paper: set words in [LocalPlakatContentColor], as a word with a colour of
 * its own keeps it, ink on ink. A picture stays as it is. [selected] puts a
 * red 8 dp bar on its left edge, the content moved right by it — red is not the only sign of it,
 * so say it in the content too (the design's «· ЧИТАЕТЕ»). Focused from the keyboard, a 3 dp ink
 * ring stands 3 dp off the sheet. [onClick] null makes it a sheet that only shows; [enabled] false
 * takes the click away and keeps the look, which the design does not draw otherwise.
 *
 * It prints as it enters the composition (`print`): a diagonal wipe from the top-left in 200 ms,
 * linear, 30 ms later for each step of [printOrder], so a list cascades; under reduced motion a
 * 100 ms fade instead. [printOrder] null shows it at once (a card that scrolls back into a lazy
 * list need not print again). Set [dismissed] and it tears (`tear`): it rides off to the right
 * along its rail in 200 ms hard-stop, then its place closes up in 200 ms hard-stop, and
 * [onDismissed] is called; under reduced motion it is gone at once. Unset, it is back at once.
 */
@Composable
public fun PlakatCard(
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    printOrder: Int? = 0,
    dismissed: Boolean = false,
    onDismissed: () -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    PlakatCardFrame(
        onClick,
        modifier,
        selected,
        enabled,
        printOrder,
        dismissed,
        onDismissed,
        interactionSource,
        content = content,
    )
}

@Composable
internal fun PlakatCardFrame(
    onClick: (() -> Unit)?,
    modifier: Modifier,
    selected: Boolean,
    enabled: Boolean,
    printOrder: Int?,
    dismissed: Boolean,
    onDismissed: () -> Unit,
    interactionSource: MutableInteractionSource?,
    forcePressed: Boolean = false,
    forceFocused: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val done by rememberUpdatedState(onDismissed)
    val keyboard = LocalInputModeManager.current.inputMode == InputMode.Keyboard
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(source) {
        source.interactions.collect {
            when (it) {
                is FocusInteraction.Focus -> focused = true
                is FocusInteraction.Unfocus -> focused = false
            }
        }
    }
    val held by source.collectIsPressedAsState()
    val pressed = forcePressed || (held && onClick != null && enabled)

    // print: the wipe's way across (0 hidden, 1 whole), or under reduced motion the fade's.
    val wipe = remember { Animatable(if (printOrder == null) 1f else 0f) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        if (printOrder == null) return@LaunchedEffect
        events.onEvent(PlakatEvent.Print)
        if (reduce) {
            fade.snapTo(0f)
            wipe.snapTo(1f)
            fade.animateTo(1f, tween(PRINT_FADE_MS, easing = PlakatEasings.Linear))
        } else {
            val delay = PlakatDurations.CASCADE * printOrder.coerceAtLeast(0)
            wipe.animateTo(1f, tween(PlakatDurations.NORMAL, delay, PlakatEasings.Linear))
        }
    }

    // tear: one 400 ms timeline, so the place starts closing up at 200 ms exactly, as the page's
    // `height … 200ms` delay does; two plays in a row would start the second a frame late.
    val tear = remember { Animatable(if (dismissed) 1f else 0f) }
    var seen by remember { mutableStateOf(dismissed) }
    LaunchedEffect(dismissed, reduce) {
        val changed = dismissed != seen
        seen = dismissed
        if (!dismissed) {
            tear.snapTo(0f)
            return@LaunchedEffect
        }
        if (changed) events.onEvent(PlakatEvent.Tear)
        if (reduce) {
            tear.snapTo(1f)
        } else {
            tear.animateTo(1f, tween(2 * PlakatDurations.NORMAL, easing = PlakatEasings.Linear))
        }
        if (changed) done()
    }
    // The ride over the first half, the collapse over the second, each on the hard-stop.
    val ride = { PlakatEasings.HardStop.transform((tear.value * 2).coerceIn(0f, 1f)) }
    val collapse = { PlakatEasings.HardStop.transform((tear.value * 2 - 1).coerceIn(0f, 1f)) }

    val click =
        if (onClick != null) {
            Modifier.clickable(
                source,
                indication = null,
                enabled = enabled && !dismissed,
                role = Role.Button,
                onClick = onClick,
            )
        } else {
            Modifier
        }
    val ring = forceFocused || (focused && keyboard)
    val bar = if (selected) SelectedBar else 0.dp
    CompositionLocalProvider(LocalPlakatContentColor provides if (pressed) colors.paper else colors.ink) {
        Row(
            modifier
                // The place closes up from the bottom; the card, gone by then, is clipped to it.
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val height = (placeable.height * (1f - collapse())).roundToInt()
                    layout(placeable.width, height) { placeable.place(0, 0) }
                }.graphicsLayer {
                    clip = ride() > 0f
                }.graphicsLayer {
                    translationX = ride() * TEAR_RIDE * size.width
                    alpha = fade.value
                }.plakatWipe(PlakatWipe.Diagonal(PrintSlant)) { wipe.value }
                .then(
                    // Torn, it leaves the Tab order and the reader at once, though it stays composed until the caller removes it.
                    if (dismissed) {
                        Modifier.clearAndSetSemantics {
                        }
                    } else {
                        Modifier.semantics { this.selected = selected }
                    },
                ).then(click)
                .drawBehind { drawCard(colors, pressed, bar.toPx(), ring) }
                .defaultMinSize(minHeight = PlakatSizes.Touch)
                .padding(
                    PaddingValues(
                        start = CardPadding + bar,
                        top = CardPadding,
                        end = CardPadding,
                        bottom = CardPadding,
                    ),
                ),
            horizontalArrangement = Arrangement.spacedBy(CardGap),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

private fun DrawScope.drawCard(
    colors: PlakatColors,
    pressed: Boolean,
    bar: Float,
    ring: Boolean,
) {
    drawRect(if (pressed) colors.ink else colors.concrete)
    if (bar > 0f) drawRect(colors.red, Offset.Zero, Size(bar, size.height))
    if (ring) {
        val w = PlakatLines.Rule.toPx()
        val o = FocusOffset.toPx() + w / 2
        drawRect(colors.ink, Offset(-o, -o), Size(size.width + 2 * o, size.height + 2 * o), style = Stroke(w))
    }
}
