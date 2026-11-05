package io.github.youndie.plakat.agit

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/** `VMIN`, `VMAX`: v = 0 is the top of the caps and v = 1 the baseline, with room over them for Й and Ё and under for Д, Ц, Щ. */
internal const val WARP_V_MIN = -0.32
internal const val WARP_V_MAX = 1.22

/** `LINE_GAP`: a line's band leaves room for those marks, gap / (1 − gap) ≥ −VMIN + (VMAX − 1) = 0.54. */
private const val LINE_GAP = .36

/** `warp`'s `nu` and `nv` when not asked; `nv` is raised to 16 whatever is asked, so the page's «40 × 8» is 40 × 16. */
private const val WARP_NU = 40
private const val WARP_NV = 8
private const val WARP_NV_LEAST = 16

/** `vfolds` already in the grid to within this are not added. */
private const val V_SAME = 1e-6

/** A point on the form, in the canvas's px, here dp. */
internal class WarpPoint(
    val x: Double,
    val y: Double,
)

/** `map(u, v) → [x, y] | null`: where a point of the text falls on the form, or null for one it does not show. */
internal fun interface WarpMap {
    fun at(
        u: Double,
        v: Double,
    ): WarpPoint?
}

private fun radians(degrees: Double) = degrees * (PI / 180)

private fun lerp(
    p: WarpPoint,
    q: WarpPoint,
    t: Double,
) = WarpPoint(p.x + (q.x - p.x) * t, p.y + (q.y - p.y) * t)

/** `rot`: turning about the origin ([ox], [oy]) by [degrees]. */
private fun rotation(
    ox: Double,
    oy: Double,
    degrees: Double,
): (Double, Double) -> WarpPoint {
    val c = cos(radians(degrees))
    val n = sin(radians(degrees))
    return { x, y -> WarpPoint(ox + x * c - y * n, oy + x * n + y * c) }
}

/** `quadMap`: the homography of the unit square onto a quadrilateral, top left, top right, bottom right, bottom left. */
internal fun warpQuad(
    p0: WarpPoint,
    p1: WarpPoint,
    p2: WarpPoint,
    p3: WarpPoint,
): WarpMap {
    val dx1 = p1.x - p2.x
    val dx2 = p3.x - p2.x
    val dx3 = p0.x - p1.x + p2.x - p3.x
    val dy1 = p1.y - p2.y
    val dy2 = p3.y - p2.y
    val dy3 = p0.y - p1.y + p2.y - p3.y
    val den = dx1 * dy2 - dx2 * dy1
    val g = (dx3 * dy2 - dx2 * dy3) / den
    val h = (dx1 * dy3 - dx3 * dy1) / den
    val a = p1.x - p0.x + g * p1.x
    val b = p3.x - p0.x + h * p3.x
    val d = p1.y - p0.y + g * p1.y
    val e = p3.y - p0.y + h * p3.y
    return WarpMap { u, v ->
        val w = g * u + h * v + 1
        WarpPoint((a * u + b * v + p0.x) / w, (d * u + e * v + p0.y) / w)
    }
}

/** What lies on a horn's axis. */
internal enum class WarpAnchor { Floor, Ceiling, Axis }

internal class WarpHorn(
    val quad: List<WarpPoint>,
    val map: WarpMap,
)

/**
 * `horn`: a flat wedge, [narrow] at the start of the axis and opening by [spread] degrees. [persp] goes from an even
 * trapezium, 0, to true perspective, 1, the letters closing up toward the narrow end.
 */
internal fun warpHorn(
    ox: Double,
    oy: Double,
    length: Double,
    narrow: Double,
    spread: Double = 17.0,
    angle: Double = 0.0,
    anchor: WarpAnchor = WarpAnchor.Floor,
    persp: Double = 1.0,
    padU: Double = .04,
    padV: Double = .16,
): WarpHorn {
    val wide = narrow + length * tan(radians(spread))
    val rot = rotation(ox, oy, angle)
    val quad =
        when (anchor) {
            WarpAnchor.Ceiling -> {
                listOf(rot(0.0, 0.0), rot(length, 0.0), rot(length, wide), rot(0.0, narrow))
            }

            WarpAnchor.Axis -> {
                listOf(rot(0.0, -narrow / 2), rot(length, -wide / 2), rot(length, wide / 2), rot(0.0, narrow / 2))
            }

            WarpAnchor.Floor -> {
                listOf(rot(0.0, -narrow), rot(length, -wide), rot(length, 0.0), rot(0.0, 0.0))
            }
        }
    val hom = warpQuad(quad[0], quad[1], quad[2], quad[3])
    return WarpHorn(quad) { u, v ->
        val pu = padU + u * (1 - 2 * padU)
        val pv = padV + v * (1 - 2 * padV)
        val flat = lerp(lerp(quad[0], quad[1], pu), lerp(quad[3], quad[2], pu), pv)
        lerp(flat, hom.at(pu, pv)!!, persp)
    }
}

