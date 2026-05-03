package pl.edu.ur.blokur.ui.views.finances.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.finances.viewmodels.AddTransactionFormState

@Composable
fun AddTransactionDialog(
    formState: AddTransactionFormState,
    onDismiss: () -> Unit,
    onTypeChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!formState.isSubmitting) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AttachMoney, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column {
                Text("Nowa operacja", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Dodaj transakcję do kartoteki lokalu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Typ transakcji ────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Typ operacji", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        formState.availableTypes.forEach { (key, label) ->
                            val selected = formState.type == key
                            val chipColor = when (key) {
                                "WPLATA" -> MaterialTheme.colorScheme.primary
                                "NALICZENIE" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) chipColor.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp,
                                        if (selected) chipColor else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(10.dp))
                                    .clickable(enabled = !formState.isSubmitting) { onTypeChanged(key) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // ── Kwota ─────────────────────────────────────────────────────
                OutlinedTextField(
                    value = formState.amount,
                    onValueChange = onAmountChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kwota (PLN)") },
                    leadingIcon = { Icon(Icons.Rounded.AttachMoney, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Wpłata: +, Naliczenie/Korekta: ±") }
                )

                // ── Opis ──────────────────────────────────────────────────────
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Opis operacji") },
                    leadingIcon = { Icon(Icons.Rounded.Description, null, modifier = Modifier.size(18.dp)) },
                    maxLines = 3,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                )

                // ── Data ──────────────────────────────────────────────────────
                OutlinedTextField(
                    value = formState.transactionDate,
                    onValueChange = onDateChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Data (RRRR-MM-DD)") },
                    leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("np. 2026-05-03") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = formState.isValid && !formState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedContent(formState.isSubmitting, label = "btn") { submitting ->
                    if (submitting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Dodaj operację")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !formState.isSubmitting) {
                Text("Anuluj")
            }
        }
    )
}
