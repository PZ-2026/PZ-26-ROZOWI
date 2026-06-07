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
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import pl.edu.ur.blokur.dtos.ConservatorDto
import pl.edu.ur.blokur.dtos.TicketDetailDto
import pl.edu.ur.blokur.dtos.TicketCommentDto
import pl.edu.ur.blokur.dtos.TicketImageDto
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
import pl.edu.ur.blokur.ui.views.tickets.components.TicketCommentsSection
import pl.edu.ur.blokur.ui.views.tickets.components.TicketImagesSection
import pl.edu.ur.blokur.ui.views.tickets.utils.ConservatorActionType
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsListState
import pl.edu.ur.blokur.ui.utils.PolishFormat
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation

@Composable
fun TicketDetailsContent(
    state: TicketDetailsListState,
    onAssignConservator: (ConservatorDto, String) -> Unit,
    onRejectTicket: (String) -> Unit,
    onConservatorAction: (ConservatorActionType, String, Boolean) -> Unit,
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onAddAfterPhoto: () -> Unit = {},
    onResumeTicket: () -> Unit = {},
    onDownloadProtocol: () -> Unit = {},
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (state) {
        is TicketDetailsListState.Loading -> LoadingIndicator()
        is TicketDetailsListState.Error -> EmptyState(
            title = "Błąd",
            description = state.message,
            onRetry = onRetry
        )
        is TicketDetailsListState.Success -> TicketDetailsSuccessContent(
            ticket = state.ticket,
            conservators = state.availableConservators,
            currentUserRole = state.currentUserRole,
            comments = state.comments,
            images = state.images,
            isLoadingComments = state.isLoadingComments,
            isLoadingImages = state.isLoadingImages,
            isSendingComment = state.isSendingComment,
            commentResetKey = state.commentResetKey,
            isUploadingImage = state.isUploadingImage,
            isDownloadingProtocol = state.isDownloadingProtocol,
            isActionInProgress = state.isActionInProgress,
            onAssignConservator = onAssignConservator,
            onRejectTicket = onRejectTicket,
            onConservatorAction = onConservatorAction,
            onAddComment = onAddComment,
            onAddAfterPhoto = onAddAfterPhoto,
            onResumeTicket = onResumeTicket,
            onDownloadProtocol = onDownloadProtocol,
            modifier = modifier
        )
    }
}

