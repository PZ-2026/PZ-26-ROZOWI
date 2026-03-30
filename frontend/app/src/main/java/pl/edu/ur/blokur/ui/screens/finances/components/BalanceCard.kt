package pl.edu.ur.blokur.ui.screens.finances.components

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
import pl.edu.ur.blokur.data.model.ApartmentBalanceDto
import pl.edu.ur.blokur.data.model.BalanceStatus
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurStatusBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.SuccessGreen

private fun formatAmount(amount: Double, currency: String): String {
    val prefix = if (amount >= 0) "+" else ""
    return "$prefix${"%.2f".format(amount)} $currency"
}

@Composable
fun BalanceCard(balance: ApartmentBalanceDto) {
    val balanceColor = when (balance.status) {
        BalanceStatus.NADPLATA -> SuccessGreen
        BalanceStatus.ZALEGLOSC -> ErrorRed
        BalanceStatus.WYZEROWANY -> MaterialTheme.colorScheme.onSurface
    }

    val balanceLabel = when (balance.status) {
        BalanceStatus.NADPLATA -> "Nadpłata"
        BalanceStatus.ZALEGLOSC -> "Zaległość"
        BalanceStatus.WYZEROWANY -> "Wyrównane"
    }

    BlokurCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Bieżące saldo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = formatAmount(balance.currentBalance, balance.currency),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = balanceColor
            )
            BlokurStatusBadge(text = balanceLabel, dotColor = balanceColor)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Suma wpłat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${"%.2f".format(balance.totalPaid)} PLN",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Suma naliczeń",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${"%.2f".format(balance.totalCharged)} PLN",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = ErrorRed
                )
            }
        }
    }
}
