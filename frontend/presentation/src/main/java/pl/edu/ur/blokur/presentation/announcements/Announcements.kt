package pl.edu.ur.blokur.presentation.announcements

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.announcements.screen.AnnouncementsScreen
import pl.edu.ur.blokur.presentation.announcements.viewmodel.AnnouncementsViewModel
import pl.edu.ur.blokur.presentation.common.AppRoute

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
