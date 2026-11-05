package io.github.youndie.plakat.agit

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.print.RaskatOptions
import io.github.youndie.plakat.print.drawRaskat
import io.github.youndie.plakat.print.raskatField
import io.github.youndie.plakat.print.raskatImage
import io.github.youndie.plakat.print.raskatInkB
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatColors
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/** The rapport's grid: `rapport(seed, cell = 24)`. */
private val RapportCell = 24.dp

/** The skeleton: `6 шагов, цикл 1200 мс`, a step being a sixth of a cell. */
private const val SKELETON_CYCLE_MS = 1200L
private const val SKELETON_STEPS = 6

/** What a [role] prints in, in this palette. */
private fun PlakatColors.of(role: PlakatRole) =
    when (role) {
        PlakatRole.Red -> red
        PlakatRole.Ink -> ink
        PlakatRole.Concrete -> concrete
        PlakatRole.Paper -> paper
    }

/** Draws [shape] as its SVG figure is drawn, 1 unit = 1 here, so the caller scales. */
private fun DrawScope.drawFigure(
    shape: PlakatShape,
    colors: PlakatColors,
) {
    val color = colors.of(shape.role)
    when (shape) {
        is PlakatShape.Rect -> {
            rotate(shape.rot.toFloat(), Offset(shape.ox.toFloat(), shape.oy.toFloat())) {
                drawRect(
                    color,
                    Offset(shape.x.toFloat(), shape.y.toFloat()),
                    Size(shape.w.toFloat(), shape.h.toFloat()),
                )
            }
        }

        is PlakatShape.Circle -> {
            drawCircle(color, shape.r.toFloat(), Offset(shape.cx.toFloat(), shape.cy.toFloat()))
        }

        is PlakatShape.Poly -> {
            drawPath(
                Path().apply {
                    shape.points.forEachIndexed { i, (x, y) ->
                        if (i == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
                    }
                    close()
                },
                color,
            )
        }
    }
}

private fun Modifier.picture(contentDescription: String?) =
    if (contentDescription == null) {
        this
    } else {
        semantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    }

/** On paper, [shapes] in a frame [frameW] × [frameH] dp that covers the box from its middle. */
@Composable
private fun Frame(
    shapes: List<PlakatShape>,
    frameW: Dp,
    frameH: Dp,
    modifier: Modifier,
    contentDescription: String?,
    ground: Color = PlakatTheme.colors.paper,
) {
    val colors = PlakatTheme.colors
    // A box and not a spacer: a spacer takes a size only from a fixed constraint, and would draw nothing at the default.
    Box(
        modifier
            .defaultMinSize(frameW, frameH)
            .picture(contentDescription)
            .clipToBounds()
            .drawBehind {
                drawRect(ground)
                val w = frameW.toPx()
                val h = frameH.toPx()
                val s = max(size.width / w, size.height / h) * density
                translate((size.width - w * s / density) / 2, (size.height - h * s / density) / 2) {
                    scale(s, s, pivot = Offset.Zero) {
                        for (shape in shapes) drawFigure(shape, colors)
                    }
                }
            },
    )
}

/**
 * A [preset] on paper, in the theme's palette, covering the box its [modifier] gives it, at its own size by default.
 * It speaks only if given a [contentDescription]: most are decoration under a title.
 */
@Composable
public fun PlakatPresetImage(
    preset: PlakatPreset,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Frame(preset.shapes, preset.width, preset.height, modifier, contentDescription)
}

/**
 * A composition generated from the whole vocabulary for [seed], in a frame of [frameWidth] × [frameHeight] on the
 * 8 dp grid, covering the box its [modifier] gives it. Take the seed from the content (an article's id), so the
 * picture stays the same between runs.
 */
@Composable
public fun PlakatGeneratedImage(
    seed: Int,
    modifier: Modifier = Modifier,
    frameWidth: Dp = 360.dp,
    frameHeight: Dp = 240.dp,
    contentDescription: String? = null,
    titleZone: PlakatTitleZone = PlakatTitleZone.Generated,
    ground: Color = PlakatTheme.colors.paper,
) {
    val composition =
        remember(seed, frameWidth, frameHeight, titleZone) {
            generate(
                seed,
                frameWidth.value.toDouble(),
                frameHeight.value.toDouble(),
                head =
                    titleZone.rect?.let {
                        PlakatZone(
                            it.left.value.toDouble(),
                            it.top.value.toDouble(),
                            (
                                it.right -
                                    it.left
                            ).value.toDouble(),
                            (it.bottom - it.top).value.toDouble(),
                        )
                    },
                noHead = titleZone == PlakatTitleZone.None,
            )
        }
    Frame(composition.shapes, frameWidth, frameHeight, modifier, contentDescription, ground)
}

/** What a [PlakatGeneratedImage] keeps clear of ink and red. */
@Immutable
public class PlakatTitleZone private constructor(
    internal val rect: DpRect?,
) {
    public companion object {
        /** The generator's own zone, 16 from the corner: the frame takes a title. */
        public val Generated: PlakatTitleZone = PlakatTitleZone(null)

        /** None: a thumbnail, with no title on it. */
        public val None: PlakatTitleZone = PlakatTitleZone(null)

        /** This [rect] of the frame. */
        public fun at(rect: DpRect): PlakatTitleZone = PlakatTitleZone(rect)
    }
}

/** The tile of [tile], on paper, repeated across the box from [shift] dp along. */
private fun DrawScope.drawRapport(
    tile: PlakatRapport,
    colors: PlakatColors,
    shift: Float,
) {
    drawRect(colors.paper)
    val w = tile.w.toFloat() * density
    val h = tile.h.toFloat() * density
    val first = shift * density - w
    var y = 0f
    while (y < size.height) {
        var x = first
        while (x < size.width) {
            translate(x, y) {
                scale(density, density, pivot = Offset.Zero) {
                    for (shape in tile.shapes) drawFigure(shape, colors)
                }
            }
            x += w
        }
        y += h
    }
}

/**
 * A geometric rapport for [seed], seamless, filling the box its [modifier] gives it: a divider of 24 dp, or a section's
 * ground, on a grid of [cell]. Text goes on a plate of paper, never on the pattern. Take the seed from the content, as for
 * a composition.
 */
@Composable
public fun PlakatRapportFill(
    seed: Int,
    modifier: Modifier = Modifier,
    cell: Dp = RapportCell,
) {
    val colors = PlakatTheme.colors
    val tile = remember(seed, cell) { rapport(seed, cell.value.toDouble()) }
    Spacer(modifier.clipToBounds().drawBehind { drawRapport(tile, colors, 0f) })
}

/** The step of the skeleton's pattern [ms] into loading, a closed form of the clock. */
internal fun skeletonStep(ms: Long): Int =
    floor(ms.toDouble() * SKELETON_STEPS / SKELETON_CYCLE_MS).toInt() % SKELETON_STEPS

/**
 * The loading pattern: a rapport for [seed] that moves along by a cell in six steps every 1200 ms while it is shown,
 * and starts over; under reduced motion it stands.
 */
@Composable
public fun PlakatSkeleton(
    seed: Int,
    modifier: Modifier = Modifier,
) {
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val tile = remember(seed) { rapport(seed, RapportCell.value.toDouble()) }
    var step by remember { mutableIntStateOf(0) }
    LaunchedEffect(reduce) {
        step = 0
        if (reduce) return@LaunchedEffect
        val start = withInfiniteAnimationFrameMillis { it }
        while (true) withInfiniteAnimationFrameMillis { step = skeletonStep(it - start) }
    }
    Spacer(
        modifier
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }
            .clipToBounds()
            .drawBehind { drawRapport(tile, colors, step * RapportCell.value / SKELETON_STEPS) },
    )
}

/** The raskat the Core and the Demo pages print in a circle: its `drawRaskat` options. */
private const val DISC_T0 = .58
private const val DISC_HARD = 12.0
private const val DISC_STREAK = .5

/**
 * A disc of raskat: two inks on one roller, red into paper, every pixel one or the other, filling the box its [modifier]
 * gives it. A round picture for a poster or a splash, one to a screen.
 */
@Composable
public fun PlakatRaskatDisc(modifier: Modifier = Modifier) {
    val colors = PlakatTheme.colors
    Spacer(
        modifier.drawBehind {
            val px = size.minDimension.roundToInt()
            if (px <= 0) return@drawBehind
            val field = raskatField(px, px, RaskatOptions(streak = DISC_STREAK, dpr = density.toDouble()))
            val image = raskatImage(px, px, raskatInkB(field, DISC_T0, DISC_HARD), colors.red, colors.paper)
            drawRaskat(
                image,
                Path().apply { addOval(Rect(Offset.Zero, Size(px.toFloat(), px.toFloat()))) },
                IntOffset.Zero,
            )
        },
    )
}
