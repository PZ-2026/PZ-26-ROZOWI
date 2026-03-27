package pl.edu.ur.blokur.ui.android.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.android.common.components.CommonEmptyState
import pl.edu.ur.blokur.ui.android.common.components.CommonFloatingActionButton
import pl.edu.ur.blokur.ui.android.common.components.CommonHighlightCard
import pl.edu.ur.blokur.ui.android.common.components.CommonLoadingIndicator
import pl.edu.ur.blokur.ui.android.common.components.CommonNormalCard
import pl.edu.ur.blokur.ui.android.common.components.CommonPrimaryButton
import pl.edu.ur.blokur.ui.android.common.components.CommonSecondaryButton
import pl.edu.ur.blokur.ui.android.common.components.CommonStatusBadge
import pl.edu.ur.blokur.ui.android.common.components.CommonTagBadge
import pl.edu.ur.blokur.ui.android.common.components.CommonTextField
import pl.edu.ur.blokur.ui.android.common.components.CommonTopBar
import pl.edu.ur.blokur.ui.android.common.popups.AlertDialogPopup
import pl.edu.ur.blokur.ui.android.common.popups.SnackbarPopup

@Preview(showBackground = true)
@Composable
private fun PreviewCommonComponents(
    darkTheme: Boolean = false,
) {
    BlokurTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier.padding(40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CommonTopBar()
            //Text field dont show anything when placed below loading indicator or empty state TD: fix or write why
            CommonTextField()

            CommonTagBadge()
            CommonStatusBadge()

            CommonPrimaryButton()
            CommonSecondaryButton()
            CommonFloatingActionButton()

            CommonNormalCard()
            CommonHighlightCard()

            CommonEmptyState()

            CommonLoadingIndicator()


        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCommonPopups(
    darkTheme: Boolean = false,
) {
    BlokurTheme(darkTheme = darkTheme) {
        Column(
            modifier = Modifier.padding(40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlertDialogPopup(onConfirm = {}, onDismiss = {})

            val hostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                hostState.showSnackbar("EXAMPLE SNACKBAR")
            }

            //this is not showing for some reason...
            SnackbarPopup(hostState = hostState)
        }
    }
}


