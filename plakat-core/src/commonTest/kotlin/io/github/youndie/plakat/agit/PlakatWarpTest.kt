package io.github.youndie.plakat.agit

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** B-32: the shape maps and the grid against what the vendored `plakat-warp.js` asks and draws in Node. */
class PlakatWarpTest {
    private val horn = warpHorn(16.0, 312.0, 358.0, 56.0)
    private val box = warpBox(16.0, 152.0, 260.0, 160.0, 96.0, 184.0, 288.0)
    private val lid = warpBox(16.0, 180.0, 300.0, 132.0, 96.0, 200.0, 300.0, lid = true, at = .2)
    private val stairs = warpStairs(16.0, 312.0, 3, 281.5, 78.3, 40.0)
    private val quad =
        warpQuad(WarpPoint(10.0, 20.0), WarpPoint(200.0, 5.0), WarpPoint(220.0, 150.0), WarpPoint(15.0, 130.0))

    private val grids: Map<String, WarpGrid> =
        mapOf(
            "hornFloor" to warpGrid(horn.map, 8, 8),
            "hornCeiling" to
                warpGrid(warpHorn(12.0, 8.0, 236.0, 28.0, anchor = WarpAnchor.Ceiling).map, 8, 8),
            "hornAxis" to
                warpGrid(warpHorn(16.0, 200.0, 340.0, 40.0, angle = -17.0, anchor = WarpAnchor.Axis).map, 8, 8),
            "hornUp" to
                warpGrid(
                    warpHorn(100.0, 190.0, 170.0, 20.0, 30.0, -90.0, persp = .5, padU = .1, padV = .2).map,
                    8,
                    8,
                ),
            "coneWhole" to
                warpGrid(
                    warpCone(40.0, 280.0, 320.0, 20.0, 92.0, 16.0, 62.0, -17.0, padStart = .06).map,
                    8,
                    10,
                ),
            "coneShaft" to warpGrid(warpCone(30.0, 100.0, 300.0, 40.0, 40.0, arc = 89.0).map, 8, 10),
            "columnFace" to warpGrid(warpColumn(264.0, 96.0, 112.0, 312.0, 168.0, 264.0).map, 8, 8),
            "columnBack" to
                warpGrid(warpColumn(130.0, 60.0, 16.0, 112.0, 40.0, 88.0, 89.0, .3, 30.0).map, 8, 8),
            "sphereBand" to warpGrid(warpSphere(254.0, 210.0, 100.0, 72.0), 8, 10),
            "sphereEdge" to warpGrid(warpSphere(100.0, 100.0, 60.0, 86.0, 80.0, 0.0), 16, 10),
            "boxSide" to warpGrid(box.map, 4, 8, folds = box.folds),
            "boxLid" to warpGrid(lid.map, 4, 8, vfolds = lid.vfolds),
            "boxAt" to
                warpGrid(warpBox(16.0, 152.0, 260.0, 160.0, 96.0, 184.0, 288.0, at = .6).map, 4, 8, folds = listOf(.6)),
            "stairs0" to warpGrid(stairs.maps[0], 4, 6),
            "stairs2" to warpGrid(stairs.maps[2], 4, 6),
            "lineOfTwo" to warpGrid(warpLineMap(horn.map, 1, 2), 4, 8),
            "quad" to warpGrid(quad, 4, 6),
        )

    private fun numbers(text: String) = text.split(',').map { it.toDouble() }

    private fun close(
        expected: Double,
        actual: Double,
        what: String,
    ) = assertTrue(abs(expected - actual) < 1e-9, "$what: $expected, got $actual")

