package pl.edu.ur.blokur.ui.views.tickets.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.FloatingActionButton
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.views.tickets.components.TicketFilterPanel
import pl.edu.ur.blokur.ui.views.tickets.contents.TicketListContent
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToCategories: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val showFab = (state as? TicketsListState.Success)?.currentUserRole
        .let { it != "KONSERWATOR" }

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
        topBar = {
            val isManager = (state as? TicketsListState.Success)?.currentUserRole == "ZARZADCA"
            TopAppBar(
                title = {
                    Text(
                        "Zgłoszenia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (isManager) {
                        IconButton(onClick = onNavigateToCategories) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Zarządzaj kategoriami",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
                    Spacer(Modifier.height(4.dp))

                    // ── Panel wyszukiwania i filtrów ──
                    TicketFilterPanel(
                        filterState = s.filterState,
                        totalCount = s.allTickets.size,
                        filteredCount = s.filteredTickets.size,
                        onFilterChanged = viewModel::onFilterChanged,
                        onRefresh = viewModel::loadTickets,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // ── Lista zgłoszeń ──
                    if (s.filteredTickets.isEmpty()) {
                        val (title, desc) = if (s.allTickets.isEmpty()) {
                            "Brak zgłoszeń" to "Nie masz żadnych aktywnych zgłoszeń serwisowych."
                        } else {
                            "Brak wyników" to "Żadne zgłoszenie nie pasuje do wybranych filtrów."
                        }
                        EmptyState(title = title, description = desc)
                    } else {
                        TicketListContent(
                            tickets = s.filteredTickets,
                            onTicketClicked = viewModel::onTicketClicked
                        )
                    }

                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

