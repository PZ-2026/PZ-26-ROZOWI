package pl.edu.ur.blokur.ui.views.auth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.auth.screens.ForgotPasswordScreen
import pl.edu.ur.blokur.ui.views.auth.screens.LoginScreen
import pl.edu.ur.blokur.ui.views.auth.screens.ResetPasswordScreen
import pl.edu.ur.blokur.ui.views.auth.viewmodels.AuthViewModel
import pl.edu.ur.blokur.ui.views.auth.viewmodels.ForgotPasswordViewModel
import pl.edu.ur.blokur.ui.views.auth.viewmodels.ResetPasswordViewModel

sealed interface AuthRoutes : AppRoute {
    @Serializable
    data object Login : AuthRoutes

    @Serializable
    data object ForgotPassword : AuthRoutes

    @Serializable
    data class ResetPassword(val token: String) : AuthRoutes

    @Serializable
    data class AcceptInvitation(val token: String) : AuthRoutes
}

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: (UserRole) -> Unit
) {
    composable<AuthRoutes.Login> {
        val viewModel: AuthViewModel = hiltViewModel()
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onForgotPassword = {
                navController.navigate(AuthRoutes.ForgotPassword)
            }
        )
    }

    composable<AuthRoutes.ForgotPassword> {
        val viewModel: ForgotPasswordViewModel = hiltViewModel()
        ForgotPasswordScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<AuthRoutes.ResetPassword> {
        val viewModel: ResetPasswordViewModel = hiltViewModel()
        ResetPasswordScreen(
            viewModel = viewModel,
            onNavigateToLogin = {
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToForgotPassword = {
                navController.navigate(AuthRoutes.ForgotPassword) {
                    popUpTo(AuthRoutes.Login) { inclusive = false }
                }
            }
        )
    }

    composable<AuthRoutes.AcceptInvitation> {
        val viewModel: pl.edu.ur.blokur.ui.views.auth.viewmodels.AcceptInvitationViewModel = hiltViewModel()
        pl.edu.ur.blokur.ui.views.auth.screens.AcceptInvitationScreen(
            viewModel = viewModel,
            onNavigateToLogin = {
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
