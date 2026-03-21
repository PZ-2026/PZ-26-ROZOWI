package pl.edu.ur.blokur.ui.screens.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurEmptyState
import pl.edu.ur.blokur.ui.components.BlokurTopBar
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme

@Composable
fun FinancesScreen() {
    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BlokurTopBar(title = "Finanse") },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            BlokurCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Moduł finansów",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tutaj będą prezentowane saldo, opłaty i historia rozliczeń mieszkańca.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            BlokurEmptyState(
                title = "Brak danych finansowych",
                description = "Po integracji z backendem w tym miejscu pojawią się informacje o płatnościach i rozliczeniach.",
                modifier = Modifier.weight(1f),
            )
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
