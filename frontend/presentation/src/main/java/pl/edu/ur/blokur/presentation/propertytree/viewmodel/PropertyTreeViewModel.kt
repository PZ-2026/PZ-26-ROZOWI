package pl.edu.ur.blokur.presentation.propertytree.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.domain.model.ApartmentDraft
import pl.edu.ur.blokur.domain.model.BuildingDraft
import pl.edu.ur.blokur.domain.model.ManagedApartment
import pl.edu.ur.blokur.domain.model.ManagedBuilding
import pl.edu.ur.blokur.domain.model.ManagedProperty
import pl.edu.ur.blokur.domain.model.ManagedStaircase
import pl.edu.ur.blokur.domain.model.PropertyDraft
import pl.edu.ur.blokur.domain.model.StaircaseDraft
import pl.edu.ur.blokur.domain.usecase.CreateApartmentUseCase
import pl.edu.ur.blokur.domain.usecase.CreateBuildingUseCase
import pl.edu.ur.blokur.domain.usecase.CreatePropertyUseCase
import pl.edu.ur.blokur.domain.usecase.CreateStaircaseUseCase
import pl.edu.ur.blokur.domain.usecase.DeleteApartmentUseCase
import pl.edu.ur.blokur.domain.usecase.DeleteBuildingUseCase
import pl.edu.ur.blokur.domain.usecase.DeleteStaircaseUseCase
import pl.edu.ur.blokur.domain.usecase.GetPropertyTreeUseCase
import pl.edu.ur.blokur.domain.usecase.UpdateApartmentUseCase
import pl.edu.ur.blokur.domain.usecase.UpdateBuildingUseCase
import pl.edu.ur.blokur.domain.usecase.UpdatePropertyUseCase
import pl.edu.ur.blokur.domain.usecase.UpdateStaircaseUseCase
import pl.edu.ur.blokur.presentation.propertytree.util.ApartmentFormState
import pl.edu.ur.blokur.presentation.propertytree.util.BuildingFormState
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyFormState
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeEvent
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeSelection
import pl.edu.ur.blokur.presentation.propertytree.util.PropertyTreeState
import pl.edu.ur.blokur.presentation.propertytree.util.StaircaseFormState
import javax.inject.Inject

