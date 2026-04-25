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

/** Trasy nawigacyjne głównego panelu po zalogowaniu. */
sealed interface ResidentRoutes : AppRoute {
    @Serializable
    data object Main : ResidentRoutes
}

/**
 * Rejestruje composable głównego panelu w grafie nawigacji.
 *
 * @param navController   globalny NavController.
 * @param onLogout        callback wywoływany po wylogowaniu – obsługiwany w AppNavHost.
 * @param announcementsRoute trasa zakładki ogłoszeń.
 * @param financesRoute      trasa zakładki finansów.
 * @param profileRoute       trasa zakładki profilu.
 * @param ticketsRoute       trasa zakładki zgłoszeń.
 * @param nestedGraphs    builder zagnieżdżonych grafów funkcjonalności.
 */
fun NavGraphBuilder.residentGraph(
    navController: NavController,
    onLogout: () -> Unit,
    announcementsRoute: AppRoute,
    financesRoute: AppRoute,
    profileRoute: AppRoute,
    ticketsRoute: AppRoute,
    nestedGraphs: NavGraphBuilder.(NavController) -> Unit
) {
    composable<ResidentRoutes.Main> {
        val viewModel: ResidentMainViewModel = hiltViewModel()
        val bottomNavController = rememberNavController()

        ResidentMainScreen(
            viewModel = viewModel,
            onNavBarItemClicked = { item ->
                val route = when (item) {
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
