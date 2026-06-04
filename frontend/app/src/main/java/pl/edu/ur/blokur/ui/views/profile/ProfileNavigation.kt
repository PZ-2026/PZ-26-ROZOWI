package pl.edu.ur.blokur.ui.views.profile

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.profile.screens.ProfileScreen
import pl.edu.ur.blokur.ui.views.profile.viewmodels.ProfileViewModel

sealed interface ProfileRoutes : AppRoute {
    @Serializable
    data object Main : ProfileRoutes
}

fun NavGraphBuilder.profileGraph(
    navController: NavController,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToCommunityLogo: () -> Unit = {},
    onNavigateToDocumentDistribution: () -> Unit = {},
    onNavigateToInspections: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {}
) {
    composable<ProfileRoutes.Main> {
        val viewModel: ProfileViewModel = hiltViewModel()
        ProfileScreen(
            viewModel = viewModel,
            onNavigateToNotificationSettings = onNavigateToNotificationSettings,
            onNavigateToCommunityLogo = onNavigateToCommunityLogo,
            onNavigateToDocumentDistribution = onNavigateToDocumentDistribution,
            onNavigateToInspections = onNavigateToInspections,
            onNavigateToCategories = onNavigateToCategories
        )
    }
}
