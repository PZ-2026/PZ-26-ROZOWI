package pl.edu.ur.blokur.ui.views.finances.viewmodels

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
import pl.edu.ur.blokur.services.FinancesService
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesState
import javax.inject.Inject

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val financesService: FinancesService
) : ViewModel() {

    private val _state = MutableStateFlow<FinancesState>(FinancesState.Loading)
    val state: StateFlow<FinancesState> = _state.asStateFlow()

    private val _events = Channel<FinancesEvent>()
    val events: Flow<FinancesEvent> = _events.receiveAsFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            runCatching {
                Triple(
                    financesService.getBalance(),
                    financesService.getTransactions(),
                    financesService.getDocuments()
                )
            }.onSuccess { (balance, transactions, documents) ->
                _state.value = FinancesState.Data(balance, transactions, documents)
            }.onFailure { e ->
                _state.value = FinancesState.Error(e.message ?: "Błąd ładowania finansów")
            }
        }
    }

    fun onNavigateToTransactions() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToTransactions) }
    }

    fun onNavigateToDocuments() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToDocuments) }
    }

    fun onNavigateToLedger() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToLedger) }
    }
}