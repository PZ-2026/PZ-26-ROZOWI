package pl.edu.ur.blokur.ui.views.properties.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.*
import pl.edu.ur.blokur.services.PropertyService
import pl.edu.ur.blokur.services.AdminUserApiService
import pl.edu.ur.blokur.ui.views.properties.utils.*
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PropertyTreeViewModel @Inject constructor(
    private val propertyService: PropertyService,
    private val adminUserApiService: AdminUserApiService
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

    private val _availableManagers = MutableStateFlow<List<String>>(emptyList())
    val availableManagers: StateFlow<List<String>> = _availableManagers.asStateFlow()

    // Stores the parent entity ID when initiating an ADD operation (e.g., buildingId for STAIRCASE)
    private val _addParentId = MutableStateFlow<String?>(null)

    fun loadAvailableManagers() {
        viewModelScope.launch {
            try {
                val response = adminUserApiService.getAllUsers()
                if (response.isSuccessful) {
                    val users = response.body() ?: emptyList()
                    val managers = users.filter { it.role == "ZARZADCA" && it.active }
                    _availableManagers.value = managers.map { it.email }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

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
            longitude = building.longitude?.toPlainString() ?: "",
            propertyId = building.propertyId
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
        if (_selectedNode.value is SelectedNode.Property) {
            loadAvailableManagers()
        }
    }

    fun startAdd(target: AddTarget, parentContext: String? = null) {
        _formError.value = null
        _formMode.value = FormMode.ADD
        _addTarget.value = target
        _addParentId.value = parentContext
        when (target) {
            AddTarget.PROPERTY -> {
                _propertyForm.value = PropertyFormFields()
                loadAvailableManagers()
            }
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
        _addParentId.value = null
    }

    // ─── Form updates ────────────────────────────────────────────────

    fun updatePropertyForm(fields: PropertyFormFields) { _propertyForm.value = fields }
    fun updateBuildingForm(fields: BuildingFormFields) { _buildingForm.value = fields }
    fun updateStaircaseForm(fields: StaircaseFormFields) { _staircaseForm.value = fields }
    fun updateApartmentForm(fields: ApartmentFormFields) { _apartmentForm.value = fields }

    // ─── Save ────────────────────────────────────────────────────────

    private fun validateForm(): String? {
        val mode = _formMode.value
        val target = if (mode == FormMode.ADD) _addTarget.value else when (_selectedNode.value) {
            is SelectedNode.Property -> AddTarget.PROPERTY
            is SelectedNode.Building -> AddTarget.BUILDING
            is SelectedNode.Staircase -> AddTarget.STAIRCASE
            is SelectedNode.Apartment -> AddTarget.APARTMENT
            else -> null
        }

        when (target) {
            AddTarget.PROPERTY -> {
                val f = _propertyForm.value
                if (f.name.isBlank()) return "Nazwa wspólnoty nie może być pusta"
                if (f.address.isBlank()) return "Adres wspólnoty nie może być pusty"
                if (f.nip.isBlank()) return "NIP nie może być pusty"
                if (!f.nip.matches(Regex("\\d{10}"))) return "NIP musi składać się z dokładnie 10 cyfr"
                if (f.managerEmail.isNotBlank() && !f.managerEmail.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"))) {
                    return "Niepoprawny format adresu e-mail zarządcy"
                }
            }
            AddTarget.BUILDING -> {
                val f = _buildingForm.value
                if (f.estateName.isBlank()) return "Nazwa osiedla nie może być pusta"
                if (f.name.isBlank()) return "Nazwa budynku nie może być pusta"
                if (f.address.isBlank()) return "Adres budynku nie może być pusty"
                if (f.latitude.isNotBlank()) {
                    val lat = f.latitude.toSafeBigDecimalOrNull()
                    if (lat == null) return "Niepoprawny format szerokości geograficznej"
                    if (lat < java.math.BigDecimal("-90") || lat > java.math.BigDecimal("90")) {
                        return "Szerokość geograficzna musi być w przedziale od -90 do 90"
                    }
                }
                if (f.longitude.isNotBlank()) {
                    val lon = f.longitude.toSafeBigDecimalOrNull()
                    if (lon == null) return "Niepoprawny format długości geograficznej"
                    if (lon < java.math.BigDecimal("-180") || lon > java.math.BigDecimal("180")) {
                        return "Długość geograficzna musi być w przedziale od -180 do 180"
                    }
                }
            }
            AddTarget.STAIRCASE -> {
                val f = _staircaseForm.value
                if (f.label.isBlank()) return "Etykieta klatki nie może być pusta"
            }
            AddTarget.APARTMENT -> {
                val f = _apartmentForm.value
                if (f.number.isBlank()) return "Numer lokalu nie może być pusty"
                if (f.floor.isNotBlank() && f.floor.toIntOrNull() == null) return "Piętro musi być liczbą całkowitą"
                if (f.areaM2.isNotBlank() && f.areaM2.toSafeBigDecimalOrNull() == null) return "Metraż musi być liczbą"
            }
            null -> {}
        }
        return null
    }

    private suspend fun validateManagerEmail(email: String): String? {
        try {
            val response = adminUserApiService.getAllUsers()
            if (!response.isSuccessful) {
                return "Błąd podczas weryfikacji adresu e-mail zarządcy."
            }
            val users = response.body() ?: emptyList()
            val matchedUser = users.find { it.email.equals(email, ignoreCase = true) }
            if (matchedUser == null) {
                return "Użytkownik o podanym adresie e-mail nie istnieje."
            }
            if (matchedUser.role != "ZARZADCA") {
                val roleName = when (matchedUser.role) {
                    "KONSERWATOR" -> "konserwatorem"
                    "MIESZKANIEC" -> "mieszkańcem"
                    else -> matchedUser.role.lowercase()
                }
                return "Użytkownik o tym adresie e-mail jest $roleName. Zarządcą nieruchomości może być tylko użytkownik z rolą Zarządca."
            }
            if (!matchedUser.active) {
                return "Wybrany zarządca jest nieaktywny."
            }
        } catch (e: Exception) {
            return "Błąd połączenia podczas weryfikacji zarządcy."
        }
        return null
    }

    fun save() {
        val validationError = validateForm()
        if (validationError != null) {
            _formError.value = validationError
            viewModelScope.launch {
                _events.send(PropertyTreeEvent.ShowSnackbar(validationError))
            }
            return
        }

        val mode = _formMode.value
        val target = if (mode == FormMode.ADD) _addTarget.value else when (_selectedNode.value) {
            is SelectedNode.Property -> AddTarget.PROPERTY
            is SelectedNode.Building -> AddTarget.BUILDING
            is SelectedNode.Staircase -> AddTarget.STAIRCASE
            is SelectedNode.Apartment -> AddTarget.APARTMENT
            else -> null
        }

        viewModelScope.launch {
            _isSaving.value = true
            _formError.value = null

            // Suspend validation for manager email if target is PROPERTY
            if (target == AddTarget.PROPERTY) {
                val email = _propertyForm.value.managerEmail.trim()
                if (email.isNotBlank()) {
                    val emailError = validateManagerEmail(email)
                    if (emailError != null) {
                        _formError.value = emailError
                        _events.send(PropertyTreeEvent.ShowSnackbar(emailError))
                        _isSaving.value = false
                        return@launch
                    }
                }
            }

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
                        latitude = f.latitude.toSafeBigDecimalOrNull(),
                        longitude = f.longitude.toSafeBigDecimalOrNull(),
                        propertyId = f.propertyId
                    )
                )
            }
            AddTarget.STAIRCASE -> {
                // Prefer the parent ID passed from the tree row button; fall back to selected node
                val buildingId = _addParentId.value
                    ?: (_selectedNode.value as? SelectedNode.Building)?.building?.id
                    ?: throw Exception("Nie wybrano budynku")
                propertyService.createStaircase(buildingId, StaircaseRequestDto(_staircaseForm.value.label))
            }
            AddTarget.APARTMENT -> {
                // Prefer the parent ID passed from the tree row button; fall back to selected node
                val staircaseId = _addParentId.value
                    ?: (_selectedNode.value as? SelectedNode.Staircase)?.staircase?.id
                    ?: throw Exception("Nie wybrano klatki")
                val f = _apartmentForm.value
                propertyService.createApartment(
                    staircaseId,
                    ApartmentRequestDto(f.number, f.floor.toIntOrNull(), f.areaM2.toSafeBigDecimalOrNull(), f.ownershipType)
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
                        latitude = f.latitude.toSafeBigDecimalOrNull(),
                        longitude = f.longitude.toSafeBigDecimalOrNull(),
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
                    ApartmentRequestDto(f.number, f.floor.toIntOrNull(), f.areaM2.toSafeBigDecimalOrNull(), f.ownershipType)
                )
            }
            SelectedNode.None -> {}
        }
    }

    fun deleteNode(target: DeleteTarget) {
        viewModelScope.launch {
            runCatching {
                when (target) {
                    is DeleteTarget.Building -> propertyService.deleteBuilding(target.id)
                    is DeleteTarget.Staircase -> propertyService.deleteStaircase(target.buildingId, target.staircaseId)
                    is DeleteTarget.Apartment -> propertyService.deleteApartment(target.staircaseId, target.apartmentId)
                }
            }.onSuccess {
                _events.send(PropertyTreeEvent.ShowSnackbar("Usunięto pomyślnie"))
                loadTree()
            }.onFailure { e ->
                _events.send(PropertyTreeEvent.ShowSnackbar(e.message ?: "Błąd usuwania elementu"))
            }
        }
    }

    private fun String.toSafeBigDecimalOrNull(): BigDecimal? {
        val clean = this.trim().replace(',', '.')
        if (clean.isBlank()) return null
        return clean.toBigDecimalOrNull()
    }
}
