package pl.edu.ur.blokur.ui.views.finances.viewmodels

import android.content.Context
import android.net.Uri
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.edu.ur.blokur.dtos.CsvImportResultDto
import pl.edu.ur.blokur.services.FinancialApiService
import javax.inject.Inject

// ── State ────────────────────────────────────────────────────────────────────

sealed interface CsvImportUiState {
    data object Idle : CsvImportUiState
    data object Uploading : CsvImportUiState
    data class Result(val result: CsvImportResultDto) : CsvImportUiState
    data class Error(val message: String) : CsvImportUiState
}

sealed interface CsvImportEvent {
    data class ShowSnackbar(val message: String) : CsvImportEvent
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val financialApi: FinancialApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<CsvImportUiState>(CsvImportUiState.Idle)
    val state: StateFlow<CsvImportUiState> = _state.asStateFlow()

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName.asStateFlow()

    private val _selectedUri = MutableStateFlow<Uri?>(null)

    private val _events = Channel<CsvImportEvent>()
    val events: Flow<CsvImportEvent> = _events.receiveAsFlow()

    fun onFileSelected(uri: Uri, fileName: String?) {
        _selectedUri.value = uri
        _selectedFileName.value = fileName ?: "plik.csv"
    }

    fun upload() {
        val uri = _selectedUri.value ?: return
        viewModelScope.launch {
            _state.value = CsvImportUiState.Uploading
            runCatching {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Nie można otworzyć pliku")
                val bytes = inputStream.readBytes()
                inputStream.close()

                val requestBody = bytes.toRequestBody("text/csv".toMediaType())
                val part = MultipartBody.Part.createFormData(
                    "file",
                    _selectedFileName.value ?: "import.csv",
                    requestBody
                )

                val resp = financialApi.importCsv(part)
                if (!resp.isSuccessful) {
                    throw Exception("Błąd importu (${resp.code()})")
                }
                resp.body() ?: throw Exception("Pusta odpowiedź z serwera")
            }
                .onSuccess { result ->
                    _state.value = CsvImportUiState.Result(result)
                    val msg = if (result.errorCount == 0)
                        "Import zakończony: ${result.importedCount} transakcji"
                    else
                        "Import: ${result.importedCount} OK, ${result.errorCount} błędów"
                    _events.send(CsvImportEvent.ShowSnackbar(msg))
                }
                .onFailure { e ->
                    _state.value = CsvImportUiState.Error(e.message ?: "Błąd importu")
                    _events.send(CsvImportEvent.ShowSnackbar(e.message ?: "Błąd importu"))
                }
        }
    }

    fun reset() {
        _state.value = CsvImportUiState.Idle
        _selectedUri.value = null
        _selectedFileName.value = null
    }
}
