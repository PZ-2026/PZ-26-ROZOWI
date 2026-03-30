package pl.edu.ur.blokur.presentation.announcements.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pl.edu.ur.blokur.presentation.announcements.content.SampleAnnouncementsContent
import pl.edu.ur.blokur.presentation.announcements.viewmodel.AnnouncementsViewModel

@Composable
fun AnnouncementsScreen(viewModel: AnnouncementsViewModel) {
    val state by viewModel.state.collectAsState()
    SampleAnnouncementsContent()
}
