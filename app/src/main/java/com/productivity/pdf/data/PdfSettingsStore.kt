package com.productivity.pdf.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Persisted PDF viewing preferences: page background (gap) color and night
 * mode. Backed by SharedPreferences so choices survive app restarts, and
 * exposed as Compose `State` so any screen reading them recomposes
 * automatically when changed from the Settings screen.
 *
 * Call [init] once at app startup (see MainActivity) before any composable
 * reads these values, so they reflect the saved choice instead of defaults.
 */
object PdfSettingsStore {
    private const val PREFS_NAME = "pdf_settings"
    private const val KEY_BG_COLOR = "bg_color"
    private const val KEY_NIGHT_MODE = "night_mode"

    /** Preset background colors, taken from design.md's own neutral tokens. */
    object BackgroundPresets {
        const val DESIGN_DEFAULT = 0xFFF4F3F8.toInt() // surface-container-low
        const val WHITE = 0xFFFFFFFF.toInt()
        const val LIGHT_GRAY = 0xFFDAD9DF.toInt() // surface-dim
        const val DARK_GRAY = 0xFF2F3034.toInt() // inverse-surface
        const val BLACK = 0xFF000000.toInt()

        val all = listOf(DESIGN_DEFAULT, WHITE, LIGHT_GRAY, DARK_GRAY, BLACK)
    }

    var backgroundColor by mutableIntStateOf(BackgroundPresets.DESIGN_DEFAULT)
        private set

    var nightMode by mutableStateOf(false)
        private set

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val prefs = prefs(context)
        backgroundColor = prefs.getInt(KEY_BG_COLOR, BackgroundPresets.DESIGN_DEFAULT)
        nightMode = prefs.getBoolean(KEY_NIGHT_MODE, false)
    }

    fun setBackgroundColor(context: Context, color: Int) {
        backgroundColor = color
        prefs(context).edit().putInt(KEY_BG_COLOR, color).apply()
    }

    fun setNightMode(context: Context, enabled: Boolean) {
        nightMode = enabled
        prefs(context).edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
