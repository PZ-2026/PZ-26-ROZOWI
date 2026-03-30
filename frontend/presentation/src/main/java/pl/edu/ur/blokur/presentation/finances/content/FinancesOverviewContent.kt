package pl.edu.ur.blokur.presentation.finances.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme
import pl.edu.ur.blokur.presentation.finances.component.BalanceCard
import pl.edu.ur.blokur.presentation.finances.util.FinancesState

@Composable
fun FinancesOverviewContent(
    state: FinancesState,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is FinancesState.Loading -> LoadingIndicator()
        is FinancesState.Error -> EmptyState(title = "Błąd", description = state.message)
        is FinancesState.Data -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            BalanceCard(balance = state.balance)
            Text("Przejdź do", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            FinancesNavItem(
                icon = Icons.Rounded.History,
                title = "Historia transakcji",
                subtitle = "Wpłaty, naliczenia i korekty z ostatnich 24 miesięcy",
                onClick = onNavigateToTransactions
            )
            FinancesNavItem(
                icon = Icons.Rounded.Description,
                title = "Dokumenty",
                subtitle = "Rozliczenia, faktury i zawiadomienia zarządcy",
                onClick = onNavigateToDocuments
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FinancesNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true)
@Composable
private fun FinancesOverviewLoadingPreview() {
    PreviewTheme { FinancesOverviewContent(FinancesState.Loading, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun FinancesOverviewErrorPreview() {
    PreviewTheme { FinancesOverviewContent(FinancesState.Error("Błąd sieci"), {}, {}) }
}
