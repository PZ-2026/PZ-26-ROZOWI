package pl.edu.ur.blokur.presentation.propertytree.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.propertytree.content.PropertyTreeContent
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeEvent
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeState
import pl.edu.ur.blokur.presentation.propertytree.viewmodel.PropertyTreeViewModel

@Composable
fun PropertyTreeScreen(
    viewModel: PropertyTreeViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PropertyTreeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val currentState = state) {
                PropertyTreeState.Loading -> LoadingIndicator()
                is PropertyTreeState.Error -> {
                    EmptyState(
                        title = "Nie udało się wczytać drzewa",
                        description = currentState.message
                    )
                }
                is PropertyTreeState.Success -> {
                    PropertyTreeContent(
                        state = currentState,
                        onNodeSelected = viewModel::onNodeSelected,
                        onPropertyExpansionToggled = viewModel::togglePropertyExpansion,
                        onBuildingExpansionToggled = viewModel::toggleBuildingExpansion,
                        onStaircaseExpansionToggled = viewModel::toggleStaircaseExpansion,
                        onPropertyFormChanged = viewModel::onPropertyFormChanged,
                        onNewPropertyFormChanged = viewModel::onNewPropertyFormChanged,
                        onBuildingFormChanged = viewModel::onBuildingFormChanged,
                        onNewBuildingFormChanged = viewModel::onNewBuildingFormChanged,
                        onStaircaseFormChanged = viewModel::onStaircaseFormChanged,
                        onNewStaircaseFormChanged = viewModel::onNewStaircaseFormChanged,
                        onApartmentFormChanged = viewModel::onApartmentFormChanged,
                        onNewApartmentFormChanged = viewModel::onNewApartmentFormChanged,
                        onCreateProperty = { viewModel.createProperty() },
                        onUpdateProperty = { viewModel.updateProperty() },
                        onCreateBuilding = { viewModel.createBuilding() },
                        onUpdateBuilding = { viewModel.updateBuilding() },
                        onDeleteBuilding = viewModel::deleteBuilding,
                        onCreateStaircase = { viewModel.createStaircase() },
                        onUpdateStaircase = { viewModel.updateStaircase() },
                        onDeleteStaircase = viewModel::deleteStaircase,
                        onCreateApartment = { viewModel.createApartment() },
                        onUpdateApartment = { viewModel.updateApartment() },
                        onDeleteApartment = viewModel::deleteApartment
                    )
                }
            }
        }
    }
}
