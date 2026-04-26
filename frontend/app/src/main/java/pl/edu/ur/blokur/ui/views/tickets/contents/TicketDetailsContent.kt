package pl.edu.ur.blokur.ui.views.tickets.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.AppUserDto
import pl.edu.ur.blokur.dtos.TicketDto
import pl.edu.ur.blokur.dtos.TicketStatus
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.StatusBadge
import pl.edu.ur.blokur.ui.components.TagBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.PreviewTheme
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.views.tickets.components.AssignConservatorSheet
import pl.edu.ur.blokur.ui.views.tickets.components.ConservatorActionSheet
import pl.edu.ur.blokur.ui.views.tickets.components.ManagerRejectSheet
import pl.edu.ur.blokur.ui.views.tickets.components.TicketTimeline
import pl.edu.ur.blokur.ui.views.tickets.utils.ConservatorActionType
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation

@Composable
fun TicketDetailsContent(
    state: TicketDetailsListState,
    onAssignConservator: (AppUserDto, String) -> Unit,
    onRejectTicket: (String) -> Unit,
    onConservatorAction: (ConservatorActionType, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is TicketDetailsListState.Loading -> LoadingIndicator()
        is TicketDetailsListState.Error -> EmptyState(title = "Błąd", description = state.message)
        is TicketDetailsListState.Success -> TicketDetailsSuccessContent(
            ticket = state.ticket,
            conservators = state.availableConservators,
            currentUserRole = state.currentUserRole,
            onAssignConservator = onAssignConservator,
            onRejectTicket = onRejectTicket,
            onConservatorAction = onConservatorAction,
            modifier = modifier
        )
    }
}

@Composable
private fun TicketDetailsSuccessContent(
    ticket: TicketDto,
    conservators: List<AppUserDto>,
    currentUserRole: String,
    onAssignConservator: (AppUserDto, String) -> Unit,
    onRejectTicket: (String) -> Unit,
    onConservatorAction: (ConservatorActionType, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val presentation = ticket.status.toPresentation()

    var showAssignSheet by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }
    var conservatorActionType by remember { mutableStateOf<ConservatorActionType?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(text = presentation.label, dotColor = presentation.color)
                TagBadge(text = ticket.category.name)
            }

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
                ticket.assignedTo?.let {
                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.Person, "Przypisany konserwator", it.fullName)
                }
                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                MetadataRow(Icons.Rounded.CalendarToday, "Data utworzenia", formatDateTime(ticket.createdAt))
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Historia zgłoszenia",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TicketTimeline(history = ticket.history)
            }

            Spacer(Modifier.height(100.dp))
        }

        // ── Kontekstowe FABs zależne od roli i statusu ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            when (currentUserRole) {
                "ADMINISTRATOR" -> {
                    when (ticket.status) {
                        TicketStatus.NOWE -> {
                            TicketFab(
                                icon = Icons.Rounded.Close,
                                contentDescription = "Odrzuć zgłoszenie",
                                containerColor = ErrorRed,
                                onClick = { showRejectSheet = true }
                            )
                            TicketFab(
                                icon = Icons.Rounded.Person,
                                contentDescription = "Przypisz konserwatora",
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = { showAssignSheet = true }
                            )
                        }
                        TicketStatus.WSTRZYMANO -> {
                            TicketFab(
                                icon = Icons.Rounded.PlayArrow,
                                contentDescription = "Wznów zgłoszenie",
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = { showAssignSheet = true }
                            )
                        }
                        else -> Unit
                    }
                }
                "KONSERWATOR" -> {
                    when (ticket.status) {
                        TicketStatus.ZAPLANOWANO -> {
                            TicketFab(
                                icon = Icons.Rounded.PlayArrow,
                                contentDescription = "Rozpocznij realizację",
                                containerColor = MaterialTheme.colorScheme.primary,
                                onClick = { conservatorActionType = ConservatorActionType.START }
                            )
                        }
                        TicketStatus.W_REALIZACJI, TicketStatus.WSTRZYMANO -> {
                            TicketFab(
                                icon = Icons.Rounded.Pause,
                                contentDescription = "Wstrzymaj / Komentarz",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                onClick = { conservatorActionType = ConservatorActionType.PAUSE_OR_COMMENT }
                            )
                            TicketFab(
                                icon = Icons.Rounded.CheckCircle,
                                contentDescription = "Zakończ pracę",
                                containerColor = SuccessGreen,
                                onClick = { conservatorActionType = ConservatorActionType.FINISH }
                            )
                        }
                        else -> Unit
                    }
                }
                else -> Unit
            }
        }
    }

    if (showAssignSheet) {
        AssignConservatorSheet(
            conservators = conservators,
            onDismissRequest = { showAssignSheet = false },
            onAssign = { conservator, scheduledAt ->
                showAssignSheet = false
                onAssignConservator(conservator, scheduledAt)
            }
        )
    }

    if (showRejectSheet) {
        ManagerRejectSheet(
            onDismissRequest = { showRejectSheet = false },
            onSubmit = { reason ->
                showRejectSheet = false
                onRejectTicket(reason)
            }
        )
    }

    conservatorActionType?.let { type ->
        ConservatorActionSheet(
            actionType = type,
            onDismissRequest = { conservatorActionType = null },
            onSubmit = { comment, pause ->
                conservatorActionType = null
                onConservatorAction(type, comment, pause)
            }
        )
    }
}

@Composable
private fun TicketFab(
    icon: ImageVector,
    contentDescription: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(4.dp)
    ) {
        Icon(icon, contentDescription = contentDescription)
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
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
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

@Preview(showBackground = true)
@Composable
private fun TicketDetailsLoadingPreview() {
    PreviewTheme { TicketDetailsContent(TicketDetailsListState.Loading, { _, _ -> }, {}, { _, _, _ -> }) }
}

@Preview(showBackground = true)
@Composable
private fun TicketDetailsErrorPreview() {
    PreviewTheme { TicketDetailsContent(TicketDetailsListState.Error("Nie znaleziono zgłoszenia"), { _, _ -> }, {}, { _, _, _ -> }) }
}
