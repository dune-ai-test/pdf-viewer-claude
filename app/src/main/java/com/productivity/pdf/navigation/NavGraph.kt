package com.productivity.pdf.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productivity.pdf.data.UriRegistry
import com.productivity.pdf.ui.screens.LibraryScreen
import com.productivity.pdf.ui.screens.PdfViewerScreen
import com.productivity.pdf.util.PdfFileUtils

private const val ROUTE_LIBRARY = "library"
private const val ROUTE_VIEWER = "viewer/{uriId}"

private fun viewerRoute(uriId: Int) = "viewer/$uriId"

/**
 * @param pendingUri reflects the most recent "Open with" / VIEW intent Uri
 * (from [android.app.Activity.onNewIntent] or the initial launch intent).
 * Every *change* to this value — including the very first one, if the app was
 * cold-launched via "Open with" — triggers an explicit navigation to the
 * viewer that clears the back stack, so opening a file always lands on a
 * clean, freshly-composed viewer screen instead of possibly reusing stale
 * state from whatever was open before.
 * @param onFinish called instead of popping the back stack when there's nowhere
 * left to go back to (i.e. we're showing a file opened via "Open with").
 */
@Composable
fun AppNavGraph(
    pendingUri: State<Uri?>? = null,
    onFinish: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LIBRARY) {
        composable(ROUTE_LIBRARY) {
            LibraryScreen(
                onOpenRealPdf = { uri ->
                    // Only this path (the Library's + button) marks the file
                    // as addToRecents = true.
                    val id = UriRegistry.register(uri, addToRecents = true)
                    navController.navigate(viewerRoute(id))
                }
            )
        }
        composable(
            route = ROUTE_VIEWER,
            arguments = listOf(navArgument("uriId") { type = NavType.IntType })
        ) { backStackEntry ->
            val uriId = backStackEntry.arguments?.getInt("uriId") ?: -1
            val registered = UriRegistry.get(uriId)

            if (registered == null) {
                // Only reachable if the process died and lost the in-memory
                // registry while this screen was still on the back stack.
                LaunchedEffect(Unit) { onFinish() }
            } else {
                val context = LocalContext.current
                val fileName = PdfFileUtils.queryDisplayName(context, registered.uri)

                PdfViewerScreen(
                    uri = registered.uri,
                    fileName = fileName,
                    addToRecents = registered.addToRecents,
                    onBack = {
                        if (!navController.popBackStack()) {
                            onFinish()
                        }
                    }
                )
            }
        }
    }

    // Handle every PDF opened via "Open with" — the initial launch intent AND
    // any later one delivered to onNewIntent while the app was already
    // running. Each distinct Uri clears the back stack and pushes a brand
    // new viewer destination, guaranteeing a fresh PdfViewerScreen instance
    // (fresh `remember`ed state, no leftover cached file from the last PDF).
    var lastHandledUri by remember { mutableStateOf<Uri?>(null) }
    val currentPendingUri = pendingUri?.value

    LaunchedEffect(currentPendingUri) {
        val uri = currentPendingUri ?: return@LaunchedEffect
        if (uri == lastHandledUri) return@LaunchedEffect
        lastHandledUri = uri

        val id = UriRegistry.register(uri, addToRecents = false)
        navController.navigate(viewerRoute(id)) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    }
}
