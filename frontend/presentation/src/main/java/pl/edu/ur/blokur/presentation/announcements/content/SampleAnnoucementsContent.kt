package pl.edu.ur.blokur.presentation.announcements.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.announcements.util.AnnouncementsState
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme

@Composable
fun SampleAnnouncementsContent(
    state: AnnouncementsState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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

        when (state) {
            AnnouncementsState.Empty -> EmptyState(
                title = "Brak ogłoszeń",
                description = "Po podłączeniu danych tutaj pojawią się najnowsze komunikaty i aktualności."
            )
            AnnouncementsState.Loading -> LoadingIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnouncementsLoadingPreview() {
    PreviewTheme {
        SampleAnnouncementsContent(AnnouncementsState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
private fun AnnouncementsEmptyPreview() {
    PreviewTheme {
        SampleAnnouncementsContent(AnnouncementsState.Empty)
    }
}