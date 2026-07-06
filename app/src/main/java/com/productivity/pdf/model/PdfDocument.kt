package com.productivity.pdf.model

data class PdfDocument(
    val id: Int,
    val title: String,
    val pageCount: Int,
    val sizeLabel: String,
    val lastOpened: String,
    val isFavorite: Boolean = false
)
