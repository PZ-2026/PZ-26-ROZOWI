package pl.edu.ur.blokur.ui.views.finances.components

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
import pl.edu.ur.blokur.dtos.UserDocumentDto
import pl.edu.ur.blokur.ui.components.NormalCard
import pl.edu.ur.blokur.ui.components.TagBadge
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.InfoBlue
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.WarningOrange

private data class DocumentPresentation(val label: String, val color: Color, val icon: ImageVector)

private fun String?.toPresentation() = when (this?.uppercase()) {
    "NALICZENIE"   -> DocumentPresentation("Naliczenie", InfoBlue, Icons.Rounded.Receipt)
    "ROZLICZENIE"  -> DocumentPresentation("Rozliczenie", SuccessGreen, Icons.Rounded.BarChart)
    "ZAWIADOMIENIE"-> DocumentPresentation("Zawiadomienie", WarningOrange, Icons.Rounded.Campaign)
    "FAKTURA"      -> DocumentPresentation("Faktura", ErrorRed, Icons.Rounded.Receipt)
    "PROTOKOL"     -> DocumentPresentation("Protokół", InfoBlue, Icons.Rounded.Description)
    "UCHWALA"      -> DocumentPresentation("Uchwała", WarningOrange, Icons.Rounded.Description)
    else           -> DocumentPresentation("Dokument", InfoBlue, Icons.Rounded.Description)
}

@Composable
fun DocumentItem(
    document: UserDocumentDto,
    onDownload: () -> Unit
) {
    val presentation = document.type.toPresentation()

    NormalCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(presentation.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    presentation.icon,
                    contentDescription = null,
                    tint = presentation.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    document.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagBadge(text = presentation.label)
                    document.createdAt?.let { date ->
                        Text(
                            text = date.take(10), // "YYYY-MM-DD"
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = "Pobierz PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
