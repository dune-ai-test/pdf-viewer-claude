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
import androidx.compose.ui.Modifier
import com.productivity.pdf.navigation.AppNavGraph
import com.productivity.pdf.ui.theme.PdfProductivityTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        renderContent(extractPdfUri(intent))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Simplest reliable way to make a fresh "Open with" launch (while the app
        // is already running) jump straight into the new file: rebuild the whole
        // Compose tree from the new intent's Uri.
        renderContent(extractPdfUri(intent))
    }

    private fun renderContent(initialUri: Uri?) {
        setContent {
            PdfProductivityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(
                        initialUri = initialUri,
                        onFinish = { finish() }
                    )
                }
            }
        }
    }

    /** Returns the PDF's Uri if this activity was launched via "Open with" / VIEW. */
    private fun extractPdfUri(intent: Intent?): Uri? {
        return if (intent?.action == Intent.ACTION_VIEW) intent.data else null
    }
}
