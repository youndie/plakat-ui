// plakat-raskat.kt — «Раскат» для Compose (референс). Токен plakat/print/raskat; выключен → плоская краска A.
package plakat.raskat

// API 33+: RuntimeShader(RASKAT_AGSL) → ShaderBrush; рисуем фигуру этим кистью (drawCircle / drawPath).
// Desktop/iOS — тот же текст через Skia RuntimeEffect.
const val RASKAT_AGSL = """
uniform float2 origin;   // левый верх фигуры, px
uniform float2 size;     // размер фигуры, px
uniform float angle;     // радианы, ось −17° = -0.2967
uniform float t0;        // линия перехода 0.3…0.7
uniform float hard;      // жёсткость 2…30
uniform float grain;     // шаг зерна, px (= grainDp * density)
uniform float seed;
uniform float mode;      // 0 зерно, 1 растр 45°, 2 штрих
uniform float streak;    // 0…1 волокно валика (только зерно)
uniform half4 inkA;
uniform half4 inkB;

float hash(float2 p) { return fract(sin(dot(p, float2(127.1, 311.7)) + seed) * 43758.5453); }
float vnoise(float2 p) {
    float2 i = floor(p); float2 f = fract(p); f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1, 0)), f.x), mix(hash(i + float2(0, 1)), hash(i + float2(1, 1)), f.x), f.y);
}
half4 main(float2 p) {
    float2 d = float2(cos(angle), sin(angle));
    float2 q = p - origin - size * 0.5;
    float across = dot(q, d), along = dot(q, float2(-d.y, d.x));
    float t = across / (abs(d.x) * size.x + abs(d.y) * size.y) + 0.5;
    float pr = 1.0 / (1.0 + exp(-hard * (t - t0)));
    float pitch = grain * 4.0, m;
    if (mode < 0.5) {
        m = 0.6 * vnoise(float2(along / (grain * (1.0 + streak * 10.0)), across / grain)) + 0.4 * hash(floor(p));
    } else if (mode < 1.5) {
        float2 uv = float2(p.x + p.y, p.y - p.x) * 0.7071 / pitch;
        float dd = length(uv - floor(uv + 0.5)) * pitch;
        m = pow(dd / (pitch * 0.7071), 2.0) + (hash(floor(p)) - 0.5) * 0.09;
    } else {
        float l = across / pitch + (vnoise(float2(along / (grain * 3.0), across / grain)) - 0.5) * 0.18;
        m = fract(l);
    }
    return pr > m ? inkB : inkA;
}
"""
// roll: анимируем только uniform t0 от 1.15 до цели за 320 мс (hard-stop) — шейдер не пересобирается.
// API < 33: порог m считаем один раз в FloatArray, на кадр — только сравнение p(t) > m.
fun raskatPixels(w: Int, h: Int, inkA: Int, inkB: Int, angleDeg: Float = -17f, t0: Float = .5f, hard: Float = 10f, grainPx: Float = 4f, seed: Float = 1919f): IntArray {
    fun fract(x: Float) = x - kotlin.math.floor(x)
    fun hash(x: Float, y: Float) = fract(kotlin.math.sin(x * 127.1f + y * 311.7f + seed) * 43758.5453f)
    fun vnoise(x: Float, y: Float): Float { val xi = kotlin.math.floor(x); val yi = kotlin.math.floor(y); var fx = x - xi; var fy = y - yi
        fx = fx * fx * (3 - 2 * fx); fy = fy * fy * (3 - 2 * fy)
        val a = hash(xi, yi); val b = hash(xi + 1, yi); val c = hash(xi, yi + 1); val d = hash(xi + 1, yi + 1)
        return (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy }
    val r = Math.toRadians(angleDeg.toDouble()); val dx = kotlin.math.cos(r).toFloat(); val dy = kotlin.math.sin(r).toFloat()
    val ext = kotlin.math.abs(dx) * w + kotlin.math.abs(dy) * h
    return IntArray(w * h) { k -> val i = k % w; val j = k / w
        val t = ((i - w / 2f) * dx + (j - h / 2f) * dy) / ext + .5f
        val p = 1f / (1f + kotlin.math.exp(-hard * (t - t0)))
        val n = .6f * vnoise(i / grainPx, j / grainPx) + .4f * hash(i.toFloat(), j.toFloat())
        if (p > n) inkB else inkA }
}
