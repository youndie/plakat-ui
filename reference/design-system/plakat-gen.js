// plakat-gen.js — единый программный источник агит-графики plakat-ui.
// Всё детерминировано: одинаковый seed → одинаковый результат (порт в Kotlin — plakat-gen.kt).
export const PAL = {
  light: { red: '#BE2F18', ink: '#1D1B18', paper: '#EEE5D1', concrete: '#D4CEC2' },
  dark:  { red: '#BE2F18', ink: '#EEE5D1', paper: '#1F1D1A', concrete: '#34312C' }
};
export const AXIS = -17;
const T = Math.tan(17 * Math.PI / 180);
const snap = v => Math.round(v / 8) * 8;

export function rng(seed) {
  let a = seed >>> 0;
  return () => { a = (a + 0x6D2B79F5) | 0; let t = Math.imul(a ^ (a >>> 15), 1 | a); t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t; return ((t ^ (t >>> 14)) >>> 0) / 4294967296; };
}
function G(seed) {
  const r = rng(seed);
  return { r, int: (a, b) => a + Math.floor(r() * (b - a + 1)), pick: a => a[Math.floor(r() * a.length)], s8: (a, b) => snap(a + r() * (b - a)), bool: (p = .5) => r() < p };
}

// ── геометрия ──────────────────────────────────────────────
function polyOf(s) {
  if (s.k === 'poly') return s.pts;
  if (s.k === 'rect') {
    let p = [[s.x, s.y], [s.x + s.w, s.y], [s.x + s.w, s.y + s.h], [s.x, s.y + s.h]];
    if (s.rot) { const a = s.rot * Math.PI / 180, c = Math.cos(a), n = Math.sin(a); p = p.map(([x, y]) => [s.ox + (x - s.ox) * c - (y - s.oy) * n, s.oy + (x - s.ox) * n + (y - s.oy) * c]); }
    return p;
  }
  return null;
}
function inPoly(x, y, p) { let c = false; for (let i = 0, j = p.length - 1; i < p.length; j = i++) { const [xi, yi] = p[i], [xj, yj] = p[j]; if ((yi > y) !== (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) c = !c; } return c; }
export function hitsZone(s, z) {
  if (s.k === 'circle') { const cx = Math.max(z.x, Math.min(s.cx, z.x + z.w)), cy = Math.max(z.y, Math.min(s.cy, z.y + z.h)); return Math.hypot(s.cx - cx, s.cy - cy) < s.r; }
  const p = polyOf(s), inZ = (x, y) => x > z.x && x < z.x + z.w && y > z.y && y < z.y + z.h;
  for (let i = 0; i < p.length; i++) { const a = p[i], b = p[(i + 1) % p.length]; for (let t = 0; t <= 1; t += .04) if (inZ(a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)) return true; }
  return [[z.x, z.y], [z.x + z.w, z.y], [z.x, z.y + z.h], [z.x + z.w, z.y + z.h]].some(([x, y]) => inPoly(x, y, p));
}
// ── generate(seed) ─────────────────────────────────────────
// Случайная композиция из всего словаря кита. Правила зашиты в код:
// сетка 8 · ось −17° · ровно один красный элемент, не больше трети кадра ·
// чёрные и красные фигуры не заходят в зону заголовка (серая плашка может лежать под ним).
export const ELEMENTS = ['plate', 'strip', 'wedge', 'circle', 'columns', 'checker', 'ground', 'bar', 'rail', 'dot'];
const RED_OK = ['circle', 'wedge', 'bar', 'dot', 'columns', 'checker', 'rail'];
function area(s) {
  if (s.k === 'circle') return Math.PI * s.r * s.r;
  if (s.k === 'rect') return s.w * s.h;
  let A = 0; const p = s.pts; for (let i = 0; i < p.length; i++) { const [x1, y1] = p[i], [x2, y2] = p[(i + 1) % p.length]; A += x1 * y2 - x2 * y1; } return Math.abs(A) / 2;
}
export function generate(seed, o = {}) {
  const W = o.W || 360, H = o.H || 240, g = G(seed), M = Math.min(W, H);
  const head = o.head === false ? null : (o.head || { x: 16, y: 16, w: g.s8(W * .45, W * .6), h: g.s8(H * .26, H * .38) });
  const make = {
    plate: () => { const w = g.s8(W * .3, W * .6), h = g.s8(H * .25, H * .55); return [{ k: 'rect', x: g.pick([0, W - w, g.s8(0, W - w)]), y: g.s8(0, H - h), w, h, f: 'concrete' }]; },
    strip: () => { const cell = g.pick([12, 16]), t = rapport(g.int(1, 99999), cell), y0 = g.s8(0, H - 2 * cell), out = [];
      for (let k = 0; k * t.w < W; k++) for (const s of t.shapes) { if (s.k === 'circle' ? s.cy >= 2 * cell : (s.k === 'rect' ? s.y : Math.min(...s.pts.map(p => p[1]))) >= 2 * cell) continue;
        const dx = k * t.w, f = s.f === 'red' ? 'ink' : s.f;
        out.push(s.k === 'circle' ? { ...s, cx: s.cx + dx, cy: s.cy + y0, f } : s.k === 'rect' ? { ...s, x: s.x + dx, y: s.y + y0, f } : { ...s, pts: s.pts.map(([x, y]) => [x + dx, y + y0]), f }); }
      return out; },
    wedge: () => { const w = g.s8(W * .35, W * .9), h = w * T, x = g.s8(0, W - w * .5), y = g.s8(h + 8, H); return [{ k: 'poly', pts: g.bool(.6) ? [[x, y], [x + w, y - h], [x + w, y]] : [[x, y - h], [x + w, y - h], [x, y]], f: 'ink' }]; },
    circle: () => { const r = g.s8(M * .18, M * .34); return [{ k: 'circle', cx: g.s8(r * .3, W - r * .3), cy: g.s8(r * .3, H + r * .3), r, f: 'ink' }]; },
    columns: () => { const n = g.int(3, 5), cw = g.pick([16, 24, 32]), gap = g.pick([8, 16]), x0 = g.s8(0, W - (n * cw + (n - 1) * gap)), base = H - g.pick([0, 8]), out = [];
      for (let i = 0; i < n; i++) { const h = g.s8(H * .25, H * .6); out.push({ k: 'rect', x: x0 + i * (cw + gap), y: base - h, w: cw, h, f: 'ink' }); } return out; },
    checker: () => { const cols = g.int(3, 5), rows = g.int(2, 3), c = g.pick([24, 32, 40]), x0 = g.s8(0, W - cols * c), y0 = g.s8(0, H - rows * c), par = g.int(0, 1), out = [];
      for (let i = 0; i < cols * rows; i++) { const cx = i % cols, cy = Math.floor(i / cols); out.push({ k: 'rect', x: x0 + cx * c, y: y0 + cy * c, w: c, h: c, f: (cx + cy + par) % 2 ? 'concrete' : 'ink' }); } return out; },
    ground: () => [{ k: 'rect', x: 0, y: H - 8, w: W, h: 8, f: 'ink' }],
    bar: () => { const h = g.pick([8, 16, 24]), w = g.s8(W * .4, W * 1.1), rot = g.pick([AXIS, AXIS, AXIS, 0, 90]), x = g.s8(-W * .1, W * .6), y = g.s8(H * .2, H * .95); return [{ k: 'rect', x, y, w, h, f: 'ink', ...(rot ? { rot, ox: x, oy: y + h / 2 } : {}) }]; },
    rail: () => { const bh = g.pick([8, 16, 24]), x0 = g.s8(-16, W * .3), yb = g.s8(H * .55, H), L1 = g.s8(W * .2, W * .5), gap = g.pick([24, 32, 40]);
      return [{ k: 'rect', x: x0, y: yb - bh / 2, w: L1, h: bh, f: 'ink', rot: AXIS, ox: x0, oy: yb }, { k: 'rect', x: x0 + L1 + gap, y: yb - bh / 2, w: W * 1.2, h: bh, f: 'ink', rot: AXIS, ox: x0, oy: yb }]; },
    dot: () => { const r = g.pick([8, 12, 16, 24]); return [{ k: 'circle', cx: g.s8(r + 8, W - r - 8), cy: g.s8(r + 8, H - r - 8), r, f: 'ink' }]; }
  };
  // состав: одна крупная форма обязательно, всего 3–6 элементов
  const pool = ELEMENTS.slice(), chosen = new Set([g.pick(['circle', 'wedge'])]), n = g.int(3, 6);
  while (chosen.size < n) chosen.add(g.pick(pool));
  const blocked = s => head && s.f !== 'concrete' && hitsZone(s, head);
  const groups = [];
  for (const el of ELEMENTS) {
    if (!chosen.has(el)) continue;
    for (let i = 0; i < 24; i++) { const sh = make[el](); if (!sh.some(blocked)) { groups.push({ el, shapes: sh }); break; } }
  }
  // красный: один элемент (или одна часть группы), не больше трети кадра
  const cands = [];
  groups.forEach(gr => { if (RED_OK.includes(gr.el)) gr.shapes.forEach((s, j) => { if (s.f === 'ink') cands.push({ gr, j, a: area(s), big: gr.el === 'circle' || gr.el === 'wedge' }); }); });
  const fit = cands.filter(c => c.a <= W * H / 3), pref = fit.filter(c => c.big);
  const pickFrom = pref.length && g.bool(.6) ? pref : fit.length ? fit : cands.sort((x, y) => x.a - y.a).slice(0, 1);
  let red = null;
  if (pickFrom.length) { const c = g.pick(pickFrom); c.gr.shapes[c.j] = { ...c.gr.shapes[c.j], f: 'red' }; red = c.gr.el; }
  return { seed, W, H, head, shapes: groups.flatMap(gr => gr.shapes), elements: groups.map(gr => gr.el), red };
}

// rapport(seed, cell) → { w, h, shapes } — бесшовный тайл периода 2tw × 2th клеток.
export function rapport(seed, cell = 24) {
  const g = G(seed), tw = g.pick([2, 3, 4]), th = g.pick([2, 3]);
  const M = ['sq', 'tri1', 'tri2', 'half', 'circle', 'bar', 'empty'];
  const redMotif = g.int(0, tw * th - 1);
  const tile = Array.from({ length: tw * th }, (_, i) => ({ m: i === redMotif ? g.pick(['circle', 'tri1', 'sq']) : g.pick(M), f: i === redMotif ? 'red' : g.pick(['ink', 'ink', 'concrete']) }));
  const shapes = [], W = 2 * tw * cell, H = 2 * th * cell;
  for (let y = 0; y < 2 * th; y++) for (let x = 0; x < 2 * tw; x++) {
    const t = tile[(y % th) * tw + (x % tw)]; if (t.m === 'empty') continue;
    const flip = (Math.floor(x / tw) + Math.floor(y / th)) % 2 === 1;
    let m = t.m; if (flip && m === 'tri1') m = 'tri2'; else if (flip && m === 'tri2') m = 'tri1';
    const X = x * cell, Y = y * cell, c = cell;
    if (m === 'sq') shapes.push({ k: 'rect', x: X, y: Y, w: c, h: c, f: t.f });
    else if (m === 'half') shapes.push({ k: 'rect', x: X, y: Y, w: c / 2, h: c, f: t.f });
    else if (m === 'bar') shapes.push({ k: 'rect', x: X, y: Y + c * .375, w: c, h: c * .25, f: t.f });
    else if (m === 'circle') shapes.push({ k: 'circle', cx: X + c / 2, cy: Y + c / 2, r: c * .375, f: t.f });
    else if (m === 'tri1') shapes.push({ k: 'poly', pts: [[X, Y + c], [X + c, Y], [X + c, Y + c]], f: t.f });
    else shapes.push({ k: 'poly', pts: [[X, Y], [X + c, Y], [X, Y + c]], f: t.f });
  }
  return { w: W, h: H, shapes, tw, th };
}

// ── вывод ──────────────────────────────────────────────────
const n2 = v => +(+v).toFixed(2);
export function shapeSVG(s, pal) {
  const fill = pal[s.f] || s.f, tr = s.rot ? ` transform="rotate(${n2(s.rot)} ${n2(s.ox)} ${n2(s.oy)})"` : '';
  if (s.k === 'rect') return `<rect x="${n2(s.x)}" y="${n2(s.y)}" width="${n2(s.w)}" height="${n2(s.h)}" fill="${fill}"${tr}/>`;
  if (s.k === 'circle') return `<circle cx="${n2(s.cx)}" cy="${n2(s.cy)}" r="${n2(s.r)}" fill="${fill}"/>`;
  return `<polygon points="${s.pts.map(p => p.map(n2).join(',')).join(' ')}" fill="${fill}"/>`;
}
export function toSVG(shapes, W, H, pal, bg = 'paper') {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}"><rect width="${W}" height="${H}" fill="${pal[bg]}"/>${shapes.map(s => shapeSVG(s, pal)).join('')}</svg>`;
}
export function toDOM(s, pal) {
  const c = pal[s.f] || s.f;
  if (s.k === 'rect') return { x: s.x + 'px', y: s.y + 'px', w: s.w + 'px', h: s.h + 'px', c, r: '0', clip: 'none', t: s.rot ? `rotate(${s.rot}deg)` : 'none', o: s.rot ? `${s.ox - s.x}px ${s.oy - s.y}px` : '50% 50%' };
  if (s.k === 'circle') return { x: (s.cx - s.r) + 'px', y: (s.cy - s.r) + 'px', w: 2 * s.r + 'px', h: 2 * s.r + 'px', c, r: '50%', clip: 'none', t: 'none', o: '50% 50%' };
  return { x: '0px', y: '0px', w: '100%', h: '100%', c, r: '0', clip: `polygon(${s.pts.map(p => `${n2(p[0])}px ${n2(p[1])}px`).join(',')})`, t: 'none', o: '50% 50%' };
}

