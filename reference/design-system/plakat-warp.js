// plakat-warp.js — текст на объёмной форме («Проекция»).
// Алгоритм тот же, что в Compose: текст один раз растрируется в битмап, затем натягивается
// сеткой треугольников на форму (Skia drawVertices + ImageShader / Android Canvas.drawBitmapMesh).
// Координаты — CSS px холста. v = 0 — верх прописных, v = 1 — базовая линия.

export const T17 = Math.tan(17 * Math.PI / 180);
const VMIN = -0.32, VMAX = 1.22;   // запас под Й/Ё сверху и Д/Ц/Щ снизу

export function textSource(text, color, font = 'Rubik Mono One', F = 240) {
  const m = document.createElement('canvas').getContext('2d');
  m.font = `${F}px "${font}"`;
  const tm = m.measureText(text), cap = m.measureText('H').actualBoundingBoxAscent || F * .7;
  const L = tm.actualBoundingBoxLeft || 0, w = Math.ceil(L + (tm.actualBoundingBoxRight || tm.width)) + 2;
  const h = Math.ceil(cap * (VMAX - VMIN));
  const c = document.createElement('canvas'); c.width = w; c.height = h;
  const x = c.getContext('2d'); x.font = m.font; x.fillStyle = color; x.textBaseline = 'alphabetic';
  x.fillText(text, L + 1, cap * (1 - VMIN));
  return { canvas: c, w, h, aspect: w / cap };   // aspect — естественное отношение ширины строки к высоте прописных
}

// warp(ctx, src, map, { nu, nv, folds }) — map(u, v) → [x, y] | null (невидимо)
export function warp(ctx, src, map, o = {}) {
  const nu = o.nu || 40, nv = Math.max(o.nv || 8, 16);
  const us = new Set(); for (let i = 0; i <= nu; i++) us.add(i / nu); (o.folds || []).forEach(f => us.add(f));
  const U = [...us].sort((a, b) => a - b), V = []; for (let j = 0; j <= nv; j++) V.push(VMIN + (VMAX - VMIN) * j / nv);
  (o.vfolds || []).forEach(f => { if (!V.some(x => Math.abs(x - f) < 1e-6)) V.push(f); }); V.sort((a, b) => a - b);
  const sx = u => u * src.w, sy = v => (v - VMIN) / (VMAX - VMIN) * src.h;
  const P = U.map(u => V.map(v => map(u, v)));
  for (let i = 0; i < U.length - 1; i++) for (let j = 0; j < V.length - 1; j++) {
    const a = P[i][j], b = P[i + 1][j], c = P[i + 1][j + 1], d = P[i][j + 1];
    if (!a || !b || !c || !d) continue;
    const s = [[sx(U[i]), sy(V[j])], [sx(U[i + 1]), sy(V[j])], [sx(U[i + 1]), sy(V[j + 1])], [sx(U[i]), sy(V[j + 1])]];
    tri(ctx, src.canvas, s[0], s[1], s[2], a, b, c);
    tri(ctx, src.canvas, s[0], s[2], s[3], a, c, d);
  }
}
function tri(ctx, img, s0, s1, s2, d0, d1, d2) {
  const den = (s1[0] - s0[0]) * (s2[1] - s0[1]) - (s2[0] - s0[0]) * (s1[1] - s0[1]); if (!den) return;
  const a = ((d1[0] - d0[0]) * (s2[1] - s0[1]) - (d2[0] - d0[0]) * (s1[1] - s0[1])) / den;
  const b = ((d1[1] - d0[1]) * (s2[1] - s0[1]) - (d2[1] - d0[1]) * (s1[1] - s0[1])) / den;
  const c = ((d2[0] - d0[0]) * (s1[0] - s0[0]) - (d1[0] - d0[0]) * (s2[0] - s0[0])) / den;
  const d = ((d2[1] - d0[1]) * (s1[0] - s0[0]) - (d1[1] - d0[1]) * (s2[0] - s0[0])) / den;
  const e = d0[0] - a * s0[0] - c * s0[1], f = d0[1] - b * s0[0] - d * s0[1];
  const cx = (d0[0] + d1[0] + d2[0]) / 3, cy = (d0[1] + d1[1] + d2[1]) / 3, g = p => { const dx = p[0] - cx, dy = p[1] - cy, l = Math.hypot(dx, dy) || 1; return [p[0] + dx / l * .6, p[1] + dy / l * .6]; };
  const q0 = g(d0), q1 = g(d1), q2 = g(d2);
  ctx.save(); ctx.beginPath(); ctx.moveTo(q0[0], q0[1]); ctx.lineTo(q1[0], q1[1]); ctx.lineTo(q2[0], q2[1]); ctx.closePath(); ctx.clip();
  ctx.transform(a, b, c, d, e, f); ctx.drawImage(img, 0, 0); ctx.restore();
}