internal class WarpCone(
    val length: Double,
    val r0: Double,
    val r1: Double,
    val yaw: Double,
    val angle: Double,
    val rot: (Double, Double) -> WarpPoint,
    val map: WarpMap,
)

/** `cone`: a horn with volume, the axis along [angle], the radius from [r0] to [r1] (equal, a shaft); [arc] is the text's. */
internal fun warpCone(
    ox: Double,
    oy: Double,
    length: Double,
    r0: Double,
    r1: Double,
    yaw: Double = 24.0,
    arc: Double = 60.0,
    angle: Double = 0.0,
    padStart: Double = .04,
    padEnd: Double = 0.0,
): WarpCone {
    val rot = rotation(ox, oy, angle)
    val s = sin(radians(yaw))
    val a = radians(arc)
    val u0 = padStart
    // The text ends before the mouth.
    val u1 = 1 - max(padEnd, (2 * r1 * s + 8) / length)
    return WarpCone(length, r0, r1, s, angle, rot) { u, v ->
        val at = u0 + u * (u1 - u0)
        val x = at * length
        val r = r0 + (r1 - r0) * at
        val th = a * (1 - 2 * v)
        if (cos(th) < .05) null else rot(x + r * cos(th) * s, -r * sin(th))
    }
}

internal class WarpColumn(
    val cx: Double,
    val r: Double,
    val top: Double,
    val bottom: Double,
    val squash: Double,
    val map: WarpMap,
)

/** `column`: an upright cylinder seen from above by [tilt]; the back is not drawn. */
internal fun warpColumn(
    cx: Double,
    r: Double,
    top: Double,
    bottom: Double,
    bandTop: Double,
    bandBottom: Double,
    arc: Double = 70.0,
    tilt: Double = .16,
    spin: Double = 0.0,
): WarpColumn {
    val squash = r * tilt
    val a = radians(arc)
    return WarpColumn(cx, r, top, bottom, squash) { u, v ->
        val th = -a + 2 * a * u + radians(spin)
        if (cos(th) <
            .05
        ) {
            null
        } else {
            WarpPoint(cx + r * sin(th), bandTop + (bandBottom - bandTop) * v + squash * cos(th))
        }
    }
}

/** `sphere`: the text as a belt on the equator, [lon] and [lat] its arc and height, [tilt] the view from above. */
internal fun warpSphere(
    cx: Double,
    cy: Double,
    r: Double,
    lon: Double = 70.0,
    lat: Double = 22.0,
    tilt: Double = 14.0,
): WarpMap {
    val lo = radians(lon)
    val la = radians(lat)
    val ct = cos(radians(tilt))
    val st = sin(radians(tilt))
    return WarpMap { u, v ->
        val along = -lo + 2 * lo * u
        val up = la * (1 - 2 * v)
        val y = r * sin(up)
        val z = r * cos(up) * cos(along)
        val x = r * cos(up) * sin(along)
        if (y * st + z * ct < r * .05) null else WarpPoint(cx + x, cy - (y * ct - z * st))
    }
}

internal class WarpBox(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
    val dx: Double,
    val dy: Double,
    val folds: List<Double>,
    val vfolds: List<Double>,
    val map: WarpMap,
)

/** The box's inset of the text from its sides, when it folds over the top. */
private const val LID_INSET = 8.0
private const val LID_FOLD = .3

/**
 * `box`: an oblique projection, the depth [d] along −17°. By default the text breaks over the side edge, [at] being the
 * share of it before the break; with [lid] over the top, the top share going onto the lid at its natural size.
 */
internal fun warpBox(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    d: Double,
    bandTop: Double,
    bandBottom: Double,
    lid: Boolean = false,
    at: Double? = null,
): WarpBox {
    val dx = d * cos(radians(17.0))
    val dy = -d * sin(radians(17.0))
    if (lid) {
        val f = min(at ?: LID_FOLD, d / ((bandBottom - y) + d))
        val cap = (bandBottom - y) / (1 - f)
        return WarpBox(x, y, w, h, dx, dy, emptyList(), listOf(f)) { u, v ->
            val px = x + LID_INSET + u * (w - 2 * LID_INSET)
            if (v >= f) {
                WarpPoint(px, y + (v - f) * cap)
            } else {
                val s = (f - v) * cap / d
                WarpPoint(px + s * dx, y + s * dy)
            }
        }
    }
    val fold = at ?: (w / (w + d))
    return WarpBox(x, y, w, h, dx, dy, listOf(fold), emptyList()) { u, v ->
        val py = bandTop + (bandBottom - bandTop) * v
        if (u <= fold) {
            WarpPoint(x + u / fold * w, py)
        } else {
            val s = (u - fold) / (1 - fold)
            WarpPoint(x + w + s * dx, py + s * dy)
        }
    }
}

