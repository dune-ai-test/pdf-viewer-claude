package com.productivity.pdf.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productivity.pdf.data.SampleData
import com.productivity.pdf.ui.screens.DocumentScreen
import com.productivity.pdf.ui.screens.LibraryScreen
import com.productivity.pdf.ui.screens.PdfViewerScreen

private const val ROUTE_LIBRARY = "library"
private const val ROUTE_DOCUMENT = "document/{docId}"
private const val ROUTE_VIEWER = "viewer/{encodedUri}"

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_LIBRARY) {
        composable(ROUTE_LIBRARY) {
            LibraryScreen(
                onOpenDocument = { doc ->
                    navController.navigate("document/${doc.id}")
                },
                onOpenRealPdf = { uri ->
                    val encoded = Uri.encode(uri.toString())
                    navController.navigate("viewer/$encoded")
                }
            )
        }
        composable(
            route = ROUTE_DOCUMENT,
            arguments = listOf(navArgument("docId") { type = NavType.IntType })
        ) { backStackEntry ->
            val docId = backStackEntry.arguments?.getInt("docId") ?: -1
            val document = SampleData.documents.firstOrNull { it.id == docId }
                ?: SampleData.documents.first()

            DocumentScreen(
                document = document,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = ROUTE_VIEWER,
            arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("encodedUri").orEmpty()
            val uri = Uri.parse(Uri.decode(encodedUri))
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Document"

            PdfViewerScreen(
                uri = uri,
                fileName = fileName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
