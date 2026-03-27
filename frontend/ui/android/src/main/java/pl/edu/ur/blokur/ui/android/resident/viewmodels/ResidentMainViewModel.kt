package pl.edu.ur.blokur.ui.android.resident.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pl.edu.ur.blokur.ui.android.resident.states.ResidentMainEvent
import pl.edu.ur.blokur.ui.android.resident.states.ResidentMainState
import javax.inject.Inject

class ResidentMainViewModel @Inject constructor(

) : ViewModel() {
    private val _state = MutableStateFlow<ResidentMainState>(ResidentMainState.Loading)
    val state: StateFlow<ResidentMainState> = _state.asStateFlow()

    private val _events = Channel<ResidentMainEvent>()
    val events: Flow<ResidentMainEvent> = _events.receiveAsFlow()

    init {

    }



}