package pl.edu.ur.blokur.ui.views.announcements.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.announcements.contents.SampleAnnouncementsContent
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel
import pl.edu.ur.blokur.ui.components.TopBar

@Composable
fun AnnouncementsScreen(viewModel: AnnouncementsViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Ogłoszenia") }
    ) { innerPadding ->
        SampleAnnouncementsContent(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )
    }
}
