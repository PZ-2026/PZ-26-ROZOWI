package pl.edu.ur.blokur.ui.views.settings

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.settings.screens.CommunityLogoScreen
import pl.edu.ur.blokur.ui.views.settings.viewmodels.CommunityLogoViewModel

sealed interface SettingsRoutes : AppRoute {
    @Serializable
    data object CommunityLogo : SettingsRoutes
}

fun NavGraphBuilder.settingsGraph(navController: NavController) {
    composable<SettingsRoutes.CommunityLogo> {
        val viewModel: CommunityLogoViewModel = hiltViewModel()
        CommunityLogoScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
