package com.productivity.pdf.data

import androidx.compose.runtime.mutableStateListOf
import com.productivity.pdf.model.RecentPdf

/**
 * Session-only "recently opened" list. Starts empty — no demo/sample data.
 * Populated only when the user actually opens a real PDF (via the Library's
 * + button or via "Open with" from another app).
 *
 * This resets when the process dies. See README "Next steps" for persisting
 * it with Room if you want it to survive app restarts.
 */
object RecentPdfsStore {
    val items = mutableStateListOf<RecentPdf>()

    fun addOrBumpToTop(pdf: RecentPdf) {
        items.removeAll { it.uri == pdf.uri }
        items.add(0, pdf)
    }
}
