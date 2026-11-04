#!/usr/bin/env node
// B-05: render the pixel references from the vendored spec pages in headless Chrome (research D3).
//
//   node scripts/design-references.mjs [--check] [--chrome <binary>]
//
// The components exist only as inline-styled elements of the spec pages, drawn by the pages' own
// runtime (support.js, React 18.3.1 and Babel from unpkg). So each page is opened as it is, from
// file://, and each reference is an element of it, photographed by its rectangle:
//
// - The page's Google Fonts stylesheet is answered, over the DevTools Fetch domain, with
//   @font-face rules for the repository's own font files (B-02) as data: URLs, so the reference
//   and the Compose fixture draw text with the same files.
// - The texture overlays (the absolutely placed layers with a blend mode and no pointer events) are
//   hidden: references are taken with the texture off (research §3, risk 4).
// - Reduced motion is emulated and two animation frames are let pass before a shot.
// - The viewport is 1584 px wide, chosen so that the theme cards come out 752 px and every grid
//   column a reference sits in is a whole number of pixels (Divider 224, Button 164); its height is
//   the page's, so nothing scrolls and the sticky toolbar stays at the top.
// - Device scale 1: viddik captures fixtures at density 1, so 1 CSS px = 1 dp = 1 px.
//
// A reference names its cell by the cell's caption ("Filled / Default") and which theme card it is
// in (0 light, 1 dark), and a part: the cell's first child (the component), the caption itself
// (the text floor), everything above the caption (a divider's rule), or the n-th icon of the row
// above the caption's block (an icon cell). The PNGs go to
// plakat-core/src/desktopTest/snapshots/design, named as viddik names a fixture's golden,
// `<Group>_<Name>.png`. --check renders into memory and fails if any PNG differs.
// Node 24 has WebSocket, so the DevTools protocol needs no dependency.

import { spawn } from 'node:child_process';
import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { setTimeout as sleep } from 'node:timers/promises';
import { pathToFileURL } from 'node:url';

const ROOT = resolve(import.meta.dirname, '..');
const DESIGN = join(ROOT, 'reference/design-system');
const FONTS = join(ROOT, 'plakat-core/src/commonMain/composeResources/font');
const OUT = join(ROOT, 'plakat-core/src/desktopTest/snapshots/design');
// Per page: the width at which every grid column a reference sits in is whole. The core and MVP pages
// grow to 1720 px, so 1584 gives 752 px theme cards (Divider columns 224, Button 164); the Tokens page
// stops at 1120 + 48 px, so it is narrowed to 1160 for a 1112 px grid (semantic swatches 216, print 272).
const VIEWPORTS = { 'plakat-ui Токены.dc.html': 1160 };
const VIEWPORT = 1584;
const viewport = (page) => VIEWPORTS[page] ?? VIEWPORT;

const CORE = 'plakat-ui Ядро.dc.html';
const TOKENS = 'plakat-ui Токены.dc.html';
const MVP = 'plakat-ui Компоненты.dc.html';
const ICONS = 'plakat-ui Иконки.dc.html';
// What says a page's template has rendered: the runtime has replaced its <x-dc> and the cells'
// captions are there ("Filled / Default"); the Icons page imports its paths after mount.
const READY = { [ICONS]: 'plakat/icon/time' };
const ready = (page) => `!document.querySelector('x-dc') && document.body.innerText.includes(${JSON.stringify(READY[page] ?? '/ ')})`;
const ICON_IDS = JSON.parse(readFileSync(join(DESIGN, 'plakat-icons.json'), 'utf8')).map((i) => i.name.split('/').pop());
const camel = (id) => id[0].toUpperCase() + id.slice(1);
const THEMES = ['Light', 'Dark'];

const cells = (group, page, part, entries) =>
  entries.flatMap(([name, caption]) => THEMES.map((theme, card) => ({ group, name: `${name}_${theme}`, page, caption, card, part })));

