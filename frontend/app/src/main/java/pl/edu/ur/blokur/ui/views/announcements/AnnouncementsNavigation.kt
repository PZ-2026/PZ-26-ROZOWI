package pl.edu.ur.blokur.ui.views.announcements

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import androidx.compose.runtime.remember
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.announcements.screens.AnnouncementsScreen
import pl.edu.ur.blokur.ui.views.announcements.screens.CreateAnnouncementScreen
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.CreateAnnouncementViewModel

sealed interface AnnouncementsRoutes : AppRoute {
    @Serializable
    data object Main : AnnouncementsRoutes

    @Serializable
    data object Create : AnnouncementsRoutes
}

fun NavGraphBuilder.announcementsGraph(navController: NavController) {
    composable<AnnouncementsRoutes.Main> {
        val viewModel: AnnouncementsViewModel = hiltViewModel()
        AnnouncementsScreen(
            viewModel = viewModel,
            onNavigateToCreate = { navController.navigate(AnnouncementsRoutes.Create) }
        )
    }

    composable<AnnouncementsRoutes.Create> {
        val viewModel: CreateAnnouncementViewModel = hiltViewModel()
        val mainEntry = remember(navController.currentBackStackEntry) {
            navController.getBackStackEntry(AnnouncementsRoutes.Main)
        }
        val mainViewModel: AnnouncementsViewModel = hiltViewModel(mainEntry)
        
        CreateAnnouncementScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
                mainViewModel.loadAnnouncements()
            }
        )
    }
}
