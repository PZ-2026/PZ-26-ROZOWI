package pl.edu.ur.blokur.presentation.tickets

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.tickets.screen.CreateTicketScreen
import pl.edu.ur.blokur.presentation.tickets.screen.TicketDetailsScreen
import pl.edu.ur.blokur.presentation.tickets.screen.TicketsScreen

sealed interface TicketRoutes : AppRoute {
    @Serializable
    data object List : TicketRoutes

    @Serializable
    data class Details(val ticketId: Int) : TicketRoutes

    @Serializable
    data object Create : TicketRoutes
}

fun NavGraphBuilder.ticketsGraph(navController: NavController) {
    composable<TicketRoutes.List> {
        val viewModel: TicketsViewModel = hiltViewModel()
        TicketsScreen(
            viewModel = viewModel,
            onNavigateToDetails = { ticketId -> navController.navigate(TicketRoutes.Details(ticketId)) },
            onNavigateToCreate = { navController.navigate(TicketRoutes.Create) }
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
