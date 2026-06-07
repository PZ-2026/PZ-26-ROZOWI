package pl.edu.ur.blokur.ui.views.meters.viewmodels

import androidx.lifecycle.SavedStateHandle
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
import pl.edu.ur.blokur.dtos.MediumType
import pl.edu.ur.blokur.dtos.MeterReadingRequestDto
import pl.edu.ur.blokur.dtos.MeterReadingResponseDto
import pl.edu.ur.blokur.dtos.MeterRequestDto
import pl.edu.ur.blokur.dtos.MeterResponseDto
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.MeterService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed interface MeterEvent {
    data class ShowSnackbar(val message: String) : MeterEvent
}

// ── Lista Liczników ─────────────────────────────────────────────────────────

sealed interface MeterListState {
    data object Loading : MeterListState
    data class Error(val message: String) : MeterListState
    data class Success(val meters: List<MeterResponseDto>) : MeterListState
}

data class CreateMeterFormState(
    val serialNumber: String = "",
    val mediumType: MediumType = MediumType.ZIMNA_WODA,
    val installationDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean get() = serialNumber.isNotBlank() && 
            installationDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
}

@HiltViewModel
class MeterListViewModel @Inject constructor(
    private val meterService: MeterService,
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])

    private val _state = MutableStateFlow<MeterListState>(MeterListState.Loading)
    val state: StateFlow<MeterListState> = _state.asStateFlow()

    private val _isManager = MutableStateFlow(false)
    val isManager: StateFlow<Boolean> = _isManager.asStateFlow()

    private val _events = Channel<MeterEvent>()
    val events: Flow<MeterEvent> = _events.receiveAsFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _formState = MutableStateFlow(CreateMeterFormState())
    val formState: StateFlow<CreateMeterFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            _isManager.value = authService.getCurrentUserRole() == UserRole.ZARZADCA
            load()
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = MeterListState.Loading
            runCatching { meterService.getMetersByApartment(apartmentId) }
                .onSuccess { list ->
                    _state.value = MeterListState.Success(list.sortedByDescending { it.installationDate })
                }
                .onFailure { e ->
                    _state.value = MeterListState.Error(e.message ?: "Błąd ładowania liczników")
                }
        }
    }

    fun openCreateDialog() {
        _formState.value = CreateMeterFormState()
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() { _showCreateDialog.value = false }

    fun onSerialNumberChanged(v: String) { _formState.value = _formState.value.copy(serialNumber = v) }
    fun onMediumTypeChanged(v: MediumType) { _formState.value = _formState.value.copy(mediumType = v) }
    fun onInstallationDateChanged(v: String) { _formState.value = _formState.value.copy(installationDate = v) }

    fun submitCreate() {
        val form = _formState.value
        if (!form.isValid) return
        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val req = MeterRequestDto(
                serialNumber = form.serialNumber.trim(),
                mediumType = form.mediumType.name,
                installationDate = form.installationDate.trim()
            )
            runCatching { meterService.createMeter(apartmentId, req) }
                .onSuccess {
                    closeCreateDialog()
                    _events.send(MeterEvent.ShowSnackbar("Licznik został dodany"))
                    load()
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(MeterEvent.ShowSnackbar(e.message ?: "Błąd dodawania licznika"))
                }
        }
    }

    fun deactivateMeter(meterId: String) {
        viewModelScope.launch {
            runCatching { meterService.deactivateMeter(meterId) }
                .onSuccess {
                    _events.send(MeterEvent.ShowSnackbar("Licznik został dezaktywowany"))
                    load()
                }
                .onFailure { e ->
                    _events.send(MeterEvent.ShowSnackbar(e.message ?: "Błąd dezaktywacji licznika"))
                }
        }
    }
}

// ── Szczegóły Licznika (Odczyty) ────────────────────────────────────────────

sealed interface MeterDetailState {
    data object Loading : MeterDetailState
    data class Error(val message: String) : MeterDetailState
    data class Success(
        val readings: List<MeterReadingResponseDto>,
        val isFetchingNextPage: Boolean = false,
        val isLastPage: Boolean = false
    ) : MeterDetailState
}

data class CreateReadingFormState(
    val value: String = "",
    val readingDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean get() = value.isNotBlank() &&
            readingDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) &&
            value.replace(",", ".").toBigDecimalOrNull() != null
}

