package pl.edu.ur.blokur.ui.views.categories.viewmodels

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
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.services.CategoryService
import javax.inject.Inject

// ── States ──────────────────────────────────────────────────────────────────

sealed interface CategoriesUiState {
    data object Loading : CategoriesUiState
    data class Error(val message: String) : CategoriesUiState
    data class Success(
        val categories: List<CategoryDto>,
        val isSubmitting: Boolean = false
    ) : CategoriesUiState
}

sealed interface CategoriesEvent {
    data class ShowSnackbar(val message: String) : CategoriesEvent
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryService: CategoryService
) : ViewModel() {

    private val _state = MutableStateFlow<CategoriesUiState>(CategoriesUiState.Loading)
    val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    private val _events = Channel<CategoriesEvent>()
    val events: Flow<CategoriesEvent> = _events.receiveAsFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = CategoriesUiState.Loading
            runCatching { categoryService.getCategories() }
                .onSuccess { _state.value = CategoriesUiState.Success(it) }
                .onFailure { _state.value = CategoriesUiState.Error(it.message ?: "Błąd ładowania") }
        }
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return
        val current = _state.value as? CategoriesUiState.Success ?: return
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true)
            runCatching { categoryService.createCategory(name) }
                .onSuccess { created ->
                    // Dodaj nową kategorię na początku listy
                    _state.value = CategoriesUiState.Success(
                        categories = listOf(CategoryDto(created.id, created.name)) + current.categories
                    )
                    _events.send(CategoriesEvent.ShowSnackbar("Kategoria \"${created.name}\" została dodana"))
                }
                .onFailure { e ->
                    _state.value = current.copy(isSubmitting = false)
                    _events.send(CategoriesEvent.ShowSnackbar(e.message ?: "Błąd tworzenia kategorii"))
                }
        }
    }

    fun updateCategory(id: String, name: String) {
        if (name.isBlank()) return
        val current = _state.value as? CategoriesUiState.Success ?: return
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true)
            runCatching { categoryService.updateCategory(id, name) }
                .onSuccess { updated ->
                    _state.value = CategoriesUiState.Success(
                        categories = current.categories.map {
                            if (it.id == id) CategoryDto(updated.id, updated.name) else it
                        }
                    )
                    _events.send(CategoriesEvent.ShowSnackbar("Kategoria \"${updated.name}\" została zaktualizowana"))
                }
                .onFailure { e ->
                    _state.value = current.copy(isSubmitting = false)
                    _events.send(CategoriesEvent.ShowSnackbar(e.message ?: "Błąd aktualizacji"))
                }
        }
    }

    fun deactivateCategory(id: String, name: String) {
        val current = _state.value as? CategoriesUiState.Success ?: return
        viewModelScope.launch {
            runCatching { categoryService.deactivateCategory(id) }
                .onSuccess {
                    // Usuń z listy (backend zwraca tylko aktywne)
                    _state.value = CategoriesUiState.Success(
                        categories = current.categories.filter { it.id != id }
                    )
                    _events.send(CategoriesEvent.ShowSnackbar("Kategoria \"$name\" została deaktywowana"))
                }
                .onFailure { e ->
                    _events.send(CategoriesEvent.ShowSnackbar(e.message ?: "Błąd deaktywacji"))
                }
        }
    }

    fun setSla(id: String, hours: Int) {
        val current = _state.value as? CategoriesUiState.Success ?: return
        viewModelScope.launch {
            _state.value = current.copy(isSubmitting = true)
            runCatching { categoryService.setSla(id, hours) }
                .onSuccess {
                    _state.value = CategoriesUiState.Success(
                        categories = current.categories.map {
                            if (it.id == id) it.copy(slaHours = hours) else it
                        }
                    )
                    _events.send(CategoriesEvent.ShowSnackbar("SLA ustawione na $hours godz."))
                }
                .onFailure { e ->
                    _state.value = current.copy(isSubmitting = false)
                    _events.send(CategoriesEvent.ShowSnackbar(e.message ?: "Błąd ustawiania SLA"))
                }
        }
    }
}
