package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.TicketImageDto

/**
 * Galeria zdjęć zgłoszenia z podziałem na BEFORE / AFTER.
 * Wyświetla metadane (nazwa pliku, data wgrania) w formie listy kafelków.
 *
 * @param images  Lista zdjęć z API
 */
@Composable
fun TicketImagesSection(
    images: List<TicketImageDto>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val before = images.filter { it.imageType == "BEFORE" }
    val after  = images.filter { it.imageType == "AFTER" }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Zdjęcia zgłoszenia (${images.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (before.isNotEmpty()) {
            ImageGroupSection("Przed pracami", before, Color(0xFF1565C0))
        }
        if (after.isNotEmpty()) {
            ImageGroupSection("Po pracach", after, Color(0xFF2E7D32))
        }
    }
}

@Composable
private fun ImageGroupSection(
    label: String,
    images: List<TicketImageDto>,
    labelColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
            fontWeight = FontWeight.SemiBold
        )
        images.forEach { img ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Placeholder ikona obrazka
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(labelColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", style = MaterialTheme.typography.titleMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        img.originalFilename ?: "Zdjęcie",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    img.uploadedAt?.let { at ->
                        val formatted = try {
                            val p = at.split("T")
                            if (p.size == 2) "${p[0]}, ${p[1].take(5)}" else at
                        } catch (_: Exception) { at }
                        Text(
                            formatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
