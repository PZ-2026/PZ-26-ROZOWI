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
import pl.edu.ur.blokur.ui.screens.finances.TransactionsScreen
import pl.edu.ur.blokur.ui.screens.finances.DocumentsScreen
import pl.edu.ur.blokur.ui.screens.profile.ProfileScreen
import pl.edu.ur.blokur.ui.screens.tickets.TicketsScreen
import pl.edu.ur.blokur.ui.screens.tickets.CreateTicketScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import pl.edu.ur.blokur.ui.components.GodModeSwitcher
import pl.edu.ur.blokur.ui.screens.tickets.TicketDetailsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            // Zachowujemy Login jako startDestination z HEAD
            startDestination = AppRoute.Login.route,
            modifier = Modifier.fillMaxSize()
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
                TicketsScreen(
                    onNavigateToDetails = { ticketId ->
                        navController.navigate(AppRoute.TicketDetails(ticketId).route)
                    },
                    onNavigateToAddTicket = {
                        navController.navigate(AppRoute.AddTicket.route)
                    }
                )
            }

            composable(AppRoute.AddTicket.route) {
                CreateTicketScreen(
                    onNavigateBack = { navController.popBackStack() }
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
                FinancesScreen(
                    onNavigateToTransactions = { navController.navigate(AppRoute.Transactions.route) },
                    onNavigateToDocuments = { navController.navigate(AppRoute.Documents.route) }
                )
            }

            composable(AppRoute.Transactions.route) {
                TransactionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppRoute.Documents.route) {
                DocumentsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppRoute.Announcements.route) {
                AnnouncementsScreen()
            }

            composable(AppRoute.Profile.route) {
                ProfileScreen()
            }
        }

        // Nakładka "God Mode" z gałęzi BLOK-38
        GodModeSwitcher(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(100f)
        )
    }
}