// ── растр (CPU) ────────────────────────────────────────────
// halftone(ctx, src, o) рисует src в прямоугольник o.W×o.H одной краской.
// mode 'dots' — AM-растр под углом o.angle, шаг o.pitch: тот же алгоритм, что AGSL-шейдер (API 33+),
//              на старом Android считается один раз в Bitmap и кэшируется.
// mode 'mask' — 1-битный порог без точек: самый дешёвый фолбэк.
// mode 'gray' — только обесцвечивание и контраст (этап 2 правила).
export function halftone(ctx, src, o) {
  const W = o.W, H = o.H, pitch = o.pitch || 8, a = (o.angle ?? 45) * Math.PI / 180, k = o.contrast ?? 1.4, mode = o.mode || 'dots';
  const off = document.createElement('canvas'); off.width = W; off.height = H;
  const oc = off.getContext('2d'); coverDraw(oc, src, W, H);
  const img = oc.getImageData(0, 0, W, H), d = img.data;
  const L = i => { const l = (.299 * d[i] + .587 * d[i + 1] + .114 * d[i + 2]) / 255; return Math.max(0, Math.min(1, (l - .5) * k + .5)); };
  if (mode === 'gray' || mode === 'mask') {
    const ink = hex(o.ink), pap = hex(o.paper), thr = o.threshold ?? .5;
    for (let i = 0; i < d.length; i += 4) {
      const l = L(i);
      if (mode === 'gray') { const v = l * 255; d[i] = d[i + 1] = d[i + 2] = v; }
      else { const c = l < thr ? ink : pap; d[i] = c[0]; d[i + 1] = c[1]; d[i + 2] = c[2]; }
      d[i + 3] = 255;
    }
    ctx.putImageData(img, o.x || 0, o.y || 0);
    return;
  }
  const lum = (x, y) => { x = Math.max(0, Math.min(W - 1, x | 0)); y = Math.max(0, Math.min(H - 1, y | 0)); return L((y * W + x) * 4); };
  ctx.save(); ctx.translate(o.x || 0, o.y || 0);
  ctx.fillStyle = o.paper; ctx.fillRect(0, 0, W, H); ctx.fillStyle = o.ink;
  const ca = Math.cos(a), sa = Math.sin(a), D = Math.hypot(W, H), rmax = pitch * Math.SQRT1_2;
  for (let v = -D; v < D; v += pitch) for (let u = -D; u < D; u += pitch) {
    const x = W / 2 + u * ca - v * sa, y = H / 2 + u * sa + v * ca;
    if (x < -pitch || y < -pitch || x > W + pitch || y > H + pitch) continue;
    const l = (lum(x, y) + lum(x - 1, y) + lum(x + 1, y) + lum(x, y - 1) + lum(x, y + 1)) / 5;
    const r = rmax * Math.sqrt(1 - l);
    if (r > .35) { ctx.beginPath(); ctx.arc(x, y, r, 0, Math.PI * 2); ctx.fill(); }
  }
  ctx.restore();
}
export function coverDraw(ctx, src, W, H) {
  const sw = src.naturalWidth || src.width, sh = src.naturalHeight || src.height, s = Math.max(W / sw, H / sh);
  ctx.drawImage(src, (W - sw * s) / 2, (H - sh * s) / 2, sw * s, sh * s);
}
export function standIn(W, H, fx = .55, fy = .4) {
  const c = document.createElement('canvas'); c.width = W; c.height = H; const x = c.getContext('2d');
  x.fillStyle = '#f2f2f2'; x.fillRect(0, 0, W, H);
  const R = Math.min(W, H), gr = x.createRadialGradient(W * fx, H * fy, 0, W * fx, H * fy, R * .62);
  gr.addColorStop(0, '#151515'); gr.addColorStop(.3, '#202020'); gr.addColorStop(.55, '#8a8a8a'); gr.addColorStop(.8, '#dcdcdc'); gr.addColorStop(1, '#f4f4f4');
  x.fillStyle = gr; x.fillRect(0, 0, W, H);
  return c;
}
function hex(h) { const n = parseInt(h.slice(1), 16); return [n >> 16 & 255, n >> 8 & 255, n & 255]; }
