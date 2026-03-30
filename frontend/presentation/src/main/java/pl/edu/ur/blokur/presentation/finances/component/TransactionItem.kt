package pl.edu.ur.blokur.presentation.finances.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.FinancialTransaction
import pl.edu.ur.blokur.domain.model.TransactionType
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.StatusBadge
import pl.edu.ur.blokur.presentation.common.theme.ErrorRed
import pl.edu.ur.blokur.presentation.common.theme.InfoBlue
import pl.edu.ur.blokur.presentation.common.theme.SuccessGreen

private data class TransactionPresentation(val label: String, val color: Color, val icon: ImageVector)

private fun TransactionType.toPresentation() = when (this) {
    TransactionType.WPLATA -> TransactionPresentation("Wpłata", SuccessGreen, Icons.Rounded.ArrowDownward)
    TransactionType.NALICZENIE -> TransactionPresentation("Naliczenie", ErrorRed, Icons.Rounded.ArrowUpward)
    TransactionType.KOREKTA -> TransactionPresentation("Korekta", InfoBlue, Icons.Rounded.SwapVert)
}

@Composable
fun TransactionItem(transaction: FinancialTransaction) {
    val presentation = transaction.type.toPresentation()
    val amountText = run {
        val prefix = if (transaction.amount >= 0) "+" else ""
        "$prefix${"%.2f".format(transaction.amount)} PLN"
    }
    val amountColor = if (transaction.amount >= 0) SuccessGreen else ErrorRed

    NormalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(presentation.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(presentation.icon, contentDescription = null, tint = presentation.color, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(text = presentation.label, dotColor = presentation.color)
                    Text(formatDate(transaction.transactionDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(amountText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = amountColor)
        }
    }
}

private fun formatDate(date: String): String = try {
    val parts = date.split("-")
    if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else date
} catch (_: Exception) { date }
