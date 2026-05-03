package pl.edu.ur.blokur.ui.views.meters.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.meters.viewmodels.CreateReadingFormState

@Composable
fun CreateMeterReadingDialog(
    formState: CreateReadingFormState,
    onDismiss: () -> Unit,
    onValueChanged: (String) -> Unit,
    onReadingDateChanged: (String) -> Unit,
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
                Icon(Icons.Rounded.EditNote, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Dodaj odczyt", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Wprowadź nowy stan licznika", style = MaterialTheme.typography.bodySmall,
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
                    value = formState.value,
                    onValueChange = onValueChanged,
                    label = { Text("Wartość (stan)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                OutlinedTextField(
                    value = formState.readingDate,
                    onValueChange = onReadingDateChanged,
                    label = { Text("Data odczytu") },
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
                AnimatedContent(formState.isSubmitting, label = "btn_submit_reading") { submitting ->
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
