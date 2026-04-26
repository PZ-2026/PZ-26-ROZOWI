package pl.edu.ur.blokur.ui.views.announcements

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.announcements.screens.AnnouncementsScreen
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel

sealed interface AnnouncementsRoutes : AppRoute {
    @Serializable
    data object Main : AnnouncementsRoutes
}

fun NavGraphBuilder.announcementsGraph(navController: NavController) {
    composable<AnnouncementsRoutes.Main> {
        val viewModel: AnnouncementsViewModel = hiltViewModel()
        AnnouncementsScreen(viewModel = viewModel)
    }
}
