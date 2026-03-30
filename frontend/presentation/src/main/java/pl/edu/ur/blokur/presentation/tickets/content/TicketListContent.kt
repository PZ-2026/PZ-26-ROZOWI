package pl.edu.ur.blokur.presentation.tickets.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.Ticket
import pl.edu.ur.blokur.presentation.tickets.component.TicketListItem
import pl.edu.ur.blokur.presentation.tickets.util.toPresentation

@Composable
fun TicketListContent(
    tickets: List<Ticket>,
    onTicketClicked: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tickets.forEach { ticket ->
            val presentation = ticket.status.toPresentation()
            val assignedTo = ticket.assignedTo
            val dateOrAssignee = if (assignedTo != null)
                "${ticket.createdAt.take(10)} • Przypisane: ${assignedTo.fullName}"
            else
                "${ticket.createdAt.take(10)} • Brak przypisania"

            TicketListItem(
                title = ticket.title,
                date = dateOrAssignee,
                categoryName = ticket.category.name,
                statusText = presentation.label,
                statusColorHex = presentation.color.value.toLong(),
                onClick = { onTicketClicked(ticket.id) }
            )
        }
    }
}
