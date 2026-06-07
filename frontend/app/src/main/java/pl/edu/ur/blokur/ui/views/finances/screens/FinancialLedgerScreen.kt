package pl.edu.ur.blokur.ui.views.finances.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancialLedgerViewModel
import pl.edu.ur.blokur.ui.views.finances.viewmodels.LedgerEvent
import pl.edu.ur.blokur.ui.utils.PolishFormat
import pl.edu.ur.blokur.ui.views.finances.viewmodels.LedgerUiState
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialLedgerScreen(
    viewModel: FinancialLedgerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val showDialog by viewModel.showAddDialog.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LedgerEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (showDialog) {
        AddTransactionDialog(
            formState = formState,
            onDismiss = viewModel::closeDialog,
            onTypeChanged = viewModel::onTypeChanged,
            onAmountChanged = viewModel::onAmountChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onDateChanged = viewModel::onDateChanged,
            onConfirm = viewModel::submitTransaction
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            val title = (state as? LedgerUiState.Success)?.apartmentLabel
                ?: "Finanse"
            TopAppBar(
                title = {
                    Column {
                        Text("Kartoteka finansowa", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        if (title.isNotBlank()) {
                            Text(title, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            val isManager = (state as? LedgerUiState.Success)?.isManager == true
            if (isManager) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openAddDialog,
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = { Text("Dodaj operację") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        when (val s = state) {
            is LedgerUiState.Loading -> LoadingIndicator()
            is LedgerUiState.Error -> EmptyState(
                title = "Błąd",
                description = s.message,
                onRetry = viewModel::load
            )
            is LedgerUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // ── Karta salda ──────────────────────────────────────────
                    BalanceCard(
                        balance = s.currentBalance,
                        transactionCount = s.transactions.size
                    )

                    Spacer(Modifier.height(16.dp))

                    if (s.transactions.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = "Brak transakcji",
                                description = if (s.isManager) "Dodaj pierwszą operację klikając przycisk poniżej."
                                              else "Historia transakcji jest pusta."
                            )
                        }
                    } else {
                        Text("Historia operacji", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize().navigationBarsPadding()
                        ) {
                            items(s.transactions, key = { it.id }) { tx ->
                                TransactionRow(tx = tx)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Karta salda ───────────────────────────────────────────────────────────────

@Composable
private fun BalanceCard(balance: BigDecimal, transactionCount: Int) {
    val isPositive = balance >= BigDecimal.ZERO
    val balanceColor = if (isPositive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
    val bgColor = if (isPositive) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(bgColor, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AccountBalanceWallet, null,
                        tint = balanceColor, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text("Saldo konta", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${if (isPositive) "+" else ""}${PolishFormat.formatMoney(balance)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = balanceColor
                    )
                    Text("$transactionCount operacji",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Wiersz transakcji ─────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(tx: FinancialTransactionDto) {
    val (typeLabel, typeColor, typeIcon) = when (tx.type) {
        "WPLATA" -> Triple("Wpłata", Color(0xFF2E7D32), Icons.Rounded.ArrowUpward)
        "NALICZENIE" -> Triple("Naliczenie", MaterialTheme.colorScheme.error, Icons.Rounded.ArrowDownward)
        else -> Triple("Korekta", MaterialTheme.colorScheme.tertiary, Icons.Rounded.SwapHoriz)
    }
    val amountColor = when (tx.type) {
        "WPLATA" -> Color(0xFF2E7D32)
        "NALICZENIE" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }
    val amountPrefix = when (tx.type) {
        "WPLATA" -> "+"
        "NALICZENIE" -> "-"
        else -> "±"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x0D000000))
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Ikona
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(typeColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(20.dp))
        }

        // Dane
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2)
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(typeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(typeLabel, style = MaterialTheme.typography.labelSmall,
                        color = typeColor, fontWeight = FontWeight.Bold)
                }
                Text(PolishFormat.formatDate(tx.transactionDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            tx.recordedByEmail?.let { email ->
                Text("przez $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Kwota
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$amountPrefix${PolishFormat.formatMoney(tx.amount.abs())}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}
