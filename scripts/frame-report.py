#!/usr/bin/env python3
"""B-51, B-06: the frame benchmark's raw log turned into the protocol's numbers and verdicts.

    python3 scripts/frame-report.py <raw log>
    python3 scripts/frame-report.py --control    # known input: each rule of the report can fail

The protocol is in docs/backlog/B-51-frame-cost-on-desktop.md. Round 0 is discarded for warm-up. Standard
library only, so the gate can run the control.
"""
import random
import sys


def pct(values, p):
    """The nearest-rank percentile."""
    if not values:
        return float("nan")
    s = sorted(values)
    return s[min(len(s) - 1, max(0, int(p / 100 * len(s) + 0.999999) - 1))]


def read(lines):
    frames = []
    for line in lines:
        if line.startswith("#") or line.startswith("arm,") or not line.strip():
            continue
        arm, rnd, frame, interval, work, restart = line.strip().split(",")
        frames.append((arm, int(rnd), int(frame), int(interval) / 1e6, int(work) / 1e6, restart == "1"))
    return frames


def work(frames, arm, restarts=True):
    return [f[4] for f in frames if f[0] == arm and f[1] > 0 and (restarts or not f[5])]


def intervals(frames, arm):
    # A frame's interval is 0 at an arm's start, where there was none.
    return [f[3] for f in frames if f[0] == arm and f[1] > 0 and f[3] > 0]


def per_round_p95(frames, arm, rounds, restarts=True):
    return {r: pct([f[4] for f in frames if f[0] == arm and f[1] == r and (restarts or not f[5])], 95) for r in rounds}


def report(frames):
    """The verdicts, as a dict, and the lines to print."""
    out = []
    rounds = sorted({f[1] for f in frames if f[1] > 0})
    arms = sorted({f[0] for f in frames})
    out.append(f"counted rounds: {len(rounds)}; frames counted: {sum(1 for f in frames if f[1] > 0)}")
    out.append("")
    out.append("| arm | frames | work p50 | work p95 | interval p50 | interval p95 | intervals >= 25 ms |")
    out.append("|---|---|---|---|---|---|---|")
    for arm in arms:
        w, i = work(frames, arm), intervals(frames, arm)
        missed = 100 * sum(1 for v in i if v >= 25) / max(1, len(i))
        out.append(f"| {arm} | {len(w)} | {pct(w, 50):.2f} | {pct(w, 95):.2f} | {pct(i, 50):.2f} | {pct(i, 95):.2f} | {missed:.1f} % |")
    v = {}
    # The controls: if either fails the run is void.
    busy = pct(work(frames, "busy-10"), 50) - pct(work(frames, "idle-a"), 50)
    v["control-10"] = 8 <= busy <= 12
    v["control-25"] = pct(intervals(frames, "busy-25"), 50) >= 25
    out.append("")
    out.append(f"control: busy-10 above idle-a at p50 of work: {busy:.2f} ms (10 +- 2): {'seen' if v['control-10'] else 'VOID'}")
    out.append(f"control: busy-25 interval p50: {pct(intervals(frames, 'busy-25'), 50):.2f} ms (>= 25): {'seen' if v['control-25'] else 'VOID'}")
    # The ruler: cascade-a against cascade-b, paired per round.
    a, b = per_round_p95(frames, "cascade-a", rounds), per_round_p95(frames, "cascade-b", rounds)
    diffs = [a[r] - b[r] for r in rounds]
    ruler = max(abs(d) for d in diffs) if diffs else float("nan")
    ia, ib = per_round_p95(frames, "idle-a", rounds), per_round_p95(frames, "idle-b", rounds)
    out.append(f"ruler: cascade-a - cascade-b, p95 per round: {[round(d, 2) for d in diffs]}, widest {ruler:.2f} ms")
    out.append(f"ruler: idle-a - idle-b, p95 per round: {[round(ia[r] - ib[r], 2) for r in rounds]}")
    # Known order.
    flat = pct(work(frames, "cascade-flat"), 95)
    full = pct(work(frames, "cascade-a"), 95)
    v["order"] = flat <= full + ruler
    out.append(f"known order: cascade-flat p95 {flat:.2f} against cascade-a {full:.2f}: {'holds' if v['order'] else 'VOID'}")
    # Verdicts.
    missed = 100 * sum(1 for x in intervals(frames, "cascade-a") if x >= 25) / max(1, len(intervals(frames, "cascade-a")))
    if abs(full - 16.7) <= 2 * ruler:
        c = "GREY"
    elif full > 16.7:
        c = "RED"
    elif missed <= 5:
        c = "GREEN"
    else:
        c = "GREY"
    v["cascade"] = c
    out.append("")
    out.append(f"**Cascade**: work p95 {full:.2f} ms against 16.7, {missed:.1f} % of intervals >= 25 ms, twice the ruler {2 * ruler:.2f}: {c}")
    for d in ("1", "2", "2.75"):
        roll = pct(work(frames, f"roll-{d}", restarts=False), 95)
        base = pct(work(frames, f"base-{d}"), 95)
        restart = [f[4] for f in frames if f[0] == f"roll-{d}" and f[1] > 0 and f[5]]
        frame = roll - base
        verdict = "GREEN" if frame <= 2 else "RED"
        v[f"raskat-{d}"] = verdict
        out.append(f"**Raskat at {d}**: roll p95 {roll:.2f} less base p95 {base:.2f} = {frame:.2f} ms against 2: {verdict}; restart frames apart, p50 {pct(restart, 50):.2f} ms")
    return v, out


def known(seed, cascade_ms, roll_extra, busy=10.0, spread=0.0):
    """A log whose answer is known: every arm's work near its given level."""
    r = random.Random(seed)
    lines = []
    levels = {"idle-a": 1, "idle-b": 1, "busy-10": 1 + busy, "busy-25": 26, "cascade-a": cascade_ms, "cascade-b": cascade_ms - spread,
              "cascade-flat": cascade_ms / 2, "roll-1": 2 + roll_extra, "roll-2": 2 + roll_extra, "roll-2.75": 2 + roll_extra,
              "base-1": 2, "base-2": 2, "base-2.75": 2}
    for rnd in range(4):
        for arm, level in levels.items():
            for i in range(200):
                wk = level * (1 + r.random() * 0.02)
                iv = 33.3 if arm == "busy-25" else 16.7
                lines.append(f"{arm},{rnd},{i},{int(iv * 1e6)},{int(wk * 1e6)},0")
    return lines


def control():
    good, _ = report(read(known(1, 8.0, 0.5)))
    assert good["cascade"] == "GREEN" and good["raskat-2.75"] == "GREEN" and good["control-10"] and good["control-25"] and good["order"], good
    bad, _ = report(read(known(2, 30.0, 6.0)))
    assert bad["cascade"] == "RED" and bad["raskat-2.75"] == "RED", bad
    near, _ = report(read(known(3, 16.7, 0.5, spread=1.0)))
    assert near["cascade"] == "GREY", near
    blind, _ = report(read(known(4, 8.0, 0.5, busy=0.0)))
    assert not blind["control-10"], blind
    assert pct([1, 2, 3, 4], 50) == 2 and pct(list(range(1, 101)), 95) == 95
    print("frame-report: each rule can fail on known input")


if __name__ == "__main__":
    if sys.argv[1:] == ["--control"]:
        control()
    else:
        with open(sys.argv[1]) as f:
            _, lines = report(read(f))
        print("\n".join(lines))
