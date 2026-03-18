package pl.edu.ur.blokur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.GradientEnd
import pl.edu.ur.blokur.ui.theme.GradientStart

@Composable
fun BlokurPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(listOf(GradientStart, GradientEnd))

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) gradient
                else Brush.linearGradient(
                    listOf(
                        Color(0xFFD1D5DB),
                        Color(0xFFD1D5DB)
                    )
                )
            )
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor   = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor   = Color(0xFF9CA3AF)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
        ) {
            Text(
                text  = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) Color.White else Color(0xFF9CA3AF)
            )
        }
    }
}

@Composable
fun BlokurSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = Color(0xFF9CA3AF),
            disabledContainerColor = Color(0xFFF3F4F6)
        )
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun BlokurFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "+"
) {
    val gradient = Brush.linearGradient(listOf(GradientStart, GradientEnd))

    Surface(
        onClick = onClick,
        modifier = modifier.size(60.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 10.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurButtonsPreview() {
    BlokurPreviewTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BlokurPrimaryButton("Zapisz zmiany", onClick = {}, modifier = Modifier.fillMaxWidth())
            BlokurSecondaryButton("Anuluj", onClick = {}, modifier = Modifier.fillMaxWidth())
            BlokurPrimaryButton("Wyłączony", onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurFabPreview() {
    BlokurPreviewTheme {
        Row(modifier = Modifier.padding(20.dp)) {
            BlokurFab(onClick = {})
        }
    }
}