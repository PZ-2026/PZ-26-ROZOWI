package pl.edu.ur.blokur.ui.views.meters

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pl.edu.ur.blokur.ui.navigation.AppRoute
import pl.edu.ur.blokur.ui.views.meters.screens.MeterDetailScreen
import pl.edu.ur.blokur.ui.views.meters.screens.MeterListScreen
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterDetailViewModel
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterListViewModel

sealed interface MeterRoutes : AppRoute {
    @Serializable
    data class List(val apartmentId: String) : MeterRoutes

    @Serializable
    data class Detail(
        val apartmentId: String,
        val meterId: String,
        val serialNumber: String,
        val mediumType: String
    ) : MeterRoutes
}

fun NavGraphBuilder.metersGraph(navController: NavController) {
    composable<MeterRoutes.List> { backStackEntry ->
        val args = backStackEntry.toRoute<MeterRoutes.List>()
        val viewModel: MeterListViewModel = hiltViewModel()
        MeterListScreen(
            viewModel = viewModel,
            onNavigateToDetail = { meterId, serialNumber, mediumType ->
                navController.navigate(MeterRoutes.Detail(
                    apartmentId = args.apartmentId,
                    meterId = meterId,
                    serialNumber = serialNumber,
                    mediumType = mediumType
                ))
            },
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable<MeterRoutes.Detail> {
        val viewModel: MeterDetailViewModel = hiltViewModel()
        MeterDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
