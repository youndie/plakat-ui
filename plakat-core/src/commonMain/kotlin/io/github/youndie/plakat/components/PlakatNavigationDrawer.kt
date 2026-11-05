package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** The panel, designed here (D13): on the grid at 280, leaving a phone's 390 some 110 of the screen to tap. */
private val PanelWidth = 280.dp

/** A row, `plakat/size/field`, as a list row is. */
private val RowHeight = PlakatSizes.Field

/** Behind the active row's icon, the [PlakatNavigationBar]'s square: `width: 40px; height: 32px`. */
private val SquareWidth = 40.dp
private val SquareHeight = 32.dp

/**
 * A navigation drawer, from the brief's row and designed here (D13): place it last in a screen-filling box. A panel of 280 dp
 * from the left, ink in the light theme and concrete in the dark as the navigation bar is, of [items] on rows of 56, each a
 * stencil and its word in capitals in on-red. The active one has the bar's red square behind its stencil, and a new
 * [selectedIndex] sends the square along in 120 ms hard-stop (`lock`).
 *
 * With [visible] true, whether from the start or set later, it opens (`rail`): it rides in from the left to a hard stop in
 * 200 ms while the dialog's screen closes the rest; set false it goes back the same way, and is gone from the composition
 * after. Under reduced motion all of it is at once. A click on the screen, Escape, the system back gesture and the screen
 * reader's dismiss call [onDismissRequest], and focus stays in the panel while it is open. [paneTitle] is what a screen
 * reader calls it, for there is no title drawn.
 */
@Composable
public fun PlakatNavigationDrawer(
    visible: Boolean,
    items: List<PlakatNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    paneTitle: String? = null,
) {
    PlakatNavigationDrawerFrame(visible, items, selectedIndex, onSelect, onDismissRequest, modifier, paneTitle)
}

@Composable
internal fun PlakatNavigationDrawerFrame(
    visible: Boolean,
    items: List<PlakatNavigationItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    paneTitle: String?,
    playIn: Boolean = true,
    forcePressed: Int? = null,
    forceFocused: Int? = null,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val dismiss by rememberUpdatedState(onDismissRequest)
    val start = if (visible && !playIn) 1f else 0f
    val ride = remember { Animatable(start) }
    val screen = remember { Animatable(start) }
    // A drawer that starts visible opens: its first `rail` is the change from what stood before it.
    var seen by remember { mutableStateOf(visible && !playIn) }
    LaunchedEffect(visible, reduce) {
        if (visible != seen) events.onEvent(PlakatEvent.Rail)
        seen = visible
        val to = if (visible) 1f else 0f
        if (reduce) {
            ride.snapTo(to)
            screen.snapTo(to)
        } else {
            coroutineScope {
                launch { ride.animateTo(to, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop)) }
                launch { screen.animateTo(to, tween(PlakatDurations.NORMAL, easing = PlakatEasings.Linear)) }
            }
        }
    }
    val square = remember { Animatable(selectedIndex.toFloat()) }
    var seenIndex by remember { mutableStateOf(selectedIndex) }
    LaunchedEffect(selectedIndex, reduce) {
        if (selectedIndex != seenIndex) events.onEvent(PlakatEvent.Lock)
        seenIndex = selectedIndex
        val to = selectedIndex.toFloat()
        if (reduce) {
            square.snapTo(to)
        } else {
            square.animateTo(to, tween(PlakatDurations.QUICK, easing = PlakatEasings.HardStop))
        }
    }
    if (!visible && ride.value == 0f && screen.value == 0f) return
    val focus = remember { FocusRequester() }
    LaunchedEffect(visible) { if (visible) focus.requestFocus() }
    NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = visible) { dismiss() }
    val ground = navigationGround(colors)
    Box(
        modifier
            .fillMaxSize()
            .clipToBounds()
            .focusProperties { onExit = { if (visible) cancelFocusChange() } }
            .focusGroup()
            .focusRequester(focus)
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                    dismiss()
                    true
                } else {
                    false
                }
            }.focusable(),
    ) {
        PlakatDotScreen(colors, reduce, { screen.value }) { if (visible) dismiss() }
        Column(
            Modifier
                .width(PanelWidth)
                .fillMaxHeight()
                .graphicsLayer { translationX = (ride.value - 1f) * size.width }
                .drawBehind { drawRect(ground) }
                .semantics {
                    dialog()
                    if (paneTitle != null) this.paneTitle = paneTitle
                    dismiss {
                        dismiss()
                        true
                    }
                }.verticalScroll(rememberScrollState())
                .padding(vertical = PlakatGrid.Margin)
                .drawBehind {
                    val row = RowHeight.toPx()
                    val h = SquareHeight.toPx()
                    drawRect(
                        colors.red,
                        Offset(PlakatGrid.Margin.toPx(), square.value * row + (row - h) / 2),
                        Size(SquareWidth.toPx(), h),
                    )
                },
        ) {
            items.forEachIndexed { i, item ->
                DrawerRow(
                    item,
                    i == selectedIndex,
                    colors,
                    { onSelect(i) },
                    forcePressed == i,
                    forceFocused == i,
                )
            }
        }
    }
}

@Composable
private fun DrawerRow(
    item: PlakatNavigationItem,
    selected: Boolean,
    colors: PlakatColors,
    onClick: () -> Unit,
    forcePressed: Boolean,
    forceFocused: Boolean,
) {
    val source = remember { MutableInteractionSource() }
    val state = rememberPlakatPress(source)
    val press = if (forcePressed) 1f else state.press.value
    val held = forcePressed || state.held
    val word = if (held) navigationGround(colors) else colors.onRed
    val type = PlakatTheme.typography
    val ring = forceFocused || state.focusShown()
    Box(
        Modifier
            .fillMaxWidth()
            .height(RowHeight)
            .semantics { contentDescription = item.label }
            .drawBehind {
                drawRect(srgbLerp(colors.onRed.copy(alpha = 0f), colors.onRed, press))
                if (ring) {
                    val line = PlakatLines.Rule.toPx()
                    drawRect(
                        word,
                        Offset(line / 2, line / 2),
                        Size(size.width - line, size.height - line),
                        style = Stroke(line),
                    )
                }
            }.selectable(selected, source, null, role = Role.Tab, onClick = onClick),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = PlakatGrid.Margin)
                .clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(PlakatGrid.Margin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(SquareWidth, SquareHeight), contentAlignment = Alignment.Center) {
                PlakatIcon(item.icon, null, tint = word)
            }
            BasicText(
                type.label.text(item.label),
                style =
                    type.label.style
                        .copy(color = word)
                        .cssLine(PlakatFaceMetrics.Body),
                softWrap = false,
                maxLines = 1,
            )
        }
    }
}
