package io.github.youndie.plakat.agit

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.tan

// The agit-graphics generator (research D4): a port of the vendored `plakat-gen.js` — `generate` and
// `rapport` — kept in the shape of the designer's `plakat-gen.kt`, which matches the JS on the JVM
// but is JVM-only. Every call that draws from the generator stays in the JS's order, because the
// sequence of random numbers is the composition. Held to goldens the JS emits (`scripts/gen-goldens.mjs`).

/** The paint a shape is printed in; the theme turns it into a colour. */
internal enum class PlakatRole { Red, Ink, Concrete }

/** One printed primitive, in dp inside its frame. */
internal sealed class PlakatShape {
    abstract val role: PlakatRole

    /** A rectangle, turned by [rot] degrees about ([ox], [oy]) when [rot] is not zero. */
    data class Rect(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        override val role: PlakatRole,
        val rot: Double = 0.0,
        val ox: Double = 0.0,
        val oy: Double = 0.0,
    ) : PlakatShape()

    data class Circle(
        val cx: Double,
        val cy: Double,
        val r: Double,
        override val role: PlakatRole,
    ) : PlakatShape()

    /** A polygon: the wedge and the triangles of a rapport. */
    data class Poly(
        val points: List<Pair<Double, Double>>,
        override val role: PlakatRole,
    ) : PlakatShape()
}

/** The title zone a composition keeps clear of ink and red. */
internal data class PlakatZone(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
)

/** What `generate(seed)` returns: [shapes] in drawing order, [elements] by name, the one [red] element. */
internal data class PlakatComposition(
    val seed: Int,
    val w: Double,
    val h: Double,
    val head: PlakatZone?,
    val shapes: List<PlakatShape>,
    val elements: List<String>,
    val red: String?,
)

/** A seamless tile, [w] × [h], of a geometric rapport. */
internal data class PlakatRapport(
    val w: Double,
    val h: Double,
    val shapes: List<PlakatShape>,
)

/** mulberry32, bit for bit the JS's: every product wraps as `Math.imul` does, and only the result is divided. */
internal class Mulberry32(
    seed: Int,
) {
    private var a = seed

    fun next(): Double {
        a += 0x6D2B79F5
        var t = (a xor (a ushr 15)) * (1 or a)
        t = (t + ((t xor (t ushr 7)) * (61 or t))) xor t
        return (t xor (t ushr 14)).toUInt().toDouble() / 4294967296.0
    }
}

private class Draw(
    seed: Int,
) {
    private val r = Mulberry32(seed)

    fun f() = r.next()

    fun int(
        a: Int,
        b: Int,
    ) = a + floor(f() * (b - a + 1)).toInt()

    fun <T> pick(xs: List<T>) = xs[floor(f() * xs.size).toInt()]

    fun s8(
        a: Double,
        b: Double,
    ) = snap(a + f() * (b - a))

    fun bool(p: Double = .5) = f() < p
}

/**
 * The 8 dp grid. JS's `Math.round` rounds a half up; Kotlin's common `round` rounds it to even, and
 * `v / 8` lands on a half for every v ≡ 4 (mod 8), so the floor form is the only faithful one (D4).
 */
internal fun snap(v: Double): Double = floor(v / 8.0 + 0.5) * 8.0

/** The kit's one diagonal, in degrees. */
internal const val AXIS: Double = -17.0

private val T17 = tan(17.0 * PI / 180.0)

/** The generator's vocabulary, in drawing order — the same order as the JS's `ELEMENTS`. */
internal val ELEMENTS: List<String> =
    listOf("plate", "strip", "wedge", "circle", "columns", "checker", "ground", "bar", "rail", "dot")

private val RED_OK = setOf("circle", "wedge", "bar", "dot", "columns", "checker", "rail")

private fun area(s: PlakatShape): Double =
    when (s) {
        is PlakatShape.Circle -> {
            PI * s.r * s.r
        }

        is PlakatShape.Rect -> {
            s.w * s.h
        }

        is PlakatShape.Poly -> {
            var a = 0.0
            for (i in s.points.indices) {
                val (x1, y1) = s.points[i]
                val (x2, y2) = s.points[(i + 1) % s.points.size]
                a += x1 * y2 - x2 * y1
            }
            abs(a) / 2
        }
    }

