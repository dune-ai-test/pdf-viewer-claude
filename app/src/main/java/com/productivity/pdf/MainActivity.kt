package com.productivity.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.productivity.pdf.navigation.AppNavGraph
import com.productivity.pdf.data.PdfSettingsStore
import com.productivity.pdf.ui.theme.PdfProductivityTheme
import com.productivity.pdf.util.PdfFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // Held here (not just inside the composable) so `onNewIntent` can push a
    // new value into an already-running Compose tree — see the class doc below
    // for why that distinction matters.
    private val pendingUri = mutableStateOf<Uri?>(null)

    /**
     * IMPORTANT: `setContent` is called exactly once, in `onCreate`.
     *
     * The earlier version called `setContent` again from `onNewIntent` to
     * "jump to the new file". That doesn't do what it looks like it does:
     * `ComponentActivity.setContent` reuses the existing `ComposeView` and its
     * *existing Composition* if one is already attached — it does not tear
     * down and rebuild the Compose tree. That meant `rememberNavController()`
     * inside `AppNavGraph` kept its old instance and old back stack every
     * time, so a new "Open with" launch while the app was merely backgrounded
     * (not process-killed) could silently keep showing the previous PDF, or
     * race with its still-cached file, surfacing as "Couldn't read this PDF".
     *
     * Fix: keep a single long-lived Composition. `onNewIntent` only updates
     * `pendingUri` (a `MutableState`), and `AppNavGraph` reacts to that change
     * itself with an explicit, back-stack-clearing `navController.navigate(...)`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingUri.value = extractPdfUri(intent)

        // Loads the saved page-background-color / night-mode choices (a
        // couple of SharedPreferences keys — negligible I/O) before any
        // composable reads PdfSettingsStore, so the correct saved value shows
        // immediately instead of a default flash.
        PdfSettingsStore.init(applicationContext)

        // Safety net: normal open/close cycles delete their own cache file
        // (see PdfViewerScreen's DisposableEffect); this only catches the case
        // where that didn't run, e.g. the app was force-killed mid-view.
        MainScope().launch(Dispatchers.IO) {
            PdfFileUtils.clearCachedPdfs(applicationContext)
        }

        setContent {
            PdfProductivityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        pendingUri = pendingUri,
                        onFinish = { finish() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingUri.value = extractPdfUri(intent)
    }

    /** Returns the PDF's Uri if this activity was launched via "Open with" / VIEW. */
    private fun extractPdfUri(intent: Intent?): Uri? {
        return if (intent?.action == Intent.ACTION_VIEW) intent.data else null
    }
}
