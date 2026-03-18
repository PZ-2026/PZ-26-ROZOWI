package pl.edu.ur.blokur.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.edu.ur.blokur.ui.screens.announcements.AnnouncementsScreen
import pl.edu.ur.blokur.ui.screens.auth.LoginScreen
import pl.edu.ur.blokur.ui.screens.finances.FinancesScreen
import pl.edu.ur.blokur.ui.screens.profile.ProfileScreen
import pl.edu.ur.blokur.ui.screens.tickets.TicketsScreen

import androidx.navigation.NavType
import androidx.navigation.navArgument
import pl.edu.ur.blokur.ui.components.GodModeSwitcher
import pl.edu.ur.blokur.ui.screens.tickets.TicketDetailsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
<<<<<<< HEAD
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

=======
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Tickets.route,
            modifier = Modifier.fillMaxSize()
        ) {
>>>>>>> 2e811e3 (BLOK-38 Implementacja globalnego przelacznika rol (God Mode))
        composable(AppRoute.Tickets.route) {
            TicketsScreen(
                onNavigateToDetails = { ticketId ->
                    navController.navigate(AppRoute.TicketDetails(ticketId).route)
                }
            )
        }

        composable(
            route = AppRoute.TicketDetails.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("ticketId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getInt("ticketId") ?: -1
            TicketDetailsScreen(
                ticketId = ticketId,
                onNavigateBack = { navController.popBackStack() }
            )
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
        
    GodModeSwitcher(modifier = Modifier.align(Alignment.TopCenter).zIndex(100f))
    }
}