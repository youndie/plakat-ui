package io.github.youndie.plakat.theme

/**
 * How close together a component stands its rows, for the hand that drives the surface (B-82).
 *
 * [Touch] is the design's: a row that is a button is a touch target of 48 dp. [Pointer] is for a surface driven by a mouse
 * or a trackpad, where a row of 36 dp is reached as surely and more of them stand in a window; under a finger it is a
 * target short of the 48 the kit holds to. The design draws no pointer size, so [Pointer] is designed here (research D13).
 *
 * Today [io.github.youndie.plakat.components.PlakatTable] takes it as a parameter; nothing in the theme reads it.
 */
public enum class PlakatDensity {
    /** The design's sizes: a row is 48 dp at the least. */
    Touch,

    /** A pointer surface: a row is 36 dp at the least. */
    Pointer,
}
