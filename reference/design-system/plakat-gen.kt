// plakat-gen.kt — порт plakat-gen.js для Compose Multiplatform (референс для реализации).
// Контракт: тот же seed → те же фигуры, что на вебе. Все координаты — dp в кадре W × H.
package plakat.gen

import kotlin.math.*

// ── RNG: mulberry32, побитово совпадает с JS ─────────────────
class Mulberry32(seed: Int) {
    private var a = seed
    fun next(): Double {
        a += 0x6D2B79F5
        var t = (a xor (a ushr 15)) * (1 or a)
        t = (t + ((t xor (t ushr 7)) * (61 or t))) xor t
        return ((t xor (t ushr 14)).toUInt().toDouble()) / 4294967296.0
    }
}
private class Gen(seed: Int) {
    val r = Mulberry32(seed)
    fun f() = r.next()
    fun int(a: Int, b: Int) = a + floor(f() * (b - a + 1)).toInt()
    fun <T> pick(xs: List<T>) = xs[floor(f() * xs.size).toInt()]
    fun s8(a: Double, b: Double) = snap(a + f() * (b - a))
    fun bool(p: Double = .5) = f() < p
}
private fun snap(v: Double) = Math.round(v / 8.0) * 8.0   // JS Math.round: half up
private val T17 = tan(17.0 * PI / 180.0)
const val AXIS = -17.0

enum class Role { Red, Ink, Concrete }
sealed class Shape { abstract val role: Role
    data class Rect(val x: Double, val y: Double, val w: Double, val h: Double, override val role: Role,
                    val rot: Double = 0.0, val ox: Double = 0.0, val oy: Double = 0.0) : Shape()
    data class Circle(val cx: Double, val cy: Double, val r: Double, override val role: Role) : Shape()
    data class Poly(val pts: List<Pair<Double, Double>>, override val role: Role) : Shape()
}
data class Zone(val x: Double, val y: Double, val w: Double, val h: Double)
data class Generated(val seed: Int, val w: Double, val h: Double, val head: Zone?, val shapes: List<Shape>, val elements: List<String>, val red: String?)

// ── generate(seed): случайная композиция из всего словаря ────
// Порядок ELEMENTS = порядок отрисовки и совпадает с JS. Правила: сетка 8, ось −17°, один красный ≤ 1/3 кадра,
// чёрное и красное не заходят в зону заголовка (серая плашка — может).
val ELEMENTS = listOf("plate", "strip", "wedge", "circle", "columns", "checker", "ground", "bar", "rail", "dot")
private val RED_OK = setOf("circle", "wedge", "bar", "dot", "columns", "checker", "rail")
private fun area(s: Shape): Double = when (s) {
    is Shape.Circle -> PI * s.r * s.r
    is Shape.Rect -> s.w * s.h
    is Shape.Poly -> { var A = 0.0; for (i in s.pts.indices) { val (x1, y1) = s.pts[i]; val (x2, y2) = s.pts[(i + 1) % s.pts.size]; A += x1 * y2 - x2 * y1 }; abs(A) / 2 }
}
private fun Shape.recolor(r: Role): Shape = when (this) { is Shape.Rect -> copy(role = r); is Shape.Circle -> copy(role = r); is Shape.Poly -> copy(role = r) }
private fun Shape.shift(dx: Double, dy: Double): Shape = when (this) {
    is Shape.Rect -> copy(x = x + dx, y = y + dy, ox = ox + dx, oy = oy + dy); is Shape.Circle -> copy(cx = cx + dx, cy = cy + dy)
    is Shape.Poly -> copy(pts = pts.map { (x, y) -> (x + dx) to (y + dy) })
}
private fun topOf(s: Shape) = when (s) { is Shape.Circle -> s.cy; is Shape.Rect -> s.y; is Shape.Poly -> s.pts.minOf { it.second } }

