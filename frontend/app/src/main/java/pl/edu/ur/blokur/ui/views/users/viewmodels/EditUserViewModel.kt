package pl.edu.ur.blokur.ui.views.users.viewmodels

import androidx.lifecycle.SavedStateHandle
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
import pl.edu.ur.blokur.dtos.AdminUserDto
import pl.edu.ur.blokur.dtos.ApartmentNodeDto
import pl.edu.ur.blokur.dtos.BuildingTreeNodeDto
import pl.edu.ur.blokur.dtos.StaircaseNodeDto
import pl.edu.ur.blokur.dtos.UpdateAdminUserRequest
import pl.edu.ur.blokur.services.AdminUserService
import pl.edu.ur.blokur.services.PropertyService
import javax.inject.Inject
import pl.edu.ur.blokur.services.AuthService
import pl.edu.ur.blokur.services.SessionManager
import pl.edu.ur.blokur.services.TokenStorage

sealed interface EditUserUiState {
    data object Loading : EditUserUiState
    data class Error(val message: String) : EditUserUiState
    data class Success(val user: AdminUserDto) : EditUserUiState
}

sealed interface EditUserEvent {
    data class ShowSnackbar(val message: String) : EditUserEvent
    data object NavigateBack : EditUserEvent
}



@HiltViewModel
class EditUserViewModel @Inject constructor(
    private val adminUserService: AdminUserService,
    private val propertyService: PropertyService,
    private val tokenStorage: TokenStorage,
    private val authService: AuthService,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = savedStateHandle.get<String>("userId") ?: ""

    private val _state = MutableStateFlow<EditUserUiState>(EditUserUiState.Loading)
    val state: StateFlow<EditUserUiState> = _state.asStateFlow()

    private val _events = Channel<EditUserEvent>()
    val events: Flow<EditUserEvent> = _events.receiveAsFlow()

    private val _formState = MutableStateFlow(NewUserFormState())
    val formState: StateFlow<NewUserFormState> = _formState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        loadUser()
    }

    fun reload() {
        loadUser()
    }

    private fun loadUser() {
        if (userId.isEmpty()) {
            _state.value = EditUserUiState.Error("Brak identyfikatora użytkownika")
            return
        }

        viewModelScope.launch {
            _state.value = EditUserUiState.Loading
            runCatching { adminUserService.getUserById(userId) }
                .onSuccess { user ->
                    _state.value = EditUserUiState.Success(user)
                    _formState.value = NewUserFormState(
                        firstName = user.firstName,
                        lastName = user.lastName,
                        email = user.email, // email is read-only in this form
                        role = user.role,
                        phone = user.phone ?: ""
                    )
                    loadBuildingTree()
                }
                .onFailure {
                    _state.value = EditUserUiState.Error(it.message ?: "Błąd ładowania profilu")
                }
        }
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
                        buildingsError = e.message ?: "Błąd ładowania budynków"
                    )
                }
        }
    }

    fun onFirstNameChanged(v: String) { _formState.value = _formState.value.copy(firstName = v) }
    fun onLastNameChanged(v: String) { _formState.value = _formState.value.copy(lastName = v) }
    fun onPhoneChanged(v: String) { _formState.value = _formState.value.copy(phone = v) }

    fun onRoleChanged(role: String) {
        _formState.value = _formState.value.copy(
            role = role,
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

    fun submitChanges() {
        val form = _formState.value
        val isValid = form.firstName.isNotBlank() && form.lastName.isNotBlank() &&
                (form.role != "MIESZKANIEC" || form.selectedApartment != null)

        if (!isValid) {
            viewModelScope.launch { _events.send(EditUserEvent.ShowSnackbar("Wypełnij wymagane pola")) }
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val request = UpdateAdminUserRequest(
                firstName = form.firstName.trim(),
                lastName = form.lastName.trim(),
                phone = form.phone.trim().takeIf { it.isNotBlank() },
                role = form.role,
                apartmentId = form.selectedApartment?.id
            )
            runCatching { adminUserService.updateUser(userId, request) }
                .onSuccess { updatedUser ->
                    val currentUserEmail = tokenStorage.getUserEmail()
                    val currentUserRole = tokenStorage.getUserRole()

                    if (updatedUser.email == currentUserEmail && updatedUser.role != currentUserRole) {
                        try {
                            val newRole = authService.forceTokenRefresh()
                            if (newRole != null) {
                                sessionManager.forceRouteRefresh()
                            }
                        } catch (e: Exception) {
                            // W razie błędu fallback do wylogowania
                            sessionManager.invalidateSession()
                        }
                    }

                    _events.send(EditUserEvent.ShowSnackbar("Zapisano zmiany"))
                    _events.send(EditUserEvent.NavigateBack)
                }
                .onFailure {
                    _events.send(EditUserEvent.ShowSnackbar(it.message ?: "Błąd zapisu"))
                }
            _isSubmitting.value = false
        }
    }
}
