package pl.edu.ur.blokur.ui.views.finances.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.UserDocumentDto
import pl.edu.ur.blokur.ui.components.EmptyState
import pl.edu.ur.blokur.ui.components.LoadingIndicator
import pl.edu.ur.blokur.ui.views.finances.components.DocumentItem
import pl.edu.ur.blokur.ui.views.finances.utils.FinancesState

@Composable
fun DocumentsContent(
    state: FinancesState,
    onDownload: (UserDocumentDto) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is FinancesState.Loading -> LoadingIndicator()
        is FinancesState.Error -> EmptyState(title = "Błąd", description = state.message)
        is FinancesState.Data -> {
            if (state.documents.isEmpty()) {
                EmptyState(
                    title = "Brak dokumentów",
                    description = "Nie masz jeszcze żadnych dokumentów."
                )
            } else {
                LazyColumn(
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${state.documents.size} dokumentów",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(state.documents, key = { it.id }) { document ->
                        DocumentItem(
                            document = document,
                            onDownload = { onDownload(document) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
