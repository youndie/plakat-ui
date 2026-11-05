package io.github.youndie.plakat.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.VertexMode
import androidx.compose.ui.graphics.Vertices
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.agit.WARP_V_MAX
import io.github.youndie.plakat.agit.WARP_V_MIN
import io.github.youndie.plakat.agit.WarpAnchor
import io.github.youndie.plakat.agit.WarpBox
import io.github.youndie.plakat.agit.WarpCone
import io.github.youndie.plakat.agit.WarpMap
import io.github.youndie.plakat.agit.WarpMesh
import io.github.youndie.plakat.agit.WarpPoint
import io.github.youndie.plakat.agit.warpBox
import io.github.youndie.plakat.agit.warpColumn
import io.github.youndie.plakat.agit.warpCone
import io.github.youndie.plakat.agit.warpGrid
import io.github.youndie.plakat.agit.warpHorn
import io.github.youndie.plakat.agit.warpLineMap
import io.github.youndie.plakat.agit.warpLineV
import io.github.youndie.plakat.agit.warpMesh
import io.github.youndie.plakat.agit.warpSphere
import io.github.youndie.plakat.agit.warpStairs
import io.github.youndie.plakat.print.PlakatWipe
import io.github.youndie.plakat.print.plakatWipe
import io.github.youndie.plakat.theme.PlakatDisplayInk
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatFaceMetrics
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.theme.cssLine
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatDurations
import io.github.youndie.plakat.tokens.PlakatEasings
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/** `textSource`'s `F`: the size a line is rasterised at, here per unit of density, up to 3. */
private const val SOURCE_PX = 240
private const val SOURCE_DENSITY_MOST = 3f

/** The picture is made at twice the pixels and halved, as the page's canvas of 2× is by the browser. */
private const val BAKE_SCALE = 2

/** Rule P1: up to three lines to a form. */
private const val LINES_MOST = 3

/** Rule P2: the opening. */
private const val SPREAD_MOST = 30f

/** Rule P3: the arc of the text about a column. */
private const val COLUMN_ARC_MOST = 70f

/** Rule P4: the share of the letters that may go onto a lid. */
private const val LID_MOST = .2f

/** `paint`'s margin of a mouth from its cone's box. */
private const val CONE_END_PAD = .06

/** The axis of a form, rule P2: 0°, −17° or −90°. Isometry would be a second axis. */
public enum class PlakatProjectionAxis(
    internal val degrees: Double,
) {
    Level(0.0),
    Tilted(-17.0),
    Upright(-90.0),
}

/** What lies on a horn's axis: its lower edge, its upper edge, or its middle, opening equally. */
public enum class PlakatHornAnchor {
    Floor,
    Ceiling,
    Axis,
}

/** The edge a box's text breaks over: the side, or the top. */
public enum class PlakatBoxFold {
    Side,
    Top,
}

/**
 * The form a [PlakatProjection]'s text lies on, in the projection's own box, top left at 0, 0. Faces are flat inks, no
 * shadow and no gradient (rule P4). Rules the design states and that a value can break are refused at construction.
 */
@Immutable
public sealed class PlakatProjectionForm {
    internal abstract fun scene(aspects: List<Double>): ProjectionScene

