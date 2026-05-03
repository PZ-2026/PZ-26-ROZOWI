package pl.edu.ur.blokur.ui.views.finances.viewmodels

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
import pl.edu.ur.blokur.dtos.ApartmentTransactionsDto
import pl.edu.ur.blokur.dtos.CreateTransactionRequest
import pl.edu.ur.blokur.dtos.FinancialTransactionDto
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.FinancialLedgerService
import pl.edu.ur.blokur.services.PropertyService
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

sealed interface LedgerUiState {
    data object Loading : LedgerUiState
    data class Error(val message: String) : LedgerUiState
    data class Success(
        val apartmentId: String,
        val apartmentLabel: String,
        val currentBalance: BigDecimal,
        val transactions: List<FinancialTransactionDto>,
        val isManager: Boolean
    ) : LedgerUiState
}

sealed interface LedgerEvent {
    data class ShowSnackbar(val message: String) : LedgerEvent
}

// ── Form state (dla zarządcy) ─────────────────────────────────────────────────

data class AddTransactionFormState(
    val type: String = "WPLATA",
    val amount: String = "",
    val description: String = "",
    val transactionDate: String = LocalDate.now().toString(),
    val isSubmitting: Boolean = false
) {
    val availableTypes = listOf(
        "WPLATA" to "Wpłata",
        "NALICZENIE" to "Naliczenie",
        "KOREKTA" to "Korekta"
    )

    val isValid: Boolean
        get() = amount.toDoubleOrNull() != null && amount.toDouble() != 0.0 &&
                description.isNotBlank() && transactionDate.isNotBlank()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class FinancialLedgerViewModel @Inject constructor(
    private val ledgerService: FinancialLedgerService,
    private val propertyService: PropertyService,
    private val authService: AuthService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // apartmentId może być przekazane jako argument nawigacji (zarządca z widoku nieruchomości)
    // lub pobrane z drzewa budynków (mieszkaniec)
    private val navApartmentId: String? = savedStateHandle["apartmentId"]

    private val _state = MutableStateFlow<LedgerUiState>(LedgerUiState.Loading)
    val state: StateFlow<LedgerUiState> = _state.asStateFlow()

    private val _events = Channel<LedgerEvent>()
    val events: Flow<LedgerEvent> = _events.receiveAsFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _formState = MutableStateFlow(AddTransactionFormState())
    val formState: StateFlow<AddTransactionFormState> = _formState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = LedgerUiState.Loading
            val role = authService.getCurrentUserRole()
            val isManager = role == UserRole.ZARZADCA

            // Jeśli zarządca przekazał apartmentId przez nawigację – używamy go.
            // Jeśli mieszkaniec – pobieramy drzewo i bierzemy pierwszy lokal z niego.
            val (apartmentId, apartmentLabel) = if (navApartmentId != null) {
                navApartmentId to ""
            } else {
                try {
                    val tree = propertyService.getBuildingTree()
                    val firstApartment = tree.firstOrNull()?.staircases
                        ?.firstOrNull()?.apartments?.firstOrNull()
                    if (firstApartment == null) {
                        _state.value = LedgerUiState.Error("Nie znaleziono przypisanego lokalu.")
                        return@launch
                    }
                    firstApartment.id to "Lokal ${firstApartment.number}"
                } catch (e: Exception) {
                    _state.value = LedgerUiState.Error(e.message ?: "Błąd ładowania lokalu")
                    return@launch
                }
            }

            runCatching { ledgerService.getTransactions(apartmentId) }
                .onSuccess { data ->
                    _state.value = LedgerUiState.Success(
                        apartmentId = apartmentId,
                        apartmentLabel = apartmentLabel.ifBlank {
                            data.transactions.firstOrNull()?.let { "Lokal ${it.apartmentId.takeLast(4)}" }
                                ?: "Kartoteka finansowa"
                        },
                        currentBalance = data.currentBalance ?: BigDecimal.ZERO,
                        transactions = data.transactions.sortedByDescending { it.transactionDate },
                        isManager = isManager
                    )
                }
                .onFailure { e ->
                    _state.value = LedgerUiState.Error(e.message ?: "Błąd ładowania transakcji")
                }
        }
    }

    // ── Dialog ────────────────────────────────────────────────────────────────

    fun openAddDialog() {
        _formState.value = AddTransactionFormState()
        _showAddDialog.value = true
    }

    fun closeDialog() { _showAddDialog.value = false }

    fun onTypeChanged(v: String) { _formState.value = _formState.value.copy(type = v) }
    fun onAmountChanged(v: String) { _formState.value = _formState.value.copy(amount = v) }
    fun onDescriptionChanged(v: String) { _formState.value = _formState.value.copy(description = v) }
    fun onDateChanged(v: String) { _formState.value = _formState.value.copy(transactionDate = v) }

    fun submitTransaction() {
        val form = _formState.value
        if (!form.isValid) return
        val current = _state.value as? LedgerUiState.Success ?: return

        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val request = CreateTransactionRequest(
                type = form.type,
                amount = form.amount.toBigDecimal(),
                description = form.description.trim(),
                transactionDate = form.transactionDate
            )
            runCatching { ledgerService.createTransaction(current.apartmentId, request) }
                .onSuccess { newTx ->
                    closeDialog()
                    _state.value = current.copy(
                        transactions = listOf(newTx) + current.transactions,
                        currentBalance = current.currentBalance.add(newTx.amount)
                    )
                    _events.send(LedgerEvent.ShowSnackbar("Operacja finansowa została zarejestrowana"))
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(LedgerEvent.ShowSnackbar(e.message ?: "Błąd dodawania operacji"))
                }
        }
    }
}
