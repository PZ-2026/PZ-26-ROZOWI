package pl.edu.ur.blokur.ui.android.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pl.edu.ur.blokur.ui.android.resident.residentGraph

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ApplicationRoutes.Resident,
        modifier = modifier
    ) {
        residentGraph(navController)
    }
}