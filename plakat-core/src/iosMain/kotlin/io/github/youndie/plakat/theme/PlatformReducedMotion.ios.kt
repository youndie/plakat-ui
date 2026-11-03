package io.github.youndie.plakat.theme

import androidx.compose.runtime.Composable
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@Composable
internal actual fun platformReducedMotion(): Boolean = UIAccessibilityIsReduceMotionEnabled()
