package com.productivity.pdf.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.productivity.pdf.data.RecentPdfsStore
import com.productivity.pdf.model.RecentPdf
import com.productivity.pdf.ui.components.AnnotationToolbar
import com.productivity.pdf.ui.components.PasswordPromptDialog
import com.productivity.pdf.ui.components.TranslucentTopBar
import com.productivity.pdf.util.PdfFileUtils

/**
 * Real PDF viewer backed by Pdfium (android-pdf-viewer). Handles:
 *  - normal PDFs: opens immediately
 *  - password-protected PDFs: catches the load error, prompts for a password,
 *    retries with it, and shows "incorrect password" if it fails again.
 *
 * IMPORTANT: the actual `.load()` call lives inside a single LaunchedEffect keyed
 * on (uri, password, loadAttempt) — NOT inside AndroidView's `update` block.
 * Putting it in `update` was the earlier bug: `update` re-runs on every
 * recomposition, and since the load itself flips `isLoading` (which this
 * composable reads), that created a reload loop that cancelled the in-flight
 * render and surfaced as "Couldn't open this PDF" for every file, password or not.
 */
@Composable
fun PdfViewerScreen(
    uri: Uri,
    fileName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var password by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isRetry by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var loadedPageCount by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Bumped when the user submits a password, to guarantee the effect re-fires
    // even if they retype the exact same (still-wrong) password.
    var loadAttempt by remember { mutableIntStateOf(0) }

    val pdfViewHolder = remember { mutableStateOf<PDFView?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = { TranslucentTopBar(title = fileName, onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PDFView(ctx, null).also { pdfViewHolder.value = it }
                }
            )

            if (isLoading && !showPasswordDialog) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            if (!isLoading && loadedPageCount == 0 && !showPasswordDialog) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage?.let { "Couldn't open this PDF.\n$it" }
                            ?: "Couldn't open this PDF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!isLoading && loadedPageCount > 0) {
                AnnotationToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }
        }
    }

    LaunchedEffect(uri, password, loadAttempt) {
        val pdfView = pdfViewHolder.value ?: return@LaunchedEffect
        isLoading = true
        errorMessage = null
        pdfView.fromUri(uri)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .password(password)
            .spacing(8)
            .onLoad { pageCount ->
                isLoading = false
                loadedPageCount = pageCount
                showPasswordDialog = false
                RecentPdfsStore.addOrBumpToTop(
                    RecentPdf(
                        uri = uri,
                        name = fileName,
                        sizeLabel = PdfFileUtils.querySizeLabel(context, uri),
                        openedAt = "Just now"
                    )
                )
            }
            .onError { throwable ->
                isLoading = false
                val isPasswordError =
                    throwable.javaClass.simpleName.contains("Password", ignoreCase = true) ||
                        (throwable.message?.contains("password", ignoreCase = true) == true)
                if (isPasswordError) {
                    isRetry = password != null
                    showPasswordDialog = true
                } else {
                    errorMessage = throwable.message
                }
            }
            .load()
    }

    if (showPasswordDialog) {
        PasswordPromptDialog(
            isRetry = isRetry,
            onDismiss = {
                showPasswordDialog = false
                onBack()
            },
            onSubmit = { entered ->
                password = entered
                loadAttempt++
            }
        )
    }
}
