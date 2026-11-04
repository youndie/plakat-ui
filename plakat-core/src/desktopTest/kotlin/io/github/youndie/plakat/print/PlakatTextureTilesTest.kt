package io.github.youndie.plakat.print

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-07: the eight bundled tiles wrap without a seam, the five delivered ones are the designer's pixels,
 * and the three made here are what research D16 says they are — read a second time, here, from the
 * vendored dark paper and the tokens, independently of `scripts/textures.py`.
 */
class PlakatTextureTilesTest {
    private val bundled = File("src/commonMain/composeResources/drawable")
    private val design = File("../reference/design-system")
    private val tokens = Json.parseToJsonElement(File(design, "plakat-tokens.json").readText()).jsonObject

    private fun read(file: File): BufferedImage = ImageIO.read(file).also { checkNotNull(it) { "$file is no image" } }

    private fun tile(name: String) = read(File(bundled, "$name.png"))

    private fun channels(argb: Int) = intArrayOf(argb ushr 16 and 255, argb ushr 8 and 255, argb and 255, argb ushr 24)

    private fun token(
        name: String,
        theme: String,
    ): IntArray {
        val hex =
            tokens["plakat/color"]!!
                .jsonObject[name]!!
                .jsonObject[theme]!!
                .jsonPrimitive.content
        return intArrayOf(hex.substring(1, 3).toInt(16), hex.substring(3, 5).toInt(16), hex.substring(5, 7).toInt(16))
    }

    /** The difference between column [x] and the next one (the first, after the last), over every channel. */
    private fun BufferedImage.columnStep(x: Int): Long {
        var sum = 0L
        for (y in 0 until height) {
            val a = channels(getRGB(x, y))
            val b = channels(getRGB((x + 1) % width, y))
            for (k in 0 until 4) sum += abs(a[k] - b[k])
        }
        return sum
    }

    private fun BufferedImage.rowStep(y: Int): Long {
        var sum = 0L
        for (x in 0 until width) {
            val a = channels(getRGB(x, y))
            val b = channels(getRGB(x, (y + 1) % height))
            for (k in 0 until 4) sum += abs(a[k] - b[k])
        }
        return sum
    }

    private fun List<Long>.median() = sorted()[size / 2].coerceAtLeast(1)

    /**
     * How far the wraps (last column to the first, last row to the first) stand out from the median
     * neighbour difference. Inner rows and columns are not held: a fibre across the paper puts single
     * ones at up to 4.7 times the median, and they are the texture, not a seam.
     */
    private fun BufferedImage.seam(): Pair<Double, Double> {
        val cols = (0 until width).map { columnStep(it) }
        val rows = (0 until height).map { rowStep(it) }
        return cols.last().toDouble() / cols.median() to rows.last().toDouble() / rows.median()
    }

    @Test
    fun `every tile wraps without a seam`() {
        val names =
            bundled
                .listFiles()!!
                .filter { it.extension == "png" }
                .map { it.nameWithoutExtension }
                .sorted()
        assertEquals(TILES.sorted(), names, "the bundled tiles")
        for (name in names) {
            val (col, row) = tile(name).seam()
            assertTrue(
                col <= SEAM && row <= SEAM,
                "$name: wrap is ${"%.2f".format(col)} × median across, ${"%.2f".format(row)} × down",
            )
        }
    }

    @Test
    fun `a tile cut short shows its seam`() {
        // The positive control: the dark paper cut to 400 px joins pixels that never met.
        val cut = tile("paper_dark").getSubimage(0, 0, 400, 400)
        val (col, row) = cut.seam()
        assertTrue(col > SEAM && row > SEAM, "a cut tile: ${"%.2f".format(col)} across, ${"%.2f".format(row)} down")
    }

    @Test
    fun `the delivered tiles are the designer's pixels`() {
        for (name in DELIVERED) {
            val ours = tile(name)
            val theirs = read(File(design, "textures/${name.replace('_', '-')}-2x.png"))
            assertEquals(theirs.width to theirs.height, ours.width to ours.height, name)
            for (y in 0 until ours.height) {
                for (x in 0 until ours.width) assertEquals(theirs.getRGB(x, y), ours.getRGB(x, y), "$name at $x, $y")
            }
        }
    }

    @Test
    fun `the opaque papers are their token minus the dark paper's grain`() {
        val dark = read(File(design, "textures/paper-dark-2x.png"))
        val darkToken = token("paper", "dark")
        for ((name, base) in listOf(
            "paper_light" to token("paper", "light"),
            "concrete_light" to token("concrete", "light"),
        )) {
            val made = tile(name)
            for (y in 0 until made.height) {
                for (x in 0 until made.width) {
                    val d = channels(dark.getRGB(x, y))
                    val m = channels(made.getRGB(x, y))
                    for (k in 0 until 3) {
                        val want = min(255, max(0, base[k] - (d[k] - darkToken[k])))
                        assertEquals(want, m[k], "$name at $x, $y, channel $k")
                    }
                    assertEquals(255, m[3], "$name is opaque at $x, $y")
                }
            }
        }
    }

    @Test
    fun `the grain darkens the light paper by the dark paper's grain`() {
        // Multiplied at the token's opacity over flat light paper, the grain is to take off each pixel's
        // luminance what the dark paper adds to its own: within one level of 8-bit alpha.
        val dark = read(File(design, "textures/paper-dark-2x.png"))
        val grain = tile("paper_grain")
        val darkToken = token("paper", "dark")
        val light = token("paper", "light")
        val opacity =
            tokens["plakat/print"]!!
                .jsonObject["paper-texture"]!!
                .jsonObject["opacity"]!!
                .jsonPrimitive.double
        // The Tokens page's grain colour, its `feColorMatrix` row: .42 .34 .20 of white.
        val ink = listOf(.42, .34, .20).map { (it * 255).roundToInt() }

        fun lum(c: IntArray) = 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]
        var inked = 0
        for (y in 0 until grain.height) {
            for (x in 0 until grain.width) {
                val g = channels(grain.getRGB(x, y))
                val d = channels(dark.getRGB(x, y))
                val dev = max(0.0, lum(IntArray(3) { d[it] - darkToken[it] }))
                val perAlpha = lum(light) * (1 - lum(g) / 255) * opacity / 255
                assertTrue(
                    abs(g[3] * perAlpha - dev) <= perAlpha / 2 + 1e-9,
                    "paper_grain at $x, $y: alpha ${g[3]} for $dev",
                )
                if (g[3] > 0) inked++
                assertEquals(ink, g.take(3), "the grain's colour at $x, $y")
            }
        }
        assertTrue(inked > 0, "the grain inks somewhere")
    }

    private companion object {
        val DELIVERED =
            listOf(
                "paper_dark",
                "nonprint_mask",
                "halftone_screen_25",
                "halftone_screen_50",
                "halftone_screen_75",
            )
        val TILES = DELIVERED + listOf("paper_light", "concrete_light", "paper_grain")

        /** Good tiles wrap at up to 1.25 times the median; a cut tile at 1.7 and more. */
        const val SEAM = 1.5
    }
}
