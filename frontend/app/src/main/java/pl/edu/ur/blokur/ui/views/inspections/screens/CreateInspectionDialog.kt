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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.dtos.ScopeType
import pl.edu.ur.blokur.ui.views.inspections.viewmodels.CreateInspectionFormState

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
                    placeholder = { Text("np. 2026-06-15T08:00:00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !formState.isSubmitting,
                    shape = RoundedCornerShape(12.dp),
                    supportingText = { Text("Format: RRRR-MM-DDTGG:MM:SS") }
                )

                Spacer(Modifier.height(4.dp))
                
                // Zasięg - Typ
                Text("Zasięg przeglądu", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ScopeType.BUDYNEK, ScopeType.KLATKA).forEach { type ->
                        FilterChip(
                            selected = formState.scopeType == type,
                            onClick = { onScopeTypeChanged(type) },
                            label = { Text(type.label) },
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
                enabled = formState.isValid && !formState.isSubmitting,
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