// гомография единичного квадрата → четырёхугольник (p0 TL, p1 TR, p2 BR, p3 BL)
export function quadMap(p0, p1, p2, p3) {
  const [x0, y0] = p0, [x1, y1] = p1, [x2, y2] = p2, [x3, y3] = p3;
  const dx1 = x1 - x2, dx2 = x3 - x2, dx3 = x0 - x1 + x2 - x3, dy1 = y1 - y2, dy2 = y3 - y2, dy3 = y0 - y1 + y2 - y3;
  const den = dx1 * dy2 - dx2 * dy1, g = (dx3 * dy2 - dx2 * dy3) / den, h = (dx1 * dy3 - dx3 * dy1) / den;
  const a = x1 - x0 + g * x1, b = x3 - x0 + h * x3, c = x0, d = y1 - y0 + g * y1, e = y3 - y0 + h * y3, f = y0;
  return (u, v) => { const w = g * u + h * v + 1; return [(a * u + b * v + c) / w, (d * u + e * v + f) / w]; };
}

// ── формы ────────────────────────────────────────────────────
const D2R = Math.PI / 180;
const lerp2 = (p, q, t) => [p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t];
const padUV = (pad, u, v) => [pad[0] + u * (1 - 2 * pad[0]), pad[1] + v * (1 - 2 * pad[1])];
const rot = (ox, oy, a) => { const c = Math.cos(a * D2R), n = Math.sin(a * D2R); return ([x, y]) => [ox + x * c - y * n, oy + x * n + y * c]; };

// Рупор: плоскость-клин. Узкий конец h0 в начале оси, раскрытие spread°.
// angle — направление оси: 0 (вправо), −17 (по оси кита), −90 (вверх).
// anchor — что лежит на оси: 'floor' (нижняя кромка), 'ceiling' (верхняя), 'axis' (середина, раскрытие поровну).
// persp 0…1 — от равномерной трапеции (0) до честной перспективы (1): буквы сжимаются к узкому концу.
export function horn({ ox, oy, L, h0, spread = 17, angle = 0, anchor = 'floor', persp = 1, pad = [.04, .16] }) {
  const t = Math.tan(spread * D2R), H = h0 + L * t;
  const loc = anchor === 'ceiling' ? [[0, 0], [L, 0], [L, H], [0, h0]]
    : anchor === 'axis' ? [[0, -h0 / 2], [L, -H / 2], [L, H / 2], [0, h0 / 2]]
    : [[0, -h0], [L, -H], [L, 0], [0, 0]];
  const R = rot(ox, oy, angle), [tl, tr, br, bl] = loc.map(R), hom = quadMap(tl, tr, br, bl);
  const bil = (u, v) => lerp2(lerp2(tl, tr, u), lerp2(bl, br, u), v);
  return { quad: [tl, tr, br, bl], folds: [], map: (u, v) => { const [U, V] = padUV(pad, u, v), p = bil(U, V), q = hom(U, V); return lerp2(p, q, persp); } };
}

// Конус: ось вдоль angle, радиус r0 → r1 (r0 = r1 — вал). yaw° — поворот к зрителю, arc° — дуга текста вокруг оси.
export function cone({ ox, oy, L, r0, r1, yaw = 24, arc = 60, angle = 0, pad = [.04, 0] }) {
  const R = rot(ox, oy, angle), s = Math.sin(yaw * D2R), A = arc * D2R;
  const u0 = pad[0], u1 = 1 - Math.max(pad[1] || 0, (2 * r1 * s + 8) / L);   // текст кончается до раструба
  return { ox, oy, L, r0, r1, s, angle, folds: [], R,
    map: (u, v) => { const U = u0 + u * (u1 - u0), x = U * L, r = r0 + (r1 - r0) * U, th = A * (1 - 2 * v); if (Math.cos(th) < .05) return null; return R([x + r * Math.cos(th) * s, -r * Math.sin(th)]); } };
}

