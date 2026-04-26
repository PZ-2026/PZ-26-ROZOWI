package pl.edu.ur.blokur.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun PreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    PresentationTheme(darkTheme = darkTheme) {
        content()
    }
}