package pl.edu.ur.blokur.ui.views.announcements.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsState
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.NormalCard
import pl.edu.ur.blokur.ui.theme.PreviewTheme

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

        when (state) {
            AnnouncementsState.Loading -> LoadingIndicator()
            AnnouncementsState.Empty -> EmptyState(
                title = "Brak ogłoszeń",
                description = "Nie ma żadnych aktualnych komunikatów i aktualności."
            )
            is AnnouncementsState.Error -> EmptyState(
                title = "Błąd ładowania",
                description = state.message
            )
            is AnnouncementsState.Success -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.announcements, key = { it.id }) { announcement ->
                    NormalCard {
                        Text(
                            text = announcement.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!announcement.authorName.isNullOrBlank()) {
                            Text(
                                text = announcement.authorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = announcement.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (announcement.hasAttachment) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📎 Ogłoszenie zawiera załącznik PDF",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
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