export const REFERENCES = [
  // The floor's ruler (B-05): a rule of each thickness, no text.
  ...cells('Divider', MVP, 'above', [['Hair', 'Divider / Hair 1'], ['Rule', 'Divider / Rule 3'], ['Bar', 'Divider / Bar 8 — раздел']]),
  // The text floor: one 12 px Golos caption alone.
  ...cells('TextFloor', MVP, 'caption', [['Caption', 'Divider / Hair 1']]),
  // The print primitives (B-10): the cut alone (the left 40 px of a filled plate, no text), the danger
  // hatch alone (the right of the Tokens page's danger swatch, past its word), registration («ЛИСТ»).
  ...cells('Cut', CORE, 'first', [['Plate', 'Filled / Default']]).map((r) => ({ ...r, crop: { x: 0, y: 0, w: 40, h: 48 } })),
  { group: 'Hatch', name: 'Danger_Light', page: TOKENS, caption: 'danger — штриховка + слово', card: 0, part: 'first', crop: { x: 120, y: 0, w: 96, h: 48 } },
  { group: 'Registration', name: 'List_Light', page: TOKENS, caption: 'plakat/print/registration', prefix: true, card: 0, part: 'first' },
  // The Button's cells, for B-13.
  ...cells('Button', CORE, 'first', [
    ['FilledDefault', 'Filled / Default'], ['FilledPressed', 'Filled / Pressed'], ['FilledFocused', 'Filled / Focused'],
    ['FilledDisabled', 'Filled / Disabled'], ['FilledLoading', 'Filled / Loading'],
    ['OutlinedDefault', 'Outlined / Default'], ['OutlinedPressed', 'Outlined / Pressed'], ['OutlinedDisabled', 'Outlined / Disabled'],
    ['TextDefault', 'Text / Default'], ['TextPressed', 'Text / Pressed'], ['TextDisabled', 'Text / Disabled'],
  // The focus ring is an outline 3 px wide, 3 px off the element: the crop takes the 6 px around it.
  ]).map((r) => (r.name.startsWith('FilledFocused_') ? { ...r, crop: { x: -6, y: -6, w: 176, h: 60 } } : r)),
  // The TextField's cells (B-14): label, field and, in error, the hatch and its message: the cell
  // above its caption.
  ...cells('TextField', CORE, 'above', [
    ['Default', 'TextField / Default'], ['Focused', 'TextField / Focused'], ['Filled', 'TextField / Filled'],
    ['Error', 'TextField / Error'], ['Disabled', 'TextField / Disabled'],
  // The error message's 1.35 line leaves its cell 148.78 px tall: the crop takes 149, the last row
  // the gap's paper below the message.
  ]).map((r) => (r.name.startsWith('Error_') ? { ...r, crop: { x: 0, y: 0, w: 224, h: 149 } } : r)),
  // The icons (B-11): each of the sixteen at 48, 24 and 16 px, ink on concrete, from the Icons page's
  // grid; a cell is found by its token caption, and the icon is the n-th svg of the row above it.
  ...ICON_IDS.flatMap((id) => [48, 24, 16].map((size, index) =>
    ({ group: 'Icon', name: `${camel(id)}_${size}`, page: ICONS, caption: `plakat/icon/${id}`, card: 0, part: 'icon', index }))),
];

// The repository's faces under the names the pages ask Google for.
const FACES = [
  ['Rubik Mono One', 'rubik_mono_one_regular.ttf', 400],
  ['Golos Text', 'golos_text_regular.ttf', 400],
  ['Golos Text', 'golos_text_semibold.ttf', 600],
];
const fontCss = FACES.map(([family, file, weight]) =>
  `@font-face{font-family:'${family}';font-style:normal;font-weight:${weight};font-display:block;` +
  `src:url(data:font/ttf;base64,${readFileSync(join(FONTS, file)).toString('base64')}) format('truetype')}`).join('\n');

