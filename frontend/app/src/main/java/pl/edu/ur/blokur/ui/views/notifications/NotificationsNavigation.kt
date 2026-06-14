package pl.edu.ur.blokur.ui.views.notifications

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.notifications.screens.NotificationsScreen
import pl.edu.ur.blokur.ui.views.notifications.screens.UserNotificationsScreen
import pl.edu.ur.blokur.ui.views.notifications.viewmodels.NotificationsViewModel
import pl.edu.ur.blokur.ui.views.notifications.viewmodels.UserNotificationsViewModel

sealed interface NotificationRoutes : AppRoute {
    @Serializable
    data object Settings : NotificationRoutes

    @Serializable
    data object UserSettings : NotificationRoutes
}

fun NavGraphBuilder.notificationsGraph(navController: NavController) {
    composable<NotificationRoutes.Settings> {
        val viewModel: NotificationsViewModel = hiltViewModel()
        NotificationsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<NotificationRoutes.UserSettings> {
        val viewModel: UserNotificationsViewModel = hiltViewModel()
        UserNotificationsScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
