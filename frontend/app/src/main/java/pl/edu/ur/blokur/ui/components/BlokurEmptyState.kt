package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme

@Composable
fun BlokurEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    emoji: String = "📭"
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = emoji,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Text(
            text      = title,
            style     = MaterialTheme.typography.titleLarge,
            color     = MaterialTheme.colorScheme.onBackground,
            modifier  = Modifier.padding(top = 28.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text      = description,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier  = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurEmptyStatePreview() {
    BlokurPreviewTheme {
        BlokurEmptyState(
            title       = "Brak zgłoszeń",
            description = "Gdy pojawi się nowe zgłoszenie serwisowe, wyświetlimy je tutaj.",
            emoji       = "📋"
        )
    }
}