package pl.edu.ur.blokur.presentation.auth.screen

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.domain.model.UserRole
import pl.edu.ur.blokur.presentation.auth.content.LoginForm
import pl.edu.ur.blokur.presentation.auth.util.AuthEvent
import pl.edu.ur.blokur.presentation.auth.viewmodel.AuthViewModel
import pl.edu.ur.blokur.presentation.common.theme.GradientEnd
import pl.edu.ur.blokur.presentation.common.theme.GradientStart

/**
 * Ekran logowania – composable najwyższego poziomu dla trasy [pl.edu.ur.blokur.presentation.auth.AuthRoutes.Login].
 *
 * Odpowiada za:
 * - podpięcie do [AuthViewModel] (stan + zdarzenia),
 * - wyświetlenie tła gradientowego, tytułu aplikacji i [LoginForm],
 * - obsługę jednorazowych zdarzeń nawigacyjnych ([AuthEvent.NavigateToMain])
 *   i błędów ([AuthEvent.ShowError]) przez Snackbar.
 *
 * @param viewModel       ViewModel wstrzykiwany przez Hilt.
 * @param onLoginSuccess  callback wywoływany po udanym logowaniu; przekazuje rolę
 *                        użytkownika, by nawigacja mogła wybrać właściwy panel.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formFields by viewModel.formFields.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> onLoginSuccess(event.role)
                is AuthEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
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
                text = "Blokur",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Zaloguj się do swojego konta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LoginForm(
                state = state,
                loginFormFields = formFields,
                onLoginFormChange = viewModel::onFormChanged,
                onLoginClicked = viewModel::login
            )

            Spacer(modifier = Modifier.height(48.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
        )
    }
}
