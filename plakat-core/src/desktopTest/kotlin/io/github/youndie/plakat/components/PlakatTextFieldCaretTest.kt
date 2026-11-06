package io.github.youndie.plakat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ScrollWheel
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.plakat.tokens.PlakatPalettes
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** B-59: the caret stands at the insertion point on screen, inside the field. */
@OptIn(ExperimentalTestApi::class)
class PlakatTextFieldCaretTest {
    private val red = PlakatPalettes.Paper.red

    private fun near(a: Color) =
        abs(a.red - red.red) < .02f && abs(a.green - red.green) < .02f && abs(a.blue - red.blue) < .02f

    /** The columns of the root holding a full-height red bar: the caret, read from what is drawn. */
    private fun ComposeUiTest.caretColumns(): IntRange? {
        val map = onAllNodes(isRoot())[0].captureToImage().toPixelMap()
        val columns = (0 until map.width).filter { x -> (18..38).all { y -> near(map[x, y]) } }
        return if (columns.isEmpty()) null else columns.first()..columns.last()
    }

    private fun ComposeUiTest.field(
        start: String,
        wide: Int = 200,
        transformation: VisualTransformation = VisualTransformation.None,
    ) {
        setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                PlakatTheme(reduceMotion = true) {
                    Row {
                        var text by remember { mutableStateOf(start) }
                        PlakatTextField(
                            text,
                            { text = it },
                            Modifier.width(wide.dp).testTag("f"),
                            visualTransformation = transformation,
                        )
                        Box(Modifier.size(100.dp))
                    }
                }
            }
        }
        onNode(hasSetTextAction()).requestFocus()
        waitForIdle()
    }

    private fun ComposeUiTest.toEnd() {
        onNode(hasSetTextAction()).performKeyInput { pressKey(Key.MoveEnd) }
        waitForIdle()
    }

    private companion object {
        const val LONG = "a line that is much longer than the two hundred wide field it is typed in"
    }

    @Test
    fun `typing past the width keeps the caret inside the field`() =
        runComposeUiTest {
            field("")
            onNode(hasSetTextAction()).performTextInput(LONG)
            waitForIdle()
            val caret = assertNotNull(caretColumns(), "no caret after typing past the width")
            assertTrue(caret.last < 200, "the caret stands at $caret, past the field")
        }

    @Test
    fun `a line a little longer than the field does not draw the caret past its end`() =
        runComposeUiTest {
            field("a little over two hundred", wide = 160)
            toEnd()
            val caret = assertNotNull(caretColumns(), "no caret at the end of the line")
            assertTrue(caret.last < 160, "the caret stands at $caret, past the field")
        }

    @Test
    fun `moving the caret home scrolls the text back`() =
        runComposeUiTest {
            field(LONG)
            toEnd()
            assertNotNull(caretColumns())
            onNode(hasSetTextAction()).performKeyInput { pressKey(Key.MoveHome) }
            waitForIdle()
            val caret = assertNotNull(caretColumns(), "no caret at the start")
            assertTrue(caret.first < 4, "the caret stands at $caret at the start of the text")
        }

    @Test
    fun `the wheel scrolls a long line and the field keeps it to its width`() =
        runComposeUiTest {
            field(LONG)
            val before =
                onNodeWithTag("f").captureToImage().toPixelMap().let { m ->
                    (0 until m.width).map { m[it, 28] }
                }
            onNodeWithTag("f").performMouseInput { scroll(4f, ScrollWheel.Horizontal) }
            waitForIdle()
            val after =
                onNodeWithTag("f").captureToImage().toPixelMap().let { m ->
                    (0 until m.width).map { m[it, 28] }
                }
            assertNotEquals(before, after, "the wheel did not move the text")
        }

    @Test
    fun `under a mask that lengthens the text the caret stands at the mapped place`() =
        runComposeUiTest {
            field("123456", wide = 300, transformation = DashedPairs)
            toEnd()
            val masked = assertNotNull(caretColumns(), "no caret under the mask")
            assertTrue(masked.first > 50, "at the end of 12-34-56 the caret stands at $masked, within the digits")
        }

    @Test
    fun `after the last letter the caret stands clear of it and between two it is centred on the insertion point`() =
        runComposeUiTest {
            var left: Pair<Float, Float>? = null
            var place = 0f
            var end = 0f
            setContent {
                val layout = rememberTextMeasurer().measure("HHHH")
                left =
                    caretLeft(layout, OffsetMapping.Identity, 2, 2f, 3f) to
                    caretLeft(layout, OffsetMapping.Identity, 4, 2f, 3f)
                place = layout.getCursorRect(2).left
                end = layout.getCursorRect(4).left
            }
            waitForIdle()
            val (between, last) = assertNotNull(left)
            assertEquals(place - 1.5f, between)
            assertEquals(end + 2f, last)
        }

    private object DashedPairs : VisualTransformation {
        override fun filter(text: AnnotatedString): TransformedText {
            val out = text.text.chunked(2).joinToString("-")
            return TransformedText(
                AnnotatedString(out),
                object : OffsetMapping {
                    override fun originalToTransformed(offset: Int) = if (offset == 0) 0 else offset + (offset - 1) / 2

                    override fun transformedToOriginal(offset: Int) = offset - (offset + 1) / 3
                },
            )
        }
    }
}