class Cdp {
  constructor(ws) {
    this.ws = ws; this.id = 0; this.pending = new Map(); this.listeners = [];
    ws.addEventListener('message', (event) => {
      const msg = JSON.parse(event.data);
      if (msg.id && this.pending.has(msg.id)) {
        const { ok, fail } = this.pending.get(msg.id); this.pending.delete(msg.id);
        msg.error ? fail(new Error(`${msg.error.message} ${msg.error.data ?? ''}`)) : ok(msg.result);
      } else for (const l of this.listeners) l(msg);
    });
  }
  send(method, params = {}, sessionId) {
    const id = ++this.id;
    this.ws.send(JSON.stringify({ id, method, params, sessionId }));
    return new Promise((ok, fail) => this.pending.set(id, { ok, fail }));
  }
  once(method, sessionId) {
    return new Promise((ok) => {
      const l = (msg) => { if (msg.method === method && msg.sessionId === sessionId) { this.listeners.splice(this.listeners.indexOf(l), 1); ok(msg.params); } };
      this.listeners.push(l);
    });
  }
}

async function launch(chrome) {
  const profile = mkdtempSync(join(tmpdir(), 'plakat-refs-'));
  const proc = spawn(chrome, ['--headless=new', '--remote-debugging-port=0', `--user-data-dir=${profile}`, '--allow-file-access-from-files',
    '--force-device-scale-factor=1', '--force-prefers-reduced-motion', '--hide-scrollbars', '--disable-gpu', '--no-first-run',
    '--no-default-browser-check', 'about:blank'], { stdio: 'ignore' });
  const portFile = join(profile, 'DevToolsActivePort');
  for (let i = 0; i < 100 && !existsSync(portFile); i++) await sleep(100);
  const [port, path] = readFileSync(portFile, 'utf8').trim().split('\n');
  const ws = new WebSocket(`ws://127.0.0.1:${port}${path}`);
  await new Promise((ok, fail) => { ws.onopen = ok; ws.onerror = fail; });
  const version = (await fetch(`http://127.0.0.1:${port}/json/version`).then((r) => r.json())).Browser;
  const exited = new Promise((ok) => proc.once('exit', ok));
  return { cdp: new Cdp(ws), version, close: async () => { ws.close(); proc.kill(); await exited; rmSync(profile, { recursive: true, force: true, maxRetries: 5 }); } };
}

// Evaluated in the page: the rectangle of a reference, in page pixels, or a reason it has none.
const LOCATE = `(ref) => {
  // A caption is the innermost div whose text is the caption (or starts with it, for captions with
  // markup in them, like the Tokens page's <b>plakat/print/…</b> lines).
  const matches = (d) => ref.prefix ? d.textContent.trim().startsWith(ref.caption) : d.textContent.trim() === ref.caption;
  const captions = [...document.querySelectorAll('div')].filter(d => matches(d) && ![...d.querySelectorAll('div')].some(matches));
  const caption = captions[ref.card];
  if (!caption) return { error: 'caption ' + JSON.stringify(ref.caption) + ' #' + ref.card + ' not found (' + captions.length + ' on the page)' };
  const cell = caption.parentElement, c = cell.getBoundingClientRect(), t = caption.getBoundingClientRect();
  const r = ref.part === 'caption' ? t : ref.part === 'first' ? cell.firstElementChild.getBoundingClientRect()
    : ref.part === 'icon' ? cell.parentElement.firstElementChild.children[ref.index].getBoundingClientRect()
    : { left: c.left, top: c.top, width: c.width, height: t.top - c.top };
  const c0 = ref.crop ?? { x: 0, y: 0, w: r.width, h: r.height };
  return { x: r.left + scrollX + c0.x, y: r.top + scrollY + c0.y, w: c0.w, h: c0.h };
}`;

