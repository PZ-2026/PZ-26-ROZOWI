package pl.edu.ur.blokur.ui.views.announcements.viewmodels

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
import pl.edu.ur.blokur.services.AnnouncementService
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.dtos.UserRole
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsEvent
import pl.edu.ur.blokur.ui.views.announcements.utils.AnnouncementsState
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val announcementService: AnnouncementService,
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<AnnouncementsState>(AnnouncementsState.Loading)
    val state: StateFlow<AnnouncementsState> = _state.asStateFlow()

    private val _events = Channel<AnnouncementsEvent>()
    val events: Flow<AnnouncementsEvent> = _events.receiveAsFlow()

    private val _isManager = MutableStateFlow(false)
    val isManager: StateFlow<Boolean> = _isManager.asStateFlow()

    init {
        viewModelScope.launch {
            _isManager.value = authService.getCurrentUserRole() == UserRole.ZARZADCA
        }
        loadAnnouncements()
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _state.value = AnnouncementsState.Loading
            runCatching { announcementService.getAnnouncements() }
                .onSuccess { list ->
                    _state.value = if (list.isEmpty()) AnnouncementsState.Empty
                    else AnnouncementsState.Success(list)
                }
                .onFailure { e ->
                    _state.value = AnnouncementsState.Error(e.message ?: "Błąd ładowania ogłoszeń")
                    _events.send(AnnouncementsEvent.ShowError(e.message ?: "Błąd ładowania ogłoszeń"))
                }
        }
    }

    fun downloadAttachment(announcementId: String, title: String) {
        viewModelScope.launch {
            runCatching { announcementService.getAttachment(announcementId) }
                .onSuccess { body ->
                    try {
                        val dir = File(context.cacheDir, "announcements").also { it.mkdirs() }
                        val safe = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
                        val file = File(dir, "zalacznik_${safe}.pdf")
                        file.writeBytes(body.bytes())
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
                        _events.send(AnnouncementsEvent.ShowError("Nie można otworzyć załącznika: ${e.message}"))
                    }
                }
                .onFailure { e ->
                    _events.send(AnnouncementsEvent.ShowError(e.message ?: "Błąd pobierania załącznika"))
                }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            runCatching { announcementService.deleteAnnouncement(id) }
                .onSuccess {
                    _events.send(AnnouncementsEvent.ShowError("Usunięto ogłoszenie"))
                    loadAnnouncements()
                }
                .onFailure { e ->
                    _events.send(AnnouncementsEvent.ShowError(e.message ?: "Błąd usuwania ogłoszenia"))
                }
        }
    }
}