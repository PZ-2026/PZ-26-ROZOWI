package pl.edu.ur.blokur.presentation.propertytree.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HomeWork
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.ApartmentOwnershipType
import pl.edu.ur.blokur.domain.model.ManagedApartment
import pl.edu.ur.blokur.domain.model.ManagedProperty
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.PrimaryButton
import pl.edu.ur.blokur.presentation.common.component.SecondaryButton
import pl.edu.ur.blokur.presentation.common.component.TextField
import pl.edu.ur.blokur.presentation.common.theme.InfoBlue
import pl.edu.ur.blokur.presentation.common.theme.SuccessGreen
import pl.edu.ur.blokur.presentation.common.theme.WarningOrange
import pl.edu.ur.blokur.presentation.propertytree.component.PropertyTreeNodeItem
import pl.edu.ur.blokur.presentation.propertytree.util.ApartmentFormState
import pl.edu.ur.blokur.presentation.propertytree.util.BuildingFormState
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyFormState
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeSelection
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeState
import pl.edu.ur.blokur.presentation.propertytree.util.StaircaseFormState

private enum class PropertyTreeSheetType {
    CREATE_PROPERTY,
    EDIT_PROPERTY,
    CREATE_BUILDING,
    EDIT_BUILDING,
    DELETE_BUILDING,
    CREATE_STAIRCASE,
    EDIT_STAIRCASE,
    DELETE_STAIRCASE,
    CREATE_APARTMENT,
    EDIT_APARTMENT,
    DELETE_APARTMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyTreeContent(
    state: PropertyTreeState.Success,
    onNodeSelected: (PropertyTreeSelection) -> Unit,
    onPropertyExpansionToggled: (String) -> Unit,
    onBuildingExpansionToggled: (String) -> Unit,
    onStaircaseExpansionToggled: (String) -> Unit,
    onPropertyFormChanged: (PropertyFormState) -> Unit,
    onNewPropertyFormChanged: (PropertyFormState) -> Unit,
    onBuildingFormChanged: (BuildingFormState) -> Unit,
    onNewBuildingFormChanged: (BuildingFormState) -> Unit,
    onStaircaseFormChanged: (StaircaseFormState) -> Unit,
    onNewStaircaseFormChanged: (StaircaseFormState) -> Unit,
    onApartmentFormChanged: (ApartmentFormState) -> Unit,
    onNewApartmentFormChanged: (ApartmentFormState) -> Unit,
    onCreateProperty: () -> Boolean,
    onUpdateProperty: () -> Boolean,
    onCreateBuilding: () -> Boolean,
    onUpdateBuilding: () -> Boolean,
    onDeleteBuilding: () -> Unit,
    onCreateStaircase: () -> Boolean,
    onUpdateStaircase: () -> Boolean,
    onDeleteStaircase: () -> Unit,
    onCreateApartment: () -> Boolean,
    onUpdateApartment: () -> Boolean,
    onDeleteApartment: () -> Unit
) {
    var activeSheet by remember(state.selectedNode) { mutableStateOf<PropertyTreeSheetType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TreeHeaderCard(
            onCreateProperty = { activeSheet = PropertyTreeSheetType.CREATE_PROPERTY }
        )

        if (state.properties.isEmpty()) {
            EmptyState(
                title = "Brak nieruchomości",
                description = "Dodaj pierwszą wspólnotę przyciskiem powyżej."
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.properties.forEach { property ->
                    val propertyExpanded = property.id in state.expandedPropertyIds
                    PropertyTreeNodeItem(
                        depth = 0,
                        title = property.name,
                        subtitle = property.address,
                        badge = "WSPÓLNOTA",
                        icon = Icons.Rounded.Domain,
                        accentColor = InfoBlue,
                        expandIndicator = property.expandIndicator(propertyExpanded),
                        selected = state.selectedNode == PropertyTreeSelection.PropertyNode(property.id),
                        trailingContent = {
                            CompactActionButton(
                                icon = Icons.Rounded.Edit,
                                contentDescription = "Edytuj wspólnotę",
                                onClick = {
                                    onNodeSelected(PropertyTreeSelection.PropertyNode(property.id))
                                    activeSheet = PropertyTreeSheetType.EDIT_PROPERTY
                                }
                            )
                            CompactActionButton(
                                icon = Icons.Outlined.Add,
                                contentDescription = "Dodaj budynek",
                                onClick = {
                                    onNodeSelected(PropertyTreeSelection.PropertyNode(property.id))
                                    activeSheet = PropertyTreeSheetType.CREATE_BUILDING
                                }
                            )
                        },
                        onClick = {
                            if (property.buildings.isNotEmpty()) {
                                onPropertyExpansionToggled(property.id)
                            }
                        }
                    )

                    if (propertyExpanded) {
                        property.buildings.forEach { building ->
                            val buildingExpanded = building.id in state.expandedBuildingIds
                            PropertyTreeNodeItem(
                                depth = 1,
                                title = building.name,
                                subtitle = building.address,
                                badge = "BUDYNEK",
                                icon = Icons.Rounded.HomeWork,
                                accentColor = SuccessGreen,
                                expandIndicator = building.expandIndicator(buildingExpanded),
                                selected = state.selectedNode == PropertyTreeSelection.BuildingNode(
                                    propertyId = property.id,
                                    buildingId = building.id
                                ),
                                trailingContent = {
                                    CompactActionButton(
                                        icon = Icons.Rounded.Edit,
                                        contentDescription = "Edytuj budynek",
                                        onClick = {
                                            onNodeSelected(
                                                PropertyTreeSelection.BuildingNode(
                                                    propertyId = property.id,
                                                    buildingId = building.id
                                                )
                                            )
                                            activeSheet = PropertyTreeSheetType.EDIT_BUILDING
                                        }
                                    )
                                    CompactActionButton(
                                        icon = Icons.Outlined.Add,
                                        contentDescription = "Dodaj klatkę",
                                        onClick = {
                                            onNodeSelected(
                                                PropertyTreeSelection.BuildingNode(
                                                    propertyId = property.id,
                                                    buildingId = building.id
                                                )
                                            )
                                            activeSheet = PropertyTreeSheetType.CREATE_STAIRCASE
                                        }
                                    )
                                    CompactActionButton(
                                        icon = Icons.Rounded.Delete,
                                        contentDescription = "Usuń budynek",
                                        onClick = {
                                            onNodeSelected(
                                                PropertyTreeSelection.BuildingNode(
                                                    propertyId = property.id,
                                                    buildingId = building.id
                                                )
                                            )
                                            activeSheet = PropertyTreeSheetType.DELETE_BUILDING
                                        }
                                    )
                                },
                                onClick = {
                                    if (building.staircases.isNotEmpty()) {
                                        onBuildingExpansionToggled(building.id)
                                    }
                                }
                            )

                            if (buildingExpanded) {
                                building.staircases.forEach { staircase ->
                                    val staircaseExpanded = staircase.id in state.expandedStaircaseIds
                                    PropertyTreeNodeItem(
                                        depth = 2,
                                        title = "Klatka ${staircase.label}",
                                        subtitle = "${staircase.apartments.size} lokali",
                                        badge = "KLATKA",
                                        icon = Icons.Rounded.Stairs,
                                        accentColor = WarningOrange,
                                        expandIndicator = staircase.expandIndicator(staircaseExpanded),
                                        selected = state.selectedNode == PropertyTreeSelection.StaircaseNode(
                                            propertyId = property.id,
                                            buildingId = building.id,
                                            staircaseId = staircase.id
                                        ),
                                        emphasized = staircaseExpanded,
                                        trailingContent = {
                                            CompactActionButton(
                                                icon = Icons.Rounded.Edit,
                                                contentDescription = "Edytuj klatkę",
                                                onClick = {
                                                    onNodeSelected(
                                                        PropertyTreeSelection.StaircaseNode(
                                                            propertyId = property.id,
                                                            buildingId = building.id,
                                                            staircaseId = staircase.id
                                                        )
                                                    )
                                                    activeSheet = PropertyTreeSheetType.EDIT_STAIRCASE
                                                }
                                            )
                                            CompactActionButton(
                                                icon = Icons.Outlined.Add,
                                                contentDescription = "Dodaj lokal",
                                                onClick = {
                                                    onNodeSelected(
                                                        PropertyTreeSelection.StaircaseNode(
                                                            propertyId = property.id,
                                                            buildingId = building.id,
                                                            staircaseId = staircase.id
                                                        )
                                                    )
                                                    activeSheet = PropertyTreeSheetType.CREATE_APARTMENT
                                                }
                                            )
                                            CompactActionButton(
                                                icon = Icons.Rounded.Delete,
                                                contentDescription = "Usuń klatkę",
                                                onClick = {
                                                    onNodeSelected(
                                                        PropertyTreeSelection.StaircaseNode(
                                                            propertyId = property.id,
                                                            buildingId = building.id,
                                                            staircaseId = staircase.id
                                                        )
                                                    )
                                                    activeSheet = PropertyTreeSheetType.DELETE_STAIRCASE
                                                }
                                            )
                                        },
                                        onClick = {
                                            if (staircase.apartments.isNotEmpty()) {
                                                onStaircaseExpansionToggled(staircase.id)
                                            }
                                        }
                                    )

                                    if (staircaseExpanded) {
                                        staircase.apartments.forEach { apartment ->
                                            PropertyTreeNodeItem(
                                                depth = 3,
                                                title = "Lokal ${apartment.number}",
                                                subtitle = apartmentSubtitle(apartment),
                                                badge = "LOKAL",
                                                icon = Icons.Rounded.Apartment,
                                                accentColor = MaterialTheme.colorScheme.primary,
                                                selected = state.selectedNode == PropertyTreeSelection.ApartmentNode(
                                                    propertyId = property.id,
                                                    buildingId = building.id,
                                                    staircaseId = staircase.id,
                                                    apartmentId = apartment.id
                                                ),
                                                emphasized = true,
                                                trailingContent = {
                                                    CompactActionButton(
                                                        icon = Icons.Rounded.Edit,
                                                        contentDescription = "Edytuj lokal",
                                                        onClick = {
                                                            onNodeSelected(
                                                                PropertyTreeSelection.ApartmentNode(
                                                                    propertyId = property.id,
                                                                    buildingId = building.id,
                                                                    staircaseId = staircase.id,
                                                                    apartmentId = apartment.id
                                                                )
                                                            )
                                                            activeSheet = PropertyTreeSheetType.EDIT_APARTMENT
                                                        }
                                                    )
                                                    CompactActionButton(
                                                        icon = Icons.Rounded.Delete,
                                                        contentDescription = "Usuń lokal",
                                                        onClick = {
                                                            onNodeSelected(
                                                                PropertyTreeSelection.ApartmentNode(
                                                                    propertyId = property.id,
                                                                    buildingId = building.id,
                                                                    staircaseId = staircase.id,
                                                                    apartmentId = apartment.id
                                                                )
                                                            )
                                                            activeSheet = PropertyTreeSheetType.DELETE_APARTMENT
                                                        }
                                                    )
                                                },
                                                onClick = {
                                                    onNodeSelected(
                                                        PropertyTreeSelection.ApartmentNode(
                                                            propertyId = property.id,
                                                            buildingId = building.id,
                                                            staircaseId = staircase.id,
                                                            apartmentId = apartment.id
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val dismiss = { activeSheet = null }

    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            when (activeSheet) {
                PropertyTreeSheetType.CREATE_PROPERTY -> FormSheetContent(
                    title = "Nowa wspólnota",
                    isSaving = state.isSaving,
                    onConfirm = { if (onCreateProperty()) dismiss() },
                    onDismiss = dismiss
                ) {
                    PropertyFormFields(state.newPropertyForm, !state.isSaving, onNewPropertyFormChanged)
                }

                PropertyTreeSheetType.EDIT_PROPERTY -> FormSheetContent(
                    title = "Edytuj wspólnotę",
                    isSaving = state.isSaving,
                    onConfirm = { if (onUpdateProperty()) dismiss() },
                    onDismiss = dismiss
                ) {
                    PropertyFormFields(state.propertyForm, !state.isSaving, onPropertyFormChanged)
                }

                PropertyTreeSheetType.CREATE_BUILDING -> FormSheetContent(
                    title = "Dodaj budynek",
                    isSaving = state.isSaving,
                    onConfirm = { if (onCreateBuilding()) dismiss() },
                    onDismiss = dismiss
                ) {
                    BuildingFormFields(state.newBuildingForm, !state.isSaving, onNewBuildingFormChanged)
                }

                PropertyTreeSheetType.EDIT_BUILDING -> FormSheetContent(
                    title = "Edytuj budynek",
                    isSaving = state.isSaving,
                    onConfirm = { if (onUpdateBuilding()) dismiss() },
                    onDismiss = dismiss
                ) {
                    BuildingFormFields(state.buildingForm, !state.isSaving, onBuildingFormChanged)
                }

                PropertyTreeSheetType.DELETE_BUILDING -> DeleteSheetContent(
                    title = "Usuń budynek",
                    message = "Czy na pewno chcesz usunąć budynek \"${state.buildingForm.name}\"?",
                    warning = "Usunięcie jest możliwe tylko wtedy, gdy budynek nie zawiera żadnych lokali.",
                    isSaving = state.isSaving,
                    onConfirm = { onDeleteBuilding(); dismiss() },
                    onDismiss = dismiss
                )

                PropertyTreeSheetType.CREATE_STAIRCASE -> FormSheetContent(
                    title = "Dodaj klatkę schodową",
                    isSaving = state.isSaving,
                    onConfirm = { if (onCreateStaircase()) dismiss() },
                    onDismiss = dismiss
                ) {
                    StaircaseFormFields(state.newStaircaseForm, !state.isSaving, onNewStaircaseFormChanged)
                }

                PropertyTreeSheetType.EDIT_STAIRCASE -> FormSheetContent(
                    title = "Edytuj klatkę schodową",
                    isSaving = state.isSaving,
                    onConfirm = { if (onUpdateStaircase()) dismiss() },
                    onDismiss = dismiss
                ) {
                    StaircaseFormFields(state.staircaseForm, !state.isSaving, onStaircaseFormChanged)
                }

                PropertyTreeSheetType.DELETE_STAIRCASE -> DeleteSheetContent(
                    title = "Usuń klatkę schodową",
                    message = "Czy na pewno chcesz usunąć klatkę \"${state.staircaseForm.label}\"?",
                    warning = "Usunięcie jest możliwe tylko wtedy, gdy klatka nie zawiera żadnych lokali.",
                    isSaving = state.isSaving,
                    onConfirm = { onDeleteStaircase(); dismiss() },
                    onDismiss = dismiss
                )

                PropertyTreeSheetType.CREATE_APARTMENT -> FormSheetContent(
                    title = "Dodaj lokal",
                    isSaving = state.isSaving,
                    onConfirm = { if (onCreateApartment()) dismiss() },
                    onDismiss = dismiss
                ) {
                    ApartmentFormFields(state.newApartmentForm, !state.isSaving, onNewApartmentFormChanged)
                }

                PropertyTreeSheetType.EDIT_APARTMENT -> FormSheetContent(
                    title = "Edytuj lokal",
                    isSaving = state.isSaving,
                    onConfirm = { if (onUpdateApartment()) dismiss() },
                    onDismiss = dismiss
                ) {
                    ApartmentFormFields(state.apartmentForm, !state.isSaving, onApartmentFormChanged)
                }

                PropertyTreeSheetType.DELETE_APARTMENT -> DeleteSheetContent(
                    title = "Usuń lokal",
                    message = "Czy na pewno chcesz usunąć lokal nr ${state.apartmentForm.number}?",
                    warning = null,
                    isSaving = state.isSaving,
                    onConfirm = { onDeleteApartment(); dismiss() },
                    onDismiss = dismiss
                )

                null -> Unit
            }
        }
    }
}

@Composable
private fun TreeHeaderCard(
    onCreateProperty: () -> Unit
) {
    NormalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Drzewo nieruchomości",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Zarządzaj strukturą wspólnot, budynków, klatek i lokali.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PrimaryButton(
                text = "Nowa wspólnota",
                onClick = onCreateProperty
            )
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FormSheetContent(
    title: String,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    fields: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        fields()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                modifier = Modifier.weight(1f),
                text = "Anuluj",
                onClick = onDismiss,
                enabled = !isSaving
            )
            PrimaryButton(
                modifier = Modifier.weight(1f),
                text = "Zapisz",
                onClick = onConfirm,
                enabled = !isSaving
            )
        }
    }
}

@Composable
private fun DeleteSheetContent(
    title: String,
    message: String,
    warning: String?,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (warning != null) {
            Text(
                text = warning,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Usuń",
            onClick = onConfirm,
            enabled = !isSaving
        )
        SecondaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Anuluj",
            onClick = onDismiss,
            enabled = !isSaving
        )
    }
}

@Composable
private fun PropertyFormFields(
    form: PropertyFormState,
    enabled: Boolean,
    onFormChanged: (PropertyFormState) -> Unit
) {
    FormSectionHeader("Dane wspólnoty")
    TextField("Nazwa wspólnoty", form.name, { onFormChanged(form.copy(name = it)) }, enabled = enabled)
    TextField("Adres", form.address, { onFormChanged(form.copy(address = it)) }, enabled = enabled)
    TextField(
        "NIP",
        form.nip,
        { onFormChanged(form.copy(nip = it.filter(Char::isDigit).take(10))) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    FormSectionDivider()
    FormSectionHeader("Dane kontaktowe zarządcy")
    TextField(
        "Telefon",
        form.managerPhone,
        { onFormChanged(form.copy(managerPhone = it)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
    TextField(
        "E-mail",
        form.managerEmail,
        { onFormChanged(form.copy(managerEmail = it)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
}

@Composable
private fun BuildingFormFields(
    form: BuildingFormState,
    enabled: Boolean,
    onFormChanged: (BuildingFormState) -> Unit
) {
    FormSectionHeader("Przynależność")
    TextField("Nazwa wspólnoty", form.estateName, { onFormChanged(form.copy(estateName = it)) }, enabled = enabled)
    FormSectionDivider()
    FormSectionHeader("Dane budynku")
    TextField("Nazwa budynku", form.name, { onFormChanged(form.copy(name = it)) }, enabled = enabled)
    TextField("Adres budynku", form.address, { onFormChanged(form.copy(address = it)) }, enabled = enabled)
    FormSectionDivider()
    FormSectionHeader("Lokalizacja (opcjonalnie)")
    TextField(
        "Szerokość geograficzna",
        form.latitude,
        { onFormChanged(form.copy(latitude = it)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    TextField(
        "Długość geograficzna",
        form.longitude,
        { onFormChanged(form.copy(longitude = it)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun StaircaseFormFields(
    form: StaircaseFormState,
    enabled: Boolean,
    onFormChanged: (StaircaseFormState) -> Unit
) {
    TextField(
        "Etykieta klatki (np. A, B, 1)",
        form.label,
        { onFormChanged(form.copy(label = it)) },
        enabled = enabled
    )
}

@Composable
private fun ApartmentFormFields(
    form: ApartmentFormState,
    enabled: Boolean,
    onFormChanged: (ApartmentFormState) -> Unit
) {
    FormSectionHeader("Dane lokalu")
    TextField("Numer lokalu", form.number, { onFormChanged(form.copy(number = it)) }, enabled = enabled)
    FormSectionDivider()
    FormSectionHeader("Szczegóły (opcjonalnie)")
    TextField(
        "Piętro",
        form.floor,
        { onFormChanged(form.copy(floor = it.filter { ch -> ch.isDigit() || ch == '-' })) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    TextField(
        "Powierzchnia (m²)",
        form.areaM2,
        { onFormChanged(form.copy(areaM2 = it)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
    FormSectionDivider()
    FormSectionHeader("Typ własności")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OwnershipTypeButton(
            text = "Własnościowy",
            selected = form.ownershipType == ApartmentOwnershipType.WLASNOSCIOWY,
            enabled = enabled,
            onClick = { onFormChanged(form.copy(ownershipType = ApartmentOwnershipType.WLASNOSCIOWY)) },
            modifier = Modifier.weight(1f)
        )
        OwnershipTypeButton(
            text = "Najem",
            selected = form.ownershipType == ApartmentOwnershipType.NAJEM,
            enabled = enabled,
            onClick = { onFormChanged(form.copy(ownershipType = ApartmentOwnershipType.NAJEM)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FormSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FormSectionDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun OwnershipTypeButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        PrimaryButton(text = text, onClick = onClick, enabled = enabled, modifier = modifier)
    } else {
        SecondaryButton(text = text, onClick = onClick, enabled = enabled, modifier = modifier)
    }
}

private fun apartmentSubtitle(apartment: ManagedApartment): String {
    val floorPart = apartment.floor?.let { "piętro $it" } ?: "bez piętra"
    val areaPart = apartment.areaM2?.let { "$it m²" } ?: "bez metrażu"
    return "$floorPart • $areaPart"
}

private fun ManagedProperty.expandIndicator(expanded: Boolean): Boolean? =
    if (buildings.isNotEmpty()) expanded else null

private fun pl.edu.ur.blokur.domain.model.ManagedBuilding.expandIndicator(expanded: Boolean): Boolean? =
    if (staircases.isNotEmpty()) expanded else null

private fun pl.edu.ur.blokur.domain.model.ManagedStaircase.expandIndicator(expanded: Boolean): Boolean? =
    if (apartments.isNotEmpty()) expanded else null
