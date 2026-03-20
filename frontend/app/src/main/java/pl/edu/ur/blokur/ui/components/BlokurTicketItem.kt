package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.GradientEnd
import pl.edu.ur.blokur.ui.theme.GradientStart
import pl.edu.ur.blokur.ui.theme.Stroke

@Composable
fun BlokurTicketItem(
    title: String,
    date: String,
    categoryText: String,
    statusText: String,
    statusColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.Build
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(5.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x1A4F46E5)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa kolorowa belka statusu
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(90.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(statusColor, statusColor.copy(alpha = 0.3f))
                        ),
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Ikona w tinted circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Tekst
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState())
                    ) {
                        BlokurTagBadge(text = categoryText)
                        BlokurStatusBadge(text = statusText, dotColor = statusColor)
                    }
                }

                // Strzałka
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Szczegóły",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurTicketItemPreview() {
    BlokurPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BlokurTicketItem(
                title = "Przeciek pod umywalką w łazience",
                date = "12 Paź 2026, 14:30",
                categoryText = "Hydraulika",
                statusText = "W realizacji",
                statusColor = Color(0xFFD97706),
                onClick = {}
            )
            BlokurTicketItem(
                title = "Awaria windy na 3 piętrze",
                date = "10 Paź 2026, 09:00",
                categoryText = "Mechanika",
                statusText = "Zakończone",
                statusColor = Color(0xFF059669),
                onClick = {}
            )
        }
    }
}