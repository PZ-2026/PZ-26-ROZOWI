package pl.edu.ur.blokur.presentation.finances.content

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.FinancialDocument
import pl.edu.ur.blokur.presentation.common.component.EmptyState
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme
import pl.edu.ur.blokur.presentation.finances.component.DocumentItem
import pl.edu.ur.blokur.presentation.finances.util.FinancesState

@Composable
fun DocumentsContent(
    state: FinancesState,
    onDownload: (FinancialDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is FinancesState.Loading -> LoadingIndicator()
        is FinancesState.Error -> EmptyState(title = "Błąd", description = state.message)
        is FinancesState.Data -> DocumentsListContent(
            documents = state.documents,
            onDownload = onDownload,
            modifier = modifier
        )
    }
}

@Composable
private fun DocumentsListContent(
    documents: List<FinancialDocument>,
    onDownload: (FinancialDocument) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${documents.size} dokumentów",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(documents) { document ->
            DocumentItem(
                document = document,
                onDownload = { onDownload(document) }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsLoadingPreview() {
    PreviewTheme { DocumentsContent(FinancesState.Loading, onDownload = {}) }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsErrorPreview() {
    PreviewTheme { DocumentsContent(FinancesState.Error("Błąd pobierania"), onDownload = {}) }
}
