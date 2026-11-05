package io.github.youndie.plakat.a11y

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.youndie.plakat.tokens.PlakatColors
import io.github.youndie.plakat.tokens.PlakatPalettes
import io.github.youndie.plakat.tokens.PlakatPrint
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * B-36: every pair of words and ground the kit sets, read over the textures it sets them on. A letter is not read
 * against one fibre, so the ratio is taken as a mean over a square of [LETTER_PX] (12 dp of a @2x tile), and the worst
 * such square anywhere on the wrapped tile must hold 4.5.
 */
class ContrastOverTextureTest {
    private val dir = File("src/commonMain/composeResources/drawable")

    private fun tile(name: String): BufferedImage = ImageIO.read(File(dir, "$name.png"))

    private fun channel(v: Int): Double {
        val c = v / 255.0
        return if (c <= .04045) c / 12.92 else Math.pow((c + .055) / 1.055, 2.4)
    }

    private fun luminance(argb: Int) =
        .2126 * channel(argb shr 16 and 255) + .7152 * channel(argb shr 8 and 255) + .0722 * channel(argb and 255)

    private fun ratio(
        a: Double,
        b: Double,
    ) = (max(a, b) + .05) / (min(a, b) + .05)

    /** The worst letter-sized mean of [ratioAt], per pixel of a wrapped tile of [w] × [h]. */
    private fun worstSquare(
        w: Int,
        h: Int,
        ratioAt: (Int, Int) -> Double,
    ): Double {
        // A summed table over the tile wrapped once, so a square may cross its edge as a tiled ground does.
        val sum = Array(h + LETTER_PX + 1) { DoubleArray(w + LETTER_PX + 1) }
        for (y in 1..h + LETTER_PX) {
            for (x in 1..w + LETTER_PX) {
                val c = ratioAt((x - 1) % w, (y - 1) % h)
                sum[y][x] = c + sum[y - 1][x] + sum[y][x - 1] - sum[y - 1][x - 1]
            }
        }
        var worst = Double.MAX_VALUE
        for (y in 0 until h) {
            for (x in 0 until w) {
                val total =
                    sum[y + LETTER_PX][x + LETTER_PX] - sum[y][x + LETTER_PX] - sum[y + LETTER_PX][x] + sum[y][x]
                worst = min(worst, total / (LETTER_PX * LETTER_PX))
            }
        }
        return worst
    }

    /** [text] on the opaque [ground] tile: only the ground moves. */
    private fun worstSquare(
        text: Color,
        ground: BufferedImage,
    ): Double {
        val l = luminance(text.toArgb())
        return worstSquare(ground.width, ground.height) { x, y -> ratio(l, luminance(ground.getRGB(x, y))) }
    }

    /** Both colours through the grain, `multiply` at the token's opacity over the words and the ground alike. */
    private fun underGrain(
        text: Color,
        ground: Color,
    ): Double {
        val grain = tile("paper_grain")

        fun through(
            color: Int,
            over: Int,
        ): Int {
            val a = (over ushr 24) / 255.0 * PlakatPrint.PAPER_TEXTURE_OPACITY

            fun ch(shift: Int) =
                ((color shr shift and 255) * (1 - a + a * (over shr shift and 255) / 255.0)).toInt() shl shift
            return ch(16) or ch(8) or ch(0)
        }
        return worstSquare(grain.width, grain.height) { x, y ->
            val over = grain.getRGB(x, y)
            ratio(luminance(through(text.toArgb(), over)), luminance(through(ground.toArgb(), over)))
        }
    }

    private fun flat(
        text: Color,
        ground: Color,
    ) = ratio(luminance(text.toArgb()), luminance(ground.toArgb()))

    private fun holds(
        what: String,
        ratio: Double,
    ) = assertTrue(ratio >= 4.5, "$what: ${"%.3f".format(ratio)}")

    private val light: PlakatColors = PlakatPalettes.Paper
    private val dark: PlakatColors = PlakatPalettes.BlackPaper

    @Test
    fun `words hold on the light paper`() {
        val paper = tile("paper_light")
        holds("ink on paper", worstSquare(light.ink, paper))
        holds("ink-muted on paper", worstSquare(light.inkMuted, paper))
    }

    @Test
    fun `words hold on the light concrete`() {
        val concrete = tile("concrete_light")
        holds("ink on concrete", worstSquare(light.ink, concrete))
        holds("ink-muted on concrete", worstSquare(light.inkMuted, concrete))
    }

    @Test
    fun `words hold on the dark paper`() {
        val paper = tile("paper_dark")
        holds("ink on paper", worstSquare(dark.ink, paper))
        holds("ink-muted on paper", worstSquare(dark.inkMuted, paper))
    }

    @Test
    fun `words hold on the flat grounds, the texture off`() {
        holds("light ink-muted on concrete", flat(light.inkMuted, light.concrete))
        holds("dark ink-muted on concrete", flat(dark.inkMuted, dark.concrete))
        holds("light on-red on red", flat(light.onRed, light.red))
        holds("dark on-red on red", flat(dark.onRed, dark.red))
    }

    @Test
    fun `words hold under the grain overlay`() {
        holds("ink on paper", underGrain(light.ink, light.paper))
        holds("ink-muted on paper", underGrain(light.inkMuted, light.paper))
        holds("ink on concrete", underGrain(light.ink, light.concrete))
        holds("on-red on red", underGrain(light.onRed, light.red))
    }

    private companion object {
        const val LETTER_PX = 24
    }
}