    private fun assertGrid(
        name: String,
        golden: WarpGolden,
        grid: WarpGrid,
    ) {
        val us = numbers(golden.us)
        val vs = numbers(golden.vs)
        assertEquals(us.size, grid.us.size, "$name: us")
        assertEquals(vs.size, grid.vs.size, "$name: vs")
        us.forEachIndexed { i, u -> close(u, grid.us[i], "$name: u $i") }
        vs.forEachIndexed { j, v -> close(v, grid.vs[j], "$name: v $j") }
        val points = golden.points.split(';')
        assertEquals(us.size * vs.size, points.size, "$name: asked")
        grid.points.flatten().forEachIndexed { k, p ->
            if (points[k] == "-") {
                assertNull(p, "$name: point $k shows")
            } else {
                val (x, y) = numbers(points[k])
                assertNotNull(p, "$name: point $k does not show")
                close(x, p.x, "$name: x $k")
                close(y, p.y, "$name: y $k")
            }
        }
        val mesh = warpMesh(grid, WarpGoldens.SOURCE_W, WarpGoldens.SOURCE_H)
        assertEquals(golden.tris, mesh.indices.size / 3, "$name: triangles")
    }

    @Test
    fun everyShapeAsksAndAnswersAsTheDesignsDoes() {
        assertEquals(WarpGoldens.cases.keys, grids.keys)
        for ((name, golden) in WarpGoldens.cases) assertGrid(name, golden, grids.getValue(name))
    }

    @Test
    fun aCellsOwnGridHasItsFoldAndARaisedNv() {
        val grid = warpGrid(box.map, 48, 8, folds = box.folds)
        assertGrid("whole", WarpGoldens.whole, grid)
        assertEquals(17, grid.vs.size, "nv is at least 16")
        assertEquals(50, grid.us.size, "the fold is a grid line of its own")
    }

    @Test
    fun theTextureLiesWhereTheDesignsTransformPutsIt() {
        // Each triangle's affine, x' = a x + c y + e and y' = b x + d y + f, carries its texture onto its positions.
        val golden = WarpGoldens.cases.getValue("boxSide")
        val matrices = golden.matrices.split(';').map(::numbers)
        val mesh = warpMesh(grids.getValue("boxSide"), WarpGoldens.SOURCE_W, WarpGoldens.SOURCE_H)
        assertEquals(matrices.size, mesh.indices.size / 3)
        matrices.forEachIndexed { t, m ->
            for (k in 0..2) {
                val i = mesh.indices[t * 3 + k]
                val s = mesh.texture[i]
                val p = mesh.positions[i]
                close(p.x, m[0] * s.x + m[2] * s.y + m[4], "triangle $t, corner $k, x")
                close(p.y, m[1] * s.x + m[3] * s.y + m[5], "triangle $t, corner $k, y")
            }
        }
    }

    @Test
    fun aMeshCoversItsWholeFormOnceInTheDesignsTwoTriangles() {
        val flat = WarpMap { u, v -> WarpPoint(u * 100, v * 100) }
        val mesh = warpMesh(warpGrid(flat, 3, 8), 10, 10)
        // A first quad's corners are made in `warp`'s order: its a, b, c and d.
        assertEquals(listOf(0, 1, 2, 0, 2, 3), mesh.indices.take(6))
        val area =
            mesh.indices.chunked(3).sumOf { (a, b, c) ->
                val p = mesh.positions[a]
                val q = mesh.positions[b]
                val r = mesh.positions[c]
                abs((q.x - p.x) * (r.y - p.y) - (r.x - p.x) * (q.y - p.y)) / 2
            }
        close(100 * 100 * (WARP_V_MAX - WARP_V_MIN), area, "the area, with no hole and no overlap")
    }

    @Test
    fun aMeshSharesItsCornersAndLeavesOutAQuadWithAPointNotShown() {
        val mesh =
            warpMesh(warpGrid(warpColumn(130.0, 60.0, 16.0, 112.0, 40.0, 88.0, 89.0, .3, 30.0).map, 8, 8), 100, 100)
        // Nine columns by seventeen rows: the column past its limb is not shown, so what stands is fewer.
        assertTrue(mesh.positions.size < 9 * 17, "${mesh.positions.size}")
        assertEquals(mesh.positions.size, mesh.texture.size)
        assertTrue(mesh.indices.isNotEmpty())
        assertTrue(mesh.indices.all { it in mesh.positions.indices })
    }
}
