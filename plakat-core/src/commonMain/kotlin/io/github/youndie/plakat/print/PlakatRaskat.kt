package io.github.youndie.plakat.print

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// «Раскат» (research D5): two inks on one roller. A port of the vendored `plakat-raskat.js`, not of
// the designer's `plakat-raskat.kt`, whose noise is a different function (research §1.3). Every
// pixel is ink A or ink B, never a mix: the profile across the transition line is compared with a
// texture threshold computed once per field. The arithmetic follows the JS operation for operation —
// the field is stored in 32-bit floats as the JS's Float32Array does — and is held to goldens the JS
// emits (`scripts/raskat-goldens.mjs`).

/** The texture of the transition: roller grain, a 45° halftone, or hatching along the seam. */
internal enum class RaskatMode { Grain, Dots, Lines }

/** The field's parameters, the JS's option names and defaults. [dpr] is the density the field is computed at. */
internal data class RaskatOptions(
    val angle: Double = -17.0,
    val grain: Double = 2.0,
    val seed: Int = 1919,
    val mode: RaskatMode = RaskatMode.Grain,
    val streak: Double = 0.0,
    val rough: Double = .35,
    val dpr: Double = 2.0,
)

/**
 * A field over a [w] × [h] pixel box: [t] is each pixel's position across the transition (0…1 over
 * the box), [m] the texture's threshold. Computed once and reused while only `t0` moves.
 */
internal class RaskatField(
    val w: Int,
    val h: Int,
    val t: FloatArray,
    val m: FloatArray,
)

private const val SQRT1_2 = 0.7071067811865476

/** The JS's integer hash of a pixel, as `Math.imul` and `>>>` compute it. */
internal fun raskatHash(
    x: Int,
    y: Int,
    s: Int,
): Double {
    var h = x * 374761393 + y * 668265263 + s * 2147483647
    h = (h xor (h ushr 13)) * 1274126177
    return (h xor (h ushr 16)).toUInt().toDouble() / 4294967296.0
}

private fun vnoise(
    x: Double,
    y: Double,
    s: Int,
): Double {
    val xi = floor(x)
    val yi = floor(y)
    val fx = x - xi
    val fy = y - yi
    val sx = fx * fx * (3 - 2 * fx)
    val sy = fy * fy * (3 - 2 * fy)
    val ix = xi.toInt()
    val iy = yi.toInt()
    val a = raskatHash(ix, iy, s)
    val b = raskatHash(ix + 1, iy, s)
    val c = raskatHash(ix, iy + 1, s)
    val d = raskatHash(ix + 1, iy + 1, s)
    return (a * (1 - sx) + b * sx) * (1 - sy) + (c * (1 - sx) + d * sx) * sy
}

/** V8's `Math.hypot` for two arguments: scaled by the larger, the squares summed with Kahan's compensation. */
private fun jsHypot(
    a: Double,
    b: Double,
): Double {
    val big = max(abs(a), abs(b))
    if (big == 0.0) return 0.0
    var sum = 0.0
    var compensation = 0.0
    for (v in doubleArrayOf(a, b)) {
        val n = abs(v) / big
        val summand = n * n - compensation
        val preliminary = sum + summand
        compensation = (preliminary - sum) - summand
        sum = preliminary
    }
    return sqrt(sum) * big
}

/** JS's `Math.round`: a half rounds up. */
private fun jsRound(v: Double) = floor(v + .5)

/** The field for a [w] × [h] pixel box, the JS's `raskatField`. */
internal fun raskatField(
    w: Int,
    h: Int,
    o: RaskatOptions,
): RaskatField {
    val r = o.angle * PI / 180
    val dx = cos(r)
    val dy = sin(r)
    val ext = abs(dx) * w + abs(dy) * h
    val g = o.grain * o.dpr
    val t = FloatArray(w * h)
    val m = FloatArray(w * h)
    val pitch = g * 4
    val rmax = pitch * SQRT1_2
    for (j in 0 until h) {
        for (i in 0 until w) {
            val k = j * w + i
            val x = i - w / 2.0
            val y = j - h / 2.0
            val across = x * dx + y * dy
            val along = -x * dy + y * dx
            t[k] = (across / ext + .5).toFloat()
            val wn = raskatHash(i, j, o.seed + 7)
            m[k] =
                when (o.mode) {
                    RaskatMode.Dots -> {
                        val u = (i * SQRT1_2 + j * SQRT1_2) / pitch
                        val v = (-i * SQRT1_2 + j * SQRT1_2) / pitch
                        val d = jsHypot(u - jsRound(u), v - jsRound(v)) * pitch
                        val q = d / rmax
                        q * q + (wn - .5) * o.rough * .25
                    }

                    RaskatMode.Lines -> {
                        val q = across / pitch + (vnoise(along / (g * 3), across / g, o.seed) - .5) * o.rough * .5
                        q - floor(q)
                    }

                    RaskatMode.Grain -> {
                        val sa = g * (1 + o.streak * 10)
                        .6 * vnoise(along / sa, across / g, o.seed) + .4 * wn
                    }
                }.toFloat()
        }
    }
    return RaskatField(w, h, t, m)
}

/**
 * Which pixels take ink B at transition [t0] with stiffness [hard]: the logistic profile
 * `1 / (1 + e^(−hard·(t − t0)))` against the threshold, the JS's `raskatImage` without its canvas.
 */
internal fun raskatInkB(
    field: RaskatField,
    t0: Double,
    hard: Double,
): BooleanArray =
    BooleanArray(field.w * field.h) { k ->
        val p = 1 / (1 + exp(-hard * (field.t[k] - t0)))
        p > field.m[k]
    }

/** Where the roll starts: the second ink just beyond the far edge. */
internal const val RASKAT_ROLL_FROM: Double = 1.15

/** The roll's own hard-stop, `cubic-bezier(.2, .8, .9, .9)` solved by 24 bisections as the JS's `roll` does. */
internal fun raskatHardStop(t: Double): Double {
    var lo = 0.0
    var hi = 1.0
    var s = t
    repeat(24) {
        s = (lo + hi) / 2
        val x = 3 * (1 - s) * (1 - s) * s * .2 + 3 * (1 - s) * s * s * .9 + s * s * s
        if (x < t) lo = s else hi = s
    }
    return 3 * (1 - s) * (1 - s) * s * .8 + 3 * (1 - s) * s * s * .9 + s * s * s
}

/** `t0` [elapsedMs] into a roll to [target] lasting [durationMs] (320, the scene duration). */
internal fun raskatRollT0(
    elapsedMs: Double,
    target: Double,
    durationMs: Double = 320.0,
): Double {
    val k = min(1.0, elapsedMs / durationMs)
    return RASKAT_ROLL_FROM + (target - RASKAT_ROLL_FROM) * raskatHardStop(k)
}
