package pl.edu.ur.blokur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.EntryPointAccessors
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.di.SessionEntryPoint
import pl.edu.ur.blokur.di.TokenStorageEntryPoint
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.announcements.AnnouncementsRoutes
import pl.edu.ur.blokur.ui.views.announcements.announcementsGraph
import pl.edu.ur.blokur.ui.views.auth.AuthRoutes
import pl.edu.ur.blokur.ui.views.auth.authGraph
import pl.edu.ur.blokur.ui.views.documents.DocumentRoutes
import pl.edu.ur.blokur.ui.views.documents.documentsGraph
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
import pl.edu.ur.blokur.ui.views.resolutions.ResolutionRoutes
import pl.edu.ur.blokur.ui.views.resolutions.resolutionsGraph
import pl.edu.ur.blokur.ui.views.inspections.InspectionRoutes
import pl.edu.ur.blokur.ui.views.inspections.inspectionsGraph
import pl.edu.ur.blokur.ui.views.notifications.NotificationRoutes
import pl.edu.ur.blokur.ui.views.notifications.notificationsGraph
import pl.edu.ur.blokur.ui.views.settings.SettingsRoutes
import pl.edu.ur.blokur.ui.views.settings.settingsGraph

import pl.edu.ur.blokur.ui.views.meters.metersGraph

/** Trasa rozruchowa — sprawdza zapisane tokeny i przekierowuje na Login lub Main. */
@Serializable
data object SplashRoute : AppRoute

/**
 * Globalny host nawigacyjny łączący wszystkie grafy funkcjonalności.
 *
 * Startuje od [SplashRoute], który sprawdza czy w DataStore istnieje zapisany
 * access token i rola użytkownika. Jeśli tak — nawiguje od razu do ekranu głównego,
 * pomijając ponowne logowanie. W przeciwnym razie kieruje na ekran logowania.
 */
@Composable
fun AppNavHost(
    appNavController: NavHostController = rememberNavController(),
    startDestination: AppRoute = SplashRoute
) {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionEntryPoint::class.java
        ).sessionManager()
    }

    LaunchedEffect(sessionManager) {
        sessionManager.sessionExpired.collect {
            appNavController.navigate(AuthRoutes.Login) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = appNavController,
        startDestination = startDestination
    ) {

        // ---------- Splash — auto-login check ----------
        composable<SplashRoute> {
            val tokenStorage = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    TokenStorageEntryPoint::class.java
                ).tokenStorage()
            }

            LaunchedEffect(Unit) {
                val accessToken = tokenStorage.getAccessToken()
                val role = tokenStorage.getUserRole()

                if (accessToken != null && role != null) {
                    // Sesja zapisana — przejdź do ekranu głównego
                    appNavController.navigate(MainRoutes.Main) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                } else {
                    // Brak sesji — ekran logowania
                    appNavController.navigate(AuthRoutes.Login) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            }

            // Minimalistyczny ekran ładowania widoczny przez ułamek sekundy
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // ---------- Auth ----------
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

        // ---------- Main + nested ----------
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
            resolutionsRoute = ResolutionRoutes.List,
            inspectionsRoute = InspectionRoutes.List,
            notificationsRoute = NotificationRoutes.Settings,
            nestedGraphs = { bottomNavController ->
                announcementsGraph(bottomNavController)
                financesGraph(bottomNavController)
                profileGraph(
                    navController = bottomNavController,
                    onNavigateToNotificationSettings = {
                        bottomNavController.navigate(NotificationRoutes.Settings)
                    },
                    onNavigateToCommunityLogo = {
                        bottomNavController.navigate(SettingsRoutes.CommunityLogo)
                    },
                    onNavigateToDocumentDistribution = {
                        bottomNavController.navigate(DocumentRoutes.Distribution)
                    },
                    onNavigateToInspections = {
                        bottomNavController.navigate(InspectionRoutes.List)
                    },
                    onNavigateToCategories = {
                        bottomNavController.navigate(CategoryRoutes.List)
                    },
                    onNavigateToFinances = {
                        bottomNavController.navigate(FinancesRoutes.Main)
                    },
                    onNavigateToAnnouncements = {
                        bottomNavController.navigate(AnnouncementsRoutes.Main)
                    }
                )
                ticketsGraph(bottomNavController)
                propertiesGraph(bottomNavController)
                categoriesGraph(bottomNavController)
                usersGraph(bottomNavController)
                resolutionsGraph(bottomNavController)
                inspectionsGraph(bottomNavController)
                metersGraph(bottomNavController)
                notificationsGraph(bottomNavController)
                settingsGraph(bottomNavController)
                documentsGraph(bottomNavController)
            }
        )
    }
}

