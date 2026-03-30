package pl.edu.ur.blokur.presentation.tickets.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.tickets.content.CreateTicketFormContent
import pl.edu.ur.blokur.presentation.tickets.util.CreateTicketScreenEvent
import pl.edu.ur.blokur.presentation.tickets.viewmodel.CreateTicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketScreen(
    viewModel: CreateTicketViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateTicketScreenEvent.NavigateBack -> onNavigateBack()
            }
        }
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
        }
    ) { innerPadding ->
        CreateTicketFormContent(
            formState = formState,
            submitState = submitState,
            categories = viewModel.categories,
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
