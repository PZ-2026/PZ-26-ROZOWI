package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.PreviewTheme

@Composable
fun NormalCard(
    content: @Composable () -> Unit = { Text("NORMAL CARD WITH TEXT BLOCK")}
) {
    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun HighlightCard(
    content: @Composable () -> Unit = { Text("HIGLIGHT CARD WITH TEXT BLOCK")}
) {
    Card(
        modifier = Modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlokurCardPreview() {
    PreviewTheme {
        NormalCard() {
            Text("Zawartość zwykłej karty")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BlokurHighlightCardPreview() {
    PreviewTheme {
        HighlightCard() {
            Text("Zawartość wyróżnionej karty")
        }
    }
}