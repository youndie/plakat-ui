package io.github.youndie.plakat.parity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.youndie.plakat.type.plakatFonts
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-05: the parity floor. The MVP page's Divider cells are a 224 px column of a theme card: a rule
// (`height: 1 | 3 | 8 px`, `margin-top: 8 | 7 | 4 px`), an 8 px gap, then a 12 px Golos caption. A
// rule is a rectangle with nothing to disagree about, so whatever these fixtures differ from their
// Chrome references by is the two rasterisers, not the drawing; the caption alone is the text floor.
// The colours are the tokens' paper and ink, written out because the token layer (B-08) and the
// Divider (B-23) do not exist yet.

private class Paper(
    val paper: Color,
    val ink: Color,
)

private val light = Paper(Color(0xFFEEE5D1), Color(0xFF1D1B18))
private val dark = Paper(Color(0xFF1F1D1A), Color(0xFFEEE5D1))

private const val W = 224

@Composable
private fun RuleCell(
    p: Paper,
    top: Int,
    thickness: Int,
) {
    val h = top + thickness + 8
    Canvas(Modifier.size(W.dp, h.dp).background(p.paper)) {
        drawRect(p.ink, Offset(0f, top.toFloat()), Size(W.toFloat(), thickness.toFloat()))
    }
}

@Composable
private fun CaptionCell(p: Paper) {
    Box(Modifier.size(W.dp, 15.dp).background(p.paper)) {
        BasicText(
            "Divider / Hair 1",
            style =
                TextStyle(
                    fontFamily = plakatFonts().body,
                    fontSize = 12.sp,
                    color = p.ink,
                ),
        )
    }
}

@ViddikScreenshot(group = "Divider", name = "Hair_Light", width = W, height = 17)
@Composable
fun DividerHairLight() = RuleCell(light, 8, 1)

@ViddikScreenshot(group = "Divider", name = "Rule_Light", width = W, height = 18)
@Composable
fun DividerRuleLight() = RuleCell(light, 7, 3)

@ViddikScreenshot(group = "Divider", name = "Bar_Light", width = W, height = 20)
@Composable
fun DividerBarLight() = RuleCell(light, 4, 8)

@ViddikScreenshot(group = "Divider", name = "Hair_Dark", width = W, height = 17)
@Composable
fun DividerHairDark() = RuleCell(dark, 8, 1)

@ViddikScreenshot(group = "Divider", name = "Rule_Dark", width = W, height = 18)
@Composable
fun DividerRuleDark() = RuleCell(dark, 7, 3)

@ViddikScreenshot(group = "Divider", name = "Bar_Dark", width = W, height = 20)
@Composable
fun DividerBarDark() = RuleCell(dark, 4, 8)

@ViddikScreenshot(group = "TextFloor", name = "Caption_Light", width = W, height = 15)
@Composable
fun TextFloorCaptionLight() = CaptionCell(light)

@ViddikScreenshot(group = "TextFloor", name = "Caption_Dark", width = W, height = 15)
@Composable
fun TextFloorCaptionDark() = CaptionCell(dark)
