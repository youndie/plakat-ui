// plakat-warp.kt — «Проекция» для Compose Multiplatform (референс; порт plakat-warp.js).
// Текст → ImageBitmap один раз; форма задаёт map(u, v) → Offset?; сетка треугольников → drawVertices с ImageShader.
package plakat.warp

import androidx.compose.ui.geometry.Offset
import kotlin.math.*

private const val VMIN = -0.32f   // запас под Й/Ё
private const val VMAX = 1.22f    // запас под Д/Ц/Щ
private val D2R = (PI / 180).toFloat()
val T17 = tan(17f * D2R)

typealias UVMap = (u: Float, v: Float) -> Offset?

// ── гомография единичного квадрата → четырёхугольник (TL, TR, BR, BL) ──
fun quadMap(p0: Offset, p1: Offset, p2: Offset, p3: Offset): (Float, Float) -> Offset {
    val dx1 = p1.x - p2.x; val dx2 = p3.x - p2.x; val dx3 = p0.x - p1.x + p2.x - p3.x
    val dy1 = p1.y - p2.y; val dy2 = p3.y - p2.y; val dy3 = p0.y - p1.y + p2.y - p3.y
    val den = dx1 * dy2 - dx2 * dy1; val g = (dx3 * dy2 - dx2 * dy3) / den; val h = (dx1 * dy3 - dx3 * dy1) / den
    val a = p1.x - p0.x + g * p1.x; val b = p3.x - p0.x + h * p3.x; val d = p1.y - p0.y + g * p1.y; val e = p3.y - p0.y + h * p3.y
    return { u, v -> val w = g * u + h * v + 1; Offset((a * u + b * v + p0.x) / w, (d * u + e * v + p0.y) / w) }
}
private fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
private fun rot(o: Offset, deg: Float): (Offset) -> Offset { val c = cos(deg * D2R); val s = sin(deg * D2R); return { p -> Offset(o.x + p.x * c - p.y * s, o.y + p.x * s + p.y * c) } }

// ── формы ─────────────────────────────────────────────────────
enum class Anchor { Floor, Ceiling, Axis }
class Horn(val quad: List<Offset>, val map: UVMap)
fun horn(origin: Offset, length: Float, h0: Float, spread: Float = 17f, angle: Float = 0f, anchor: Anchor = Anchor.Floor, persp: Float = 1f, padU: Float = .04f, padV: Float = .16f): Horn {
    val H = h0 + length * tan(spread * D2R)
    val loc = when (anchor) {
        Anchor.Ceiling -> listOf(Offset(0f, 0f), Offset(length, 0f), Offset(length, H), Offset(0f, h0))
        Anchor.Axis -> listOf(Offset(0f, -h0 / 2), Offset(length, -H / 2), Offset(length, H / 2), Offset(0f, h0 / 2))
        Anchor.Floor -> listOf(Offset(0f, -h0), Offset(length, -H), Offset(length, 0f), Offset(0f, 0f))
    }
    val R = rot(origin, angle); val q = loc.map(R); val hom = quadMap(q[0], q[1], q[2], q[3])
    return Horn(q) { u, v -> val U = padU + u * (1 - 2 * padU); val V = padV + v * (1 - 2 * padV)
        lerp(lerp(lerp(q[0], q[1], U), lerp(q[3], q[2], U), V), hom(U, V), persp) }
}
fun cone(origin: Offset, length: Float, r0: Float, r1: Float, yaw: Float = 24f, arc: Float = 60f, angle: Float = 0f, padU: Float = .04f): UVMap {
    val R = rot(origin, angle); val s = sin(yaw * D2R); val A = arc * D2R; val u1 = 1 - (2 * r1 * s + 8) / length
    return { u, v -> val U = padU + u * (u1 - padU); val x = U * length; val r = r0 + (r1 - r0) * U; val th = A * (1 - 2 * v)
        if (cos(th) < .05f) null else R(Offset(x + r * cos(th) * s, -r * sin(th))) }
}
fun column(cx: Float, r: Float, bandTop: Float, bandBottom: Float, arc: Float = 70f, tilt: Float = .16f, spin: Float = 0f): UVMap {
    val b = r * tilt; val A = arc * D2R
    return { u, v -> val th = -A + 2 * A * u + spin * D2R; if (cos(th) < .05f) null else Offset(cx + r * sin(th), bandTop + (bandBottom - bandTop) * v + b * cos(th)) }
}
fun sphere(c: Offset, R: Float, lon: Float = 70f, lat: Float = 22f, tilt: Float = 14f): UVMap {
    val Lo = lon * D2R; val La = lat * D2R; val ct = cos(tilt * D2R); val st = sin(tilt * D2R)
    return { u, v -> val lo = -Lo + 2 * Lo * u; val la = La * (1 - 2 * v); val y = R * sin(la); val z = R * cos(la) * cos(lo); val x = R * cos(la) * sin(lo)
        if (y * st + z * ct < R * .05f) null else Offset(c.x + x, c.y - (y * ct - z * st)) }
}
/** Брус, перелом через боковое ребро: fold — доля u до ребра (вставить в сетку как обязательный узел). */
fun boxSide(x: Float, y0: Float, y1: Float, w: Float, d: Float, fold: Float): UVMap {
    val dx = d * cos(17 * D2R); val dy = -d * sin(17 * D2R)
    return { u, v -> val yy = y0 + (y1 - y0) * v; if (u <= fold) Offset(x + u / fold * w, yy) else { val s = (u - fold) / (1 - fold); Offset(x + w + s * dx, yy + s * dy) } }
}
/** Брус, перелом через верхнее ребро: верхние f ≤ 0.2 высоты букв уходят на крышку в натуральном размере. */
fun boxTop(x: Float, top: Float, w: Float, d: Float, capH: Float, f: Float): UVMap {
    val dx = d * cos(17 * D2R); val dy = -d * sin(17 * D2R); val fc = min(f, .2f); val Hc = capH / (1 - fc)
    return { u, v -> val X = x + 8 + u * (w - 16); if (v >= fc) Offset(X, top + (v - fc) * Hc) else { val s = (fc - v) * Hc / d; Offset(X + s * dx, top + s * dy) } }
}
/** Несколько строк на одной форме: строка i из n — своя полоса v. gap вмещает Й/Ё и Д/Ц/Щ: gap/(1−gap) ≥ 0.54. */
fun lineMap(map: UVMap, i: Int, n: Int, gap: Float = .36f): UVMap { val a = i.toFloat() / n + gap / (2 * n); val k = (1 - gap) / n; return { u, v -> map(u, a + v * k) } }

