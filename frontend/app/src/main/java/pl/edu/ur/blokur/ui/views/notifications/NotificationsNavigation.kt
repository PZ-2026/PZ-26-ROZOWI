package pl.edu.ur.blokur.ui.views.notifications

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.notifications.screens.NotificationsScreen
import pl.edu.ur.blokur.ui.views.notifications.viewmodels.NotificationsViewModel

sealed interface NotificationRoutes : AppRoute {
    @Serializable
    data object Settings : NotificationRoutes
}

fun NavGraphBuilder.notificationsGraph(navController: NavController) {
    composable<NotificationRoutes.Settings> {
        val viewModel: NotificationsViewModel = hiltViewModel()
        NotificationsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
