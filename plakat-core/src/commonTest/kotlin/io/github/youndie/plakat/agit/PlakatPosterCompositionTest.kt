package io.github.youndie.plakat.agit

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * B-19: the port of the Core page's own `comp` draws what the page's code draws, seed for seed, for
 * every way the page calls it, on every target this suite runs on. The goldens are the page's own
 * output (`scripts/poster-goldens.mjs`).
 */
class PlakatPosterCompositionTest {
    // The same frames, in the same order, as FRAMES in scripts/poster-goldens.mjs.
    private val frames =
        listOf(
            "circle" to PosterRecipes.Circle,
            "background" to PosterRecipes.Background,
            "banner" to PosterRecipes.Banner,
            "thumbnail" to PosterRecipes.Thumbnail,
        )

    /** The page's own strings for a value: whole numbers print bare, as JS prints them. */
    private fun px(v: Double): String {
        val n = v.toLong()
        return (if (n.toDouble() == v) n.toString() else v.toString()) + "px"
    }

    private fun letter(role: PosterRole) =
        when (role) {
            PosterRole.Red -> "R"
            PosterRole.Ink -> "I"
            PosterRole.Gray -> "G"
        }

    /** The canonical text of scripts/poster-goldens.mjs: each shape's CSS values, then the circle. */
    private fun canonical(c: PosterComposition): String {
        val shapes =
            c.shapes.joinToString(";") { s ->
                when (s) {
                    is PosterShape.Box -> {
                        val t = s.rotate?.let { "rotate(${it.toLong()}deg)" } ?: "none"
                        listOf(
                            px(s.x),
                            px(s.y),
                            px(s.w),
                            px(s.h),
                            letter(s.role),
                            "0",
                            "none",
                            t,
                            if (s.rotate ==
                                null
                            ) {
                                "50% 50%"
                            } else {
                                "0 50%"
                            },
                        )
                    }

                    is PosterShape.Round -> {
                        listOf(px(s.x), px(s.y), px(s.d), px(s.d), letter(s.role), "50%", "none", "none", "50% 50%")
                    }

                    is PosterShape.Wedge -> {
                        val clip = if (s.lowerRight) "polygon(0 100%,100% 0,100% 100%)" else "polygon(0 0,100% 0,0 100%)"
                        listOf(px(s.x), px(s.y), px(s.w), px(s.h), letter(s.role), "0", clip, "none", "50% 50%")
                    }
                }.joinToString(",")
            }
        val circle = c.circle?.let { "${px(it.x)},${px(it.y)},${px(it.d)}" } ?: "-999px,0px,0px"
        return "$shapes|$circle"
    }

    private fun compose(
        recipe: PosterRecipe,
        seed: Int,
    ) = posterComposition(seed, recipe)

    private fun fnv(text: String): String {
        var h = 0xcbf29ce484222325UL
        for (b in text.encodeToByteArray()) {
            h = h xor (b.toUByte().toULong())
            h *= 0x100000001b3UL
        }
        return h.toString(16).padStart(16, '0')
    }

    @Test
    fun `the first seeds of every frame match the page shape for shape`() {
        for ((name, recipe) in frames) {
            PosterGoldens.whole.getValue(name).forEachIndexed { i, expected ->
                assertEquals(expected, canonical(compose(recipe, i + 1)), "$name seed ${i + 1}")
            }
        }
    }

    @Test
    fun `every seed of every frame hashes as the page's does`() {
        for ((name, recipe) in frames) {
            val hashes = PosterGoldens.hashes.getValue(name)
            for (seed in 1..PosterGoldens.SEEDS) {
                val expected = hashes.substring((seed - 1) * 16, seed * 16)
                assertEquals(expected, fnv(canonical(compose(recipe, seed))), "$name seed $seed")
            }
        }
    }
}
