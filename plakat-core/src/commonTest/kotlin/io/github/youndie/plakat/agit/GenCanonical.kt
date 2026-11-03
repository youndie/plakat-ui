package io.github.youndie.plakat.agit

import kotlin.math.floor

// The canonical text of a composition, the same one scripts/gen-goldens.mjs writes from the JS:
// coordinates as integers of thousandths of a dp, so that no float printing takes part. JS's
// `Math.round` rounds a half up, hence the floor form.

private fun q(v: Double): String = floor(v * 1000 + 0.5).toLong().toString()

private fun role(r: PlakatRole) =
    when (r) {
        PlakatRole.Red -> "R"
        PlakatRole.Ink -> "I"
        PlakatRole.Concrete -> "C"
    }

internal fun canonical(s: PlakatShape): String =
    when (s) {
        is PlakatShape.Rect -> {
            "R" + listOf(s.x, s.y, s.w, s.h).joinToString(",") { q(it) } + "," + role(s.role) +
                if (s.rot != 0.0) "," + listOf(s.rot, s.ox, s.oy).joinToString(",") { q(it) } else ""
        }

        is PlakatShape.Circle -> {
            "C" + listOf(s.cx, s.cy, s.r).joinToString(",") { q(it) } + "," + role(s.role)
        }

        is PlakatShape.Poly -> {
            "P" + s.points.joinToString(" ") { "${q(it.first)},${q(it.second)}" } + "," + role(s.role)
        }
    }

internal fun canonical(c: PlakatComposition): String =
    c.elements.joinToString(",") + "|" + (c.red ?: "-") + "|" +
        (c.head?.let { listOf(it.x, it.y, it.w, it.h).joinToString(",") { v -> q(v) } } ?: "-") + "|" +
        c.shapes.joinToString(";") { canonical(it) }

internal fun canonical(t: PlakatRapport): String = "${q(t.w)}x${q(t.h)}|" + t.shapes.joinToString(";") { canonical(it) }

/** FNV-1a, 64 bits, over the text's UTF-8 bytes, as 16 hex digits. */
internal fun fnv(text: String): String {
    var h = 0xcbf29ce484222325uL
    for (b in text.encodeToByteArray()) {
        h = h xor (b.toUByte().toULong())
        h *= 0x100000001b3uL
    }
    return h.toString(16).padStart(16, '0')
}