async function open(cdp, page) {
  const { targetId } = await cdp.send('Target.createTarget', { url: 'about:blank' });
  const { sessionId } = await cdp.send('Target.attachToTarget', { targetId, flatten: true });
  const errors = [];
  const listener = (msg) => {
    if (msg.sessionId !== sessionId) return;
    if (msg.method === 'Runtime.exceptionThrown') errors.push(msg.params.exceptionDetails.exception?.description ?? msg.params.exceptionDetails.text);
    if (msg.method === 'Fetch.requestPaused') {
      cdp.send('Fetch.fulfillRequest', { requestId: msg.params.requestId, responseCode: 200, responseHeaders: [{ name: 'Content-Type', value: 'text/css' }],
        body: Buffer.from(fontCss).toString('base64') }, sessionId);
    }
  };
  cdp.listeners.push(listener);
  await cdp.send('Page.enable', {}, sessionId);
  await cdp.send('Runtime.enable', {}, sessionId);
  await cdp.send('Fetch.enable', { patterns: [{ urlPattern: '*fonts.googleapis.com*' }] }, sessionId);
  await cdp.send('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: 'reduce' }] }, sessionId);
  await cdp.send('Emulation.setDeviceMetricsOverride', { width: viewport(page), height: 900, deviceScaleFactor: 1, mobile: false }, sessionId);
  const loaded = cdp.once('Page.loadEventFired', sessionId);
  await cdp.send('Page.navigate', { url: pathToFileURL(join(DESIGN, page)).href }, sessionId);
  await loaded;
  const evaluate = async (expression) => (await cdp.send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true }, sessionId)).result.value;
  // The runtime compiles the page with Babel after load: wait until the template has rendered.
  for (let i = 0; i < 200 && !(await evaluate(ready(page))); i++) await sleep(100);
  if (!(await evaluate(ready(page)))) throw new Error(`${page}: the template never rendered`);
  await evaluate('document.fonts.ready.then(() => true)');
  const height = await evaluate('document.documentElement.scrollHeight');
  await cdp.send('Emulation.setDeviceMetricsOverride', { width: viewport(page), height, deviceScaleFactor: 1, mobile: false }, sessionId);
  // macOS text smoothing thickens every stem in Chrome; Skia draws the font's own outline on every
  // platform Compose runs on, so the references are taken with greyscale antialiasing only.
  await evaluate(`(() => { const s = document.createElement('style'); s.textContent = '*{-webkit-font-smoothing:antialiased}';
    document.head.appendChild(s); return true; })()`);
  await evaluate(`(() => { for (const e of document.querySelectorAll('*')) { const s = getComputedStyle(e);
    if ((s.position === 'absolute' || s.position === 'fixed') && s.pointerEvents === 'none' && s.mixBlendMode !== 'normal') e.style.visibility = 'hidden'; } return true; })()`);
  await evaluate('new Promise(r => requestAnimationFrame(() => requestAnimationFrame(() => setTimeout(() => r(true), 100))))');
  if (errors.length) throw new Error(`${page}: ${errors.join('; ')}`);
  return { sessionId, targetId, evaluate, close: async () => { cdp.listeners.splice(cdp.listeners.indexOf(listener), 1); await cdp.send('Target.closeTarget', { targetId }); } };
}

async function shoot(cdp, tab, ref) {
  // A cell sits as far down the page as the lines above it make, and a 14.4 px line leaves it on a
  // fraction of a pixel; Chrome then draws its text on that fraction, which no fixture can. So the
  // page is pushed down by the fraction (a body padding below a pixel moves every box by the same
  // amount), and the cell is photographed on whole pixels, where Blink puts a baseline on a whole
  // pixel, as Compose does (research §1.7).
  await tab.evaluate(`(() => { document.body.style.paddingTop = '0px'; return true; })()`);
  let r = await tab.evaluate(`(${LOCATE})(${JSON.stringify(ref)})`);
  if (r.error) throw new Error(`${ref.group}_${ref.name}: ${r.error}`);
  const shift = Math.ceil(r.y) - r.y;
  if (shift > 0) {
    await tab.evaluate(`new Promise(ok => { document.body.style.paddingTop = '${shift}px'; requestAnimationFrame(() => requestAnimationFrame(() => ok(true))); })`);
    r = await tab.evaluate(`(${LOCATE})(${JSON.stringify(ref)})`);
  }
  r.shift = shift;
  if (![r.x, r.y, r.w, r.h].every((v) => Number.isInteger(v))) throw new Error(`${ref.group}_${ref.name}: ${JSON.stringify(r)} is not whole pixels`);
  const shot = await cdp.send('Page.captureScreenshot', { format: 'png', clip: { x: r.x, y: r.y, width: r.w, height: r.h, scale: 1 } }, tab.sessionId);
  return { png: Buffer.from(shot.data, 'base64'), rect: r };
}

