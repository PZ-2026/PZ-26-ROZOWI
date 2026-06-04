package pl.edu.ur.blokur.ui.views.finances.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.finances.contents.DocumentsContent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.viewmodels.FinancesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: FinancesViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Obsługa eventów z ViewModelu
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FinancesEvent.OpenPdf -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(event.uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                is FinancesEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Dokumenty", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        DocumentsContent(
            state = state,
            onDownload = { document -> viewModel.downloadDocument(document) },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )
    }
}
