package com.productivity.pdf.data

import android.net.Uri

/**
 * Holds real `Uri` objects keyed by a plain int, so they can be handed between
 * composables/navigation destinations without ever being serialized to a
 * string and parsed back.
 *
 * Why this exists: the previous approach passed the Uri as a percent-encoded
 * Navigation route argument (`Uri.encode(uri.toString())` → route arg →
 * `Uri.decode()` → `Uri.parse()`). Navigation Compose's own route matching
 * also decodes path segments, so that was a double-decode — it reconstructed
 * a Uri whose string no longer exactly matched the one the system actually
 * granted read permission for, so every read against it failed (regardless of
 * scheme), even after switching to reading bytes ourselves. Routing the exact
 * same Uri object through this registry sidesteps that entirely.
 */
object UriRegistry {
    private val entries = mutableMapOf<Int, Uri>()
    private var nextId = 0

    fun register(uri: Uri): Int {
        val id = nextId++
        entries[id] = uri
        return id
    }

    fun get(id: Int): Uri? = entries[id]
}