// ── сетка для drawVertices ────────────────────────────────────
// Возвращает позиции, UV в пикселях битмапа и индексы треугольников. Ячейки с невидимой вершиной пропускаются.
class Mesh(val positions: FloatArray, val texCoords: FloatArray, val indices: ShortArray)
fun buildMesh(map: UVMap, bmpW: Int, bmpH: Int, nu: Int = 40, nv: Int = 8, uFolds: List<Float> = emptyList(), vFolds: List<Float> = emptyList()): Mesh {
    val U = ((0..nu).map { it.toFloat() / nu } + uFolds).distinct().sorted()
    val V = ((0..nv).map { VMIN + (VMAX - VMIN) * it / nv } + vFolds).distinct().sorted()
    val pos = ArrayList<Float>(); val tex = ArrayList<Float>(); val idx = ArrayList<Short>(); val id = HashMap<Int, Short>()
    fun vert(i: Int, j: Int): Short? { val key = i * 1000 + j; id[key]?.let { return it }
        val p = map(U[i], V[j]) ?: return null
        pos += p.x; pos += p.y; tex += U[i] * bmpW; tex += (V[j] - VMIN) / (VMAX - VMIN) * bmpH
        val n = (pos.size / 2 - 1).toShort(); id[key] = n; return n }
    for (i in 0 until U.size - 1) for (j in 0 until V.size - 1) {
        val a = vert(i, j) ?: continue; val b = vert(i + 1, j) ?: continue; val c = vert(i + 1, j + 1) ?: continue; val d = vert(i, j + 1) ?: continue
        idx += a; idx += b; idx += c; idx += a; idx += c; idx += d
    }
    return Mesh(pos.toFloatArray(), tex.toFloatArray(), idx.toShortArray())
}

/*
Использование (DrawScope, Skia/Android):

  val bmp = remember(text, color) { rasterizeText(text, RubikMonoOne, 240.sp, color) }   // cap-top на v = 0, baseline на v = 1, поля VMIN/VMAX
  Canvas(Modifier.size(390.dp, 392.dp).semantics { contentDescription = text }) {
      drawPath(hornPath, Plakat.red)                                                  // грань — плоская краска
      val m = buildMesh(horn(Offset(16f, 312f), 358f, 56f).map, bmp.width, bmp.height)
      drawIntoCanvas { c ->
          val paint = Paint().apply { shader = ImageShader(bmp, TileMode.Clamp, TileMode.Clamp) }
          c.nativeCanvas.drawVertices(VertexMode.TRIANGLES, m.positions.size, m.positions, 0, m.texCoords, 0, null, 0, m.indices, 0, m.indices.size, paint.asFrameworkPaint())
      }
  }
  // Пересчёт сетки — только при смене текста, размера или параметров формы. Плакат статичен; появление — шторкой (clip), не масштабом.
*/
