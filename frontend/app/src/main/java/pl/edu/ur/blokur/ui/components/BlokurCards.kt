package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.GradientEnd
import pl.edu.ur.blokur.ui.theme.GradientStart
import pl.edu.ur.blokur.ui.theme.Stroke

@Composable
fun BlokurCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = Stroke,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun BlokurHighlightCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        GradientStart.copy(alpha = 0.08f),
                        GradientEnd.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(GradientStart.copy(alpha = 0.35f), GradientEnd.copy(alpha = 0.20f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurCardPreview() {
    BlokurPreviewTheme {
        BlokurCard(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Zawartość zwykłej karty", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurHighlightCardPreview() {
    BlokurPreviewTheme {
        BlokurHighlightCard(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Zawartość wyróżnionej karty", style = MaterialTheme.typography.bodyMedium)
        }
    }
}