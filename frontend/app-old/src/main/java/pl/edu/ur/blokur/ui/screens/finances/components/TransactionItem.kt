package pl.edu.ur.blokur.ui.screens.finances.components

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
import pl.edu.ur.blokur.data.model.FinancialTransactionDto
import pl.edu.ur.blokur.data.model.TransactionType
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurStatusBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen

private data class TransactionPresentation(
    val label: String,
    val color: Color,
    val icon: ImageVector,
)

private fun transactionPresentation(type: TransactionType) =
    when (type) {
        TransactionType.WPLATA -> TransactionPresentation("Wpłata", SuccessGreen, Icons.Rounded.ArrowDownward)
        TransactionType.NALICZENIE -> TransactionPresentation("Naliczenie", ErrorRed, Icons.Rounded.ArrowUpward)
        TransactionType.KOREKTA -> TransactionPresentation("Korekta", InfoBlue, Icons.Rounded.SwapVert)
    }

private fun formatAmount(
    amount: Double,
    currency: String,
): String {
    val prefix = if (amount >= 0) "+" else ""
    return "$prefix${"%.2f".format(amount)} $currency"
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else dateString
    } catch (e: Exception) {
        dateString
    }
}

@Composable
fun TransactionItem(transaction: FinancialTransactionDto) {
    val presentation = transactionPresentation(transaction.type)
    val amountText = formatAmount(transaction.amount, "PLN")
    val amountColor = if (transaction.amount >= 0) SuccessGreen else ErrorRed

    BlokurCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(46.dp)
                        .background(presentation.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.color,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BlokurStatusBadge(text = presentation.label, dotColor = presentation.color)
                    Text(
                        text = formatDate(transaction.transactionDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor,
            )
        }
    }
}
