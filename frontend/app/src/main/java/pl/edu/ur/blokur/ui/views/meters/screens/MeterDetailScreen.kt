package pl.edu.ur.blokur.ui.views.meters.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ShowChart
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
import pl.edu.ur.blokur.dtos.MeterReadingResponseDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterDetailState
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterDetailViewModel
import pl.edu.ur.blokur.ui.views.meters.viewmodels.MeterEvent
import java.time.format.DateTimeFormatter

@Composable
fun MeterDetailScreen(
    viewModel: MeterDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showCreateDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MeterEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showDialog) {
        CreateMeterReadingDialog(
            formState = formState,
            onDismiss = viewModel::closeCreateDialog,
            onValueChanged = viewModel::onValueChanged,
            onReadingDateChanged = viewModel::onReadingDateChanged,
            onConfirm = viewModel::submitCreate
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopBar(
                title = "Szczegóły licznika",
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
                text = { Text("Wprowadź stan") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Nagłówek informacyjny
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Licznik: ${viewModel.serialNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Medium: ${viewModel.mediumType}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            when (val s = state) {
                is MeterDetailState.Loading -> LoadingIndicator()
                is MeterDetailState.Error -> EmptyState(title = "Błąd", description = s.message)
                is MeterDetailState.Success -> {
                    if (s.readings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = "Brak odczytów",
                                description = "Ten licznik nie posiada jeszcze żadnych zarejestrowanych odczytów."
                            )
                        }
                    } else {
                        Text(
                            "Historia odczytów",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(s.readings, key = { it.id }) { reading ->
                                ReadingCard(reading = reading)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadingCard(reading: MeterReadingResponseDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x10000000))
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ShowChart, null, tint = MaterialTheme.colorScheme.primary)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Wartość: ${reading.value}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Data: ${reading.readingDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            reading.recordedBy?.let {
                Text(
                    "Osoba spisująca: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
