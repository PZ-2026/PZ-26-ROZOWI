package pl.edu.ur.blokur.presentation.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.presentation.PresentationRoutes

@Composable
fun SampleNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = PresentationRoutes.Sample,
        modifier = modifier
    ) {
        sampleNavGraph()
    }
}