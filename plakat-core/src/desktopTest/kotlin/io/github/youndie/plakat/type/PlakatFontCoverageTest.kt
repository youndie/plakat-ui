package io.github.youndie.plakat.type

import org.jetbrains.skia.Typeface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-02: every character the design's pages put on screen is in both bundled families, except the
 * ones listed here with their reason. Compose never falls back to the next face in a `FontFamily`;
 * a missing glyph is drawn by whatever the host has, differently per machine. Fixture strings are
 * copied from these pages verbatim (research D11), so a fixture that brings a new character brings
 * it here first.
 */
class PlakatFontCoverageTest {
    /** Text the pages render: element text and the quoted strings their `DCLogic` builds. */
    private val corpus: Set<Int> by lazy {
        val text = Regex(""">([^<]*)<|'([^'\n]*)'""")
        BundledFonts.designPages
            .flatMap { page ->
                text.findAll(page.readText()).map { it.groupValues[1] + it.groupValues[2] }.toList()
            }.joinToString("")
            .codePoints()
            .toArray()
            .filter { it > 0x20 && !Character.isWhitespace(it) && !Character.isISOControl(it) }
            .toSet()
    }

    private fun missing(face: Typeface): Set<String> =
        corpus.filter { face.getUTF32Glyph(it).toInt() == 0 }.map { String(Character.toChars(it)) }.toSet()

    @Test
    fun `the corpus is the design, not an empty directory`() {
        assertEquals(11, BundledFonts.designPages.size, "spec pages found")
        assertTrue(corpus.containsAll("ЁЖЩЯёжщя".codePoints().toArray().toList()), "Cyrillic in the corpus")
    }

    @Test
    fun `Golos Text draws every character of the design but the Counter's pictograms`() {
        // ■ and ● are the Counter legend's pictograms and are drawn as shapes (B-30). ₀ is only in the
        // Stretch page's prose about r₀ = r₁.
        val allowed = setOf("■", "●", "₀")
        assertEquals(emptySet(), missing(BundledFonts.golosRegular) - allowed, "Golos 400")
        assertEquals(emptySet(), missing(BundledFonts.golosSemiBold) - allowed, "Golos 600")
    }

    @Test
    fun `Rubik Mono One draws every character of the design but pictograms and arrows`() {
        // ■ ● as above; the arrows are the Stretch page's sandbox controls and its prose, never
        // display text of a component.
        val allowed = setOf("■", "●", "↑", "→", "↗")
        assertEquals(emptySet(), missing(BundledFonts.rubikMonoOne) - allowed, "Rubik Mono One")
    }

    @Test
    fun `both families carry the whole Russian alphabet`() {
        val alphabet = (('А'..'я') + 'Ё' + 'ё').map { it.code }
        for (face in listOf(BundledFonts.rubikMonoOne, BundledFonts.golosRegular, BundledFonts.golosSemiBold)) {
            assertEquals(emptyList(), alphabet.filter { face.getUTF32Glyph(it).toInt() == 0 }, face.familyName)
        }
    }
}