fun generate(seed: Int, w: Double = 360.0, h: Double = 240.0, head: Zone? = null, noHead: Boolean = false): Generated {
    val W = w; val H = h; val g = Gen(seed); val M = min(W, H)
    val hd = if (noHead) null else head ?: Zone(16.0, 16.0, g.s8(W * .45, W * .6), g.s8(H * .26, H * .38))
    val make: Map<String, () -> List<Shape>> = mapOf(
        "plate" to { val pw = g.s8(W * .3, W * .6); val ph = g.s8(H * .25, H * .55); listOf(Shape.Rect(g.pick(listOf(0.0, W - pw, g.s8(0.0, W - pw))), g.s8(0.0, H - ph), pw, ph, Role.Concrete)) },
        "strip" to { val cell = g.pick(listOf(12.0, 16.0)); val t = rapport(g.int(1, 99999), cell); val y0 = g.s8(0.0, H - 2 * cell); val out = mutableListOf<Shape>()
            var k = 0; while (k * t.w < W) { for (s in t.shapes) { if (topOf(s) >= 2 * cell) continue; out += s.shift(k * t.w, y0).let { if (it.role == Role.Red) it.recolor(Role.Ink) else it } }; k++ }; out },
        "wedge" to { val ww = g.s8(W * .35, W * .9); val wh = ww * T17; val x = g.s8(0.0, W - ww * .5); val y = g.s8(wh + 8, H)
            listOf(Shape.Poly(if (g.bool(.6)) listOf(x to y, x + ww to y - wh, x + ww to y) else listOf(x to y - wh, x + ww to y - wh, x to y), Role.Ink)) },
        "circle" to { val r = g.s8(M * .18, M * .34); listOf(Shape.Circle(g.s8(r * .3, W - r * .3), g.s8(r * .3, H + r * .3), r, Role.Ink)) },
        "columns" to { val n = g.int(3, 5); val cw = g.pick(listOf(16.0, 24.0, 32.0)); val gap = g.pick(listOf(8.0, 16.0)); val x0 = g.s8(0.0, W - (n * cw + (n - 1) * gap)); val base = H - g.pick(listOf(0.0, 8.0))
            List(n) { i -> val ch = g.s8(H * .25, H * .6); Shape.Rect(x0 + i * (cw + gap), base - ch, cw, ch, Role.Ink) } },
        "checker" to { val cols = g.int(3, 5); val rows = g.int(2, 3); val c = g.pick(listOf(24.0, 32.0, 40.0)); val x0 = g.s8(0.0, W - cols * c); val y0 = g.s8(0.0, H - rows * c); val par = g.int(0, 1)
            List(cols * rows) { i -> val cx = i % cols; val cy = i / cols; Shape.Rect(x0 + cx * c, y0 + cy * c, c, c, if ((cx + cy + par) % 2 == 1) Role.Concrete else Role.Ink) } },
        "ground" to { listOf(Shape.Rect(0.0, H - 8, W, 8.0, Role.Ink)) },
        "bar" to { val bh = g.pick(listOf(8.0, 16.0, 24.0)); val bw = g.s8(W * .4, W * 1.1); val rot = g.pick(listOf(AXIS, AXIS, AXIS, 0.0, 90.0)); val x = g.s8(-W * .1, W * .6); val y = g.s8(H * .2, H * .95)
            listOf(Shape.Rect(x, y, bw, bh, Role.Ink, rot, x, y + bh / 2)) },
        "rail" to { val bh = g.pick(listOf(8.0, 16.0, 24.0)); val x0 = g.s8(-16.0, W * .3); val yb = g.s8(H * .55, H); val l1 = g.s8(W * .2, W * .5); val gap = g.pick(listOf(24.0, 32.0, 40.0))
            listOf(Shape.Rect(x0, yb - bh / 2, l1, bh, Role.Ink, AXIS, x0, yb), Shape.Rect(x0 + l1 + gap, yb - bh / 2, W * 1.2, bh, Role.Ink, AXIS, x0, yb)) },
        "dot" to { val r = g.pick(listOf(8.0, 12.0, 16.0, 24.0)); listOf(Shape.Circle(g.s8(r + 8, W - r - 8), g.s8(r + 8, H - r - 8), r, Role.Ink)) }
    )
    // состав: одна крупная форма + всего 3–6 элементов (LinkedHashSet — порядок вставки не важен, обходим ELEMENTS)
    val chosen = mutableSetOf(g.pick(listOf("circle", "wedge"))); val n = g.int(3, 6)
    while (chosen.size < n) chosen += g.pick(ELEMENTS)
    fun blocked(s: Shape) = hd != null && s.role != Role.Concrete && hitsZone(s, hd)
    val groups = mutableListOf<Pair<String, MutableList<Shape>>>()
    for (el in ELEMENTS) { if (el !in chosen) continue
        for (i in 0 until 24) { val sh = make.getValue(el)(); if (sh.none { blocked(it) }) { groups += el to sh.toMutableList(); break } } }
    data class Cand(val gi: Int, val j: Int, val a: Double, val big: Boolean)
    val cands = groups.flatMapIndexed { gi, (el, sh) -> if (el in RED_OK) sh.mapIndexedNotNull { j, s -> if (s.role == Role.Ink) Cand(gi, j, area(s), el == "circle" || el == "wedge") else null } else emptyList() }
    val fit = cands.filter { it.a <= W * H / 3 }; val pref = fit.filter { it.big }
    val from = if (pref.isNotEmpty() && g.bool(.6)) pref else if (fit.isNotEmpty()) fit else cands.sortedBy { it.a }.take(1)
    var red: String? = null
    if (from.isNotEmpty()) { val c = g.pick(from); groups[c.gi].second[c.j] = groups[c.gi].second[c.j].recolor(Role.Red); red = groups[c.gi].first }
    return Generated(seed, W, H, hd, groups.flatMap { it.second }, groups.map { it.first }, red)
}

