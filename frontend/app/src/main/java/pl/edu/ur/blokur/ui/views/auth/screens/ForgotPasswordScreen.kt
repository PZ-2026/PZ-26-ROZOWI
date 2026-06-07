package pl.edu.ur.blokur.ui.views.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.theme.GradientEnd
import pl.edu.ur.blokur.ui.theme.GradientStart
import pl.edu.ur.blokur.ui.views.auth.contents.ForgotPasswordForm
import pl.edu.ur.blokur.ui.views.auth.utils.ForgotPasswordEvent
import pl.edu.ur.blokur.ui.views.auth.viewmodels.ForgotPasswordViewModel

/**
 * Ekran „Zapomniałem hasła" — użytkownik podaje e-mail, na który zostanie
 * wysłany link resetujący.
 */
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formFields by viewModel.formFields.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotPasswordEvent.NavigateBack -> onNavigateBack()
                is ForgotPasswordEvent.ShowSnackbar -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "BlokUR",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Odzyskiwanie dostępu",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            ForgotPasswordForm(
                state = state,
                formFields = formFields,
                onFormChanged = viewModel::onFormChanged,
                onSubmit = viewModel::submit,
                onNavigateBack = viewModel::onNavigateBack
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
