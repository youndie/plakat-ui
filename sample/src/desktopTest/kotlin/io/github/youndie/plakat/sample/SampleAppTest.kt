package io.github.youndie.plakat.sample

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import io.github.youndie.plakat.theme.PlakatEvent
import io.github.youndie.plakat.theme.PlakatEventListener
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** B-01, B-35: the sample composes through the library's public API, as a consumer does. */
@OptIn(ExperimentalTestApi::class)
class SampleAppTest {
    @Test
    fun `the label switches the paper texture off and on`() =
        runComposeUiTest {
            setContent { PlakatUiSample(autoplay = false) }

            // B-07: a row of the paper away from the text, textured, then flat, then textured again.
            fun paperRow() =
                onNodeWithTag("sample-root").captureToImage().toPixelMap().let { px ->
                    (0 until px.width).map { px[it, px.height - 2] }.toSet()
                }
            assertTrue(paperRow().size > 3, "textured paper")
            onNodeWithText("TEXTURE: ON").performClick()
            assertEquals(
                setOf(PlakatPalettes.Paper.paper),
                paperRow()
                    .filter {
                        it.alpha == 1f
                    }.toSet()
                    .take(1)
                    .toSet(),
                "flat paper",
            )
            onNodeWithText("TEXTURE: OFF").performClick()
            assertTrue(paperRow().size > 3, "textured again")
        }

    /** Every section in each theme and both ways of moving, with the clock paused so nothing waits on a loop. */
    @Test
    fun `every section composes in each theme, the clock paused`() {
        for (dark in listOf(false, true)) {
            for (reduce in listOf(false, true)) {
                runComposeUiTest {
                    mainClock.autoAdvance = false
                    setContent { PlakatUiSample(dark = dark, reduceMotion = reduce) }
                    for (section in SampleSection.entries) {
                        onNodeWithTag("section-${section.name}").performClick()
                        mainClock.advanceTimeBy(500)
                        onNodeWithTag("sample-root").assertIsDisplayed()
                    }
                }
            }
        }
    }

    /** B-57: every word the sample shows or names is English; the design's Russian stays in the fixtures. */
    @Test
    fun `no section shows a word in Russian`() =
        runComposeUiTest {
            setContent { PlakatUiSample(reduceMotion = true, autoplay = false, texture = false) }
            for (section in SampleSection.entries) {
                onNodeWithTag("section-${section.name}").performClick()
                waitForIdle()
                val words =
                    onAllNodes(SemanticsMatcher("any node") { true }, useUnmergedTree = true)
                        .fetchSemanticsNodes()
                        .flatMap { node ->
                            node.config.getOrElse(SemanticsProperties.Text) { emptyList() }.map { it.text } +
                                node.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }
                        }
                assertTrue(words.isNotEmpty(), "$section shows words")
                assertEquals(emptyList(), words.filter { it.any { c -> c in '\u0400'..'\u04FF' } }, "$section")
            }
        }

    /** What a finger or a pointer can press, over a section of the sample, with its bounds and role. */
    private fun ComposeUiTest.controls(section: SampleSection) {
        setContent { PlakatUiSample(section = section, reduceMotion = true, autoplay = false, texture = false) }
        waitForIdle()
        val density = density.density
        val nodes = onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue(nodes.isNotEmpty(), "$section has controls")
        val small = mutableListOf<String>()
        val mute = mutableListOf<String>()
        for (node in nodes) {
            val name =
                listOf(
                    SemanticsProperties.Text,
                    SemanticsProperties.EditableText,
                    SemanticsProperties.ContentDescription,
                ).firstNotNullOfOrNull { node.config.getOrElseNullable(it) { null } }
                    ?.toString() ?: "?"
            val w = node.size.width / density
            val h = node.size.height / density
            if (w < 48f || h < 48f) small += "$name ${w}x$h"
            if (SemanticsActions.OnClick in node.config && SemanticsProperties.Role !in node.config &&
                name == "?"
            ) {
                mute += "$name at ${node.boundsInRoot}"
            }
        }
        assertEquals(emptyList(), small, "$section: under 48 dp")
        assertEquals(emptyList(), mute, "$section: no name and no role")
    }

    @Test
    fun `every control of the core page has its target and its name`() =
        runComposeUiTest { controls(SampleSection.Core) }

    @Test
    fun `every control of the components page has its target and its name`() =
        runComposeUiTest { controls(SampleSection.Components) }

    @Test
    fun `every control of the onboarding has its target and its name`() =
        runComposeUiTest { controls(SampleSection.Onboarding) }

    @Test
    fun `every control of the feed has its target and its name`() = runComposeUiTest { controls(SampleSection.Feed) }

    @Test
    fun `every control of the settings has its target and its name`() =
        runComposeUiTest { controls(SampleSection.Settings) }

    private fun ComposeUiTest.session(heard: MutableList<PlakatEvent>) {
        setContent {
            PlakatUiSample(
                reduceMotion = true,
                texture = false,
                section = SampleSection.Onboarding,
                autoplay = false,
                events = PlakatEventListener { heard += it },
            )
        }
        waitForIdle()
    }

    /** A scripted session through the kit's listener: the names come in the spec's order, from the tokens. */
    @Test
    fun `a scripted session is heard in the spec's names`() =
        runComposeUiTest {
            val heard = mutableListOf<PlakatEvent>()
            session(heard)
            heard.clear()
            // Onboarding: next, which presses a button and turns the window.
            onNodeWithText("NEXT").performScrollTo().performClick()
            waitForIdle()
            // A chip to the settings screen, then a row's switch.
            onNodeWithTag("section-Settings").performClick()
            waitForIdle()
            onNodeWithText("Send the digest").performScrollTo().performClick()
            waitForIdle()
            assertEquals(listOf("stamp", "release", "rail", "stamp", "lever"), heard.map { it.token })
        }
}
