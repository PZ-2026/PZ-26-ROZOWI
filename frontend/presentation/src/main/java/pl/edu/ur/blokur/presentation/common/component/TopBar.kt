package pl.edu.ur.blokur.presentation.common.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.edu.ur.blokur.presentation.common.theme.PreviewTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String = "TOP BAR"
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun BlokurTopBarPreview() {
    PreviewTheme {
        TopBar(title = "Profil")
    }
}