package pl.edu.ur.blokur.ui.screens.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurTicketItem
import pl.edu.ur.blokur.ui.components.BlokurFab
import pl.edu.ur.blokur.ui.components.BlokurHighlightCard
import pl.edu.ur.blokur.ui.components.BlokurPrimaryButton
import pl.edu.ur.blokur.ui.components.BlokurStatusBadge
import pl.edu.ur.blokur.ui.components.BlokurTagBadge
import pl.edu.ur.blokur.ui.components.BlokurTopBar
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange
import pl.edu.ur.blokur.data.mock.MockTickets
import pl.edu.ur.blokur.data.model.TicketStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketsScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BlokurTopBar(title = "Zgłoszenia")
        },
        floatingActionButton = {
            BlokurFab(
                onClick = { },
                text = "+"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

/*
            BlokurHighlightCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Masz 3 aktywne zgłoszenia",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Śledź status spraw, dodawaj nowe zgłoszenia i sprawdzaj odpowiedzi administracji.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                BlokurPrimaryButton(
                    text = "Dodaj nowe zgłoszenie",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
            */

            MockTickets.tickets.forEach { ticket ->
                val (statusText, statusColor) = when (ticket.status) {
                    TicketStatus.NOWE -> "Nowe" to InfoBlue
                    TicketStatus.ZAPLANOWANO -> "Zaplanowano" to InfoBlue
                    TicketStatus.W_REALIZACJI -> "W realizacji" to WarningOrange
                    TicketStatus.WSTRZYMANO -> "Wstrzymano" to WarningOrange
                    TicketStatus.ZAKONCZONE -> "Do weryfikacji" to SuccessGreen
                    TicketStatus.ZAMKNIETE -> "Zamknięte" to SuccessGreen
                    TicketStatus.ODRZUCONE -> "Odrzucone" to ErrorRed
                }

                // Temporary logic to format assignee for the ticket.
                // Depending on requirements, we can show assignee name or author name
                val dateOrAssignee = if (ticket.assignedTo != null) {
                    "${ticket.createdAt.substring(0, 10)} • Przypisane: ${ticket.assignedTo.firstName} ${ticket.assignedTo.lastName}"
                } else {
                    "${ticket.createdAt.substring(0, 10)} • Brak przypisania"
                }

                BlokurTicketItem(
                    title = ticket.title,
                    date = dateOrAssignee,
                    categoryText = ticket.category.name,
                    statusText = statusText,
                    statusColor = statusColor,
                    onClick = {}
                )
            }

            BlokurCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Statusy",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Najczęściej używane statusy zgłoszeń w systemie.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BlokurStatusBadge("Nowe", InfoBlue)
                    BlokurStatusBadge("Zaplanowano", InfoBlue)
                    BlokurStatusBadge("W realizacji", WarningOrange)
                    BlokurStatusBadge("Wstrzymano", WarningOrange)
                    BlokurStatusBadge("Do weryfikacji", SuccessGreen)
                    BlokurStatusBadge("Zamknięte", SuccessGreen)
                    BlokurStatusBadge("Odrzucone", ErrorRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketsScreenPreview() {
    BlokurPreviewTheme {
        TicketsScreen()
    }
}