#!/usr/bin/env node
// B-11: generate PlakatIcons.kt from the vendored plakat-icons.json.
// B-77: and from scripts/kit-icons.json, the kit's own stencils (D13), which the design does not draw: the
// vendored file is never edited. A kit icon given as a stroke (straight segments, butt caps, miter joins)
// is outlined here to the fill a stencil is, once, and the outline is what the Kotlin carries.
//
//   node scripts/icons.mjs [--check]
//
// Each icon is one SVG path on a 24-unit grid, filled nonzero (holes counter-clockwise), and is
// written into Kotlin as its path string, verbatim, for Compose's own SVG path parser. The JSON is
// the source; the design's plakat-icons.js, which the Icons page draws from, must agree with it path
// for path, or the references would show other icons than the code. --check fails when the committed
// file is not what the JSON makes today.

import { readFileSync, writeFileSync } from 'node:fs';
import { join, resolve } from 'node:path';

const ROOT = resolve(import.meta.dirname, '..');
const DESIGN = join(ROOT, 'reference/design-system');
const OUT = join(ROOT, 'plakat-core/src/commonMain/kotlin/io/github/youndie/plakat/icons/PlakatIcons.kt');
const FIXTURES = join(ROOT, 'plakat-core/src/desktopTest/kotlin/io/github/youndie/plakat/parity/IconFixtures.kt');
// The sizes the Icons page draws each icon at: 48 (twice), 24 (nominal), 16 (legibility).
const SIZES = [48, 24, 16];

const design = JSON.parse(readFileSync(join(DESIGN, 'plakat-icons.json'), 'utf8'));
// plakat-icons.js is an ES module without a package.json saying so; read it as a data: URL.
const js = await import(`data:text/javascript;base64,${readFileSync(join(DESIGN, 'plakat-icons.js')).toString('base64')}`);
const drift = design.filter((icon, i) => js.ICONS[i]?.d !== icon.d || icon.name !== `plakat/icon/${js.ICONS[i]?.id}`);
if (drift.length || js.ICONS.length !== design.length) {
  console.error(`plakat-icons.json and plakat-icons.js disagree: ${drift.map((i) => i.name).join(', ') || 'in count'}`);
  process.exit(1);
}

// The polyline points of a path of moves and straight lines (absolute M, L, H, V), one list per subpath.
function polylines(d) {
  const lines = [];
  let at = [0, 0];
  for (const [, c, args] of d.matchAll(/([MLHV])([^MLHVZ]*)/g)) {
    const n = args.trim().split(/[\s,]+/).filter(Boolean).map(Number);
    if (c === 'M') { at = [n[0], n[1]]; lines.push([at]); continue; }
    at = c === 'H' ? [n[0], at[1]] : c === 'V' ? [at[0], n[0]] : [n[0], n[1]];
    lines.at(-1).push(at);
  }
  return lines;
}

// A stroke of [width] along each polyline as a polygon: each side offset by half the width, butt caps at
// the ends, miter joins between segments (no limit is reached: the kit's corners are 45° and 90°). Every
// polygon is turned the same way, so where two overlap the nonzero rule fills both.
function outline(stroke) {
  if (stroke.cap !== 'butt' || stroke.join !== 'miter') throw new Error(`only butt caps and miter joins: ${JSON.stringify(stroke)}`);
  const h = stroke.width / 2;
  const fmt = (v) => String(Math.round(v * 1000) / 1000);
  return polylines(stroke.d).map((pts) => {
    const normals = pts.slice(1).map((p, i) => {
      const [dx, dy] = [p[0] - pts[i][0], p[1] - pts[i][1]];
      const len = Math.hypot(dx, dy);
      return [(-dy / len) * h, (dx / len) * h];
    });
    const side = (sign) => pts.map((p, k) => {
      const a = normals[Math.max(0, k - 1)], b = normals[Math.min(normals.length - 1, k)];
      // The miter: v·n̂₁ = v·n̂₂ = h.
      const scale = (h * h) / (h * h + a[0] * b[0] + a[1] * b[1]);
      return [p[0] + sign * (a[0] + b[0]) * scale, p[1] + sign * (a[1] + b[1]) * scale];
    });
    let poly = [...side(1), ...side(-1).reverse()];
    const area = poly.reduce((s, p, i) => { const q = poly[(i + 1) % poly.length]; return s + p[0] * q[1] - q[0] * p[1]; }, 0);
    if (area < 0) poly = poly.reverse();
    return 'M' + poly.map((p) => `${fmt(p[0])} ${fmt(p[1])}`).join('L') + 'Z';
  }).join('');
}

