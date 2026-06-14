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
import pl.edu.ur.blokur.ui.views.announcements.screens.EditAnnouncementScreen
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.CreateAnnouncementViewModel
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.EditAnnouncementViewModel

sealed interface AnnouncementsRoutes : AppRoute {
    @Serializable
    data object Main : AnnouncementsRoutes

    @Serializable
    data object Create : AnnouncementsRoutes

    @Serializable
    data class Edit(
        val id: String,
        val title: String,
        val content: String,
        val hasAttachment: Boolean = false
    ) : AnnouncementsRoutes
}

fun NavGraphBuilder.announcementsGraph(navController: NavController) {
    composable<AnnouncementsRoutes.Main> {
        val viewModel: AnnouncementsViewModel = hiltViewModel()
        AnnouncementsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToCreate = { navController.navigate(AnnouncementsRoutes.Create) },
            onNavigateToEdit = { announcement ->
                navController.navigate(
                    AnnouncementsRoutes.Edit(
                        id = announcement.id,
                        title = announcement.title,
                        content = announcement.content,
                        hasAttachment = announcement.hasAttachment
                    )
                )
            }
        )
    }

    composable<AnnouncementsRoutes.Create> {
        val viewModel: CreateAnnouncementViewModel = hiltViewModel()
        val mainEntry = remember(navController.currentBackStackEntry) {
            navController.previousBackStackEntry
        }
        val mainViewModel: AnnouncementsViewModel? = mainEntry?.let { hiltViewModel(it) }

        CreateAnnouncementScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
                mainViewModel?.loadAnnouncements()
            }
        )
    }

    composable<AnnouncementsRoutes.Edit> {
        val viewModel: EditAnnouncementViewModel = hiltViewModel()
        val mainEntry = remember(navController.currentBackStackEntry) {
            navController.previousBackStackEntry
        }
        val mainViewModel: AnnouncementsViewModel? = mainEntry?.let { hiltViewModel(it) }

        EditAnnouncementScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.popBackStack()
                mainViewModel?.loadAnnouncements()
            }
        )
    }
}
