package pl.edu.ur.blokur.ui.views.inspections.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.InspectionResponseDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.InfoBlueBg
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionEvent
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionsListState
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionsListScreen(
    viewModel: InspectionsListViewModel,
    isManager: Boolean = true,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showCreateDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val editingInspection by viewModel.editingInspection.collectAsState()
    val editFormState by viewModel.editFormState.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InspectionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Dialog tworzenia przeglądu
    if (showDialog) {
        CreateInspectionDialog(
            formState = formState,
            onDismiss = viewModel::closeCreateDialog,
            onTitleChanged = viewModel::onTitleChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onScheduledAtChanged = viewModel::onScheduledAtChanged,
            onScopeTypeChanged = viewModel::onScopeTypeChanged,
            onScopeIdChanged = viewModel::onScopeIdChanged,
            onConfirm = viewModel::submitCreate
        )
    }

    // Dialog edycji — ten sam komponent co tworzenia, ale prefillowany danymi
    if (editingInspection != null) {
        CreateInspectionDialog(
            formState = editFormState,
            onDismiss = viewModel::closeEditDialog,
            onTitleChanged = viewModel::onEditTitleChanged,
            onDescriptionChanged = viewModel::onEditDescriptionChanged,
            onScheduledAtChanged = viewModel::onEditScheduledAtChanged,
            onScopeTypeChanged = {},
            onScopeIdChanged = viewModel::onEditScopeIdChanged,
            onConfirm = viewModel::submitUpdate,
            confirmLabel = "Zaktualizuj"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Harmonogram przeglądów",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (isManager) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openCreateDialog,
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = { Text("Zaplanuj przegląd") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            is InspectionsListState.Loading -> LoadingIndicator()
            is InspectionsListState.Error -> EmptyState(title = "Błąd", description = s.message)
            is InspectionsListState.Success -> {
                if (s.inspections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "Brak przeglądów",
                            description = "Brak zaplanowanych przeglądów. Kliknij przycisk poniżej, aby utworzyć nowy."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
                    ) {
                        items(s.inspections, key = { it.id }) { inspection ->
                            InspectionCard(
                                inspection = inspection,
                                isManager = isManager,
                                onEdit = { viewModel.openEditDialog(inspection) },
                                onDelete = { viewModel.deleteInspection(inspection.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Karta przeglądu ────────────────────────────────────────────────────────────

@Composable
private fun InspectionCard(
    inspection: InspectionResponseDto,
    isManager: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val isUpcoming = inspection.isUpcoming
    val statusColor = if (isUpcoming) InfoBlue else MaterialTheme.colorScheme.onSurfaceVariant
    val statusIcon = if (isUpcoming) Icons.Rounded.CalendarToday else Icons.Rounded.CheckCircle
    val statusText = if (isUpcoming) "Zaplanowane" else "Odbyte"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x10000000))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isUpcoming) InfoBlueBg else MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Build, null, tint = statusColor, modifier = Modifier.size(26.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                inspection.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            val dateLabel = try {
                val ldt = java.time.LocalDateTime.parse(inspection.scheduledAt)
                "${ldt.toLocalDate()} ${ldt.toLocalTime().withSecond(0)}"
            } catch (_: Exception) { inspection.scheduledAt }
            Text(
                "Termin: $dateLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Zasięg: ${inspection.scopeTypeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            inspection.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(12.dp))
                        Text(
                            statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (isManager && isUpcoming) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edytuj",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Usuń",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
