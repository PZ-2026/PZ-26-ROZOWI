package pl.edu.ur.blokur.presentation.common.theme

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