package pl.edu.ur.blokur.ui.views.announcements.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.AnnouncementRequestDto
import pl.edu.ur.blokur.dtos.AnnouncementTargetType
import pl.edu.ur.blokur.services.AnnouncementService
import pl.edu.ur.blokur.ui.views.announcements.AnnouncementsRoutes
import java.io.InputStream
import javax.inject.Inject

sealed interface EditAnnouncementEvent {
    data object Success : EditAnnouncementEvent
    data class ShowError(val message: String) : EditAnnouncementEvent
}

data class EditAnnouncementState(
    val title: String = "",
    val content: String = "",
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val hasExistingAttachment: Boolean = false,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean get() = title.isNotBlank() && content.isNotBlank()
}

@HiltViewModel
class EditAnnouncementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val announcementService: AnnouncementService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AnnouncementsRoutes.Edit>()

    private val _state = MutableStateFlow(
        EditAnnouncementState(
            title = route.title,
            content = route.content,
            hasExistingAttachment = route.hasAttachment
        )
    )
    val state: StateFlow<EditAnnouncementState> = _state.asStateFlow()

    private val _events = Channel<EditAnnouncementEvent>()
    val events: Flow<EditAnnouncementEvent> = _events.receiveAsFlow()

    fun onTitleChange(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    fun onContentChange(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    fun onAttachmentSelected(uri: Uri?, name: String?) {
        _state.value = _state.value.copy(attachmentUri = uri, attachmentName = name)
    }

    fun removeAttachment() {
        _state.value = _state.value.copy(
            attachmentUri = null,
            attachmentName = null,
            hasExistingAttachment = false
        )
    }

    fun submit() {
        val currentState = _state.value
        if (!currentState.isValid) return

        viewModelScope.launch {
            _state.value = currentState.copy(isSubmitting = true)
            runCatching {
                var pdfBytes: ByteArray? = null
                var pdfName = "attachment.pdf"

                if (currentState.attachmentUri != null) {
                    val uri = currentState.attachmentUri
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    pdfBytes = inputStream?.readBytes()
                    inputStream?.close()
                    pdfName = currentState.attachmentName ?: "attachment.pdf"
                }

                val request = AnnouncementRequestDto(
                    title = currentState.title.trim(),
                    content = currentState.content.trim(),
                    targetType = AnnouncementTargetType.WSZYSCY.name,
                    targetId = null,
                    plannedDate = null
                )

                announcementService.updateAnnouncement(route.id, request, pdfBytes, pdfName)
            }.onSuccess {
                _state.value = currentState.copy(isSubmitting = false)
                _events.send(EditAnnouncementEvent.Success)
            }.onFailure { e ->
                _state.value = currentState.copy(isSubmitting = false)
                _events.send(EditAnnouncementEvent.ShowError(e.message ?: "Błąd zapisu ogłoszenia"))
            }
        }
    }
}
