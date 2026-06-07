package pl.edu.ur.blokur.ui.views.tickets.utils

import androidx.compose.ui.graphics.Color
import pl.edu.ur.blokur.dtos.TicketStatus
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange

data class StatusPresentation(val label: String, val color: Color)

fun TicketStatus.toPresentation(): StatusPresentation = when (this) {
    TicketStatus.NOWE -> StatusPresentation("Nowe", InfoBlue)
    TicketStatus.ZAPLANOWANO -> StatusPresentation("Zaplanowano", InfoBlue)
    TicketStatus.W_REALIZACJI -> StatusPresentation("W realizacji", WarningOrange)
    TicketStatus.WSTRZYMANO -> StatusPresentation("Wstrzymano", WarningOrange)
    TicketStatus.ZAKONCZONE_DO_WERYFIKACJI -> StatusPresentation("Zakończone do weryfikacji", SuccessGreen)
    TicketStatus.ZAMKNIETE -> StatusPresentation("Zamknięte", SuccessGreen)
    TicketStatus.ODRZUCONE -> StatusPresentation("Odrzucone", ErrorRed)
}

// helper for component that receives raw data without domain model
fun statusPresentation(text: String, colorHex: Long): Pair<String, Color> =
    text to Color(colorHex)
