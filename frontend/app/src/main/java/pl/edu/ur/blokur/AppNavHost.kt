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
 * **Routing po logowaniu (per rola):**
 * - [UserRole.MIESZKANIEC]  → panel mieszkańca (wszystkie 4 zakładki)
 * - [UserRole.KONSERWATOR]  → panel główny z ograniczonymi zakładkami (Zgłoszenia + Profil)
 * - [UserRole.ZARZADCA]     → panel główny z pełnymi zakładkami
 *
 * **Wylogowanie:**
 * Po kliknięciu ikony wylogowania w TopBar stos nawigacji jest całkowicie czyszczony
 * i użytkownik trafia z powrotem na ekran logowania.
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
            onLogout = {
                appNavController.navigate(AuthRoutes.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
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
