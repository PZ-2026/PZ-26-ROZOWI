package pl.edu.ur.blokur.presentation.resident

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.resident.screen.ResidentMainScreen
import pl.edu.ur.blokur.presentation.resident.util.NavBarOption
import pl.edu.ur.blokur.presentation.resident.viewmodel.ResidentMainViewModel

sealed interface ResidentRoutes : AppRoute {
    @Serializable
    data object Main : ResidentRoutes
}

fun NavGraphBuilder.residentGraph(
    navController: NavController,
    announcementsRoute : AppRoute,
    financesRoute: AppRoute,
    profileRoute: AppRoute,
    ticketsRoute: AppRoute,
    nestedGraphs: NavGraphBuilder.(NavController) -> Unit
) {
    composable<ResidentRoutes.Main> {
        val viewModel: ResidentMainViewModel = hiltViewModel()
        val bottomNavController = rememberNavController()
        ResidentMainScreen(
            viewModel,
            onNavBarItemClicked = { item ->
                val route = when(item) {
                    NavBarOption.NONE -> ResidentRoutes.Main
                    NavBarOption.ANNOUNCEMENTS -> announcementsRoute
                    NavBarOption.FINANCES -> financesRoute
                    NavBarOption.PROFILE -> profileRoute
                    NavBarOption.TICKETS -> ticketsRoute
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
                startDestination = profileRoute,
                modifier = modifier
            ) {
                nestedGraphs(bottomNavController)
            }
        }
    }
}