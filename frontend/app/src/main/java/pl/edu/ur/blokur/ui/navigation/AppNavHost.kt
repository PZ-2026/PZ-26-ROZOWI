package pl.edu.ur.blokur.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    NavHost(
        navController = navController,
        startDestination = AppRoute.Login.route,
        modifier = modifier
    ) {
        composable(AppRoute.Login.route) {
            LoginScreen(
                onLoginClick = { _, _ ->
                    navController.navigate(AppRoute.Tickets.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
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