// ── зона заголовка ──────────────────────────────────────────
fun hitsZone(s: Shape, z: Zone): Boolean {
    if (s is Shape.Circle) { val cx = s.cx.coerceIn(z.x, z.x + z.w); val cy = s.cy.coerceIn(z.y, z.y + z.h); return hypot(s.cx - cx, s.cy - cy) < s.r }
    val p = polyOf(s); fun inZ(x: Double, y: Double) = x > z.x && x < z.x + z.w && y > z.y && y < z.y + z.h
    for (i in p.indices) { val (ax, ay) = p[i]; val (bx, by) = p[(i + 1) % p.size]; var t = 0.0; while (t <= 1.0) { if (inZ(ax + (bx - ax) * t, ay + (by - ay) * t)) return true; t += .04 } }
    return listOf(z.x to z.y, z.x + z.w to z.y, z.x to z.y + z.h, z.x + z.w to z.y + z.h).any { inPoly(it.first, it.second, p) }
}
private fun polyOf(s: Shape): List<Pair<Double, Double>> = when (s) {
    is Shape.Poly -> s.pts
    is Shape.Rect -> { val p = listOf(s.x to s.y, s.x + s.w to s.y, s.x + s.w to s.y + s.h, s.x to s.y + s.h)
        if (s.rot == 0.0) p else { val a = s.rot * PI / 180; val c = cos(a); val n = sin(a); p.map { (x, y) -> (s.ox + (x - s.ox) * c - (y - s.oy) * n) to (s.oy + (x - s.ox) * n + (y - s.oy) * c) } } }
    is Shape.Circle -> emptyList()
}
private fun inPoly(x: Double, y: Double, p: List<Pair<Double, Double>>): Boolean { var c = false; var j = p.size - 1
    for (i in p.indices) { val (xi, yi) = p[i]; val (xj, yj) = p[j]; if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) c = !c; j = i }; return c }

