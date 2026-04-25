package pl.edu.ur.blokur

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.domain.model.UserRole
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

/**
 * Globalny host nawigacyjny łączący wszystkie grafy funkcjonalności.
 *
 * Po udanym logowaniu wybiera panel docelowy na podstawie roli użytkownika:
 * - [UserRole.MIESZKANIEC]  → panel mieszkańca ([ResidentRoutes.Main])
 * - [UserRole.KONSERWATOR]  → panel konserwatora (docelowo osobny graf; chwilowo mieszkaniec)
 * - [UserRole.ADMINISTRATOR]→ panel zarządcy (docelowo osobny graf; chwilowo mieszkaniec)
 *
 * Ekran logowania jest usuwany ze stosu po przekierowaniu (`inclusive = true`),
 * by przycisk Wstecz nie wracał na ekran logowania.
 *
 * @param appNavController globalny NavController; domyślnie tworzony przez [rememberNavController].
 * @param startDestination trasa startowa; domyślnie [AuthRoutes.Login].
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
                    UserRole.MIESZKANIEC -> ResidentRoutes.Main
                    // TODO: dodać osobne panele gdy zostaną zaimplementowane
                    UserRole.KONSERWATOR -> ResidentRoutes.Main
                    UserRole.ZARZADCA -> ResidentRoutes.Main
                }
                appNavController.navigate(destination) {
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