    /**
     * Рупор: a plane in perspective, [narrow] at ([x], [y]) on its axis and opening by [spread] degrees along the
     * [length], read from the narrow end. [perspective] goes from an even trapezium, 0, to true perspective, 1. The
     * design asks for a narrow end of at least 24, though its own upright cell, fitted to its box, comes to 20.
     */
    public data class Horn(
        val x: Dp,
        val y: Dp,
        val length: Dp,
        val narrow: Dp,
        val spread: Float = 17f,
        val axis: PlakatProjectionAxis = PlakatProjectionAxis.Level,
        val anchor: PlakatHornAnchor = PlakatHornAnchor.Floor,
        val perspective: Float = 1f,
    ) : PlakatProjectionForm() {
        init {
            require(spread in 0f..SPREAD_MOST) { "A horn opens by at most $SPREAD_MOST degrees (rule P2): $spread" }
        }

        private fun warp(
            ox: Double = x.value.toDouble(),
            oy: Double = y.value.toDouble(),
        ) = warpHorn(
            ox,
            oy,
            length.value.toDouble(),
            narrow.value.toDouble(),
            spread.toDouble(),
            axis.degrees,
            when (anchor) {
                PlakatHornAnchor.Floor -> WarpAnchor.Floor
                PlakatHornAnchor.Ceiling -> WarpAnchor.Ceiling
                PlakatHornAnchor.Axis -> WarpAnchor.Axis
            },
            perspective.toDouble(),
        )

        /**
         * This horn fitted to [box], as the page's `fitHorn`: scaled down if it must be and never up, centred across, and
         * placed in the box's height by [vertical].
         */
        public fun fittedIn(
            box: DpRect,
            vertical: Alignment.Vertical = Alignment.CenterVertically,
        ): Horn {
            val q = warp(0.0, 0.0).quad
            val fit = Fit(q, box, vertical)
            return copy(
                x = fit.ox.dp,
                y = fit.oy.dp,
                length = length * fit.k.toFloat(),
                narrow = narrow * fit.k.toFloat(),
            )
        }

        override fun scene(aspects: List<Double>): ProjectionScene {
            val horn = warp()
            return ProjectionScene(
                behind = { c -> drawPath(polygon(horn.quad), c.red) },
                lines = linesOn(horn.map, aspects, along = 1.0, nu = 48, nv = 8),
            )
        }
    }

    /**
     * Конус: a horn with volume, the axis from ([x], [y]) along [length], the radius from [startRadius] to [endRadius]
     * (equal, a shaft). [yaw] turns it to the viewer and [arc] is the text's about the axis. Its mouth is ink.
     */
    public data class Cone(
        val x: Dp,
        val y: Dp,
        val length: Dp,
        val startRadius: Dp,
        val endRadius: Dp,
        val yaw: Float = 24f,
        val arc: Float = 60f,
        val axis: PlakatProjectionAxis = PlakatProjectionAxis.Level,
    ) : PlakatProjectionForm() {
        private fun warp(
            ox: Double = x.value.toDouble(),
            oy: Double = y.value.toDouble(),
        ) = warpCone(
            ox,
            oy,
            length.value.toDouble(),
            startRadius.value.toDouble(),
            endRadius.value.toDouble(),
            yaw.toDouble(),
            arc.toDouble(),
            axis.degrees,
            padStart = CONE_END_PAD,
        )

        /** This cone fitted to [box], as the page's `fitCone`: scaled down if it must be and never up, and centred. */
        public fun fittedIn(box: DpRect): Cone {
            val c = warp(0.0, 0.0)
            val r0 = c.r0
            val r1 = c.r1
            val pts =
                listOf(
                    c.rot(0.0, -r0),
                    c.rot(c.length, -r1),
                    c.rot(c.length, r1),
                    c.rot(0.0, r0),
                    c.rot(c.length + r1 * c.yaw, 0.0),
                    c.rot(-r0 * c.yaw, 0.0),
                )
            val fit = Fit(pts, box, Alignment.CenterVertically)
            return copy(
                x = fit.ox.dp,
                y = fit.oy.dp,
                length = length * fit.k.toFloat(),
                startRadius = startRadius * fit.k.toFloat(),
                endRadius = endRadius * fit.k.toFloat(),
            )
        }

        override fun scene(aspects: List<Double>): ProjectionScene {
            val cone = warp()
            return ProjectionScene(
                behind = { c -> drawConeBody(cone, c.red) },
                lines = linesOn(cone.map, aspects, along = 1.0, nu = 48, nv = 10),
                over = { c -> drawConeMouth(cone, c.ink) },
            )
        }
    }

