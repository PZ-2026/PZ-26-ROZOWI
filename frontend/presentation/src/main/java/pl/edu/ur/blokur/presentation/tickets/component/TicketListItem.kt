package pl.edu.ur.blokur.presentation.tickets.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.StatusBadge
import pl.edu.ur.blokur.presentation.common.component.TagBadge
import pl.edu.ur.blokur.presentation.tickets.util.statusPresentation

@Composable
fun TicketListItem(
    title: String,
    date: String,
    categoryName: String,
    statusText: String,
    statusColorHex: Long,
    onClick: () -> Unit
) {
    val (_, color) = statusPresentation(statusText, statusColorHex)

    NormalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TagBadge(text = categoryName)
                StatusBadge(text = statusText, dotColor = color)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
