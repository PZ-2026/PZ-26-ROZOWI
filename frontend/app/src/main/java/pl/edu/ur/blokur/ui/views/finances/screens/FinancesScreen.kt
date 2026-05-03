package pl.edu.ur.blokur.ui.views.finances.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.TopBar
import pl.edu.ur.blokur.ui.views.finances.contents.FinancesOverviewContent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancesViewModel

@Composable
fun FinancesScreen(
    viewModel: FinancesViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToLedger: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FinancesEvent.NavigateToTransactions -> onNavigateToTransactions()
                FinancesEvent.NavigateToDocuments -> onNavigateToDocuments()
                FinancesEvent.NavigateToLedger -> onNavigateToLedger()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Finanse") }
    ) { innerPadding ->
        FinancesOverviewContent(
            state = state,
            onNavigateToTransactions = viewModel::onNavigateToTransactions,
            onNavigateToDocuments = viewModel::onNavigateToDocuments,
            onNavigateToLedger = viewModel::onNavigateToLedger,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        )
    }
}
