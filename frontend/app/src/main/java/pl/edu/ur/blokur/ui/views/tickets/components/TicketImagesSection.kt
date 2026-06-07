package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.TicketImageDto

/**
 * Galeria zdjęć zgłoszenia z podziałem na BEFORE / AFTER.
 */
@Composable
fun TicketImagesSection(
    images: List<TicketImageDto>,
    isLoading: Boolean = false,
    isUploading: Boolean = false,
    showUploadAfter: Boolean = false,
    onAddAfterPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val before = images.filter { it.imageType == "BEFORE" }
    val after = images.filter { it.imageType == "AFTER" }
    var galleryIndex by remember { mutableStateOf<Int?>(null) }

    galleryIndex?.let { index ->
        TicketImageGalleryDialog(
            images = images,
            initialIndex = index,
            onDismiss = { galleryIndex = null }
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Zdjęcia zgłoszenia (${images.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }
            images.isEmpty() && !showUploadAfter -> {
                Text(
                    "Brak zdjęć do tego zgłoszenia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                if (before.isNotEmpty()) {
                    ImageGroupSection(
                        label = "Przed pracami",
                        images = before,
                        labelColor = Color(0xFF1565C0),
                        onImageClick = { galleryIndex = images.indexOf(it) }
                    )
                }
                if (after.isNotEmpty()) {
                    ImageGroupSection(
                        label = "Po pracach",
                        images = after,
                        labelColor = Color(0xFF2E7D32),
                        onImageClick = { galleryIndex = images.indexOf(it) }
                    )
                }
                if (images.isEmpty() && showUploadAfter) {
                    Text(
                        "Brak zdjęć. Dodaj dokumentację po zakończeniu prac.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showUploadAfter) {
            OutlinedButton(
                onClick = onAddAfterPhoto,
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                }
                Text(
                    if (isUploading) "Wgrywanie…" else "Dodaj zdjęcie po pracach",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ImageGroupSection(
    label: String,
    images: List<TicketImageDto>,
    labelColor: Color,
    onImageClick: (TicketImageDto) -> Unit
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
                TicketImageThumbnail(
                    imageId = img.id,
                    onClick = { onImageClick(img) }
                )
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
                        } catch (_: Exception) {
                            at
                        }
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
