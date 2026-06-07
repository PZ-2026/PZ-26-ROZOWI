package pl.edu.ur.blokur.ui.views.resolutions.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.CreateResolutionRequest
import pl.edu.ur.blokur.dtos.ResolutionDetailDto
import pl.edu.ur.blokur.dtos.ResolutionDto
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.PropertyService
import pl.edu.ur.blokur.services.ResolutionService
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface ResolutionEvent {
    data class ShowSnackbar(val message: String) : ResolutionEvent
    data class NavigateToDetail(val id: String) : ResolutionEvent
}

// ── List State ────────────────────────────────────────────────────────────────

sealed interface ResolutionsListState {
    data object Loading : ResolutionsListState
    data class Error(val message: String) : ResolutionsListState
    data class Success(
        val resolutions: List<ResolutionDto>,
        val isManager: Boolean
    ) : ResolutionsListState
}

// ── Detail State ──────────────────────────────────────────────────────────────

sealed interface ResolutionDetailState {
    data object Loading : ResolutionDetailState
    data class Error(val message: String) : ResolutionDetailState
    data class Success(
        val detail: ResolutionDetailDto,
        val selectedOptionId: String?,
        val isVoting: Boolean,
        val hasVoted: Boolean,
        val isManager: Boolean,
        val isDownloadingReport: Boolean
    ) : ResolutionDetailState
}

// ── Create Form ───────────────────────────────────────────────────────────────

data class CreateResolutionFormState(
    val title: String = "",
    val description: String = "",
    val endDate: String = "",         // "YYYY-MM-DDTHH:MM:SS"
    val options: List<String> = listOf("Za", "Przeciw", "Wstrzymuję się"),
    val targetBuildingId: String = "",
    val availableBuildings: List<Pair<String, String>> = emptyList(), // id to name
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = getValidationError() == null

    val endDateError: String?
        get() {
            if (endDate.isBlank()) return null
            if (!endDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$"))) {
                return "Niepoprawny format daty"
            }
            return try {
                val formatted = if (endDate.length == 16) "$endDate:00" else endDate
                val ldt = java.time.LocalDateTime.parse(formatted)
                if (ldt.isBefore(java.time.LocalDateTime.now())) {
                    "Data zakończenia musi być w przyszłości"
                } else {
                    null
                }
            } catch (_: Exception) {
                "Błąd parsowania daty"
            }
        }

    fun getValidationError(): String? {
        if (title.isBlank()) return "Tytuł uchwały nie może być pusty"
        if (description.isBlank()) return "Opis uchwały nie może być pusty"
        if (endDate.isBlank()) return "Data zakończenia nie może być pusta"
        if (!endDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}(:\\d{2})?$"))) {
            return "Niepoprawny format daty"
        }
        try {
            val formatted = if (endDate.length == 16) "$endDate:00" else endDate
            val ldt = java.time.LocalDateTime.parse(formatted)
            if (ldt.isBefore(java.time.LocalDateTime.now())) {
                return "Data zakończenia musi być w przyszłości"
            }
        } catch (_: Exception) {
            return "Błąd parsowania daty"
        }
        if (options.size < 2) return "Musisz podać co najmniej 2 opcje głosowania"
        if (options.any { it.isBlank() }) return "Opcje głosowania nie mogą być puste"
        if (targetBuildingId.isBlank()) return "Musisz wybrać budynek"
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

// ── ResolutionsListViewModel ──────────────────────────────────────────────────

@HiltViewModel
class ResolutionsListViewModel @Inject constructor(
    private val resolutionService: ResolutionService,
    private val propertyService: PropertyService,
    private val authService: AuthService
) : ViewModel() {

    private val _state = MutableStateFlow<ResolutionsListState>(ResolutionsListState.Loading)
    val state: StateFlow<ResolutionsListState> = _state.asStateFlow()

    private val _events = Channel<ResolutionEvent>()
    val events: Flow<ResolutionEvent> = _events.receiveAsFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _formState = MutableStateFlow(CreateResolutionFormState())
    val formState: StateFlow<CreateResolutionFormState> = _formState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = ResolutionsListState.Loading
            val role = authService.getCurrentUserRole()
            val isManager = role == UserRole.ZARZADCA
            runCatching { resolutionService.getResolutions() }
                .onSuccess { list ->
                    _state.value = ResolutionsListState.Success(
                        resolutions = list.sortedByDescending { it.endDate },
                        isManager = isManager
                    )
                }
                .onFailure { e ->
                    _state.value = ResolutionsListState.Error(e.message ?: "Błąd ładowania uchwał")
                }
        }
    }

    fun onResolutionClicked(id: String) {
        viewModelScope.launch { _events.send(ResolutionEvent.NavigateToDetail(id)) }
    }

    fun openCreateDialog() {
        viewModelScope.launch {
            _formState.value = CreateResolutionFormState()
            // Pobierz listę budynków dla wyboru targetBuildingId
            runCatching { propertyService.getBuildingTree() }
                .onSuccess { tree ->
                    val buildings = tree.map { it.id to "Budynek ${it.address}" }
                    val firstId = tree.firstOrNull()?.id ?: ""
                    _formState.value = _formState.value.copy(
                        availableBuildings = buildings,
                        targetBuildingId = firstId
                    )
                }
                .onFailure { /* ignoruj – użytkownik wpisze ręcznie */ }
            _showCreateDialog.value = true
        }
    }

    fun closeCreateDialog() { _showCreateDialog.value = false }

    fun onTitleChanged(v: String) { _formState.value = _formState.value.copy(title = v) }
    fun onDescriptionChanged(v: String) { _formState.value = _formState.value.copy(description = v) }
    fun onEndDateChanged(v: String) { _formState.value = _formState.value.copy(endDate = v) }
    fun onBuildingChanged(v: String) { _formState.value = _formState.value.copy(targetBuildingId = v) }
    fun onOptionChanged(index: Int, v: String) {
        val updated = _formState.value.options.toMutableList().also { it[index] = v }
        _formState.value = _formState.value.copy(options = updated)
    }
    fun addOption() {
        if (_formState.value.options.size < 10)
            _formState.value = _formState.value.copy(options = _formState.value.options + "")
    }
    fun removeOption(index: Int) {
        if (_formState.value.options.size > 2)
            _formState.value = _formState.value.copy(
                options = _formState.value.options.toMutableList().also { it.removeAt(index) }
            )
    }

    fun submitCreate() {
        val form = _formState.value
        val validationError = form.getValidationError()
        if (validationError != null) {
            viewModelScope.launch {
                _events.send(ResolutionEvent.ShowSnackbar(validationError))
            }
            return
        }
        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val formattedDate = if (form.endDate.length == 16) "${form.endDate}:00" else form.endDate
            val request = CreateResolutionRequest(
                title = form.title.trim(),
                description = form.description.trim(),
                endDate = formattedDate.trim(),
                options = form.options.map { it.trim() }.filter { it.isNotBlank() },
                targetBuildingId = form.targetBuildingId
            )
            runCatching { resolutionService.createResolution(request) }
                .onSuccess {
                    closeCreateDialog()
                    _events.send(ResolutionEvent.ShowSnackbar("Uchwała została utworzona"))
                    load()
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(ResolutionEvent.ShowSnackbar(e.message ?: "Błąd tworzenia uchwały"))
                }
        }
    }
}

