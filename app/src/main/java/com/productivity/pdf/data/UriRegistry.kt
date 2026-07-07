package com.productivity.pdf.data

import android.net.Uri

/**
 * Holds real `Uri` objects (plus where they came from) keyed by a plain int,
 * so they can be handed between composables/navigation destinations without
 * ever being serialized to a string and parsed back (see the comment history
 * in NavGraph.kt for why that mattered for SAF permission grants).
 */
data class RegisteredUri(
    val uri: Uri,
    /** True only for PDFs opened via the Library's own "+" Add button. */
    val addToRecents: Boolean
)

object UriRegistry {
    private val entries = mutableMapOf<Int, RegisteredUri>()
    private var nextId = 0

    fun register(uri: Uri, addToRecents: Boolean): Int {
        val id = nextId++
        entries[id] = RegisteredUri(uri, addToRecents)
        return id
    }

    fun get(id: Int): RegisteredUri? = entries[id]
}
