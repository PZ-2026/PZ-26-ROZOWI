package pl.edu.ur.blokur.presentation.auth

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.presentation.auth.screen.LoginScreen
import pl.edu.ur.blokur.presentation.auth.viewmodel.AuthViewModel
import pl.edu.ur.blokur.presentation.common.AppRoute

/** Trasy nawigacyjne modułu autoryzacji. */
sealed interface AuthRoutes : AppRoute {
    @Serializable
    data object Login : AuthRoutes
}

/**
 * Rejestruje composable'e modułu auth w globalnym grafie nawigacji.
 *
 * @param navController   globalny NavController (zarządza `AuthRoutes`).
 * @param onLoginSuccess  callback z rolą użytkownika – wywoływany po udanym logowaniu;
 *                        odpowiedzialność za wybór docelowego ekranu leży w [AppNavHost].
 */
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
