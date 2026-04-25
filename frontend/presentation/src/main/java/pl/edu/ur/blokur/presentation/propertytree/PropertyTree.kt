package pl.edu.ur.blokur.presentation.propertytree

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.presentation.common.AppRoute
import pl.edu.ur.blokur.presentation.propertytree.screen.PropertyTreeScreen
import pl.edu.ur.blokur.presentation.propertytree.viewmodel.PropertyTreeViewModel

/**
 * Trasy nawigacyjne modułu drzewa nieruchomości.
 */
sealed interface PropertyTreeRoutes : AppRoute {
    @Serializable
    data object Main : PropertyTreeRoutes
}

/**
 * Rejestruje ekran drzewa nieruchomości w grafie nawigacji.
 */
fun NavGraphBuilder.propertyTreeGraph(navController: NavController) {
    composable<PropertyTreeRoutes.Main> {
        val viewModel: PropertyTreeViewModel = hiltViewModel()
        PropertyTreeScreen(viewModel = viewModel)
    }
}
