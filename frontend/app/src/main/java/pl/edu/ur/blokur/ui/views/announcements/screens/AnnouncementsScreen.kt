package pl.edu.ur.blokur.ui.views.announcements.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.announcements.contents.SampleAnnouncementsContent
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel

@Composable
fun AnnouncementsScreen(
    viewModel: AnnouncementsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    SampleAnnouncementsContent(
        state = state,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    )
}

