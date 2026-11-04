package io.github.youndie.plakat.theme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import java.io.File
import java.nio.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

/** B-14: Blink's line box, and the face metrics it is computed from held to the bundled files. */
@OptIn(ExperimentalTestApi::class)
class CssLineTest {
    /** `hhea`'s ascender and descender over `head`'s units per em, read from the font file itself. */
    private fun hhea(file: String): Pair<Float, Float> {
        val b = ByteBuffer.wrap(File("src/commonMain/composeResources/font/$file").readBytes())
        val tables = b.getShort(4).toInt()

        fun offset(tag: String) =
            (0 until tables).map { 12 + it * 16 }.first { r -> String(ByteArray(4) { b.get(r + it) }) == tag }.let {
                b.getInt(it + 8)
            }
        val upm = b.getShort(offset("head") + 18).toInt()
        val h = offset("hhea")
        return b.getShort(h + 4).toFloat() / upm to -b.getShort(h + 6).toFloat() / upm
    }

    @Test
    fun `the face metrics are the files'`() {
        for (file in listOf("golos_text_regular.ttf", "golos_text_semibold.ttf")) {
            assertEquals(PlakatFaceMetrics.Body.ascent to PlakatFaceMetrics.Body.descent, hhea(file), file)
        }
        val display = PlakatFaceMetrics.Display
        assertEquals(display.ascent to display.descent, hhea("rubik_mono_one_regular.ttf"))
    }

    @Test
    fun `a line box puts the baseline where Blink does`() =
        runComposeUiTest {
            val baselines = mutableListOf<Float>()
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    PlakatTheme {
                        val measurer = rememberTextMeasurer()
                        val body = PlakatTheme.typography.bodyL.style
                        // Golos 17 px on a `normal` line: ascent 16.66 → 17, descent 3.74 → 4, the line
                        // 21, the baseline in its 17th pixel.
                        val f = measurer.measure("anna", body.cssLine(PlakatFaceMetrics.Body))
                        // Golos 14 px on 1.35: the line 18.9, the half-leading 0.95 floored to 0, the
                        // baseline in the 14th pixel; Compose's own centring puts it in the 15th.
                        val m =
                            measurer.measure(
                                "ОШИБКА",
                                body.copy(fontSize = 14.sp).cssLine(PlakatFaceMetrics.Body, 1.35f),
                            )
                        val plain =
                            measurer.measure(
                                "ОШИБКА",
                                body.copy(fontSize = 14.sp, lineHeight = (14 * 1.35f).sp),
                            )
                        assertEquals(21, f.size.height)
                        assertEquals(19, m.size.height)
                        baselines += listOf(f.firstBaseline, m.firstBaseline, plain.firstBaseline)
                    }
                }
            }
            waitForIdle()
            val (field, message, plain) = baselines.map { kotlin.math.round(it).toInt() }
            assertEquals(17, field)
            assertEquals(14, message)
            assertEquals(15, plain, "without the line box, a pixel low")
        }
}
