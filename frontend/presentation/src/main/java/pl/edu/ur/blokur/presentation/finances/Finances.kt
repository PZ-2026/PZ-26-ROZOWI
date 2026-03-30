package pl.edu.ur.blokur.presentation.finances

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.finances.screen.DocumentsScreen
import pl.edu.ur.blokur.presentation.finances.screen.FinancesScreen
import pl.edu.ur.blokur.presentation.finances.screen.TransactionsScreen
import pl.edu.ur.blokur.presentation.finances.viewmodel.FinancesViewModel

sealed interface FinancesRoutes : AppRoute {
    @Serializable
    data object Main : FinancesRoutes

    @Serializable
    data object Transactions : FinancesRoutes

    @Serializable
    data object Documents : FinancesRoutes
}

fun NavGraphBuilder.financesGraph(navController: NavController) {
    composable<FinancesRoutes.Main> {
        val viewModel: FinancesViewModel = hiltViewModel()
        FinancesScreen(
            viewModel = viewModel,
            onNavigateToTransactions = { navController.navigate(FinancesRoutes.Transactions) },
            onNavigateToDocuments = { navController.navigate(FinancesRoutes.Documents) }
        )
    }

    composable<FinancesRoutes.Transactions> {
        // Shared FinancesViewModel — dane załadowane przez Main, nie trzeba ich ponownie ładować
        val parentEntry = navController.getBackStackEntry(FinancesRoutes.Main)
        val viewModel: FinancesViewModel = hiltViewModel(parentEntry)
        TransactionsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<FinancesRoutes.Documents> {
        val parentEntry = navController.getBackStackEntry(FinancesRoutes.Main)
        val viewModel: FinancesViewModel = hiltViewModel(parentEntry)
        DocumentsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
