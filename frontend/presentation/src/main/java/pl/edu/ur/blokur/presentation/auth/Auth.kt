package pl.edu.ur.blokur.presentation.auth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute

sealed interface AuthRoutes : AppRoute {
    @Serializable
    data object Login : AuthRoutes
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    composable<AuthRoutes.Login> {
        val viewModel: AuthViewModel = hiltViewModel()
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess
        )
    }
}
