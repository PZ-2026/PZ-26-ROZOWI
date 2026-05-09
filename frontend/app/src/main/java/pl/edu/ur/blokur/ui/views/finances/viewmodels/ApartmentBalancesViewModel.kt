package pl.edu.ur.blokur.ui.views.finances.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.ApartmentBalanceItemDto
import pl.edu.ur.blokur.services.ApartmentBalanceService
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
    val sort: String = "debt_desc"
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ApartmentBalancesViewModel @Inject constructor(
    private val balanceService: ApartmentBalanceService
) : ViewModel() {

    private val _uiState = MutableStateFlow<BalancesUiState>(BalancesUiState.Loading)
    val uiState: StateFlow<BalancesUiState> = _uiState.asStateFlow()

    private val _filterState = MutableStateFlow(BalancesFilterState())
    val filterState: StateFlow<BalancesFilterState> = _filterState.asStateFlow()

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

    /** Buduje URL raportu PDF z bieżącymi filtrami (dla przycisku „Pobierz PDF"). */
    fun buildPdfUrl(baseUrl: String): String {
        val f = _filterState.value
        val params = buildList {
            if (f.propertyId.isNotBlank()) add("propertyId=${f.propertyId}")
            if (f.minDebt.isNotBlank()) add("minDebt=${f.minDebt}")
            val days = f.minDaysOverdue.toLongOrNull()
            if (days != null) add("minDaysOverdue=$days")
            add("sort=${f.sort}")
        }
        return "$baseUrl/api/pdf/balances" + if (params.isEmpty()) "" else "?${params.joinToString("&")}"
    }
}
