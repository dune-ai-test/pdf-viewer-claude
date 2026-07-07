package com.productivity.pdf.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlin.math.roundToInt

object PdfFileUtils {

    /** Returns the real file name (falls back to the last path segment / "Document.pdf"). */
    fun queryDisplayName(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex) ?: fallbackName(uri)
                }
            }
        }
        return fallbackName(uri)
    }

    /** Returns a human-readable file size (e.g. "3.2 MB"), or "" if unavailable. */
    fun querySizeLabel(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                    return formatBytes(cursor.getLong(sizeIndex))
                }
            }
        }
        return ""
    }

    private fun fallbackName(uri: Uri): String =
        uri.lastPathSegment?.substringAfterLast('/') ?: "Document.pdf"

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return ""
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> "${(mb * 10).roundToInt() / 10.0} MB"
            kb >= 1.0 -> "${(kb * 10).roundToInt() / 10.0} KB"
            else -> "$bytes B"
        }
    }
}
