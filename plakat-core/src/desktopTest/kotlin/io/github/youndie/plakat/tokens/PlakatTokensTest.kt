package io.github.youndie.plakat.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.youndie.plakat.print.raskatHardStop
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * B-08: a second reading of `plakat-tokens.json`, independent of the generator, held against the
 * generated tokens — a slip in one reader is caught by the other.
 */
class PlakatTokensTest {
    private val json: JsonObject =
        Json.parseToJsonElement(File("../reference/design-system/plakat-tokens.json").readText()).jsonObject

    private fun section(name: String) = json.getValue(name).jsonObject

    private fun hex(c: Color) = "#" + (c.toArgb() and 0xFFFFFF).toString(16).padStart(6, '0').uppercase()

    @Test
    fun `both palettes are the design's colours`() {
        val colors = section("plakat/color")
        for ((theme, palette) in listOf("light" to PlakatPalettes.Paper, "dark" to PlakatPalettes.BlackPaper)) {
            val ours =
                mapOf(
                    "paper" to palette.paper,
                    "concrete" to palette.concrete,
                    "ink" to palette.ink,
                    "ink-muted" to palette.inkMuted,
                    "red" to palette.red,
                    "on-red" to palette.onRed,
                    "ocher" to palette.ocher,
                    "slate" to palette.slate,
                )
            assertEquals(colors.keys, ours.keys, "every colour token, no more")
            for ((k, c) in ours) {
                assertEquals(
                    colors
                        .getValue(k)
                        .jsonObject
                        .getValue(theme)
                        .jsonPrimitive.content,
                    hex(c),
                    "$theme $k",
                )
            }
        }
    }

    @Test
    fun `the semantic roles point at the design's inks`() {
        val semantic = section("plakat/semantic")
        val p = PlakatPalettes.Paper
        val ours =
            mapOf(
                "primary" to p.primary,
                "danger" to p.danger,
                "success" to p.success,
                "warning" to p.warning,
                "info" to p.info,
            )
        val byName = mapOf("red" to p.red, "ink" to p.ink, "ocher" to p.ocher, "slate" to p.slate)
        for ((role, c) in ours) {
            assertEquals(
                byName.getValue(
                    semantic
                        .getValue(role)
                        .jsonObject
                        .getValue("fill")
                        .jsonPrimitive.content,
                ),
                c,
                role,
            )
        }
    }

    @Test
    fun `the type scale is the design's`() {
        val type = section("plakat/type")
        val ours =
            mapOf(
                "display/l" to PlakatTypeScale.DisplayL,
                "display/m" to PlakatTypeScale.DisplayM,
                "headline" to PlakatTypeScale.Headline,
                "title" to PlakatTypeScale.Title,
                "body/l" to PlakatTypeScale.BodyL,
                "body/m" to PlakatTypeScale.BodyM,
                "body/s" to PlakatTypeScale.BodyS,
                "label" to PlakatTypeScale.Label,
                "button" to PlakatTypeScale.Button,
            )
        assertEquals(type.keys.filterNot { it.startsWith("family/") }.toSet(), ours.keys)
        for ((k, r) in ours) {
            val t = type.getValue(k).jsonObject
            assertEquals(
                t
                    .getValue("size")
                    .jsonPrimitive.double
                    .toFloat(),
                r.size,
                k,
            )
            assertEquals(
                t
                    .getValue("line")
                    .jsonPrimitive.double
                    .toFloat(),
                r.lineHeight,
                k,
            )
            assertEquals(t["weight"]?.jsonPrimitive?.int ?: 400, r.weight, k)
            assertEquals(t["tracking"]?.jsonPrimitive?.double?.toFloat() ?: 0f, r.tracking, k)
            assertEquals(t["case"]?.jsonPrimitive?.content == "upper", r.upperCase, k)
            assertEquals(
                if (t.getValue("family").jsonPrimitive.content ==
                    "display"
                ) {
                    PlakatFamily.Display
                } else {
                    PlakatFamily.Body
                },
                r.family,
                k,
            )
        }
        // The brief's poster jump, which the scale has to keep: Display M = 3 × Body M, Headline = 2 × Body M.
        assertEquals(3f, PlakatTypeScale.DisplayM.size / PlakatTypeScale.BodyM.size)
        assertEquals(2f, PlakatTypeScale.Headline.size / PlakatTypeScale.BodyM.size)
    }

