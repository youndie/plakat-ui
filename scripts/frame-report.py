#!/usr/bin/env python3
"""B-51, B-06: the frame benchmark's raw log turned into the protocol's numbers and verdicts.

    python3 scripts/frame-report.py <raw log>
    python3 scripts/frame-report.py --device <frames log> <metrics log>   # B-06: also Android's FrameMetrics
    python3 scripts/frame-report.py --blind <frames log> <metrics log>    # B-06: the recorder against a known cost
    python3 scripts/frame-report.py --control    # known input: each rule of the report can fail

The protocols are in docs/backlog/B-51-frame-cost-on-desktop.md and B-06-frame-cost-measurement.md. Round 0 is
discarded for warm-up. Standard library only, so the gate can run the control.
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


# A roll restarts every 21 frames, and the protocol reads its frame with the restarts apart. FrameMetrics has no frame's
# number to tag them by, and they are the slowest of an arm by far, so that one frame in 21 is taken off the top, per round.
RESTART_EVERY = 21


def read_metrics(lines):
    """Per counted frame: arm, round, main thread (animation + layout + draw), draw, total, GPU, past its deadline."""
    rows = []
    head = None
    for line in lines:
        if line.startswith("#") or not line.strip():
            continue
        cells = line.strip().split(",")
        if cells[0] == "arm":
            head = cells
            continue
        m = dict(zip(head, cells))
        if m["first"] == "1":
            continue
        ns = lambda k: int(m[k]) / 1e6  # noqa: E731
        rows.append((m["arm"], int(m["round"]), ns("animation_ns") + ns("layout_ns") + ns("draw_ns"), ns("draw_ns"),
                     ns("total_ns"), ns("gpu_ns"), ns("total_ns") > ns("deadline_ns")))
    return rows


def main_thread(rows, arm, restarts=True):
    """The main thread's cost per frame, less a roll's restarts."""
    out = []
    for rnd in sorted({r[1] for r in rows if r[0] == arm and r[1] > 0}):
        values = sorted(r[2] for r in rows if r[0] == arm and r[1] == rnd)
        drop = 0 if restarts else len(values) // RESTART_EVERY
        out += values[: len(values) - drop]
    return out


def past(rows, arm):
    these = [r for r in rows if r[0] == arm and r[1] > 0]
    return 100 * sum(1 for r in these if r[6]) / max(1, len(these))


def on_time(share):
    return "GREEN" if share <= 5 else "RED" if share > 20 else "GREY"


def device_report(frames, rows):
    """B-06, on top of the desktop's report: what FrameMetrics says, the amended reading, and the literal one beside it."""
    v, out = report(frames)
    out.append("")
    out.append("| arm | frames | main p50 | main p95 | draw p50 | draw p95 | total p50 | total p95 | GPU p50 | GPU p95 | past % |")
    out.append("|---|---|---|---|---|---|---|---|---|---|---|")
    arms = sorted({r[0] for r in rows})
    for arm in arms:
        these = [r for r in rows if r[0] == arm and r[1] > 0]
        col = lambda i: [r[i] for r in these]  # noqa: E731
        out.append(
            f"| {arm} | {len(these)} | {pct(col(2), 50):.2f} | {pct(col(2), 95):.2f} | {pct(col(3), 50):.2f} | {pct(col(3), 95):.2f} "
            f"| {pct(col(4), 50):.2f} | {pct(col(4), 95):.2f} | {pct(col(5), 50):.2f} | {pct(col(5), 95):.2f} | {past(rows, arm):.1f} |",
        )
    out.append("")
    v["control-25-past"] = past(rows, "busy-25") >= 90
    v["control-idle-past"] = past(rows, "idle-a") <= 5
    out.append(f"control: busy-25 past its deadline {past(rows, 'busy-25'):.1f} % (>= 90): {'seen' if v['control-25-past'] else 'VOID'}")
    out.append(f"control: idle-a past its deadline {past(rows, 'idle-a'):.1f} % (<= 5): {'seen' if v['control-idle-past'] else 'VOID'}")
    share = past(rows, "cascade-a")
    full = pct(work(frames, "cascade-a"), 95)
    missed = 100 * sum(1 for x in intervals(frames, "cascade-a") if x >= 25) / max(1, len(intervals(frames, "cascade-a")))
    if v["cascade"] == "RED" or full > 16.7 or share > 20:
        c = "RED"
    elif v["cascade"] == "GREEN" and missed <= 5 and share <= 5:
        c = "GREEN"
    else:
        c = "GREY"
    v["cascade-device"] = c
    main = pct(main_thread(rows, "cascade-a"), 95)
    out.append("")
    out.append(f"**Cascade, device**: work p95 {full:.2f}, main thread p95 {main:.2f}, {missed:.1f} % of intervals >= 25 ms, {share:.1f} % past deadline: {c}")
    for d in ("1", "2", "2.75"):
        roll = pct(main_thread(rows, f"roll-{d}", restarts=False), 95)
        whole = pct(main_thread(rows, f"roll-{d}"), 95)
        base = pct(main_thread(rows, f"base-{d}"), 95)
        frame = roll - base
        verdict = "GREEN" if frame <= 2 else "RED"
        v[f"raskat-main-{d}"] = verdict
        t = on_time(past(rows, f"roll-{d}"))
        v[f"ontime-{d}"] = t
        out.append(
            f"**Raskat at {d}, main thread**: roll p95 {roll:.2f} less base p95 {base:.2f} = {frame:.2f} ms against 2: {verdict} "
            f"(with restarts {whole - base:.2f}); on time, {past(rows, f'roll-{d}'):.1f} % past: {t}",
        )
    return v, out


