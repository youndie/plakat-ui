package io.github.youndie.plakat.sample.bench

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.GraphicsEnvironment
import java.io.File
import kotlin.system.exitProcess

/**
 * `./gradlew :sample:frameBench --args="<rounds> <seconds per arm> <out file> <commit>"`: the frame benchmark in a real
 * window, on top. It needs the display awake and the machine otherwise left alone.
 */
fun main(args: Array<String>) {
    val rounds = args[0].toInt()
    val seconds = args[1].toInt()
    val out = File(args[2])
    val commit = args[3]
    val mode = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode
    val writer = out.bufferedWriter()

    fun say(line: String) = writer.appendLine(line)
    say(
        "# machine: ${System.getProperty(
            "os.name",
        )} ${System.getProperty(
            "os.version",
        )} ${System.getProperty("os.arch")}, ${Runtime.getRuntime().availableProcessors()} cores",
    )
    say("# jvm: ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")}")
    say("# display: ${mode.width}x${mode.height} at ${mode.refreshRate} Hz")
    say("# commit: $commit")
    say("# rounds: $rounds, seconds per arm: $seconds")
    say("arm,round,frame,interval_ns,work_ns,restart")
    application {
        Window(
            ::exitApplication,
            rememberWindowState(size = DpSize(560.dp, 760.dp)),
            title = "plakat-ui frame benchmark",
            alwaysOnTop = true,
            resizable = false,
        ) {
            FrameBenchmark(rounds, seconds, ::say) {
                writer.close()
                exitProcess(0)
            }
        }
    }
}
