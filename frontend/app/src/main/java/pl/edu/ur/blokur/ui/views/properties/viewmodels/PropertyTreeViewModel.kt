package pl.edu.ur.blokur.ui.views.properties.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.*
import pl.edu.ur.blokur.services.PropertyService
import pl.edu.ur.blokur.ui.views.properties.utils.*
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PropertyTreeViewModel @Inject constructor(
    private val propertyService: PropertyService
) : ViewModel() {

    private val _state = MutableStateFlow<PropertyTreeState>(PropertyTreeState.Loading)
    val state: StateFlow<PropertyTreeState> = _state.asStateFlow()

    private val _selectedNode = MutableStateFlow<SelectedNode>(SelectedNode.None)
    val selectedNode: StateFlow<SelectedNode> = _selectedNode.asStateFlow()

    private val _formMode = MutableStateFlow(FormMode.VIEW)
    val formMode: StateFlow<FormMode> = _formMode.asStateFlow()

    private val _addTarget = MutableStateFlow<AddTarget?>(null)
    val addTarget: StateFlow<AddTarget?> = _addTarget.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    // Form fields
    private val _propertyForm = MutableStateFlow(PropertyFormFields())
    val propertyForm: StateFlow<PropertyFormFields> = _propertyForm.asStateFlow()

    private val _buildingForm = MutableStateFlow(BuildingFormFields())
    val buildingForm: StateFlow<BuildingFormFields> = _buildingForm.asStateFlow()

    private val _staircaseForm = MutableStateFlow(StaircaseFormFields())
    val staircaseForm: StateFlow<StaircaseFormFields> = _staircaseForm.asStateFlow()

    private val _apartmentForm = MutableStateFlow(ApartmentFormFields())
    val apartmentForm: StateFlow<ApartmentFormFields> = _apartmentForm.asStateFlow()

    private val _events = Channel<PropertyTreeEvent>()
    val events: Flow<PropertyTreeEvent> = _events.receiveAsFlow()

    // Expanded state tracking
    private val _expandedProperties = MutableStateFlow<Set<String>>(emptySet())
    val expandedProperties: StateFlow<Set<String>> = _expandedProperties.asStateFlow()

    private val _expandedBuildings = MutableStateFlow<Set<String>>(emptySet())
    val expandedBuildings: StateFlow<Set<String>> = _expandedBuildings.asStateFlow()

    private val _expandedStaircases = MutableStateFlow<Set<String>>(emptySet())
    val expandedStaircases: StateFlow<Set<String>> = _expandedStaircases.asStateFlow()

    init {
        loadTree()
    }

    fun loadTree() {
        viewModelScope.launch {
            _state.value = PropertyTreeState.Loading
            runCatching {
                val properties = propertyService.getProperties()
                val buildings = propertyService.getBuildingTree()
                PropertyTreeState.Success(properties, buildings)
            }.onSuccess { _state.value = it }
             .onFailure { _state.value = PropertyTreeState.Error(it.message ?: "Błąd ładowania") }
        }
    }

    fun toggleProperty(name: String) {
        _expandedProperties.value = _expandedProperties.value.let {
            if (name in it) it - name else it + name
        }
    }

    fun toggleBuilding(id: String) {
        _expandedBuildings.value = _expandedBuildings.value.let {
            if (id in it) it - id else it + id
        }
    }

    fun toggleStaircase(id: String) {
        _expandedStaircases.value = _expandedStaircases.value.let {
            if (id in it) it - id else it + id
        }
    }

    // ─── Node selection ──────────────────────────────────────────────

    fun selectProperty(property: PropertyResponseDto) {
        _formError.value = null
        _selectedNode.value = SelectedNode.Property(property)
        _formMode.value = FormMode.VIEW
        _propertyForm.value = PropertyFormFields(
            name = property.name,
            address = property.address,
            nip = property.nip ?: "",
            managerPhone = property.managerPhone ?: "",
            managerEmail = property.managerEmail ?: ""
        )
        _showBottomSheet.value = true
    }

    fun selectBuilding(building: BuildingTreeNodeDto) {
        _formError.value = null
        _selectedNode.value = SelectedNode.Building(building)
        _formMode.value = FormMode.VIEW
        _buildingForm.value = BuildingFormFields(
            estateName = building.estateName ?: "",
            name = building.name,
            address = building.address,
            latitude = building.latitude?.toPlainString() ?: "",
            longitude = building.longitude?.toPlainString() ?: ""
        )
        _showBottomSheet.value = true
    }

    fun selectStaircase(staircase: StaircaseNodeDto, buildingId: String) {
        _formError.value = null
        _selectedNode.value = SelectedNode.Staircase(staircase, buildingId)
        _formMode.value = FormMode.VIEW
        _staircaseForm.value = StaircaseFormFields(label = staircase.label)
        _showBottomSheet.value = true
    }

    fun selectApartment(apartment: ApartmentNodeDto, staircaseId: String) {
        _formError.value = null
        _selectedNode.value = SelectedNode.Apartment(apartment, staircaseId)
        _formMode.value = FormMode.VIEW
        _apartmentForm.value = ApartmentFormFields(
            number = apartment.number,
            floor = apartment.floor?.toString() ?: "",
            areaM2 = apartment.areaM2?.toPlainString() ?: "",
            ownershipType = apartment.ownershipType ?: "WLASNOSCIOWY"
        )
        _showBottomSheet.value = true
    }

    // ─── Mode switching ──────────────────────────────────────────────

    fun startEdit() {
        _formError.value = null
        _formMode.value = FormMode.EDIT
    }

    fun startAdd(target: AddTarget, parentContext: String? = null) {
        _formError.value = null
        _formMode.value = FormMode.ADD
        _addTarget.value = target
        when (target) {
            AddTarget.PROPERTY -> _propertyForm.value = PropertyFormFields()
            AddTarget.BUILDING -> _buildingForm.value = BuildingFormFields(propertyId = parentContext)
            AddTarget.STAIRCASE -> _staircaseForm.value = StaircaseFormFields()
            AddTarget.APARTMENT -> _apartmentForm.value = ApartmentFormFields()
        }
        _showBottomSheet.value = true
    }

    fun dismissSheet() {
        _formError.value = null
        _showBottomSheet.value = false
        _formMode.value = FormMode.VIEW
        _addTarget.value = null
    }

    // ─── Form updates ────────────────────────────────────────────────

    fun updatePropertyForm(fields: PropertyFormFields) { _propertyForm.value = fields }
    fun updateBuildingForm(fields: BuildingFormFields) { _buildingForm.value = fields }
    fun updateStaircaseForm(fields: StaircaseFormFields) { _staircaseForm.value = fields }
    fun updateApartmentForm(fields: ApartmentFormFields) { _apartmentForm.value = fields }

    // ─── Save ────────────────────────────────────────────────────────

    fun save() {
        val mode = _formMode.value
        viewModelScope.launch {
            _isSaving.value = true
            runCatching {
                when {
                    mode == FormMode.ADD -> saveNew()
                    mode == FormMode.EDIT -> saveEdit()
                }
            }.onSuccess {
                _events.send(PropertyTreeEvent.ShowSnackbar("Zapisano pomyślnie"))
                _formError.value = null
                _showBottomSheet.value = false
                _formMode.value = FormMode.VIEW
                loadTree()
            }.onFailure { e ->
                _formError.value = e.message ?: "Wystąpił nieznany błąd podczas zapisu"
            }
            _isSaving.value = false
        }
    }

    private suspend fun saveNew() {
        when (_addTarget.value) {
            AddTarget.PROPERTY -> {
                val f = _propertyForm.value
                propertyService.createProperty(
                    PropertyRequestDto(f.name, f.address, f.nip, f.managerPhone.ifBlank { null }, f.managerEmail.ifBlank { null })
                )
            }
            AddTarget.BUILDING -> {
                val f = _buildingForm.value
                propertyService.createBuilding(
                    BuildingRequestDto(
                        estateName = f.estateName.ifBlank { null },
                        name = f.name, address = f.address,
                        latitude = f.latitude.toBigDecimalOrNull(),
                        longitude = f.longitude.toBigDecimalOrNull(),
                        propertyId = f.propertyId
                    )
                )
            }
            AddTarget.STAIRCASE -> {
                val node = _selectedNode.value
                val buildingId = when (node) {
                    is SelectedNode.Building -> node.building.id
                    else -> throw Exception("Nie wybrano budynku")
                }
                propertyService.createStaircase(buildingId, StaircaseRequestDto(_staircaseForm.value.label))
            }
            AddTarget.APARTMENT -> {
                val node = _selectedNode.value
                val staircaseId = when (node) {
                    is SelectedNode.Staircase -> node.staircase.id
                    else -> throw Exception("Nie wybrano klatki")
                }
                val f = _apartmentForm.value
                propertyService.createApartment(
                    staircaseId,
                    ApartmentRequestDto(f.number, f.floor.toIntOrNull(), f.areaM2.toBigDecimalOrNull(), f.ownershipType)
                )
            }
            null -> {}
        }
    }

    private suspend fun saveEdit() {
        when (val node = _selectedNode.value) {
            is SelectedNode.Property -> {
                val f = _propertyForm.value
                propertyService.updateProperty(
                    node.property.id,
                    PropertyRequestDto(f.name, f.address, f.nip, f.managerPhone.ifBlank { null }, f.managerEmail.ifBlank { null })
                )
            }
            is SelectedNode.Building -> {
                val f = _buildingForm.value
                propertyService.updateBuilding(
                    node.building.id,
                    BuildingRequestDto(
                        estateName = f.estateName.ifBlank { null },
                        name = f.name, address = f.address,
                        latitude = f.latitude.toBigDecimalOrNull(),
                        longitude = f.longitude.toBigDecimalOrNull(),
                        propertyId = f.propertyId
                    )
                )
            }
            is SelectedNode.Staircase -> {
                propertyService.updateStaircase(
                    node.buildingId, node.staircase.id,
                    StaircaseRequestDto(_staircaseForm.value.label)
                )
            }
            is SelectedNode.Apartment -> {
                val f = _apartmentForm.value
                propertyService.updateApartment(
                    node.staircaseId, node.apartment.id,
                    ApartmentRequestDto(f.number, f.floor.toIntOrNull(), f.areaM2.toBigDecimalOrNull(), f.ownershipType)
                )
            }
            SelectedNode.None -> {}
        }
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        takeIf { it.isNotBlank() }?.toBigDecimalOrNull()
}
