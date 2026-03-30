package pl.edu.ur.blokur.presentation.announcements.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.announcements.viewmodel.AnnouncementsViewModel
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.TopBar
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme

@Composable
fun AnnouncementsScreen(viewModel: AnnouncementsViewModel) {
    val state by viewModel.state.collectAsState()
    AnnouncementsContent()
}

@Composable
private fun AnnouncementsContent() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Ogłoszenia") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            NormalCard {
                Text(
                    text = "Tablica ogłoszeń",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "W tym module będą wyświetlane komunikaty administracji, wydarzenia i ważne informacje dla mieszkańców.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            EmptyState(
                title = "Brak ogłoszeń",
                description = "Po podłączeniu danych tutaj pojawią się najnowsze komunikaty i aktualności."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnouncementsScreenPreview() {
    PreviewTheme { AnnouncementsContent() }
}