internal class WarpRiser(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
)

internal class WarpStairs(
    val dx: Double,
    val dy: Double,
    val risers: List<WarpRiser>,
    val maps: List<WarpMap>,
)

/** The inset of the text on a riser, and its band in the riser's height. */
private const val RISER_INSET = 8.0
private const val RISER_TOP = .16
private const val RISER_BAND = .68

/** `stairs`: [n] steps going up and back along −17°, a line to a riser, the maps from the top down, as it reads. */
internal fun warpStairs(
    x: Double,
    y: Double,
    n: Int,
    w: Double,
    riser: Double,
    depth: Double,
): WarpStairs {
    val dx = depth * cos(radians(17.0))
    val dy = -depth * sin(radians(17.0))
    val risers = List(n) { i -> WarpRiser(x + i * dx, y - (i + 1) * riser + i * dy, w, riser) }
    val maps =
        risers.reversed().map { r ->
            WarpMap { u, v ->
                WarpPoint(r.x + RISER_INSET + u * (r.w - 2 * RISER_INSET), r.y + r.h * RISER_TOP + v * r.h * RISER_BAND)
            }
        }
    return WarpStairs(dx, dy, risers, maps)
}

/** `lineMap`: line [i] of [n] on a form takes its own band of v. */
internal fun warpLineMap(
    map: WarpMap,
    i: Int,
    n: Int,
): WarpMap {
    val a = i.toDouble() / n + LINE_GAP / (2 * n)
    val k = (1 - LINE_GAP) / n
    return WarpMap { u, v -> map.at(u, a + v * k) }
}

/** `lineV`: a v on the form, as a v of line [i] of [n]. */
internal fun warpLineV(
    v: Double,
    i: Int,
    n: Int,
) = (v - i.toDouble() / n - LINE_GAP / (2 * n)) * n / (1 - LINE_GAP)

/** The grid `warp` draws: its [us] and [vs], and per u, per v, where each falls. */
internal class WarpGrid(
    val us: List<Double>,
    val vs: List<Double>,
    val points: List<List<WarpPoint?>>,
)

/** [folds] and [vfolds] are grid lines too, so that a fold bends at a line and not across a triangle. */
internal fun warpGrid(
    map: WarpMap,
    nu: Int = WARP_NU,
    nv: Int = WARP_NV,
    folds: List<Double> = emptyList(),
    vfolds: List<Double> = emptyList(),
): WarpGrid {
    val rows = max(nv, WARP_NV_LEAST)
    val us = (List(nu + 1) { it.toDouble() / nu } + folds).distinct().sorted()
    val vs = List(rows + 1) { WARP_V_MIN + (WARP_V_MAX - WARP_V_MIN) * it / rows }.toMutableList()
    for (f in vfolds) if (vs.none { abs(it - f) < V_SAME }) vs += f
    vs.sort()
    return WarpGrid(us, vs, us.map { u -> vs.map { v -> map.at(u, v) } })
}

/** A [WarpGrid] as triangles: positions and their texture's px, flat, and two triangles per quad it shows whole. */
internal class WarpMesh(
    val positions: List<WarpPoint>,
    val texture: List<WarpPoint>,
    val indices: List<Int>,
)

internal fun warpMesh(
    grid: WarpGrid,
    sourceW: Int,
    sourceH: Int,
): WarpMesh {
    val positions = mutableListOf<WarpPoint>()
    val texture = mutableListOf<WarpPoint>()
    val indices = mutableListOf<Int>()
    val made = HashMap<Int, Int>()

    fun vertex(
        i: Int,
        j: Int,
    ): Int =
        made.getOrPut(i * grid.vs.size + j) {
            positions += grid.points[i][j]!!
            texture +=
                WarpPoint(grid.us[i] * sourceW, (grid.vs[j] - WARP_V_MIN) / (WARP_V_MAX - WARP_V_MIN) * sourceH)
            positions.size - 1
        }
    for (i in 0 until grid.us.size - 1) {
        for (j in 0 until grid.vs.size - 1) {
            val whole =
                grid.points[i][j] != null &&
                    grid.points[i + 1][j] != null &&
                    grid.points[i + 1][j + 1] != null &&
                    grid.points[i][j + 1] != null
            if (!whole) continue
            val a = vertex(i, j)
            val b = vertex(i + 1, j)
            val c = vertex(i + 1, j + 1)
            val d = vertex(i, j + 1)
            indices += listOf(a, b, c, a, c, d)
        }
    }
    return WarpMesh(positions, texture, indices)
}
