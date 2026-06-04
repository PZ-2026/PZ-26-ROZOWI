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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.FloatingActionButton
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.Alignment
import pl.edu.ur.blokur.ui.views.tickets.components.TicketFilterPanel
import pl.edu.ur.blokur.ui.views.tickets.components.TicketListItem
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketsScreenEvent
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
            is TicketsListState.Error -> EmptyState(title = "Błąd", description = s.message)
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

                    // ── Panel wyszukiwania i filtrów ──
                    item {
                        TicketFilterPanel(
                            filterState = s.filterState,
                            totalCount = s.tickets.size,
                            filteredCount = s.tickets.size,
                            onFilterChanged = viewModel::onFilterChanged,
                            onRefresh = { viewModel.loadTickets(reset = true) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Lista zgłoszeń ──
                    if (s.tickets.isEmpty()) {
                        item {
                            EmptyState(title = "Brak wyników", description = "Nie znaleziono zgłoszeń dla podanych kryteriów.")
                        }
                    } else {
                        itemsIndexed(s.tickets) { index, ticket ->
                            val presentation = ticket.status.toPresentation()
                            val assignedTo = ticket.assignedToName
                            val dateOrAssignee = if (assignedTo != null)
                                "${ticket.createdAt.take(10)} • Przypisane: $assignedTo"
                            else
                                "${ticket.createdAt.take(10)} • Brak przypisania"

                            TicketListItem(
                                title = ticket.title,
                                date = dateOrAssignee,
                                categoryName = ticket.categoryName,
                                statusText = presentation.label,
                                statusColorHex = presentation.color.value.toLong(),
                                onClick = { viewModel.onTicketClicked(ticket.id) }
                            )

                            // Pagination check
                            if (index == s.tickets.lastIndex && !s.isFetchingNextPage && !s.hasReachedEnd) {
                                LaunchedEffect(ticket.id) {
                                    viewModel.loadNextPage()
                                }
                            }
                        }
                    }

                    if (s.isFetchingNextPage) {
                        item {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

