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
import pl.edu.ur.blokur.dtos.UserDocumentDto
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.FinancialLedgerService
import pl.edu.ur.blokur.services.PropertyService
import pl.edu.ur.blokur.services.UserApiService
import pl.edu.ur.blokur.services.UserDocumentService
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesState
import java.io.File
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val ledgerService: FinancialLedgerService,
    private val propertyService: PropertyService,
    private val userApiService: UserApiService,
    private val authService: AuthService,
    private val userDocumentService: UserDocumentService,
    @ApplicationContext private val context: Context
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
            _state.value = FinancesState.Loading
            runCatching {
                val isManager = authService.getCurrentUserRole() == UserRole.ZARZADCA
                val me = userApiService.getMe().body()
                val apartmentId = me?.apartmentId ?: run {
                    if (isManager) {
                        val tree = propertyService.getBuildingTree()
                        tree.firstOrNull()?.staircases?.firstOrNull()?.apartments?.firstOrNull()?.id
                    } else null
                }

                val transactionsData = if (apartmentId != null) {
                    ledgerService.getTransactions(apartmentId)
                } else null

                val documents = userDocumentService.getDocuments()

                Pair(transactionsData, documents)
            }.onSuccess { (transactionsData, documents) ->
                _state.value = FinancesState.Data(
                    currentBalance = transactionsData?.currentBalance ?: BigDecimal.ZERO,
                    transactions = transactionsData?.transactions?.sortedByDescending { it.transactionDate } ?: emptyList(),
                    documents = documents
                )
            }.onFailure { e ->
                _state.value = FinancesState.Error(e.message ?: "Błąd ładowania finansów")
            }
        }
    }

    suspend fun isManager(): Boolean =
        authService.getCurrentUserRole() == UserRole.ZARZADCA

    fun onNavigateToTransactions() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToTransactions) }
    }

    fun onNavigateToDocuments() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToDocuments) }
    }

    fun onNavigateToLedger() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToLedger) }
    }

    fun onNavigateToBalances() {
        viewModelScope.launch { _events.send(FinancesEvent.NavigateToBalances) }
    }

    /**
     * Pobiera plik PDF z backendu używając prawdziwego UUID dokumentu,
     * zapisuje w cache/pdf/ i otwiera przez FileProvider → Intent ACTION_VIEW.
     */
    fun downloadDocument(document: UserDocumentDto) {
        val documentId = document.id
        if (documentId.isBlank()) {
            viewModelScope.launch {
                _events.send(FinancesEvent.ShowSnackbar("Brak identyfikatora dokumentu"))
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                val body = userDocumentService.downloadDocument(documentId)

                val pdfDir = File(context.cacheDir, "pdf").also { it.mkdirs() }
                val safeTitle = document.title
                    .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                    .take(50)
                val pdfFile = File(pdfDir, "${safeTitle}_${documentId}.pdf")

                withContext(Dispatchers.IO) {
                    pdfFile.outputStream().use { out ->
                        body.byteStream().use { it.copyTo(out) }
                    }
                }

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    pdfFile
                )
            }.onSuccess { uri: Uri ->
                _events.send(FinancesEvent.OpenPdf(uri))
            }.onFailure { e ->
                _events.send(FinancesEvent.ShowSnackbar("Błąd pobierania: ${e.message}"))
            }
        }
    }
}