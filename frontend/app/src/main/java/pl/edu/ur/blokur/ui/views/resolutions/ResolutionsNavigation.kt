package pl.edu.ur.blokur.ui.views.resolutions

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.resolutions.screens.ResolutionDetailScreen
import pl.edu.ur.blokur.ui.views.resolutions.screens.ResolutionsListScreen
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionDetailViewModel
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionsListViewModel

sealed interface ResolutionRoutes : AppRoute {

    @Serializable
    data object List : ResolutionRoutes

    @Serializable
    data class Detail(val resolutionId: String) : ResolutionRoutes
}

fun NavGraphBuilder.resolutionsGraph(navController: NavController) {

    composable<ResolutionRoutes.List> {
        val viewModel: ResolutionsListViewModel = hiltViewModel()
        ResolutionsListScreen(
            viewModel = viewModel,
            onNavigateToDetail = { id -> navController.navigate(ResolutionRoutes.Detail(id)) }
        )
    }

    composable<ResolutionRoutes.Detail> {
        val viewModel: ResolutionDetailViewModel = hiltViewModel()
        ResolutionDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