@Composable
private fun TicketDetailsSuccessContent(
    ticket: TicketDetailDto,
    conservators: List<ConservatorDto>,
    currentUserRole: String,
    comments: List<TicketCommentDto>,
    images: List<TicketImageDto>,
    isLoadingComments: Boolean,
    isLoadingImages: Boolean,
    isSendingComment: Boolean,
    commentResetKey: Int,
    isUploadingImage: Boolean,
    isDownloadingProtocol: Boolean,
    isActionInProgress: Boolean,
    onAssignConservator: (ConservatorDto, String) -> Unit,
    onRejectTicket: (String) -> Unit,
    onConservatorAction: (ConservatorActionType, String, Boolean) -> Unit,
    onAddComment: (String, String) -> Unit,
    onAddAfterPhoto: () -> Unit,
    onResumeTicket: () -> Unit,
    onDownloadProtocol: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val presentation = ticket.status.toPresentation()

    var showAssignSheet by remember { mutableStateOf(false) }
    var showRejectSheet by remember { mutableStateOf(false) }
    var showResumeDialog by remember { mutableStateOf(false) }
    var conservatorActionType by remember { mutableStateOf<ConservatorActionType?>(null) }

    val canUploadAfter = currentUserRole == "KONSERWATOR" && ticket.status in listOf(
        TicketStatus.W_REALIZACJI,
        TicketStatus.WSTRZYMANO,
        TicketStatus.ZAKONCZONE_DO_WERYFIKACJI
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(text = presentation.label, dotColor = presentation.color)
                TagBadge(text = ticket.categoryName)
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
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                MetadataRow(Icons.Rounded.Person, "Zgłaszający", ticket.authorName)

                ticket.locationLabel?.takeIf { it.isNotBlank() }?.let { location ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.Place, "Lokalizacja", location)
                }

                ticket.assignedToName?.let {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.Person, "Przypisany konserwator", it)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                MetadataRow(Icons.Rounded.CalendarToday, "Data utworzenia", formatDateTime(ticket.createdAt))

                ticket.updatedAt?.let {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.CalendarToday, "Ostatnia aktualizacja", formatDateTime(it))
                }

                ticket.closedAt?.let {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.CheckCircle, "Data zamknięcia", formatDateTime(it))
                }

                ticket.plannedVisitAt?.let {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    MetadataRow(Icons.Rounded.CalendarToday, "Planowana wizyta", formatDateTime(it))
                }
            }

            ticket.internalNote?.takeIf { it.isNotBlank() }?.let { note ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Notatka wewnętrzna",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            TicketImagesSection(
                images = images,
                isLoading = isLoadingImages,
                isUploading = isUploadingImage,
                showUploadAfter = canUploadAfter,
                onAddAfterPhoto = onAddAfterPhoto,
                modifier = Modifier.fillMaxWidth()
            )

            TicketCommentsSection(
                comments = comments,
                currentRole = currentUserRole,
                isLoading = isLoadingComments,
                isSending = isSendingComment,
                commentResetKey = commentResetKey,
                onAddComment = onAddComment,
                modifier = Modifier.fillMaxWidth()
            )

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
                "ZARZADCA" -> {
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
                                onClick = { showResumeDialog = true }
                            )
                        }
                        TicketStatus.ZAKONCZONE_DO_WERYFIKACJI -> {
                            TicketFab(
                                icon = Icons.Rounded.CheckCircle,
                                contentDescription = "Zatwierdź i zamknij",
                                containerColor = SuccessGreen,
                                onClick = { conservatorActionType = ConservatorActionType.CLOSE_VERIFICATION }
                            )
                        }
                        TicketStatus.ZAMKNIETE -> {
                            TicketFab(
                                icon = Icons.Rounded.PictureAsPdf,
                                contentDescription = "Pobierz protokół odbioru",
                                containerColor = MaterialTheme.colorScheme.secondary,
                                isLoading = isDownloadingProtocol,
                                onClick = onDownloadProtocol
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
                        TicketStatus.ZAMKNIETE -> {
                            TicketFab(
                                icon = Icons.Rounded.PictureAsPdf,
                                contentDescription = "Pobierz protokół odbioru",
                                containerColor = MaterialTheme.colorScheme.secondary,
                                isLoading = isDownloadingProtocol,
                                onClick = onDownloadProtocol
                            )
                        }
                        else -> Unit
                    }
                }
                else -> Unit
            }
        }
    }

    if (showResumeDialog) {
        AlertDialog(
            onDismissRequest = { showResumeDialog = false },
            title = { Text("Wznów zgłoszenie") },
            text = { Text("Czy na pewno chcesz wznowić realizację tego zgłoszenia? Konserwator pozostaje bez zmian.") },
            confirmButton = {
                TextButton(onClick = {
                    showResumeDialog = false
                    onResumeTicket()
                }) {
                    Text("Wznów")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResumeDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    var lastActionInProgress by remember { mutableStateOf(false) }
    LaunchedEffect(isActionInProgress) {
        if (lastActionInProgress && !isActionInProgress) {
            showAssignSheet = false
            showRejectSheet = false
            conservatorActionType = null
        }
        lastActionInProgress = isActionInProgress
    }

    if (showAssignSheet) {
        AssignConservatorSheet(
            conservators = conservators,
            isLoading = isActionInProgress,
            onDismissRequest = { if (!isActionInProgress) showAssignSheet = false },
            onAssign = { conservator, scheduledAt ->
                onAssignConservator(conservator, scheduledAt)
            }
        )
    }

    if (showRejectSheet) {
        ManagerRejectSheet(
            isLoading = isActionInProgress,
            onDismissRequest = { if (!isActionInProgress) showRejectSheet = false },
            onSubmit = { reason ->
                onRejectTicket(reason)
            }
        )
    }

    conservatorActionType?.let { type ->
        ConservatorActionSheet(
            actionType = type,
            isLoading = isActionInProgress,
            onDismissRequest = { if (!isActionInProgress) conservatorActionType = null },
            onSubmit = { comment, pause ->
                onConservatorAction(type, comment, pause)
            }
        )
    }

    if (isActionInProgress) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun TicketFab(
    icon: ImageVector,
    contentDescription: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(4.dp)
    ) {
        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(icon, contentDescription = contentDescription)
        }
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

private fun formatDateTime(iso: String): String = PolishFormat.formatDate(iso)

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
