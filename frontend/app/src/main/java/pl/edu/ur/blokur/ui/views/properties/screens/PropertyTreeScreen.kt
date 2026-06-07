package pl.edu.ur.blokur.ui.views.properties.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.properties.contents.PropertyDetailPanel
import pl.edu.ur.blokur.ui.views.properties.contents.PropertyTreeView
import pl.edu.ur.blokur.ui.views.properties.contents.PropertyTreeView
import pl.edu.ur.blokur.ui.views.properties.utils.*
import pl.edu.ur.blokur.ui.views.properties.viewmodels.PropertyTreeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyTreeScreen(
    viewModel: PropertyTreeViewModel,
    modifier: Modifier = Modifier,
    onNavigateToMeters: (String) -> Unit = {},
    onNavigateToLedger: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val formMode by viewModel.formMode.collectAsState()
    val addTarget by viewModel.addTarget.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val formError by viewModel.formError.collectAsState()

    val propertyForm by viewModel.propertyForm.collectAsState()
    val buildingForm by viewModel.buildingForm.collectAsState()
    val staircaseForm by viewModel.staircaseForm.collectAsState()
    val apartmentForm by viewModel.apartmentForm.collectAsState()
    val availableManagers by viewModel.availableManagers.collectAsState()

    val expandedProperties by viewModel.expandedProperties.collectAsState()
    val expandedBuildings by viewModel.expandedBuildings.collectAsState()
    val expandedStaircases by viewModel.expandedStaircases.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PropertyTreeEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is PropertyTreeEvent.TreeRefreshed -> {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val s = state) {
            is PropertyTreeState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is PropertyTreeState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    FilledTonalButton(onClick = viewModel::loadTree) {
                        Text("Spróbuj ponownie")
                    }
                }
            }
            is PropertyTreeState.Success -> {
                PropertyTreeView(
                    properties = s.properties,
                    buildings = s.buildings,
                    expandedProperties = expandedProperties,
                    expandedBuildings = expandedBuildings,
                    expandedStaircases = expandedStaircases,
                    onToggleProperty = viewModel::toggleProperty,
                    onToggleBuilding = viewModel::toggleBuilding,
                    onToggleStaircase = viewModel::toggleStaircase,
                    onSelectProperty = viewModel::selectProperty,
                    onSelectBuilding = viewModel::selectBuilding,
                    onSelectStaircase = viewModel::selectStaircase,
                    onSelectApartment = viewModel::selectApartment,
                    onAdd = { target, parentId -> viewModel.startAdd(target, parentId) },
                    onDelete = { deleteTarget = it }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissSheet,
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                PropertyDetailPanel(
                    selectedNode = selectedNode,
                    formMode = formMode,
                    addTarget = addTarget,
                    isSaving = isSaving,
                    formError = formError,
                    propertyForm = propertyForm,
                    buildingForm = buildingForm,
                    staircaseForm = staircaseForm,
                    apartmentForm = apartmentForm,
                    availableManagers = availableManagers,
                    onPropertyFormChange = viewModel::updatePropertyForm,
                    onBuildingFormChange = viewModel::updateBuildingForm,
                    onStaircaseFormChange = viewModel::updateStaircaseForm,
                    onApartmentFormChange = viewModel::updateApartmentForm,
                    onEdit = viewModel::startEdit,
                    onSave = viewModel::save,
                    onDismiss = viewModel::dismissSheet,
                    onNavigateToMeters = onNavigateToMeters,
                    onNavigateToLedger = onNavigateToLedger
                )
            }
        }

        if (deleteTarget != null) {
            val target = deleteTarget!!
            val title = when (target) {
                is DeleteTarget.Building -> "Usuń budynek"
                is DeleteTarget.Staircase -> "Usuń klatkę"
                is DeleteTarget.Apartment -> "Usuń lokal"
            }
            val text = when (target) {
                is DeleteTarget.Building -> "Czy na pewno chcesz usunąć budynek ${target.name}?"
                is DeleteTarget.Staircase -> "Czy na pewno chcesz usunąć klatkę ${target.label}?"
                is DeleteTarget.Apartment -> "UWAGA: Usunięcie lokalu ${target.number} bezpowrotnie usunie wszystkie przypisane do niego liczniki oraz historię odczytów! Upewnij się, że lokal nie posiada ważnych danych.\n\nCzy na pewno chcesz kontynuować?"
            }

            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text(title) },
                text = { Text(text) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteNode(target)
                        deleteTarget = null
                    }) {
                        Text("Usuń", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text("Anuluj")
                    }
                }
            )
        }
    }
}
