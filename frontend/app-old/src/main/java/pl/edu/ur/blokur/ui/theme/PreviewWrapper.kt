package pl.edu.ur.blokur.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun BlokurPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    BlokurTheme(darkTheme = darkTheme) {
        content()
    }
}
