// plakat-raskat.js — «Раскат»: переход двух красок, как при ирисовой печати (две краски на одном валике).
// Единственное исключение из «нет градиентов»: одна фигура на экран. Пиксель = краска A или B, без смешивания.
//
//   t  — положение поперёк линии перехода (по направлению angle), 0…1 по фигуре
//   p  — логистический профиль 1 / (1 + e^(−hard·(t − t0))): переход сжат к линии t0 — у раската есть шов
//   m  — порог фактуры (0…1), считается один раз и кэшируется:
//        grain — шум зерна; streak вытягивает его вдоль хода валика (вдоль линии перехода)
//        dots  — AM-растр 45°: m = (d / rmax)²  → площадь точки ∝ p
//        lines — штрих параллельно линии перехода: m = frac(поперёк / период) → толщина линии ∝ p
//   цвет = p > m ? B : A
// Движение roll: t0 едет от 1.15 до целевого — вторая краска вкатывается с дальнего края. Порог m не пересчитывается.

function hash(x, y, s) { let h = (x * 374761393 + y * 668265263 + s * 2147483647) | 0; h = Math.imul(h ^ (h >>> 13), 1274126177); return ((h ^ (h >>> 16)) >>> 0) / 4294967296; }
function vnoise(x, y, s) {
  const xi = Math.floor(x), yi = Math.floor(y), fx = x - xi, fy = y - yi, sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy);
  const a = hash(xi, yi, s), b = hash(xi + 1, yi, s), c = hash(xi, yi + 1, s), d = hash(xi + 1, yi + 1, s);
  return (a * (1 - sx) + b * sx) * (1 - sy) + (c * (1 - sx) + d * sx) * sy;
}
const hex = h => [parseInt(h.slice(1, 3), 16), parseInt(h.slice(3, 5), 16), parseInt(h.slice(5, 7), 16)];
const cache = new Map();

export function raskatField(w, h, o) {
  const { angle = -17, grain = 2, seed = 1919, mode = 'grain', streak = 0, rough = .35, dpr = 2 } = o;
  const key = [w, h, angle, grain, seed, mode, streak, rough, dpr].join('|');
  if (cache.has(key)) return cache.get(key);
  const r = angle * Math.PI / 180, dx = Math.cos(r), dy = Math.sin(r), ext = Math.abs(dx) * w + Math.abs(dy) * h, g = grain * dpr;
  const T = new Float32Array(w * h), Mt = new Float32Array(w * h);
  const pitch = g * 4, rmax = pitch * Math.SQRT1_2, c45 = Math.SQRT1_2;
  for (let j = 0; j < h; j++) for (let i = 0; i < w; i++) {
    const k = j * w + i, X = i - w / 2, Y = j - h / 2;
    const across = X * dx + Y * dy, along = -X * dy + Y * dx;
    T[k] = across / ext + .5;
    const wn = hash(i, j, seed + 7);
    let m;
    if (mode === 'dots') {
      const u = (i * c45 + j * c45) / pitch, v = (-i * c45 + j * c45) / pitch, du = u - Math.round(u), dv = v - Math.round(v);
      const d = Math.hypot(du, dv) * pitch; m = (d / rmax) ** 2 + (wn - .5) * rough * .25;
    } else if (mode === 'lines') {
      const q = across / pitch + (vnoise(along / (g * 3), across / g, seed) - .5) * rough * .5; m = q - Math.floor(q);
    } else {
      const sa = g * (1 + streak * 10), sc = g;
      m = .6 * vnoise(along / sa, across / sc, seed) + .4 * wn;
    }
    Mt[k] = m;
  }
  const f = { T, M: Mt, w, h };
  if (cache.size > 24) cache.delete(cache.keys().next().value);
  cache.set(key, f);
  return f;
}

export function raskatImage(w, h, o) {
  const { a = '#BE2F18', b = '#EEE5D1', t0 = .5, hard = 10 } = o, F = raskatField(w, h, o);
  const c = document.createElement('canvas'); c.width = w; c.height = h;
  const x = c.getContext('2d'), img = x.createImageData(w, h), d = img.data, A = hex(a), B = hex(b);
  for (let k = 0; k < w * h; k++) {
    const p = 1 / (1 + Math.exp(-hard * (F.T[k] - t0))), C = p > F.M[k] ? B : A, q = k * 4;
    d[q] = C[0]; d[q + 1] = C[1]; d[q + 2] = C[2]; d[q + 3] = 255;
  }
  x.putImageData(img, 0, 0);
  return c;
}

// shape: { k: 'circle', cx, cy, r } | { k: 'rect', x, y, w, h } | { k: 'poly', pts } — CSS px; ctx уже масштабирован на dpr
export function drawRaskat(ctx, shape, o = {}) {
  const dpr = o.dpr || 2;
  const bb = shape.k === 'circle' ? { x: shape.cx - shape.r, y: shape.cy - shape.r, w: 2 * shape.r, h: 2 * shape.r }
    : shape.k === 'rect' ? { x: shape.x, y: shape.y, w: shape.w, h: shape.h }
    : (() => { const xs = shape.pts.map(p => p[0]), ys = shape.pts.map(p => p[1]); return { x: Math.min(...xs), y: Math.min(...ys), w: Math.max(...xs) - Math.min(...xs), h: Math.max(...ys) - Math.min(...ys) }; })();
  const img = raskatImage(Math.max(1, Math.round(bb.w * dpr)), Math.max(1, Math.round(bb.h * dpr)), { ...o, dpr });
  ctx.save(); ctx.beginPath();
  if (shape.k === 'circle') ctx.arc(shape.cx, shape.cy, shape.r, 0, Math.PI * 2);
  else if (shape.k === 'rect') ctx.rect(shape.x, shape.y, shape.w, shape.h);
  else { ctx.moveTo(...shape.pts[0]); shape.pts.slice(1).forEach(p => ctx.lineTo(...p)); ctx.closePath(); }
  ctx.clip(); ctx.imageSmoothingEnabled = false; ctx.drawImage(img, bb.x, bb.y, bb.w, bb.h); ctx.restore();
}

// roll: t0(τ) от 1.15 до цели за dur мс, кривая hard-stop. Возвращает функцию отмены.
export function roll(draw, target, dur = 320, onDone) {
  const HS = t => { let lo = 0, hi = 1, s = t; for (let i = 0; i < 24; i++) { s = (lo + hi) / 2; const x = 3 * (1 - s) * (1 - s) * s * .2 + 3 * (1 - s) * s * s * .9 + s * s * s; if (x < t) lo = s; else hi = s; } return 3 * (1 - s) * (1 - s) * s * .8 + 3 * (1 - s) * s * s * .9 + s * s * s; };
  const t0 = performance.now(), from = 1.15; let raf;
  const step = now => { const k = Math.min(1, (now - t0) / dur); draw(from + (target - from) * HS(k)); if (k < 1) raf = requestAnimationFrame(step); else onDone && onDone(); };
  raf = requestAnimationFrame(step);
  return () => cancelAnimationFrame(raf);
}

export const PAIRS = {
  'red-paper': ['#BE2F18', '#EEE5D1', 'киноварь → бумага'],
  'red-ink': ['#BE2F18', '#1D1B18', 'киноварь → чёрный'],
  'ocher-red': ['#B5862F', '#BE2F18', 'охра → киноварь'],
  'slate-red': ['#4E6272', '#BE2F18', 'серо-синий → киноварь'],
  'ink-paper': ['#1D1B18', '#EEE5D1', 'чёрный → бумага']
};