    /**
     * Колонна: an upright cylinder of [radius] about [centerX] from [top] to [bottom], seen from above by [tilt], the text
     * in the band from [bandTop] to [bandBottom] by [arc] degrees each way, turned about the axis by [spin]. Its back is
     * not drawn.
     */
    public data class Column(
        val centerX: Dp,
        val radius: Dp,
        val top: Dp,
        val bottom: Dp,
        val bandTop: Dp,
        val bandBottom: Dp,
        val arc: Float = COLUMN_ARC_MOST,
        val tilt: Float = .16f,
        val spin: Float = 0f,
    ) : PlakatProjectionForm() {
        init {
            require(
                arc <= COLUMN_ARC_MOST,
            ) { "The text goes at most $COLUMN_ARC_MOST degrees round a column (rule P3): $arc" }
        }

        override fun scene(aspects: List<Double>): ProjectionScene {
            val column =
                warpColumn(
                    centerX.value.toDouble(),
                    radius.value.toDouble(),
                    top.value.toDouble(),
                    bottom.value.toDouble(),
                    bandTop.value.toDouble(),
                    bandBottom.value.toDouble(),
                    arc.toDouble(),
                    tilt.toDouble(),
                    spin.toDouble(),
                )
            val cx = column.cx.toFloat()
            val r = column.r.toFloat()
            val b = column.squash.toFloat()
            val t = column.top.toFloat()
            val f = column.bottom.toFloat()
            return ProjectionScene(
                behind = { c ->
                    val body =
                        Path().apply {
                            moveTo(cx - r, t)
                            lineTo(cx - r, f)
                            arcTo(Rect(cx - r, f - b, cx + r, f + b), 180f, -180f, false)
                            lineTo(cx + r, t)
                            close()
                        }
                    drawPath(body, c.red)
                    drawOval(c.concrete, Offset(cx - r, t - b), Size(2 * r, 2 * b))
                },
                lines = linesOn(column.map, aspects, along = .5, nu = 60, nv = 8),
            )
        }
    }

    /** Шар: a sphere with the text as a belt on its equator, [longitude] and [latitude] degrees each way, seen from above by [tilt]. */
    public data class Sphere(
        val centerX: Dp,
        val centerY: Dp,
        val radius: Dp,
        val longitude: Float = 70f,
        val latitude: Float = 22f,
        val tilt: Float = 14f,
    ) : PlakatProjectionForm() {
        override fun scene(aspects: List<Double>): ProjectionScene {
            val map =
                warpSphere(
                    centerX.value.toDouble(),
                    centerY.value.toDouble(),
                    radius.value.toDouble(),
                    longitude.toDouble(),
                    latitude.toDouble(),
                    tilt.toDouble(),
                )
            return ProjectionScene(
                behind = { c -> drawCircle(c.red, radius.value, Offset(centerX.value, centerY.value)) },
                lines = linesOn(map, aspects, along = .5, nu = 64, nv = 10),
            )
        }
    }

    /**
     * Брус: an oblique projection of a box of [width] × [height] at ([x], [y]), the [depth] along −17°, the text in the band
     * from [bandTop] to [bandBottom]. It breaks over the side, [at] being the share of it before the edge, or over the
     * top, the share going onto the lid, at most 20 % of the letters, and the lid turning ink so that they read.
     */
    public data class Box(
        val x: Dp,
        val y: Dp,
        val width: Dp,
        val height: Dp,
        val depth: Dp,
        val bandTop: Dp,
        val bandBottom: Dp,
        val fold: PlakatBoxFold = PlakatBoxFold.Side,
        val at: Float? = null,
    ) : PlakatProjectionForm() {
        init {
            if (fold == PlakatBoxFold.Top && at != null) {
                require(at <= LID_MOST) { "At most $LID_MOST of the letters go onto a lid (rule P4): $at" }
            }
        }

        override fun scene(aspects: List<Double>): ProjectionScene {
            val lid = fold == PlakatBoxFold.Top
            val box =
                warpBox(
                    x.value.toDouble(),
                    y.value.toDouble(),
                    width.value.toDouble(),
                    height.value.toDouble(),
                    depth.value.toDouble(),
                    bandTop.value.toDouble(),
                    bandBottom.value.toDouble(),
                    lid,
                    (at ?: if (lid) LID_MOST else null)?.toDouble(),
                )
            return ProjectionScene(
                behind = { c -> drawBoxFaces(box, c.red, c.ink, if (lid) c.ink else c.concrete) },
                lines = linesOn(box.map, aspects, along = 0.0, nu = 48, nv = 8, folds = box.folds, vfolds = box.vfolds),
            )
        }
    }

