package pl.edu.ur.blokur.ui.views.inspections

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.inspections.screens.InspectionsListScreen
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionsListViewModel

sealed interface InspectionRoutes : AppRoute {
    @Serializable
    data object List : InspectionRoutes
}

fun NavGraphBuilder.inspectionsGraph(navController: NavController) {
    composable<InspectionRoutes.List> {
        val viewModel: InspectionsListViewModel = hiltViewModel()
        InspectionsListScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
