package pl.edu.ur.blokur.presentation.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.ur.blokur.domain.usecase.TestAUseCase
import javax.inject.Inject
@HiltViewModel
class SampleViewModel @Inject constructor(
    private val uc : TestAUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            uc("WORK1 TEST")
            uc("WORK2 TEST")
        }
    }
}