// Колонна: вертикальный цилиндр, вид сверху под tilt. arc — дуга текста, spin° — поворот надписи вокруг оси.
export function column({ cx, r, top, bottom, band, arc = 70, tilt = .16, spin = 0 }) {
  const b = r * tilt, A = arc * D2R, y0 = band[0], y1 = band[1];
  return { cx, r, top, bottom, b, folds: [],
    map: (u, v) => { const th = -A + 2 * A * u + spin * D2R; if (Math.cos(th) < .05) return null; return [cx + r * Math.sin(th), y0 + (y1 - y0) * v + b * Math.cos(th)]; } };
}

// Шар: надпись поясом по экватору. lon° — дуга по долготе, lat° — высота пояса, tilt° — вид сверху.
export function sphere({ cx, cy, R, lon = 70, lat = 22, tilt = 14 }) {
  const Lo = lon * D2R, La = lat * D2R, ct = Math.cos(tilt * D2R), st = Math.sin(tilt * D2R);
  return { cx, cy, R, folds: [],
    map: (u, v) => { const lo = -Lo + 2 * Lo * u, la = La * (1 - 2 * v), y = R * Math.sin(la), z = R * Math.cos(la) * Math.cos(lo), x = R * Math.cos(la) * Math.sin(lo);
      if (y * st + z * ct < R * .05) return null; return [cx + x, cy - (y * ct - z * st)]; } };
}

// Брус: косоугольная проекция, глубина d по −17°. fold: 'side' — надпись ломается через боковое ребро,
// 'top' — через верхнее; at — доля надписи до перелома.
export function box({ x, y, w, h, d, band, fold = 'side', at = null }) {
  const dx = d * Math.cos(17 * D2R), dy = -d * Math.sin(17 * D2R), y0 = band[0], y1 = band[1];
  if (fold === 'top') {
    // верхняя доля f высоты букв перегибается на крышку в натуральном размере (без сжатия): уходит вглубь на f·Hcap ≤ d
    let f = at ?? .3; f = Math.min(f, d / ((y1 - y) + d));
    const Hc = (y1 - y) / (1 - f);
    return { x, y, w, h, dx, dy, folds: [], vfold: f,
      map: (u, v) => { const X = x + 8 + u * (w - 16); if (v >= f) return [X, y + (v - f) * Hc]; const s = (f - v) * Hc / d; return [X + s * dx, y + s * dy]; } };
  }
  const Lt = w + d, fu = at ?? w / Lt;
  return { x, y, w, h, dx, dy, folds: [fu],
    map: (u, v) => { const yy = y0 + (y1 - y0) * v; if (u <= fu) return [x + u / fu * w, yy]; const s = (u - fu) / (1 - fu); return [x + w + s * dx, yy + s * dy]; } };
}

// ── заливка форм (плоские грани, без градиентов) ─────────────
// Несколько строк на одной форме: строка i из n занимает свою полосу v. Зазор должен вместить над- и подстрочные
// знаки (Й/Ё сверху, Д/Ц/Щ снизу): gap/(1−gap) ≥ −VMIN + (VMAX − 1) = 0.54 → gap = 0.36.
export const LINE_GAP = .36;
export function lineMap(map, i, n, gap = LINE_GAP) { const a = i / n + gap / (2 * n), k = (1 - gap) / n; return (u, v) => map(u, a + v * k); }
export const lineV = (v, i, n, gap = LINE_GAP) => (v - i / n - gap / (2 * n)) * n / (1 - gap);

