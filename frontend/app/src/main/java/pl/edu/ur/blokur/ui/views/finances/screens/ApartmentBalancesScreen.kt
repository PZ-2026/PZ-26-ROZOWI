package pl.edu.ur.blokur.ui.views.finances.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.views.finances.viewmodels.ApartmentBalancesViewModel
import pl.edu.ur.blokur.ui.views.finances.viewmodels.BalancesEvent
import pl.edu.ur.blokur.ui.views.finances.viewmodels.BalancesFilterState
import pl.edu.ur.blokur.ui.views.finances.viewmodels.BalancesUiState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentBalancesScreen(
    viewModel: ApartmentBalancesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BalancesEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Monitorowanie zaległości",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Zarządzanie saldem lokali",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onSortToggled) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = "Zmień sortowanie",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            FilterPanel(
                filterState = filterState,
                sortDesc = filterState.sort == "debt_desc",
                onPropertyIdChanged = viewModel::onPropertyIdChanged,
                onMinDebtChanged = viewModel::onMinDebtChanged,
                onMinDaysOverdueChanged = viewModel::onMinDaysOverdueChanged,
                onApply = viewModel::load
            )
            Spacer(Modifier.height(12.dp))

            val isDownloading = (uiState as? BalancesUiState.Success)?.isDownloadingPdf == true

            Button(
                onClick = viewModel::downloadPdf,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDownloading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedContent(isDownloading, label = "pdf_dl") { downloading ->
                    if (downloading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Generuję PDF...")
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Pobierz zestawienie PDF", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            when (val s = uiState) {
                is BalancesUiState.Loading -> LoadingIndicator()
                is BalancesUiState.Error -> EmptyState(
                    title = "Błąd",
                    description = s.message
                )

                is BalancesUiState.Success -> {
                    if (s.items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = "Brak wyników",
                                description = "Żaden lokal nie spełnia podanych kryteriów."
                            )
                        }
                    } else {
                        Text(
                            "Lokale (${s.items.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            items(s.items, key = { it.apartmentId }) { item ->
                                ApartmentBalanceRow(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Panel filtrów ─────────────────────────────────────────────────────────────

@Composable
private fun FilterPanel(
    filterState: BalancesFilterState,
    sortDesc: Boolean,
    onPropertyIdChanged: (String) -> Unit,
    onMinDebtChanged: (String) -> Unit,
    onMinDaysOverdueChanged: (String) -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Filtry",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = filterState.minDebt,
                onValueChange = onMinDebtChanged,
                label = { Text("Min. zaległość (PLN)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = filterState.minDaysOverdue,
                onValueChange = onMinDaysOverdueChanged,
                label = { Text("Min. dni zalegania") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
            Text(
                "Sortowanie: ${if (sortDesc) "największa zaległość ↓" else "najmniejsza zaległość ↑"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Rounded.Search, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Zastosuj filtry")
            }
        }
    }
}

// ── Wiersz lokalu ─────────────────────────────────────────────────────────────

@Composable
private fun ApartmentBalanceRow(item: ApartmentBalanceItemDto) {
    val isDebt = item.balance < BigDecimal.ZERO
    val balanceColor = if (isDebt) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
    val bgColor = if (isDebt)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
    else
        Color(0xFFE8F5E9)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x0D000000))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.address,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.lastPaymentDate?.let { date ->
                    Text(
                        "Wpłata: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: Text(
                    "Brak wpłat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                item.daysOverdue?.let { days ->
                    Box(
                        modifier = Modifier
                            .background(bgColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "$days dni",
                            style = MaterialTheme.typography.labelSmall,
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (isDebt) "" else "+"}${item.balance.setScale(2)} PLN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
        }
    }
}
