package pl.edu.ur.blockur

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.presentation.announcements.AnnouncementsRoutes
import pl.edu.ur.blokur.presentation.announcements.announcementsGraph
import pl.edu.ur.blokur.presentation.auth.AuthRoutes
import pl.edu.ur.blokur.presentation.auth.authGraph
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.finances.FinancesRoutes
import pl.edu.ur.blokur.presentation.finances.financesGraph
import pl.edu.ur.blokur.presentation.profile.ProfileRoutes
import pl.edu.ur.blokur.presentation.profile.profileGraph
import pl.edu.ur.blokur.presentation.resident.ResidentRoutes
import pl.edu.ur.blokur.presentation.resident.residentGraph
import pl.edu.ur.blokur.presentation.tickets.TicketRoutes
import pl.edu.ur.blokur.presentation.tickets.ticketsGraph

@Composable
fun AppNavHost(
    appNavController: NavHostController = rememberNavController(),
    startDestination: AppRoute = AuthRoutes.Login
) {
    NavHost(
        navController = appNavController,
        startDestination = startDestination
    ) {
        authGraph(
            navController = appNavController,
            onLoginSuccess = {
                appNavController.navigate(ResidentRoutes.Main) {
                    popUpTo(AuthRoutes.Login) { inclusive = true }
                }
            }
        )

        residentGraph(
            navController = appNavController,
            announcementsRoute = AnnouncementsRoutes.Main,
            financesRoute = FinancesRoutes.Main,
            profileRoute = ProfileRoutes.Main,
            ticketsRoute = TicketRoutes.List,
            nestedGraphs = { bottomNavController ->
                announcementsGraph(bottomNavController)
                financesGraph(bottomNavController)
                profileGraph(bottomNavController)
                ticketsGraph(bottomNavController)
            }
        )
    }
}