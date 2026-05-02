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
import androidx.compose.material3.CircularProgressIndicator
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
import pl.edu.ur.blokur.dtos.CategoryDto
import pl.edu.ur.blokur.ui.theme.PreviewTheme
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketFormState
import pl.edu.ur.blokur.ui.views.tickets.utils.CreateTicketSubmitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketFormContent(
    formState: CreateTicketFormState,
    submitState: CreateTicketSubmitState,
    categories: List<CategoryDto>,
    categoriesLoading: Boolean,
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
        if (categoriesLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else {
            ExposedDropdownMenuBox(
                expanded = formState.isCategoryExpanded,
                onExpandedChange = { onFormChanged(formState.copy(isCategoryExpanded = it)) }
            ) {
                OutlinedTextField(
                    value = formState.selectedCategoryName.ifBlank { "Wybierz kategorię" },
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
                            text = { Text(category.name) },
                            onClick = {
                                onFormChanged(
                                    formState.copy(
                                        selectedCategoryId = category.id,
                                        selectedCategoryName = category.name,
                                        isCategoryExpanded = false
                                    )
                                )
                            }
                        )
                    }
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Brak dostępnych kategorii", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            onClick = { onFormChanged(formState.copy(isCategoryExpanded = false)) }
                        )
                    }
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
                    && formState.selectedCategoryId.isNotBlank()
                    && formState.description.isNotBlank()
                    && !isSubmitting
                    && !categoriesLoading,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Zgłoś usterkę",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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
            categories = listOf(CategoryDto("1", "Hydraulika"), CategoryDto("2", "Elektryka")),
            categoriesLoading = false,
            onFormChanged = {},
            onSubmitClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateTicketFormLoadingCategoriesPreview() {
    PreviewTheme {
        CreateTicketFormContent(
            formState = CreateTicketFormState(),
            submitState = CreateTicketSubmitState.Idle,
            categories = emptyList(),
            categoriesLoading = true,
            onFormChanged = {},
            onSubmitClicked = {}
        )
    }
}
