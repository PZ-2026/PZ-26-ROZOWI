package pl.edu.ur.blokur.ui.views.documents.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

/**
 * ViewModel dystrybucji dokumentów zarządcy.
 *
 * WIP: Backend nie dostarcza jeszcze endpointów do generowania i dystrybucji dokumentów PDF.
 * Klasa jest gotowa architektonicznie — wysyłkę `sendRateChange()` i `sendAnnualSettlement()`
 * należy podłączyć do odpowiednich serwisów, gdy będą dostępne.
 */
@HiltViewModel
class DocDistributionViewModel @Inject constructor() : ViewModel() {

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

    /**
     * Wysyła zawiadomienie o zmianie stawek.
     *
     * TODO WIP: Podłączyć do endpointu POST /api/documents/rate-change (lub analogicznego)
     * gdy zostanie zaimplementowany.
     */
    fun sendRateChange() {
        val s = _state.value
        if (s.isSubmitting) return
        if (s.rateChangeSubject.isBlank() || s.rateChangeBody.isBlank()) return
        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, lastSentTab = null)
            // WIP: zastąp symulację prawdziwym wywołaniem API
            runCatching { simulateSend() }
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        lastSentTab = DocDistributionTab.RATE_CHANGE
                    )
                    val scopeLabel = scopeLabel(s.recipientScope, s.targetId)
                    _events.send(DocDistributionEvent.ShowSnackbar(
                        "Zawiadomienie wysłane do: $scopeLabel (WIP — symulacja)"
                    ))
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isSubmitting = false)
                    _events.send(DocDistributionEvent.ShowSnackbar(e.message ?: "Błąd wysyłki"))
                }
        }
    }

    /**
     * Wysyła rozliczenie roczne.
     *
     * TODO WIP: Podłączyć do endpointu POST /api/documents/annual-settlement
     * gdy zostanie zaimplementowany.
     */
    fun sendAnnualSettlement() {
        val s = _state.value
        if (s.isSubmitting) return
        val year = s.settlementYear.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.value = s.copy(isSubmitting = true, lastSentTab = null)
            // WIP: zastąp symulację prawdziwym wywołaniem API
            runCatching { simulateSend() }
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        lastSentTab = DocDistributionTab.ANNUAL_SETTLEMENT
                    )
                    val scopeLabel = scopeLabel(s.recipientScope, s.targetId)
                    _events.send(DocDistributionEvent.ShowSnackbar(
                        "Rozliczenie za $year wysłane do: $scopeLabel (WIP — symulacja)"
                    ))
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isSubmitting = false)
                    _events.send(DocDistributionEvent.ShowSnackbar(e.message ?: "Błąd wysyłki"))
                }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Symuluje opóźnienie sieciowe (zostanie zastąpione wywołaniem API). */
    private suspend fun simulateSend() {
        delay(1500)
    }

    private fun scopeLabel(scope: RecipientScope, targetId: String) = when (scope) {
        RecipientScope.ALL -> "wszyscy mieszkańcy"
        RecipientScope.BUILDING -> "budynek $targetId"
        RecipientScope.APARTMENT -> "lokal $targetId"
    }
}
