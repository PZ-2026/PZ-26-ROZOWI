package pl.edu.ur.blokur.ui.views.categories

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.categories.screens.CategoriesScreen
import pl.edu.ur.blokur.ui.views.categories.viewmodels.CategoriesViewModel

sealed interface CategoryRoutes : AppRoute {
    @Serializable
    data object List : CategoryRoutes
}

fun NavGraphBuilder.categoriesGraph(navController: NavController) {
    composable<CategoryRoutes.List> {
        val viewModel: CategoriesViewModel = hiltViewModel()
        CategoriesScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
