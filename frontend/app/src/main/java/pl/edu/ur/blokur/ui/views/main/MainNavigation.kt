package pl.edu.ur.blokur.ui.views.main

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.main.screens.ResidentMainScreen
import pl.edu.ur.blokur.ui.views.main.utils.NavBarOption
import pl.edu.ur.blokur.ui.views.main.viewmodels.ResidentMainViewModel

sealed interface MainRoutes : AppRoute {
    @Serializable
    data object Main : MainRoutes
}

fun NavGraphBuilder.mainGraph(
    navController: NavController,
    onLogout: () -> Unit,
    announcementsRoute: AppRoute,
    financesRoute: AppRoute,
    profileRoute: AppRoute,
    ticketsRoute: AppRoute,
    nestedGraphs: NavGraphBuilder.(NavController) -> Unit
) {
    composable<MainRoutes.Main> {
        val viewModel: ResidentMainViewModel = hiltViewModel()
        val bottomNavController = rememberNavController()

        ResidentMainScreen(
            viewModel = viewModel,
            onNavBarItemClicked = { item ->
                val route = when (item) {
                    NavBarOption.NONE -> MainRoutes.Main
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
            },
            onLogout = onLogout
        ) { modifier ->
            NavHost(
                navController = bottomNavController,
                startDestination = profileRoute,
                modifier = modifier
            ) {
                nestedGraphs(bottomNavController)
            }
        }
    }
}
