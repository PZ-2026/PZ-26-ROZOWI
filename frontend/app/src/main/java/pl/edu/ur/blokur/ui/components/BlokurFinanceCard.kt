package pl.edu.ur.blokur.ui.components

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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme
import pl.edu.ur.blokur.ui.theme.ErrorRed
import pl.edu.ur.blokur.ui.theme.GradientEnd
import pl.edu.ur.blokur.ui.theme.GradientStart
import pl.edu.ur.blokur.ui.theme.SuccessGreen
import pl.edu.ur.blokur.ui.theme.White

@Composable
fun BlokurFinanceCard(
    balance: String,
    dateText: String,
    modifier: Modifier = Modifier,
    isDebt: Boolean = false
) {
    val gradient = if (isDebt) {
        Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFF87171)))
    } else {
        Brush.linearGradient(listOf(GradientStart, GradientEnd))
    }
    val iconBg = White.copy(alpha = 0.20f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation    = 8.dp,
                shape        = RoundedCornerShape(24.dp),
                spotColor    = if (isDebt) ErrorRed.copy(alpha = 0.30f)
                               else GradientStart.copy(alpha = 0.30f),
                ambientColor = Color(0x0F000000)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text  = "Bieżące saldo",
                    style = MaterialTheme.typography.titleSmall,
                    color = White.copy(alpha = 0.80f)
                )
                Text(
                    text  = balance,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = White
                )
                Text(
                    text     = "Stan na: $dateText",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint               = White,
                    modifier           = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8F9FF)
@Composable
private fun BlokurFinanceCardPreview() {
    BlokurPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BlokurFinanceCard(balance = "124,50 PLN",  dateText = "17 Marzec 2026")
            BlokurFinanceCard(balance = "-450,00 PLN", dateText = "17 Marzec 2026", isDebt = true)
        }
    }
}