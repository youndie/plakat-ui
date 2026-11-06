package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines

/** The list's width, designed here (D13): on the grid of 8, narrow enough to stand in a phone's 358 with its margins. */
private val MenuWidthLeast = 160.dp
private val MenuWidthMost = 320.dp

/** Stands under its anchor, at its left edge, and over it when it does not fit under. */
@Immutable
private data class MenuPosition(
    val x: Int,
    val y: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val under = anchorBounds.bottom + y
        val over = anchorBounds.top - y - popupContentSize.height
        val top =
            when {
                under + popupContentSize.height <= windowSize.height -> under
                over >= 0 -> over
                else -> (windowSize.height - popupContentSize.height).coerceAtLeast(0)
            }
        // The popup itself keeps inside the window across.
        return IntOffset(anchorBounds.left + x, top)
    }
}

/**
 * A menu, from the brief's row and designed here (D13): [PlakatMenuItem]s on paper in an 8 dp bar frame, 160 to 320 dp wide.
 * Put it beside what opens it, in one parent: it stands under that parent at its left edge, moved by [offset], and over
 * it where it does not fit under.
 *
 * With [expanded] true it opens by a wipe from the top, 120 ms linear (`wipe`); set false it goes back the same way, and is
 * gone from the composition after. Under reduced motion both are at once. A click outside it, Escape or the system back
 * gesture calls [onDismissRequest], and it takes the keyboard's focus while open, the arrows and Tab going round its items.
 */
@Composable
public fun PlakatMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    content: @Composable ColumnScope.() -> Unit,
) {
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val dismiss by rememberUpdatedState(onDismissRequest)
    val open = remember { Animatable(0f) }
    var seen by remember { mutableStateOf(false) }
    LaunchedEffect(expanded, reduce) {
        if (expanded != seen) events.onEvent(PlakatEvent.Wipe)
        seen = expanded
        val to = if (expanded) 1f else 0f
        if (reduce) {
            open.snapTo(to)
        } else {
            open.animateTo(to, tween(PlakatDurations.QUICK, easing = PlakatEasings.Linear))
        }
    }
    val gone by remember { derivedStateOf { open.value == 0f } }
    if (!expanded && gone) return
    val density = LocalDensity.current
    val position =
        remember(offset, density) {
            with(density) { MenuPosition(offset.x.roundToPx(), offset.y.roundToPx()) }
        }
    Popup(position, onDismissRequest, PopupProperties(focusable = true)) {
        val focus = LocalFocusManager.current
        val panel = remember { FocusRequester() }
        // Focus on the panel and not an item, so that nothing looks chosen until the arrows choose it.
        LaunchedEffect(Unit) { panel.requestFocus() }
        var onPanel by remember { mutableStateOf(false) }
        // Round the items, past the panel, which is only where the keyboard starts and Enter does nothing there.
        val move = { direction: FocusDirection ->
            val moved = focus.moveFocus(direction)
            if (onPanel) focus.moveFocus(direction)
            moved
        }
        PlakatMenuPanel(
            modifier
                .focusRequester(panel)
                .onPreviewKeyEvent {
                    if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (it.key) {
                        Key.DirectionDown -> {
                            move(FocusDirection.Next)
                        }

                        Key.DirectionUp -> {
                            move(FocusDirection.Previous)
                        }

                        Key.Tab -> {
                            move(if (it.isShiftPressed) FocusDirection.Previous else FocusDirection.Next)
                        }

                        Key.Escape -> {
                            dismiss()
                            true
                        }

                        else -> {
                            false
                        }
                    }
                }.onFocusChanged { onPanel = it.isFocused }
                .focusable(),
            { open.value },
            content,
        )
    }
}

/** The list itself: its frame and its paper, wiped at [progress]. */
@Composable
internal fun PlakatMenuPanel(
    modifier: Modifier,
    progress: () -> Float,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PlakatTheme.colors
    Column(
        modifier
            .plakatWipe(PlakatWipe.Top, progress)
            .widthIn(MenuWidthLeast, MenuWidthMost)
            .width(IntrinsicSize.Max)
            .background(colors.paper)
            .drawWithContent {
                drawContent()
                val bar = PlakatLines.Bar.toPx()
                drawRect(
                    colors.ink,
                    Offset(bar / 2, bar / 2),
                    Size(size.width - bar, size.height - bar),
                    style = Stroke(bar),
                )
            }.padding(PlakatLines.Bar)
            .verticalScroll(rememberScrollState()),
        content = content,
    )
}

/**
 * A menu's item: a [PlakatListItem] of one line, its [number] or its marker, its [text] and its [trailing], and the same
 * pressed inversion (`stamp`). A hair runs under it; give the last item [divider] false, as the frame closes it.
 */
@Composable
public fun PlakatMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    number: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    divider: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    PlakatListItemFrame(text, modifier, null, number, trailing, onClick, enabled, divider, interactionSource)
}
