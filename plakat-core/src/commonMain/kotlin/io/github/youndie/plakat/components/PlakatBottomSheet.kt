package io.github.youndie.plakat.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.youndie.plakat.theme.LocalPlakatContentColor
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatSizes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** The handle, designed here (D13): the brief's «брус», 8 high, on the grid at 64 wide, in a touch target of 48. */
private val HandleWidth = 64.dp

/** How much of the screen it leaves showing at its tallest, the dialog's `top: 96px`, designed here (D13). */
private val TopInset = 96.dp

/** Let go, it goes if it is over half down, or moving down faster than this in dp a second, designed here (D13). */
private const val LET_GO_SHARE = .5f
private const val FLICK_DP_S = 600f

/** The paper a [PlakatBottomSheet] is printed on. */
public enum class PlakatSheetGround {
    Paper,
    Concrete,
}

/**
 * A sheet from below, from the brief's row and designed here (D13): place it last in a screen-filling box. On [ground], an
 * 8 dp bar for its handle over [content], which scrolls if it must, as tall as it needs up to 96 dp short of the box.
 *
 * With [visible] true, whether from the start or set later, it opens (`rail`): it rides up its rail to a hard stop in 320 ms
 * while the dialog's screen closes the background by its diagonal wipe; set false it goes back the same way, and is gone from
 * the composition after. Under reduced motion both are at once. The handle follows a finger down the rail, and never above
 * its stop: let go over halfway, or flicked, it calls [onDismissRequest]; else it settles back in 120 ms hard-stop. A click
 * on the screen, Escape, the system back gesture and the screen reader's dismiss call [onDismissRequest] too, and focus stays
 * in the sheet while it is open, going on closing to [returnFocusTo], a requester on whatever opened it. [paneTitle] is what a
 * screen reader calls it, for there is no title drawn. Its ground runs to the foot of the box, and [content] stops above
 * [windowInsets], the system's bar by default.
 */
@Composable
public fun PlakatBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    ground: PlakatSheetGround = PlakatSheetGround.Paper,
    paneTitle: String? = null,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
    returnFocusTo: FocusRequester? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlakatBottomSheetFrame(
        visible,
        onDismissRequest,
        modifier,
        ground,
        paneTitle,
        content,
        windowInsets = windowInsets,
        returnFocusTo = returnFocusTo,
    )
}

@Composable
internal fun PlakatBottomSheetFrame(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    ground: PlakatSheetGround,
    paneTitle: String?,
    content: @Composable ColumnScope.() -> Unit,
    playIn: Boolean = true,
    windowInsets: WindowInsets = NoInsets,
    returnFocusTo: FocusRequester? = null,
) {
    ReturnFocus(visible, returnFocusTo)
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val dismiss by rememberUpdatedState(onDismissRequest)
    val showing by rememberUpdatedState(visible)
    val scope = rememberCoroutineScope()
    // How far down its rail the sheet is, 0 at its stop and 1 its whole height, and how far the screen has closed.
    val start = if (visible && !playIn) 1f else 0f
    var down by remember { mutableFloatStateOf(1f - start) }
    var screen by remember { mutableFloatStateOf(start) }
    var height by remember { mutableFloatStateOf(0f) }
    var motion by remember { mutableStateOf<Job?>(null) }

    fun runTo(
        to: Float,
        ms: Int,
    ) {
        motion?.cancel()
        motion =
            scope.launch {
                if (reduce) {
                    down = to
                } else {
                    animate(down, to, animationSpec = tween(ms, easing = PlakatEasings.HardStop)) { v, _ -> down = v }
                }
            }
    }
    // A sheet that starts visible opens: its first `rail` is the change from what stood before it.
    var seen by remember { mutableStateOf(visible && !playIn) }
    LaunchedEffect(visible, reduce) {
        if (visible != seen) events.onEvent(PlakatEvent.Rail)
        seen = visible
        runTo(if (visible) 0f else 1f, PlakatDurations.SCENE)
        val to = if (visible) 1f else 0f
        if (reduce) {
            screen = to
        } else {
            animate(screen, to, animationSpec = tween(PlakatDurations.SCENE, easing = PlakatEasings.Linear)) { v, _ ->
                screen = v
            }
        }
    }
    if (!visible && screen == 0f && down == 1f) return
    val focus = remember { FocusRequester() }
    LaunchedEffect(visible) { if (visible) focus.requestFocus() }
    NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None), isBackEnabled = visible) { dismiss() }
    val drag =
        rememberDraggableState { delta ->
            motion?.cancel()
            if (height > 0f) down = (down + delta / height).coerceIn(0f, 1f)
        }
    val flick = with(LocalDensity.current) { FLICK_DP_S.dp.toPx() }
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
        PlakatDotScreen(colors, reduce, { screen }) { if (visible) dismiss() }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(top = TopInset)
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    height = placeable.height.toFloat()
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, (placeable.height * down).roundToInt())
                    }
                }.background(if (ground == PlakatSheetGround.Paper) colors.paper else colors.concrete)
                .semantics {
                    dialog()
                    if (paneTitle != null) this.paneTitle = paneTitle
                    dismiss {
                        dismiss()
                        true
                    }
                },
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(PlakatSizes.Touch)
                    .draggable(
                        drag,
                        Orientation.Vertical,
                        enabled = visible,
                        onDragStopped = { velocity ->
                            if (down > LET_GO_SHARE || velocity > flick) {
                                dismiss()
                                // Asked to go: a frame for its answer, and if it stays it settles.
                                repeat(2) { withFrameMillis { } }
                                if (showing) runTo(0f, PlakatDurations.QUICK)
                            } else {
                                runTo(0f, PlakatDurations.QUICK)
                            }
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(HandleWidth, PlakatLines.Bar).background(colors.ink))
            }
            CompositionLocalProvider(LocalPlakatContentColor provides colors.ink) {
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .windowInsetsPadding(windowInsets),
                    content = content,
                )
            }
        }
    }
}