    /**
     * Лестница: [steps] steps of [width] × [riser] rising from ([x], [y]) and going back by [depth] along −17°, a line to
     * a riser, from the top down as it reads. Where there are more lines than steps the last step takes the rest.
     */
    public data class Stairs(
        val x: Dp,
        val y: Dp,
        val steps: Int,
        val width: Dp,
        val riser: Dp,
        val depth: Dp,
    ) : PlakatProjectionForm() {
        override fun scene(aspects: List<Double>): ProjectionScene {
            val stairs =
                warpStairs(
                    x.value.toDouble(),
                    y.value.toDouble(),
                    steps,
                    width.value.toDouble(),
                    riser.value.toDouble(),
                    depth.value.toDouble(),
                )
            return ProjectionScene(
                behind = { c ->
                    for (i in stairs.risers.indices.reversed()) {
                        val r = stairs.risers[i]
                        val x0 = r.x.toFloat()
                        val y0 = r.y.toFloat()
                        val w = r.w.toFloat()
                        val h = r.h.toFloat()
                        val dx = stairs.dx.toFloat()
                        val dy = stairs.dy.toFloat()
                        drawPath(polygon(x0, y0, x0 + dx, y0 + dy, x0 + w + dx, y0 + dy, x0 + w, y0), c.concrete)
                        val foot = y0 + h * (i + 1)
                        drawPath(polygon(x0 + w, y0, x0 + w + dx, y0 + dy, x0 + w + dx, foot + dy, x0 + w, foot), c.ink)
                        drawRect(c.red, Offset(x0, y0), Size(w, h))
                    }
                },
                lines = aspects.take(stairs.maps.size).mapIndexed { i, _ -> ProjectionLine(i, stairs.maps[i], 24, 6) },
            )
        }

        internal override val joinsTheRest: Int? get() = steps
    }

    /** How many lines a form takes, the last one the rest of them joined; null for any, up to the rule's three. */
    internal open val joinsTheRest: Int? get() = null
}

/** A form's fit to a box: `fitHorn` and `fitCone`, built at 0, measured, scaled and placed. */
private class Fit(
    points: List<WarpPoint>,
    box: DpRect,
    vertical: Alignment.Vertical,
) {
    val k: Double
    val ox: Float
    val oy: Float

    init {
        val minX = points.minOf { it.x }
        val minY = points.minOf { it.y }
        val bw = points.maxOf { it.x } - minX
        val bh = points.maxOf { it.y } - minY
        val w = (box.right - box.left).value
        val h = (box.bottom - box.top).value
        k = min(1.0, min(w / bw, h / bh))
        ox = (box.left.value + (w - bw * k) / 2 - minX * k).toFloat()
        oy =
            when (vertical) {
                Alignment.Bottom -> box.bottom.value - points.maxOf { it.y } * k
                Alignment.Top -> box.top.value - minY * k
                else -> box.top.value + (h - bh * k) / 2 - minY * k
            }.toFloat()
    }
}

/** A line on a form: which of the lines, where it falls, and `warp`'s options. */
internal class ProjectionLine(
    val index: Int,
    val map: WarpMap,
    val nu: Int,
    val nv: Int,
    val folds: List<Double> = emptyList(),
    val vfolds: List<Double> = emptyList(),
)

