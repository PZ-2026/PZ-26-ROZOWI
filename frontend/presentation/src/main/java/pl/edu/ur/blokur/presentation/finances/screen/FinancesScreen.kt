package pl.edu.ur.blokur.presentation.finances.screen

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
import pl.edu.ur.blokur.presentation.common.component.TopBar
import pl.edu.ur.blokur.presentation.finances.content.FinancesOverviewContent
import pl.edu.ur.blokur.presentation.finances.util.FinancesEvent
import pl.edu.ur.blokur.presentation.finances.viewmodel.FinancesViewModel

@Composable
fun FinancesScreen(
    viewModel: FinancesViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDocuments: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FinancesEvent.NavigateToTransactions -> onNavigateToTransactions()
                FinancesEvent.NavigateToDocuments -> onNavigateToDocuments()
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
