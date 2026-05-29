package pl.edu.ur.blokur.ui.views.auth.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.edu.ur.blokur.ui.views.auth.utils.AuthState
import pl.edu.ur.blokur.ui.views.auth.utils.LoginFormFields
import pl.edu.ur.blokur.ui.components.PrimaryButton
import pl.edu.ur.blokur.ui.theme.PreviewTheme

@Composable
fun LoginForm(
    state: AuthState,
    loginFormFields: LoginFormFields,
    onLoginFormChange: (LoginFormFields) -> Unit,
    onLoginClicked: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val isLoading = state is AuthState.Loading
    val isLocked = state is AuthState.AccountLocked

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Zaloguj się",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Banner konta zablokowanego (HTTP 423)
        if (isLocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = (state as AuthState.AccountLocked).message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        OutlinedTextField(
            value = loginFormFields.email,
            onValueChange = { s ->
                onLoginFormChange(loginFormFields.copy(email = s))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Adres e-mail") },
            singleLine = true,
            enabled = !isLoading && !isLocked,
            isError = state is AuthState.Error,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = loginFormFields.password,
            onValueChange = { s ->
                onLoginFormChange(loginFormFields.copy(password = s))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hasło") },
            singleLine = true,
            enabled = !isLoading && !isLocked,
            isError = state is AuthState.Error,
            visualTransformation = if (loginFormFields.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClicked()
                }
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onLoginFormChange(loginFormFields.copy(passwordVisible = !loginFormFields.passwordVisible))
                    }
                ) {
                    Icon(
                        imageVector = if (loginFormFields.passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (loginFormFields.passwordVisible) "Ukryj hasło" else "Pokaż hasło"
                    )
                }
            },
            supportingText = if (state is AuthState.Error) {
                { Text(state.message, color = MaterialTheme.colorScheme.error) }
            } else null,
            shape = MaterialTheme.shapes.medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword) {
                Text(
                    text = "Zapomniałem hasła",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        PrimaryButton(
            text = if (isLoading) "Logowanie..." else "Zaloguj się",
            onClick = onLoginClicked,
            enabled = !isLoading && !isLocked && loginFormFields.email.isNotBlank() && loginFormFields.password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormIdlePreview() {
    PreviewTheme {
        LoginForm(
            state = AuthState.Idle,
            loginFormFields = LoginFormFields("", "", false),
            onLoginFormChange = {},
            onLoginClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormLoadingPreview() {
    PreviewTheme {
        LoginForm(
            state = AuthState.Loading,
            loginFormFields = LoginFormFields("test@test.pl", "haslo123", false),
            onLoginFormChange = {},
            onLoginClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormErrorPreview() {
    PreviewTheme {
        LoginForm(
            state = AuthState.Error("Nieprawidłowy e-mail lub hasło"),
            loginFormFields = LoginFormFields("wrong@test.pl", "wrong", false),
            onLoginFormChange = {},
            onLoginClicked = {}
        )
    }
}