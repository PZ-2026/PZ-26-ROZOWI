package pl.edu.ur.blokur.ui.views.tickets.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.TicketSummaryDto
import pl.edu.ur.blokur.ui.views.tickets.components.TicketListItem
import pl.edu.ur.blokur.ui.views.tickets.utils.toPresentation

@Composable
fun TicketListContent(
    tickets: List<TicketSummaryDto>,
    onTicketClicked: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        tickets.forEach { ticket ->
            val presentation = ticket.status.toPresentation()
            val assignedTo = ticket.assignedToName
            val dateOrAssignee = if (assignedTo != null)
                "${ticket.createdAt.take(10)} • Przypisane: $assignedTo"
            else
                "${ticket.createdAt.take(10)} • Brak przypisania"

            TicketListItem(
                title = ticket.title,
                date = dateOrAssignee,
                categoryName = ticket.categoryName,
                statusText = presentation.label,
                statusColorHex = presentation.color.value.toLong(),
                onClick = { onTicketClicked(ticket.id) }
            )
        }
    }
}
