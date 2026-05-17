package pl.edu.ur.blokur.ui.views.finances.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
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
import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import pl.edu.ur.blokur.services.ApartmentBalanceService
import java.io.File
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface BalancesUiState {
    data object Loading : BalancesUiState
    data class Error(val message: String) : BalancesUiState
    data class Success(
        val items: List<ApartmentBalanceItemDto>,
        val isDownloadingPdf: Boolean = false
    ) : BalancesUiState
}

sealed interface BalancesEvent {
    data class ShowSnackbar(val message: String) : BalancesEvent
}

data class BalancesFilterState(
    val propertyId: String = "",
    val minDebt: String = "",
    val minDaysOverdue: String = "",
    val sort: String = "debt_desc"
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ApartmentBalancesViewModel @Inject constructor(
    private val balanceService: ApartmentBalanceService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalancesUiState>(BalancesUiState.Loading)
    val uiState: StateFlow<BalancesUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(BalancesFilterState())
    val filterState: StateFlow<BalancesFilterState> = _filterState.asStateFlow()

    private val _events = Channel<BalancesEvent>()
    val events: Flow<BalancesEvent> = _events.receiveAsFlow()

    init {
        load()
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
        _filterState.value = _filterState.value.copy(propertyId = v)
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

    fun downloadPdf() {
        val current = _uiState.value as? BalancesUiState.Success ?: return
        if (current.isDownloadingPdf) return

        val f = _filterState.value
        viewModelScope.launch {
            _uiState.value = current.copy(isDownloadingPdf = true)
            runCatching {
                balanceService.getBalancesPdf(
                    propertyId = f.propertyId.ifBlank { null },
                    minDebt = f.minDebt.ifBlank { null },
                    minDaysOverdue = f.minDaysOverdue.toLongOrNull(),
                    sort = f.sort
                )
            }.onSuccess { bytes ->
                savePdfAndOpen(bytes, "zestawienie_zaleglosci.pdf")
                _uiState.value = current.copy(isDownloadingPdf = false)
            }.onFailure { e ->
                _uiState.value = current.copy(isDownloadingPdf = false)
                _events.send(BalancesEvent.ShowSnackbar(e.message ?: "Błąd pobierania PDF"))
            }
        }
    }

    private fun savePdfAndOpen(bytes: ByteArray, filename: String) {
        try {
            val dir = File(context.cacheDir, "pdfs").also { it.mkdirs() }
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
                _events.send(BalancesEvent.ShowSnackbar("Nie można otworzyć PDF: ${e.message}"))
            }
        }
    }
}
