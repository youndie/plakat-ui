package io.github.youndie.plakat.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** B-12: the event names are the design's, and a state-started animation reports once per change. */
@OptIn(ExperimentalTestApi::class)
class PlakatEventTest {
    private val design = File("../reference/design-system")

    private fun read(name: String) = File(design, name).readText()

    private fun Regex.firsts(text: String) = findAll(text).map { it.groupValues[1] }.toSet()

    @Test
    fun `the events are exactly the names the design gives`() {
        // The core page's spec rows, R('<event>', '<trigger>', …). Its caret row is R('caret', '—', …),
        // the columns swapped: the brief gives the caret no event, so rows with no trigger are not events.
        val core = Regex("""R\('([^']+)', '(?!—')""").firsts(read("plakat-ui Ядро.dc.html"))
        // The MVP page's: S('<component>', '<event>', …).
        val mvp = Regex("""S\('[^']+', '([^']+)', '""").firsts(read("plakat-ui Компоненты.dc.html"))
        // The brief's motion table, whose third column is the event in backticks (RadioButton's `lock`).
        val brief =
            Regex("""^\| [^|]+ \| [^|]+ \| `([a-z-]+)` \|""", RegexOption.MULTILINE)
                .firsts(read("uploads/plakat-ui - бриф для дизайнера.md"))
        // The Principles page's slider: "Событие <b>notch</b>".
        val principles = Regex("""Событие <b>([a-z-]+)</b>""").firsts(read("plakat-ui Принципы.dc.html"))
        val named = core + mvp + brief + principles - "—"
        assertEquals(named.sorted(), PlakatEvent.entries.map { it.token }.sorted())
    }

    @Test
    fun `a state change reports its event once and an appearance only when asked`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            var value by mutableIntStateOf(0)
            setContent {
                PlakatTheme(events = { heard += it }) {
                    ReportOnChange(value, PlakatEvent.Tick)
                    ReportOnChange(Unit, PlakatEvent.Print, initial = true)
                }
            }
            waitForIdle()
            assertEquals(listOf(PlakatEvent.Print), heard, "on the first composition")
            value = 1
            waitForIdle()
            value = 2
            waitForIdle()
            assertEquals(listOf(PlakatEvent.Print, PlakatEvent.Tick, PlakatEvent.Tick), heard, "after two changes")
        }

    @Test
    fun `the theme is silent unless given a listener`() =
        runComposeUiTest {
            var listener: PlakatEventListener? = null
            setContent { PlakatTheme { listener = PlakatTheme.events } }
            waitForIdle()
            assertEquals(PlakatEventListener.Silent, listener)
        }
}
