package pl.edu.ur.blokur.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.ui.components.BlokurAlertDialog
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurEmptyState
import pl.edu.ur.blokur.ui.components.BlokurPrimaryButton
import pl.edu.ur.blokur.ui.components.BlokurSnackbarHost
import pl.edu.ur.blokur.ui.components.BlokurTextField
import pl.edu.ur.blokur.ui.components.BlokurTopBar
import androidx.compose.material3.Text

@Composable
fun ProfileScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showEmptyState by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BlokurTopBar(title = "Profil")
        },
        snackbarHost = {
            BlokurSnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        if (showDialog) {
            BlokurAlertDialog(
                title = "Potwierdzenie",
                message = "Czy chcesz zapisać dane użytkownika?",
                onConfirm = {
                    showDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Zapisano zmiany")
                    }
                },
                onDismiss = {
                    showDialog = false
                }
            )
        }

        if (showEmptyState) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                BlokurEmptyState(
                    title = "Brak danych",
                    description = "Tutaj pojawią się dane profilu użytkownika.",
                    modifier = Modifier.weight(1f)
                )

                BlokurPrimaryButton(
                    text = "Wróć",
                    onClick = { showEmptyState = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
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

                BlokurCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Formularz testowy",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BlokurTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Imię i nazwisko"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BlokurPrimaryButton(
                        text = "Pokaż dialog",
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BlokurPrimaryButton(
                        text = "Pokaż snackbar",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("To jest przykładowy snackbar")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    BlokurPrimaryButton(
                        text = "Pokaż empty state",
                        onClick = { showEmptyState = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
