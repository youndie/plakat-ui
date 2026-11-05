package io.github.youndie.plakat.agit

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

/**
 * The Core page's own composition generator, `comp(seed, W, H, pal, safe, opt)` in its script, which
 * draws the posters and the card thumbnails — a smaller vocabulary than [generate]: a gray plate, a
 * circle, a bar, a wedge and maybe a dot, in that order, each placed once or tried up to twenty times
 * where it must keep out of the [safe] zones. Ported line for line; `scripts/poster-goldens.mjs` runs
 * the page's code in Node and the common tests hold this to it.
 */
internal sealed class PosterShape {
    abstract val role: PosterRole

    /**
     * A plate, or a bar turned [rotate] degrees about the middle of its left end
     * (`transform-origin: 0 50%`); a plate has no rotation at all, a bar always has one, 0 included.
     */
    data class Box(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        override val role: PosterRole,
        val rotate: Double? = null,
    ) : PosterShape()

    data class Round(
        val x: Double,
        val y: Double,
        val d: Double,
        override val role: PosterRole,
    ) : PosterShape()

    /** A right triangle in its [w] × [h] box: the lower-right half, or the upper-left one. */
    data class Wedge(
        val x: Double,
        val y: Double,
        val w: Double,
        val h: Double,
        override val role: PosterRole,
        val lowerRight: Boolean,
    ) : PosterShape()
}

/** The page's palette keys: `pal.red`, `pal.ink`, `pal.gray`. */
internal enum class PosterRole { Red, Ink, Gray }

/** A box to keep ink out of, the page's `safe` entries. */
internal data class PosterZone(
    val x: Double,
    val y: Double,
    val w: Double,
    val h: Double,
)

/** The page's `{ shapes, circle }`; [circle] is null where the page writes its `-999px` stand-in. */
internal data class PosterComposition(
    val shapes: List<PosterShape>,
    val circle: PosterShape.Round?,
)

/** How the page calls `comp`: its `pal` (with or without gray), `safe` and `opt`. */
internal data class PosterRecipe(
    val w: Double,
    val h: Double,
    val gray: Boolean = true,
    val safe: List<PosterZone> = emptyList(),
    val redSlot: String? = null,
    val strict: Set<String> = emptySet(),
    val photoCircle: Boolean = false,
    val dot: Double = 1.0,
    val scale: Double = 1.0,
)

internal fun posterComposition(
    seed: Int,
    recipe: PosterRecipe,
): PosterComposition {
    val r = Mulberry32(seed)
    val w0 = recipe.w
    val h0 = recipe.h
    val k = recipe.scale

    fun f() = r.next()

    fun <T> pick(xs: List<T>) = xs[floor(f() * xs.size).toInt()]

    // `Math.max(8, Math.round(v / 8) * 8)`, JS's round: a half rounds up.
    fun s8(v: Double) = max(8.0, floor(v / 8 + .5) * 8)

    fun hit(b: PosterZone) =
        recipe.safe.any { z ->
            b.x < z.x + z.w && b.x + b.w > z.x && b.y < z.y + z.h &&
                b.y + b.h > z.y
        }
    val t = tan(17 * PI / 180)
    val redSlot = recipe.redSlot ?: pick(listOf("circle", "wedge", "bar"))
    val out = mutableListOf<PosterShape>()

    // One element: tried up to twenty times while ink (or a strict key) lands in a safe zone.
    fun <S : PosterShape> place(
        key: String,
        make: (PosterRole) -> Pair<S, PosterZone>,
    ): S? {
        val role =
            when {
                key == "gray" -> PosterRole.Gray
                key == "dot" -> PosterRole.Ink
                key == redSlot -> PosterRole.Red
                else -> PosterRole.Ink
            }
        repeat(20) {
            val (shape, box) = make(role)
            if ((role == PosterRole.Ink || key in recipe.strict) && hit(box)) return@repeat
            if (!(recipe.photoCircle && key == "circle")) out += shape
            return shape
        }
        return null
    }
    if (recipe.gray) {
        place("gray") { role ->
            val w = s8(w0 * (.35 + f() * .3))
            val h = s8(h0 * (.25 + f() * .35))
            val x = pick(listOf(0.0, w0 - w))
            val y = s8(f() * h0 * .4)
            PosterShape.Box(x, y, w, h, role) to PosterZone(x, y, w, h)
        }
    }
    val circle =
        place("circle") { role ->
            val d = s8(min(w0, h0) * (.4 + f() * .3) * k)
            val x = s8(pick(listOf(w0 - d * .6, w0 - d * .9, w0 * .5 - d * .2)))
            val y = s8(h0 * (.03 + f() * .25))
            PosterShape.Round(x, y, d, role) to PosterZone(x, y, d, d)
        }
    place("bar") { role ->
        val bt = s8(pick(listOf(8.0, 16.0, 24.0)) * k)
        val bl = s8(w0 * (.5 + f() * .5))
        val rot = pick(listOf(-17.0, -17.0, -17.0, 0.0, 90.0))
        val x = s8(w0 * (.05 + f() * .5))
        val y = s8(h0 * (.3 + f() * .5))
        val a = rot * PI / 180
        val x2 = x + bl * cos(a)
        val y2 = y + bl * sin(a)
        PosterShape.Box(x, y, bl, bt, role, rot) to
            PosterZone(min(x, x2) - bt, min(y, y2) - bt, abs(x2 - x) + 2 * bt, abs(y2 - y) + 2 * bt)
    }
    place("wedge") { role ->
        val w = s8(w0 * (.2 + f() * .3) * k)
        val h = floor(w * t + .5)
        val x = s8(f() * max(8.0, w0 - w))
        val y = s8(h0 * (.05 + f() * .85))
        PosterShape.Wedge(x, y, w, h, role, pick(listOf(true, false))) to PosterZone(x, y, w, h)
    }
    if (f() > .35) {
        place("dot") { role ->
            val d = s8(pick(listOf(16.0, 24.0, 32.0, 48.0)) * recipe.dot * k)
            val x = s8(f() * (w0 - d))
            val y = s8(f() * (h0 - d))
            PosterShape.Round(x, y, d, role) to PosterZone(x, y, d, d)
        }
    }
    return PosterComposition(out, circle)
}

