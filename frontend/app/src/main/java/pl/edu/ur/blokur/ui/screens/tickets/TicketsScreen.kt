package pl.edu.ur.blokur.ui.screens.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.components.BlokurCard
import pl.edu.ur.blokur.ui.components.BlokurFab
import pl.edu.ur.blokur.ui.components.BlokurHighlightCard
import pl.edu.ur.blokur.ui.components.BlokurPrimaryButton
import pl.edu.ur.blokur.ui.components.BlokurStatusChip
import pl.edu.ur.blokur.ui.components.BlokurTagChip
import pl.edu.ur.blokur.ui.components.BlokurTopBar
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TicketsScreen() {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BlokurTopBar(title = "Aplikacja")
        },
        floatingActionButton = {
            BlokurFab(onClick = { })
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            BlokurHighlightCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Wyróżniony element",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Wykorzystuje \"Primary container\" jako tło oraz \"Primary\" dla tekstu i akcentów.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            BlokurCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Zwykła karta",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tło \"Surface\", nagłówek \"Text primary\", treść \"Text secondary\".",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BlokurTagChip(text = "Secondary tag")
                    BlokurTagChip(text = "Opcja")
                }
            }

            BlokurCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kolory statusów",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BlokurStatusChip("Success", Color(0xFF059669))
                    BlokurStatusChip("Warning", Color(0xFFD97706))
                    BlokurStatusChip("Error", Color(0xFFDC2626))
                    BlokurStatusChip("Info", Color(0xFF2563EB))
                }
            }

            BlokurPrimaryButton(
                text = "Główna akcja (Primary)",
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}