package pl.edu.ur.blokur.ui.screens.finances.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.data.model.DocumentType
import pl.edu.ur.blokur.data.model.FinancialDocumentDto
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurTagBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange

private data class DocumentPresentation(
    val label: String,
    val color: Color,
    val icon: ImageVector,
)

private fun documentPresentation(type: DocumentType) =
    when (type) {
        DocumentType.NALICZENIE -> DocumentPresentation("Naliczenie", InfoBlue, Icons.Rounded.Receipt)
        DocumentType.ROZLICZENIE -> DocumentPresentation("Rozliczenie", SuccessGreen, Icons.Rounded.BarChart)
        DocumentType.ZAWIADOMIENIE -> DocumentPresentation("Zawiadomienie", WarningOrange, Icons.Rounded.Campaign)
        DocumentType.FAKTURA -> DocumentPresentation("Faktura", ErrorRed, Icons.Rounded.Receipt)
        DocumentType.INNE -> DocumentPresentation("Inne", InfoBlue, Icons.Rounded.Description)
    }

@Composable
fun DocumentItem(
    document: FinancialDocumentDto,
    onDownload: () -> Unit,
) {
    val presentation = documentPresentation(document.type)

    BlokurCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(46.dp)
                        .background(presentation.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    tint = presentation.color,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BlokurTagBadge(text = presentation.label)
                    Text(
                        text = "${document.issueYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Pobierz PDF",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
