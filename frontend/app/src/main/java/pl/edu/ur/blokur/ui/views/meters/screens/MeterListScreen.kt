package pl.edu.ur.blokur.ui.views.meters.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.MeterResponseDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.theme.MeterGas
import pl.edu.ur.blokur.ui.theme.MeterGasBg
import pl.edu.ur.blokur.ui.theme.MeterHeat
import pl.edu.ur.blokur.ui.theme.MeterHeatBg
import pl.edu.ur.blokur.ui.theme.MeterWaterCold
import pl.edu.ur.blokur.ui.theme.MeterWaterColdBg
import pl.edu.ur.blokur.ui.theme.MeterWaterHot
import pl.edu.ur.blokur.ui.theme.MeterWaterHotBg
import pl.edu.ur.blokur.ui.theme.ShadowOverlay
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterEvent
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterListState
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterListViewModel

@Composable
fun MeterListScreen(
    viewModel: MeterListViewModel,
    onNavigateToDetail: (String, String, String) -> Unit, // meterId, serialNumber, mediumType
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showCreateDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var meterToDeactivate by remember { mutableStateOf<MeterResponseDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MeterEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showDialog) {
        CreateMeterDialog(
            formState = formState,
            onDismiss = viewModel::closeCreateDialog,
            onSerialNumberChanged = viewModel::onSerialNumberChanged,
            onMediumTypeChanged = viewModel::onMediumTypeChanged,
            onInstallationDateChanged = viewModel::onInstallationDateChanged,
            onConfirm = viewModel::submitCreate
        )
    }

    meterToDeactivate?.let { meter ->
        AlertDialog(
            onDismissRequest = { meterToDeactivate = null },
            icon = {
                Icon(
                    Icons.Rounded.PowerSettingsNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Dezaktywuj licznik") },
            text = {
                Text(
                    "Czy na pewno chcesz dezaktywować licznik SN: ${meter.serialNumber}?\n\n" +
                        "Urządzenie zostanie oznaczone jako nieaktywne.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deactivateMeter(meter.id)
                        meterToDeactivate = null
                    }
                ) {
                    Text("Dezaktywuj", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { meterToDeactivate = null }) {
                    Text("Anuluj")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Liczniki lokalu",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Powrót")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openCreateDialog,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Dodaj licznik") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        when (val s = state) {
            is MeterListState.Loading -> LoadingIndicator()
            is MeterListState.Error -> EmptyState(title = "Błąd", description = s.message)
            is MeterListState.Success -> {
                if (s.meters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "Brak liczników",
                            description = "Brak zarejestrowanych urządzeń pomiarowych. Kliknij przycisk poniżej, aby dodać nowe."
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
                        items(s.meters, key = { it.id }) { meter ->
                            MeterCard(
                                meter = meter,
                                onClick = {
                                    onNavigateToDetail(meter.id, meter.serialNumber, meter.mediumTypeLabel)
                                },
                                onDeactivate = if (meter.active) {
                                    { meterToDeactivate = meter }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeterCard(
    meter: MeterResponseDto,
    onClick: () -> Unit,
    onDeactivate: (() -> Unit)? = null
) {
    val isActive = meter.active
    val statusColor = if (isActive) SuccessGreen else MaterialTheme.colorScheme.error
    val statusText = if (isActive) "Aktywny" else "Nieaktywny"
    var menuExpanded by remember { mutableStateOf(false) }

    val icon = when (meter.mediumType) {
        "ZIMNA_WODA", "CIEPLA_WODA" -> Icons.Rounded.WaterDrop
        "GAZ" -> Icons.Rounded.LocalFireDepartment
        "CIEPLO" -> Icons.Rounded.Thermostat
        else -> Icons.Rounded.Speed
    }

    val iconTint = when (meter.mediumType) {
        "ZIMNA_WODA" -> MeterWaterCold
        "CIEPLA_WODA" -> MeterWaterHot
        "GAZ" -> MeterGas
        "CIEPLO" -> MeterHeat
        else -> MaterialTheme.colorScheme.primary
    }

    val iconBg = when (meter.mediumType) {
        "ZIMNA_WODA" -> MeterWaterColdBg
        "CIEPLA_WODA" -> MeterWaterHotBg
        "GAZ" -> MeterGasBg
        "CIEPLO" -> MeterHeatBg
        else -> MaterialTheme.colorScheme.primaryContainer
    }

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
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBg, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(26.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "SN: ${meter.serialNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                "Medium: ${meter.mediumTypeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Data montażu: ${meter.installationDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                    Text(
                        statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (onDeactivate != null) {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Opcje licznika",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Dezaktywuj", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDeactivate()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.PowerSettingsNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }

        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = "Szczegóły",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
