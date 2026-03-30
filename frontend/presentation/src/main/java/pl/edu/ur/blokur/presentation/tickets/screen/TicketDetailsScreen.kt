package pl.edu.ur.blokur.presentation.tickets.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.AppUser
import pl.edu.ur.blokur.domain.model.Ticket
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.component.StatusBadge
import pl.edu.ur.blokur.presentation.common.component.TagBadge
import pl.edu.ur.blokur.presentation.tickets.TicketDetailsEvent
import pl.edu.ur.blokur.presentation.tickets.TicketDetailsState
import pl.edu.ur.blokur.presentation.tickets.TicketDetailsViewModel
import pl.edu.ur.blokur.presentation.tickets.component.AssignConservatorSheet
import pl.edu.ur.blokur.presentation.tickets.component.ManagerRejectSheet
import pl.edu.ur.blokur.presentation.tickets.component.TicketTimeline
import pl.edu.ur.blokur.presentation.tickets.util.toPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    viewModel: TicketDetailsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TicketDetailsEvent.NavigateBack -> onNavigateBack()
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
        }
    ) { innerPadding ->
        when (val s = state) {
            is TicketDetailsState.Loading -> LoadingIndicator()
            is TicketDetailsState.Error -> EmptyState(title = "Błąd", description = s.message)
            is TicketDetailsState.Data -> TicketDetailsContent(
                ticket = s.ticket,
                conservators = s.availableConservators,
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
}

@Composable
private fun TicketDetailsContent(
    ticket: Ticket,
    conservators: List<AppUser>,
    modifier: Modifier = Modifier
) {
    val presentation = ticket.status.toPresentation()
    var showAssignSheet by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Spacer(modifier = Modifier.height(4.dp))

        // Tags
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(text = presentation.label, dotColor = presentation.color)
            TagBadge(text = ticket.category.name)
        }

        // Title + description
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Metadata card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetadataRow(Icons.Rounded.Article, "Numer zgłoszenia", ticket.ticketNumber)
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            MetadataRow(Icons.Rounded.Person, "Zgłaszający", ticket.author.fullName)
            val location = buildString {
                ticket.building?.let { append(it.address) }
                ticket.staircase?.let { append(" • Klatka ${it.label}") }
                ticket.apartment?.let { append(" • Mieszkanie ${it.number}") }
            }
            if (location.isNotBlank()) {
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                MetadataRow(Icons.Rounded.Place, "Lokalizacja", location)
            }
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            MetadataRow(Icons.Rounded.CalendarToday, "Data utworzenia", formatDateTime(ticket.createdAt))
        }

        // Timeline
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Historia zgłoszenia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TicketTimeline(history = ticket.history)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showAssignSheet) {
        AssignConservatorSheet(
            conservators = conservators,
            onDismissRequest = { showAssignSheet = false },
            onAssign = { showAssignSheet = false }
        )
    }
    if (showRejectSheet) {
        ManagerRejectSheet(
            onDismissRequest = { showRejectSheet = false },
            onSubmit = { showRejectSheet = false }
        )
    }
}

@Composable
private fun MetadataRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDateTime(iso: String): String = try {
    val parts = iso.split("T")
    if (parts.size == 2) "${parts[0]}, ${parts[1].take(5)}" else iso
} catch (_: Exception) { iso }
