package pl.edu.ur.blokur

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.announcements.AnnouncementsRoutes
import pl.edu.ur.blokur.ui.views.announcements.announcementsGraph
import pl.edu.ur.blokur.ui.views.auth.AuthRoutes
import pl.edu.ur.blokur.ui.views.auth.authGraph
import pl.edu.ur.blokur.ui.views.finances.FinancesRoutes
import pl.edu.ur.blokur.ui.views.finances.financesGraph
import pl.edu.ur.blokur.ui.views.main.MainRoutes
import pl.edu.ur.blokur.ui.views.main.mainGraph
import pl.edu.ur.blokur.ui.views.profile.ProfileRoutes
import pl.edu.ur.blokur.ui.views.profile.profileGraph
import pl.edu.ur.blokur.ui.views.properties.PropertyRoutes
import pl.edu.ur.blokur.ui.views.properties.propertiesGraph
import pl.edu.ur.blokur.ui.views.categories.CategoryRoutes
import pl.edu.ur.blokur.ui.views.categories.categoriesGraph
import pl.edu.ur.blokur.ui.views.tickets.TicketRoutes
import pl.edu.ur.blokur.ui.views.tickets.ticketsGraph
import pl.edu.ur.blokur.ui.views.users.UserRoutes
import pl.edu.ur.blokur.ui.views.users.usersGraph
import pl.edu.ur.blokur.ui.views.meters.metersGraph

/**
 * Globalny host nawigacyjny łączący wszystkie grafy funkcjonalności.
 */
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
            onLoginSuccess = { role ->
                val destination: AppRoute = when (role) {
                    UserRole.MIESZKANIEC -> MainRoutes.Main
                    UserRole.KONSERWATOR -> MainRoutes.Main
                    UserRole.ZARZADCA -> MainRoutes.Main
                }
                appNavController.navigate(destination) {
                    popUpTo(AuthRoutes.Login) { inclusive = true }
                }
            }
        )

        mainGraph(
            navController = appNavController,
            onLogout = {
                appNavController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
            announcementsRoute = AnnouncementsRoutes.Main,
            financesRoute = FinancesRoutes.Main,
            profileRoute = ProfileRoutes.Main,
            ticketsRoute = TicketRoutes.List,
            propertiesRoute = PropertyRoutes.Tree,
            usersRoute = UserRoutes.List,
            categoriesRoute = CategoryRoutes.List,
            nestedGraphs = { bottomNavController ->
                announcementsGraph(bottomNavController)
                financesGraph(bottomNavController)
                profileGraph(bottomNavController)
                ticketsGraph(bottomNavController)
                propertiesGraph(bottomNavController)
                categoriesGraph(bottomNavController)
                usersGraph(bottomNavController)
                metersGraph(bottomNavController)
            }
        )
    }
}
