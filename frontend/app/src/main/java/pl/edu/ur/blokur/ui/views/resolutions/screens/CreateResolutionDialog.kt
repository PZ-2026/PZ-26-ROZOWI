package pl.edu.ur.blokur.ui.views.resolutions.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.resolutions.viewmodels.CreateResolutionFormState

@Composable
fun CreateResolutionDialog(
    formState: CreateResolutionFormState,
    onDismiss: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onEndDateChanged: (String) -> Unit,
    onBuildingChanged: (String) -> Unit,
    onOptionChanged: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
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
                Icon(Icons.Rounded.Gavel, null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column {
                Text("Nowe głosowanie", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Utwórz uchwałę dla wspólnoty.", style = MaterialTheme.typography.bodySmall,
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
                    label = { Text("Tytuł uchwały") },
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
                    label = { Text("Opis / treść uchwały") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                )

                // Data zakończenia
                OutlinedTextField(
                    value = formState.endDate,
                    onValueChange = onEndDateChanged,
                    label = { Text("Data zakończenia") },
                    placeholder = { Text("np. 2026-05-15T23:59:59") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Format: RRRR-MM-DDTGG:MM:SS") }
                )

                // Budynek (jeśli jest więcej niż jeden – dropdown, inaczej pole)
                if (formState.availableBuildings.size > 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Budynek", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        formState.availableBuildings.forEach { (id, name) ->
                            val selected = formState.targetBuildingId == id
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
                                    .clickable { onBuildingChanged(id) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { onBuildingChanged(id) })
                                Spacer(Modifier.width(8.dp))
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = formState.targetBuildingId,
                        onValueChange = onBuildingChanged,
                        label = { Text("ID Budynku (UUID)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !formState.isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Opcje głosowania
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Opcje głosowania (min. 2, max. 10)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (formState.options.size < 10) {
                            IconButton(
                                onClick = onAddOption,
                                modifier = Modifier.size(32.dp),
                                enabled = !formState.isSubmitting
                            ) {
                                Icon(Icons.Rounded.Add, "Dodaj opcję",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    formState.options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { onOptionChanged(index, it) },
                                label = { Text("Opcja ${index + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !formState.isSubmitting,
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (formState.options.size > 2) {
                                IconButton(
                                    onClick = { onRemoveOption(index) },
                                    enabled = !formState.isSubmitting
                                ) {
                                    Icon(Icons.Rounded.Close, "Usuń",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = formState.isValid && !formState.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                AnimatedContent(formState.isSubmitting, label = "btn") { submitting ->
                    if (submitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Utwórz głosowanie")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !formState.isSubmitting) { Text("Anuluj") }
        }
    )
}
