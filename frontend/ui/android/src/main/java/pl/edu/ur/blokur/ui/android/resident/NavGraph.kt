package pl.edu.ur.blokur.ui.android.resident

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            val bottomNavController = rememberNavController()
            ResidentMainScreen(
                viewModel,
                onNavBarItemClicked = { item ->
                    val route = when(item) {
                        NavBarOption.NONE -> ProfileRoutes.ProfileView
                        NavBarOption.ANNOUNCEMENTS -> AnnouncementsRoutes.AnnouncementsView
                        NavBarOption.FINANCES -> FinancesRoutes.FinancesView
                        NavBarOption.PROFILE -> ProfileRoutes.ProfileView
                        NavBarOption.TICKETS -> TicketsRoutes.TicketsView
                    }

                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                modifier -> NavHost(
                    navController = bottomNavController,
                    startDestination = AnnouncementsRoutes.AnnouncementsView,
                    modifier = modifier
                ){
                    innerResidentGraph()
                 }
            }
        }
    }
}

fun NavGraphBuilder.innerResidentGraph() {
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