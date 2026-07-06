package com.productivity.pdf.data

import com.productivity.pdf.model.PdfDocument

object SampleData {
    val documents = listOf(
        PdfDocument(1, "Q3 Financial Report", 24, "3.2 MB", "2h ago", isFavorite = true),
        PdfDocument(2, "Product Roadmap 2027", 12, "1.1 MB", "Yesterday"),
        PdfDocument(3, "Design System Guidelines", 48, "8.6 MB", "2 days ago", isFavorite = true),
        PdfDocument(4, "Employment Contract", 6, "412 KB", "Last week"),
        PdfDocument(5, "Research Paper Draft", 18, "2.0 MB", "Last week"),
        PdfDocument(6, "Meeting Notes - July", 3, "180 KB", "2 weeks ago"),
        PdfDocument(7, "Onboarding Handbook", 32, "5.4 MB", "3 weeks ago"),
        PdfDocument(8, "Invoice #4471", 2, "96 KB", "1 month ago")
    )
}