// Лестница: n ступеней уходят вверх и вглубь по −17°. На каждом подступенке — своя строка (сверху вниз — по ходу чтения).
export function stairs({ x, y, n, w, rh, sd }) {
  const sdx = sd * Math.cos(17 * Math.PI / 180), sdy = -sd * Math.sin(17 * Math.PI / 180);
  const risers = Array.from({ length: n }, (_, i) => ({ x: x + i * sdx, y: y - (i + 1) * rh + i * sdy, w, h: rh }));
  return { n, sdx, sdy, risers, maps: risers.slice().reverse().map(r => (u, v) => [r.x + 8 + u * (r.w - 16), r.y + r.h * .16 + v * r.h * .68]) };
}
export function drawStairs(ctx, f, riser, tread, side) {
  for (let i = f.n - 1; i >= 0; i--) { const r = f.risers[i];
    ctx.fillStyle = tread; ctx.beginPath(); ctx.moveTo(r.x, r.y); ctx.lineTo(r.x + f.sdx, r.y + f.sdy); ctx.lineTo(r.x + r.w + f.sdx, r.y + f.sdy); ctx.lineTo(r.x + r.w, r.y); ctx.closePath(); ctx.fill();
    if (side) { ctx.fillStyle = side; ctx.beginPath(); ctx.moveTo(r.x + r.w, r.y); ctx.lineTo(r.x + r.w + f.sdx, r.y + f.sdy); ctx.lineTo(r.x + r.w + f.sdx, r.y + r.h * (i + 1) + f.sdy - i * f.sdy * 0); ctx.lineTo(r.x + r.w, r.y + r.h * (i + 1)); ctx.closePath(); ctx.fill(); }
    ctx.fillStyle = riser; ctx.fillRect(r.x, r.y, r.w, r.h); }
}
export function drawHorn(ctx, f, fill) { const q = f.quad; ctx.fillStyle = fill; ctx.beginPath(); ctx.moveTo(...q[0]); q.slice(1).forEach(p => ctx.lineTo(...p)); ctx.closePath(); ctx.fill(); }
export function drawColumn(ctx, f, body, cap) {
  ctx.fillStyle = body; ctx.beginPath(); ctx.moveTo(f.cx - f.r, f.top); ctx.lineTo(f.cx - f.r, f.bottom); ctx.ellipse(f.cx, f.bottom, f.r, f.b, 0, Math.PI, 0, true); ctx.lineTo(f.cx + f.r, f.top); ctx.closePath(); ctx.fill();
  ctx.fillStyle = cap; ctx.beginPath(); ctx.ellipse(f.cx, f.top, f.r, f.b, 0, 0, Math.PI * 2); ctx.fill();
}
export function drawCone(ctx, f, body, mouth) {
  const P = f.R, s = f.s, L = f.L;
  ctx.fillStyle = body; ctx.beginPath(); const q = [[0, -f.r0], [L, -f.r1], [L, f.r1], [0, f.r0]].map(P); ctx.moveTo(...q[0]); q.slice(1).forEach(p => ctx.lineTo(...p)); ctx.closePath(); ctx.fill();
  const ell = (cx, r, fill, half) => { ctx.save(); const c = P([cx, 0]); ctx.translate(c[0], c[1]); ctx.rotate((f.angle || 0) * D2R); ctx.fillStyle = fill; ctx.beginPath(); if (half) ctx.ellipse(0, 0, Math.max(.5, r * s), r, 0, -Math.PI / 2, Math.PI / 2, true); else ctx.ellipse(0, 0, Math.max(.5, r * s), r, 0, 0, Math.PI * 2); ctx.fill(); ctx.restore(); };
  ell(0, f.r0, body, true); if (mouth) ell(L, f.r1, mouth, false);
}
export function drawSphere(ctx, f, body) { ctx.fillStyle = body; ctx.beginPath(); ctx.arc(f.cx, f.cy, f.R, 0, Math.PI * 2); ctx.fill(); }
export function drawBox(ctx, f, front, side, topc) {
  const { x, y, w, h, dx, dy } = f;
  ctx.fillStyle = topc; ctx.beginPath(); ctx.moveTo(x, y); ctx.lineTo(x + dx, y + dy); ctx.lineTo(x + w + dx, y + dy); ctx.lineTo(x + w, y); ctx.closePath(); ctx.fill();
  ctx.fillStyle = side; ctx.beginPath(); ctx.moveTo(x + w, y); ctx.lineTo(x + w + dx, y + dy); ctx.lineTo(x + w + dx, y + h + dy); ctx.lineTo(x + w, y + h); ctx.closePath(); ctx.fill();
  ctx.fillStyle = front; ctx.fillRect(x, y, w, h);
}
