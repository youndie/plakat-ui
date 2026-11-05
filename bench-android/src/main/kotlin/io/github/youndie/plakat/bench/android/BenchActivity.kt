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
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.components.PlakatProjection
import io.github.youndie.plakat.components.PlakatProjectionForm
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

/** Before API 31 a frame has no deadline of its own, and the protocol's is a vsync at 60 Hz. */
private const val NO_DEADLINE_NS = 16_666_667L

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
            "still" -> setContent { Still(intent.getStringExtra("form") ?: "proj-horn") }
            "kit" -> setContent { PlakatUiSample() }
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
        out.appendLine("arm,round,total_ns,gpu_ns,deadline_ns,first")
        val thread = HandlerThread("frame-metrics").apply { start() }
        val listener =
            Window.OnFrameMetricsAvailableListener { _, m, _ ->
                val line =
                    "$arm,$round,${m.getMetric(FrameMetrics.TOTAL_DURATION)},${gpu(m)},${deadline(m)}," +
                        "${m.getMetric(FrameMetrics.FIRST_DRAW_FRAME)}"
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

    private fun gpu(m: FrameMetrics) = if (Build.VERSION.SDK_INT >= 31) m.getMetric(FrameMetrics.GPU_DURATION) else -1L

    private fun deadline(m: FrameMetrics): Long =
        if (Build.VERSION.SDK_INT >= 31) m.getMetric(FrameMetrics.DEADLINE) else NO_DEADLINE_NS

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

/** One arm for as long as the screenshot takes, restarting as it does in a run; no number is kept from it. */
@Composable
private fun LeverArm(id: String) {
    val arm = BenchArm.entries.first { it.id == id }
    FrameBenchmark(1000, 4, {}, only = arm) {}
}
