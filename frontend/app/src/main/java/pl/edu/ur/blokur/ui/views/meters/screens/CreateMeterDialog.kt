package pl.edu.ur.blokur.ui.views.meters.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.MediumType
import pl.edu.ur.blokur.ui.views.meters.viewmodels.CreateMeterFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeterDialog(
    formState: CreateMeterFormState,
    onDismiss: () -> Unit,
    onSerialNumberChanged: (String) -> Unit,
    onMediumTypeChanged: (MediumType) -> Unit,
    onInstallationDateChanged: (String) -> Unit,
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
                Icon(Icons.Rounded.Speed, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Dodaj licznik", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Zarejestruj nowe urządzenie pomiarowe", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = formState.serialNumber,
                    onValueChange = onSerialNumberChanged,
                    label = { Text("Numer seryjny licznika") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Typ medium", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    // Używamy siatki przycisków lub FlowRow dla MediumType
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediumType.entries.take(2).forEach { type ->
                            FilterChip(
                                selected = formState.mediumType == type,
                                onClick = { onMediumTypeChanged(type) },
                                label = { Text(type.label) },
                                enabled = !formState.isSubmitting,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MediumType.entries.drop(2).forEach { type ->
                            FilterChip(
                                selected = formState.mediumType == type,
                                onClick = { onMediumTypeChanged(type) },
                                label = { Text(type.label) },
                                enabled = !formState.isSubmitting,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.installationDate,
                    onValueChange = onInstallationDateChanged,
                    label = { Text("Data montażu") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Format: RRRR-MM-DD") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = formState.isValid && !formState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedContent(formState.isSubmitting, label = "btn_submit_meter") { submitting ->
                    if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Zapisz")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !formState.isSubmitting) { Text("Anuluj") }
        }
    )
}
