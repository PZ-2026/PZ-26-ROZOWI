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
import pl.edu.ur.blokur.services.FinancesService
import pl.edu.ur.blokur.services.UserDocumentService
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesEvent
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FinancesViewModel @Inject constructor(
    private val financesService: FinancesService,
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
            runCatching {
                Triple(
                    financesService.getBalance(),
                    financesService.getTransactions(),
                    // Dokumenty z prawdziwego API zamiast mocka
                    userDocumentService.getDocuments()
                )
            }.onSuccess { (balance, transactions, documents) ->
                _state.value = FinancesState.Data(balance, transactions, documents)
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