package pl.edu.ur.blokur.ui.views.tickets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.TicketCommentDto

/**
 * Sekcja komentarzy do zgłoszenia — lista wpisów + formularz dodania.
 *
 * @param comments     Lista komentarzy pobranych z API
 * @param currentRole  Rola zalogowanego użytkownika (ZARZADCA / KONSERWATOR / MIESZKANIEC)
 * @param isLoading    Czy trwa ładowanie komentarzy
 * @param onAddComment Callback wywoływany po wysłaniu komentarza (treść, typ: PUBLICZNY|WEWNETRZNY)
 */
@Composable
fun TicketCommentsSection(
    comments: List<TicketCommentDto>,
    currentRole: String,
    isLoading: Boolean,
    onAddComment: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var isInternal by remember { mutableStateOf(false) }
    val canToggleInternal = currentRole == "ZARZADCA" || currentRole == "KONSERWATOR"

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Komentarze (${comments.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (comments.isEmpty()) {
            Text(
                "Brak komentarzy. Bądź pierwszy!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                comments.forEach { comment ->
                    CommentBubble(comment = comment)
                }
            }
        }

        // Formularz dodania komentarza
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Napisz komentarz...") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                val type = if (isInternal) "WEWNETRZNY" else "PUBLICZNY"
                                onAddComment(commentText.trim(), type)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Rounded.Send,
                            contentDescription = "Wyślij",
                            tint = if (commentText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            if (canToggleInternal) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Komentarz wewnętrzny",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isInternal,
                        onCheckedChange = { isInternal = it },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentBubble(comment: TicketCommentDto) {
    val isInternal = comment.isInternal
    val bgColor = if (isInternal)
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    comment.authorName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isInternal) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = "Wewnętrzny",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Text(
                        formatCommentDate(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatCommentDate(iso: String?): String {
    if (iso == null) return ""
    return try {
        val parts = iso.split("T")
        if (parts.size == 2) "${parts[0]}, ${parts[1].take(5)}" else iso
    } catch (_: Exception) { iso ?: "" }
}
