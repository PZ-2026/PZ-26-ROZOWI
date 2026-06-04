package pl.edu.ur.blokur.ui.views.announcements.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.announcements.contents.SampleAnnouncementsContent
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsEvent
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.AnnouncementsViewModel

@Composable
fun AnnouncementsScreen(
    viewModel: AnnouncementsViewModel,
    onNavigateToCreate: () -> Unit = {},
    onNavigateToEdit: (pl.edu.ur.blokur.dtos.AnnouncementDto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val isManager by viewModel.isManager.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AnnouncementsEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    androidx.compose.material3.Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isManager) {
                androidx.compose.material3.FloatingActionButton(onClick = onNavigateToCreate) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Dodaj ogłoszenie"
                    )
                }
            }
        }
    ) { paddingValues ->
        SampleAnnouncementsContent(
            state = state,
            isManager = isManager,
            onDownloadAttachment = viewModel::downloadAttachment,
            onEditAnnouncement = onNavigateToEdit,
            onDeleteAnnouncement = viewModel::deleteAnnouncement,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        )
    }
}
