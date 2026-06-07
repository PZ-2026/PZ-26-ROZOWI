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
    
    @Serializable
    data class Edit(val userId: String) : UserRoutes
}

fun NavGraphBuilder.usersGraph(navController: NavController) {
    composable<UserRoutes.List> {
        val viewModel: UsersViewModel = hiltViewModel()
        UsersScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToUser = { id -> navController.navigate(UserRoutes.Edit(id)) }
        )
    }

    composable<UserRoutes.Edit> {
        // viewModel will be implemented next
        val userId = it.arguments?.getString("userId") ?: return@composable
        val viewModel: pl.edu.ur.blokur.ui.views.users.viewmodels.EditUserViewModel = hiltViewModel()
        pl.edu.ur.blokur.ui.views.users.screens.EditUserScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