/** What a form draws: its faces, the lines over them, and what goes over the lines. */
internal class ProjectionScene(
    val behind: DrawScope.(PlakatColors) -> Unit,
    val lines: List<ProjectionLine>,
    val over: DrawScope.(PlakatColors) -> Unit = {},
)

/**
 * The page's `warpLines`: each line takes its own band, and a length by its natural width, the longest the whole form.
 * Short lines lie [along] the form, 0 at its start, 1 at its end.
 */
private fun linesOn(
    map: WarpMap,
    aspects: List<Double>,
    along: Double,
    nu: Int,
    nv: Int,
    folds: List<Double> = emptyList(),
    vfolds: List<Double> = emptyList(),
): List<ProjectionLine> {
    val n = aspects.size
    val longest = aspects.max()
    return aspects.mapIndexed { i, aspect ->
        val k = aspect / longest
        val o = (1 - k) * along
        val line = if (n > 1) warpLineMap(map, i, n) else map
        ProjectionLine(
            i,
            if (n > 1) WarpMap { u, v -> line.at(o + u * k, v) } else line,
            nu,
            nv,
            folds.map { (it - o) / k }.filter { it > 0 && it < 1 },
            if (n > 1) vfolds.map { warpLineV(it, i, n) } else vfolds,
        )
    }
}

private fun polygon(points: List<WarpPoint>) =
    Path().apply {
        moveTo(points[0].x.toFloat(), points[0].y.toFloat())
        for (p in points.drop(1)) lineTo(p.x.toFloat(), p.y.toFloat())
        close()
    }

private fun polygon(vararg xy: Float) =
    Path().apply {
        moveTo(xy[0], xy[1])
        for (i in 2 until xy.size step 2) lineTo(xy[i], xy[i + 1])
        close()
    }

/** `drawBox`: the top, the side, then the front over their edges. */
private fun DrawScope.drawBoxFaces(
    b: WarpBox,
    front: Color,
    side: Color,
    top: Color,
) {
    val x = b.x.toFloat()
    val y = b.y.toFloat()
    val w = b.w.toFloat()
    val h = b.h.toFloat()
    val dx = b.dx.toFloat()
    val dy = b.dy.toFloat()
    drawPath(polygon(x, y, x + dx, y + dy, x + w + dx, y + dy, x + w, y), top)
    drawPath(polygon(x + w, y, x + w + dx, y + dy, x + w + dx, y + h + dy, x + w, y + h), side)
    drawRect(front, Offset(x, y), Size(w, h))
}

/** The ellipse's half-width: `Math.max(.5, r * s)`, so a cone seen side on still has an end. */
private const val END_LEAST = .5f

/** `drawCone`'s body: the two edges, and the near half of the narrow end's ellipse. */
private fun DrawScope.drawConeBody(
    c: WarpCone,
    fill: Color,
) {
    val q = listOf(c.rot(0.0, -c.r0), c.rot(c.length, -c.r1), c.rot(c.length, c.r1), c.rot(0.0, c.r0))
    drawPath(polygon(q), fill)
    val at = c.rot(0.0, 0.0)
    val r = c.r0.toFloat()
    val w = max(END_LEAST, (c.r0 * c.yaw).toFloat())
    val end = Path().apply { arcTo(Rect(-w, -r, w, r), -90f, -180f, true) }
    translate(at.x.toFloat(), at.y.toFloat()) {
        rotate(c.angle.toFloat(), Offset.Zero) { drawPath(end, fill) }
    }
}

