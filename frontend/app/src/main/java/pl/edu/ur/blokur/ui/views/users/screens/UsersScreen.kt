package pl.edu.ur.blokur.ui.views.users.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange
import pl.edu.ur.blokur.ui.views.users.viewmodels.UsersEvent
import pl.edu.ur.blokur.ui.views.users.viewmodels.UsersUiState
import pl.edu.ur.blokur.ui.views.users.viewmodels.UsersViewModel

@Composable
fun UsersScreen(
    viewModel: UsersViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val showDialog by viewModel.showDialog.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDeactivate by remember { mutableStateOf<AdminUserDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UsersEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // ── Formularz tworzenia użytkownika ──────────────────────────────────────
    if (showDialog) {
        CreateUserDialog(
            formState = formState,
            onDismiss = viewModel::closeDialog,
            onFirstNameChanged = viewModel::onFirstNameChanged,
            onLastNameChanged = viewModel::onLastNameChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onRoleChanged = viewModel::onRoleChanged,
            onBuildingSelected = viewModel::onBuildingSelected,
            onStaircaseSelected = viewModel::onStaircaseSelected,
            onApartmentSelected = viewModel::onApartmentSelected,
            onConfirm = viewModel::submitCreateUser
        )
    }

    // ── Dialog deaktywacji ───────────────────────────────────────────────────
    confirmDeactivate?.let { user ->
        AlertDialog(
            onDismissRequest = { confirmDeactivate = null },
            icon = {
                Icon(Icons.Rounded.Block, null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("Deaktywuj konto") },
            text = {
                Text(
                    "Czy na pewno chcesz deaktywować konto użytkownika ${user.fullName} (${user.email})?\n\n" +
                    "Historia powiązana z kontem zostanie zachowana.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deactivateUser(user.id, user.fullName)
                    confirmDeactivate = null
                }) { Text("Deaktywuj", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeactivate = null }) { Text("Anuluj") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Scaffold ─────────────────────────────────────────────────────────────
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openCreateDialog,
                icon = { Icon(Icons.Rounded.PersonAdd, null) },
                text = { Text("Nowe konto") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        when (val s = state) {
            is UsersUiState.Loading -> LoadingIndicator()
            is UsersUiState.Error -> EmptyState(title = "Błąd", description = s.message)
            is UsersUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(8.dp))

                    // ── Wyszukiwarka ──────────────────────────────────────────
                    OutlinedTextField(
                        value = s.searchQuery,
                        onValueChange = viewModel::onSearchChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Szukaj po imieniu, email, roli...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = {
                            if (s.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchChanged("") }) {
                                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    if (s.filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                title = if (s.searchQuery.isBlank()) "Brak użytkowników" else "Brak wyników",
                                description = if (s.searchQuery.isBlank())
                                    "Dodaj pierwsze konto klikając przycisk poniżej."
                                else
                                    "Żaden użytkownik nie pasuje do wyszukiwania."
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize().navigationBarsPadding()
                        ) {
                            items(s.filtered, key = { it.id }) { user ->
                                UserRow(
                                    user = user,
                                    onDeactivate = { confirmDeactivate = user }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Wiersz użytkownika ────────────────────────────────────────────────────────

@Composable
private fun UserRow(
    user: AdminUserDto,
    onDeactivate: () -> Unit
) {
    val roleColor = when (user.role) {
        "ZARZADCA" -> MaterialTheme.colorScheme.tertiary
        "KONSERWATOR" -> WarningOrange
        else -> InfoBlue
    }
    val roleLabel = when (user.role) {
        "ZARZADCA" -> "Zarządca"
        "KONSERWATOR" -> "Konserwator"
        else -> "Mieszkaniec"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (user.active) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (user.active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${user.firstName.firstOrNull() ?: "?"}${user.lastName.firstOrNull() ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (user.active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Dane
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(user.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (!user.active) {
                    Text("NIEAKTYWNE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(roleColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(roleLabel, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = roleColor)
                }
                if (user.active) {
                    Icon(Icons.Rounded.CheckCircle, null,
                        tint = SuccessGreen, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Przycisk deaktywacji (tylko dla aktywnych)
        if (user.active) {
            IconButton(onClick = onDeactivate) {
                Icon(Icons.Rounded.Block, "Deaktywuj",
                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}
