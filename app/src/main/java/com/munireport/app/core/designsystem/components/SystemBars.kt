package com.munireport.app.core.designsystem.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Keeps the status and navigation bar icons readable for the header currently
 * on screen. Navy headers use light icons; light headers use dark icons.
 */
@Composable
fun SystemBarIcons(useDarkStatusIcons: Boolean) {
    val view = LocalView.current
    val activity = remember(view) { findActivity(view.context) }
    if (activity == null) return

    DisposableEffect(activity, useDarkStatusIcons) {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = useDarkStatusIcons
        controller.isAppearanceLightNavigationBars = true
        onDispose { }
    }
}

private fun findActivity(context: Context): Activity? {
    var current: Context = context
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}
