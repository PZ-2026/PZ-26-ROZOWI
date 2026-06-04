package pl.edu.ur.blokur.ui.views.announcements.contents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.announcements.viewmodels.CreateAnnouncementState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAnnouncementContent(
    state: CreateAnnouncementState,
    snackbarHostState: SnackbarHostState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPickAttachment: () -> Unit,
    onRemoveAttachment: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Dodaj ogłoszenie") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Tytuł ogłoszenia") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = state.content,
                onValueChange = onContentChange,
                label = { Text("Treść ogłoszenia") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default
                )
            )

            if (state.attachmentUri == null) {
                OutlinedButton(
                    onClick = onPickAttachment,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting
                ) {
                    Text("Dodaj załącznik (PDF)")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Wybrano: ${state.attachmentName}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemoveAttachment, enabled = !state.isSubmitting) {
                        Icon(Icons.Rounded.Close, contentDescription = "Usuń załącznik")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isSubmitting && state.isValid
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Opublikuj ogłoszenie")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
