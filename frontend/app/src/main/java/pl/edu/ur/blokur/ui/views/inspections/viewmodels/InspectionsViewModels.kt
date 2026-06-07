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
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.InspectionRequestDto
import pl.edu.ur.blokur.dtos.InspectionResponseDto
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import pl.edu.ur.blokur.dtos.ScopeType
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
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
    val scheduledAt: String = "",
    val scopeType: ScopeType = ScopeType.BUDYNEK,
    val scopeId: String = "",
    val availableScopes: List<Pair<String, String>> = emptyList(),
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = getValidationError() == null

    fun getValidationError(): String? {
        if (title.isBlank()) return "Tytuł przeglądu nie może być pusty"
        if (scheduledAt.isBlank()) return "Planowana data nie może być pusta"
        if (!scheduledAt.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$"))) {
            return "Niepoprawny format daty"
        }
        try {
            val formatted = if (scheduledAt.length == 16) "$scheduledAt:00" else scheduledAt
            val ldt = java.time.LocalDateTime.parse(formatted)
            if (ldt.isBefore(java.time.LocalDateTime.now())) {
                return "Planowana data musi być w przyszłości"
            }
        } catch (_: Exception) {
            return "Błąd parsowania daty"
        }
        if (scopeId.isBlank()) return "Musisz wybrać obiekt (zasięg) przeglądu"
        return null
    }

    private fun validateDate(dateStr: String): Boolean {
        if (!dateStr.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$"))) return false
        return try {
            val formatted = if (dateStr.length == 16) "$dateStr:00" else dateStr
            val ldt = java.time.LocalDateTime.parse(formatted)
            ldt.isAfter(java.time.LocalDateTime.now())
        } catch (_: Exception) {
            false
        }
    }
}