const argv = process.argv.slice(2);
const check = argv.includes('--check');

// --manifest: no Chrome, for the gate. The references are a claim about the pages and fonts they were
// rendered from; when either changes and nobody re-rendered, this fails.
if (argv.includes('--manifest')) {
  const sha = (file) => createHash('sha256').update(readFileSync(file)).digest('hex');
  const m = JSON.parse(readFileSync(join(OUT, 'manifest.json'), 'utf8'));
  const moved = [
    ...Object.entries(m.pages).filter(([p, h]) => sha(join(DESIGN, p)) !== h).map(([p]) => p),
    ...Object.entries(m.fonts).filter(([f, h]) => sha(join(FONTS, f)) !== h).map(([f]) => f),
    ...m.references.filter((r) => !existsSync(join(OUT, r.file))).map((r) => r.file),
  ];
  const listed = new Set(m.references.map((r) => r.file));
  const unlisted = REFERENCES.map((r) => `${r.group}_${r.name}.png`).filter((f) => !listed.has(f));
  if (moved.length || unlisted.length) {
    console.error(`design references are stale: ${[...moved, ...unlisted].join(', ')}; run make references`);
    process.exit(1);
  }
  console.log(`${m.references.length} design references match the vendored pages and fonts`);
  process.exit(0);
}
const chrome = argv.includes('--chrome') ? argv[argv.indexOf('--chrome') + 1] : '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const { cdp, version, close } = await launch(chrome);
const stale = [];
const rects = {};
try {
  if (!check) mkdirSync(OUT, { recursive: true });
  for (const page of [...new Set(REFERENCES.map((r) => r.page))]) {
    const tab = await open(cdp, page);
    try {
      for (const ref of REFERENCES.filter((r) => r.page === page)) {
        const { png, rect } = await shoot(cdp, tab, ref);
        const stem = `${ref.group}_${ref.name}`;
        rects[stem] = rect;
        const target = join(OUT, `${stem}.png`);
        if (check) { if (!existsSync(target) || !readFileSync(target).equals(png)) stale.push(stem); } else writeFileSync(target, png);
      }
    } finally { await tab.close(); }
  }
} finally { await close(); }

const sha = (file) => createHash('sha256').update(readFileSync(file)).digest('hex');
const manifest = {
  browser: version, scale: 1, viewports: Object.fromEntries([...new Set(REFERENCES.map((r) => r.page))].map((p) => [p, viewport(p)])),
  pages: Object.fromEntries([...new Set(REFERENCES.map((r) => r.page))].map((p) => [p, sha(join(DESIGN, p))])),
  fonts: Object.fromEntries(FACES.map(([, file]) => [file, sha(join(FONTS, file))])),
  references: REFERENCES.map((r) => ({ file: `${r.group}_${r.name}.png`, page: r.page, caption: r.caption, card: r.card, part: r.part, crop: r.crop, ...rects[`${r.group}_${r.name}`] })),
};
if (check) {
  const current = existsSync(join(OUT, 'manifest.json')) ? JSON.parse(readFileSync(join(OUT, 'manifest.json'), 'utf8')) : {};
  if (JSON.stringify(current.pages) !== JSON.stringify(manifest.pages)) stale.push('manifest.json (the vendored pages changed)');
  if (stale.length) { console.error(`design references differ from a fresh render in ${version}: ${stale.join(', ')}`); process.exit(1); }
  console.log(`${REFERENCES.length} design references match a fresh render in ${version}`);
} else {
  writeFileSync(join(OUT, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
  console.log(`${REFERENCES.length} design references written with ${version}`);
}
