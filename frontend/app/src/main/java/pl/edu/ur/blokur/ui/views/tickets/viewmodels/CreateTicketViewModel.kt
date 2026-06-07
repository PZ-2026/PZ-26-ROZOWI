package pl.edu.ur.blokur.ui.views.tickets.viewmodels

import android.content.Context
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
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.dtos.CreateTicketRequest
import pl.edu.ur.blokur.services.TicketImageService
import pl.edu.ur.blokur.services.TicketService
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketFormState
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketScreenEvent
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketSubmitState
import javax.inject.Inject

@HiltViewModel
class CreateTicketViewModel @Inject constructor(
    private val ticketService: TicketService,
    private val ticketImageService: TicketImageService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private val _categoriesLoading = MutableStateFlow(true)
    val categoriesLoading: StateFlow<Boolean> = _categoriesLoading.asStateFlow()

    private val _formState = MutableStateFlow(CreateTicketFormState())
    val formState: StateFlow<CreateTicketFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<CreateTicketSubmitState>(CreateTicketSubmitState.Idle)
    val submitState: StateFlow<CreateTicketSubmitState> = _submitState.asStateFlow()

    private val _events = Channel<CreateTicketScreenEvent>()
    val events: Flow<CreateTicketScreenEvent> = _events.receiveAsFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _categoriesLoading.value = true
            runCatching { ticketService.getCategories() }
                .onSuccess { _categories.value = it }
                .onFailure { /* zachowaj pustą listę, UI wyświetli brak */ }
            _categoriesLoading.value = false
        }
    }

    fun onFormChanged(state: CreateTicketFormState) {
        _formState.value = state
        if (_submitState.value is CreateTicketSubmitState.Error) {
            _submitState.value = CreateTicketSubmitState.Idle
        }
    }

    fun submit() {
        val form = _formState.value
        if (form.title.isBlank() || form.selectedCategoryId.isBlank() || form.description.isBlank()) {
            _submitState.value = CreateTicketSubmitState.Error("Wypełnij wszystkie pola")
            return
        }
        viewModelScope.launch {
            _submitState.value = CreateTicketSubmitState.Submitting
            runCatching {
                val createdTicket = ticketService.createTicket(
                    CreateTicketRequest(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        categoryId = form.selectedCategoryId
                    )
                )

                // Prześlij wybrane zdjęcia
                if (form.imageUris.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        val resolver = context.contentResolver
                        for (uri in form.imageUris) {
                            try {
                                val mime = resolver.getType(uri) ?: "image/jpeg"
                                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                                    ?: continue
                                val ext = if (mime.contains("png")) "png" else "jpg"
                                val filename = "photo_${System.currentTimeMillis()}.$ext"
                                ticketImageService.uploadImage(
                                    ticketId = createdTicket.id,
                                    imageType = "BEFORE",
                                    imageBytes = bytes,
                                    filename = filename,
                                    mimeType = mime
                                )
                            } catch (e: Exception) {
                                // Ignoruj błędy pojedynczych zdjęć
                            }
                        }
                    }
                }

                createdTicket
            }.onSuccess { created ->
                _submitState.value = CreateTicketSubmitState.Success
                _events.send(CreateTicketScreenEvent.ShowSuccess(created.ticketNumber))
            }.onFailure { e ->
                _submitState.value = CreateTicketSubmitState.Error(e.message ?: "Nieznany błąd")
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch { _events.send(CreateTicketScreenEvent.NavigateBack) }
    }
}
