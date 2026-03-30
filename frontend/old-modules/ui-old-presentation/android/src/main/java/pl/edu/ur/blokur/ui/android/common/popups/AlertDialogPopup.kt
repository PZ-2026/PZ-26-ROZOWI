package pl.edu.ur.blokur.ui.android.common.popups

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import pl.edu.ur.blokur.ui.android.common.components.CommonPrimaryButton
import pl.edu.ur.blokur.ui.android.common.components.CommonSecondaryButton

@Composable
fun AlertDialogPopup(
    title: String = "ALERT TITLE TEXT",
    message: String = "ALERT MESSAGE",
    confirmText: String = "ALERT CONFIRM BUTTON TEXT",
    dismissText: String = "ALERT DISMISS BUTTON TEXT",
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
            CommonPrimaryButton(
                text = confirmText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            CommonSecondaryButton(
                text = dismissText,
                onClick = onDismiss
            )
        }
    )
}