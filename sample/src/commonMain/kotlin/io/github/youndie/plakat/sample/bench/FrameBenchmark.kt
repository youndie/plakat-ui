package io.github.youndie.plakat.sample.bench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.youndie.plakat.agit.PlakatGeneratedImage
import io.github.youndie.plakat.components.PlakatCard
import io.github.youndie.plakat.components.PlakatPoster
import io.github.youndie.plakat.print.plakatPaper
import io.github.youndie.plakat.theme.PlakatPrintSwitches
import io.github.youndie.plakat.theme.PlakatTheme
import kotlin.time.TimeSource

// B-51, and B-06 unchanged on the device: frame times from the running screen, over interleaved arms. The protocol and
// its thresholds are in docs/backlog/B-51-frame-cost-on-desktop.md, and were committed before the first number.

/** What an arm shows, and what it costs on purpose. */
enum class BenchArm(
    val id: String,
    val busyMs: Int = 0,
    val density: Float? = null,
    val rolls: Boolean = false,
    val cards: Boolean = false,
    val print: Boolean = true,
) {
    IdleA("idle-a"),
    IdleB("idle-b"),
    Busy10("busy-10", busyMs = 10),
    Busy25("busy-25", busyMs = 25),
    CascadeA("cascade-a", cards = true),
    CascadeB("cascade-b", cards = true),
    CascadeFlat("cascade-flat", cards = true, print = false),
    Roll1("roll-1", density = 1f, rolls = true),
    Roll2("roll-2", density = 2f, rolls = true),
    Roll275("roll-2.75", density = 2.75f, rolls = true),
    Base1("base-1", density = 1f),
    Base2("base-2", density = 2f),
    Base275("base-2.75", density = 2.75f),
}

/** The arms of round [round], rotated by one each round so that none always follows the same other. */
fun armsOfRound(round: Int): List<BenchArm> {
    val all = BenchArm.entries
    val by = round % all.size
    return all.drop(by) + all.take(by)
}

/** A cascade restarts every half second, as often as its ten cards can finish; a roll every 21 frames, its 320 ms. */
private const val CASCADE_EVERY_FRAMES = 30
private const val ROLL_EVERY_FRAMES = 21
private const val POSTER_SEED = 1919

/**
 * Runs every arm of every round in turn for [armSeconds] and hands each frame to [onFrame] as `arm,round,frame,interval
 * ns,work ns,restart`, then calls [onDone]. Nothing here is the kit's: the kit is what it measures, through its public API.
 */
@Composable
fun FrameBenchmark(
    rounds: Int,
    armSeconds: Int,
    onFrame: (String) -> Unit,
    onDone: () -> Unit,
) {
    val plan = remember(rounds) { (0 until rounds).flatMap { r -> armsOfRound(r).map { r to it } } }
    var at by remember { mutableIntStateOf(0) }
    if (at >= plan.size) {
        LaunchedEffect(Unit) { onDone() }
        return
    }
    val (round, arm) = plan[at]
    key(at) {
        Arm(arm, armSeconds, { interval, work, frame, restart ->
            onFrame("${arm.id},$round,$frame,$interval,$work,${if (restart) 1 else 0}")
        }) { at++ }
    }
}

@Composable
private fun Arm(
    arm: BenchArm,
    armSeconds: Int,
    onFrame: (Long, Long, Int, Boolean) -> Unit,
    onEnd: () -> Unit,
) {
    var tick by remember { mutableLongStateOf(0L) }
    var generation by remember { mutableIntStateOf(0) }
    val clock = remember { Stamps() }
    val every =
        when {
            arm.cards -> CASCADE_EVERY_FRAMES
            arm.rolls -> ROLL_EVERY_FRAMES
            else -> 0
        }
    LaunchedEffect(arm) {
        val begun = TimeSource.Monotonic.markNow()
        var frames = 0
        while (begun.elapsedNow().inWholeSeconds < armSeconds) {
            withFrameNanos {
                frames++
                clock.begin(frames, every > 0 && frames % every == 0)
                if (arm.busyMs > 0) {
                    val spin = TimeSource.Monotonic.markNow()
                    while (spin.elapsedNow().inWholeMilliseconds < arm.busyMs) {
                        // The control: the frame's cost, by the clock.
                    }
                }
                if (every > 0 && frames % every == 0) generation++
                tick = frames.toLong()
            }
        }
        onEnd()
    }
    PlakatTheme(print = PlakatPrintSwitches(texture = arm.print, registration = arm.print)) {
        Box(Modifier.fillMaxSize().plakatPaper().testTag(arm.id)) {
            key(generation) {
                when {
                    arm.cards -> {
                        Cards()
                    }

                    arm.density != null -> {
                        CompositionLocalProvider(LocalDensity provides Density(arm.density)) {
                            PlakatPoster(
                                listOf("ЧИТАЙ", "ВЕСЬ", "НОЯБРЬ"),
                                "ПОДПИСАТЬСЯ",
                                {},
                                body = "12 новых выпусков.",
                                seed = POSTER_SEED,
                                raskat = true,
                                print = arm.rolls,
                            )
                        }
                    }
                }
            }
            // Drawn last, over everything: the frame's work ends here. It reads the tick, so every frame draws.
            Spacer(
                Modifier.fillMaxSize().drawBehind {
                    tick
                    clock.end(onFrame)
                },
            )
        }
    }
}

/** Ten cards printing in a cascade, on the paper, with their thumbnails. */
@Composable
private fun Cards() {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(10) { i ->
            PlakatCard({}, Modifier.fillMaxWidth(), printOrder = i) {
                PlakatGeneratedImage(71 + i, Modifier.size(40.dp), frameWidth = 64.dp, frameHeight = 64.dp)
                BasicText(
                    "Город из фанеры: как строят павильоны",
                    style =
                        PlakatTheme.typography.bodyS.style
                            .copy(color = PlakatTheme.colors.ink),
                )
            }
        }
    }
}

/** A frame's stamps. A plain class: the recorder must not itself invalidate what it measures. */
private class Stamps {
    private var start = TimeSource.Monotonic.markNow()
    private var last: TimeSource.Monotonic.ValueTimeMark? = null
    private var interval = 0L
    private var frame = 0
    private var restart = false
    private var open = false

    fun begin(
        frame: Int,
        restart: Boolean,
    ) {
        val now = TimeSource.Monotonic.markNow()
        interval = last?.let { (now - it).inWholeNanoseconds } ?: 0L
        last = now
        start = now
        this.frame = frame
        this.restart = restart
        open = true
    }

    fun end(onFrame: (Long, Long, Int, Boolean) -> Unit) {
        if (!open) return
        open = false
        onFrame(interval, start.elapsedNow().inWholeNanoseconds, frame, restart)
    }
}
