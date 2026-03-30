package pl.edu.ur.blokur.presentation.tickets.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.FloatingActionButton
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.component.TopBar
import pl.edu.ur.blokur.presentation.tickets.util.TicketsListState
import pl.edu.ur.blokur.presentation.tickets.util.TicketsScreenEvent
import pl.edu.ur.blokur.presentation.tickets.content.TicketListContent
import pl.edu.ur.blokur.presentation.tickets.viewmodel.TicketsViewModel

@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
    onNavigateToDetails: (Int) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
            FloatingActionButton(text = "+", onClick = viewModel::onCreateTicketClicked)
        }
    ) { innerPadding ->
        when (val s = state) {
            is TicketsListState.Loading -> LoadingIndicator()
            is TicketsListState.Error -> EmptyState(title = "Błąd", description = s.message)
            is TicketsListState.Success -> {
                if (s.tickets.isEmpty()) {
                    EmptyState(
                        title = "Brak zgłoszeń",
                        description = "Nie masz żadnych aktywnych zgłoszeń serwisowych."
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TicketListContent(
                            tickets = s.tickets,
                            onTicketClicked = viewModel::onTicketClicked
                        )
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