private fun PlakatShape.recolor(r: PlakatRole): PlakatShape =
    when (this) {
        is PlakatShape.Rect -> copy(role = r)
        is PlakatShape.Circle -> copy(role = r)
        is PlakatShape.Poly -> copy(role = r)
    }

private fun PlakatShape.shift(
    dx: Double,
    dy: Double,
): PlakatShape =
    when (this) {
        is PlakatShape.Rect -> copy(x = x + dx, y = y + dy)
        is PlakatShape.Circle -> copy(cx = cx + dx, cy = cy + dy)
        is PlakatShape.Poly -> copy(points = points.map { (x, y) -> (x + dx) to (y + dy) })
    }

private fun topOf(s: PlakatShape) =
    when (s) {
        is PlakatShape.Circle -> s.cy
        is PlakatShape.Rect -> s.y
        is PlakatShape.Poly -> s.points.minOf { it.second }
    }

/**
 * A composition from the whole vocabulary for [seed], in a [w] × [h] frame: one large form (circle
 * or wedge), 3–6 elements, the 8 dp grid, the −17° axis, exactly one red element no bigger than a
 * third of the frame, and no ink or red inside the title zone — [head], or the generator's own when
 * null, or none when [noHead]. The same seed gives the same composition on every target.
 */
internal fun generate(
    seed: Int,
    w: Double = 360.0,
    h: Double = 240.0,
    head: PlakatZone? = null,
    noHead: Boolean = false,
): PlakatComposition {
    val g = Draw(seed)
    val m = min(w, h)
    val hd = if (noHead) null else head ?: PlakatZone(16.0, 16.0, g.s8(w * .45, w * .6), g.s8(h * .26, h * .38))
    val make: Map<String, () -> List<PlakatShape>> =
        mapOf(
            "plate" to {
                val pw = g.s8(w * .3, w * .6)
                val ph = g.s8(h * .25, h * .55)
                listOf(
                    PlakatShape.Rect(
                        g.pick(listOf(0.0, w - pw, g.s8(0.0, w - pw))),
                        g.s8(0.0, h - ph),
                        pw,
                        ph,
                        PlakatRole.Concrete,
                    ),
                )
            },
            "strip" to {
                val cell = g.pick(listOf(12.0, 16.0))
                val t = rapport(g.int(1, 99999), cell)
                val y0 = g.s8(0.0, h - 2 * cell)
                val out = mutableListOf<PlakatShape>()
                var k = 0
                while (k * t.w < w) {
                    for (s in t.shapes) {
                        if (topOf(s) >= 2 * cell) continue
                        val moved = s.shift(k * t.w, y0)
                        out += if (moved.role == PlakatRole.Red) moved.recolor(PlakatRole.Ink) else moved
                    }
                    k++
                }
                out
            },
            "wedge" to {
                val ww = g.s8(w * .35, w * .9)
                val wh = ww * T17
                val x = g.s8(0.0, w - ww * .5)
                val y = g.s8(wh + 8, h)
                val points =
                    if (g.bool(.6)) {
                        listOf(x to y, x + ww to y - wh, x + ww to y)
                    } else {
                        listOf(x to y - wh, x + ww to y - wh, x to y)
                    }
                listOf(PlakatShape.Poly(points, PlakatRole.Ink))
            },
            "circle" to {
                val r = g.s8(m * .18, m * .34)
                listOf(PlakatShape.Circle(g.s8(r * .3, w - r * .3), g.s8(r * .3, h + r * .3), r, PlakatRole.Ink))
            },
            "columns" to {
                val n = g.int(3, 5)
                val cw = g.pick(listOf(16.0, 24.0, 32.0))
                val gap = g.pick(listOf(8.0, 16.0))
                val x0 = g.s8(0.0, w - (n * cw + (n - 1) * gap))
                val base = h - g.pick(listOf(0.0, 8.0))
                List(n) { i ->
                    val ch = g.s8(h * .25, h * .6)
                    PlakatShape.Rect(x0 + i * (cw + gap), base - ch, cw, ch, PlakatRole.Ink)
                }
            },
            "checker" to {
                val cols = g.int(3, 5)
                val rows = g.int(2, 3)
                val c = g.pick(listOf(24.0, 32.0, 40.0))
                val x0 = g.s8(0.0, w - cols * c)
                val y0 = g.s8(0.0, h - rows * c)
                val par = g.int(0, 1)
                List(cols * rows) { i ->
                    val cx = i % cols
                    val cy = i / cols
                    val role = if ((cx + cy + par) % 2 == 1) PlakatRole.Concrete else PlakatRole.Ink
                    PlakatShape.Rect(x0 + cx * c, y0 + cy * c, c, c, role)
                }
            },
            "ground" to { listOf(PlakatShape.Rect(0.0, h - 8, w, 8.0, PlakatRole.Ink)) },
            "bar" to {
                val bh = g.pick(listOf(8.0, 16.0, 24.0))
                val bw = g.s8(w * .4, w * 1.1)
                val rot = g.pick(listOf(AXIS, AXIS, AXIS, 0.0, 90.0))
                val x = g.s8(-w * .1, w * .6)
                val y = g.s8(h * .2, h * .95)
                listOf(PlakatShape.Rect(x, y, bw, bh, PlakatRole.Ink, rot, x, y + bh / 2))
            },
            "rail" to {
                val bh = g.pick(listOf(8.0, 16.0, 24.0))
                val x0 = g.s8(-16.0, w * .3)
                val yb = g.s8(h * .55, h)
                val l1 = g.s8(w * .2, w * .5)
                val gap = g.pick(listOf(24.0, 32.0, 40.0))
                listOf(
                    PlakatShape.Rect(x0, yb - bh / 2, l1, bh, PlakatRole.Ink, AXIS, x0, yb),
                    PlakatShape.Rect(x0 + l1 + gap, yb - bh / 2, w * 1.2, bh, PlakatRole.Ink, AXIS, x0, yb),
                )
            },
            "dot" to {
                val r = g.pick(listOf(8.0, 12.0, 16.0, 24.0))
                listOf(PlakatShape.Circle(g.s8(r + 8, w - r - 8), g.s8(r + 8, h - r - 8), r, PlakatRole.Ink))
            },
        )
    // One large form, then 3–6 elements in all; drawn in ELEMENTS order whatever the order chosen.
    val chosen = mutableSetOf(g.pick(listOf("circle", "wedge")))
    val n = g.int(3, 6)
    while (chosen.size < n) chosen += g.pick(ELEMENTS)

    fun blocked(s: PlakatShape) = hd != null && s.role != PlakatRole.Concrete && hitsZone(s, hd)
    val groups = mutableListOf<Pair<String, MutableList<PlakatShape>>>()
    for (el in ELEMENTS) {
        if (el !in chosen) continue
        for (i in 0 until 24) {
            val sh = make.getValue(el)()
            if (sh.none { blocked(it) }) {
                groups += el to sh.toMutableList()
                break
            }
        }
    }

    // Red: one element (or one part of a group), no bigger than a third of the frame.
    data class Candidate(
        val group: Int,
        val index: Int,
        val area: Double,
        val big: Boolean,
    )
    val candidates =
        groups.flatMapIndexed { gi, (el, sh) ->
            if (el !in RED_OK) {
                emptyList()
            } else {
                sh.mapIndexedNotNull { j, s ->
                    if (s.role == PlakatRole.Ink) Candidate(gi, j, area(s), el == "circle" || el == "wedge") else null
                }
            }
        }
    val fit = candidates.filter { it.area <= w * h / 3 }
    val preferred = fit.filter { it.big }
    val from =
        if (preferred.isNotEmpty() && g.bool(.6)) {
            preferred
        } else if (fit.isNotEmpty()) {
            fit
        } else {
            candidates.sortedBy { it.area }.take(1)
        }
    var red: String? = null
    if (from.isNotEmpty()) {
        val c = g.pick(from)
        val shapes = groups[c.group].second
        shapes[c.index] = shapes[c.index].recolor(PlakatRole.Red)
        red = groups[c.group].first
    }
    return PlakatComposition(seed, w, h, hd, groups.flatMap { it.second }, groups.map { it.first }, red)
}

