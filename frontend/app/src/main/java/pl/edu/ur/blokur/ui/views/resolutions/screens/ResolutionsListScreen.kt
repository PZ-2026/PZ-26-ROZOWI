package pl.edu.ur.blokur.ui.views.resolutions.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.HourglassBottom
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
import pl.edu.ur.blokur.dtos.ResolutionDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.SuccessGreenBgLight
import pl.edu.ur.blokur.ui.theme.ShadowOverlay
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionEvent
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionsListState
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.ResolutionsListViewModel

@Composable
fun ResolutionsListScreen(
    viewModel: ResolutionsListViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showCreateDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResolutionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ResolutionEvent.NavigateToDetail -> onNavigateToDetail(event.id)
            }
        }
    }

    if (showDialog) {
        CreateResolutionDialog(
            formState = formState,
            onDismiss = viewModel::closeCreateDialog,
            onTitleChanged = viewModel::onTitleChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onEndDateChanged = viewModel::onEndDateChanged,
            onBuildingChanged = viewModel::onBuildingChanged,
            onOptionChanged = viewModel::onOptionChanged,
            onAddOption = viewModel::addOption,
            onRemoveOption = viewModel::removeOption,
            onConfirm = viewModel::submitCreate
        )
    }

    val isManager = (state as? ResolutionsListState.Success)?.isManager == true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isManager) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openCreateDialog,
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = { Text("Nowe głosowanie") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            is ResolutionsListState.Loading -> LoadingIndicator()
            is ResolutionsListState.Error -> EmptyState(title = "Błąd", description = s.message)
            is ResolutionsListState.Success -> {
                if (s.resolutions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "Brak uchwał",
                            description = if (s.isManager)
                                "Utwórz pierwsze głosowanie klikając przycisk poniżej."
                            else
                                "Nie masz aktywnych uchwał do głosowania."
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
                        items(s.resolutions, key = { it.id }) { resolution ->
                            ResolutionCard(
                                resolution = resolution,
                                onClick = { viewModel.onResolutionClicked(resolution.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionCard(
    resolution: ResolutionDto,
    onClick: () -> Unit
) {
    val isActive = resolution.isActive
    val statusColor = if (isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    val statusIcon = if (isActive) Icons.Rounded.HourglassBottom else Icons.Rounded.CheckCircle
    val statusText = if (isActive) "Aktywne" else "Zakończone"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x10000000))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikona
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isActive) SuccessGreenBgLight else MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Gavel, null,
                tint = statusColor,
                modifier = Modifier.size(26.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                resolution.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            resolution.authorName?.let {
                Text("Autor: $it", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Data zakończenia
            val endLabel = try {
                val ldt = java.time.LocalDateTime.parse(resolution.endDate)
                "Do: ${ldt.toLocalDate()}"
            } catch (_: Exception) { "Do: ${resolution.endDate}" }

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
                Text(endLabel, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
