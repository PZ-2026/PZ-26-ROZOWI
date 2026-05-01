package pl.edu.ur.blokur.ui.views.tickets.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.FloatingActionButton
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.NormalCard
import pl.edu.ur.blokur.ui.components.StatusBadge
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange
import pl.edu.ur.blokur.ui.views.tickets.contents.TicketListContent
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketsViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val showFab = (state as? TicketsListState.Success)?.currentUserRole != "KONSERWATOR"

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TicketsScreenEvent.NavigateToDetails -> onNavigateToDetails(event.ticketId)
                is TicketsScreenEvent.NavigateToCreate -> onNavigateToCreate()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Zgłoszenia") },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(text = "+", onClick = viewModel::onCreateTicketClicked)
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            is TicketsListState.Loading -> LoadingIndicator()
            is TicketsListState.Error -> EmptyState(title = "Błąd", description = s.message)
            is TicketsListState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    if (s.tickets.isEmpty()) {
                        EmptyState(
                            title = "Brak zgłoszeń",
                            description = "Nie masz żadnych aktywnych zgłoszeń serwisowych."
                        )
                    } else {
                        TicketListContent(
                            tickets = s.tickets,
                            onTicketClicked = viewModel::onTicketClicked
                        )
                    }

                    NormalCard {
                        Text(
                            text = "Statusy zgłoszeń",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Najczęściej używane statusy w systemie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusBadge("Nowe", InfoBlue)
                            StatusBadge("Zaplanowano", InfoBlue)
                            StatusBadge("W realizacji", WarningOrange)
                            StatusBadge("Wstrzymano", WarningOrange)
                            StatusBadge("Do weryfikacji", SuccessGreen)
                            StatusBadge("Zamknięte", SuccessGreen)
                            StatusBadge("Odrzucone", ErrorRed)
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}