/** Whether [s] reaches into [z]: its outline sampled every 4 %, or a corner of [z] inside it. */
internal fun hitsZone(
    s: PlakatShape,
    z: PlakatZone,
): Boolean {
    if (s is PlakatShape.Circle) {
        val cx = s.cx.coerceIn(z.x, z.x + z.w)
        val cy = s.cy.coerceIn(z.y, z.y + z.h)
        return hypot(s.cx - cx, s.cy - cy) < s.r
    }
    val p = outline(s)

    fun inZone(
        x: Double,
        y: Double,
    ) = x > z.x && x < z.x + z.w && y > z.y && y < z.y + z.h
    for (i in p.indices) {
        val (ax, ay) = p[i]
        val (bx, by) = p[(i + 1) % p.size]
        var t = 0.0
        while (t <= 1.0) {
            if (inZone(ax + (bx - ax) * t, ay + (by - ay) * t)) return true
            t += .04
        }
    }
    return listOf(z.x to z.y, z.x + z.w to z.y, z.x to z.y + z.h, z.x + z.w to z.y + z.h).any {
        inPolygon(it.first, it.second, p)
    }
}

/** The corners of a rectangle (turned) or a polygon's points; a circle has none. */
internal fun outline(s: PlakatShape): List<Pair<Double, Double>> =
    when (s) {
        is PlakatShape.Poly -> {
            s.points
        }

        is PlakatShape.Circle -> {
            emptyList()
        }

        is PlakatShape.Rect -> {
            val p = listOf(s.x to s.y, s.x + s.w to s.y, s.x + s.w to s.y + s.h, s.x to s.y + s.h)
            if (s.rot == 0.0) {
                p
            } else {
                val a = s.rot * PI / 180
                val c = kotlin.math.cos(a)
                val n = kotlin.math.sin(a)
                p.map { (x, y) -> (s.ox + (x - s.ox) * c - (y - s.oy) * n) to (s.oy + (x - s.ox) * n + (y - s.oy) * c) }
            }
        }
    }

