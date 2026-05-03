package pl.edu.ur.blokur.ui.views.users.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.ApartmentNodeDto
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.ui.views.users.viewmodels.NewUserFormState

private val AVAILABLE_ROLES = listOf(
    "MIESZKANIEC" to "Mieszkaniec",
    "KONSERWATOR" to "Konserwator",
    "ZARZADCA" to "Zarządca"
)

@Composable
fun CreateUserDialog(
    formState: NewUserFormState,
    onDismiss: () -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onRoleChanged: (String) -> Unit,
    onBuildingSelected: (BuildingTreeNodeDto?) -> Unit,
    onStaircaseSelected: (StaircaseNodeDto?) -> Unit,
    onApartmentSelected: (ApartmentNodeDto?) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!formState.isSubmitting) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column {
                Text("Nowe konto użytkownika", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Użytkownik otrzyma e-mail z linkiem do ustawienia hasła.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Imię i Nazwisko ─────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = formState.firstName,
                        onValueChange = onFirstNameChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Imię") },
                        leadingIcon = { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = formState.lastName,
                        onValueChange = onLastNameChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Nazwisko") },
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // ── E-mail ───────────────────────────────────────────────
                OutlinedTextField(
                    value = formState.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Adres e-mail") },
                    leadingIcon = { Icon(Icons.Rounded.Email, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Rola ─────────────────────────────────────────────────
                RoleSelector(
                    selectedRole = formState.role,
                    onRoleSelected = onRoleChanged,
                    enabled = !formState.isSubmitting
                )

                // ── Wybór lokalu (tylko dla MIESZKANIEC) ──────────────────
                AnimatedVisibility(
                    visible = formState.role == "MIESZKANIEC",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Przypisz lokal",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (formState.isLoadingBuildings) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        } else if (formState.buildingsError != null) {
                            Text(
                                formState.buildingsError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            // Budynek
                            DropdownSelector(
                                label = "Budynek",
                                selectedLabel = formState.selectedBuilding?.let { "${it.name} – ${it.address}" } ?: "Wybierz budynek",
                                icon = Icons.Rounded.Home,
                                items = formState.buildings,
                                itemLabel = { "${it.name} – ${it.address}" },
                                onSelected = { onBuildingSelected(it) },
                                enabled = formState.buildings.isNotEmpty() && !formState.isSubmitting
                            )

                            // Klatka schodowa
                            if (formState.selectedBuilding != null) {
                                DropdownSelector(
                                    label = "Klatka schodowa",
                                    selectedLabel = formState.selectedStaircase?.let { "Klatka ${it.label}" } ?: "Wybierz klatkę",
                                    icon = Icons.Rounded.Apartment,
                                    items = formState.selectedBuilding.staircases,
                                    itemLabel = { "Klatka ${it.label}" },
                                    onSelected = { onStaircaseSelected(it) },
                                    enabled = !formState.isSubmitting
                                )
                            }

                            // Lokal
                            if (formState.selectedStaircase != null) {
                                DropdownSelector(
                                    label = "Lokal",
                                    selectedLabel = formState.selectedApartment?.let {
                                        "Lokal ${it.number}${it.floor?.let { f -> ", piętro $f" } ?: ""}"
                                    } ?: "Wybierz lokal",
                                    icon = Icons.Rounded.Home,
                                    items = formState.selectedStaircase.apartments,
                                    itemLabel = { "Lokal ${it.number}${it.floor?.let { f -> ", piętro $f" } ?: ""}" },
                                    onSelected = { onApartmentSelected(it) },
                                    enabled = !formState.isSubmitting
                                )
                            }

                            // Potwierdzenie wyboru
                            if (formState.selectedApartment != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Check, null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp))
                                    Text(
                                        "Wybrany lokal: ${formState.selectedApartment.number}" +
                                        " (${formState.selectedBuilding?.name}, kl. ${formState.selectedStaircase?.label})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = formState.isValid && !formState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Utwórz konto")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !formState.isSubmitting) {
                Text("Anuluj")
            }
        }
    )
}

// ── Role Selector ────────────────────────────────────────────────────────────

@Composable
private fun RoleSelector(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Rola w systemie", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AVAILABLE_ROLES.forEach { (roleKey, roleLabel) ->
                val isSelected = selectedRole == roleKey
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable(enabled = enabled) { onRoleSelected(roleKey) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        roleLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Generic Dropdown Selector ─────────────────────────────────────────────────

@Composable
private fun <T> DropdownSelector(
    label: String,
    selectedLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<T>,
    itemLabel: (T) -> String,
    onSelected: (T) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .clickable(enabled = enabled) { if (items.isNotEmpty()) expanded = true }
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selectedLabel, style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ArrowDropDown, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemLabel(item)) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
