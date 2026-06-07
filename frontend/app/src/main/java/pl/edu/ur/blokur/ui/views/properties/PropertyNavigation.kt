package pl.edu.ur.blokur.ui.views.properties

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.properties.screens.PropertyTreeScreen
import pl.edu.ur.blokur.ui.views.properties.viewmodels.PropertyTreeViewModel
import pl.edu.ur.blokur.ui.views.finances.FinancesRoutes
import pl.edu.ur.blokur.ui.views.meters.MeterRoutes

sealed interface PropertyRoutes : AppRoute {
    @Serializable
    data object Tree : PropertyRoutes
}

fun NavGraphBuilder.propertiesGraph(navController: NavController) {
    composable<PropertyRoutes.Tree> {
        val viewModel: PropertyTreeViewModel = hiltViewModel()
        PropertyTreeScreen(
            viewModel = viewModel,
            onNavigateToMeters = { apartmentId ->
                navController.navigate(MeterRoutes.List(apartmentId))
            },
            onNavigateToLedger = { apartmentId ->
                navController.navigate(FinancesRoutes.Ledger(apartmentId))
            }
        )
    }
}
