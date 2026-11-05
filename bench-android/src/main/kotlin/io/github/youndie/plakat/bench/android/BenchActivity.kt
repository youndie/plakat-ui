package io.github.youndie.plakat.bench.android

import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatBottomSheet
import io.github.youndie.plakat.components.PlakatListItem
import io.github.youndie.plakat.components.PlakatMasthead
import io.github.youndie.plakat.components.PlakatNavigationBar
import io.github.youndie.plakat.components.PlakatNavigationDrawer
import io.github.youndie.plakat.components.PlakatNavigationItem
import io.github.youndie.plakat.components.PlakatProjection
import io.github.youndie.plakat.components.PlakatProjectionForm
import io.github.youndie.plakat.components.PlakatTopAppBar
import io.github.youndie.plakat.icons.PlakatIcons
import io.github.youndie.plakat.print.plakatPaper
import io.github.youndie.plakat.sample.PlakatUiSample
import io.github.youndie.plakat.sample.bench.BenchArm
import io.github.youndie.plakat.sample.bench.FrameBenchmark
import io.github.youndie.plakat.theme.PlakatTheme
import java.io.BufferedWriter
import java.io.File
import kotlin.time.TimeSource

private const val TAG = "PlakatBench"

private val forms =
    listOf(
        "proj-horn" to ("НОЯБРЬ" to PlakatProjectionForm.Horn(16.dp, 312.dp, 358.dp, 56.dp)),
        "proj-box" to ("НОВОЕ" to PlakatProjectionForm.Box(16.dp, 152.dp, 260.dp, 160.dp, 96.dp, 184.dp, 288.dp)),
    )

/** A restart of a form each so many frames, and how many restarts go before the 40 counted. */
private const val RESTART_EVERY = 30
private const val WARM = 20
private const val COUNTED = 40

private const val METRICS_HEADER =
    "arm,round,input_ns,animation_ns,layout_ns,draw_ns,sync_ns,command_ns,swap_ns,total_ns,gpu_ns,deadline_ns,first"

/** The instrument's control: rounds of 4 s an arm, and the known cost. */
private const val BLIND_ROUNDS = 4
private const val BLIND_SECONDS = 4
private const val SPIN_NS = 5_000_000L

/**
 * The host, by the intent's `mode`: `bench` runs [FrameBenchmark] over `rounds` of `seconds` a arm into `frames.csv` and
 * Android's own per-frame metrics into `metrics.csv`; `arm` shows one arm for the lever's screenshot; `projection` times a form's
 * first frame; `still` shows a form; `kit` the sample. It logs `DONE` when its files are closed.
 */
class BenchActivity : ComponentActivity() {
    @Volatile private var arm = "start"

    @Volatile private var round = -1

