package pl.edu.ur.blokur.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.ur.blokur.ui.theme.PreviewTheme


@Composable
fun AlertDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            PrimaryButton(
                text = confirmText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            SecondaryButton(
                text = dismissText,
                onClick = onDismiss
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AlertDialogPreview() {
    PreviewTheme {
        AlertDialog(
            title = "Potwierdzenie",
            message = "Czy na pewno chcesz zapisać zmiany?",
            confirmText = "OK",
            dismissText = "Anuluj",
            onConfirm = {},
            onDismiss = {}
        )
    }
}