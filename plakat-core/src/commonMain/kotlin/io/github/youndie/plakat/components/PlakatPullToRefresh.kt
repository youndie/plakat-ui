package io.github.youndie.plakat.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatLines
import io.github.youndie.plakat.tokens.PlakatShape
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.tan

/** How far a pull must come to count, and the most it comes, designed here (D13): on the grid. */
private val PullToCount = 80.dp
private val PullMost = 120.dp

/** The sector's plate, designed here (D13): a step of 8 about the progress's 40. */
private val PlateSize = 56.dp

/**
 * Pull-to-refresh, from the brief's row and designed here (D13): put a list in [content], and a pull from its top grows a
 * red wedge from the box's top left, as tall as the finger has come, up to 120 dp, its edge on the −17° axis. At 80 dp it
 * gains a rule of ink along that edge, the sign that letting go now will count. Let go there, it calls [onRefresh]; let go
 * short, it does not; either way the wedge goes back in 120 ms hard-stop, at once under reduced motion. While [refreshing],
 * the circular progress's stepped sector stands on a plate of paper in that corner (`engage`), over the list.
 *
 * A pull only comes from a finger on the scroll of [content]: a wheel has no letting go. A reader has the same as an action,
 * named by the theme's `refresh`, and the sector says the theme's `loading`.
 */
@Composable
public fun PlakatPullToRefreshBox(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    PlakatPullToRefreshFrame(refreshing, onRefresh, modifier, enabled, content)
}

@Composable
internal fun PlakatPullToRefreshFrame(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable BoxScope.() -> Unit,
    forcePull: Float? = null,
) {
    val colors = PlakatTheme.colors
    val reduce by rememberUpdatedState(PlakatTheme.reduceMotion)
    val events by rememberUpdatedState(PlakatTheme.events)
    val refresh by rememberUpdatedState(onRefresh)
    val allowed by rememberUpdatedState(enabled && !refreshing)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val most = with(density) { PullMost.toPx() }
    val counts = with(density) { PullToCount.toPx() }
    var pull by remember { mutableFloatStateOf(0f) }
    var fingers by remember { mutableStateOf(false) }
    var motion by remember { mutableStateOf<Job?>(null) }

    fun letGo() {
        if (pull >= counts) refresh()
        motion?.cancel()
        if (pull == 0f) return
        if (reduce) {
            pull = 0f
        } else {
            motion =
                scope.launch {
                    animate(
                        pull,
                        0f,
                        animationSpec = tween(PlakatDurations.QUICK, easing = PlakatEasings.HardStop),
                    ) { v, _ ->
                        pull = v
                    }
                }
        }
    }
    val connection =
        remember {
            object : NestedScrollConnection {
                // Going back up, the wedge goes first and the list only after it.
                override fun onPreScroll(
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y >= 0f || pull <= 0f) return Offset.Zero
                    val before = pull
                    pull = (before + available.y).coerceAtLeast(0f)
                    return Offset(0f, pull - before)
                }

                // What the list has no use for at its top is the pull.
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (available.y <= 0f || !allowed || !fingers) return Offset.Zero
                    motion?.cancel()
                    val before = pull
                    pull = (before + available.y).coerceAtMost(most)
                    return Offset(0f, pull - before)
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (pull <= 0f) return Velocity.Zero
                    letGo()
                    return available
                }
            }
        }
    var seen by remember { mutableStateOf(false) }
    LaunchedEffect(refreshing) {
        if (refreshing && !seen) events.onEvent(PlakatEvent.Engage)
        seen = refreshing
    }
    val hypotenuse = remember { 1f / tan(-PlakatShape.AXIS * PI.toFloat() / 180f) }
    val strings = PlakatTheme.strings
    Box(
        modifier
            .clipToBounds()
            .semantics {
                // A pull is not the only way: a reader has no finger to drag with.
                if (enabled && !refreshing) {
                    customActions =
                        listOf(
                            CustomAccessibilityAction(strings.refresh) {
                                refresh()
                                true
                            },
                        )
                }
            }.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        fingers = event.changes.any { it.pressed }
                    }
                }
            }.nestedScroll(connection)
            .drawWithContent {
                drawContent()
                val h = forcePull?.let { it.dp.toPx() } ?: pull
                if (h <= 0f) return@drawWithContent
                val x = h * hypotenuse
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f)
                        lineTo(x, 0f)
                        lineTo(0f, h)
                        close()
                    },
                    colors.red,
                )
                if (h >= PullToCount.toPx()) {
                    drawLine(colors.ink, Offset(x, 0f), Offset(0f, h), PlakatLines.Rule.toPx())
                }
            },
    ) {
        content()
        if (refreshing) {
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .size(PlateSize)
                    .background(colors.paper)
                    .semantics { contentDescription = strings.loading },
                contentAlignment = Alignment.Center,
            ) { PlakatCircularProgress(null) }
        }
    }
}