const kit = JSON.parse(readFileSync(join(ROOT, 'scripts/kit-icons.json'), 'utf8')).map((icon) =>
  ({ ...icon, kit: true, d: icon.d ?? outline(icon.stroke) }));
const icons = [...design, ...kit];

// A stencil draws moves, lines and unrotated arcs (PlakatIcon.kt's stencilPath); refuse anything else
// here, where a new icon enters, rather than at its first draw.
const commands = /^(?:[MmLlHhVvZz]|[Aa](?:\s*[-\d.]+[\s,]+[-\d.]+[\s,]+0[\s,]))/;
const unsupported = icons.filter((icon) => (icon.d.match(/[A-Za-z][^A-Za-z]*/g) ?? []).some((c) => !commands.test(c)));
if (unsupported.length) {
  console.error(`only moves, lines and unrotated arcs: ${unsupported.map((i) => i.name).join(', ')}`);
  process.exit(1);
}

const id = (icon) => icon.name.split('/').pop();
const property = (icon) => id(icon)[0].toUpperCase() + id(icon).slice(1);
// ktlint's layout for a string that does not fit: the call breaks after its opening parenthesis.
const entry = (icon) => {
  const one = `    public val ${property(icon)}: PlakatStencil = PlakatStencil("${icon.name}", "${icon.d}")`;
  if (one.length <= 120) return one;
  return [
    `    public val ${property(icon)}: PlakatStencil =`,
    `        PlakatStencil(`,
    `            "${icon.name}",`,
    `            "${icon.d}",`,
    `        )`,
  ].join('\n');
};

const kotlin = `// GENERATED by scripts/icons.mjs from reference/design-system/plakat-icons.json. Do not edit.

package io.github.youndie.plakat.icons

/**
 * The sixteen stencil icons (\`plakat/icon\`): a fill, never an outline, one path each on a 24 dp grid.
 * Draw one with [PlakatIcon], which takes its colour from the parent. After them, the kit's own stencils
 * (B-77), which the design does not draw.
 */
public object PlakatIcons {
${icons.map((icon) => `    /** ${icon.ru} (${icon.kit ? "the kit's own, B-77" : icon.group}). */\n${entry(icon)}`).join('\n\n')}

    /** Every icon of the design, in its order; not the kit's own. */
    public val All: List<PlakatStencil> =
        listOf(
${design.map((icon) => `            ${property(icon)},`).join('\n')}
        )
}
`;

// One viddik fixture per cell of the Icons page's grid, named as scripts/design-references.mjs names
// the reference: ink on concrete at the cell's size.
const fixtures = `// GENERATED by scripts/icons.mjs from reference/design-system/plakat-icons.json. Do not edit.

package io.github.youndie.plakat.parity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.icons.PlakatIcon
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.icons.PlakatStencil
import io.github.youndie.plakat.theme.PlakatTheme
import io.github.youndie.viddik.annotations.ViddikScreenshot

// B-11: the Icons page's grid, each icon at ${SIZES.join(', ')} px in ink on a concrete cell.

@Composable
private fun IconCell(
    icon: PlakatStencil,
    size: Int,
) = PlakatTheme {
    Box(Modifier.size(size.dp).background(PlakatTheme.colors.concrete)) {
        PlakatIcon(icon, null, Modifier.size(size.dp))
    }
}
${icons.flatMap((icon) => SIZES.map((size) => `
@ViddikScreenshot(group = "Icon", name = "${property(icon)}_${size}", width = ${size}, height = ${size})
@Composable
fun Icon${property(icon)}${size}() = IconCell(PlakatIcons.${property(icon)}, ${size})
`)).join('')}`;

const outputs = [[OUT, kotlin], [FIXTURES, fixtures]];
if (process.argv.includes('--check')) {
  const stale = outputs.filter(([file, text]) => { try { return readFileSync(file, 'utf8') !== text; } catch { return true; } });
  if (stale.length) {
    console.error(`${stale.map(([f]) => f.split('/').pop()).join(', ')} differ from what plakat-icons.json makes; run node scripts/icons.mjs`);
    process.exit(1);
  }
  console.log(`${design.length} icons, ${kit.length} of the kit's own and their fixtures are up to date`);
} else {
  for (const [file, text] of outputs) writeFileSync(file, text);
  console.log(`${design.length} icons, ${kit.length} of the kit's own and ${icons.length * SIZES.length} fixtures written`);
}
