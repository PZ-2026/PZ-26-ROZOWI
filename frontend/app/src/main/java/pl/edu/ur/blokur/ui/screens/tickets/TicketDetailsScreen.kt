package pl.edu.ur.blokur.ui.screens.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.data.mock.MockTickets
import pl.edu.ur.blokur.data.model.TicketDto
import pl.edu.ur.blokur.data.model.TicketStatus
import pl.edu.ur.blokur.ui.components.BlokurStatusBadge
import pl.edu.ur.blokur.ui.components.BlokurTagBadge
import pl.edu.ur.blokur.ui.screens.tickets.components.TicketTimeline
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    ticketId: Int,
    onNavigateBack: () -> Unit
) {
    val ticket = MockTickets.tickets.find { it.id == ticketId }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Szczegóły zgłoszenia",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Powrót",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "Nie znaleziono zgłoszenia", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        val (_, statusColor) = getStatusColor(ticket.status)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // HEADER SEKCJA
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tagi (Kategoria i Status)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BlokurStatusBadge(text = getStatusText(ticket.status), dotColor = statusColor)
                    BlokurTagBadge(text = ticket.category.name)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = ticket.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = ticket.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // METADATA SEKCJA (Zgłaszający, Lokalizacja)
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetadataRow(
                        icon = Icons.Rounded.Article,
                        label = "Numer zgłoszenia",
                        value = ticket.ticketNumber
                    )
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    
                    MetadataRow(
                        icon = Icons.Rounded.Person,
                        label = "Zgłaszający",
                        value = "${ticket.author.firstName} ${ticket.author.lastName}"
                    )
                    
                    val locationText = buildString {
                        ticket.building?.let { append("${it.address}\n") }
                        ticket.staircase?.let { append("Klatka: ${it.label} ") }
                        ticket.apartment?.let { append("Mieszkanie: ${it.number}") }
                    }
                    if (locationText.isNotBlank()) {
                        Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        MetadataRow(
                            icon = Icons.Rounded.Place,
                            label = "Lokalizacja",
                            value = locationText.trimEnd()
                        )
                    }
                    
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    MetadataRow(
                        icon = Icons.Rounded.CalendarToday,
                        label = "Data utworzenia",
                        value = formatDateTime(ticket.createdAt)
                    )
                }
            }

            // SEKCJA HISTORII (TIMELINE)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Historia zgłoszenia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TicketTimeline(
                    history = ticket.history,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetadataRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getStatusColor(status: TicketStatus): Pair<String, Color> {
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

private fun getStatusText(status: TicketStatus): String {
    return when (status) {
        TicketStatus.NOWE -> "Nowe"
        TicketStatus.ZAPLANOWANO -> "Zaplanowano"
        TicketStatus.W_REALIZACJI -> "W realizacji"
        TicketStatus.WSTRZYMANO -> "Wstrzymano"
        TicketStatus.ZAKONCZONE -> "Zakończone"
        TicketStatus.ZAMKNIETE -> "Zamknięte"
        TicketStatus.ODRZUCONE -> "Odrzucone"
    }
}

private fun formatDateTime(isoString: String): String {
    try {
        val parts = isoString.split("T")
        if (parts.size == 2) {
            val date = parts[0]
            val time = parts[1].substring(0, 5)
            return "$date, $time"
        }
    } catch (e: Exception) {}
    return isoString
}
