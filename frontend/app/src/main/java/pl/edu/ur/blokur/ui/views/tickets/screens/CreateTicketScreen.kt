package pl.edu.ur.blokur.ui.views.tickets.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.tickets.contents.CreateTicketFormContent
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketScreenEvent
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketSubmitState
import pl.edu.ur.blokur.ui.views.tickets.viewmodels.CreateTicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: CreateTicketViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoriesLoading by viewModel.categoriesLoading.collectAsState()

    var successTicketNumber by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateTicketScreenEvent.NavigateBack -> onNavigateBack()
                is CreateTicketScreenEvent.ShowSuccess -> successTicketNumber = event.ticketNumber
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(submitState) {
        val state = submitState
        if (state is CreateTicketSubmitState.Error) {
            snackbarHostState.showSnackbar("Błąd: ${state.message}")
        }
    }

    if (successTicketNumber != null) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Zgłoszenie przyjęte!") },
            text = {
                Text(
                    "Zgłoszenie nr ${successTicketNumber} zostało pomyślnie przyjęte do realizacji.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    successTicketNumber = null
                    onNavigateBack()
                }) {
                    Text("Wróć do listy", style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Nowe zgłoszenie", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = viewModel::onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        CreateTicketFormContent(
            formState = formState,
            submitState = submitState,
            categories = categories,
            categoriesLoading = categoriesLoading,
            onFormChanged = viewModel::onFormChanged,
            onSubmitClicked = viewModel::submit,
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

