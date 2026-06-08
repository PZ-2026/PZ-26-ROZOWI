package pl.edu.ur.blokur.ui.views.users.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.views.users.viewmodels.EditUserEvent
import pl.edu.ur.blokur.ui.views.users.viewmodels.EditUserUiState
import pl.edu.ur.blokur.ui.views.users.viewmodels.EditUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    viewModel: EditUserViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditUserEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is EditUserEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edycja konta") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (state is EditUserUiState.Success) {
                val isUserActive = (state as EditUserUiState.Success).user.active
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isUserActive) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Anuluj") }

                            Button(
                                onClick = viewModel::submitChanges,
                                modifier = Modifier.weight(1f).height(50.dp),
                                enabled = formState.isValid && !isSubmitting,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Icon(Icons.Rounded.Save, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Zapisz", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Powrót", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val s = state) {
                is EditUserUiState.Loading -> LoadingIndicator()
                is EditUserUiState.Error -> EmptyState(
                    title = "Błąd",
                    description = s.message,
                    onRetry = viewModel::reload
                )
                is EditUserUiState.Success -> {
                    val isUserActive = s.user.active
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!isUserActive) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "To konto jest nieaktywne. Edycja danych jest zablokowana.",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // ── Dane podstawowe ──────────────────────────────────
                        Text("Dane podstawowe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        OutlinedTextField(
                            value = formState.email,
                            onValueChange = {},
                            label = { Text("E-mail") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            enabled = false,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = formState.firstName,
                                onValueChange = viewModel::onFirstNameChanged,
                                label = { Text("Imię") },
                                modifier = Modifier.weight(1f),
                                enabled = isUserActive,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = formState.lastName,
                                onValueChange = viewModel::onLastNameChanged,
                                label = { Text("Nazwisko") },
                                modifier = Modifier.weight(1f),
                                enabled = isUserActive,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = formState.phone,
                            onValueChange = viewModel::onPhoneChanged,
                            label = { Text("Telefon (opcjonalny)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isUserActive,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // ── Rola ─────────────────────────────────────────────
                        Spacer(Modifier.height(8.dp))
                        Text("Rola w systemie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val roles = listOf("MIESZKANIEC" to "Mieszkaniec", "KONSERWATOR" to "Konserwator", "ZARZADCA" to "Zarządca")
                            roles.forEach { (key, label) ->
                                FilterChip(
                                    selected = formState.role == key,
                                    onClick = { viewModel.onRoleChanged(key) },
                                    label = { Text(label) },
                                    enabled = isUserActive,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        // ── Lokal ────────────────────────────────────────────
                        if (formState.role == "MIESZKANIEC") {
                            Spacer(Modifier.height(8.dp))
                            Text("Przypisany lokal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                            if (formState.isLoadingBuildings) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else if (formState.buildingsError != null) {
                                Text(formState.buildingsError!!, color = ErrorRed)
                            } else {
                                // Nieruchomość/Budynek
                                var expandedBuilding by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedBuilding && isUserActive,
                                    onExpandedChange = { if (isUserActive) expandedBuilding = it }
                                ) {
                                    OutlinedTextField(
                                        value = formState.selectedBuilding?.name ?: "Wybierz budynek",
                                        onValueChange = {},
                                        readOnly = true,
                                        enabled = isUserActive,
                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding && isUserActive) },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedBuilding && isUserActive,
                                        onDismissRequest = { expandedBuilding = false },
                                        modifier = Modifier.exposedDropdownSize()
                                    ) {
                                        formState.buildings.forEach { b ->
                                            DropdownMenuItem(
                                                text = { Text(b.name) },
                                                onClick = {
                                                    viewModel.onBuildingSelected(b)
                                                    expandedBuilding = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Klatka
                                if (formState.selectedBuilding != null) {
                                    var expandedStaircase by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expandedStaircase && isUserActive,
                                        onExpandedChange = { if (isUserActive) expandedStaircase = it }
                                    ) {
                                        OutlinedTextField(
                                            value = formState.selectedStaircase?.label ?: "Wybierz klatkę",
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = isUserActive,
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStaircase && isUserActive) },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedStaircase && isUserActive,
                                            onDismissRequest = { expandedStaircase = false },
                                            modifier = Modifier.exposedDropdownSize()
                                        ) {
                                            formState.selectedBuilding?.staircases?.forEach { s ->
                                                DropdownMenuItem(
                                                    text = { Text(s.label) },
                                                    onClick = {
                                                        viewModel.onStaircaseSelected(s)
                                                        expandedStaircase = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Lokal
                                if (formState.selectedStaircase != null) {
                                    var expandedApartment by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expandedApartment && isUserActive,
                                        onExpandedChange = { if (isUserActive) expandedApartment = it }
                                    ) {
                                        OutlinedTextField(
                                            value = formState.selectedApartment?.number ?: "Wybierz numer",
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = isUserActive,
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedApartment && isUserActive) },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedApartment && isUserActive,
                                            onDismissRequest = { expandedApartment = false },
                                            modifier = Modifier.exposedDropdownSize()
                                        ) {
                                            formState.selectedStaircase?.apartments?.forEach { a ->
                                                DropdownMenuItem(
                                                    text = { Text(a.number) },
                                                    onClick = {
                                                        viewModel.onApartmentSelected(a)
                                                        expandedApartment = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp)) // padding dla BottomBar
                    }
                }
            }

            // Szare tło podczas zapisu
            if (isSubmitting) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)))
            }
        }
    }
}
