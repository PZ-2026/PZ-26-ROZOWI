package pl.edu.ur.blokur.ui.views.settings.viewmodels

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
import pl.edu.ur.blokur.services.DocumentApiService
import pl.edu.ur.blokur.services.PropertyApiService
import javax.inject.Inject

// ── State & Events ────────────────────────────────────────────────────────────

data class CommunityLogoState(
    val propertyId: String? = null,
    val selectedFileName: String? = null,
    val isUploading: Boolean = false,
    val isLoadingProperties: Boolean = true,
    val uploadSuccess: Boolean = false
)

sealed interface CommunityLogoEvent {
    data class ShowSnackbar(val message: String) : CommunityLogoEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class CommunityLogoViewModel @Inject constructor(
    private val propertyApiService: PropertyApiService,
    private val documentApiService: DocumentApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityLogoState())
    val state: StateFlow<CommunityLogoState> = _state.asStateFlow()

    private val _events = Channel<CommunityLogoEvent>()
    val events: Flow<CommunityLogoEvent> = _events.receiveAsFlow()

    private var selectedUri: Uri? = null

    init {
        loadFirstProperty()
    }

    private fun loadFirstProperty() {
        viewModelScope.launch {
            runCatching { propertyApiService.getProperties() }
                .onSuccess { resp ->
                    val firstId = resp.body()?.firstOrNull()?.id
                    _state.value = _state.value.copy(
                        propertyId = firstId,
                        isLoadingProperties = false
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(isLoadingProperties = false)
                }
        }
    }

    fun onFileSelected(uri: Uri, fileName: String?) {
        selectedUri = uri
        _state.value = _state.value.copy(
            selectedFileName = fileName ?: "logo.png",
            uploadSuccess = false
        )
    }

    fun upload() {
        val uri = selectedUri ?: return
        val propertyId = _state.value.propertyId ?: run {
            viewModelScope.launch {
                _events.send(CommunityLogoEvent.ShowSnackbar("Brak nieruchomości w systemie"))
            }
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploading = true)
            runCatching {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Nie można otworzyć pliku")
                val bytes = inputStream.readBytes()
                inputStream.close()

                val mimeType = context.contentResolver.getType(uri) ?: "image/png"
                val requestBody = bytes.toRequestBody(mimeType.toMediaType())
                val part = MultipartBody.Part.createFormData(
                    "file",
                    _state.value.selectedFileName ?: "logo.png",
                    requestBody
                )

                val resp = documentApiService.uploadPropertyLogo(propertyId, part)
                if (!resp.isSuccessful) {
                    throw Exception("Błąd uploadu (${resp.code()})")
                }
            }.onSuccess {
                _state.value = _state.value.copy(isUploading = false, uploadSuccess = true)
                _events.send(CommunityLogoEvent.ShowSnackbar("Logo zostało zaktualizowane"))
            }.onFailure { e ->
                _state.value = _state.value.copy(isUploading = false)
                _events.send(CommunityLogoEvent.ShowSnackbar(e.message ?: "Błąd uploadu logo"))
            }
        }
    }
}
