package pl.edu.ur.blokur.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.PrimaryButton
import pl.edu.ur.blokur.presentation.common.component.TopBar
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme
import pl.edu.ur.blokur.presentation.profile.util.ProfileEvent
import pl.edu.ur.blokur.presentation.profile.util.ProfileState
import pl.edu.ur.blokur.presentation.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ProfileEvent.ShowSaveDialog -> showDialog = true
                is ProfileEvent.SaveSuccess -> Unit
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Potwierdzenie") },
            text = { Text("Czy chcesz zapisać dane użytkownika?") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; viewModel.confirmSave() }) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar(title = "Profil") },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val data = state as? ProfileState.Data ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            NormalCard {
                Text(
                    "Dane użytkownika",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Zmień podstawowe informacje profilu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = data.name,
                    onValueChange = viewModel::onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Imię i nazwisko") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = "Zapisz zmiany",
                    onClick = viewModel::requestSave,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryButton(
                    text = "Wyślij powiadomienie testowe",
                    onClick = viewModel::sendTestNotification,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    PreviewTheme {
        // Static preview without ViewModel
        Text("Profile Screen Preview", style = MaterialTheme.typography.bodyLarge)
    }
}