    @Test
    fun `lines shape grid and sizes are the design's`() {
        val line = section("plakat/line")
        assertEquals(
            line
                .getValue("hair")
                .jsonPrimitive.double
                .toFloat(),
            PlakatLines.Hair.value,
        )
        assertEquals(
            line
                .getValue("rule")
                .jsonPrimitive.double
                .toFloat(),
            PlakatLines.Rule.value,
        )
        assertEquals(
            line
                .getValue("bar")
                .jsonPrimitive.double
                .toFloat(),
            PlakatLines.Bar.value,
        )
        val shape = section("plakat/shape")
        assertEquals(
            shape
                .getValue("axis")
                .jsonPrimitive.double
                .toFloat(),
            PlakatShape.AXIS,
        )
        assertEquals(0f, PlakatShape.Radius.value)
        val cut = shape.getValue("cut").jsonObject
        assertEquals(
            cut
                .getValue("w")
                .jsonPrimitive.double
                .toFloat(),
            PlakatShape.CutWidth.value,
        )
        assertEquals(
            cut
                .getValue("h")
                .jsonPrimitive.double
                .toFloat(),
            PlakatShape.CutHeight.value,
        )
        val grid = section("plakat/grid")
        assertEquals(
            grid
                .getValue("step")
                .jsonPrimitive.double
                .toFloat(),
            PlakatGrid.Step.value,
        )
        assertEquals(
            grid
                .getValue("margin")
                .jsonPrimitive.double
                .toFloat(),
            PlakatGrid.Margin.value,
        )
        assertEquals(
            grid
                .getValue("gutter")
                .jsonPrimitive.double
                .toFloat(),
            PlakatGrid.Gutter.value,
        )
        val size = section("plakat/size")
        for ((k, v) in mapOf(
            "button" to PlakatSizes.Button,
            "field" to PlakatSizes.Field,
            "chip" to PlakatSizes.Chip,
            "touch" to PlakatSizes.Touch,
            "checkbox" to PlakatSizes.Checkbox,
        )) {
            assertEquals(
                size
                    .getValue(k)
                    .jsonPrimitive.double
                    .toFloat(),
                v.value,
                k,
            )
        }
        val switch = size.getValue("switch").jsonObject
        assertEquals(
            switch
                .getValue("w")
                .jsonPrimitive.double
                .toFloat(),
            PlakatSizes.SwitchWidth.value,
        )
        val carriage = switch.getValue("carriage").jsonArray.map { it.jsonPrimitive.double.toFloat() }
        assertEquals(carriage, listOf(PlakatSizes.SwitchCarriageWidth.value, PlakatSizes.SwitchCarriageHeight.value))
        // The cut lies on the axis: 46 × 14 is 17°.
        assertTrue(abs(Math.toDegrees(kotlin.math.atan(14.0 / 46.0)) - 17) < .1)
    }

    @Test
    fun `print layers and motion are the design's`() {
        val print = section("plakat/print")
        assertEquals(
            print
                .getValue("paper-texture")
                .jsonObject
                .getValue("opacity")
                .jsonPrimitive.double
                .toFloat(),
            PlakatPrint.PAPER_TEXTURE_OPACITY,
        )
        assertEquals(
            print
                .getValue("nonprint")
                .jsonObject
                .getValue("opacity")
                .jsonPrimitive.double
                .toFloat(),
            PlakatPrint.NONPRINT_OPACITY,
        )
        val reg =
            print.getValue("registration").jsonObject.getValue("offset").jsonArray.map {
                it.jsonPrimitive.double.toFloat()
            }
        assertEquals(reg, listOf(PlakatPrint.RegistrationX.value, PlakatPrint.RegistrationY.value))
        val raskat = print.getValue("raskat").jsonObject
        val hard = raskat.getValue("hard").jsonArray.map { it.jsonPrimitive.double.toFloat() }
        assertEquals(hard, listOf(PlakatPrint.RaskatHard.start, PlakatPrint.RaskatHard.endInclusive))
        val d = section("plakat/motion/duration")
        assertEquals(
            listOf(
                "snap",
                "quick",
                "normal",
                "scene",
                "cascade",
                "screen-max",
            ).map { d.getValue(it).jsonPrimitive.int },
            with(PlakatDurations) { listOf(SNAP, QUICK, NORMAL, SCENE, CASCADE, SCREEN_MAX) },
        )
    }

    @Test
    fun `the hard-stop easing is the curve raskat's roll solves, without overshoot`() {
        // Compose's solver and the design's 24 bisections are different routes to one curve.
        for (i in 0..1000) {
            val t = i / 1000f
            val ours = PlakatEasings.HardStop.transform(t)
            assertTrue(abs(ours - raskatHardStop(t.toDouble())) < 1e-3, "at $t: $ours")
            assertTrue(ours in 0f..1f, "no overshoot at $t: $ours")
        }
    }

    @Test
    fun `stepped jumps at the end of each step as CSS steps does`() {
        val six = PlakatEasings.stepped(6)
        assertEquals(0f, six.transform(0f))
        assertEquals(0f, six.transform(.16f))
        assertEquals(1f / 6, six.transform(.17f))
        assertEquals(5f / 6, six.transform(.99f))
        assertEquals(1f, six.transform(1f))
    }
}
