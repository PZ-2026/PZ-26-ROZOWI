package pl.edu.ur.blokur.presentation.tickets.util

import androidx.compose.ui.graphics.Color
import pl.edu.ur.blokur.domain.model.TicketStatus
import pl.edu.ur.blokur.presentation.common.theme.ErrorRed
import pl.edu.ur.blokur.presentation.common.theme.InfoBlue
import pl.edu.ur.blokur.presentation.common.theme.SuccessGreen
import pl.edu.ur.blokur.presentation.common.theme.WarningOrange

data class StatusPresentation(val label: String, val color: Color)

fun TicketStatus.toPresentation(): StatusPresentation = when (this) {
    TicketStatus.NOWE -> StatusPresentation("Nowe", InfoBlue)
    TicketStatus.ZAPLANOWANO -> StatusPresentation("Zaplanowano", InfoBlue)
    TicketStatus.W_REALIZACJI -> StatusPresentation("W realizacji", WarningOrange)
    TicketStatus.WSTRZYMANO -> StatusPresentation("Wstrzymano", WarningOrange)
    TicketStatus.ZAKONCZONE -> StatusPresentation("Zakończone", SuccessGreen)
    TicketStatus.ZAMKNIETE -> StatusPresentation("Zamknięte", SuccessGreen)
    TicketStatus.ODRZUCONE -> StatusPresentation("Odrzucone", ErrorRed)
}

// helper for component that receives raw data without domain model
fun statusPresentation(text: String, colorHex: Long): Pair<String, Color> =
    text to Color(colorHex)