def blind_report(frames, rows):
    """B-06: `nested-5` costs 5 ms inside a layer. The recorder is blind if `work` misses it where `draw` sees it."""
    v = {}
    out = []
    for arm in sorted({f[0] for f in frames}):
        draw = [r[3] for r in rows if r[0] == arm and r[1] > 0]
        out.append(f"{arm}: work p50 {pct(work(frames, arm), 50):.2f}, draw p50 {pct(draw, 50):.2f}")
    plain_w, plain_d = pct(work(frames, "plain"), 50), pct([r[3] for r in rows if r[0] == "plain" and r[1] > 0], 50)
    for arm in ("spin-5", "nested-5"):
        wk = pct(work(frames, arm), 50) - plain_w
        dr = pct([r[3] for r in rows if r[0] == arm and r[1] > 0], 50) - plain_d
        v[arm] = "seen" if wk >= 3 else "blind" if dr >= 3 else "void"
        out.append(f"{arm}: above plain, work {wk:.2f}, draw {dr:.2f}: the recorder is {v[arm]}")
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
    device_control()
    print("frame-report: each rule can fail on known input")


def known_metrics(cascade_past, roll_main, restart_main=20.0):
    """FrameMetrics whose answer is known: a roll with its restart frame every 21."""
    lines = ["arm,round,input_ns,animation_ns,layout_ns,draw_ns,sync_ns,command_ns,swap_ns,total_ns,gpu_ns,deadline_ns,first"]
    levels = {"idle-a": 1, "idle-b": 1, "busy-10": 11, "busy-25": 26, "cascade-a": 2, "cascade-b": 2, "cascade-flat": 2,
              "roll-1": roll_main, "roll-2": roll_main, "roll-2.75": roll_main, "base-1": 1, "base-2": 1, "base-2.75": 1}
    for rnd in range(4):
        for arm, level in levels.items():
            for i in range(210):
                # A restart and the frame after it, so that a reading with the restarts left in lands on one.
                main = restart_main if arm.startswith("roll") and i % RESTART_EVERY < 2 else level
                late = (arm == "busy-25") or (arm == "cascade-a" and i < cascade_past * 210 / 100)
                total = 40.0 if late else 12.0
                lines.append(f"{arm},{rnd},0,0,0,{int(main * 1e6)},0,0,0,{int(total * 1e6)},0,{int(33.3 * 1e6)},0")
    return lines


def device_control():
    good, _ = device_report(read(known(1, 8.0, 0.5)), read_metrics(known_metrics(0, 2.5)))
    assert good["cascade-device"] == "GREEN" and good["raskat-main-2.75"] == "GREEN" and good["ontime-2.75"] == "GREEN", good
    assert good["control-25-past"] and good["control-idle-past"], good
    # The restarts are apart: a frame of 2.5 with a restart of 20 is read at 2.5 less the base, and past the line is past it.
    assert pct(main_thread(read_metrics(known_metrics(0, 2.5)), "roll-1"), 95) == 20
    assert pct(main_thread(read_metrics(known_metrics(0, 2.5)), "roll-1", restarts=False), 95) == 2.5
    bad, _ = device_report(read(known(2, 8.0, 0.5)), read_metrics(known_metrics(50, 4.0)))
    assert bad["cascade-device"] == "RED" and bad["raskat-main-1"] == "RED", bad
    grey, _ = device_report(read(known(3, 8.0, 0.5)), read_metrics(known_metrics(10, 2.5)))
    assert grey["cascade-device"] == "GREY", grey
    # The recorder: seen, blind, and a control whose cost shows nowhere.
    def blind_log(arm, wk, dr):
        frames = [f"{arm},1,{i},16700000,{int(wk * 1e6)},0" for i in range(50)]
        rows = [f"{arm},1,0,0,0,{int(dr * 1e6)},0,0,0,12000000,0,33000000,0" for _ in range(50)]
        return frames, rows
    head = "arm,round,input_ns,animation_ns,layout_ns,draw_ns,sync_ns,command_ns,swap_ns,total_ns,gpu_ns,deadline_ns,first"
    logs = [blind_log("plain", 1, 0.5), blind_log("spin-5", 6, 5.5), blind_log("nested-5", 1, 5.5)]
    frames = read(sum((f for f, _ in logs), []))
    rows = read_metrics([head] + sum((r for _, r in logs), []))
    seen, _ = blind_report(frames, rows)
    assert seen == {"spin-5": "seen", "nested-5": "blind"}, seen
    logs = [blind_log("plain", 1, 0.5), blind_log("spin-5", 1, 0.5), blind_log("nested-5", 1, 0.5)]
    void, _ = blind_report(read(sum((f for f, _ in logs), [])), read_metrics([head] + sum((r for _, r in logs), [])))
    assert void == {"spin-5": "void", "nested-5": "void"}, void


if __name__ == "__main__":
    if sys.argv[1:] == ["--control"]:
        control()
    elif sys.argv[1] in ("--device", "--blind"):
        with open(sys.argv[2]) as f, open(sys.argv[3]) as g:
            read_frames, read_rows = read(f), read_metrics(g)
        _, lines = (device_report if sys.argv[1] == "--device" else blind_report)(read_frames, read_rows)
        print("\n".join(lines))
    else:
        with open(sys.argv[1]) as f:
            _, lines = report(read(f))
        print("\n".join(lines))
