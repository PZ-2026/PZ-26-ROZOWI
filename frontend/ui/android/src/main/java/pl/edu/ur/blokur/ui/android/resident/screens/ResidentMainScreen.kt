package pl.edu.ur.blokur.ui.android.resident.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.edu.ur.blokur.ui.android.resident.viewmodels.MainViewModel

@Composable
fun ResidentMainScreen(
    viewModel: MainViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->

        }
    }

    Text("RESIDENTSCREEN")

    //contents
}