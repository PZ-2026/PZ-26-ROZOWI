package pl.edu.ur.blokur.ui.views.tickets.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.PreviewTheme
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketFormState
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketFormContent(
    formState: CreateTicketFormState,
    submitState: CreateTicketSubmitState,
    categories: List<String>,
    onFormChanged: (CreateTicketFormState) -> Unit,
    onSubmitClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubmitting = submitState is CreateTicketSubmitState.Submitting

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(modifier = Modifier.height(4.dp))

        FormLabel("Tytuł zgłoszenia")
        OutlinedTextField(
            value = formState.title,
            onValueChange = { if (it.length <= 100) onFormChanged(formState.copy(title = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Np. Brak ciepłej wody") },
            singleLine = true,
            enabled = !isSubmitting,
            supportingText = { Text("${formState.title.length}/100") },
            shape = RoundedCornerShape(12.dp)
        )

        FormLabel("Kategoria")
        ExposedDropdownMenuBox(
            expanded = formState.isCategoryExpanded,
            onExpandedChange = { onFormChanged(formState.copy(isCategoryExpanded = it)) }
        ) {
            OutlinedTextField(
                value = formState.selectedCategory.ifBlank { "Wybierz kategorię" },
                onValueChange = {},
                readOnly = true,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formState.isCategoryExpanded) },
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = formState.isCategoryExpanded,
                onDismissRequest = { onFormChanged(formState.copy(isCategoryExpanded = false)) }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onFormChanged(formState.copy(selectedCategory = category, isCategoryExpanded = false))
                        }
                    )
                }
            }
        }

        FormLabel("Opis")
        OutlinedTextField(
            value = formState.description,
            onValueChange = { if (it.length <= 2000) onFormChanged(formState.copy(description = it)) },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            placeholder = { Text("Dokładnie opisz problem...") },
            maxLines = 5,
            enabled = !isSubmitting,
            supportingText = { Text("${formState.description.length}/2000") },
            shape = RoundedCornerShape(12.dp)
        )

        if (submitState is CreateTicketSubmitState.Error) {
            Text(
                text = submitState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        FormLabel("Zdjęcia (opcjonalnie)")
        PhotoPlaceholderRow()

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSubmitClicked,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = formState.title.isNotBlank()
                    && formState.selectedCategory.isNotBlank()
                    && formState.description.isNotBlank()
                    && !isSubmitting,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (isSubmitting) "Wysyłanie..." else "Zgłoś usterkę",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PhotoPlaceholderRow() {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Dodaj", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTicketFormIdlePreview() {
    PreviewTheme {
        CreateTicketFormContent(
            formState = CreateTicketFormState(),
            submitState = CreateTicketSubmitState.Idle,
            categories = listOf("Hydraulika", "Elektryka", "Inne"),
            onFormChanged = {},
            onSubmitClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTicketFormSubmittingPreview() {
    PreviewTheme {
        CreateTicketFormContent(
            formState = CreateTicketFormState(title = "Brak wody", selectedCategory = "Hydraulika", description = "Opis..."),
            submitState = CreateTicketSubmitState.Submitting,
            categories = listOf("Hydraulika", "Elektryka"),
            onFormChanged = {},
            onSubmitClicked = {}
        )
    }
}