/** `drawCone`'s mouth: the whole ellipse at the far end, over the text. */
private fun DrawScope.drawConeMouth(
    c: WarpCone,
    fill: Color,
) {
    val at = c.rot(c.length, 0.0)
    val r = c.r1.toFloat()
    val w = max(END_LEAST, (c.r1 * c.yaw).toFloat())
    translate(at.x.toFloat(), at.y.toFloat()) {
        rotate(c.angle.toFloat(), Offset.Zero) { drawOval(fill, Offset(-w, -r), Size(2 * w, 2 * r)) }
    }
}

/** A line rasterised once: `textSource`, and its natural width over the height of its caps. */
private class ProjectionSource(
    val image: ImageBitmap,
) {
    val w get() = image.width
    val h get() = image.height
}

/** `textSource`'s measure, from the ink a canvas would report: the natural width and the height, at [px] to the em. */
private fun sourceSize(
    text: String,
    px: Int,
): IntSize {
    val ink = stretchInk(text)
    val cap = PlakatDisplayInk.CAP_HEIGHT / 1000.0 * px
    val w = if (ink == null) 2 else ceil((ink.right - ink.left) * px.toDouble()).toInt() + 2
    return IntSize(w, ceil(cap * (WARP_V_MAX - WARP_V_MIN)).toInt())
}

private fun aspectOf(text: String): Double =
    sourceSize(text, SOURCE_PX).width / (PlakatDisplayInk.CAP_HEIGHT / 1000.0 * SOURCE_PX)

private fun rasterise(
    text: String,
    px: Int,
    color: Color,
    measurer: TextMeasurer,
    font: TextStyle,
): ProjectionSource {
    val size = sourceSize(text, px)
    val image = ImageBitmap(size.width, size.height)
    val ink = stretchInk(text) ?: return ProjectionSource(image)
    val face = PlakatFaceMetrics.Display
    val layout =
        measurer.measure(
            text,
            font.copy(fontSize = px.sp).cssLine(face),
            softWrap = false,
            maxLines = 1,
            density = Density(1f),
        )
    val cap = PlakatDisplayInk.CAP_HEIGHT / 1000.0 * px
    val baseline = cap * (1 - WARP_V_MIN)
    CanvasDrawScope().draw(
        Density(1f),
        LayoutDirection.Ltr,
        Canvas(image),
        Size(size.width.toFloat(), size.height.toFloat()),
    ) {
        drawText(
            layout,
            color,
            Offset(1 - ink.left * px, (baseline - round(face.ascent * px)).toFloat()),
        )
    }
    return ProjectionSource(image)
}

private fun Canvas.drawMesh(
    mesh: WarpMesh,
    source: ProjectionSource,
) {
    val paint = Paint().apply { shader = ImageShader(source.image) }
    drawVertices(
        Vertices(
            VertexMode.Triangles,
            mesh.positions.map { Offset(it.x.toFloat(), it.y.toFloat()) },
            mesh.texture.map { Offset(it.x.toFloat(), it.y.toFloat()) },
            List(mesh.positions.size) { Color.White },
            mesh.indices,
        ),
        BlendMode.Modulate,
        paint,
    )
}

/**
 * The picture, once per text, form and size: at twice the pixels on the CPU, then halved. `drawVertices` on a bitmap's
 * canvas is the same call on every target, where on Android's hardware canvas it needs API 29, and where Skiko's image
 * shader samples by the nearest pixel and Android's by its paint, twice the pixels leaves the difference under a pixel.
 */