    private lateinit var frames: BufferedWriter
    private var metrics: BufferedWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // The brief's target is 60 fps, and the panel can do more.
        window.attributes = window.attributes.apply { preferredRefreshRate = 60f }
        val dir = getExternalFilesDir(null)!!
        val commit = intent.getStringExtra("commit") ?: "unknown"
        when (intent.getStringExtra("mode") ?: "bench") {
            "bench" -> bench(dir, commit)
            "arm" -> setContent { LeverArm(intent.getStringExtra("arm") ?: "cascade-a") }
            "projection" -> projection(dir, commit)
            "blind" -> blind(dir, commit)
            "still" -> setContent { Still(intent.getStringExtra("form") ?: "proj-horn") }
            "kit" -> setContent { PlakatUiSample() }
            "edges" -> setContent { Edges(intent.getStringExtra("which") ?: "bars") }
        }
    }

    private fun header(
        out: BufferedWriter,
        commit: String,
    ) {
        val shown = display
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val size = "${shown?.mode?.physicalWidth}x${shown?.mode?.physicalHeight}"
        out.appendLine(
            "# device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}",
        )
        out.appendLine("# build: ${Build.FINGERPRINT}")
        out.appendLine("# display: $size at ${shown?.refreshRate} Hz, density ${resources.displayMetrics.density}")
        out.appendLine("# cores: ${Runtime.getRuntime().availableProcessors()}, debuggable: $debuggable")
        out.appendLine("# commit: $commit")
    }

    private fun thermal() = getSystemService(PowerManager::class.java).currentThermalStatus

    private fun bench(
        dir: File,
        commit: String,
    ) {
        val rounds = intent.getIntExtra("rounds", 6)
        val seconds = intent.getIntExtra("seconds", 4)
        frames = File(dir, "frames.csv").bufferedWriter()
        header(frames, commit)
        frames.appendLine("# rounds: $rounds, seconds per arm: $seconds")
        frames.appendLine("arm,round,frame,interval_ns,work_ns,restart")
        val out = File(dir, "metrics.csv").bufferedWriter()
        metrics = out
        out.appendLine(METRICS_HEADER)
        val thread = HandlerThread("frame-metrics").apply { start() }
        val listener =
            Window.OnFrameMetricsAvailableListener { _, m, _ ->
                val line = metricsLine(m)
                synchronized(out) { metrics?.appendLine(line) }
            }
        window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper))
        setContent {
            FrameBenchmark(rounds, seconds, { line ->
                val first = line.indexOf(',')
                val second = line.indexOf(',', first + 1)
                val r = line.substring(first + 1, second).toInt()
                if (r != round) frames.appendLine("# round $r thermal ${thermal()}")
                arm = line.substring(0, first)
                round = r
                frames.appendLine(line)
            }) {
                window.removeOnFrameMetricsAvailableListener(listener)
                frames.appendLine("# end thermal ${thermal()}")
                frames.close()
                synchronized(out) {
                    out.close()
                    metrics = null
                }
                Log.i(TAG, "DONE")
                finish()
            }
        }
    }

    private fun metricsLine(m: FrameMetrics): String =
        "$arm,$round,${m.getMetric(
            FrameMetrics.INPUT_HANDLING_DURATION,
        )},${m.getMetric(FrameMetrics.ANIMATION_DURATION)}," +
            "${m.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION)},${m.getMetric(FrameMetrics.DRAW_DURATION)}," +
            "${m.getMetric(FrameMetrics.SYNC_DURATION)},${m.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)}," +
            "${m.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION)},${m.getMetric(FrameMetrics.TOTAL_DURATION)}," +
            "${m.getMetric(
                FrameMetrics.GPU_DURATION,
            )},${m.getMetric(FrameMetrics.DEADLINE)},${m.getMetric(FrameMetrics.FIRST_DRAW_FRAME)}"

    /**
     * The instrument's own control (B-06): a known cost inside a graphics layer's draw, which on Android is recorded after
     * the root's. `spin-5` must show in `draw` and, if the recorder cannot see a layer, not in `work`.
     */
    private fun blind(
        dir: File,
        commit: String,
    ) {
        frames = File(dir, "blind-frames.csv").bufferedWriter()
        header(frames, commit)
        frames.appendLine("arm,round,frame,interval_ns,work_ns,restart")
        val out = File(dir, "blind-metrics.csv").bufferedWriter()
        metrics = out
        out.appendLine(METRICS_HEADER)
        val thread = HandlerThread("frame-metrics").apply { start() }
        val listener =
            Window.OnFrameMetricsAvailableListener { _, m, _ ->
                val line = metricsLine(m)
                synchronized(out) { metrics?.appendLine(line) }
            }
        window.addOnFrameMetricsAvailableListener(listener, Handler(thread.looper))
        setContent {
            var at by remember { mutableIntStateOf(0) }
            val plan =
                remember {
                    (0 until BLIND_ROUNDS).flatMap { r -> listOf(r to "plain", r to "spin-5", r to "nested-5") }
                }
            if (at >= plan.size) {
                LaunchedEffect(Unit) {
                    window.removeOnFrameMetricsAvailableListener(listener)
                    frames.close()
                    synchronized(out) {
                        out.close()
                        metrics = null
                    }
                    Log.i(TAG, "DONE")
                    finish()
                }
            } else {
                val (r, name) = plan[at]
                key(at) {
                    LayerArm(name, { frame, interval, work ->
                        arm = name
                        round = r
                        frames.appendLine("$name,$r,$frame,$interval,$work,0")
                    }) { at++ }
                }
            }
        }
    }

    private fun projection(
        dir: File,
        commit: String,
    ) {
        frames = File(dir, "projection.csv").bufferedWriter()
        header(frames, commit)
        frames.appendLine("form,restart,work_ns,kept")
        setContent {
            var at by remember { mutableIntStateOf(0) }
            if (at >= forms.size) {
                LaunchedEffect(Unit) {
                    frames.appendLine("# end thermal ${thermal()}")
                    frames.close()
                    Log.i(TAG, "DONE")
                    finish()
                }
            } else {
                val (name, form) = forms[at]
                key(at) {
                    FirstFrames(name, form.first, form.second, { line -> frames.appendLine(line) }) { at++ }
                }
            }
        }
    }
}

/**
 * A layer that costs 5 ms when it is drawn, under a recorder that ends as `FrameBenchmark`'s does: `spin-5` with the ticks read
 * in it, directly under what the recorder draws in, and `nested-5` inside a second layer that nothing invalidates.
 */
