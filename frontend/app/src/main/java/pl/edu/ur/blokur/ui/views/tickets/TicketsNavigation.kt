package pl.edu.ur.blokur.ui.views.tickets

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.categories.CategoryRoutes
import pl.edu.ur.blokur.ui.views.users.UserRoutes
import pl.edu.ur.blokur.ui.views.tickets.screens.CreateTicketScreen
import pl.edu.ur.blokur.ui.views.tickets.screens.TicketDetailsScreen
import pl.edu.ur.blokur.ui.views.tickets.screens.TicketsScreen
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.CreateTicketViewModel
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketDetailsViewModel
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketsViewModel

sealed interface TicketRoutes : AppRoute {
    @Serializable
    data object List : TicketRoutes

    @Serializable
    data class Details(val ticketId: String) : TicketRoutes

    @Serializable
    data object Create : TicketRoutes
}

fun NavGraphBuilder.ticketsGraph(navController: NavController) {
    composable<TicketRoutes.List> {
        val viewModel: TicketsViewModel = hiltViewModel()
        TicketsScreen(
            viewModel = viewModel,
            onNavigateToDetails = { ticketId -> navController.navigate(TicketRoutes.Details(ticketId)) },
            onNavigateToCreate = { navController.navigate(TicketRoutes.Create) },
            onNavigateToCategories = { navController.navigate(CategoryRoutes.List) },
            onNavigateToUsers = { navController.navigate(UserRoutes.List) }
        )
    }

    composable<TicketRoutes.Details> {
        val viewModel: TicketDetailsViewModel = hiltViewModel()
        TicketDetailsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<TicketRoutes.Create> {
        val viewModel: CreateTicketViewModel = hiltViewModel()
        CreateTicketScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
