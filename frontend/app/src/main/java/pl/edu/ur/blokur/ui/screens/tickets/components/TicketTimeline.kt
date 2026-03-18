package pl.edu.ur.blokur.ui.screens.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.data.model.TicketHistoryDto
import pl.edu.ur.blokur.data.model.TicketStatus
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange

@Composable
fun TicketTimeline(
    history: List<TicketHistoryDto>,
    modifier: Modifier = Modifier
) {
    val sortedHistory = history.reversed()

    Column(modifier = modifier) {
        sortedHistory.forEachIndexed { index, item ->
            val isLast = index == sortedHistory.size - 1
            val (statusText, statusColor) = getStatusPresentation(item.status)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // Wymaga wysokości na podstawie zawartości dla linii pionowej
            ) {
                // Lewa strona - linia czasu (kółko + pionowa gruba linia)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(statusColor, CircleShape)
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxHeight() // Linia dociągnięta do dołu wiersza
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Prawa strona - treść wydarzenia w czystym stylu trackingu
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (index == 0) statusColor else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = formatDateTime(item.createdAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "Osoba: ${item.changedBy.firstName} ${item.changedBy.lastName} (${item.changedBy.role.lowercase().replaceFirstChar { it.uppercase() }})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!item.comment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getStatusPresentation(status: TicketStatus): Pair<String, Color> {
    return when (status) {
        TicketStatus.NOWE -> "Nowe zgłoszenie" to InfoBlue
        TicketStatus.ZAPLANOWANO -> "Zaplanowano" to InfoBlue
        TicketStatus.W_REALIZACJI -> "W realizacji" to WarningOrange
        TicketStatus.WSTRZYMANO -> "Wstrzymano" to WarningOrange
        TicketStatus.ZAKONCZONE -> "Zakończone - Do weryfikacji" to SuccessGreen
        TicketStatus.ZAMKNIETE -> "Zamknięte" to SuccessGreen
        TicketStatus.ODRZUCONE -> "Odrzucone" to ErrorRed
    }
}

private fun formatDateTime(isoString: String): String {
    // Prosty formatter, w produkcji lepiej użyć java.time.format.DateTimeFormatter
    try {
        val parts = isoString.split("T")
        if (parts.size == 2) {
            val date = parts[0]
            val time = parts[1].substring(0, 5)
            return "$date, $time"
        }
    } catch (e: Exception) {
        // Ignoruj i zwracaj oryginalny string w razie błędu parsowania
    }
    return isoString
}