@HiltViewModel
class MeterDetailViewModel @Inject constructor(
    private val meterService: MeterService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId: String = checkNotNull(savedStateHandle["apartmentId"])
    val meterId: String = checkNotNull(savedStateHandle["meterId"])
    val serialNumber: String = checkNotNull(savedStateHandle["serialNumber"])
    val mediumType: String = checkNotNull(savedStateHandle["mediumType"])

    private val _state = MutableStateFlow<MeterDetailState>(MeterDetailState.Loading)
    val state: StateFlow<MeterDetailState> = _state.asStateFlow()

    private val _events = Channel<MeterEvent>()
    val events: Flow<MeterEvent> = _events.receiveAsFlow()

    private val _showCreateDialog = MutableStateFlow(false)
    val showCreateDialog: StateFlow<Boolean> = _showCreateDialog.asStateFlow()

    private val _formState = MutableStateFlow(CreateReadingFormState())
    val formState: StateFlow<CreateReadingFormState> = _formState.asStateFlow()

    private var currentPage = 0
    private var isLastPage = false
    private var isFetchingNextPage = false

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = MeterDetailState.Loading
            currentPage = 0
            isLastPage = false
            
            runCatching { meterService.getMeterReadingsByApartment(apartmentId, meterId, currentPage, 15) }
                .onSuccess { paged ->
                    isLastPage = paged.number >= paged.totalPages - 1
                    _state.value = MeterDetailState.Success(
                        readings = paged.content,
                        isFetchingNextPage = false,
                        isLastPage = isLastPage
                    )
                }
                .onFailure { e ->
                    _state.value = MeterDetailState.Error(e.message ?: "Błąd ładowania odczytów")
                }
        }
    }

    fun loadNextPage() {
        if (isLastPage || isFetchingNextPage) return
        val currentState = _state.value as? MeterDetailState.Success ?: return

        viewModelScope.launch {
            isFetchingNextPage = true
            _state.value = currentState.copy(isFetchingNextPage = true)

            runCatching { meterService.getMeterReadingsByApartment(apartmentId, meterId, currentPage + 1, 15) }
                .onSuccess { paged ->
                    currentPage++
                    isLastPage = paged.number >= paged.totalPages - 1
                    val newReadings = currentState.readings + paged.content
                    _state.value = MeterDetailState.Success(
                        readings = newReadings,
                        isFetchingNextPage = false,
                        isLastPage = isLastPage
                    )
                }
                .onFailure {
                    // W przypadku błędu przywracamy poprzedni stan (ukrywamy loader)
                    _events.send(MeterEvent.ShowSnackbar("Nie udało się pobrać kolejnej strony"))
                    _state.value = currentState.copy(isFetchingNextPage = false)
                }
            isFetchingNextPage = false
        }
    }

    fun openCreateDialog() {
        _formState.value = CreateReadingFormState()
        _showCreateDialog.value = true
    }

    fun closeCreateDialog() { _showCreateDialog.value = false }

    fun onValueChanged(v: String) { _formState.value = _formState.value.copy(value = v) }
    fun onReadingDateChanged(v: String) { _formState.value = _formState.value.copy(readingDate = v) }

    fun submitCreate() {
        val form = _formState.value
        val valBigDecimal = form.value.replace(",", ".").toBigDecimalOrNull()
        if (!form.isValid || valBigDecimal == null) return

        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val req = MeterReadingRequestDto(
                meterId = meterId,
                value = valBigDecimal,
                readingDate = form.readingDate.trim()
            )
            runCatching { meterService.createMeterReading(apartmentId, req) }
                .onSuccess {
                    closeCreateDialog()
                    _events.send(MeterEvent.ShowSnackbar("Odczyt został zapisany"))
                    load()
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(MeterEvent.ShowSnackbar(e.message ?: "Błąd dodawania odczytu"))
                }
        }
    }

    fun deleteReading(readingId: String) {
        viewModelScope.launch {
            runCatching { meterService.deleteMeterReading(readingId) }
                .onSuccess {
                    _events.send(MeterEvent.ShowSnackbar("Odczyt został usunięty"))
                    load()
                }
                .onFailure { e ->
                    _events.send(MeterEvent.ShowSnackbar(e.message ?: "Błąd usuwania odczytu"))
                }
        }
    }

    // ── Edycja odczytu ──────────────────────────────────────────────────

    private val _editingReading = MutableStateFlow<MeterReadingResponseDto?>(null)
    val editingReading: StateFlow<MeterReadingResponseDto?> = _editingReading.asStateFlow()

    private val _editFormState = MutableStateFlow(CreateReadingFormState())
    val editFormState: StateFlow<CreateReadingFormState> = _editFormState.asStateFlow()

    fun openEditDialog(reading: MeterReadingResponseDto) {
        _editingReading.value = reading
        _editFormState.value = CreateReadingFormState(
            value = reading.value.toString(),
            readingDate = reading.readingDate
        )
    }

    fun closeEditDialog() { _editingReading.value = null }
    fun onEditValueChanged(v: String) { _editFormState.value = _editFormState.value.copy(value = v) }
    fun onEditReadingDateChanged(v: String) { _editFormState.value = _editFormState.value.copy(readingDate = v) }

    fun submitUpdate() {
        val reading = _editingReading.value ?: return
        val form = _editFormState.value
        val valBigDecimal = form.value.replace(",", ".").toBigDecimalOrNull()
        if (!form.isValid || valBigDecimal == null) return
        viewModelScope.launch {
            _editFormState.value = form.copy(isSubmitting = true)
            val req = MeterReadingRequestDto(
                meterId = meterId,
                value = valBigDecimal,
                readingDate = form.readingDate.trim()
            )
            runCatching { meterService.updateMeterReading(reading.id, req) }
                .onSuccess {
                    closeEditDialog()
                    _events.send(MeterEvent.ShowSnackbar("Odczyt został zaktualizowany"))
                    load()
                }
                .onFailure { e ->
                    _editFormState.value = form.copy(isSubmitting = false)
                    _events.send(MeterEvent.ShowSnackbar(e.message ?: "Błąd aktualizacji odczytu"))
                }
        }
    }
}
