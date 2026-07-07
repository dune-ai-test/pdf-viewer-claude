package com.productivity.pdf.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.roundToInt

object PdfFileUtils {

    private const val CACHE_PREFIX = "opened_"
    private const val CACHE_SUFFIX = ".pdf"

    /** Returns the real file name (falls back to the last path segment / "Document.pdf"). */
    fun queryDisplayName(context: Context, uri: Uri): String {
        if (uri.scheme == "file") {
            return uri.lastPathSegment?.substringAfterLast('/') ?: fallbackName(uri)
        }
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
        if (uri.scheme == "file") {
            val path = uri.path ?: return ""
            val file = File(path)
            return if (file.exists()) formatBytes(file.length()) else ""
        }
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

    /**
     * Reads the PDF's bytes into our own cache dir and returns that local File.
     * The caller is responsible for deleting it once the PDF is closed —
     * `PdfViewerScreen` does this via `DisposableEffect`, so cache usage stays
     * at "at most the PDFs currently open", not "every PDF ever opened".
     * `clearCachedPdfs` below is a startup safety net for the case where that
     * cleanup never ran (e.g. the app was force-killed).
     *
     * Why read it ourselves at all: `android-pdf-viewer`'s built-in `.fromUri()`
     * reader doesn't reliably carry over SAF read grants (causing "Permission
     * Denial ... requires ACTION_OPEN_DOCUMENT" even right after picking a
     * file), and it can't read `file://` URIs at all (ContentResolver only
     * routes `content://` — a raw `file://` surfaces as "No content
     * provider"). Reading the bytes ourselves — via ContentResolver for
     * `content://` and a plain FileInputStream for `file://` — then handing
     * Pdfium a local `java.io.File` via `.fromFile()` sidesteps both problems.
     *
     * Call this from a background thread/coroutine (it does blocking I/O).
     * Returns null if the source couldn't be read at all (deleted, moved, no
     * permission from the source app, etc).
     */
    fun copyToCache(context: Context, uri: Uri): File? {
        val cacheFile = File(context.cacheDir, "$CACHE_PREFIX${System.currentTimeMillis()}$CACHE_SUFFIX")
        return runCatching {
            val input = when (uri.scheme) {
                "file" -> {
                    val path = uri.path ?: return@runCatching null
                    FileInputStream(path)
                }
                else -> context.contentResolver.openInputStream(uri) ?: return@runCatching null
            }
            input.use { inStream ->
                FileOutputStream(cacheFile).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
            cacheFile
        }.getOrNull()
    }

    /**
     * Deletes any leftover cached PDF copies. Call this once at app startup
     * (see MainActivity) as a safety net — normal open/close cycles clean up
     * after themselves, this only catches cases where that didn't happen
     * (process killed, crash, etc), so cache size never grows unbounded.
     */
    fun clearCachedPdfs(context: Context) {
        context.cacheDir
            .listFiles { file -> file.name.startsWith(CACHE_PREFIX) && file.name.endsWith(CACHE_SUFFIX) }
            ?.forEach { it.delete() }
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
