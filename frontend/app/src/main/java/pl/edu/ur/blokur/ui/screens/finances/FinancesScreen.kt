package pl.edu.ur.blokur.ui.screens.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.data.mock.MockFinances
import pl.edu.ur.blokur.ui.components.BlokurTopBar
import pl.edu.ur.blokur.ui.screens.finances.components.BalanceCard
import pl.edu.ur.blokur.ui.screens.finances.components.TransactionItem
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme

@Composable
fun FinancesScreen() {
    val balance = MockFinances.balance
    val transactions = MockFinances.transactions

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BlokurTopBar(title = "Finanse") }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item { BalanceCard(balance = balance) }

            item {
                Text(
                    text = "Historia transakcji (24 mies.)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FinancesScreenPreview() {
    BlokurPreviewTheme {
        FinancesScreen()
    }
}