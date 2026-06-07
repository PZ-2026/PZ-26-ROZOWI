package pl.edu.ur.blokur.ui.views.properties.contents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.PrimaryButton
import pl.edu.ur.blokur.ui.views.properties.utils.*

@Composable
fun PropertyDetailPanel(
    selectedNode: SelectedNode,
    formMode: FormMode,
    addTarget: AddTarget?,
    isSaving: Boolean,
    formError: String?,
    propertyForm: PropertyFormFields,
    buildingForm: BuildingFormFields,
    staircaseForm: StaircaseFormFields,
    apartmentForm: ApartmentFormFields,
    availableManagers: List<String> = emptyList(),
    onPropertyFormChange: (PropertyFormFields) -> Unit,
    onBuildingFormChange: (BuildingFormFields) -> Unit,
    onStaircaseFormChange: (StaircaseFormFields) -> Unit,
    onApartmentFormChange: (ApartmentFormFields) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToMeters: (String) -> Unit = {},
    onNavigateToLedger: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isEditable = formMode == FormMode.EDIT || formMode == FormMode.ADD
    val title = when {
        formMode == FormMode.ADD -> when (addTarget) {
            AddTarget.PROPERTY -> "Nowa wspólnota"
            AddTarget.BUILDING -> "Nowy budynek"
            AddTarget.STAIRCASE -> "Nowa klatka"
            AddTarget.APARTMENT -> "Nowy lokal"
            null -> ""
        }
        selectedNode is SelectedNode.Property -> "Wspólnota"
        selectedNode is SelectedNode.Building -> "Budynek"
        selectedNode is SelectedNode.Staircase -> "Klatka schodowa"
        selectedNode is SelectedNode.Apartment -> "Lokal"
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        when {
            formMode == FormMode.ADD && addTarget == AddTarget.PROPERTY ||
            formMode != FormMode.ADD && selectedNode is SelectedNode.Property -> {
                PropertyFields(propertyForm, onPropertyFormChange, isEditable, availableManagers)
            }
            formMode == FormMode.ADD && addTarget == AddTarget.BUILDING ||
            formMode != FormMode.ADD && selectedNode is SelectedNode.Building -> {
                BuildingFields(buildingForm, onBuildingFormChange, isEditable)
            }
            formMode == FormMode.ADD && addTarget == AddTarget.STAIRCASE ||
            formMode != FormMode.ADD && selectedNode is SelectedNode.Staircase -> {
                StaircaseFields(staircaseForm, onStaircaseFormChange, isEditable)
            }
            formMode == FormMode.ADD && addTarget == AddTarget.APARTMENT ||
            formMode != FormMode.ADD && selectedNode is SelectedNode.Apartment -> {
                ApartmentFields(apartmentForm, onApartmentFormChange, isEditable)
            }
        }

        if (formError != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formError,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (formMode) {
            FormMode.VIEW -> {
                if (selectedNode is SelectedNode.Apartment) {
                    OutlinedButton(
                        onClick = { onNavigateToLedger(selectedNode.apartment.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kartoteka finansowa")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onNavigateToMeters(selectedNode.apartment.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zarządzaj licznikami")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                PrimaryButton(
                    text = "Edytuj",
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            FormMode.EDIT, FormMode.ADD -> {
                PrimaryButton(
                    text = if (isSaving) "Zapisywanie..." else "Zapisz",
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Anuluj")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyFields(
    form: PropertyFormFields,
    onChange: (PropertyFormFields) -> Unit,
    enabled: Boolean,
    availableManagers: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = form.name, onValueChange = { onChange(form.copy(name = it)) },
        label = { Text("Nazwa wspólnoty") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. Wspólnota Mieszkaniowa Słoneczna") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.address, onValueChange = { onChange(form.copy(address = it)) },
        label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. ul. Piłsudskiego 12, Rzeszów") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.nip, onValueChange = {
            val filtered = it.filter { c -> c.isDigit() }
            if (filtered.length <= 10) {
                onChange(form.copy(nip = filtered))
            }
        },
        label = { Text("NIP") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. 1234567890") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = form.managerPhone, onValueChange = {
            val filtered = it.filter { c -> c.isDigit() || c == '+' || c == '-' || c == ' ' }
            if (filtered.length <= 15) {
                onChange(form.copy(managerPhone = filtered))
            }
        },
        label = { Text("Telefon zarządcy") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. +48 123 456 789") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )

    // E-mail zarządcy dropdown/autocomplete selection
    ExposedDropdownMenuBox(
        expanded = enabled && expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = form.managerEmail,
            onValueChange = { 
                onChange(form.copy(managerEmail = it))
                expanded = true
            },
            label = { Text("E-mail zarządcy") },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            placeholder = { Text("np. zarzadca@wspolnota.pl") },
            enabled = enabled,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            trailingIcon = {
                if (enabled && availableManagers.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )
        if (enabled && availableManagers.isNotEmpty()) {
            val filteredManagers = availableManagers.filter {
                it.contains(form.managerEmail, ignoreCase = true)
            }
            if (filteredManagers.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredManagers.forEach { email ->
                        DropdownMenuItem(
                            text = { Text(email) },
                            onClick = {
                                onChange(form.copy(managerEmail = email))
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildingFields(
    form: BuildingFormFields,
    onChange: (BuildingFormFields) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = form.name, onValueChange = { onChange(form.copy(name = it)) },
        label = { Text("Nazwa budynku") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. Budynek A (Solaris)") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.address, onValueChange = { onChange(form.copy(address = it)) },
        label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. ul. Słoneczna 15") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.estateName, onValueChange = { onChange(form.copy(estateName = it)) },
        label = { Text("Nazwa osiedla") }, modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("np. Osiedle Zielone") },
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = form.latitude, onValueChange = {
                val filtered = it.filter { c -> c.isDigit() || c == '.' || c == '-' }
                if (filtered.length <= 15) {
                    onChange(form.copy(latitude = filtered))
                }
            },
            label = { Text("Szer. geo.") }, modifier = Modifier.weight(1f),
            placeholder = { Text("np. 50.0413") },
            enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = form.longitude, onValueChange = {
                val filtered = it.filter { c -> c.isDigit() || c == '.' || c == '-' }
                if (filtered.length <= 15) {
                    onChange(form.copy(longitude = filtered))
                }
            },
            label = { Text("Dł. geo.") }, modifier = Modifier.weight(1f),
            placeholder = { Text("np. 21.9990") },
            enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
private fun StaircaseFields(
    form: StaircaseFormFields,
    onChange: (StaircaseFormFields) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = form.label, onValueChange = { onChange(form.copy(label = it)) },
        label = { Text("Etykieta klatki (np. A, B, C)") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApartmentFields(
    form: ApartmentFormFields,
    onChange: (ApartmentFormFields) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = form.number, onValueChange = { onChange(form.copy(number = it)) },
        label = { Text("Numer lokalu") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.floor, onValueChange = { onChange(form.copy(floor = it)) },
        label = { Text("Piętro") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = form.areaM2, onValueChange = { onChange(form.copy(areaM2 = it)) },
        label = { Text("Powierzchnia (m²)") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )

    // Ownership type selector
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = form.ownershipType == "WLASNOSCIOWY",
            onClick = { onChange(form.copy(ownershipType = "WLASNOSCIOWY")) },
            label = { Text("Własnościowy") },
            enabled = enabled
        )
        FilterChip(
            selected = form.ownershipType == "NAJEM",
            onClick = { onChange(form.copy(ownershipType = "NAJEM")) },
            label = { Text("Najem") },
            enabled = enabled
        )
    }
}
