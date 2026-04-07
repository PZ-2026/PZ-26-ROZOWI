package pl.edu.ur.blokur.presentation.profile.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.presentation.common.component.LoadingIndicator
import pl.edu.ur.blokur.presentation.common.component.NormalCard
import pl.edu.ur.blokur.presentation.common.component.PrimaryButton
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme
import pl.edu.ur.blokur.presentation.profile.util.ProfileState

@Composable
fun ProfileContent(
    state: ProfileState,
    showSaveDialog: Boolean,
    onNameChanged: (String) -> Unit,
    onRequestSave: () -> Unit,
    onConfirmSave: () -> Unit,
    onDismissDialog: () -> Unit,
    onSendNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is ProfileState.Loading -> LoadingIndicator()
        is ProfileState.Data -> ProfileDataContent(
            data = state,
            showSaveDialog = showSaveDialog,
            onNameChanged = onNameChanged,
            onRequestSave = onRequestSave,
            onConfirmSave = onConfirmSave,
            onDismissDialog = onDismissDialog,
            onSendNotification = onSendNotification,
            modifier = modifier
        )
    }
}

@Composable
private fun ProfileDataContent(
    data: ProfileState.Data,
    showSaveDialog: Boolean,
    onNameChanged: (String) -> Unit,
    onRequestSave: () -> Unit,
    onConfirmSave: () -> Unit,
    onDismissDialog: () -> Unit,
    onSendNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text("Potwierdzenie") },
            text = { Text("Czy chcesz zapisać dane użytkownika?") },
            confirmButton = {
                TextButton(onClick = onConfirmSave) { Text("Zapisz") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) { Text("Anuluj") }
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        NormalCard {
            Text(
                "Dane użytkownika",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Zmień podstawowe informacje profilu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = data.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Imię i nazwisko") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Zapisz zmiany",
                onClick = onRequestSave,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrimaryButton(
                text = "Wyślij powiadomienie testowe",
                onClick = onSendNotification,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentLoadingPreview() {
    PreviewTheme { ProfileContent(ProfileState.Loading, false, {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentDataPreview() {
    PreviewTheme {
        ProfileContent(
            state = ProfileState.Data(name = "Jan Kowalski"),
            showSaveDialog = false,
            onNameChanged = {},
            onRequestSave = {},
            onConfirmSave = {},
            onDismissDialog = {},
            onSendNotification = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentDialogPreview() {
    PreviewTheme {
        ProfileContent(
            state = ProfileState.Data(name = "Jan Kowalski"),
            showSaveDialog = true,
            onNameChanged = {},
            onRequestSave = {},
            onConfirmSave = {},
            onDismissDialog = {},
            onSendNotification = {}
        )
    }
}
