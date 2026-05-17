package pl.edu.ur.blokur.ui.views.documents

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.documents.screens.DocumentDistributionScreen
import pl.edu.ur.blokur.ui.views.documents.viewmodels.DocDistributionViewModel

sealed interface DocumentRoutes : AppRoute {
    @Serializable
    data object Distribution : DocumentRoutes
}

fun NavGraphBuilder.documentsGraph(navController: NavController) {
    composable<DocumentRoutes.Distribution> {
        val viewModel: DocDistributionViewModel = hiltViewModel()
        DocumentDistributionScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