private fun bakeProjection(
    lines: List<String>,
    form: PlakatProjectionForm,
    size: IntSize,
    density: Float,
    colors: PlakatColors,
    measurer: TextMeasurer,
    font: TextStyle,
): ImageBitmap {
    val px = SOURCE_PX * ceil(density).coerceIn(1f, SOURCE_DENSITY_MOST).toInt()
    val scene = form.scene(lines.map(::aspectOf))
    val sources = lines.map { rasterise(it, px, colors.paper, measurer, font) }
    val big = ImageBitmap(size.width * BAKE_SCALE, size.height * BAKE_SCALE)
    val canvas = Canvas(big)
    CanvasDrawScope().draw(
        Density(density),
        LayoutDirection.Ltr,
        canvas,
        Size(big.width.toFloat(), big.height.toFloat()),
    ) {
        scale(density * BAKE_SCALE, pivot = Offset.Zero) {
            scene.behind(this, colors)
            for (line in scene.lines) {
                val source = sources[line.index]
                val grid = warpGrid(line.map, line.nu, line.nv, line.folds, line.vfolds)
                drawContext.canvas.drawMesh(warpMesh(grid, source.w, source.h), source)
            }
            scene.over(this, colors)
        }
    }
    val out = ImageBitmap(size.width, size.height)
    Canvas(out).drawImageRect(
        big,
        IntOffset.Zero,
        IntSize(big.width, big.height),
        IntOffset.Zero,
        size,
        Paint().apply { filterQuality = FilterQuality.Low },
    )
    return out
}

/**
 * [lines] of text as the surface of a flat-faced [form]: rasterised once and meshed onto it, in the paper's colour on
 * the form's red. For a poster or a splash, one to a screen, static: it appears by a wipe from the left, 120 ms linear,
 * at once under reduced motion, and is never scaled. The picture is rebuilt only when the text, the form or the size
 * change. It is read as a heading of the lines' words. Its box is the caller's, and a form stands in it by its own
 * numbers, as the page's cells do.
 *
 * @param print false shows it printed, for a projection that scrolls into view and need not appear again.
 * @throws IllegalArgumentException if there is no line, or more than three (rule P1); a [PlakatProjectionForm.Stairs]
 *   takes one to a step.
 */
@Composable
public fun PlakatProjection(
    lines: List<String>,
    form: PlakatProjectionForm,
    modifier: Modifier = Modifier,
    print: Boolean = true,
) {
    PlakatProjectionFrame(lines, form, modifier, print)
}

@Composable
internal fun PlakatProjectionFrame(
    lines: List<String>,
    form: PlakatProjectionForm,
    modifier: Modifier,
    print: Boolean,
    onBake: () -> Unit = {},
) {
    val caps = lines.map { it.uppercase() }.filter { it.isNotBlank() }
    val limit = form.joinsTheRest
    require(caps.isNotEmpty() && (limit != null || caps.size <= LINES_MOST)) {
        "A projection takes one to $LINES_MOST lines (rule P1): $lines"
    }
    val shown =
        if (limit != null && caps.size > limit) {
            caps.take(limit - 1) + caps.drop(limit - 1).joinToString(" ")
        } else {
            caps
        }
    val colors = PlakatTheme.colors
    val reduce = PlakatTheme.reduceMotion
    val events by rememberUpdatedState(PlakatTheme.events)
    val font = PlakatTheme.typography.displayL.style
    val measurer = rememberTextMeasurer()
    val hook by rememberUpdatedState(onBake)
    val words = remember(shown) { shown.joinToString(" ") }
    val wipe = remember { Animatable(if (print && !reduce) 0f else 1f) }
    LaunchedEffect(words, form, print, reduce) {
        if (!print || reduce) {
            wipe.snapTo(1f)
        } else {
            events.onEvent(PlakatEvent.Print)
            wipe.snapTo(0f)
            wipe.animateTo(1f, tween(PlakatDurations.QUICK, easing = PlakatEasings.Linear))
        }
    }
    val picture =
        remember(shown, form, colors, font, measurer) {
            Modifier.drawWithCache {
                val width = size.width.toInt()
                val height = size.height.toInt()
                if (width <= 0 || height <= 0) return@drawWithCache onDrawBehind {}
                hook()
                val image = bakeProjection(shown, form, IntSize(width, height), density, colors, measurer, font)
                onDrawBehind { drawImage(image) }
            }
        }
    Spacer(
        modifier
            .semantics {
                heading()
                contentDescription = words
            }.plakatWipe(PlakatWipe.Left) { wipe.value }
            .then(picture),
    )
}
