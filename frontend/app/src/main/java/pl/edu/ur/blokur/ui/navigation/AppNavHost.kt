package pl.edu.ur.blokur.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.edu.ur.blokur.data.UserPreferences
import pl.edu.ur.blokur.ui.screens.auth.AuthViewModel
import pl.edu.ur.blokur.ui.screens.auth.AuthViewModelFactory
import pl.edu.ur.blokur.ui.screens.auth.AuthState
import pl.edu.ur.blokur.ui.screens.announcements.AnnouncementsScreen
import pl.edu.ur.blokur.ui.screens.auth.LoginScreen
import pl.edu.ur.blokur.ui.screens.finances.FinancesScreen
import pl.edu.ur.blokur.ui.screens.profile.ProfileScreen
import pl.edu.ur.blokur.ui.screens.tickets.TicketsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userPrefs = UserPreferences(context)
    val token by userPrefs.authToken.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (token != null) {
            navController.navigate(AppRoute.Tickets.route) {
                popUpTo(AppRoute.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory.provideFactory(LocalContext.current)
            )
            val authState by authViewModel.authState.collectAsState()

            LoginScreen(
                authState = authState,
                onLoginClick = { email, pass ->
                    authViewModel.login(email, pass)
                },
                onLoginSuccess = {
                    // Navigate handled by LaunchedEffect above when token is written
                }
            )
        }

        composable(AppRoute.Tickets.route) {
            TicketsScreen()
        }

        composable(AppRoute.Finances.route) {
            FinancesScreen()
        }

        composable(AppRoute.Announcements.route) {
            AnnouncementsScreen()
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen()
        }
    }
}