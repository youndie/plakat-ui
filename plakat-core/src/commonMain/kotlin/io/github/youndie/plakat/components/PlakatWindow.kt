package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import io.github.youndie.plakat.tokens.PlakatGrid
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A panel's measures. Compact, the MVP page's cell: a picture `height: 160px`, its number `left: 8px; top: 8px; font-size:
 * 14px; padding: 2px 6px`, `gap: 8px` between the parts and the panels, caption and body at 14 px, lines of 1.15 and 1.4.
 * Large, the Demo page's onboarding, each panel the whole width: `padding: 0 16px`, a picture of 360, its number
 * `left: 12px; top: 12px; padding: 4px 8px`, `gap: 20px`, the caption `font-size: 32px; line-height: 1`, the body
 * `font-size: 17px; line-height: 1.45; max-width: 320px`.
 */
private class WindowMeasures(
    val picture: Dp,
    val numberInset: Dp,
    val numberPadX: Dp,
    val numberPadY: Dp,
    val gap: Dp,
    val panelPad: Dp,
    val panelGap: Dp,
    val captionSp: Int,
    val captionLine: Float,
    val bodySp: Int,
    val bodyLine: Float,
    val bodyWide: Dp,
)

private val Compact =
    WindowMeasures(
        160.dp,
        PlakatGrid.Step,
        6.dp,
        2.dp,
        PlakatGrid.Step,
        0.dp,
        PlakatGrid.Step,
        14,
        1.15f,
        14,
        1.4f,
        Dp.Infinity,
    )
private val Large =
    WindowMeasures(360.dp, 12.dp, PlakatGrid.Step, 4.dp, 20.dp, PlakatGrid.Margin, 0.dp, 32, 1f, 17, 1.45f, 320.dp)

/** The number in its tag is always 14 px. */
private const val NUMBER_SP = 14

/** A panel of a [PlakatWindow]: a [picture] (a preset, a generated composition), a caption and maybe a body. */
@Immutable
public class PlakatWindowPanel(
    public val caption: String,
    public val body: String? = null,
    public val picture: @Composable BoxScope.() -> Unit,
)

/**
 * The window, after ROSTA's: [panels], picture and caption, numbered, [panelsInView] of them side by side (the
 * page's onboarding shows 2 of 4). A new [page], the first panel in view, shifts them across in 200 ms
 * hard-stop (`rail`), and only then do the captions open by a wipe from the left, 120 ms linear; under
 * reduced motion it is all there at once. [large] sets a panel as the whole width of a screen, as the onboarding does.
 */
@Composable
public fun PlakatWindow(
    panels: List<PlakatWindowPanel>,
    page: Int,
    modifier: Modifier = Modifier,
    panelsInView: Int = 1,
    large: Boolean = false,
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val shift = remember { Animatable(page.toFloat()) }
    val words = remember { Animatable(1f) }
    var seen by remember { mutableStateOf(page) }
    LaunchedEffect(page, reduce) {
        val changed = page != seen
        if (changed) events.onEvent(PlakatEvent.Rail)
        seen = page
        val target = page.toFloat()
        if (reduce) {
            shift.snapTo(target)
            words.snapTo(1f)
        } else {
            // The words go at once, as the page's `capIn: false` with no transition, and return after the stop.
            if (changed) words.snapTo(0f)
            coroutineScope {
                launch { shift.animateTo(target, tween(PlakatDurations.NORMAL, easing = PlakatEasings.HardStop)) }
                launch {
                    words.animateTo(
                        1f,
                        tween(PlakatDurations.QUICK, PlakatDurations.NORMAL, PlakatEasings.Linear),
                    )
                }
            }
        }
    }
    val m = if (large) Large else Compact
    val number = type.displayL.style.copy(fontSize = NUMBER_SP.sp, color = colors.ink)
    val display = number.copy(fontSize = m.captionSp.sp)
    val text = type.bodyS.style.copy(fontSize = m.bodySp.sp)
    val measurer = rememberTextMeasurer()
    Layout(
        content = {
            panels.forEachIndexed { i, panel ->
                Column(Modifier.padding(horizontal = m.panelPad)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(m.picture)
                            .clipToBounds()
                            .background(colors.concrete),
                    ) {
                        panel.picture(this)
                        BasicText(
                            (i + 1).toString().padStart(2, '0'),
                            Modifier
                                .padding(start = m.numberInset, top = m.numberInset)
                                .background(colors.paper)
                                .padding(horizontal = m.numberPadX, vertical = m.numberPadY),
                            style = number.cssLine(PlakatFaceMetrics.Display),
                            softWrap = false,
                        )
                    }
                    Spacer(Modifier.height(m.gap))
                    CssParagraph(
                        panel.caption,
                        display,
                        m.captionLine,
                        colors.ink,
                        Modifier.plakatWipe(PlakatWipe.Left) { words.value },
                        PlakatFaceMetrics.Display,
                    )
                    if (panel.body != null) {
                        Spacer(Modifier.height(m.gap))
                        CssParagraph(
                            panel.body,
                            text,
                            m.bodyLine,
                            colors.ink,
                            Modifier.widthIn(max = m.bodyWide).plakatWipe(PlakatWipe.Left) { words.value },
                        )
                    }
                }
            }
        },
        modifier = modifier.clipToBounds(),
    ) { measurables, constraints ->
        val inView = panelsInView.coerceAtLeast(1)
        val gap = m.panelGap.roundToPx()
        // Given no limit, a panel is as wide as its words, the picture filling that: `max-content`.
        val panelWidth =
            if (constraints.hasBoundedWidth) {
                (constraints.maxWidth - gap * (inView - 1)) / inView
            } else {
                val bodyWide = if (m.bodyWide == Dp.Infinity) Int.MAX_VALUE else m.bodyWide.roundToPx()
                val wordsWide =
                    panels.maxOfOrNull { panel ->
                        val caption = measurer.measure(panel.caption, display.copy(lineHeight = TextUnit.Unspecified))
                        val body =
                            panel.body?.let {
                                measurer.measure(
                                    it,
                                    text.copy(lineHeight = TextUnit.Unspecified),
                                )
                            }
                        max(caption.size.width, min(body?.size?.width ?: 0, bodyWide))
                    } ?: 0
                wordsWide + m.panelPad.roundToPx() * 2
            }
        val width =
            if (constraints.hasBoundedWidth) {
                constraints.maxWidth
            } else {
                (panelWidth * inView + gap * (inView - 1)).coerceAtLeast(constraints.minWidth)
            }
        val placeables = measurables.map { it.measure(Constraints(minWidth = panelWidth, maxWidth = panelWidth)) }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        val step = panelWidth + gap
        layout(width, height) {
            placeables.forEachIndexed { i, p -> p.placeRelative(((i - shift.value) * step).roundToInt(), 0) }
        }
    }
}