@HiltViewModel
class InspectionsListViewModel @Inject constructor(
    private val inspectionService: InspectionService,
    private val propertyService: PropertyService,
    private val authService: AuthService
) : ViewModel() {

    suspend fun isManager(): Boolean =
        authService.getCurrentUserRole() == UserRole.ZARZADCA

    private val _state = MutableStateFlow<InspectionsListState>(InspectionsListState.Loading)
    val state: StateFlow<InspectionsListState> = _state.asStateFlow()

    private val _events = Channel<InspectionEvent>()
    val events: Flow<InspectionEvent> = _events.receiveAsFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

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
        _formState.value = CreateInspectionFormState()
        loadScopes(ScopeType.BUDYNEK) { scopes, firstId ->
            _formState.value = _formState.value.copy(
                availableScopes = scopes,
                scopeId = firstId
            )
        }
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() { _showCreateDialog.value = false }

    fun onTitleChanged(v: String) { _formState.value = _formState.value.copy(title = v) }
    fun onDescriptionChanged(v: String) { _formState.value = _formState.value.copy(description = v) }
    fun onScheduledAtChanged(v: String) { _formState.value = _formState.value.copy(scheduledAt = v) }
    fun onScopeIdChanged(v: String) { _formState.value = _formState.value.copy(scopeId = v) }
    
    private fun loadScopes(type: ScopeType, onLoaded: (List<Pair<String, String>>, String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                if (type == ScopeType.NIERUCHOMOSC) {
                    propertyService.getProperties()
                } else {
                    propertyService.getBuildingTree()
                }
            }
                .onSuccess { data ->
                    val scopes = mutableListOf<Pair<String, String>>()
                    when (type) {
                        ScopeType.NIERUCHOMOSC -> {
                            @Suppress("UNCHECKED_CAST")
                            val properties = data as List<PropertyResponseDto>
                            properties.forEach { p -> scopes.add(p.id to "Wspólnota ${p.name}") }
                        }
                        ScopeType.BUDYNEK -> {
                            @Suppress("UNCHECKED_CAST")
                            val tree = data as List<BuildingTreeNodeDto>
                            tree.forEach { b -> scopes.add(b.id to "Budynek ${b.address}") }
                        }
                        ScopeType.KLATKA -> {
                            @Suppress("UNCHECKED_CAST")
                            val tree = data as List<BuildingTreeNodeDto>
                            tree.forEach { b ->
                                b.staircases.forEach { s ->
                                    scopes.add(s.id to "Klatka ${s.label} (Budynek ${b.address})")
                                }
                            }
                        }
                    }
                    val firstId = scopes.firstOrNull()?.first ?: ""
                    onLoaded(scopes, firstId)
                }
        }
    }

    fun onScopeTypeChanged(type: ScopeType) {
        _formState.value = _formState.value.copy(scopeType = type, scopeId = "")
        loadScopes(type) { scopes, firstId ->
            _formState.value = _formState.value.copy(
                availableScopes = scopes,
                scopeId = firstId
            )
        }
    }

    fun submitCreate() {
        val form = _formState.value
        val validationError = form.getValidationError()
        if (validationError != null) {
            viewModelScope.launch {
                _events.send(InspectionEvent.ShowSnackbar(validationError))
            }
            return
        }
        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val formattedDate = if (form.scheduledAt.length == 16) "${form.scheduledAt}:00" else form.scheduledAt
            val request = InspectionRequestDto(
                title = form.title.trim(),
                description = form.description.trim().takeIf { it.isNotBlank() },
                scheduledAt = formattedDate.trim(),
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

    // ── Edycja przeglądu ────────────────────────────────────────────────────────

    private val _editingInspection = MutableStateFlow<InspectionResponseDto?>(null)
    val editingInspection: StateFlow<InspectionResponseDto?> = _editingInspection.asStateFlow()

    private val _editFormState = MutableStateFlow(CreateInspectionFormState())
    val editFormState: StateFlow<CreateInspectionFormState> = _editFormState.asStateFlow()

    fun openEditDialog(inspection: InspectionResponseDto) {
        _editingInspection.value = inspection
        val parsedScopeType = try { ScopeType.valueOf(inspection.scopeType) } catch (_: Exception) { ScopeType.BUDYNEK }
        _editFormState.value = CreateInspectionFormState(
            title = inspection.title,
            description = inspection.description ?: "",
            scheduledAt = inspection.scheduledAt,
            scopeType = parsedScopeType,
            scopeId = inspection.scopeId
        )
        loadScopes(parsedScopeType) { scopes, _ ->
            _editFormState.value = _editFormState.value.copy(
                availableScopes = scopes,
                scopeId = inspection.scopeId
            )
        }
    }

    fun closeEditDialog() { _editingInspection.value = null }

    fun onEditTitleChanged(v: String) { _editFormState.value = _editFormState.value.copy(title = v) }
    fun onEditDescriptionChanged(v: String) { _editFormState.value = _editFormState.value.copy(description = v) }
    fun onEditScheduledAtChanged(v: String) { _editFormState.value = _editFormState.value.copy(scheduledAt = v) }
    fun onEditScopeTypeChanged(type: ScopeType) {
        _editFormState.value = _editFormState.value.copy(scopeType = type, scopeId = "")
        loadScopes(type) { scopes, firstId ->
            _editFormState.value = _editFormState.value.copy(
                availableScopes = scopes,
                scopeId = firstId
            )
        }
    }
    fun onEditScopeIdChanged(v: String) { _editFormState.value = _editFormState.value.copy(scopeId = v) }

    fun submitUpdate() {
        val inspection = _editingInspection.value ?: return
        val form = _editFormState.value
        if (!form.isValid) return
        viewModelScope.launch {
            _editFormState.value = form.copy(isSubmitting = true)
            val formattedDate = if (form.scheduledAt.length == 16) "${form.scheduledAt}:00" else form.scheduledAt
            val request = InspectionRequestDto(
                title = form.title.trim(),
                description = form.description.trim().takeIf { it.isNotBlank() },
                scheduledAt = formattedDate.trim(),
                scopeType = inspection.scopeType,
                scopeId = inspection.scopeId
            )
            runCatching { inspectionService.update(inspection.id, request) }
                .onSuccess {
                    closeEditDialog()
                    _events.send(InspectionEvent.ShowSnackbar("Przegląd został zaktualizowany"))
                    load()
                }
                .onFailure { e ->
                    _editFormState.value = form.copy(isSubmitting = false)
                    _events.send(InspectionEvent.ShowSnackbar(e.message ?: "Błąd aktualizacji przeglądu"))
                }
        }
    }

    // ── Usuwanie przeglądu ──────────────────────────────────────────────────────

    fun deleteInspection(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDeleting.value = true
            runCatching { inspectionService.delete(id) }
                .onSuccess {
                    _isDeleting.value = false
                    _events.send(InspectionEvent.ShowSnackbar("Przegląd został usunięty"))
                    onSuccess()
                    load()
                }
                .onFailure { e ->
                    _isDeleting.value = false
                    _events.send(InspectionEvent.ShowSnackbar(e.message ?: "Błąd usuwania przeglądu"))
                }
        }
    }
}
