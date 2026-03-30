package pl.edu.ur.blokur.presentation.sample

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import pl.edu.ur.blokur.presentation.PresentationRoutes

fun NavGraphBuilder.sampleNavGraph() {
    composable<PresentationRoutes.Sample> {
        val viewModel: SampleViewModel = hiltViewModel()
        SampleScreen(viewModel)
    }
}
