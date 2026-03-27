package pl.edu.ur.blokur.ui.android.resident

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import pl.edu.ur.blokur.ui.android.main.ApplicationRoutes
import pl.edu.ur.blokur.ui.android.resident.screens.AnnouncementsScreen
import pl.edu.ur.blokur.ui.android.resident.screens.ResidentMainScreen
import pl.edu.ur.blokur.ui.android.resident.screens.FinancesScreen
import pl.edu.ur.blokur.ui.android.resident.screens.ProfileScreen
import pl.edu.ur.blokur.ui.android.resident.screens.TicketsScreen
import pl.edu.ur.blokur.ui.android.resident.viewmodels.AnnouncementsViewModel
import pl.edu.ur.blokur.ui.android.resident.viewmodels.FinancesViewModel
import pl.edu.ur.blokur.ui.android.resident.viewmodels.ResidentMainViewModel
import pl.edu.ur.blokur.ui.android.resident.viewmodels.TicketsViewModel
import pl.edu.ur.blokur.ui.android.resident.viewmodels.ProfileViewModel

fun NavGraphBuilder.residentGraph(
    navController: NavController
) {
    navigation<ApplicationRoutes.Resident>(
        startDestination = ResidentMainRoutes.ResidentMainView
    ) {
        composable<ResidentMainRoutes.ResidentMainView> {
            val viewModel: ResidentMainViewModel = hiltViewModel()
            ResidentMainScreen(viewModel)
        }

        composable<AnnouncementsRoutes.AnnouncementsView> {
            val viewModel: AnnouncementsViewModel = hiltViewModel()
            AnnouncementsScreen(viewModel)
        }

        composable<FinancesRoutes.FinancesView> {
            val viewModel: FinancesViewModel = hiltViewModel()
            FinancesScreen(viewModel)
        }

        composable<TicketsRoutes.TicketsView> {
            val viewModel: TicketsViewModel = hiltViewModel()
            TicketsScreen(viewModel)
        }

        composable<ProfileRoutes.ProfileView> {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(viewModel)
        }
    }
}