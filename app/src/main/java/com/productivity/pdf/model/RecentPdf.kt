package com.productivity.pdf.model

import android.net.Uri

data class RecentPdf(
    val uri: Uri,
    val name: String,
    val sizeLabel: String,
    val openedAt: String
)