@HiltViewModel
class PropertyTreeViewModel @Inject constructor(
    private val getPropertyTreeUseCase: GetPropertyTreeUseCase,
    private val createPropertyUseCase: CreatePropertyUseCase,
    private val updatePropertyUseCase: UpdatePropertyUseCase,
    private val createBuildingUseCase: CreateBuildingUseCase,
    private val updateBuildingUseCase: UpdateBuildingUseCase,
    private val deleteBuildingUseCase: DeleteBuildingUseCase,
    private val createStaircaseUseCase: CreateStaircaseUseCase,
    private val updateStaircaseUseCase: UpdateStaircaseUseCase,
    private val deleteStaircaseUseCase: DeleteStaircaseUseCase,
    private val createApartmentUseCase: CreateApartmentUseCase,
    private val updateApartmentUseCase: UpdateApartmentUseCase,
    private val deleteApartmentUseCase: DeleteApartmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PropertyTreeState>(PropertyTreeState.Loading)
    val state: StateFlow<PropertyTreeState> = _state.asStateFlow()

    private val _events = Channel<PropertyTreeEvent>()
    val events: Flow<PropertyTreeEvent> = _events.receiveAsFlow()

    init {
        loadTree()
    }

    fun onNodeSelected(selection: PropertyTreeSelection) {
        val success = _state.value as? PropertyTreeState.Success ?: return
        _state.value = buildSuccessState(
            properties = success.properties,
            selection = selection,
            isSaving = false,
            expandedPropertyIds = success.expandedPropertyIds + selection.ancestorPropertyIds(),
            expandedBuildingIds = success.expandedBuildingIds + selection.ancestorBuildingIds(),
            expandedStaircaseIds = success.expandedStaircaseIds + selection.ancestorStaircaseIds()
        )
    }

    fun togglePropertyExpansion(propertyId: String) = updateSuccessState {
        copy(expandedPropertyIds = expandedPropertyIds.toggle(propertyId))
    }

    fun toggleBuildingExpansion(buildingId: String) = updateSuccessState {
        copy(expandedBuildingIds = expandedBuildingIds.toggle(buildingId))
    }

    fun toggleStaircaseExpansion(staircaseId: String) = updateSuccessState {
        copy(expandedStaircaseIds = expandedStaircaseIds.toggle(staircaseId))
    }

    fun collapseAllExpansions() = updateSuccessState {
        copy(
            expandedPropertyIds = emptySet(),
            expandedBuildingIds = emptySet(),
            expandedStaircaseIds = emptySet()
        )
    }

    fun onPropertyFormChanged(form: PropertyFormState) = updateSuccessState { copy(propertyForm = form) }
    fun onNewPropertyFormChanged(form: PropertyFormState) = updateSuccessState { copy(newPropertyForm = form) }
    fun onBuildingFormChanged(form: BuildingFormState) = updateSuccessState { copy(buildingForm = form) }
    fun onNewBuildingFormChanged(form: BuildingFormState) = updateSuccessState { copy(newBuildingForm = form) }
    fun onStaircaseFormChanged(form: StaircaseFormState) = updateSuccessState { copy(staircaseForm = form) }
    fun onNewStaircaseFormChanged(form: StaircaseFormState) = updateSuccessState { copy(newStaircaseForm = form) }
    fun onApartmentFormChanged(form: ApartmentFormState) = updateSuccessState { copy(apartmentForm = form) }
    fun onNewApartmentFormChanged(form: ApartmentFormState) = updateSuccessState { copy(newApartmentForm = form) }

    fun createProperty(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val draft = runValidation { success.newPropertyForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { createPropertyUseCase(draft) }
                .onSuccess { property ->
                    emitMessage("Dodano wspólnotę ${property.name}.")
                    loadTree(PropertyTreeSelection.PropertyNode(property.id))
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się dodać wspólnoty."))
                }
        }
        return true
    }

    fun updateProperty(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.PropertyNode ?: return false
        if (selection.propertyId.isBlank()) {
            viewModelScope.launch { emitMessage("Tej wspólnoty nie można edytować — brak identyfikatora.") }
            return false
        }
        val draft = runValidation { success.propertyForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { updatePropertyUseCase(selection.propertyId, draft) }
                .onSuccess {
                    emitMessage("Zapisano zmiany wspólnoty.")
                    loadTree(PropertyTreeSelection.PropertyNode(selection.propertyId))
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się zapisać wspólnoty."))
                }
        }
        return true
    }

    fun createBuilding(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.PropertyNode ?: return false
        if (selection.propertyId.isBlank()) {
            viewModelScope.launch { emitMessage("Najpierw wybierz wspólnotę z poprawnym identyfikatorem.") }
            return false
        }
        val draft = runValidation { success.newBuildingForm.toDraft(selection.propertyId) } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { createBuildingUseCase(draft) }
                .onSuccess { building ->
                    emitMessage("Dodano budynek ${building.name}.")
                    loadTree(PropertyTreeSelection.BuildingNode(selection.propertyId, building.id))
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się dodać budynku."))
                }
        }
        return true
    }

    fun updateBuilding(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.BuildingNode ?: return false
        val draft = runValidation { success.buildingForm.toDraft(selection.propertyId) } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { updateBuildingUseCase(selection.buildingId, draft) }
                .onSuccess {
                    emitMessage("Zapisano zmiany budynku.")
                    loadTree(selection)
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się zapisać budynku."))
                }
        }
        return true
    }

    fun deleteBuilding() {
        val success = _state.value as? PropertyTreeState.Success ?: return
        val selection = success.selectedNode as? PropertyTreeSelection.BuildingNode ?: return
        viewModelScope.launch {
            setSaving(true)
            runCatching {
                deleteBuildingUseCase(selection.buildingId)
            }.onSuccess {
                emitMessage("Usunięto budynek.")
                loadTree(PropertyTreeSelection.PropertyNode(selection.propertyId))
            }.onFailure { error ->
                setSaving(false)
                emitMessage(error.userMessage("Nie udało się usunąć budynku. Sprawdź, czy budynek nie zawiera lokali."))
            }
        }
    }

    fun createStaircase(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.BuildingNode ?: return false
        val draft = runValidation { success.newStaircaseForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { createStaircaseUseCase(selection.buildingId, draft) }
                .onSuccess { staircase ->
                    emitMessage("Dodano klatkę ${staircase.label}.")
                    loadTree(
                        PropertyTreeSelection.StaircaseNode(
                            propertyId = selection.propertyId,
                            buildingId = selection.buildingId,
                            staircaseId = staircase.id
                        )
                    )
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się dodać klatki."))
                }
        }
        return true
    }

    fun updateStaircase(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.StaircaseNode ?: return false
        val draft = runValidation { success.staircaseForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { updateStaircaseUseCase(selection.buildingId, selection.staircaseId, draft) }
                .onSuccess {
                    emitMessage("Zapisano zmiany klatki.")
                    loadTree(selection)
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się zapisać klatki."))
                }
        }
        return true
    }

    fun deleteStaircase() {
        val success = _state.value as? PropertyTreeState.Success ?: return
        val selection = success.selectedNode as? PropertyTreeSelection.StaircaseNode ?: return
        viewModelScope.launch {
            setSaving(true)
            runCatching {
                deleteStaircaseUseCase(selection.buildingId, selection.staircaseId)
            }.onSuccess {
                emitMessage("Usunięto klatkę schodową.")
                loadTree(PropertyTreeSelection.BuildingNode(selection.propertyId, selection.buildingId))
            }.onFailure { error ->
                setSaving(false)
                emitMessage(error.userMessage("Nie udało się usunąć klatki. Sprawdź, czy klatka nie zawiera lokali."))
            }
        }
    }

    fun createApartment(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.StaircaseNode ?: return false
        val draft = runValidation { success.newApartmentForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { createApartmentUseCase(selection.staircaseId, draft) }
                .onSuccess { apartment ->
                    emitMessage("Dodano lokal ${apartment.number}.")
                    loadTree(
                        PropertyTreeSelection.ApartmentNode(
                            propertyId = selection.propertyId,
                            buildingId = selection.buildingId,
                            staircaseId = selection.staircaseId,
                            apartmentId = apartment.id
                        )
                    )
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się dodać lokalu."))
                }
        }
        return true
    }

    fun updateApartment(): Boolean {
        val success = _state.value as? PropertyTreeState.Success ?: return false
        val selection = success.selectedNode as? PropertyTreeSelection.ApartmentNode ?: return false
        val draft = runValidation { success.apartmentForm.toDraft() } ?: return false
        viewModelScope.launch {
            setSaving(true)
            runCatching { updateApartmentUseCase(selection.staircaseId, selection.apartmentId, draft) }
                .onSuccess {
                    emitMessage("Zapisano zmiany lokalu.")
                    loadTree(selection)
                }.onFailure { error ->
                    setSaving(false)
                    emitMessage(error.userMessage("Nie udało się zapisać lokalu."))
                }
        }
        return true
    }

    fun deleteApartment() {
        val success = _state.value as? PropertyTreeState.Success ?: return
        val selection = success.selectedNode as? PropertyTreeSelection.ApartmentNode ?: return
        viewModelScope.launch {
            setSaving(true)
            runCatching {
                deleteApartmentUseCase(selection.staircaseId, selection.apartmentId)
            }.onSuccess {
                emitMessage("Usunięto lokal.")
                loadTree(
                    PropertyTreeSelection.StaircaseNode(
                        propertyId = selection.propertyId,
                        buildingId = selection.buildingId,
                        staircaseId = selection.staircaseId
                    )
                )
            }.onFailure { error ->
                setSaving(false)
                emitMessage(error.userMessage("Nie udało się usunąć lokalu."))
            }
        }
    }

    private fun loadTree(selectionOverride: PropertyTreeSelection? = null) {
        viewModelScope.launch {
            val previousState = _state.value as? PropertyTreeState.Success
            if (previousState == null) {
                _state.value = PropertyTreeState.Loading
            }
            runCatching {
                getPropertyTreeUseCase()
            }.onSuccess { properties ->
                val currentSelection = selectionOverride ?: previousState?.selectedNode
                val resolvedSelection = resolveSelection(properties, currentSelection)
                _state.value = buildSuccessState(
                    properties = properties,
                    selection = resolvedSelection,
                    isSaving = false,
                    expandedPropertyIds = (
                        previousState?.expandedPropertyIds.orEmpty() + resolvedSelection.ancestorPropertyIds()
                    ).filterExistingProperties(properties),
                    expandedBuildingIds = (
                        previousState?.expandedBuildingIds.orEmpty() + resolvedSelection.ancestorBuildingIds()
                    ).filterExistingBuildings(properties),
                    expandedStaircaseIds = (
                        previousState?.expandedStaircaseIds.orEmpty() + resolvedSelection.ancestorStaircaseIds()
                    ).filterExistingStaircases(properties)
                )
            }.onFailure { error ->
                _state.value = PropertyTreeState.Error(
                    error.userMessage("Wystąpił błąd podczas pobierania drzewa nieruchomości.")
                )
            }
        }
    }

    private fun buildSuccessState(
        properties: List<ManagedProperty>,
        selection: PropertyTreeSelection,
        isSaving: Boolean,
        expandedPropertyIds: Set<String> = emptySet(),
        expandedBuildingIds: Set<String> = emptySet(),
        expandedStaircaseIds: Set<String> = emptySet()
    ): PropertyTreeState.Success {
        val property = selection.findProperty(properties)
        val building = selection.findBuilding(properties)
        val staircase = selection.findStaircase(properties)
        val apartment = selection.findApartment(properties)

        return PropertyTreeState.Success(
            properties = properties,
            selectedNode = selection,
            expandedPropertyIds = expandedPropertyIds,
            expandedBuildingIds = expandedBuildingIds,
            expandedStaircaseIds = expandedStaircaseIds,
            propertyForm = property?.toForm() ?: PropertyFormState(),
            newPropertyForm = PropertyFormState(),
            buildingForm = building?.toForm() ?: BuildingFormState(),
            newBuildingForm = when {
                property != null -> BuildingFormState(estateName = property.name)
                building != null -> BuildingFormState(estateName = building.estateName)
                else -> BuildingFormState()
            },
            staircaseForm = staircase?.toForm() ?: StaircaseFormState(),
            newStaircaseForm = StaircaseFormState(),
            apartmentForm = apartment?.toForm() ?: ApartmentFormState(),
            newApartmentForm = ApartmentFormState(),
            isSaving = isSaving
        )
    }

    private fun resolveSelection(
        properties: List<ManagedProperty>,
        preferredSelection: PropertyTreeSelection?
    ): PropertyTreeSelection {
        val selection = preferredSelection ?: PropertyTreeSelection.Root
        return when (selection) {
            PropertyTreeSelection.Root -> {
                properties.firstOrNull()?.let { PropertyTreeSelection.PropertyNode(it.id) }
                    ?: PropertyTreeSelection.Root
            }
            is PropertyTreeSelection.PropertyNode -> {
                properties.firstOrNull { it.id == selection.propertyId }?.let {
                    PropertyTreeSelection.PropertyNode(it.id)
                } ?: properties.firstOrNull()?.let { PropertyTreeSelection.PropertyNode(it.id) }
                ?: PropertyTreeSelection.Root
            }
            is PropertyTreeSelection.BuildingNode -> {
                selection.findBuilding(properties)?.let {
                    PropertyTreeSelection.BuildingNode(selection.propertyId, it.id)
                } ?: resolveSelection(properties, PropertyTreeSelection.PropertyNode(selection.propertyId))
            }
            is PropertyTreeSelection.StaircaseNode -> {
                selection.findStaircase(properties)?.let {
                    PropertyTreeSelection.StaircaseNode(selection.propertyId, selection.buildingId, it.id)
                } ?: resolveSelection(
                    properties,
                    PropertyTreeSelection.BuildingNode(selection.propertyId, selection.buildingId)
                )
            }
            is PropertyTreeSelection.ApartmentNode -> {
                selection.findApartment(properties)?.let {
                    PropertyTreeSelection.ApartmentNode(
                        selection.propertyId, selection.buildingId, selection.staircaseId, it.id
                    )
                } ?: resolveSelection(
                    properties,
                    PropertyTreeSelection.StaircaseNode(
                        selection.propertyId, selection.buildingId, selection.staircaseId
                    )
                )
            }
        }
    }

    private fun updateSuccessState(
        transform: PropertyTreeState.Success.() -> PropertyTreeState.Success
    ) {
        val success = _state.value as? PropertyTreeState.Success ?: return
        _state.value = success.transform()
    }

    private fun setSaving(isSaving: Boolean) {
        updateSuccessState { copy(isSaving = isSaving) }
    }

    private suspend fun emitMessage(message: String) {
        _events.send(PropertyTreeEvent.ShowMessage(message))
    }

    private fun PropertyTreeSelection.findProperty(properties: List<ManagedProperty>): ManagedProperty? =
        when (this) {
            PropertyTreeSelection.Root -> null
            is PropertyTreeSelection.PropertyNode -> properties.find { it.id == propertyId }
            is PropertyTreeSelection.BuildingNode -> properties.find { it.id == propertyId }
            is PropertyTreeSelection.StaircaseNode -> properties.find { it.id == propertyId }
            is PropertyTreeSelection.ApartmentNode -> properties.find { it.id == propertyId }
        }

    private fun PropertyTreeSelection.findBuilding(properties: List<ManagedProperty>): ManagedBuilding? =
        when (this) {
            PropertyTreeSelection.Root,
            is PropertyTreeSelection.PropertyNode -> null
            is PropertyTreeSelection.BuildingNode -> properties
                .find { it.id == propertyId }?.buildings?.find { it.id == buildingId }
            is PropertyTreeSelection.StaircaseNode -> properties
                .find { it.id == propertyId }?.buildings?.find { it.id == buildingId }
            is PropertyTreeSelection.ApartmentNode -> properties
                .find { it.id == propertyId }?.buildings?.find { it.id == buildingId }
        }

    private fun PropertyTreeSelection.findStaircase(properties: List<ManagedProperty>): ManagedStaircase? =
        when (this) {
            PropertyTreeSelection.Root,
            is PropertyTreeSelection.PropertyNode,
            is PropertyTreeSelection.BuildingNode -> null
            is PropertyTreeSelection.StaircaseNode -> findBuilding(properties)
                ?.staircases?.find { it.id == staircaseId }
            is PropertyTreeSelection.ApartmentNode -> findBuilding(properties)
                ?.staircases?.find { it.id == staircaseId }
        }

    private fun PropertyTreeSelection.findApartment(properties: List<ManagedProperty>): ManagedApartment? =
        when (this) {
            PropertyTreeSelection.Root,
            is PropertyTreeSelection.PropertyNode,
            is PropertyTreeSelection.BuildingNode,
            is PropertyTreeSelection.StaircaseNode -> null
            is PropertyTreeSelection.ApartmentNode -> findStaircase(properties)
                ?.apartments?.find { it.id == apartmentId }
        }

    private fun ManagedProperty.toForm(): PropertyFormState = PropertyFormState(
        name = name,
        address = address,
        nip = nip,
        managerPhone = managerPhone.orEmpty(),
        managerEmail = managerEmail.orEmpty()
    )

    private fun ManagedBuilding.toForm(): BuildingFormState = BuildingFormState(
        estateName = estateName,
        name = name,
        address = address,
        latitude = latitude?.toString().orEmpty(),
        longitude = longitude?.toString().orEmpty()
    )

    private fun ManagedStaircase.toForm(): StaircaseFormState = StaircaseFormState(label = label)

    private fun ManagedApartment.toForm(): ApartmentFormState = ApartmentFormState(
        number = number,
        floor = floor?.toString().orEmpty(),
        areaM2 = areaM2?.toString().orEmpty(),
        ownershipType = ownershipType
    )

    private fun PropertyFormState.toDraft(): PropertyDraft {
        require(name.isNotBlank()) { "Nazwa wspólnoty jest wymagana." }
        require(name.length <= 255) { "Nazwa wspólnoty nie może przekraczać 255 znaków." }
        require(address.isNotBlank()) { "Adres wspólnoty jest wymagany." }
        require(address.length <= 255) { "Adres nie może przekraczać 255 znaków." }
        require(nip.isNotBlank()) { "NIP jest wymagany." }
        require(nip.matches(Regex("\\d{10}"))) { "NIP musi składać się z dokładnie 10 cyfr." }
        val phone = managerPhone.trim().ifBlank { null }
        require(phone == null || phone.length <= 20) { "Numer telefonu nie może przekraczać 20 znaków." }
        val email = managerEmail.trim().ifBlank { null }
        require(email == null || email.length <= 255) { "Adres e-mail nie może przekraczać 255 znaków." }
        require(email == null || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Adres e-mail zarządcy ma niepoprawny format."
        }
        return PropertyDraft(
            name = name.trim(),
            address = address.trim(),
            nip = nip,
            managerPhone = phone,
            managerEmail = email
        )
    }

    private fun BuildingFormState.toDraft(propertyId: String): BuildingDraft {
        require(estateName.isNotBlank()) { "Nazwa wspólnoty dla budynku jest wymagana." }
        require(estateName.length <= 255) { "Nazwa wspólnoty nie może przekraczać 255 znaków." }
        require(name.isNotBlank()) { "Nazwa budynku jest wymagana." }
        require(name.length <= 255) { "Nazwa budynku nie może przekraczać 255 znaków." }
        require(address.isNotBlank()) { "Adres budynku jest wymagany." }
        require(address.length <= 255) { "Adres nie może przekraczać 255 znaków." }
        val lat = latitude.parseOptionalDouble("Szerokość geograficzna")
        val lon = longitude.parseOptionalDouble("Długość geograficzna")
        require(lat == null || lat in -90.0..90.0) { "Szerokość geograficzna musi być z zakresu -90 do 90." }
        require(lon == null || lon in -180.0..180.0) { "Długość geograficzna musi być z zakresu -180 do 180." }
        return BuildingDraft(
            propertyId = propertyId,
            estateName = estateName.trim(),
            name = name.trim(),
            address = address.trim(),
            latitude = lat,
            longitude = lon
        )
    }

    private fun StaircaseFormState.toDraft(): StaircaseDraft {
        require(label.isNotBlank()) { "Etykieta klatki jest wymagana." }
        require(label.length <= 50) { "Etykieta klatki nie może przekraczać 50 znaków." }
        return StaircaseDraft(label = label.trim())
    }

    private fun ApartmentFormState.toDraft(): ApartmentDraft {
        require(number.isNotBlank()) { "Numer lokalu jest wymagany." }
        require(number.length <= 50) { "Numer lokalu nie może przekraczać 50 znaków." }
        return ApartmentDraft(
            number = number.trim(),
            floor = floor.parseOptionalInt("Piętro"),
            areaM2 = areaM2.parseOptionalDouble("Powierzchnia"),
            ownershipType = ownershipType
        )
    }

    private fun String.parseOptionalInt(label: String): Int? {
        if (isBlank()) return null
        return toIntOrNull() ?: error("$label musi być poprawną liczbą całkowitą.")
    }

    private fun String.parseOptionalDouble(label: String): Double? {
        if (isBlank()) return null
        return replace(',', '.').toDoubleOrNull() ?: error("$label musi być poprawną liczbą.")
    }

    private fun <T> runValidation(block: () -> T): T? {
        return try {
            block()
        } catch (e: IllegalArgumentException) {
            viewModelScope.launch { emitMessage(e.message ?: "Błąd walidacji.") }
            null
        }
    }

    private fun Throwable.userMessage(defaultMessage: String): String =
        message?.takeIf { it.isNotBlank() } ?: defaultMessage

    private fun Set<String>.toggle(id: String): Set<String> =
        if (id in this) this - id else this + id

    private fun PropertyTreeSelection.ancestorPropertyIds(): Set<String> = when (this) {
        PropertyTreeSelection.Root -> emptySet()
        is PropertyTreeSelection.PropertyNode -> setOf(propertyId)
        is PropertyTreeSelection.BuildingNode -> setOf(propertyId)
        is PropertyTreeSelection.StaircaseNode -> setOf(propertyId)
        is PropertyTreeSelection.ApartmentNode -> setOf(propertyId)
    }

    private fun PropertyTreeSelection.ancestorBuildingIds(): Set<String> = when (this) {
        PropertyTreeSelection.Root,
        is PropertyTreeSelection.PropertyNode -> emptySet()
        is PropertyTreeSelection.BuildingNode -> setOf(buildingId)
        is PropertyTreeSelection.StaircaseNode -> setOf(buildingId)
        is PropertyTreeSelection.ApartmentNode -> setOf(buildingId)
    }

    private fun PropertyTreeSelection.ancestorStaircaseIds(): Set<String> = when (this) {
        PropertyTreeSelection.Root,
        is PropertyTreeSelection.PropertyNode,
        is PropertyTreeSelection.BuildingNode -> emptySet()
        is PropertyTreeSelection.StaircaseNode -> setOf(staircaseId)
        is PropertyTreeSelection.ApartmentNode -> setOf(staircaseId)
    }

    private fun Set<String>.filterExistingProperties(properties: List<ManagedProperty>): Set<String> {
        val existingIds = properties.map { it.id }.toSet()
        return filterTo(linkedSetOf()) { it in existingIds }
    }

    private fun Set<String>.filterExistingBuildings(properties: List<ManagedProperty>): Set<String> {
        val existingIds = properties.flatMap { it.buildings.map { b -> b.id } }.toSet()
        return filterTo(linkedSetOf()) { it in existingIds }
    }

    private fun Set<String>.filterExistingStaircases(properties: List<ManagedProperty>): Set<String> {
        val existingIds = properties.flatMap { p ->
            p.buildings.flatMap { b -> b.staircases.map { s -> s.id } }
        }.toSet()
        return filterTo(linkedSetOf()) { it in existingIds }
    }
}
