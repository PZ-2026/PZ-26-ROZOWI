package pl.edu.ur.blokur.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.ur.blokur.ui.theme.PreviewTheme

@Composable
fun Snackbar(
    hostState: SnackbarHostState
) {
    SnackbarHost(
        hostState = hostState,
        snackbar = { snackbarData: SnackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                actionColor = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BlokurSnackbarHostPreview() {
    PreviewTheme {
        val hostState = remember { SnackbarHostState() }

        LaunchedEffect(Unit) {
            hostState.showSnackbar("To jest przykładowy snackbar")
        }

        Snackbar(hostState = hostState)
    }
}