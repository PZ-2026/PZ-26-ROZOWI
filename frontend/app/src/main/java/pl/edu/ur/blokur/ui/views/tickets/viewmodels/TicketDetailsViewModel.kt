package pl.edu.ur.blokur.ui.views.tickets.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
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
import pl.edu.ur.blokur.services.PdfApiService
import pl.edu.ur.blokur.services.TicketService
import pl.edu.ur.blokur.services.TicketCommentApiService
import pl.edu.ur.blokur.services.TicketImageApiService
import pl.edu.ur.blokur.services.WorkAcceptanceProtocolRequestDto
import pl.edu.ur.blokur.ui.views.tickets.TicketRoutes
import pl.edu.ur.blokur.ui.views.tickets.utils.ConservatorActionType
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsListState
import pl.edu.ur.blokur.ui.views.tickets.utils.TicketDetailsScreenEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TicketDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ticketService: TicketService,
    private val commentApi: TicketCommentApiService,
    private val imageApi: TicketImageApiService,
    private val pdfApi: PdfApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val route = savedStateHandle.toRoute<TicketRoutes.Details>()

    private val _state = MutableStateFlow<TicketDetailsListState>(TicketDetailsListState.Loading)
    val state: StateFlow<TicketDetailsListState> = _state.asStateFlow()

    private val _events = Channel<TicketDetailsScreenEvent>()
    val events: Flow<TicketDetailsScreenEvent> = _events.receiveAsFlow()

    init {
        loadTicket()
    }

    private fun loadTicket() {
        viewModelScope.launch {
            runCatching {
                val ticket = ticketService.getTicketById(route.ticketId)
                    ?: error("Nie znaleziono zgłoszenia #${route.ticketId}")
                val conservators = ticketService.getAvailableConservators()
                ticket to conservators
            }.onSuccess { (ticket, conservators) ->
                val role = ticketService.getCurrentUserRole()
                _state.value = TicketDetailsListState.Success(
                    ticket = ticket,
                    availableConservators = conservators,
                    currentUserRole = role
                )
                // Ładuj komentarze, zdjęcia i historię równolegle po załadowaniu ticketu
                loadComments(ticket.id)
                loadImages(ticket.id)
                loadHistory(ticket.id)
            }.onFailure { e ->
                _state.value = TicketDetailsListState.Error(e.message ?: "Błąd ładowania zgłoszenia")
            }
        }
    }

    private fun loadComments(ticketId: String) {
        val current = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            _state.value = current.copy(isLoadingComments = true)
            runCatching { commentApi.getComments(ticketId) }
                .onSuccess { response ->
                    val s = _state.value as? TicketDetailsListState.Success ?: return@onSuccess
                    _state.value = s.copy(
                        comments = if (response.isSuccessful) response.body() ?: emptyList() else emptyList(),
                        isLoadingComments = false
                    )
                }
                .onFailure { e ->
                    val s = _state.value as? TicketDetailsListState.Success ?: return@onFailure
                    _state.value = s.copy(isLoadingComments = false)
                    _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd ładowania komentarzy"))
                }
        }
    }

    private fun loadImages(ticketId: String) {
        viewModelScope.launch {
            runCatching { imageApi.getImagesForTicket(ticketId) }
                .onSuccess { response ->
                    val s = _state.value as? TicketDetailsListState.Success ?: return@onSuccess
                    _state.value = s.copy(
                        images = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                    )
                }
                .onFailure { e ->
                    _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd ładowania obrazów"))
                }
        }
    }

    private fun loadHistory(ticketId: String) {
        viewModelScope.launch {
            runCatching { ticketService.getTicketHistory(ticketId) }
                .onSuccess { history ->
                    val s = _state.value as? TicketDetailsListState.Success ?: return@onSuccess
                    _state.value = s.copy(
                        history = history,
                        historyError = null
                    )
                }
                .onFailure { error ->
                    val s = _state.value as? TicketDetailsListState.Success ?: return@onFailure
                    _state.value = s.copy(
                        history = emptyList(), // Pusta lista by ukryć loading
                        historyError = error.message ?: "Błąd ładowania historii zgłoszenia"
                    )
                }
        }
    }

    fun addComment(content: String, commentType: String) {
        val current = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                commentApi.addComment(
                    current.ticket.id,
                    pl.edu.ur.blokur.dtos.TicketCommentRequestDto(content = content, commentType = commentType)
                )
            }.onSuccess {
                loadComments(current.ticket.id)
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd dodawania komentarza"))
            }
        }
    }

    fun deleteImage(imageId: String) {
        val current = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching { imageApi.deleteImage(imageId) }
                .onSuccess {
                    loadImages(current.ticket.id)
                    _events.send(TicketDetailsScreenEvent.ShowSnackbar("Zdjęcie zostało usunięte pomyślnie"))
                }
                .onFailure { e ->
                    _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd usuwania obrazu"))
                }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(TicketDetailsScreenEvent.NavigateBack) }
    }

    fun onAssignConservator(conservatorId: String, plannedVisitAt: String) {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                ticketService.assignTicket(
                    ticketId = currentState.ticket.id,
                    conservatorId = conservatorId,
                    plannedVisitAt = plannedVisitAt
                )
            }.onSuccess {
                loadTicket()
                _events.send(TicketDetailsScreenEvent.ShowSnackbar("Konserwator został przypisany pomyślnie"))
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd przypisywania"))
            }
        }
    }

    fun onRejectTicket(reason: String) {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                ticketService.rejectTicket(
                    ticketId = currentState.ticket.id,
                    reason = reason
                )
            }.onSuccess {
                loadTicket()
                _events.send(TicketDetailsScreenEvent.ShowSnackbar("Zgłoszenie odrzucone"))
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd odrzucania zgłoszenia"))
            }
        }
    }

    fun onCloseTicket() {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                ticketService.closeTicket(ticketId = currentState.ticket.id)
            }.onSuccess {
                loadTicket()
                _events.send(TicketDetailsScreenEvent.ShowSnackbar("Zgłoszenie zostało zamknięte"))
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd zamykania zgłoszenia"))
            }
        }
    }

    fun onConservatorAction(type: ConservatorActionType, comment: String, pause: Boolean = false) {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        viewModelScope.launch {
            runCatching {
                when (type) {
                    ConservatorActionType.START -> ticketService.startWork(currentState.ticket.id)
                    ConservatorActionType.FINISH -> ticketService.completeWork(
                        ticketId = currentState.ticket.id,
                        workDescription = comment.ifBlank { "Prace zakończone." }
                    )
                    ConservatorActionType.PAUSE_OR_COMMENT -> ticketService.suspendWork(
                        ticketId = currentState.ticket.id,
                        reason = comment.ifBlank { "Prace wstrzymane." }
                    )
                    ConservatorActionType.CLOSE_VERIFICATION -> ticketService.closeTicket(
                        ticketId = currentState.ticket.id
                    )
                }
            }.onSuccess {
                loadTicket()
                _events.send(TicketDetailsScreenEvent.ShowSnackbar("Zaktualizowano status zgłoszenia"))
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd wykonywania akcji"))
            }
        }
    }

    fun downloadWorkAcceptanceProtocol() {
        val currentState = _state.value as? TicketDetailsListState.Success ?: return
        val ticket = currentState.ticket
        viewModelScope.launch {
            runCatching {
                val request = WorkAcceptanceProtocolRequestDto(
                    ticketNumber = ticket.ticketNumber,
                    workDescription = ticket.internalNote ?: "Prace zakończone.",
                    maintenanceWorkerName = ticket.assignedToName ?: "Nieznany"
                )
                val response = pdfApi.getWorkAcceptanceProtocol(request)
                if (!response.isSuccessful) error("Błąd generowania protokołu (${response.code()})")
                val responseBody = response.body() ?: error("Pusta odpowiedź serwera")
                withContext(Dispatchers.IO) {
                    val dir = File(context.cacheDir, "protocols").also { it.mkdirs() }
                    val file = File(dir, "protokol_${ticket.ticketNumber}.pdf")
                    file.writeBytes(responseBody.bytes())
                    file
                }
            }.onSuccess { file ->
                val uri = FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }.onFailure { e ->
                _events.send(TicketDetailsScreenEvent.ShowError(e.message ?: "Błąd pobierania protokołu"))
            }
        }
    }
}
