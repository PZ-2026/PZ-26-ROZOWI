package pl.edu.ur.blokur.ui.views.auth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.auth.screens.LoginScreen
import pl.edu.ur.blokur.ui.views.auth.viewmodels.AuthViewModel

sealed interface AuthRoutes : AppRoute {
    @Serializable
    data object Login : AuthRoutes
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: (UserRole) -> Unit
) {
    composable<AuthRoutes.Login> {
        val viewModel: AuthViewModel = hiltViewModel()
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess
        )
    }
}
