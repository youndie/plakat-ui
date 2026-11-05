package io.github.youndie.plakat.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * The named moments of the kit's motion (`plakat/motion`): every animation reports its event once per
 * trigger, so that a sound can be hung on it later (brief: "под них потом подвесим звук"). The kit
 * itself plays no sound. [token] is the name the spec rows use.
 *
 * Reported whether or not motion is reduced: the trigger happened either way, only its picture changed.
 */
public enum class PlakatEvent(
    public val token: String,
) {
    /** Button, IconButton, List item press; Chip select: inversion in one frame. */
    Stamp("stamp"),

    /** Button release: the inks and the shift return. */
    Release("release"),

    /** Button loading: the label wiped off, the sector turning. */
    Engage("engage"),

    /** Checkbox check: the wedge enters from the top-right corner. */
    WedgeIn("wedge-in"),

    /** Checkbox uncheck: the wedge leaves the same way. */
    WedgeOut("wedge-out"),

    /** Switch toggle: the carriage runs along its rail. */
    Lever("lever"),

    /** TextField focus: the rule grows into a bar. */
    Underline("underline"),

    /** TextField error: the hatch wipes in, the field jolts 2 dp. */
    Hatch("hatch"),

    /** Card and Poster appear: printed by a diagonal wipe. */
    Print("print"),

    /** Card dismiss: it leaves along the rail. */
    Tear("tear"),

    /** Snackbar show and hide. */
    Telegram("telegram"),

    /** Dialog open and close. */
    Announce("announce"),

    /** RadioButton and NavigationBar select. */
    Lock("lock"),

    /**
     * FAB appear, Tabs switch, Window next panel. Designed here: a segmented button's fill (B-49), a bottom sheet and a
     * drawer riding (B-46, B-47).
     */
    Rail("rail"),

    /** Counter and Badge value: one step of the digits. */
    Tick("tick"),

    /** TopAppBar scroll: the rapport and title fold away. */
    Fold("fold"),

    /** Screen push and pop: the red bar crosses on the diagonal. A menu's or tooltip's wipe, designed here (B-45, B-48). */
    Wipe("wipe"),

    /** Raskat appear: the second ink rolls in to the seam. */
    Roll("roll"),

    /** Slider: the carriage stops on a tick. */
    Notch("notch"),
}

/** Receives the kit's [PlakatEvent]s, on the main thread, as they happen. */
public fun interface PlakatEventListener {
    public fun onEvent(event: PlakatEvent)

    public companion object {
        /** Hears nothing: the default. */
        public val Silent: PlakatEventListener = PlakatEventListener { }
    }
}

/**
 * Reports [event] when [trigger] changes, once per change, and on the first composition too when
 * [initial] (an appearance). For animations started by state; one started by an input reports from
 * its handler through [PlakatTheme.events] instead.
 */
@Composable
internal fun ReportOnChange(
    trigger: Any?,
    event: PlakatEvent,
    initial: Boolean = false,
) {
    val listener by rememberUpdatedState(PlakatTheme.events)
    val first = remember { FirstSeen() }
    LaunchedEffect(trigger) {
        if (!first.consume() || initial) listener.onEvent(event)
    }
}

private class FirstSeen {
    private var seen = false

    /** True the first time only. */
    fun consume(): Boolean = !seen.also { seen = true }
}
