package com.productivity.pdf.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productivity.pdf.ui.screens.LibraryScreen
import com.productivity.pdf.ui.screens.PdfViewerScreen
import com.productivity.pdf.util.PdfFileUtils

private const val ROUTE_LIBRARY = "library"
private const val ROUTE_VIEWER = "viewer/{encodedUri}"

private fun viewerRoute(uri: Uri) = "viewer/${Uri.encode(uri.toString())}"

/**
 * @param initialUri set when the app was launched via "Open with" / a VIEW
 * intent on a .pdf file — in that case we skip straight to the viewer instead
 * of the (now-empty) Library screen.
 * @param onFinish called instead of popping the back stack when there's nowhere
 * left to go back to (i.e. we were launched directly into the viewer).
 */
@Composable
fun AppNavGraph(
    initialUri: Uri? = null,
    onFinish: () -> Unit = {}
) {
    val navController = rememberNavController()
    val startDestination = initialUri?.let { viewerRoute(it) } ?: ROUTE_LIBRARY

    NavHost(navController = navController, startDestination = startDestination) {
        composable(ROUTE_LIBRARY) {
            LibraryScreen(
                onOpenRealPdf = { uri -> navController.navigate(viewerRoute(uri)) }
            )
        }
        composable(
            route = ROUTE_VIEWER,
            arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val encodedUri = backStackEntry.arguments?.getString("encodedUri").orEmpty()
            val uri = Uri.parse(Uri.decode(encodedUri))
            val fileName = PdfFileUtils.queryDisplayName(context, uri)

            PdfViewerScreen(
                uri = uri,
                fileName = fileName,
                onBack = {
                    if (!navController.popBackStack()) {
                        onFinish()
                    }
                }
            )
        }
    }
}