@Composable
private fun LayerArm(
    name: String,
    log: (Int, Long, Long) -> Unit,
    onEnd: () -> Unit,
) {
    var tick by remember { mutableLongStateOf(0L) }
    val stamp = remember { LongArray(2) }
    LaunchedEffect(Unit) {
        val begun = System.nanoTime()
        var frames = 0
        while (System.nanoTime() - begun < BLIND_SECONDS * 1_000_000_000L) {
            withFrameNanos {
                frames++
                val now = System.nanoTime()
                stamp[1] = if (stamp[0] == 0L) 0L else now - stamp[0]
                stamp[0] = now
                tick = frames.toLong()
            }
        }
        onEnd()
    }
    PlakatTheme {
        Box(Modifier.fillMaxSize().plakatPaper()) {
            val spin = {
                val until = System.nanoTime() + SPIN_NS
                while (System.nanoTime() < until) {
                    // The control: the layer's cost, by the clock.
                }
            }
            if (name == "nested-5") {
                Box(Modifier.fillMaxSize().graphicsLayer { }) {
                    Box(
                        Modifier.fillMaxSize().graphicsLayer { }.drawBehind {
                            tick
                            spin()
                        },
                    )
                }
            } else {
                Box(
                    Modifier.fillMaxSize().graphicsLayer { }.drawBehind {
                        tick
                        if (name == "spin-5") spin()
                    },
                )
            }
            Spacer(
                Modifier.fillMaxSize().drawBehind {
                    log(tick.toInt(), stamp[1], System.nanoTime() - stamp[0])
                },
            )
        }
    }
}

/** A form's first frame: a new composition of it, whose picture is made when it is drawn, timed to the end of that draw. */
@Composable
private fun FirstFrames(
    name: String,
    word: String,
    form: PlakatProjectionForm,
    log: (String) -> Unit,
    onEnd: () -> Unit,
) {
    var generation by remember { mutableIntStateOf(0) }
    var tick by remember { mutableLongStateOf(0L) }
    val stamp = remember { LongArray(2) }
    LaunchedEffect(Unit) {
        var frames = 0
        while (generation <= WARM + COUNTED) {
            withFrameNanos {
                frames++
                stamp[0] = System.nanoTime()
                // Marks this frame as the restart's, for the draw below to close.
                if (frames % RESTART_EVERY == 0) {
                    generation++
                    stamp[1] = 1L
                }
                tick = frames.toLong()
            }
        }
        onEnd()
    }
    PlakatTheme {
        Box(Modifier.fillMaxSize().plakatPaper()) {
            key(generation) {
                PlakatProjection(listOf(word), form, Modifier.size(390.dp, 320.dp), print = false)
            }
            Spacer(
                Modifier.fillMaxSize().drawBehind {
                    tick
                    if (stamp[1] == 1L) {
                        stamp[1] = 0L
                        val kept = if (generation > WARM) 1 else 0
                        log("$name,$generation,${System.nanoTime() - stamp[0]},$kept")
                    }
                },
            )
        }
    }
}

@Composable
private fun Still(name: String) {
    val (word, form) = forms.first { it.first == name }.second
    PlakatTheme {
        Box(Modifier.fillMaxSize().plakatPaper()) {
            PlakatProjection(listOf(word), form, Modifier.size(390.dp, 320.dp), print = false)
        }
    }
}

/** The components that own an edge of the screen, on the whole screen, for a look at what stands under a system's bar (B-54). */
@Composable
private fun Edges(which: String) {
    val items =
        listOf(
            PlakatNavigationItem(PlakatIcons.Home, "ЛЕНТА"),
            PlakatNavigationItem(PlakatIcons.Search, "ПОИСК"),
            PlakatNavigationItem(PlakatIcons.Settings, "ОПЦИИ"),
        )
    PlakatTheme {
        Box(Modifier.fillMaxSize().plakatPaper()) {
            Column(Modifier.fillMaxSize()) {
                if (which == "masthead") {
                    // Not the page's «ЛИСТ»: on this phone's width it stretches under 1.5, and throws (B-55).
                    PlakatMasthead("ВЫПУСК", startLabel = "№ 47", endLabel = "НОЯБРЬ")
                } else {
                    PlakatTopAppBar("ЛИСТ", subtitle = "№ 47")
                }
                Spacer(Modifier.weight(1f))
                PlakatNavigationBar(items, 0, {})
            }
            when (which) {
                "sheet" -> {
                    PlakatBottomSheet(true, {}) {
                        PlakatListItem("Сохранить")
                        PlakatListItem("Поделиться", divider = false)
                    }
                }

                "drawer" -> {
                    PlakatNavigationDrawer(true, items, 0, {}, {})
                }
            }
        }
    }
}

/** One arm for as long as the screenshot takes, restarting as it does in a run; no number is kept from it. */
@Composable
private fun LeverArm(id: String) {
    val arm = BenchArm.entries.first { it.id == id }
    FrameBenchmark(1000, 4, {}, only = arm) {}
}
