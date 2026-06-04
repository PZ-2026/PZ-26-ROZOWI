package pl.edu.ur.blokur.ui.views.tickets.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.tickets.contents.TicketDetailsContent
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsScreenEvent
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.TicketDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    viewModel: TicketDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TicketDetailsScreenEvent.NavigateBack -> onNavigateBack()
                is TicketDetailsScreenEvent.AssignConservator ->
                    snackbarHostState.showSnackbar("Przypisano konserwatora na: ${event.scheduledAt}")
                is TicketDetailsScreenEvent.RejectTicket ->
                    snackbarHostState.showSnackbar("Zgłoszenie odrzucone")
                is TicketDetailsScreenEvent.ConservatorAction ->
                    snackbarHostState.showSnackbar("Zaktualizowano status zgłoszenia")
                is TicketDetailsScreenEvent.ShowSnackbar ->
                    snackbarHostState.showSnackbar(event.message)
                is TicketDetailsScreenEvent.ShowError ->
                    snackbarHostState.showSnackbar("Błąd: ${event.message}")
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły zgłoszenia", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        TicketDetailsContent(
            state = state,
            onAssignConservator = { conservator, scheduledAt ->
                viewModel.onAssignConservator(conservator.id, scheduledAt)
            },
            onRejectTicket = viewModel::onRejectTicket,
            onConservatorAction = viewModel::onConservatorAction,
            onAddComment = viewModel::addComment,
            onDeleteImage = viewModel::deleteImage,
            onDownloadProtocol = viewModel::downloadWorkAcceptanceProtocol,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        )
    }
}
