package pl.edu.ur.blokur.ui.views.users.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.dtos.ApartmentNodeDto
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.CreateAdminUserRequest
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.services.AdminUserService
import pl.edu.ur.blokur.services.PropertyService
import javax.inject.Inject

// ── States ───────────────────────────────────────────────────────────────────

sealed interface UsersUiState {
    data object Loading : UsersUiState
    data class Error(val message: String) : UsersUiState
    data class Success(
        val users: List<AdminUserDto>,
        val searchQuery: String = "",
        val page: Int = 0,
        val isFetchingNextPage: Boolean = false,
        val isLastPage: Boolean = false
    ) : UsersUiState {
        val filtered: List<AdminUserDto> get() = users
    }
}

sealed interface UsersEvent {
    data class ShowSnackbar(val message: String) : UsersEvent
}

// ── Form state ───────────────────────────────────────────────────────────────

data class NewUserFormState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "MIESZKANIEC",
    // Wybór drzewa lokalu
    val buildings: List<BuildingTreeNodeDto> = emptyList(),
    val selectedBuilding: BuildingTreeNodeDto? = null,
    val selectedStaircase: StaircaseNodeDto? = null,
    val selectedApartment: ApartmentNodeDto? = null,
    val isLoadingBuildings: Boolean = false,
    val buildingsError: String? = null,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() &&
                email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) &&
                (role != "MIESZKANIEC" || selectedApartment != null)
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val adminUserService: AdminUserService,
    private val propertyService: PropertyService
) : ViewModel() {

    private val _state = MutableStateFlow<UsersUiState>(UsersUiState.Loading)
    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    private val _events = Channel<UsersEvent>()
    val events: Flow<UsersEvent> = _events.receiveAsFlow()

    private val _formState = MutableStateFlow(NewUserFormState())
    val formState: StateFlow<NewUserFormState> = _formState.asStateFlow()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .collect { query ->
                    fetchUsers(reset = true, query = query)
                }
        }
    }

    fun reload() {
        viewModelScope.launch {
            fetchUsers(reset = true, query = _searchQuery.value)
        }
    }

    private fun fetchUsers(reset: Boolean, query: String) {
        viewModelScope.launch {
            val current = _state.value as? UsersUiState.Success
            val currentPage = if (reset) 0 else (current?.page ?: 0)
            val currentUsers = if (reset) emptyList() else (current?.users ?: emptyList())
            
            if (reset) {
                _state.value = UsersUiState.Loading
            } else if (current != null) {
                _state.value = current.copy(isFetchingNextPage = true)
            }

            runCatching { 
                adminUserService.getAllUsers(
                    page = currentPage,
                    size = 15,
                    search = query.takeIf { it.isNotBlank() }
                ) 
            }
                .onSuccess { pageDto ->
                    _state.value = UsersUiState.Success(
                        users = currentUsers + pageDto.content,
                        searchQuery = query,
                        page = currentPage + 1,
                        isFetchingNextPage = false,
                        isLastPage = pageDto.last
                    )
                }
                .onFailure { e ->
                    if (reset) {
                        _state.value = UsersUiState.Error(e.message ?: "Błąd ładowania")
                    } else if (current != null) {
                        _state.value = current.copy(isFetchingNextPage = false)
                        _events.send(UsersEvent.ShowSnackbar(e.message ?: "Błąd ładowania"))
                    }
                }
        }
    }

    fun loadNextPage() {
        val current = _state.value as? UsersUiState.Success ?: return
        if (current.isLastPage || current.isFetchingNextPage) return
        fetchUsers(reset = false, query = current.searchQuery)
    }

    fun onSearchChanged(query: String) {
        val current = _state.value as? UsersUiState.Success
        if (current != null) {
            _state.value = current.copy(searchQuery = query)
        }
        _searchQuery.value = query
    }

    // ── Dialog ───────────────────────────────────────────────────────────────

    fun openCreateDialog() {
        _formState.value = NewUserFormState()
        _showDialog.value = true
        loadBuildingTree()
    }

    fun closeDialog() {
        _showDialog.value = false
    }

    private fun loadBuildingTree() {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoadingBuildings = true, buildingsError = null)
            runCatching { propertyService.getBuildingTree() }
                .onSuccess { buildings ->
                    _formState.value = _formState.value.copy(
                        buildings = buildings,
                        isLoadingBuildings = false
                    )
                }
                .onFailure { e ->
                    _formState.value = _formState.value.copy(
                        isLoadingBuildings = false,
                        buildingsError = e.message ?: "Błąd ładowania struktury budynków"
                    )
                }
        }
    }

    // ── Form field updates ────────────────────────────────────────────────────

    fun onFirstNameChanged(v: String) { _formState.value = _formState.value.copy(firstName = v) }
    fun onLastNameChanged(v: String) { _formState.value = _formState.value.copy(lastName = v) }
    fun onEmailChanged(v: String) { _formState.value = _formState.value.copy(email = v) }

    fun onRoleChanged(role: String) {
        _formState.value = _formState.value.copy(
            role = role,
            // Przy zmianie roli na nie-mieszkaniec czyścimy wybór lokalu
            selectedBuilding = if (role == "MIESZKANIEC") _formState.value.selectedBuilding else null,
            selectedStaircase = if (role == "MIESZKANIEC") _formState.value.selectedStaircase else null,
            selectedApartment = if (role == "MIESZKANIEC") _formState.value.selectedApartment else null
        )
    }

    fun onBuildingSelected(building: BuildingTreeNodeDto?) {
        _formState.value = _formState.value.copy(
            selectedBuilding = building,
            selectedStaircase = null,
            selectedApartment = null
        )
    }

    fun onStaircaseSelected(staircase: StaircaseNodeDto?) {
        _formState.value = _formState.value.copy(
            selectedStaircase = staircase,
            selectedApartment = null
        )
    }

    fun onApartmentSelected(apartment: ApartmentNodeDto?) {
        _formState.value = _formState.value.copy(selectedApartment = apartment)
    }

    // ── Submit ────────────────────────────────────────────────────────────────

    fun submitCreateUser() {
        val form = _formState.value
        if (!form.isValid) return

        viewModelScope.launch {
            _formState.value = form.copy(isSubmitting = true)
            val request = CreateAdminUserRequest(
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                email = form.email.trim(),
                role = form.role,
                apartmentId = form.selectedApartment?.id
            )
            runCatching { adminUserService.createUser(request) }
                .onSuccess { newUser ->
                    closeDialog()
                    val current = _state.value as? UsersUiState.Success
                    if (current != null) {
                        _state.value = current.copy(users = listOf(newUser) + current.users)
                    }
                    _events.send(UsersEvent.ShowSnackbar(
                        "Konto dla ${newUser.fullName} zostało utworzone. " +
                        "Użytkownik musi ustawić hasło przez e-mail."
                    ))
                }
                .onFailure { e ->
                    _formState.value = form.copy(isSubmitting = false)
                    _events.send(UsersEvent.ShowSnackbar(e.message ?: "Błąd tworzenia konta"))
                }
        }
    }

    fun deactivateUser(id: String, name: String) {
        viewModelScope.launch {
            runCatching { adminUserService.deactivateUser(id) }
                .onSuccess {
                    val current = _state.value as? UsersUiState.Success ?: return@launch
                    _state.value = current.copy(
                        users = current.users.map {
                            if (it.id == id) it.copy(active = false) else it
                        }
                    )
                    _events.send(UsersEvent.ShowSnackbar("Konto użytkownika $name zostało deaktywowane"))
                }
                .onFailure { e ->
                    _events.send(UsersEvent.ShowSnackbar(e.message ?: "Błąd deaktywacji"))
                }
        }
    }
}
