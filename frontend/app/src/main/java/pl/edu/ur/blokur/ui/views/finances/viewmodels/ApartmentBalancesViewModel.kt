package pl.edu.ur.blokur.ui.views.finances.viewmodels

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import pl.edu.ur.blokur.services.ApiResponseHandler
import pl.edu.ur.blokur.services.ApartmentBalanceService
import pl.edu.ur.blokur.services.PdfApiService
import pl.edu.ur.blokur.services.PropertyService
import java.io.File
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface BalancesUiState {
    data object Loading : BalancesUiState
    data class Error(val message: String) : BalancesUiState
    data class Success(val items: List<ApartmentBalanceItemDto>) : BalancesUiState
}

data class BalancesFilterState(
    val propertyId: String = "",
    val minDebt: String = "",
    val minDaysOverdue: String = "",
    val sort: String = "debt_desc",
    val availableProperties: List<PropertyResponseDto> = emptyList(),
    val isPropertyExpanded: Boolean = false
)

sealed interface BalancesEvent {
    data class OpenPdf(val uri: Uri) : BalancesEvent
    data class ShowSnackbar(val message: String) : BalancesEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ApartmentBalancesViewModel @Inject constructor(
    private val balanceService: ApartmentBalanceService,
    private val pdfApi: PdfApiService,
    private val propertyService: PropertyService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalancesUiState>(BalancesUiState.Loading)
    val uiState: StateFlow<BalancesUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(BalancesFilterState())
    val filterState: StateFlow<BalancesFilterState> = _filterState.asStateFlow()

    private val _isDownloadingPdf = MutableStateFlow(false)
    val isDownloadingPdf: StateFlow<Boolean> = _isDownloadingPdf.asStateFlow()

    private val _events = Channel<BalancesEvent>()
    val events: Flow<BalancesEvent> = _events.receiveAsFlow()

    init {
        loadProperties()
        load()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            runCatching {
                propertyService.getProperties()
            }.onSuccess { props ->
                _filterState.value = _filterState.value.copy(availableProperties = props)
            }
        }
    }

    /** Pobiera zestawienie z aktualnymi filtrami. */
    fun load() {
        val f = _filterState.value
        viewModelScope.launch {
            _uiState.value = BalancesUiState.Loading
            runCatching {
                balanceService.getBalances(
                    propertyId = f.propertyId.ifBlank { null },
                    minDebt = f.minDebt.ifBlank { null },
                    minDaysOverdue = f.minDaysOverdue.toLongOrNull(),
                    sort = f.sort
                )
            }.onSuccess { items ->
                _uiState.value = BalancesUiState.Success(items)
            }.onFailure { e ->
                _uiState.value = BalancesUiState.Error(e.message ?: "Błąd ładowania zestawienia")
            }
        }
    }

    fun onPropertyIdChanged(v: String) {
        _filterState.value = _filterState.value.copy(propertyId = v, isPropertyExpanded = false)
    }

    fun onPropertyExpandedChange(expanded: Boolean) {
        _filterState.value = _filterState.value.copy(isPropertyExpanded = expanded)
    }

    fun onMinDebtChanged(v: String) {
        _filterState.value = _filterState.value.copy(minDebt = v)
    }

    fun onMinDaysOverdueChanged(v: String) {
        _filterState.value = _filterState.value.copy(minDaysOverdue = v)
    }

    fun onSortToggled() {
        val current = _filterState.value.sort
        _filterState.value = _filterState.value.copy(
            sort = if (current == "debt_desc") "debt_asc" else "debt_desc"
        )
    }

    /** Pobiera raport PDF przez API z JWT i otwiera go w przeglądarce/podglądzie. */
    fun downloadBalancesPdf() {
        if (_isDownloadingPdf.value) return
        val f = _filterState.value
        viewModelScope.launch {
            _isDownloadingPdf.value = true
            runCatching {
                val responseBody = ApiResponseHandler.requireSuccess(
                    pdfApi.getBalancesPdf(
                        propertyId = f.propertyId.ifBlank { null },
                        minDebt = f.minDebt.ifBlank { null },
                        minDaysOverdue = f.minDaysOverdue.toLongOrNull(),
                        sort = f.sort
                    ),
                    "Nie udało się wygenerować zestawienia PDF"
                )
                withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "pdf").also { it.mkdirs() }
                    val file = File(dir, "zestawienie_sald.pdf")
                    file.writeBytes(responseBody.bytes())
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }
            }.onSuccess { uri ->
                _events.send(BalancesEvent.OpenPdf(uri))
            }.onFailure { e ->
                _events.send(
                    BalancesEvent.ShowSnackbar(e.message ?: "Błąd pobierania zestawienia PDF")
                )
            }
            _isDownloadingPdf.value = false
        }
    }
}
