package pl.edu.ur.blokur.ui.views.users

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.users.screens.UsersScreen
import pl.edu.ur.blokur.ui.views.users.viewmodels.UsersViewModel

sealed interface UserRoutes : AppRoute {
    @Serializable
    data object List : UserRoutes
}

fun NavGraphBuilder.usersGraph(navController: NavController) {
    composable<UserRoutes.List> {
        val viewModel: UsersViewModel = hiltViewModel()
        UsersScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
