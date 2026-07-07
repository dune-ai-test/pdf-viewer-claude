package com.productivity.pdf.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.productivity.pdf.data.RecentPdfsStore
import com.productivity.pdf.model.RecentPdf
import com.productivity.pdf.ui.components.AnnotationToolbar
import com.productivity.pdf.ui.components.PasswordPromptDialog
import com.productivity.pdf.ui.components.TranslucentTopBar
import com.productivity.pdf.util.PdfFileUtils
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Real PDF viewer backed by Pdfium (android-pdf-viewer).
 *
 * UI behavior: immersive by default — only the page(s) are shown. Tapping
 * anywhere on a page toggles the top title bar and the bottom annotation
 * toolbar back on/off (like most PDF/photo viewers). Hardware/gesture back
 * always works via `BackHandler`, regardless of whether the bars are showing.
 *
 * @param addToRecents true only when this file was opened via the Library's
 * own "+" button. Files opened via "Open with" from another app are viewed
 * normally but deliberately NOT added to Recents.
 */
@Composable
fun PdfViewerScreen(
    uri: Uri,
    fileName: String,
    addToRecents: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler(onBack = onBack)

    // Immersive chrome toggle — starts hidden, tapping the page flips it.
    var chromeVisible by remember { mutableStateOf(false) }

    // Only file:// sources on API <= 28 (Android 9 and below) need the legacy
    // runtime permission at all — content:// (SAF / most "Open with" senders)
    // and every API 29+ device never touch this path.
    val needsLegacyStoragePermission = uri.scheme == "file" && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    var hasStoragePermission by remember {
        mutableStateOf(
            !needsLegacyStoragePermission ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        permissionDenied = !granted
    }

    var cachedFile by remember { mutableStateOf<File?>(null) }
    var copyFailed by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf<String?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isRetry by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var loadedPageCount by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // Bumped when the user submits a password, to guarantee the load effect
    // re-fires even if they retype the exact same (still-wrong) password.
    var loadAttempt by remember { mutableIntStateOf(0) }

    val pdfViewHolder = remember { mutableStateOf<PDFView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
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
                    text = when {
                        permissionDenied ->
                            "Storage permission is needed to open this file.\nGrant it in Settings > Apps > PDF Productivity > Permissions, then try again."
                        copyFailed ->
                            "Couldn't read this PDF.\nIt may have been moved, deleted, or the app that shared it didn't grant access."
                        errorMessage != null -> "Couldn't open this PDF.\n$errorMessage"
                        else -> "Couldn't open this PDF."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Immersive overlay chrome — only drawn (and only takes up space) when
        // toggled on, floating over the full-bleed page content underneath.
        if (chromeVisible) {
            TranslucentTopBar(
                title = fileName,
                onBack = onBack,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (!isLoading && loadedPageCount > 0) {
                AnnotationToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }
        }
    }

    // Step 1: for legacy file:// sources, make sure we actually have the
    // runtime permission before trying to read anything.
    LaunchedEffect(uri, hasStoragePermission) {
        if (needsLegacyStoragePermission && !hasStoragePermission) {
            isLoading = true
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            return@LaunchedEffect
        }

        // Step 2: read the source Uri's bytes into our own cache file exactly
        // once per Uri (not repeated on password retries).
        isLoading = true
        errorMessage = null
        copyFailed = false
        cachedFile = withContext(Dispatchers.IO) {
            PdfFileUtils.copyToCache(context, uri)
        }
        if (cachedFile == null) {
            isLoading = false
            copyFailed = true
        }
    }

    // Step 3: load the local file into Pdfium. Re-runs when the cached file
    // becomes available, or when the user submits a (new) password.
    LaunchedEffect(cachedFile, password, loadAttempt) {
        val file = cachedFile ?: return@LaunchedEffect
        val pdfView = pdfViewHolder.value ?: return@LaunchedEffect
        isLoading = true
        errorMessage = null
        pdfView.fromFile(file)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .password(password)
            .spacing(8)
            .onTap {
                chromeVisible = !chromeVisible
                true
            }
            .onLoad { pageCount ->
                isLoading = false
                loadedPageCount = pageCount
                showPasswordDialog = false
                if (addToRecents) {
                    RecentPdfsStore.addOrBumpToTop(
                        RecentPdf(
                            uri = uri,
                            name = fileName,
                            sizeLabel = PdfFileUtils.querySizeLabel(context, uri),
                            openedAt = "Just now"
                        )
                    )
                }
            }
            .onError { throwable ->
                isLoading = false
                val isPasswordError =
                    throwable.javaClass.simpleName.contains("Password", ignoreCase = true) ||
                        (throwable.message?.contains("password", ignoreCase = true) == true)
                if (isPasswordError) {
                    isRetry = password != null
                    showPasswordDialog = true
                    // Show the bars while the password dialog is up so the
                    // screen doesn't look stuck on a blank page underneath it.
                    chromeVisible = true
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
