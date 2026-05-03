package pl.edu.ur.blokur.ui.views.properties.contents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onPropertyFormChange: (PropertyFormFields) -> Unit,
    onBuildingFormChange: (BuildingFormFields) -> Unit,
    onStaircaseFormChange: (StaircaseFormFields) -> Unit,
    onApartmentFormChange: (ApartmentFormFields) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onNavigateToMeters: (String) -> Unit = {},
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
                PropertyFields(propertyForm, onPropertyFormChange, isEditable)
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

@Composable
private fun PropertyFields(
    form: PropertyFormFields,
    onChange: (PropertyFormFields) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = form.name, onValueChange = { onChange(form.copy(name = it)) },
        label = { Text("Nazwa wspólnoty") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.address, onValueChange = { onChange(form.copy(address = it)) },
        label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.nip, onValueChange = { onChange(form.copy(nip = it)) },
        label = { Text("NIP") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    OutlinedTextField(
        value = form.managerPhone, onValueChange = { onChange(form.copy(managerPhone = it)) },
        label = { Text("Telefon zarządcy") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
    OutlinedTextField(
        value = form.managerEmail, onValueChange = { onChange(form.copy(managerEmail = it)) },
        label = { Text("E-mail zarządcy") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
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
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.address, onValueChange = { onChange(form.copy(address = it)) },
        label = { Text("Adres") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    OutlinedTextField(
        value = form.estateName, onValueChange = { onChange(form.copy(estateName = it)) },
        label = { Text("Nazwa osiedla") }, modifier = Modifier.fillMaxWidth(),
        enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = form.latitude, onValueChange = { onChange(form.copy(latitude = it)) },
            label = { Text("Szer. geo.") }, modifier = Modifier.weight(1f),
            enabled = enabled, singleLine = true, shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        OutlinedTextField(
            value = form.longitude, onValueChange = { onChange(form.copy(longitude = it)) },
            label = { Text("Dł. geo.") }, modifier = Modifier.weight(1f),
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
