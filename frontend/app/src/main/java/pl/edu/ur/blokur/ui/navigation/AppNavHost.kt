package pl.edu.ur.blokur.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
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

    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = AppRoute.Login.route,
            modifier = Modifier.fillMaxSize()
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
                        // Nawigacja obsługiwana przez LaunchedEffect wyżej po zapisaniu tokenu
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

        GodModeSwitcher(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(100f)
        )
    }
}