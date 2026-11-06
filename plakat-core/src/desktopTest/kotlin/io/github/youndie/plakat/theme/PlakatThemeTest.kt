package io.github.youndie.plakat.theme

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** B-09: what [PlakatTheme] provides, and that a fixture can force each of its switches. */
@OptIn(ExperimentalTestApi::class)
class PlakatThemeTest {
    private class Seen {
        var reduce: Boolean? = null
        var colors: Any? = null
        var typography: PlakatTypography? = null
        var print: PlakatPrintSwitches? = null
        var strings: PlakatStrings? = null
    }

    private fun seen(
        content: @androidx.compose.runtime.Composable (@androidx.compose.runtime.Composable () -> Unit) -> Unit,
    ): Seen {
        val s = Seen()
        runComposeUiTest {
            setContent {
                content {
                    val reduce = PlakatTheme.reduceMotion
                    val colors = PlakatTheme.colors
                    val typography = PlakatTheme.typography
                    val print = PlakatTheme.print
                    val strings = PlakatTheme.strings
                    SideEffect {
                        s.reduce = reduce
                        s.colors = colors
                        s.typography = typography
                        s.print = print
                        s.strings = strings
                    }
                }
            }
            waitForIdle()
        }
        return s
    }

    @Test
    fun `by default the paper theme with every print layer and the desktop's motion`() {
        val s = seen { PlakatTheme(content = it) }
        assertEquals(PlakatPalettes.Paper, s.colors)
        assertFalse(s.reduce!!, "the desktop JVM has no reduce-motion setting")
        assertTrue(s.print!!.texture && s.print!!.nonprint && s.print!!.registration && s.print!!.raskat)
        assertEquals("Error", s.strings!!.error)
    }

    @Test
    fun `a fixture forces reduce motion the black paper and the print layers`() {
        val s =
            seen {
                PlakatTheme(
                    colors = PlakatPalettes.BlackPaper,
                    reduceMotion = true,
                    print = PlakatPrintSwitches(texture = false, raskat = false),
                    strings = PlakatStrings(error = "Ошибка"),
                    content = it,
                )
            }
        assertTrue(s.reduce!!)
        assertEquals(PlakatPalettes.BlackPaper, s.colors)
        assertFalse(s.print!!.texture)
        assertFalse(s.print!!.raskat)
        assertTrue(s.print!!.registration)
        assertEquals("Ошибка", s.strings!!.error)
    }

    @Test
    fun `the typography is the token scale in the kit's families`() {
        val t = seen { PlakatTheme(content = it) }.typography!!
        assertEquals(48.sp, t.displayM.style.fontSize)
        assertEquals((48 * .95f).sp, t.displayM.style.lineHeight)
        assertTrue(t.displayM.upperCase)
        assertEquals(FontWeight(600), t.title.style.fontWeight)
        assertFalse(t.bodyM.upperCase)
        assertEquals(.08f.em, t.label.style.letterSpacing)
        assertEquals("ПОДПИСАТЬСЯ", t.button.text("Подписаться"))
        assertEquals("Павильон стоит три месяца.", t.bodyM.text("Павильон стоит три месяца."))
        assertTrue(
            t.button.style.fontFamily == t.headline.style.fontFamily &&
                t.title.style.fontFamily == t.bodyM.style.fontFamily,
        )
        assertTrue(t.button.style.fontFamily != t.bodyM.style.fontFamily, "display and body are two families")
    }

    @Test
    fun `typography outside a theme is a clear error not a silent default`() {
        assertFailsWith<IllegalStateException> {
            runComposeUiTest { setContent { PlakatTheme.typography } }
        }
    }
}