// ── растр ───────────────────────────────────────────────────
// Rapport: период 2tw × 2th клеток; во втором мотиве клин зеркалится. Тайлим через ShaderBrush(ImageShader(bitmap, TileMode.Repeated)).
data class Rapport(val w: Double, val h: Double, val shapes: List<Shape>)
fun rapport(seed: Int, cell: Double = 24.0): Rapport {
    val g = Gen(seed); val tw = g.pick(listOf(2, 3, 4)); val th = g.pick(listOf(2, 3))
    val motifs = listOf("sq", "tri1", "tri2", "half", "circle", "bar", "empty"); val redMotif = g.int(0, tw * th - 1)
    val tile = List(tw * th) { i -> if (i == redMotif) g.pick(listOf("circle", "tri1", "sq")) to Role.Red else g.pick(motifs) to g.pick(listOf(Role.Ink, Role.Ink, Role.Concrete)) }
    val out = mutableListOf<Shape>(); val c = cell
    for (y in 0 until 2 * th) for (x in 0 until 2 * tw) {
        val (m0, f) = tile[(y % th) * tw + (x % tw)]; if (m0 == "empty") continue
        val flip = ((x / tw) + (y / th)) % 2 == 1
        val m = if (flip && m0 == "tri1") "tri2" else if (flip && m0 == "tri2") "tri1" else m0
        val X = x * c; val Y = y * c
        out += when (m) {
            "sq" -> Shape.Rect(X, Y, c, c, f); "half" -> Shape.Rect(X, Y, c / 2, c, f); "bar" -> Shape.Rect(X, Y + c * .375, c, c * .25, f)
            "circle" -> Shape.Circle(X + c / 2, Y + c / 2, c * .375, f)
            "tri1" -> Shape.Poly(listOf(X to Y + c, X + c to Y, X + c to Y + c), f)
            else -> Shape.Poly(listOf(X to Y, X + c to Y, X to Y + c), f)
        }
    }
    return Rapport(2 * tw * c, 2 * th * c, out)
}

// API 33+: RuntimeShader(HALFTONE_AGSL) → Modifier.graphicsLayer { renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "image").asComposeRenderEffect() }
// Desktop/iOS: тот же текст через Skia RuntimeEffect.
const val HALFTONE_AGSL = """
uniform shader image;
uniform float pitch;      // px, = 4.dp
uniform float contrast;   // 1.4
uniform half4 ink;
uniform half4 paper;
half4 main(float2 p) {
    const float a = 0.7853981;                 // 45°
    float2x2 R = float2x2(cos(a), -sin(a), sin(a), cos(a));
    float2 q = R * p;
    float2 cell = (floor(q / pitch) + 0.5) * pitch;
    half3 c = image.eval(transpose(R) * cell).rgb;
    float l = clamp((dot(c, half3(0.299, 0.587, 0.114)) - 0.5) * contrast + 0.5, 0.0, 1.0);
    float r = pitch * 0.7071 * sqrt(1.0 - l);
    return length(q - cell) < r ? ink : paper;
}
"""

enum class HalftoneMode { Dots, Mask }

// API < 33: считаем один раз, кладём в кэш по (uri, size, mode, pitch).
// Вход — luma 0..1 построчно (w*h), выход — команды для Canvas: круги (Dots) или маска (Mask).
fun halftoneDots(luma: FloatArray, w: Int, h: Int, pitch: Float, contrast: Float = 1.4f, angleDeg: Float = 45f): List<Triple<Float, Float, Float>> {
    val a = angleDeg * PI.toFloat() / 180f; val ca = cos(a); val sa = sin(a); val d = hypot(w.toFloat(), h.toFloat()); val rmax = pitch * 0.70710677f
    fun l(x: Float, y: Float): Float { val i = (y.toInt().coerceIn(0, h - 1)) * w + x.toInt().coerceIn(0, w - 1); return ((luma[i] - .5f) * contrast + .5f).coerceIn(0f, 1f) }
    val out = ArrayList<Triple<Float, Float, Float>>()
    var v = -d; while (v < d) { var u = -d; while (u < d) {
        val x = w / 2f + u * ca - v * sa; val y = h / 2f + u * sa + v * ca
        if (x >= -pitch && y >= -pitch && x <= w + pitch && y <= h + pitch) {
            val lv = (l(x, y) + l(x - 1, y) + l(x + 1, y) + l(x, y - 1) + l(x, y + 1)) / 5f
            val r = rmax * sqrt(1f - lv); if (r > .35f) out += Triple(x, y, r)
        }
        u += pitch }; v += pitch }
    return out   // drawScope: drawRect(paper); out.forEach { drawCircle(ink, it.third, Offset(it.first, it.second)) }
}
fun halftoneMask(luma: FloatArray, contrast: Float = 1.4f, threshold: Float = .5f): BooleanArray =
    BooleanArray(luma.size) { ((luma[it] - .5f) * contrast + .5f) < threshold }   // true = ink; в ImageBitmap 1 раз
