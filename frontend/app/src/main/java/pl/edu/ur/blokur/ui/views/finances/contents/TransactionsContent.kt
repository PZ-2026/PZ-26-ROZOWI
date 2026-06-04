package pl.edu.ur.blokur.ui.views.finances.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import pl.edu.ur.blokur.ui.components.EmptyState
import java.math.BigDecimal
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.theme.PreviewTheme
import pl.edu.ur.blokur.ui.views.finances.components.BalanceCard
import pl.edu.ur.blokur.ui.views.finances.components.TransactionItem
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesState

@Composable
fun TransactionsContent(
    state: FinancesState,
    modifier: Modifier = Modifier
) {
    when (state) {
        is FinancesState.Loading -> LoadingIndicator()
        is FinancesState.Error -> EmptyState(title = "Błąd", description = state.message)
        is FinancesState.Data -> TransactionsListContent(
            currentBalance = state.currentBalance,
            transactions = state.transactions,
            modifier = modifier
        )
    }
}

@Composable
private fun TransactionsListContent(
    currentBalance: BigDecimal,
    transactions: List<FinancialTransactionDto>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item { BalanceCard(currentBalance = currentBalance, transactions = transactions) }
        item {
            Text(
                text = "Ostatnie 24 miesiące · ${transactions.size} operacji",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(transactions) { transaction -> TransactionItem(transaction = transaction) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsLoadingPreview() {
    PreviewTheme { TransactionsContent(FinancesState.Loading) }
}

@Preview(showBackground = true)
@Composable
private fun TransactionsErrorPreview() {
    PreviewTheme { TransactionsContent(FinancesState.Error("Wystąpił błąd")) }
}
