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
import pl.edu.ur.blokur.BuildConfig
import pl.edu.ur.blokur.dtos.PropertyResponseDto
import pl.edu.ur.blokur.services.ApiException
import pl.edu.ur.blokur.services.DocumentApiService
import pl.edu.ur.blokur.services.PropertyService
import javax.inject.Inject

data class CommunityLogoState(
    val properties: List<PropertyResponseDto> = emptyList(),
    val selectedPropertyId: String? = null,
    val selectedPropertyName: String? = null,
    val logoUrl: String? = null,
    val selectedFileName: String? = null,
    val isUploading: Boolean = false,
    val isLoadingProperties: Boolean = true,
    val uploadSuccess: Boolean = false,
    val showPropertyPicker: Boolean = false
)

sealed interface CommunityLogoEvent {
    data class ShowSnackbar(val message: String) : CommunityLogoEvent
}

@HiltViewModel
class CommunityLogoViewModel @Inject constructor(
    private val propertyService: PropertyService,
    private val documentApiService: DocumentApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityLogoState())
    val state: StateFlow<CommunityLogoState> = _state.asStateFlow()

    private val _events = Channel<CommunityLogoEvent>()
    val events: Flow<CommunityLogoEvent> = _events.receiveAsFlow()

    private var selectedUri: Uri? = null

    init {
        loadProperties()
    }

    private fun loadProperties() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingProperties = true)
            runCatching { propertyService.getProperties() }
                .onSuccess { properties ->
                    val showPicker = properties.size > 1
                    val initial = properties.firstOrNull()
                    _state.value = _state.value.copy(
                        properties = properties,
                        isLoadingProperties = false,
                        showPropertyPicker = showPicker,
                        selectedPropertyId = initial?.id,
                        selectedPropertyName = initial?.name,
                        logoUrl = initial?.logoPath?.toLogoUrl()
                    )
                    initial?.id?.let { refreshPropertyDetails(it) }
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoadingProperties = false)
                    _events.send(
                        CommunityLogoEvent.ShowSnackbar(
                            e.message ?: "Nie udało się załadować wspólnot"
                        )
                    )
                }
        }
    }

    fun onPropertySelected(propertyId: String) {
        val property = _state.value.properties.find { it.id == propertyId } ?: return
        _state.value = _state.value.copy(
            selectedPropertyId = property.id,
            selectedPropertyName = property.name,
            uploadSuccess = false,
            selectedFileName = null
        )
        selectedUri = null
        refreshPropertyDetails(propertyId)
    }

    private fun refreshPropertyDetails(propertyId: String) {
        viewModelScope.launch {
            runCatching { propertyService.getPropertyById(propertyId) }
                .onSuccess { detail ->
                    _state.value = _state.value.copy(
                        selectedPropertyName = detail.name,
                        logoUrl = detail.logoPath?.toLogoUrl()
                    )
                }
                .onFailure {
                    // Lista wspólnot już załadowana — szczegóły są opcjonalne
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
        val propertyId = _state.value.selectedPropertyId ?: run {
            viewModelScope.launch {
                _events.send(CommunityLogoEvent.ShowSnackbar("Wybierz wspólnotę przed przesłaniem logo"))
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

                val response = documentApiService.uploadPropertyLogo(propertyId, part)
                if (!response.isSuccessful) {
                    val message = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                        ?: "Błąd uploadu (${response.code()})"
                    throw ApiException(message, response.code())
                }
                response.body()
            }.onSuccess { updated ->
                selectedUri = null
                _state.value = _state.value.copy(
                    isUploading = false,
                    uploadSuccess = true,
                    selectedFileName = null,
                    logoUrl = updated?.logoPath?.toLogoUrl()
                )
                _events.send(CommunityLogoEvent.ShowSnackbar("Logo zostało zaktualizowane"))
            }.onFailure { e ->
                _state.value = _state.value.copy(isUploading = false)
                _events.send(CommunityLogoEvent.ShowSnackbar(e.message ?: "Błąd uploadu logo"))
            }
        }
    }

    private fun String.toLogoUrl(): String {
        val base = BuildConfig.BACKEND_URL.removeSuffix("/")
        return if (startsWith("/")) "$base$this" else "$base/$this"
    }
}
