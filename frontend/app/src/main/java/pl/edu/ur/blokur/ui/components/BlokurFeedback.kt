package pl.edu.ur.blokur.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BlokurSnackbarHost(
    hostState: SnackbarHostState
) {
    SnackbarHost(hostState = hostState)
}

@Composable
fun BlokurAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Anuluj"
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
            BlokurPrimaryButton(
                text = confirmText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            BlokurSecondaryButton(
                text = dismissText,
                onClick = onDismiss
            )
        }
    )
}