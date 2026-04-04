package pl.edu.ur.blokur.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.ur.blokur.ui.theme.BlokurPreviewTheme

@Composable
fun BlokurSnackbarHost(hostState: SnackbarHostState) {
    SnackbarHost(
        hostState = hostState,
        snackbar = { snackbarData: SnackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium,
            )
        },
    )
}

@Composable
fun BlokurAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Anuluj",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            BlokurPrimaryButton(
                text = confirmText,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            BlokurSecondaryButton(
                text = dismissText,
                onClick = onDismiss,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun BlokurDialogPreview() {
    BlokurPreviewTheme {
        BlokurAlertDialog(
            title = "Potwierdzenie",
            message = "Czy na pewno chcesz zapisać zmiany?",
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlokurSnackbarHostPreview() {
    BlokurPreviewTheme {
        val hostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            hostState.showSnackbar("To jest przykładowy snackbar")
        }

        BlokurSnackbarHost(hostState = hostState)
    }
}
