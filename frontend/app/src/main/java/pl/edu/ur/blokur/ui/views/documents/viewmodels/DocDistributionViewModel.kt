package pl.edu.ur.blokur.ui.views.documents.viewmodels

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
import pl.edu.ur.blokur.services.AnnualSettlementDistributionRequestDto
import pl.edu.ur.blokur.services.DocumentApiService
import pl.edu.ur.blokur.services.RateChangeDistributionRequestDto
import javax.inject.Inject

// ── Enums ─────────────────────────────────────────────────────────────────────

enum class DocDistributionTab { RATE_CHANGE, ANNUAL_SETTLEMENT }

/**
 * Zakres odbiorców dokumentu.
 * ALL = wszyscy mieszkańcy wspólnoty
 * BUILDING = jeden budynek (targetId = buildingId)
 * APARTMENT = jeden lokal (targetId = apartmentId)
 */
enum class RecipientScope { ALL, BUILDING, APARTMENT }

// ── State & Events ────────────────────────────────────────────────────────────

data class DocDistributionState(
    val activeTab: DocDistributionTab = DocDistributionTab.RATE_CHANGE,
    // ── Rate Change form
    val rateChangeSubject: String = "",
    val rateChangeBody: String = "",
    val rateChangeEffectiveDate: String = "",
    // ── Annual Settlement form
    val settlementYear: String = "",
    val settlementNote: String = "",
    // ── Shared recipient config
    val recipientScope: RecipientScope = RecipientScope.ALL,
    val targetId: String = "",
    // ── Status
    val isSubmitting: Boolean = false,
    val lastSentTab: DocDistributionTab? = null
)

sealed interface DocDistributionEvent {
    data class ShowSnackbar(val message: String) : DocDistributionEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class DocDistributionViewModel @Inject constructor(
    private val documentApiService: DocumentApiService
) : ViewModel() {

    private val _state = MutableStateFlow(DocDistributionState())
    val state: StateFlow<DocDistributionState> = _state.asStateFlow()

    private val _events = Channel<DocDistributionEvent>()
    val events: Flow<DocDistributionEvent> = _events.receiveAsFlow()

    // ── Tab navigation ────────────────────────────────────────────────────────

    fun selectTab(tab: DocDistributionTab) {
        _state.value = _state.value.copy(activeTab = tab, lastSentTab = null)
    }

    // ── Rate Change form handlers ─────────────────────────────────────────────

    fun onRateChangeSubjectChanged(v: String) {
        _state.value = _state.value.copy(rateChangeSubject = v)
    }

    fun onRateChangeBodyChanged(v: String) {
        _state.value = _state.value.copy(rateChangeBody = v)
    }

    fun onRateChangeEffectiveDateChanged(v: String) {
        _state.value = _state.value.copy(rateChangeEffectiveDate = v)
    }

    // ── Annual Settlement form handlers ───────────────────────────────────────

    fun onSettlementYearChanged(v: String) {
        _state.value = _state.value.copy(settlementYear = v)
    }

    fun onSettlementNoteChanged(v: String) {
        _state.value = _state.value.copy(settlementNote = v)
    }

    // ── Shared handlers ───────────────────────────────────────────────────────

    fun onRecipientScopeChanged(scope: RecipientScope) {
        _state.value = _state.value.copy(recipientScope = scope, targetId = "")
    }

    fun onTargetIdChanged(v: String) {
        _state.value = _state.value.copy(targetId = v)
    }

    // ── Send actions ──────────────────────────────────────────────────────────

    fun sendRateChange() {
        val s = _state.value
        if (s.isSubmitting) return
        if (s.rateChangeSubject.isBlank() || s.rateChangeBody.isBlank()) return
        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, lastSentTab = null)
            runCatching {
                documentApiService.distributeRateChange(
                    RateChangeDistributionRequestDto(
                        subject = s.rateChangeSubject,
                        body = s.rateChangeBody,
                        effectiveDate = s.rateChangeEffectiveDate,
                        scope = s.recipientScope.name,
                        targetId = s.targetId.ifBlank { null }
                    )
                )
            }.onSuccess { response ->
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    lastSentTab = DocDistributionTab.RATE_CHANGE
                )
                val result = response.body()
                val msg = result?.message
                    ?: "Zawiadomienie wysłane do ${result?.recipientsNotified ?: 0} mieszkańców"
                _events.send(DocDistributionEvent.ShowSnackbar(msg))
            }.onFailure { e ->
                _state.value = _state.value.copy(isSubmitting = false)
                _events.send(DocDistributionEvent.ShowSnackbar(e.message ?: "Błąd wysyłki"))
            }
        }
    }

    fun sendAnnualSettlement() {
        val s = _state.value
        if (s.isSubmitting) return
        val year = s.settlementYear.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, lastSentTab = null)
            runCatching {
                documentApiService.distributeAnnualSettlement(
                    AnnualSettlementDistributionRequestDto(
                        year = year,
                        note = s.settlementNote.ifBlank { null },
                        scope = s.recipientScope.name,
                        targetId = s.targetId.ifBlank { null }
                    )
                )
            }.onSuccess { response ->
                _state.value = _state.value.copy(
                    isSubmitting = false,
                    lastSentTab = DocDistributionTab.ANNUAL_SETTLEMENT
                )
                val result = response.body()
                val msg = result?.message
                    ?: "Rozliczenie za $year wygenerowane dla ${result?.documentsGenerated ?: 0} lokali"
                _events.send(DocDistributionEvent.ShowSnackbar(msg))
            }.onFailure { e ->
                _state.value = _state.value.copy(isSubmitting = false)
                _events.send(DocDistributionEvent.ShowSnackbar(e.message ?: "Błąd wysyłki"))
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun scopeLabel(scope: RecipientScope, targetId: String) = when (scope) {
        RecipientScope.ALL -> "wszyscy mieszkańcy"
        RecipientScope.BUILDING -> "budynek $targetId"
        RecipientScope.APARTMENT -> "lokal $targetId"
    }
}
