package pl.edu.ur.blokur.ui.views.inspections.viewmodels

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
import pl.edu.ur.blokur.dtos.InspectionRequestDto
import pl.edu.ur.blokur.dtos.InspectionResponseDto
import pl.edu.ur.blokur.dtos.ScopeType
import pl.edu.ur.blokur.services.InspectionService
import pl.edu.ur.blokur.services.PropertyService
import javax.inject.Inject

sealed interface InspectionEvent {
    data class ShowSnackbar(val message: String) : InspectionEvent
}

sealed interface InspectionsListState {
    data object Loading : InspectionsListState
    data class Error(val message: String) : InspectionsListState
    data class Success(val inspections: List<InspectionResponseDto>) : InspectionsListState
}

data class CreateInspectionFormState(
    val title: String = "",
    val description: String = "",
    val scheduledAt: String = "",         // "YYYY-MM-DDTHH:MM:SS"
    val scopeType: ScopeType = ScopeType.BUDYNEK,
    val scopeId: String = "",
    val availableScopes: List<Pair<String, String>> = emptyList(), // id to name
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = title.isNotBlank() && scheduledAt.isNotBlank() && scopeId.isNotBlank()
}

@HiltViewModel
class InspectionsListViewModel @Inject constructor(
    private val inspectionService: InspectionService,
    private val propertyService: PropertyService
) : ViewModel() {

    private val _state = MutableStateFlow<InspectionsListState>(InspectionsListState.Loading)
    val state: StateFlow<InspectionsListState> = _state.asStateFlow()

    private val _events = Channel<InspectionEvent>()
    val events: Flow<InspectionEvent> = _events.receiveAsFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _formState = MutableStateFlow(CreateInspectionFormState())
    val formState: StateFlow<CreateInspectionFormState> = _formState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = InspectionsListState.Loading
            runCatching { inspectionService.getAll() }
                .onSuccess { list ->
                    _state.value = InspectionsListState.Success(
                        inspections = list.sortedBy { it.scheduledAt }
                    )
                }
                .onFailure { e ->
                    _state.value = InspectionsListState.Error(e.message ?: "Błąd ładowania przeglądów")
                }
        }
    }

    fun openCreateDialog() {
        viewModelScope.launch {
            _formState.value = CreateInspectionFormState()
            loadScopesForForm(ScopeType.BUDYNEK)
            _showCreateDialog.value = true
        }
    }

    fun closeCreateDialog() { _showCreateDialog.value = false }

    fun onTitleChanged(v: String) { _formState.value = _formState.value.copy(title = v) }
    fun onDescriptionChanged(v: String) { _formState.value = _formState.value.copy(description = v) }
    fun onScheduledAtChanged(v: String) { _formState.value = _formState.value.copy(scheduledAt = v) }
    fun onScopeIdChanged(v: String) { _formState.value = _formState.value.copy(scopeId = v) }
    
    fun onScopeTypeChanged(type: ScopeType) {
        _formState.value = _formState.value.copy(scopeType = type, scopeId = "")
        loadScopesForForm(type)
    }

    private fun loadScopesForForm(type: ScopeType) {
        viewModelScope.launch {
            runCatching { propertyService.getBuildingTree() }
                .onSuccess { tree ->
                    val scopes = mutableListOf<Pair<String, String>>()
                    when (type) {
                        ScopeType.NIERUCHOMOSC -> { /* TODO: pobranie ID nieruchomości jeśli jest jednoznaczne */ }
                        ScopeType.BUDYNEK -> {
                            tree.forEach { b -> scopes.add(b.id to "Budynek ${b.address}") }
                        }
                        ScopeType.KLATKA -> {
                            tree.forEach { b ->
                                b.staircases.forEach { s ->
                                    scopes.add(s.id to "Klatka ${s.label} (Budynek ${b.address})")
                                }
                            }
                        }
                    }
                    val firstId = scopes.firstOrNull()?.first ?: ""
                    _formState.value = _formState.value.copy(
                        availableScopes = scopes,
                        scopeId = firstId
                    )
                }
        }
    }

    fun submitCreate() {
        val form = _formState.value
        if (!form.isValid) return
        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val request = InspectionRequestDto(
                title = form.title.trim(),
                description = form.description.trim().takeIf { it.isNotBlank() },
                scheduledAt = form.scheduledAt.trim(),
                scopeType = form.scopeType.name,
                scopeId = form.scopeId.trim()
            )
            runCatching { inspectionService.create(request) }
                .onSuccess {
                    closeCreateDialog()
                    _events.send(InspectionEvent.ShowSnackbar("Przegląd został zaplanowany"))
                    load()
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(InspectionEvent.ShowSnackbar(e.message ?: "Błąd tworzenia przeglądu"))
                }
        }
    }
}
