package pl.edu.ur.blokur.ui.views.finances.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.finances.contents.FinancesOverviewContent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    viewModel: FinancesViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    onNavigateToLedger: () -> Unit = {},
    onNavigateToBalances: () -> Unit = {},
    onNavigateToCsvImport: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var isManager by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isManager = viewModel.isManager()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FinancesEvent.NavigateToTransactions -> onNavigateToTransactions()
                FinancesEvent.NavigateToDocuments -> onNavigateToDocuments()
                FinancesEvent.NavigateToLedger -> onNavigateToLedger()
                FinancesEvent.NavigateToBalances -> onNavigateToBalances()
                else -> Unit // OpenPdf i ShowSnackbar obsługuje DocumentsScreen
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finanse") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        FinancesOverviewContent(
            state = state,
            onNavigateToTransactions = viewModel::onNavigateToTransactions,
            onNavigateToDocuments = viewModel::onNavigateToDocuments,
            onNavigateToLedger = viewModel::onNavigateToLedger,
            onNavigateToBalances = viewModel::onNavigateToBalances,
            onNavigateToCsvImport = onNavigateToCsvImport,
            isManager = isManager,
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

