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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.productivity.pdf.ui.components.AnnotationToolbar
import com.productivity.pdf.ui.components.PasswordPromptDialog
import com.productivity.pdf.ui.components.TranslucentTopBar

/**
 * Real PDF viewer backed by Pdfium (android-pdf-viewer). Handles:
 *  - normal PDFs: opens immediately
 *  - password-protected PDFs: catches the load error, prompts for a password,
 *    retries with it, and shows "incorrect password" if it fails again.
 */
@Composable
fun PdfViewerScreen(
    uri: Uri,
    fileName: String,
    onBack: () -> Unit
) {
    var password by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isRetry by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var loadedPageCount by remember { mutableIntStateOf(0) }
    // Bumps whenever we need PDFView's `update` block to run .load() again
    // (e.g. right after the user submits a password).
    var loadKey by remember { mutableIntStateOf(0) }

    val currentUri = rememberUpdatedState(uri)
    val currentPassword = rememberUpdatedState(password)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = { TranslucentTopBar(title = fileName, onBack = onBack) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Keying on loadKey forces AndroidView to recreate/re-run update
            // whenever a new password attempt should trigger a fresh .load().
            androidx.compose.runtime.key(loadKey) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx -> PDFView(ctx, null) },
                    update = { pdfView ->
                        isLoading = true
                        pdfView.fromUri(currentUri.value)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .password(currentPassword.value)
                            .spacing(8)
                            .onLoad { pageCount ->
                                isLoading = false
                                loadedPageCount = pageCount
                                showPasswordDialog = false
                            }
                            .onError { throwable ->
                                isLoading = false
                                val isPasswordError =
                                    throwable.javaClass.simpleName.contains("Password", ignoreCase = true) ||
                                        (throwable.message?.contains("password", ignoreCase = true) == true)
                                if (isPasswordError) {
                                    isRetry = currentPassword.value != null
                                    showPasswordDialog = true
                                }
                            }
                            .load()
                    }
                )
            }

            if (isLoading && !showPasswordDialog) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            if (!isLoading && loadedPageCount == 0 && !showPasswordDialog) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Couldn't open this PDF.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showPasswordDialog) {
        PasswordPromptDialog(
            isRetry = isRetry,
            onDismiss = {
                showPasswordDialog = false
                onBack()
            },
            onSubmit = { entered ->
                password = entered
                loadKey++
            }
        )
    }
}
