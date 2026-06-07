package pl.edu.ur.blokur.ui.views.inspections.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.ui.platform.LocalContext
import pl.edu.ur.blokur.dtos.ScopeType
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.CreateInspectionFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInspectionDialog(
    formState: CreateInspectionFormState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onScheduledAtChanged: (String) -> Unit,
    onScopeTypeChanged: (ScopeType) -> Unit,
    onScopeIdChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Zapisz"
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var tempDateMillis by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Dalej") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anuluj") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tempDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                            set(java.util.Calendar.SECOND, 0)
                        }
                        val formatted = String.format(
                            java.util.Locale.US,
                            "%04d-%02d-%02dT%02d:%02d:00",
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH),
                            cal.get(java.util.Calendar.HOUR_OF_DAY),
                            cal.get(java.util.Calendar.MINUTE)
                        )
                        onScheduledAtChanged(formatted)
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Anuluj") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

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
                Icon(Icons.Rounded.Build, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column {
                Text("Zaplanuj przegląd", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Wprowadź dane przeglądu technicznego.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tytuł
                OutlinedTextField(
                    value = formState.title,
                    onValueChange = onTitleChanged,
                    label = { Text("Tytuł (np. Przegląd gazowy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Opis
                OutlinedTextField(
                    value = formState.description,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                )

                // Data
                OutlinedTextField(
                    value = formState.scheduledAt,
                    onValueChange = onScheduledAtChanged,
                    label = { Text("Planowana data") },
                    placeholder = { Text("Wybierz planowaną datę...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    readOnly = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }, enabled = !formState.isSubmitting) {
                            Icon(Icons.Rounded.DateRange, "Wybierz datę")
                        }
                    },
                    supportingText = { Text("Format: RRRR-MM-DDTGG:MM:SS") }
                )

                Spacer(Modifier.height(4.dp))
                
                // Zasięg - Typ
                Text("Zasięg przeglądu", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ScopeType.NIERUCHOMOSC, ScopeType.BUDYNEK, ScopeType.KLATKA).forEach { type ->
                        FilterChip(
                            selected = formState.scopeType == type,
                            onClick = { onScopeTypeChanged(type) },
                            label = { Text(type.label, maxLines = 1) },
                            enabled = !formState.isSubmitting
                        )
                    }
                }

                // Zasięg - Wybór obiektu
                if (formState.availableScopes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        formState.availableScopes.forEach { (id, name) ->
                            val selected = formState.scopeId == id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(.5f))
                                    .border(1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(10.dp))
                                    .clickable { onScopeIdChanged(id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { onScopeIdChanged(id) })
                                Spacer(Modifier.width(8.dp))
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = formState.scopeId,
                        onValueChange = onScopeIdChanged,
                        label = { Text("ID obiektu (UUID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !formState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedContent(formState.isSubmitting, label = "btn") { submitting ->
                    if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Text(confirmLabel)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !formState.isSubmitting) { Text("Anuluj") }
        }
    )
}
