package io.github.youndie.plakat.agit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-03: the port draws what the vendored `plakat-gen.js` draws, seed for seed, on every target this
 * suite runs on (research D4, H2). The goldens are the JS's own output (`scripts/gen-goldens.mjs`).
 */
class PlakatGenTest {
    private class Frame(
        val name: String,
        val w: Double,
        val h: Double,
        val head: PlakatZone? = null,
        val noHead: Boolean = false,
    )

    // The same frames, in the same order, as FRAMES in scripts/gen-goldens.mjs.
    private val frames =
        listOf(
            Frame("emptyState", 360.0, 240.0),
            Frame("circlePoster", 392.0, 392.0),
            Frame("splash", 240.0, 400.0),
            Frame("panel", 358.0, 360.0, head = PlakatZone(0.0, 0.0, 72.0, 40.0)),
            Frame("thumbnail", 64.0, 64.0, noHead = true),
        )

    private fun Frame.compose(seed: Int) = generate(seed, w, h, head, noHead)

    @Test
    fun `the first seeds of every frame match the design shape for shape`() {
        for (f in frames) {
            GenGoldens.whole.getValue(f.name).forEachIndexed { i, expected ->
                assertEquals(expected, canonical(f.compose(i + 1)), "${f.name} seed ${i + 1}")
            }
        }
    }

    @Test
    fun `every seed of every frame matches the design`() {
        for (f in frames) {
            val hashes = GenGoldens.hashes.getValue(f.name)
            val wrong =
                (1..GenGoldens.SEEDS).filter { seed ->
                    fnv(canonical(f.compose(seed))) != hashes.substring((seed - 1) * 16, seed * 16)
                }
            assertTrue(wrong.isEmpty(), "${f.name}: ${wrong.size} seeds differ, first ${wrong.take(5)}")
        }
    }

    @Test
    fun `the vendored rapports and four hundred more match the design`() {
        for ((key, expected) in GenGoldens.rapports) {
            assertEquals(expected, canonical(rapport(key.first, key.second.toDouble())), "rapport $key")
        }
        val wrong =
            (1..GenGoldens.RAPPORT_SEEDS).filter { seed ->
                fnv(canonical(rapport(seed, 24.0))) != GenGoldens.rapportHashes.substring((seed - 1) * 16, seed * 16)
            }
        assertTrue(wrong.isEmpty(), "rapport: ${wrong.size} seeds differ, first ${wrong.take(5)}")
    }

    @Test
    fun `snap rounds a half up as the design Math round does`() {
        // 4 / 8 = 0.5 and 12 / 8 = 1.5: half-to-even would give 0 and 16.
        assertEquals(8.0, snap(4.0))
        assertEquals(16.0, snap(12.0))
        assertEquals(0.0, snap(-4.0))
    }

    @Test
    fun `every composition keeps the design's rules`() {
        for (seed in 1..500) {
            val c = generate(seed)
            assertEquals(1, c.shapes.count { it.role == PlakatRole.Red }, "seed $seed: one red element")
            val zone = c.head!!
            for (s in c.shapes) {
                if (s.role !=
                    PlakatRole.Concrete
                ) {
                    assertTrue(!hitsZone(s, zone), "seed $seed: ${s.role} in the title zone")
                }
            }
        }
    }
}