/** The page's poster and thumbnail recipes, its calls of `comp` in `renderVals`. */
internal object PosterRecipes {
    /** Круг: 390 × 312 over the title's corner, the red circle reserved for the photo. */
    val Circle =
        PosterRecipe(
            390.0,
            312.0,
            safe = listOf(PosterZone(0.0, 24.0, 290.0, 288.0)),
            redSlot = "circle",
            strict = setOf("circle"),
            photoCircle = true,
        )

    /** Фон: the whole 390 × 500, no gray plate, ink kept off the title's stripes. */
    val Background = PosterRecipe(390.0, 500.0, gray = false, safe = listOf(PosterZone(0.0, 80.0, 300.0, 240.0)))

    /** Баннер: the 176 × 160 window at its right, the red circle, small dots. */
    val Banner = PosterRecipe(176.0, 160.0, redSlot = "circle", dot = .5)

    /** A card's 64 × 64 thumbnail, smaller dots still. */
    val Thumbnail = PosterRecipe(64.0, 64.0, dot = .4)
}

/** The shape of a strip's cell: the MVP page's motifs. */
internal enum class StripMotif { Square, WedgeLowerRight, WedgeUpperLeft, Half, Circle, Bar }

/** The paint of a strip's cell: the page's `pal.red` and its two `pal.alts`, ink and concrete. */
internal enum class StripPaint { Red, Ink, Concrete }

/** One drawn cell of a strip, at its [column] and [row]. */
internal data class StripCell(
    val column: Int,
    val row: Int,
    val motif: StripMotif,
    val paint: StripPaint,
)

/**
 * The rapport strip under a top app bar, the MVP page's `raport(seed, cols, rows, cell, pal)` — not the
 * [rapport] of `plakat-gen.js`, which chooses its red differently. A tile of 2…4 × 2…3 motifs repeats
 * over the strip, its wedges flipping in every other tile. `scripts/poster-goldens.mjs` runs the page's
 * code and the common tests hold this to it. The tile does not depend on [columns], so a wider strip
 * only goes on with it.
 */
internal fun plakatStrip(
    seed: Int,
    columns: Int,
    rows: Int = 2,
): List<StripCell> {
    val r = Mulberry32(seed)

    fun <T> pick(xs: List<T>) = xs[floor(r.next() * xs.size).toInt()]
    val tw = pick(listOf(2, 3, 4))
    val th = pick(listOf(2, 3))
    val motifs =
        listOf(
            StripMotif.Square,
            StripMotif.WedgeLowerRight,
            StripMotif.WedgeUpperLeft,
            StripMotif.Half,
            StripMotif.Circle,
            StripMotif.Bar,
            null,
        )
    val tile =
        MutableList(tw * th) {
            // The motif is drawn before its paint, as in the JS's object literal.
            val motif = pick(motifs)
            motif to if (r.next() < .3) StripPaint.Red else pick(listOf(StripPaint.Ink, StripPaint.Concrete))
        }
    if (tile.none { it.second == StripPaint.Red && it.first != null }) tile[0] = StripMotif.Circle to StripPaint.Red
    val out = mutableListOf<StripCell>()
    for (y in 0 until rows) {
        for (x in 0 until columns) {
            val (m0, paint) = tile[(y % th) * tw + (x % tw)]
            if (m0 == null) continue
            val flip = ((x / tw) + (y / th)) % 2 == 1
            val motif =
                when {
                    flip && m0 == StripMotif.WedgeLowerRight -> StripMotif.WedgeUpperLeft
                    flip && m0 == StripMotif.WedgeUpperLeft -> StripMotif.WedgeLowerRight
                    else -> m0
                }
            out += StripCell(x, y, motif, paint)
        }
    }
    return out
}