private fun inPolygon(
    x: Double,
    y: Double,
    p: List<Pair<Double, Double>>,
): Boolean {
    var inside = false
    var j = p.size - 1
    for (i in p.indices) {
        val (xi, yi) = p[i]
        val (xj, yj) = p[j]
        if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
        j = i
    }
    return inside
}

/**
 * A seamless rapport tile for [seed] on a [cell] grid: a motif of 2–4 × 2–3 cells, one of them red,
 * repeated twice along each axis with the wedges mirrored in the second copy, so the 2tw × 2th tile
 * wraps without a seam.
 */
internal fun rapport(
    seed: Int,
    cell: Double = 24.0,
): PlakatRapport {
    val g = Draw(seed)
    val tw = g.pick(listOf(2, 3, 4))
    val th = g.pick(listOf(2, 3))
    val motifs = listOf("sq", "tri1", "tri2", "half", "circle", "bar", "empty")
    val redMotif = g.int(0, tw * th - 1)
    val tile =
        List(tw * th) { i ->
            if (i == redMotif) {
                g.pick(listOf("circle", "tri1", "sq")) to PlakatRole.Red
            } else {
                // The motif is drawn before its paint, as in the JS's object literal.
                val motif = g.pick(motifs)
                motif to g.pick(listOf(PlakatRole.Ink, PlakatRole.Ink, PlakatRole.Concrete))
            }
        }
    val out = mutableListOf<PlakatShape>()
    val c = cell
    for (y in 0 until 2 * th) {
        for (x in 0 until 2 * tw) {
            val (m0, f) = tile[(y % th) * tw + (x % tw)]
            if (m0 == "empty") continue
            val flip = ((x / tw) + (y / th)) % 2 == 1
            val m =
                when {
                    flip && m0 == "tri1" -> "tri2"
                    flip && m0 == "tri2" -> "tri1"
                    else -> m0
                }
            val cx = x * c
            val cy = y * c
            out +=
                when (m) {
                    "sq" -> PlakatShape.Rect(cx, cy, c, c, f)
                    "half" -> PlakatShape.Rect(cx, cy, c / 2, c, f)
                    "bar" -> PlakatShape.Rect(cx, cy + c * .375, c, c * .25, f)
                    "circle" -> PlakatShape.Circle(cx + c / 2, cy + c / 2, c * .375, f)
                    "tri1" -> PlakatShape.Poly(listOf(cx to cy + c, cx + c to cy, cx + c to cy + c), f)
                    else -> PlakatShape.Poly(listOf(cx to cy, cx + c to cy, cx to cy + c), f)
                }
        }
    }
    return PlakatRapport(2 * tw * c, 2 * th * c, out)
}