// ── ResolutionDetailViewModel ─────────────────────────────────────────────────

@HiltViewModel
class ResolutionDetailViewModel @Inject constructor(
    private val resolutionService: ResolutionService,
    private val authService: AuthService,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val resolutionId: String = checkNotNull(savedStateHandle["resolutionId"])

    private val _state = MutableStateFlow<ResolutionDetailState>(ResolutionDetailState.Loading)
    val state: StateFlow<ResolutionDetailState> = _state.asStateFlow()

    private val _events = Channel<ResolutionEvent>()
    val events: Flow<ResolutionEvent> = _events.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = ResolutionDetailState.Loading
            val role = authService.getCurrentUserRole()
            val isManager = role == UserRole.ZARZADCA
            runCatching { resolutionService.getResolutionDetails(resolutionId) }
                .onSuccess { detail ->
                    _state.value = ResolutionDetailState.Success(
                        detail = detail,
                        selectedOptionId = null,
                        isVoting = false,
                        hasVoted = !isManager && detail.results != null,
                        isManager = isManager,
                        isDownloadingReport = false
                    )
                }
                .onFailure { e ->
                    _state.value = ResolutionDetailState.Error(e.message ?: "Błąd ładowania uchwały")
                }
        }
    }

    fun selectOption(optionId: String) {
        val s = _state.value as? ResolutionDetailState.Success ?: return
        if (s.hasVoted || s.isVoting) return
        _state.value = s.copy(selectedOptionId = optionId)
    }

    fun castVote() {
        val s = _state.value as? ResolutionDetailState.Success ?: return
        val optionId = s.selectedOptionId ?: return
        if (s.hasVoted || s.isVoting) return

        viewModelScope.launch {
            _state.value = s.copy(isVoting = true)
            runCatching { resolutionService.castVote(resolutionId, optionId) }
                .onSuccess {
                    _events.send(ResolutionEvent.ShowSnackbar("Głos oddany pomyślnie!"))
                    // Odśwież szczegóły, żeby pokazać wyniki
                    loadAfterVote()
                }
                .onFailure { e ->
                    _state.value = s.copy(isVoting = false)
                    _events.send(ResolutionEvent.ShowSnackbar(e.message ?: "Błąd oddawania głosu"))
                }
        }
    }

    private fun loadAfterVote() {
        viewModelScope.launch {
            val role = authService.getCurrentUserRole()
            val isManager = role == UserRole.ZARZADCA
            runCatching { resolutionService.getResolutionDetails(resolutionId) }
                .onSuccess { detail ->
                    _state.value = ResolutionDetailState.Success(
                        detail = detail,
                        selectedOptionId = null,
                        isVoting = false,
                        hasVoted = true,
                        isManager = isManager,
                        isDownloadingReport = false
                    )
                }
        }
    }

    fun downloadReport() {
        val s = _state.value as? ResolutionDetailState.Success ?: return
        if (s.isDownloadingReport) return
        viewModelScope.launch {
            _state.value = s.copy(isDownloadingReport = true)
            runCatching { resolutionService.getResolutionReport(resolutionId) }
                .onSuccess { bytes ->
                    savePdfAndOpen(bytes, "raport_uchwala_$resolutionId.pdf")
                    _state.value = s.copy(isDownloadingReport = false)
                }
                .onFailure { e ->
                    _state.value = s.copy(isDownloadingReport = false)
                    _events.send(ResolutionEvent.ShowSnackbar(e.message ?: "Błąd pobierania raportu"))
                }
        }
    }

    private fun savePdfAndOpen(bytes: ByteArray, filename: String) {
        try {
            val dir = File(context.cacheDir, "pdf").also { it.mkdirs() }
            val file = File(dir, filename)
            file.writeBytes(bytes)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            viewModelScope.launch {
                _events.send(ResolutionEvent.ShowSnackbar("Nie można otworzyć PDF: ${e.message}"))
            }
        }
    }
}
