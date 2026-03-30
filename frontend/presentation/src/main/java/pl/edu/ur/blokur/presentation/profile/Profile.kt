package pl.edu.ur.blokur.presentation.profile

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.profile.screen.ProfileScreen
import pl.edu.ur.blokur.presentation.profile.viewmodel.ProfileViewModel

sealed interface ProfileRoutes : AppRoute {
    @Serializable
    data object Main : ProfileRoutes
}

fun NavGraphBuilder.profileGraph(
    navController: NavController
) {
    composable<ProfileRoutes.Main> {
        val viewModel: ProfileViewModel = hiltViewModel()
        ProfileScreen(viewModel)
    }
}