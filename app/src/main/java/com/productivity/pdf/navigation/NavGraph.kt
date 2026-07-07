package com.productivity.pdf.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

/**
 * @param initialUri set when the app was launched via "Open with" / a VIEW
 * intent on a .pdf file — in that case we skip straight to the viewer instead
 * of the Library screen, and (per design) this file is NOT added to Recents:
 * only PDFs opened via the Library's own "+" button count as "recent".
 * @param onFinish called instead of popping the back stack when there's nowhere
 * left to go back to (i.e. we were launched directly into the viewer).
 */
@Composable
fun AppNavGraph(
    initialUri: Uri? = null,
    onFinish: () -> Unit = {}
) {
    val navController = rememberNavController()
    val startDestination = initialUri
        ?.let { "viewer/${UriRegistry.register(it, addToRecents = false)}" }
        ?: ROUTE_LIBRARY

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LIBRARY) {
            LibraryScreen(
                onOpenRealPdf = { uri ->
                    // Only this path (the Library's + button) marks the file
                    // as addToRecents = true.
                    val id = UriRegistry.register(uri, addToRecents = true)
                    navController.navigate("viewer/$id")
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
}
