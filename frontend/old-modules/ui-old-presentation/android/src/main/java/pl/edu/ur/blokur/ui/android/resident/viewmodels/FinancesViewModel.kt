package pl.edu.ur.blokur.ui.android.resident.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import pl.edu.ur.blokur.ui.android.resident.states.AnnouncementsEvent
import pl.edu.ur.blokur.ui.android.resident.states.FinancesEvent
import pl.edu.ur.blokur.ui.android.resident.states.FinancesState
import javax.inject.Inject

class FinancesViewModel @Inject constructor(

) : ViewModel() {
    private val _state = MutableStateFlow< FinancesState>(FinancesState.Loading)
    val state: StateFlow<FinancesState> = _state.asStateFlow()

    private val _events = Channel<FinancesEvent>()
    val events: Flow<FinancesEvent> = _events.receiveAsFlow()

    init {

    }



}