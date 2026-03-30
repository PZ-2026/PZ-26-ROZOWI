package pl.edu.ur.blockur

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.profile.ProfileRoutes
import pl.edu.ur.blokur.presentation.profile.profileGraph
import pl.edu.ur.blokur.presentation.resident.ResidentRoutes
import pl.edu.ur.blokur.presentation.resident.residentGraph

@Composable
fun AppNavHost(
    appNavController: NavHostController = rememberNavController(),
    startDestination: AppRoute = ResidentRoutes.Main
) {
    NavHost(
        navController = appNavController,
        startDestination = startDestination
    ) {
        //resident graph with inner navigation
        residentGraph(
            navController = appNavController,
            announcementsRoute = ProfileRoutes.Main,
            financesRoute = ProfileRoutes.Main,
            profileRoute = ProfileRoutes.Main,
            ticketsRoute = ProfileRoutes.Main,
            nestedGraphs = { bottomNavController ->
                profileGraph(bottomNavController)
                profileGraph(bottomNavController)
                profileGraph(bottomNavController)
                profileGraph(bottomNavController)
            }
        )
    }
}