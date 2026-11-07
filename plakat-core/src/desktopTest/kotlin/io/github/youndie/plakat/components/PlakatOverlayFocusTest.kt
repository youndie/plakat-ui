package io.github.youndie.plakat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** B-63: where the keyboard and clicks go around an overlay and a change of screen. */
@OptIn(ExperimentalTestApi::class)
class PlakatOverlayFocusTest {
    private var open by mutableStateOf(true)
    private var dismissed = 0

    private enum class Kind { Dialog, Sheet, Drawer }

    private val nav =
        listOf(PlakatNavigationItem(PlakatIcons.Home, "HOME"), PlakatNavigationItem(PlakatIcons.Search, "FIND"))

    @Composable
    private fun overlay(
        kind: Kind,
        to: FocusRequester?,
    ) {
        val close = {
            dismissed++
            open = false
        }
        when (kind) {
            Kind.Dialog -> {
                PlakatDialog(open, "TITLE", close, returnFocusTo = to) { PlakatDialogAction("FIRST", {}) }
            }

            Kind.Sheet -> {
                PlakatBottomSheet(open, close, returnFocusTo = to) { PlakatListItem("FIRST", onClick = {}) }
            }

            Kind.Drawer -> {
                PlakatNavigationDrawer(
                    open,
                    nav.map {
                        PlakatNavigationItem(
                            it.icon,
                            if (it.label ==
                                "HOME"
                            ) {
                                "FIRST"
                            } else {
                                it.label
                            },
                        )
                    },
                    0,
                    {},
                    close,
                    returnFocusTo = to,
                )
            }
        }
    }

    private fun ComposeUiTest.stage(
        kind: Kind,
        returning: Boolean = true,
    ) {
        mainClock.autoAdvance = false
        setContent {
            PlakatTheme(reduceMotion = true) {
                val opener = remember { FocusRequester() }
                Column(Modifier.size(390.dp, 600.dp)) {
                    PlakatButton("OPEN", { open = true }, Modifier.focusRequester(opener).testTag("opener"))
                    Box(Modifier.fillMaxSize()) { overlay(kind, if (returning) opener else null) }
                }
            }
        }
        mainClock.advanceTimeBy(500)
    }

    private fun ComposeUiTest.tab() {
        onRoot().performKeyInput { pressKey(Key.Tab) }
        mainClock.advanceTimeBy(100)
    }

    private fun ComposeUiTest.firstControl() =
        onNode(
            androidx.compose.ui.test
                .hasText("FIRST", substring = true)
                .or(
                    androidx.compose.ui.test
                        .hasContentDescription("FIRST"),
                ),
        )

    private fun everyKind(block: ComposeUiTest.(Kind) -> Unit) {
        for (kind in Kind.entries) {
            open = true
            dismissed = 0
            runComposeUiTest { block(kind) }
        }
    }

    @Test
    fun `the first Tab lands on a control the eye can see`() =
        everyKind { kind ->
            stage(kind)
            tab()
            firstControl().assertIsFocused()
        }

    @Test
    fun `Enter on the first stop does not dismiss`() =
        everyKind { kind ->
            stage(kind)
            tab()
            onRoot().performKeyInput { pressKey(Key.Enter) }
            mainClock.advanceTimeBy(100)
            assertEquals(0, dismissed, "$kind dismissed from its first stop")
        }

    @Test
    fun `a click on the screen still dismisses`() =
        everyKind { kind ->
            stage(kind)
            onRoot().performMouseInput { click(Offset(385f, 50f)) }
            mainClock.advanceTimeBy(100)
            assertEquals(1, dismissed, "$kind")
        }

    @Test
    fun `on closing focus goes back to the opener it names`() =
        everyKind { kind ->
            stage(kind)
            tab()
            open = false
            mainClock.advanceTimeBy(1000)
            onNodeWithTag("opener").assertIsFocused()
        }

    @Test
    fun `with no opener named closing just leaves focus to the window`() =
        everyKind { kind ->
            stage(kind, returning = false)
            tab()
            open = false
            mainClock.advanceTimeBy(1000)
            assertEquals(
                0,
                onAllNodes(
                    androidx.compose.ui.test
                        .isFocused(),
                ).fetchSemanticsNodes().size,
                "$kind",
            )
        }

    @Test
    fun `opening nothing takes nothing to the opener`() =
        everyKind { kind ->
            open = false
            stage(kind)
            assertEquals(
                0,
                onAllNodes(
                    androidx.compose.ui.test
                        .isFocused(),
                ).fetchSemanticsNodes().size,
                "$kind",
            )
        }

    @Test
    fun `an opener that is gone is passed over`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            val elsewhere = FocusRequester()
            setContent {
                PlakatTheme(
                    reduceMotion = true,
                ) { PlakatDialog(open, "TITLE", {}, returnFocusTo = elsewhere) { } }
            }
            mainClock.advanceTimeBy(500)
            open = false
            mainClock.advanceTimeBy(1000)
        }

    @Test
    fun `a reader can dismiss each of them`() =
        everyKind { kind ->
            stage(kind)
            val node = onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss))
            assertNotNull(node.fetchSemanticsNode())
            node.performSemanticsAction(SemanticsActions.Dismiss)
            mainClock.advanceTimeBy(100)
            assertEquals(1, dismissed, "$kind")
        }

    @Test
    fun `during a change neither screen takes a click and after it the new one does`() {
        for (reduce in listOf(false, true)) {
            runComposeUiTest {
                var screen by mutableStateOf(0)
                val hits = mutableListOf<Int>()
                mainClock.autoAdvance = false
                setContent {
                    PlakatTheme(reduceMotion = reduce) {
                        PlakatScreenWipe(screen, screen, Modifier.size(300.dp).testTag("w")) { s ->
                            Box(Modifier.fillMaxSize().clickable { hits += s }) { BasicText("S$s") }
                        }
                    }
                }
                mainClock.advanceTimeBy(100)
                onNodeWithText("S0").performClick()
                mainClock.advanceTimeBy(100)
                assertEquals(listOf(0), hits, "at rest it takes a click")
                screen = 1
                mainClock.advanceTimeByFrame()
                mainClock.advanceTimeByFrame()
                onNodeWithTag("w").performMouseInput { click(Offset(20f, 150f)) }
                mainClock.advanceTimeByFrame()
                assertEquals(listOf(0), hits, "a click went through during the change")
                mainClock.advanceTimeBy(1000)
                onNodeWithTag("w").performMouseInput { click(Offset(20f, 150f)) }
                mainClock.advanceTimeBy(100)
                assertEquals(listOf(0, 1), hits, "the new screen takes no click after it")
            }
        }
    }
}
