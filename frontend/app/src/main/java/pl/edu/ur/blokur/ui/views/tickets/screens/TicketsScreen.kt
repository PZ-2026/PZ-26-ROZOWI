package pl.edu.ur.blokur.ui.views.tickets.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.FloatingActionButton
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import pl.edu.ur.blokur.ui.views.tickets.components.TicketFilterPanel
import pl.edu.ur.blokur.ui.views.tickets.components.TicketListItem
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
import pl.edu.ur.blokur.ui.utils.PolishFormat
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketsViewModel

@Composable
fun TicketsScreen(
    viewModel: TicketsViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToCategories: () -> Unit = {},
    onNavigateToUsers: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val showFab = (state as? TicketsListState.Success)?.currentUserRole == "MIESZKANIEC"

    LaunchedEffect(Unit) {
        viewModel.loadTickets()
        viewModel.events.collect { event ->
            when (event) {
                is TicketsScreenEvent.NavigateToDetails -> onNavigateToDetails(event.ticketId)
                is TicketsScreenEvent.NavigateToCreate -> onNavigateToCreate()
                is TicketsScreenEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    icon = Icons.Rounded.Add,
                    contentDescription = "Utwórz zgłoszenie",
                    onClick = viewModel::onCreateTicketClicked
                )
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            is TicketsListState.Loading -> LoadingIndicator()
            is TicketsListState.Error -> EmptyState(
                title = "Błąd",
                description = s.message,
                onRetry = viewModel::loadTickets
            )
            is TicketsListState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    item {
                        TicketFilterPanel(
                            filterState = s.filterState,
                            filterOptions = s.filterOptions,
                            currentUserRole = s.currentUserRole,
                            totalCount = s.tickets.size,
                            filteredCount = s.tickets.size,
                            onFilterChanged = viewModel::onFilterChanged,
                            onRefresh = { viewModel.loadTickets() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (s.tickets.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            ) {
                                val hasFilters = s.filterState.hasActiveFilters()
                                EmptyState(
                                    title = if (hasFilters) "Brak wyników" else "Brak zgłoszeń",
                                    description = if (hasFilters)
                                        "Nie znaleziono zgłoszeń dla wybranych kryteriów. Spróbuj zmienić filtry."
                                    else
                                        "Gdy pojawią się nowe zgłoszenia, zobaczysz je tutaj."
                                )
                            }
                        }
                    } else {
                        items(s.tickets, key = { it.id }) { ticket ->
                            val presentation = ticket.status.toPresentation()
                            val assignedTo = ticket.assignedToName
                            val formattedDate = PolishFormat.formatDate(ticket.createdAt)
                            val dateOrAssignee = if (assignedTo != null)
                                "$formattedDate • Przypisane: $assignedTo"
                            else
                                "$formattedDate • Brak przypisania"

                            TicketListItem(
                                title = ticket.title,
                                date = dateOrAssignee,
                                categoryName = ticket.categoryName,
                                statusText = presentation.label,
                                statusColorHex = presentation.color.value.toLong(),
                                onClick = { viewModel.onTicketClicked(ticket.id) }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}
