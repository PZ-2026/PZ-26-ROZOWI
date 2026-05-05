package pl.edu.ur.blokur.ui.views.inspections.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
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
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionEvent
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionsListState
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.InspectionsListViewModel

@Composable
fun InspectionsListScreen(
    viewModel: InspectionsListViewModel,
    isManager: Boolean = true // Panel jest tylko dla zarządcy
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showCreateDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InspectionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Harmonogram i Przeglądy") },
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
                            InspectionCard(inspection = inspection)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectionCard(
    inspection: InspectionResponseDto
) {
    val isUpcoming = inspection.isUpcoming
    val statusColor = if (isUpcoming) Color(0xFF1976D2) else MaterialTheme.colorScheme.onSurfaceVariant
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
        // Ikona
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isUpcoming) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Build, null,
                tint = statusColor,
                modifier = Modifier.size(26.dp)
            )
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

            Text("Termin: $dateLabel", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Text("Zasięg: ${inspection.scopeTypeLabel}", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
                
            inspection.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }

            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        Text(statusText, style = MaterialTheme.typography.labelSmall,
                            color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
