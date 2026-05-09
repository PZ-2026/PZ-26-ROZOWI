package pl.edu.ur.blokur.ui.views.finances

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.finances.screens.ApartmentBalancesScreen
import pl.edu.ur.blokur.ui.views.finances.screens.DocumentsScreen
import pl.edu.ur.blokur.ui.views.finances.screens.FinancesScreen
import pl.edu.ur.blokur.ui.views.finances.screens.FinancialLedgerScreen
import pl.edu.ur.blokur.ui.views.finances.screens.TransactionsScreen
import pl.edu.ur.blokur.ui.views.finances.viewmodels.ApartmentBalancesViewModel
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancesViewModel
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancialLedgerViewModel

sealed interface FinancesRoutes : AppRoute {
    @Serializable
    data object Main : FinancesRoutes

    @Serializable
    data object Transactions : FinancesRoutes

    @Serializable
    data object Documents : FinancesRoutes

    /** Kartoteka finansowa:
     *  - bez apartmentId → mieszkaniec (sam pobiera swój lokal z drzewa)
     *  - z apartmentId   → zarządca otwiera konkretny lokal
     */
    @Serializable
    data class Ledger(val apartmentId: String? = null) : FinancesRoutes

    @Serializable
    data object Balances : FinancesRoutes
}

fun NavGraphBuilder.financesGraph(navController: NavController) {
    composable<FinancesRoutes.Main> {
        val viewModel: FinancesViewModel = hiltViewModel()
        FinancesScreen(
            viewModel = viewModel,
            onNavigateToTransactions = { navController.navigate(FinancesRoutes.Transactions) },
            onNavigateToDocuments = { navController.navigate(FinancesRoutes.Documents) },
            onNavigateToLedger = { navController.navigate(FinancesRoutes.Ledger()) },
            onNavigateToBalances = { navController.navigate(FinancesRoutes.Balances) }
        )
    }

    composable<FinancesRoutes.Transactions> {
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

    composable<FinancesRoutes.Ledger> {
        val viewModel: FinancialLedgerViewModel = hiltViewModel()
        FinancialLedgerScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<FinancesRoutes.Balances> {
        val viewModel: ApartmentBalancesViewModel = hiltViewModel()
        ApartmentBalancesScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
