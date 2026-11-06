package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.print.RaskatOptions
import io.github.youndie.plakat.print.raskatField
import io.github.youndie.plakat.print.raskatImage
import io.github.youndie.plakat.print.raskatInkB
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatLines
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.tan

/** The block: `padding: 8px 0; gap: 12px`. */
private val BlockPadding = 8.dp
private val BlockGap = 12.dp

/** The band: `height: 120px` on concrete, an ink bar of 8 along its foot, a circle of 24 about (right − 58, 40). */
private val BandHeight = 120.dp
private val BandBar = PlakatLines.Bar
private val CircleRadius = 24.dp
private val CircleFromRight = 58.dp
private val CircleY = 40.dp

/** The wedge's rise: `T = tan(17°)`, across the whole band, and the page's `drawRaskat` options. */
private val WedgeRise = tan(17 * PI / 180).toFloat()
private const val RASKAT_ANGLE = -107.0
private const val RASKAT_T0 = .5
private const val RASKAT_HARD = 10.0
private const val RASKAT_SEED = 47
private const val RASKAT_STREAK = .5

/** The page's canvas is 2×; a raskat never prints coarser than that, and a denser screen prints at its own pixels. */
private const val RASKAT_LEAST_DPR = 2f

/** The words: `font-size: 20px; line-height: 1.1`, `font-size: 15px; line-height: 1.4`. */
private const val TITLE_SP = 20
private const val TITLE_LINE = 1.1f
private const val BODY_SP = 15
private const val BODY_LINE = 1.4f

/** The slogan: `width: 200px`, its word `font-size: 14px` with no tracking. */
private val ButtonWidth = 200.dp
private const val BUTTON_SP = 14

/**
 * An empty state, a call and not an apology: a band with the screen's one raskat wedge rising at −17°, red into concrete,
 * under an ink bar and a circle; then [title], [body] and one slogan, [action], which runs [onAction].
 */
@Composable
public fun PlakatEmptyState(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val type = PlakatTheme.typography
    Column(
        modifier.maxContentWidth().padding(vertical = BlockPadding),
        verticalArrangement = Arrangement.spacedBy(BlockGap),
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(BandHeight)
                // Past 392 wide the wedge's rise is more than the band's height, and it is cut there.
                .clipToBounds()
                .drawWithCache {
                    val rise = size.width * WedgeRise
                    val dpr = max(density, RASKAT_LEAST_DPR)
                    val w = (size.width / density * dpr).roundToInt().coerceAtLeast(1)
                    val h = (rise / density * dpr).roundToInt().coerceAtLeast(1)
                    val field =
                        raskatField(
                            w,
                            h,
                            RaskatOptions(
                                angle = RASKAT_ANGLE,
                                seed = RASKAT_SEED,
                                streak = RASKAT_STREAK,
                                dpr = dpr.toDouble(),
                            ),
                        )
                    val image =
                        raskatImage(w, h, raskatInkB(field, RASKAT_T0, RASKAT_HARD), colors.red, colors.concrete)
                    val wedge =
                        Path().apply {
                            moveTo(0f, size.height)
                            lineTo(size.width, size.height - rise)
                            lineTo(size.width, size.height)
                            close()
                        }
                    onDrawBehind {
                        drawRect(colors.concrete)
                        clipPath(wedge) {
                            drawImage(
                                image,
                                dstOffset = IntOffset(0, (size.height - rise).roundToInt()),
                                dstSize = IntSize(size.width.roundToInt(), rise.roundToInt()),
                                filterQuality = FilterQuality.Low,
                            )
                        }
                        val bar = BandBar.toPx()
                        drawRect(colors.ink, Offset(0f, size.height - bar), Size(size.width, bar))
                        drawCircle(
                            colors.ink,
                            CircleRadius.toPx(),
                            Offset(size.width - CircleFromRight.toPx(), CircleY.toPx()),
                        )
                    }
                },
        )
        CssParagraph(
            title,
            type.displayL.style.copy(fontSize = TITLE_SP.sp),
            TITLE_LINE,
            colors.ink,
            Modifier.semantics { heading() },
            PlakatFaceMetrics.Display,
        )
        CssParagraph(body, type.bodyS.style.copy(fontSize = BODY_SP.sp), BODY_LINE, colors.ink)
        PlakatButtonFrame(
            PlakatButtonKind.Filled,
            action,
            onAction,
            Modifier.width(ButtonWidth),
            enabled = true,
            loading = false,
            interactionSource = null,
            metrics = ButtonMetrics(labelSize = BUTTON_SP.sp, tracked = false),
        )
    }
}
