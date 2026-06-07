package pl.edu.ur.blokur.ui.views.finances.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import pl.edu.ur.blokur.ui.components.NormalCard
import pl.edu.ur.blokur.ui.components.StatusBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.utils.PolishFormat
import java.math.BigDecimal

@Composable
fun BalanceCard(currentBalance: BigDecimal, transactions: List<FinancialTransactionDto>) {
    val totalPaid = transactions.filter { it.isCredit }.sumOf { it.amount }
    val totalCharged = transactions.filter { !it.isCredit }.sumOf { it.amount }

    val (label, color) = when {
        currentBalance > BigDecimal.ZERO -> "Nadpłata" to SuccessGreen
        currentBalance < BigDecimal.ZERO -> "Zaległość" to ErrorRed
        else -> "Wyrównane" to MaterialTheme.colorScheme.onSurface
    }
    val amountText = formatAmount(currentBalance)

    NormalCard {
        Text("Bieżące saldo", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(amountText, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            StatusBadge(text = label, dotColor = color)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Suma wpłat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("+${PolishFormat.formatMoney(totalPaid)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = SuccessGreen)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Suma naliczeń", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-${PolishFormat.formatMoney(totalCharged)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = ErrorRed)
            }
        }
    }
}

private fun formatAmount(amount: BigDecimal): String {
    val prefix = if (amount >= BigDecimal.ZERO) "+" else ""
    return "$prefix${PolishFormat.formatMoney(amount)}"
}
